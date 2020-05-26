/*********************************************************************
 * Copyright (c) 2014 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.orcs.account.admin.internal.oauth;

import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import com.google.common.io.ByteSource;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.OrcsTypesData;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.CoreTupleTypes;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.util.OseeInf;
import org.eclipse.osee.framework.jdk.core.type.LazyObject;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.transaction.TransactionBuilder;

/**
 * @author Roberto E. Escobar
 */
public class ClientStorageProvider extends LazyObject<ClientStorage> {

   private static final String OAUTH_TYPES_DEFITIONS = "orcsTypes/OseeTypes_OAuth.osee";

   private Log logger;
   private OrcsApi orcsApi;

   public void setLogger(Log logger) {
      this.logger = logger;
   }

   public void setOrcsApi(OrcsApi orcsApi) {
      this.orcsApi = orcsApi;
   }

   @Override
   protected FutureTask<ClientStorage> createLoaderTask() {
      Callable<ClientStorage> callable = new Callable<ClientStorage>() {

         @Override
         public ClientStorage call() throws Exception {
            BranchId storageBranch = CoreBranches.COMMON;
            ClientStorage clientStorage = new ClientStorage(logger, orcsApi, storageBranch);

            if (!clientStorage.typesExist()) {
               ByteSource newTypesSupplier = newTypesSupplier();
               ArtifactReadable typeArt = (ArtifactReadable) clientStorage.storeTypes(newTypesSupplier);

               TransactionBuilder tx = orcsApi.getTransactionFactory().createTransaction(COMMON, SystemUser.OseeSystem,
                  "Add OseeTypeDef OAuth Tuple to Common Branch");
               tx.addTuple2(CoreTupleTypes.OseeTypeDef, OrcsTypesData.OSEE_TYPE_VERSION,
                  typeArt.getAttributes(CoreAttributeTypes.UriGeneralStringData).iterator().next());

               tx.commit();
            }

            return clientStorage;
         }

      };
      return new FutureTask<>(callable);
   }

   private ByteSource newTypesSupplier() {
      return new ByteSource() {

         @Override
         public InputStream openStream() throws IOException {
            URL resource = OseeInf.getResourceAsUrl(OAUTH_TYPES_DEFITIONS, getClass());
            return new BufferedInputStream(resource.openStream());
         }
      };
   }
}