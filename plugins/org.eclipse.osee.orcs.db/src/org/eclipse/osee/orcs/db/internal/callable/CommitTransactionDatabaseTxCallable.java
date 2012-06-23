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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.eclipse.osee.database.schema.DatabaseTxCallable;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.model.TransactionRecordFactory;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.cache.TransactionCache;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.jdk.core.util.time.GlobalTime;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.ArtifactTransactionData;
import org.eclipse.osee.orcs.core.ds.TransactionData;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.db.internal.SqlProvider;
import org.eclipse.osee.orcs.db.internal.sql.OseeSql;
import org.eclipse.osee.orcs.db.internal.transaction.BinaryStoreTx;
import org.eclipse.osee.orcs.db.internal.transaction.TxSqlBuilder;

/**
 * @author Roberto E. Escobar
 * @author Ryan D. Brooks
 * @author Jeff C. Phillips
 */
public final class CommitTransactionDatabaseTxCallable extends DatabaseTxCallable<TransactionRecord> {

   private static final String INSERT_INTO_TRANSACTION_TABLE =
      "INSERT INTO osee_txs (transaction_id, gamma_id, mod_type, tx_current, branch_id) VALUES (?, ?, ?, ?, ?)";

   private static final String INSERT_INTO_TRANSACTION_DETAIL =
      "INSERT INTO osee_tx_details (transaction_id, osee_comment, time, author, branch_id, tx_type) VALUES (?, ?, ?, ?, ?, ?)";

   private static final String UPDATE_TXS_NOT_CURRENT =
      "UPDATE osee_txs SET tx_current = " + TxChange.NOT_CURRENT.getValue() + " WHERE branch_id = ? AND transaction_id = ? AND gamma_id = ?";

   private final BranchCache branchCache;
   private final TransactionRecordFactory factory = null;
   private final TransactionCache transactionCache = null;
   private SqlProvider sqlProvider;

   private final TransactionData transactionData;
   private List<BinaryStoreTx> binaryStores;

   public CommitTransactionDatabaseTxCallable(Log logger, IOseeDatabaseService dbService, BranchCache branchCache, IOseeBranch branch, TransactionData transactionData) {
      super(logger, dbService, String.format("Committing Transaction: [%s] for branch [%s]",
         transactionData.getComment(), transactionData.getBranch()));
      this.branchCache = branchCache;
      this.transactionData = transactionData;
   }

   @Override
   protected TransactionRecord handleTxWork(OseeConnection connection) throws OseeCoreException {
      Collection<ArtifactTransactionData> txData = transactionData.getArtifactTransactionData();
      Conditions.checkNotNullOrEmpty(txData, "artifacts modified");

      Branch branch = branchCache.get(transactionData.getBranch());
      TransactionRecord txRecord =
         createTransactionRecord(branch, transactionData.getAuthor(), transactionData.getComment());
      persistTx(connection, txRecord);

      if (!txData.isEmpty()) {
         executeTransactionDataItems(txData, connection, branch);
      }

      if (branch.getBranchState() == BranchState.CREATED) {
         branch.setBranchState(BranchState.MODIFIED);
         branchCache.storeItems(branch);
      }
      return txRecord;
   }

   @Override
   protected void handleTxException(Exception ex) {
      super.handleTxException(ex);
      for (BinaryStoreTx tx : binaryStores) {
         try {
            tx.rollBack();
         } catch (OseeCoreException ex1) {
            //TX_TODO 
            getLogger().error(ex1, "Error during rollback");
         }
      }
   }

   private void executeTransactionDataItems(Collection<ArtifactTransactionData> txData, OseeConnection connection, Branch branch) throws OseeCoreException {
      TxSqlBuilder builder = new TxSqlBuilder(txData);
      builder.build();

      binaryStores = builder.getBinaryTxs();
      for (BinaryStoreTx tx : binaryStores) {
         tx.persist();
      }

      List<Object[]> txNotCurrentData = new ArrayList<Object[]>();
      for (OseeSql sql : builder.getTxSql()) {
         for (Object[] params : builder.getTxParameters(sql)) {
            fetchTxNotCurrent(connection, branch, txNotCurrentData, sql, params);
         }
      }
      //         // Collect inserts for attribute, relation, artifact, and artifact version tables
      //         transactionData.addInsertToBatch(this);
      //
      //         // Collect stale tx currents for batch update
      //         fetchTxNotCurrent(connection, branch, transactionData, txNotCurrentData);
      //      }

      // Insert into data tables - i.e. attribute, relation and artifact version tables
      for (String sql : builder.getObjectSql()) {
         getDatabaseService().runBatchUpdate(connection, sql, builder.getObjectParameters(sql));
      }

      // Set stale tx currents in txs table
      getDatabaseService().runBatchUpdate(connection, UPDATE_TXS_NOT_CURRENT, txNotCurrentData);
   }

