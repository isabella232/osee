/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.internal.change;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.osee.framework.core.data.ApplicabilityId;
import org.eclipse.osee.framework.core.data.ApplicabilityToken;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactTypeId;
import org.eclipse.osee.framework.core.data.AttributeId;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.core.data.GammaId;
import org.eclipse.osee.framework.core.data.RelationId;
import org.eclipse.osee.framework.core.data.RelationTypeId;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.model.change.ChangeItem;
import org.eclipse.osee.framework.core.model.change.ChangeItemUtil;
import org.eclipse.osee.framework.jdk.core.type.DoubleKeyHashMap;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.jdbc.JdbcConstants;
import org.eclipse.osee.jdbc.JdbcStatement;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.data.TransactionTokenDelta;
import org.eclipse.osee.orcs.db.internal.callable.AbstractDatastoreCallable;
import org.eclipse.osee.orcs.db.internal.sql.join.ExportImportJoinQuery;
import org.eclipse.osee.orcs.db.internal.sql.join.SqlJoinFactory;
import org.eclipse.osee.orcs.search.ApplicabilityQuery;

/**
 * @author Ryan D. Brooks
 * @author Roberto E. Escobar
 * @author Ryan Schmitt
 * @author Jeff C. Phillips
 */
public class LoadDeltasBetweenTxsOnTheSameBranch extends AbstractDatastoreCallable<List<ChangeItem>> {

  // @formatter:off
   private static final String SELECT_ITEMS_BETWEEN_TRANSACTIONS =
      "with txsOuter as (select gamma_id, mod_type, app_id from osee_txs%s where branch_id = ? and transaction_id > ? and transaction_id <= ?) \n" +
      "SELECT 1 as table_type, attr_type_id as item_type_id, attr_id as item_id, art_id as item_first, 0 as item_second, value as item_value, item.gamma_id, mod_type, app_id \n" +
      "FROM osee_attribute item, txsOuter where txsOuter.gamma_id = item.gamma_id\n" +
      "UNION ALL\n" +
      "SELECT 2 as table_type, art_type_id as item_type_id, art_id as item_id, 0 as item_first, 0 as item_second, 'na' as item_value, item.gamma_id, mod_type, app_id \n" +
      "FROM osee_artifact item, txsOuter where txsOuter.gamma_id = item.gamma_id\n" +
      "UNION ALL\n" +
      "SELECT 3 as table_type, rel_link_type_id as item_type_id, rel_link_id as item_id,  a_art_id as item_first, b_art_id as item_second, rationale as item_value, item.gamma_id, mod_type, app_id \n" +
      "FROM osee_relation_link item, txsOuter where txsOuter.gamma_id = item.gamma_id";
   // @formatter:on
   private static final String SELECT_IS_BRANCH_ARCHIVED = "select archived from osee_branch where branch_id = ?";

   private final SqlJoinFactory joinFactory;
   private final TransactionTokenDelta txDelta;
   private final HashMap<Long, ApplicabilityToken> applicTokens;

   public LoadDeltasBetweenTxsOnTheSameBranch(Log logger, OrcsSession session, JdbcClient jdbcClient, SqlJoinFactory joinFactory, TransactionTokenDelta txDelta, ApplicabilityQuery applicQuery) {
      super(logger, session, jdbcClient);
      this.joinFactory = joinFactory;
      this.txDelta = txDelta;
      this.applicTokens = applicQuery.getApplicabilityTokens(txDelta.getStartTx().getBranch());
   }

   private ApplicabilityToken getApplicabilityToken(ApplicabilityId appId) {
      ApplicabilityToken toReturn = applicTokens.get(appId.getId());
      if (toReturn != null) {
         return toReturn;
      }
      return ApplicabilityToken.BASE;
   }

   private TransactionToken getEndTx() {
      return txDelta.getEndTx();
   }

   private TransactionToken getStartTx() {
      return txDelta.getStartTx();
   }

   @Override
   public List<ChangeItem> call() throws Exception {

      Conditions.checkExpressionFailOnTrue(!txDelta.areOnTheSameBranch(),
         "Unable to compute deltas between transactions on different branches [%s]", txDelta);

      Integer result = getJdbcClient().fetchOrException(Integer.class,
         () -> new OseeCoreException("Failed to get Branch archived state for %s", getEndTx().getBranch()),
         SELECT_IS_BRANCH_ARCHIVED, getEndTx().getBranch());
      Integer archived = result.intValue();
      boolean isArchived = archived.equals(BranchArchivedState.ARCHIVED.getValue());

      DoubleKeyHashMap<Integer, Long, ChangeItem> hashChangeData = loadChangesAtEndTx(isArchived);
      return loadItemsByItemId(hashChangeData, isArchived);
   }

