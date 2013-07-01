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
package org.eclipse.osee.orcs.db.internal.exchange;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.services.IdentityService;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.IOseeSequence;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.resource.management.IResourceManager;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.ExportOptions;
import org.eclipse.osee.orcs.db.internal.exchange.export.AbstractExportItem;
import org.eclipse.osee.orcs.db.internal.exchange.export.DbTableExportItem;
import org.eclipse.osee.orcs.db.internal.exchange.export.ManifestExportItem;
import org.eclipse.osee.orcs.db.internal.exchange.export.MetadataExportItem;
import org.eclipse.osee.orcs.db.internal.exchange.export.OseeTypeModelExportItem;
import org.eclipse.osee.orcs.db.internal.exchange.handler.ExportItem;
import org.eclipse.osee.orcs.db.internal.types.IOseeModelingService;

public class ExportItemFactory {
   private static final String GET_MAX_TX =
      "SELECT last_sequence FROM osee_sequence WHERE sequence_name = '" + IOseeSequence.TRANSACTION_ID_SEQ + "'";

   private static final String BRANCH_TABLE_QUERY =
      "SELECT br.* FROM osee_join_export_import jex, osee_branch br WHERE jex.query_id=? AND jex.id1=br.branch_id ORDER BY br.branch_id";

   private static final String TX_DETAILS_TABLE_QUERY =
      "SELECT txd.* FROM osee_join_export_import jex, osee_tx_details txd WHERE jex.query_id=? AND jex.id1=txd.branch_id";

   private static final String TXS_TABLE_QUERY =
      "SELECT txs.* FROM osee_join_export_import jex, osee_txs txs WHERE jex.query_id=? AND jex.id1=txs.branch_id";

   private static final String TXS_ARCHIVE_TABLE_QUERY = TXS_TABLE_QUERY.replace("osee_txs", "osee_txs_archived");

   private static final String ARTIFACT_TABLE_QUERY =
      "SELECT item.* FROM osee_join_id oji, osee_artifact item WHERE oji.query_id = ? AND oji.id = item.gamma_id";

   private static final String ATTRIBUTE_TABLE_QUERY = ARTIFACT_TABLE_QUERY.replace("osee_artifact", "osee_attribute");

   private static final String RELATION_LINK_TABLE_QUERY = ARTIFACT_TABLE_QUERY.replace("osee_artifact",
      "osee_relation_link");

   private static final String MERGE_TABLE_QUERY =
      "SELECT om.* FROM osee_join_export_import jex, osee_merge om WHERE jex.query_id=? AND jex.id1=om.merge_branch_id ORDER BY om.merge_branch_id";

   private static final String CONFLICT_TABLE_QUERY =
      "SELECT oc.* FROM osee_join_export_import jex, osee_merge om, osee_conflict oc WHERE jex.query_id=? AND jex.id1=om.merge_branch_id AND om.merge_branch_id=oc.merge_branch_id";

   private static final String ARTIFACT_ACL_QUERY =
      "SELECT aac.* FROM osee_join_export_import jex, osee_artifact_acl aac WHERE jex.query_id=? AND jex.id1=aac.branch_id ORDER BY aac.branch_id";

   private static final String BRANCH_ACL_QUERY =
      "SELECT bac.* FROM osee_join_export_import jex, osee_branch_acl bac WHERE jex.query_id=? AND jex.id1=bac.branch_id ORDER BY bac.branch_id";

   private final Log logger;
   private final IOseeDatabaseService dbService;
   private final IdentityService identityService;
   private final IOseeModelingService typeModelService;
   private final IResourceManager resourceManager;

   public ExportItemFactory(Log logger, IOseeDatabaseService dbService, IdentityService identityService, IOseeModelingService typeModelService, IResourceManager resourceManager) {
      this.logger = logger;
      this.dbService = dbService;
      this.identityService = identityService;
      this.typeModelService = typeModelService;
      this.resourceManager = resourceManager;
   }

   public Log getLogger() {
      return logger;
   }

   public IOseeDatabaseService getDbService() {
      return dbService;
   }

   public IOseeModelingService getModelingService() {
      return typeModelService;
   }

   public IResourceManager getResourceManager() {
      return resourceManager;
   }

   public IdentityService getIdentityService() {
      return identityService;
   }

   public List<AbstractExportItem> createTaskList(int joinId, PropertyStore options) throws OseeCoreException {
      List<AbstractExportItem> items = new ArrayList<AbstractExportItem>();

      processTxOptions(options);

      int gammaJoinId = createGammaJoin(getDbService(), joinId, options);

      items.add(new ManifestExportItem(logger, items, options));
      items.add(new MetadataExportItem(logger, items, getMetaData(getDbService())));
      items.add(new OseeTypeModelExportItem(logger, getModelingService()));

      addItem(items, joinId, options, gammaJoinId, ExportItem.OSEE_BRANCH_DATA, BRANCH_TABLE_QUERY);
      addItem(items, joinId, options, gammaJoinId, ExportItem.OSEE_TX_DETAILS_DATA, TX_DETAILS_TABLE_QUERY);
      addItem(items, joinId, options, gammaJoinId, ExportItem.OSEE_TXS_DATA, TXS_TABLE_QUERY);
      addItem(items, joinId, options, gammaJoinId, ExportItem.OSEE_TXS_ARCHIVED_DATA, TXS_ARCHIVE_TABLE_QUERY);
      addItem(items, joinId, options, gammaJoinId, ExportItem.OSEE_ARTIFACT_DATA, ARTIFACT_TABLE_QUERY);
      addItem(items, joinId, options, gammaJoinId, ExportItem.OSEE_ATTRIBUTE_DATA, ATTRIBUTE_TABLE_QUERY);
      addItem(items, joinId, options, gammaJoinId, ExportItem.OSEE_RELATION_LINK_DATA, RELATION_LINK_TABLE_QUERY);
      addItem(items, joinId, options, gammaJoinId, ExportItem.OSEE_MERGE_DATA, MERGE_TABLE_QUERY);
      addItem(items, joinId, options, gammaJoinId, ExportItem.OSEE_CONFLICT_DATA, CONFLICT_TABLE_QUERY);
      addItem(items, joinId, options, gammaJoinId, ExportItem.OSEE_BRANCH_ACL_DATA, BRANCH_ACL_QUERY);
      addItem(items, joinId, options, gammaJoinId, ExportItem.OSEE_ARTIFACT_ACL_DATA, ARTIFACT_ACL_QUERY);
      return items;
   }