   //   private void x(int transactionNumber, int branchId) {
   //      long gammaId = getGammaId();
   //      ModificationType modTypeToStore = getAdjustedModificationType();
   //      TxChange txChange = TxChange.getCurrent(modTypeToStore);
   //
   //      internalAddInsertToBatch(collector, Integer.MAX_VALUE, INSERT_INTO_TRANSACTION_TABLE, transactionNumber, gammaId,
   //         modTypeToStore.getValue(), txChange.getValue(), branchId);
   //   }

   @SuppressWarnings("unchecked")
   private void persistTx(OseeConnection connection, TransactionRecord transactionRecord) throws OseeCoreException {
      getDatabaseService().runPreparedUpdate(connection, INSERT_INTO_TRANSACTION_DETAIL, transactionRecord.getId(),
         transactionRecord.getComment(), transactionRecord.getTimeStamp(), transactionRecord.getAuthor(),
         transactionRecord.getBranchId(), transactionRecord.getTxType().getId());
   }

   private TransactionRecord createTransactionRecord(Branch branch, ArtifactReadable author, String comment) throws OseeCoreException {
      Integer transactionNumber = getDatabaseService().getSequence().getNextTransactionId();
      if (comment == null) {
         comment = "";
      }
      int authorArtId = author.getLocalId();
      TransactionDetailsType txType = TransactionDetailsType.NonBaselined;
      Date transactionTime = GlobalTime.GreenwichMeanTimestamp();

      int branchId = branch.getId();
      return factory.createOrUpdate(transactionCache, transactionNumber, branchId, comment, transactionTime,
         authorArtId, -1, txType, branchCache);
   }

   private void fetchTxNotCurrent(OseeConnection connection, Branch branch, List<Object[]> results, OseeSql sql, Object[] params) throws OseeCoreException {
      IOseeStatement chStmt = getDatabaseService().getStatement(connection);
      try {
         String query = sqlProvider.getSql(sql);

         chStmt.runPreparedQuery(query, params);
         while (chStmt.next()) {
            results.add(new Object[] {branch.getId(), chStmt.getInt("transaction_id"), chStmt.getLong("gamma_id")});
         }
      } finally {
         chStmt.close();
      }
   }

   //   private void updateModifiedCachedObject() throws OseeCoreException {
   //      ArtifactEvent artifactEvent = new ArtifactEvent(transactionRecord.getBranch());
   //      artifactEvent.setTransactionId(getTransactionNumber());
   //
   //      // Update all transaction items before collecting events
   //      for (BaseTransactionData transactionData : txDatas) {
   //         transactionData.internalUpdate(transactionRecord);
   //      }
   //
   //      // Collect events before clearing any dirty flags
   //      for (BaseTransactionData transactionData : txDatas) {
   //         transactionData.internalAddToEvents(artifactEvent);
   //      }
   //
   //      // Collect attribute events
   //      for (Artifact artifact : artifactReferences) {
   //         if (artifact.hasDirtyAttributes()) {
   //            EventModifiedBasicGuidArtifact guidArt =
   //               new EventModifiedBasicGuidArtifact(artifact.getBranch().getGuid(), artifact.getArtifactType().getGuid(),
   //                  artifact.getGuid(), artifact.getDirtyFrameworkAttributeChanges());
   //            artifactEvent.getArtifacts().add(guidArt);
   //
   //            // Collection relation reorder records for events
   //            if (!artifact.getRelationOrderRecords().isEmpty()) {
   //               artifactEvent.getRelationOrderRecords().addAll(artifact.getRelationOrderRecords());
   //            }
   //         }
   //      }
   //
   //      // Clear all dirty flags
   //      for (BaseTransactionData transactionData : txDatas) {
   //         transactionData.internalClearDirtyState();
   //      }
   //
   //      // Clear all relation order records
   //      for (Artifact artifact : artifactReferences) {
   //         artifact.getRelationOrderRecords().clear();
   //      }
   //
   //      if (!artifactEvent.getArtifacts().isEmpty() || !artifactEvent.getRelations().isEmpty()) {
   //         OseeEventManager.kickPersistEvent(this, artifactEvent);
   //      }
   //   }
   //
   //   protected static int getNewAttributeId(Artifact artifact, Attribute<?> attribute) throws OseeCoreException {
   //      return ConnectionHandler.getSequence().getNextAttributeId();
   //   }

}
