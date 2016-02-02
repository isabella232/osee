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
import java.util.List;
import org.eclipse.osee.framework.core.data.ApplicabilityId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.ConflictStatus;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.core.model.change.ChangeItem;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.time.GlobalTime;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.jdbc.JdbcConnection;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.data.BranchReadable;
import org.eclipse.osee.orcs.db.internal.IdentityManager;
import org.eclipse.osee.orcs.db.internal.accessor.UpdatePreviousTxCurrent;
import org.eclipse.osee.orcs.db.internal.sql.join.SqlJoinFactory;

/**
 * @author Ryan D. Brooks
 */
public class CommitBranchDatabaseTxCallable extends AbstractDatastoreTxCallable<Integer> {
   private static final String COMMIT_COMMENT = "Commit Branch ";

   private static final String INSERT_COMMIT_TRANSACTION =
      "insert into osee_tx_details(tx_type, branch_id, transaction_id, osee_comment, time, author, commit_art_id) values(?,?,?,?,?,?,?)";

   private static final String INSERT_COMMIT_ADDRESSING =
      "insert into osee_txs(transaction_id, branch_id, gamma_id, mod_type, tx_current, app_id) values(?,?,?,?,?,?)";

   private static final String UPDATE_CONFLICT_STATUS =
      "update osee_conflict SET status = ? WHERE status = ? AND merge_branch_id = ?";

   private static final String UPDATE_MERGE_COMMIT_TX =
      "update osee_merge set commit_transaction_id = ? Where source_branch_id = ? and dest_branch_id = ?";

   private static final String SELECT_SOURCE_BRANCH_STATE =
      "select (1) from osee_branch where branch_id=? and branch_state=?";

   private static final String UPDATE_SOURCE_BRANCH_STATE = "update osee_branch set branch_state=? where branch_id=?";

   private final SqlJoinFactory joinFactory;
   private final IdentityManager idManager;
   private final int userArtId;
   private final BranchReadable sourceBranch;
   private final BranchReadable destinationBranch;
   private final BranchId mergeBranch;
   private final List<ChangeItem> changes;

   public CommitBranchDatabaseTxCallable(Log logger, OrcsSession session, JdbcClient jdbcClient, SqlJoinFactory joinFactory, IdentityManager idManager, int userArtId, BranchReadable sourceBranch, BranchReadable destinationBranch, BranchId mergeBranch, List<ChangeItem> changes) {
      super(logger, session, jdbcClient);
      this.joinFactory = joinFactory;
      this.idManager = idManager;
      this.userArtId = userArtId;
      this.sourceBranch = sourceBranch;
      this.destinationBranch = destinationBranch;
      this.mergeBranch = mergeBranch;
      this.changes = changes;
   }

   @Override
   protected Integer handleTxWork(JdbcConnection connection) throws OseeCoreException {
      BranchState storedBranchState;
      if (changes.isEmpty()) {
         throw new OseeStateException("A branch can not be committed without any changes made.");
      }
      storedBranchState = sourceBranch.getBranchState();
      checkPreconditions();

      Integer newTx = null;
      try {
         newTx = addCommitTransactionToDatabase(userArtId, connection);
         updatePreviousCurrentsOnDestinationBranch(connection);
         insertCommitAddressing(newTx, connection);

         getJdbcClient().runPreparedUpdate(connection, UPDATE_MERGE_COMMIT_TX, newTx, sourceBranch.getUuid(),
            destinationBranch.getUuid());

         manageBranchStates();
         if (mergeBranch.isValid()) {
            getJdbcClient().runPreparedUpdate(UPDATE_CONFLICT_STATUS, ConflictStatus.COMMITTED.getValue(),
               ConflictStatus.RESOLVED.getValue(), mergeBranch);
         }
      } catch (OseeCoreException ex) {
         updateBranchState(storedBranchState, sourceBranch);
         throw ex;
      }
      return newTx;
   }

   public synchronized void checkPreconditions() throws OseeCoreException {
      int count = getJdbcClient().runPreparedQueryFetchObject(0, SELECT_SOURCE_BRANCH_STATE, sourceBranch,
         BranchState.COMMIT_IN_PROGRESS.getValue());
      if (sourceBranch.getBranchState().isCommitInProgress() || sourceBranch.getArchiveState().isArchived() || count > 0) {
         throw new OseeStateException("Commit completed or in progress for [%s]", sourceBranch);
      }

      if (!sourceBranch.getBranchState().equals(BranchState.COMMITTED)) {
         updateBranchState(BranchState.COMMIT_IN_PROGRESS, sourceBranch);
      }
   }

   public void updateBranchState(BranchState state, BranchId branchId) throws OseeCoreException {
      getJdbcClient().runPreparedUpdate(UPDATE_SOURCE_BRANCH_STATE, state.getValue(), branchId);
   }

   private void updatePreviousCurrentsOnDestinationBranch(JdbcConnection connection) throws OseeCoreException {
      UpdatePreviousTxCurrent updater =
         new UpdatePreviousTxCurrent(getJdbcClient(), joinFactory, connection, destinationBranch.getUuid());
      for (ChangeItem change : changes) {
         switch (change.getChangeType()) {
            case ARTIFACT_CHANGE:
               updater.addArtifact(change.getItemId());
               break;
            case ATTRIBUTE_CHANGE:
               updater.addAttribute(change.getItemId());
               break;
            case RELATION_CHANGE:
               updater.addRelation(change.getItemId());
               break;
            default:
               throw new OseeStateException("Unexpected change type");
         }
      }
      updater.updateTxNotCurrents();
   }

   private Integer addCommitTransactionToDatabase(int userArtId, JdbcConnection connection) throws OseeCoreException {
      int newTransactionNumber = idManager.getNextTransactionId();

      Timestamp timestamp = GlobalTime.GreenwichMeanTimestamp();
      String comment = COMMIT_COMMENT + sourceBranch.getName();

      getJdbcClient().runPreparedUpdate(connection, INSERT_COMMIT_TRANSACTION,
         TransactionDetailsType.NonBaselined.getId(), destinationBranch.getUuid(), newTransactionNumber, comment,
         timestamp, userArtId, sourceBranch.getAssociatedArtifactId());
      return newTransactionNumber;
   }

   private void insertCommitAddressing(Integer newTx, JdbcConnection connection) throws OseeCoreException {
      List<Object[]> insertData = new ArrayList<>();
      for (ChangeItem change : changes) {
         ModificationType modType = change.getNetChange().getModType();
         ApplicabilityId appId = change.getNetChange().getApplicabilityId();
         insertData.add(new Object[] {
            newTx,
            destinationBranch.getUuid(),
            change.getNetChange().getGammaId(),
            modType.getValue(),
            TxChange.getCurrent(modType).getValue(),
            appId.getId()});
      }
      getJdbcClient().runBatchUpdate(connection, INSERT_COMMIT_ADDRESSING, insertData);
   }

   private void manageBranchStates() throws OseeCoreException {
      updateBranchState(BranchState.MODIFIED, destinationBranch);

      BranchState sourceBranchState = sourceBranch.getBranchState();
      if (!sourceBranchState.isCreationInProgress() && !sourceBranchState.isRebaselined() && !sourceBranchState.isRebaselineInProgress() && !sourceBranchState.isCommitted()) {
         updateBranchState(BranchState.COMMITTED, sourceBranch);
      }
      if (mergeBranch.isValid()) {
         updateBranchState(BranchState.COMMITTED, mergeBranch);
      }
   }

}