   private DoubleKeyHashMap<Integer, Long, ChangeItem> loadChangesAtEndTx(boolean isArchived) throws OseeCoreException {
      DoubleKeyHashMap<Integer, Long, ChangeItem> hashChangeData = new DoubleKeyHashMap<>();
      Consumer<JdbcStatement> consumer = stmt -> {
         checkForCancelled();
         GammaId gammaId = GammaId.valueOf(stmt.getLong("gamma_id"));
         ModificationType modType = ModificationType.getMod(stmt.getInt("mod_type"));
         ApplicabilityId appId = ApplicabilityId.valueOf(stmt.getLong("app_id"));
         int tableType = stmt.getInt("table_type");
         Long itemId = stmt.getLong("item_id");
         Long itemTypeId = stmt.getLong("item_type_id");
         switch (tableType) {
            case 1: {
               ArtifactId artId = ArtifactId.valueOf(stmt.getLong("item_first"));
               String value = stmt.getString("item_value");
               hashChangeData.put(1, itemId, ChangeItemUtil.newAttributeChange(AttributeId.valueOf(itemId),
                  AttributeTypeId.valueOf(itemTypeId), artId, gammaId, modType, value, getApplicabilityToken(appId)));
               break;
            }
            case 2: {
               hashChangeData.put(2, itemId, ChangeItemUtil.newArtifactChange(ArtifactId.valueOf(itemId),
                  ArtifactTypeId.valueOf(itemTypeId), gammaId, modType, getApplicabilityToken(appId)));
               break;
            }
            case 3: {
               ArtifactId aArtId = ArtifactId.valueOf(stmt.getLong("item_first"));
               ArtifactId bArtId = ArtifactId.valueOf(stmt.getLong("item_second"));
               String rationale = stmt.getString("item_value");
               hashChangeData.put(3, itemId,
                  ChangeItemUtil.newRelationChange(RelationId.valueOf(itemId), RelationTypeId.valueOf(itemTypeId),
                     gammaId, modType, aArtId, bArtId, rationale, getApplicabilityToken(appId)));
               break;
            }
         }
      };
      String query = String.format(SELECT_ITEMS_BETWEEN_TRANSACTIONS, isArchived ? "_archived" : "");
      getJdbcClient().runQuery(consumer, JdbcConstants.JDBC__MAX_FETCH_SIZE, query, getEndTx().getBranch(),
         getStartTx(), getEndTx());

      return hashChangeData;
   }

   private List<ChangeItem> loadItemsByItemId(DoubleKeyHashMap<Integer, Long, ChangeItem> changeData, boolean isArchived) throws OseeCoreException {
      ExportImportJoinQuery idJoin = joinFactory.createExportImportJoinQuery();
      try {
         for (Integer i : changeData.getKeySetOne()) {
            for (ChangeItem item : changeData.get(i)) {
               idJoin.add(Long.valueOf(i), item.getItemId().getId());
            }
         }
         idJoin.store();
         loadCurrentVersionData(idJoin.getQueryId(), changeData, getStartTx(), isArchived);
      } finally {
         idJoin.delete();
      }
      List<ChangeItem> list = new LinkedList<ChangeItem>(changeData.allValues());
      return list;
   }

   private void loadCurrentVersionData(int queryId, DoubleKeyHashMap<Integer, Long, ChangeItem> changesByItemId, TransactionToken transactionLimit, boolean isArchived) throws OseeCoreException {

      Consumer<JdbcStatement> consumer = stmt -> {
         checkForCancelled();
         Long itemId = stmt.getLong("item_id");
         Integer tableType = stmt.getInt("table_type");
         GammaId gammaId = GammaId.valueOf(stmt.getLong("gamma_id"));
         ApplicabilityId appId = ApplicabilityId.valueOf(stmt.getLong("app_id"));
         ModificationType modType = ModificationType.getMod(stmt.getInt("mod_type"));
         ChangeItem change = changesByItemId.get(tableType, itemId);
         change.getDestinationVersion().setModType(modType);
         change.getDestinationVersion().setGammaId(gammaId);
         change.getDestinationVersion().setApplicabilityToken(getApplicabilityToken(appId));
         change.getBaselineVersion().copy(change.getDestinationVersion());
      };

      String archiveTable = isArchived ? "osee_txs_archived" : "osee_txs";
      String query = String.format(
         "select txs.gamma_id, txs.mod_type, txs.app_id, item.art_id as item_id, 2 as table_type from osee_join_export_import idj," + //
         " osee_artifact item, %s txs where idj.query_id = ? and idj.id2 = item.art_id and idj.id1 = 2" + //
         " and item.gamma_id = txs.gamma_id and txs.branch_id = ? and txs.transaction_id <= ?" + //
         " union all select txs.gamma_id, txs.mod_type, txs.app_id, item.attr_id as item_id, 1 as table_type from osee_join_export_import idj," + //
         " osee_attribute item, %s txs where idj.query_id = ? and idj.id2 = item.attr_id and idj.id1 = 1" + //
         " and item.gamma_id = txs.gamma_id and txs.branch_id = ? and txs.transaction_id <= ?" + //
         " union all select txs.gamma_id, txs.mod_type, txs.app_id, item.rel_link_id as item_id, 3 as table_type  from osee_join_export_import idj," + //
         " osee_relation_link item, %s txs where idj.query_id = ? and idj.id2 = item.rel_link_id and idj.id1 = 3" + //
         " and item.gamma_id = txs.gamma_id and txs.branch_id = ? and txs.transaction_id <= ?", archiveTable,
         archiveTable, archiveTable);

      getJdbcClient().runQuery(consumer, JdbcConstants.JDBC__MAX_FETCH_SIZE, query, queryId,
         transactionLimit.getBranch(), transactionLimit, queryId, transactionLimit.getBranch(), transactionLimit,
         queryId, transactionLimit.getBranch(), transactionLimit);
   }
}