   private void addItem(List<AbstractExportItem> items, int exportJoinId, PropertyStore options, int gammaJoinId, ExportItem exportItem, String query) throws OseeCoreException {
      StringBuilder modifiedQuery = new StringBuilder(query);
      Object[] bindData = prepareQuery(exportItem, modifiedQuery, options, exportJoinId, gammaJoinId);
      items.add(new DbTableExportItem(getLogger(), getDbService(), getIdentityService(), getResourceManager(),
         exportItem, modifiedQuery.toString(), bindData));
   }

   private void processTxOptions(PropertyStore options) throws OseeCoreException {
      long maxTx = getDbService().runPreparedQueryFetchObject(-1L, GET_MAX_TX);
      long userMaxTx = getMaxTransaction(options);
      if (userMaxTx == Long.MIN_VALUE || userMaxTx > maxTx) {
         options.put(ExportOptions.MAX_TXS.name(), Long.toString(maxTx));
      }
   }

   private int createGammaJoin(IOseeDatabaseService databaseService, int exportJoinId, PropertyStore options) throws OseeCoreException {
      List<Object> bindList = new ArrayList<Object>();
      int gammaJoinId = new Random().nextInt();
      StringBuilder sql =
         new StringBuilder(
            "INSERT INTO osee_join_id (id, query_id) SELECT DISTINCT(gamma_id), %s FROM osee_join_export_import jex, osee_txs txs WHERE jex.query_id=? AND jex.id1 = txs.branch_id");
      bindList.add(exportJoinId);
      addMaxMinFilter(sql, bindList, options);

      sql.append(" UNION SELECT DISTINCT(gamma_id), %s FROM osee_join_export_import jex, osee_txs_archived txs WHERE jex.query_id=? AND jex.id1 = txs.branch_id");
      bindList.add(exportJoinId);
      addMaxMinFilter(sql, bindList, options);

      Object[] bindData = bindList.toArray(new Object[bindList.size()]);
      String insert = String.format(sql.toString(), gammaJoinId, gammaJoinId);
      int itemsInserted = databaseService.runPreparedUpdate(insert, bindData);

      getLogger().info("Export join rows: [%s]", itemsInserted);

      return gammaJoinId;
   }

   private static Object[] prepareQuery(ExportItem exportItem, StringBuilder query, PropertyStore options, int exportJoinId, int gammaJionId) throws OseeCoreException {
      List<Object> bindData = new ArrayList<Object>();

      if (exportItem.matches(ExportItem.OSEE_ARTIFACT_DATA, ExportItem.OSEE_ATTRIBUTE_DATA,
         ExportItem.OSEE_RELATION_LINK_DATA)) {
         bindData.add(gammaJionId);
      } else {
         bindData.add(exportJoinId);
      }

      if (exportItem.matches(ExportItem.OSEE_TX_DETAILS_DATA, ExportItem.OSEE_TXS_DATA,
         ExportItem.OSEE_TXS_ARCHIVED_DATA)) {
         // this can not be accurately applied to osee_merge and osee_conflict because the best you can do is filter a the merge_branch level
         addMaxMinFilter(query, bindData, options);
      }

      if (exportItem.matches(ExportItem.OSEE_TX_DETAILS_DATA)) {
         // tx_details needs to be ordered so transactions are sequenced properly
         query.append(" ORDER BY transaction_id ASC");
      }
      return bindData.toArray(new Object[bindData.size()]);
   }

   private static void addMaxMinFilter(StringBuilder query, List<Object> bindData, PropertyStore options) throws OseeCoreException {
      long minTxs = getMinTransaction(options);
      long maxTxs = getMaxTransaction(options);

      if (minTxs > maxTxs) {
         throw new OseeArgumentException("Invalid transaction range: min - %d >  max - %d", minTxs, maxTxs);
      }

      if (minTxs != Long.MIN_VALUE) {
         query.append(" AND transaction_id >= ?");
         bindData.add(minTxs);
      }
      if (maxTxs != Long.MIN_VALUE) {
         query.append(" AND transaction_id <= ?");
         bindData.add(maxTxs);
      }
   }

   private static Long getMaxTransaction(PropertyStore options) {
      return getTransactionNumber(options, ExportOptions.MAX_TXS.name());
   }

   private static Long getMinTransaction(PropertyStore options) {
      return getTransactionNumber(options, ExportOptions.MIN_TXS.name());
   }

   private static Long getTransactionNumber(PropertyStore options, String exportOption) {
      String transactionNumber = options.get(exportOption);
      long toReturn = Long.MIN_VALUE;
      if (Strings.isValid(transactionNumber)) {
         toReturn = Long.valueOf(transactionNumber);
      }
      return toReturn;
   }

   private static DatabaseMetaData getMetaData(IOseeDatabaseService dbService) throws OseeCoreException {
      OseeConnection connection = dbService.getConnection();
      try {
         return connection.getMetaData();
      } finally {
         connection.close();
      }
   }
}
