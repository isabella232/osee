/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.internal.callable;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.jdbc.JdbcMigrationOptions;
import org.eclipse.osee.jdbc.JdbcMigrationResource;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.SystemPreferences;
import org.eclipse.osee.orcs.core.ds.BranchDataStore;
import org.eclipse.osee.orcs.core.ds.DataStoreConstants;
import org.eclipse.osee.orcs.core.ds.DataStoreInfo;
import org.eclipse.osee.orcs.data.CreateBranchData;
import org.eclipse.osee.orcs.db.internal.IdentityManager;
import org.eclipse.osee.orcs.db.internal.resource.ResourceConstants;
import org.eclipse.osee.orcs.db.internal.sql.RelationalConstants;
import com.google.common.base.Supplier;

/**
 * @author Roberto E. Escobar
 */
public class InitializeDatastoreCallable extends AbstractDatastoreCallable<DataStoreInfo> {

   private static final String ADD_PERMISSION =
      "INSERT INTO OSEE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) VALUES (?,?)";

   private final SystemPreferences preferences;
   private final Supplier<Iterable<JdbcMigrationResource>> schemaProvider;
   private final JdbcMigrationOptions options;
   private final IdentityManager identityService;
   private final BranchDataStore branchStore;

   public InitializeDatastoreCallable(OrcsSession session, Log logger, JdbcClient jdbcClient, IdentityManager identityService, BranchDataStore branchStore, SystemPreferences preferences, Supplier<Iterable<JdbcMigrationResource>> schemaProvider, JdbcMigrationOptions options) {
      super(logger, session, jdbcClient);
      this.identityService = identityService;
      this.branchStore = branchStore;
      this.preferences = preferences;
      this.schemaProvider = schemaProvider;
      this.options = options;
   }

   @Override
   public DataStoreInfo call() throws Exception {
      Conditions.checkExpressionFailOnTrue(getJdbcClient().getConfig().isProduction(),
         "Error - attempting to initialize a production datastore.");

      getJdbcClient().migrate(options, schemaProvider.get());

      String attributeDataPath = ResourceConstants.getAttributeDataPath(preferences);
      getLogger().info("Deleting application server binary data [%s]...", attributeDataPath);
      Lib.deleteDir(new File(attributeDataPath));

      preferences.putValue(DataStoreConstants.DATASTORE_ID_KEY, GUID.create());

      addDefaultPermissions();

      clearStateCaches();

      //      boolean doesSystemRootExist = cacheService.getBranchCache().existsByGuid(CoreBranches.SYSTEM_ROOT.getGuid());
      //      Conditions.checkExpressionFailOnTrue(doesSystemRootExist, "System Root branch already exists.");

      CreateBranchData systemRootData = getSystemRootData();

      // TODO tie in the session information
      Callable<Void> createSystemRoot = branchStore.createBranch(getSession(), systemRootData);
      callAndCheckForCancel(createSystemRoot);

      Callable<DataStoreInfo> fetchCallable =
         new FetchDatastoreInfoCallable(getLogger(), getSession(), getJdbcClient(), schemaProvider, preferences);
      DataStoreInfo dataStoreInfo = callAndCheckForCancel(fetchCallable);
      return dataStoreInfo;

   }

   private CreateBranchData getSystemRootData() {
      CreateBranchData data = new CreateBranchData();

      data.setUuid(CoreBranches.SYSTEM_ROOT.getGuid());
      data.setName(CoreBranches.SYSTEM_ROOT.getName());
      data.setUuid(CoreBranches.SYSTEM_ROOT.getUuid());
      data.setBranchType(BranchType.SYSTEM_ROOT);

      String creationComment = String.format("%s Creation", CoreBranches.SYSTEM_ROOT.getName());
      data.setCreationComment(creationComment);

      data.setFromTransaction(null);

      data.setMergeAddressingQueryId(RelationalConstants.JOIN_QUERY_ID_SENTINEL);
      return data;
   }

   private void clearStateCaches() throws OseeDataStoreException {
      identityService.invalidateIds();
   }

   private void addDefaultPermissions() throws OseeCoreException {
      List<Object[]> data = new LinkedList<Object[]>();
      for (PermissionEnum permission : PermissionEnum.values()) {
         data.add(new Object[] {permission.getPermId(), permission.getName()});
      }
      getJdbcClient().runBatchUpdate(ADD_PERMISSION, data);
   }
}
