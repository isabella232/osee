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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.osee.database.schema.DatabaseTxCallable;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.ConflictStatus;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.model.TransactionRecordFactory;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.change.ArtifactChangeItem;
import org.eclipse.osee.framework.core.model.change.AttributeChangeItem;
import org.eclipse.osee.framework.core.model.change.ChangeItem;
import org.eclipse.osee.framework.core.model.change.RelationChangeItem;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.jdk.core.util.time.GlobalTime;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.db.internal.accessor.UpdatePreviousTxCurrent;

/**
 * @author Ryan D. Brooks
 */
public class CommitBranchDatabaseTxCallable extends DatabaseTxCallable<TransactionRecord> {
   private static final String COMMIT_COMMENT = "Commit Branch ";

   private static final String INSERT_COMMIT_TRANSACTION =
      "insert into osee_tx_details(tx_type, branch_id, transaction_id, osee_comment, time, author, commit_art_id) values(?,?,?,?,?,?,?)";

   private static final String INSERT_COMMIT_ADDRESSING =
      "insert into osee_txs(transaction_id, branch_id, gamma_id, mod_type, tx_current) values(?,?,?,?,?)";

   private static final String UPDATE_CONFLICT_STATUS =
      "update osee_conflict SET status = ? WHERE status = ? AND merge_branch_id = ?";

   private static final String UPDATE_MERGE_COMMIT_TX =
      "update osee_merge set commit_transaction_id = ? Where source_branch_id = ? and dest_branch_id = ?";

   private static final String SELECT_SOURCE_BRANCH_STATE =
      "select (1) from osee_branch where branch_id=? and branch_state=?";

   private static final String UPDATE_SOURCE_BRANCH_STATE = "update osee_branch set branch_state=? where branch_id=?";

   private final int userArtId;
   private final BranchCache branchCache;
   private final Map<Branch, BranchState> savedBranchStates;
   private final Branch sourceBranch;
   private final Branch destinationBranch;
   private final Branch mergeBranch;
   private final List<ChangeItem> changes;
   private final TransactionRecordFactory txFactory;

   private OseeConnection connection;
   private boolean success;

   public CommitBranchDatabaseTxCallable(Log logger, IOseeDatabaseService databaseService, BranchCache branchCache, int userArtId, Branch sourceBranch, Branch destinationBranch, Branch mergeBranch, List<ChangeItem> changes, TransactionRecordFactory txFactory) {
      super(logger, databaseService, "Commit branch");
      this.savedBranchStates = new HashMap<Branch, BranchState>();
      this.branchCache = branchCache;
      this.userArtId = userArtId;
      this.sourceBranch = sourceBranch;
      this.destinationBranch = destinationBranch;
      this.mergeBranch = mergeBranch;
      this.changes = changes;
      this.txFactory = txFactory;

      this.success = true;
      savedBranchStates.put(sourceBranch, sourceBranch.getBranchState());
      savedBranchStates.put(destinationBranch, destinationBranch.getBranchState());
   }

   @Override
   protected TransactionRecord handleTxWork(OseeConnection connection) throws OseeCoreException {
      BranchState storedBranchState;
      this.connection = connection;
      if (changes.isEmpty()) {
         throw new OseeStateException("A branch can not be committed without any changes made.");
      }
      storedBranchState = sourceBranch.getBranchState();
      checkPreconditions();

      TransactionRecord newTx = null;
      try {
         newTx = addCommitTransactionToDatabase(userArtId);
         updatePreviousCurrentsOnDestinationBranch();
         insertCommitAddressing(newTx);

         getDatabaseService().runPreparedUpdate(connection, UPDATE_MERGE_COMMIT_TX, newTx.getId(),
            sourceBranch.getId(), destinationBranch.getId());

         manageBranchStates();
      } catch (OseeCoreException ex) {
         updateBranchState(storedBranchState);
         throw ex;
      }
      return newTx;
   }

   public synchronized void checkPreconditions() throws OseeCoreException {
      int count =
         getDatabaseService().runPreparedQueryFetchObject(0, SELECT_SOURCE_BRANCH_STATE, sourceBranch.getId(),
            BranchState.COMMIT_IN_PROGRESS.getValue());
      if (sourceBranch.getBranchState().isCommitInProgress() || sourceBranch.getArchiveState().isArchived() || count > 0) {
         throw new OseeStateException("Commit completed or in progress for [%s]", sourceBranch);
      }

      if (!sourceBranch.getBranchState().equals(BranchState.COMMITTED)) {
         updateBranchState(BranchState.COMMIT_IN_PROGRESS);
         sourceBranch.setBranchState(BranchState.COMMIT_IN_PROGRESS);
      }
   }

