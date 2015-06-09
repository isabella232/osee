/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.impl.internal.convert;

import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import org.eclipse.osee.ats.api.util.IAtsDatabaseConversion;
import org.eclipse.osee.ats.impl.IAtsServer;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.BranchReadable;
import org.eclipse.osee.orcs.transaction.TransactionBuilder;
import org.eclipse.osee.orcs.transaction.TransactionFactory;

/**
 * @author Megumi Telles
 */
public abstract class AbstractConvertGuidToUuid implements IAtsDatabaseConversion {

   private static final String SELECT_BRANCH_ID_BY_GUID = "select branch_id from osee_branch where branch_guid = ?";

   private final Log logger;
   private final JdbcClient jdbcClient;
   private final OrcsApi orcsApi;
   private final IAtsServer atsServer;

   public AbstractConvertGuidToUuid(Log logger, JdbcClient jdbcClient, OrcsApi orcsApi, IAtsServer atsServer) {
      super();
      this.logger = logger;
      this.jdbcClient = jdbcClient;
      this.orcsApi = orcsApi;
      this.atsServer = atsServer;
   }

   protected OrcsApi getOrcsApi() {
      return orcsApi;
   }

   protected Log getLogger() {
      return logger;
   }

   protected JdbcClient getJdbcClient() {
      return jdbcClient;
   }

   protected BranchReadable getBranch(String guid) throws OseeCoreException {
      return orcsApi.getQueryFactory().branchQuery().andUuids(getBranchIdLegacy(guid)).getResults().getExactlyOne();
   }

   protected TransactionBuilder createTransactionBuilder() throws OseeCoreException {
      TransactionFactory txFactory = getOrcsApi().getTransactionFactory();
      Conditions.checkNotNull(txFactory, "transaction factory");
      return txFactory.createTransaction(COMMON, atsServer.getArtifactByGuid(SystemUser.OseeSystem.getGuid()),
         getName());
   }

   /**
    * Temporary method till all code uses branch uuid. Remove after 0.17.0
    */
   private long getBranchIdLegacy(String branchGuid) {
      Long longId = getJdbcClient().runPreparedQueryFetchObject(0L, SELECT_BRANCH_ID_BY_GUID, branchGuid);
      Conditions.checkExpressionFailOnTrue(longId <= 0, "Error getting branch_id for branch: [%s]", branchGuid);
      return longId;
   }

}