   public void updateBranchState(BranchState state) throws OseeCoreException {
      getDatabaseService().runPreparedUpdate(UPDATE_SOURCE_BRANCH_STATE, state.getValue(), sourceBranch.getId());
   }

   private void updatePreviousCurrentsOnDestinationBranch() throws OseeCoreException {
      UpdatePreviousTxCurrent updater =
         new UpdatePreviousTxCurrent(getDatabaseService(), connection, destinationBranch.getId());
      for (ChangeItem change : changes) {
         if (change instanceof ArtifactChangeItem) {
            updater.addArtifact(change.getItemId());
         } else if (change instanceof AttributeChangeItem) {
            updater.addAttribute(change.getItemId());
         } else if (change instanceof RelationChangeItem) {
            updater.addRelation(change.getItemId());
         } else {
            throw new OseeStateException("Unexpected change type");
         }
      }
      updater.updateTxNotCurrents();
   }

   @SuppressWarnings("unchecked")
   private TransactionRecord addCommitTransactionToDatabase(int userArtId) throws OseeCoreException {
      int newTransactionNumber = getDatabaseService().getSequence().getNextTransactionId();

      Timestamp timestamp = GlobalTime.GreenwichMeanTimestamp();
      String comment = COMMIT_COMMENT + sourceBranch.getName();

      getDatabaseService().runPreparedUpdate(connection, INSERT_COMMIT_TRANSACTION,
         TransactionDetailsType.NonBaselined.getId(), destinationBranch.getId(), newTransactionNumber, comment,
         timestamp, userArtId, sourceBranch.getAssociatedArtifactId());
      TransactionRecord record =
         txFactory.create(newTransactionNumber, destinationBranch.getId(), comment, timestamp, userArtId,
            sourceBranch.getAssociatedArtifactId(), TransactionDetailsType.NonBaselined, branchCache);

      return record;
   }

   private void insertCommitAddressing(TransactionRecord newTx) throws OseeCoreException {
      List<Object[]> insertData = new ArrayList<Object[]>();
      for (ChangeItem change : changes) {
         ModificationType modType = change.getNetChange().getModType();
         insertData.add(new Object[] {
            newTx.getId(),
            destinationBranch.getId(),
            change.getNetChange().getGammaId(),
            modType.getValue(),
            TxChange.getCurrent(modType).getValue()});
      }
      getDatabaseService().runBatchUpdate(connection, INSERT_COMMIT_ADDRESSING, insertData);
   }

   private void manageBranchStates() throws OseeCoreException {
      destinationBranch.setBranchState(BranchState.MODIFIED);
      BranchState sourceBranchState = sourceBranch.getBranchState();
      if (!sourceBranchState.isCreationInProgress() && !sourceBranchState.isRebaselined() && !sourceBranchState.isRebaselineInProgress() && !sourceBranchState.isCommitted()) {
         sourceBranch.setBranchState(BranchState.COMMITTED);
      }
      if (mergeBranch != null) {
         savedBranchStates.put(mergeBranch, mergeBranch.getBranchState());
         mergeBranch.setBranchState(BranchState.COMMITTED);
         branchCache.storeItems(mergeBranch, destinationBranch, sourceBranch);
      } else {
         branchCache.storeItems(destinationBranch, sourceBranch);
      }
   }

   @Override
   protected void handleTxException(Exception ex) {
      success = false;
      // Restore Original Branch States
      try {
         for (Entry<Branch, BranchState> entry : savedBranchStates.entrySet()) {
            entry.getKey().setBranchState(entry.getValue());
         }
         branchCache.storeItems(savedBranchStates.keySet());
      } catch (OseeCoreException ex1) {
         getLogger().error(ex1, "Error during branch commit of [%s] into [%s]", sourceBranch, destinationBranch);
      }
   }

   @Override
   protected void handleTxFinally() throws OseeCoreException {
      if (success) {
         // update conflict status, if necessary
         if (mergeBranch != null) {
            getDatabaseService().runPreparedUpdate(UPDATE_CONFLICT_STATUS, ConflictStatus.COMMITTED.getValue(),
               ConflictStatus.RESOLVED.getValue(), mergeBranch.getId());
         }
      }
   }

}