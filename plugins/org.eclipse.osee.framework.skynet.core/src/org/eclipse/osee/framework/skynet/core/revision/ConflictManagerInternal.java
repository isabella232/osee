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
package org.eclipse.osee.framework.skynet.core.revision;

import static org.eclipse.osee.framework.core.enums.DeletionFlag.INCLUDE_DELETED;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.enums.ConflictStatus;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.BranchMergeException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.database.core.OseeSql;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.time.GlobalTime;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoader;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.conflict.ArtifactConflictBuilder;
import org.eclipse.osee.framework.skynet.core.conflict.AttributeConflict;
import org.eclipse.osee.framework.skynet.core.conflict.AttributeConflictBuilder;
import org.eclipse.osee.framework.skynet.core.conflict.Conflict;
import org.eclipse.osee.framework.skynet.core.conflict.ConflictBuilder;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;

/**
 * @author Theron Virgin
 * @author Jeff C. Phillips
 */
public class ConflictManagerInternal {
   private static final String CONFLICT_CLEANUP =
      "DELETE FROM osee_conflict t1 WHERE merge_branch_id = ? and NOT EXISTS (SELECT 'X' FROM osee_join_artifact WHERE query_id = ? and t1.conflict_id = art_id and (t1.conflict_type = transaction_id or transaction_id is NULL))";

   private static final String GET_DESTINATION_BRANCHES =
      "SELECT dest_branch_id FROM osee_merge WHERE source_branch_id = ?";

   private static final String GET_MERGE_DATA =
      "SELECT commit_transaction_id, merge_branch_id FROM osee_merge WHERE source_branch_id = ? AND dest_branch_id = ?";

   private static final String GET_COMMIT_TRANSACTION_COMMENT =
      "SELECT transaction_id FROM osee_tx_details WHERE osee_comment = ? AND branch_id = ?";

   public static List<Conflict> getConflictsPerBranch(TransactionRecord commitTransaction, IProgressMonitor monitor) throws OseeCoreException {
      monitor.beginTask(String.format("Loading Merge Manager for Transaction %d", commitTransaction.getId()), 100);
      monitor.subTask("Finding Database stored conflicts");
      ArrayList<Conflict> conflicts = new ArrayList<Conflict>();
      IOseeStatement chStmt = ConnectionHandler.getStatement();
      try {
         chStmt.runPreparedQuery(ClientSessionManager.getSql(OseeSql.CONFLICT_GET_HISTORICAL_ATTRIBUTES),
            commitTransaction.getId());
         while (chStmt.next()) {
            AttributeConflict attributeConflict =
               new AttributeConflict(chStmt.getInt("source_gamma_id"), chStmt.getInt("dest_gamma_id"),
                  chStmt.getInt("art_id"), commitTransaction, chStmt.getString("source_value"),
                  chStmt.getInt("attr_id"), chStmt.getLong("attr_type_id"),
                  BranchManager.getBranch(chStmt.getLong("merge_branch_id")),
                  BranchManager.getBranch(chStmt.getLong("dest_branch_id")));
            attributeConflict.setStatus(ConflictStatus.valueOf(chStmt.getInt("status")));
            conflicts.add(attributeConflict);
         }
      } finally {
         chStmt.close();
         monitor.done();
      }
      return conflicts;
   }

   public static List<Conflict> getConflictsPerBranch(Branch sourceBranch, Branch destinationBranch, TransactionRecord baselineTransaction, IProgressMonitor monitor) throws OseeCoreException {
      @SuppressWarnings("unused")
      // This is for bulk loading so we do not lose our references
      Collection<Artifact> bulkLoadedArtifacts;
      List<ConflictBuilder> conflictBuilders = new ArrayList<ConflictBuilder>();
      List<Conflict> conflicts = new ArrayList<Conflict>();
      Set<Integer> artIdSet = new HashSet<Integer>();
      Set<Integer> artIdSetDontShow = new HashSet<Integer>();
      Set<Integer> artIdSetDontAdd = new HashSet<Integer>();

      // Check to see if the branch has already been committed, then use the
      // transaction version
      int commitTransactionId = getCommitTransaction(sourceBranch, destinationBranch);
      if (commitTransactionId > 0) {
         return getConflictsPerBranch(TransactionManager.getTransactionId(commitTransactionId), monitor);
      }
      if (sourceBranch == null || destinationBranch == null) {
         throw new OseeArgumentException("Source Branch = %s Destination Branch = %s",
            sourceBranch == null ? "NULL" : sourceBranch.getUuid(),
            destinationBranch == null ? "NULL" : destinationBranch.getUuid());
      }
      monitor.beginTask(
         String.format("Loading Merge Manager for Branch %d into Branch %d", sourceBranch.getUuid(),
            destinationBranch.getUuid()), 100);
      monitor.subTask("Finding Database stored conflicts");

      TransactionRecord commonTransaction = findCommonTransaction(sourceBranch, destinationBranch);

      loadArtifactVersionConflicts(ClientSessionManager.getSql(OseeSql.CONFLICT_GET_ARTIFACTS_DEST), sourceBranch,
         destinationBranch, baselineTransaction, conflictBuilders, artIdSet, artIdSetDontShow, artIdSetDontAdd,
         monitor, commonTransaction);
      loadArtifactVersionConflicts(ClientSessionManager.getSql(OseeSql.CONFLICT_GET_ARTIFACTS_SRC), sourceBranch,
         destinationBranch, baselineTransaction, conflictBuilders, artIdSet, artIdSetDontShow, artIdSetDontAdd,
         monitor, commonTransaction);
      loadAttributeConflictsNew(sourceBranch, destinationBranch, baselineTransaction, conflictBuilders, artIdSet,
         monitor, commonTransaction);
      artIdSet.removeAll(artIdSetDontAdd);
      if (artIdSet.isEmpty()) {
         return conflicts;
      }

      monitor.subTask("Creating and/or maintaining the Merge Branch");
      Branch mergeBranch =
         BranchManager.getOrCreateMergeBranch(sourceBranch, destinationBranch, new ArrayList<Integer>(artIdSet));

      if (mergeBranch == null) {
         throw new BranchMergeException("Could not create the Merge Branch.");
      }
      monitor.worked(15);

      bulkLoadedArtifacts = preloadConflictArtifacts(sourceBranch, destinationBranch, mergeBranch, artIdSet, monitor);

      // Don't create the conflicts for attributes on an artifact that is
      // deleted etc.
      for (ConflictBuilder conflictBuilder : conflictBuilders) {
         Conflict conflict = getConflict(conflictBuilder, mergeBranch, artIdSetDontShow);
         if (conflict != null) {
            conflicts.add(conflict);
         }
      }
      cleanUpConflictDB(conflicts, mergeBranch.getUuid(), monitor);
      return conflicts;
   }

   private static Conflict getConflict(ConflictBuilder conflictBuilder, Branch mergeBranch, Set<Integer> artIdSetDontShow) throws OseeCoreException {
      Conflict conflict = conflictBuilder.getConflict(mergeBranch, artIdSetDontShow);
      if (conflict != null) {
         conflict.computeStatus();
      }
      return conflict;
   }

   private static Collection<Artifact> preloadConflictArtifacts(Branch sourceBranch, Branch destBranch, Branch mergeBranch, Collection<Integer> artIdSet, IProgressMonitor monitor) throws OseeCoreException {
      monitor.subTask("Preloading Artifacts Associated with the Conflicts");

      Collection<Artifact> artifacts = ArtifactQuery.getArtifactListFromIds(artIdSet, sourceBranch, INCLUDE_DELETED);
      artifacts.addAll(ArtifactQuery.getArtifactListFromIds(artIdSet, destBranch, INCLUDE_DELETED));
      artifacts.addAll(ArtifactQuery.getArtifactListFromIds(artIdSet, mergeBranch, INCLUDE_DELETED));

      monitor.worked(25);
      return artifacts;
   }

   private static void loadArtifactVersionConflicts(String sql, Branch sourceBranch, Branch destinationBranch, TransactionRecord baselineTransaction, Collection<ConflictBuilder> conflictBuilders, Set<Integer> artIdSet, Set<Integer> artIdSetDontShow, Set<Integer> artIdSetDontAdd, IProgressMonitor monitor, TransactionRecord transactionId) throws OseeCoreException {
      boolean hadEntries = false;

      monitor.subTask("Finding Artifact Version Conflicts");

      IOseeStatement chStmt = ConnectionHandler.getStatement();
      try {
         int commonTransactionNumber = transactionId != null ? transactionId.getId() : 0;
         long commonBranchId = transactionId != null ? transactionId.getBranchId() : 0;

         chStmt.runPreparedQuery(sql, sourceBranch.getUuid(), sourceBranch.getBaseTransaction().getId(),
            destinationBranch.getUuid(), commonBranchId, commonTransactionNumber);

         ArtifactConflictBuilder artifactConflictBuilder;
         int artId = 0;
         while (chStmt.next()) {
            hadEntries = true;
            int nextArtId = chStmt.getInt("art_id");
            int sourceGamma = chStmt.getInt("source_gamma");
            int destGamma = chStmt.getInt("dest_gamma");
            ModificationType sourceModType = ModificationType.getMod(chStmt.getInt("source_mod_type"));
            ModificationType destModType = ModificationType.getMod(chStmt.getInt("dest_mod_type"));
            long artTypeId = chStmt.getLong("art_type_id");

            if (artId != nextArtId) {
               artId = nextArtId;

               if (destModType == ModificationType.DELETED && sourceModType == ModificationType.MODIFIED || //
               destModType == ModificationType.MODIFIED && sourceModType == ModificationType.DELETED) {

                  artifactConflictBuilder =
                     new ArtifactConflictBuilder(sourceGamma, destGamma, artId, baselineTransaction, sourceBranch,
                        destinationBranch, sourceModType, destModType, artTypeId);

                  conflictBuilders.add(artifactConflictBuilder);
                  artIdSet.add(artId);
               } else if (destModType == ModificationType.DELETED && sourceModType == ModificationType.DELETED) {
                  artIdSetDontShow.add(artId);
                  artIdSetDontAdd.add(artId);
               }
            }
         }
      } finally {
         chStmt.close();
      }

      if (hadEntries) {
         monitor.worked(20);
         for (Integer integer : artIdSet) {
            artIdSetDontShow.add(integer);
         }
      }
   }

   private static void loadAttributeConflictsNew(Branch sourceBranch, Branch destinationBranch, TransactionRecord baselineTransaction, Collection<ConflictBuilder> conflictBuilders, Set<Integer> artIdSet, IProgressMonitor monitor, TransactionRecord transactionId) throws OseeCoreException {
      monitor.subTask("Finding the Attribute Conflicts");
      IOseeStatement chStmt = ConnectionHandler.getStatement();
      AttributeConflictBuilder attributeConflictBuilder;
      try {
         int commonTransactionNumber = transactionId != null ? transactionId.getId() : 0;
         long commonBranchId = transactionId != null ? transactionId.getBranchId() : 0;

         chStmt.runPreparedQuery(ClientSessionManager.getSql(OseeSql.CONFLICT_GET_ATTRIBUTES), sourceBranch.getUuid(),
            sourceBranch.getBaseTransaction().getId(), destinationBranch.getUuid(), commonBranchId,
            commonTransactionNumber);

         int attrId = 0;

         if (chStmt.next()) {

            do {
               int nextAttrId = chStmt.getInt("attr_id");
               int artId = chStmt.getInt("art_id");
               int sourceGamma = chStmt.getInt("source_gamma");
               int destGamma = chStmt.getInt("dest_gamma");
               long attrTypeId = chStmt.getLong("attr_type_id");
               String sourceValue =
                  chStmt.getString("source_value") != null ? chStmt.getString("source_value") : chStmt.getString("dest_value");

               if (attrId != nextAttrId && isAttributeConflictValid(destGamma, sourceBranch)) {
                  attrId = nextAttrId;
                  attributeConflictBuilder =
                     new AttributeConflictBuilder(sourceGamma, destGamma, artId, baselineTransaction, sourceBranch,
                        destinationBranch, sourceValue, attrId, attrTypeId);

                  conflictBuilders.add(attributeConflictBuilder);
                  artIdSet.add(artId);
               }
            } while (chStmt.next());
         }
      } finally {
         chStmt.close();
      }
      monitor.worked(30);
   }

   /**
    * Checks source branch hierarchy to see if the conflict gamma exists. If it does, its not a real conflict because
    * the source branch has already seen this change.
    * 
    * @return Returns True if the AttributeConflict candidate is really a conflict.
    */
   private static boolean isAttributeConflictValid(int destinationGammaId, Branch sourceBranch) throws OseeCoreException {
      boolean isValidConflict = true;
      // We just need the largest value at first so the complete source branch
      // will be searched
      int parentTransactionNumber = Integer.MAX_VALUE;

      for (Branch branch : sourceBranch.getAncestors()) {
         if (!branch.getBranchType().isSystemRootBranch() && !branch.getParentBranch().getBranchType().isSystemRootBranch()) {
            isValidConflict &= isAttributeConflictValidOnBranch(destinationGammaId, branch, parentTransactionNumber);

            if (branch.getSourceTransaction() != null) {
               parentTransactionNumber = branch.getSourceTransaction().getId();
            }
         }

         if (!isValidConflict) {
            break;
         }
      }
      return isValidConflict;
   }

   /**
    * @return Returns True if the destination gamma does not exist on a branch else false if it does.
    */
   private static boolean isAttributeConflictValidOnBranch(int destinationGammaId, Branch branch, int endTransactionNumber) throws OseeCoreException {
      String sql =
         "SELECT count(1) FROM osee_txs txs WHERE txs.gamma_id = ? AND txs.branch_id = ? AND txs.transaction_id <= ?";
      return ConnectionHandler.runPreparedQueryFetchInt(0, sql, destinationGammaId, branch.getUuid(),
         endTransactionNumber) == 0;
   }

   private static void cleanUpConflictDB(Collection<Conflict> conflicts, long branchId, IProgressMonitor monitor) throws OseeCoreException {
      monitor.subTask("Cleaning up old conflict data");
      int queryId = ArtifactLoader.getNewQueryId();
      try {
         if (conflicts != null && conflicts.size() != 0 && branchId != 0) {
            Timestamp insertTime = GlobalTime.GreenwichMeanTimestamp();

            List<Object[]> insertParameters = new LinkedList<Object[]>();
            for (Conflict conflict : conflicts) {
               insertParameters.add(new Object[] {
                  queryId,
                  insertTime,
                  conflict.getObjectId(),
                  branchId,
                  conflict.getConflictType().getValue()});
            }
            ArtifactLoader.insertIntoArtifactJoin(insertParameters);
            ConnectionHandler.runPreparedUpdate(CONFLICT_CLEANUP, branchId, queryId);
         }
      } finally {
         ArtifactLoader.clearQuery(queryId);
      }
      monitor.worked(10);
   }

   public static Collection<Long> getDestinationBranchesMerged(long sourceBranchId) throws OseeCoreException {
      List<Long> destinationBranches = new LinkedList<Long>();
      IOseeStatement chStmt = ConnectionHandler.getStatement();
      try {
         chStmt.runPreparedQuery(GET_DESTINATION_BRANCHES, sourceBranchId);
         while (chStmt.next()) {
            destinationBranches.add(chStmt.getLong("dest_branch_id"));
         }
      } finally {
         chStmt.close();
      }
      if (destinationBranches.size() > 1) {
         Collections.sort(destinationBranches);
      }
      return destinationBranches;
   }

   private static int getCommitTransaction(Branch sourceBranch, Branch destBranch) throws OseeCoreException {
      int transactionId = 0;
      IOseeStatement chStmt = ConnectionHandler.getStatement();
      try {
         if (sourceBranch != null && destBranch != null) {
            chStmt.runPreparedQuery(GET_MERGE_DATA, sourceBranch.getUuid(), destBranch.getUuid());
            if (chStmt.next()) {
               transactionId = chStmt.getInt("commit_transaction_id");
            }
            if (transactionId == 0) {
               chStmt.runPreparedQuery(GET_COMMIT_TRANSACTION_COMMENT,
                  BranchManager.COMMIT_COMMENT + sourceBranch.getName(), destBranch.getUuid());
               if (chStmt.next()) {
                  transactionId = chStmt.getInt("transaction_id");
               }
            }
         }
      } finally {
         chStmt.close();
      }
      return transactionId;
   }

   /**
    * The purpose of this function is find the transaction (Branch Create) that holds the last common values for two
    * branches that share a common history. If two branches share the same history than the point at which they diverged
    * should provide the reference for detecting conflicts based on the gamma at that point.
    */
   public static TransactionRecord findCommonTransaction(Branch sourceBranch, Branch destBranch) throws OseeCoreException {
      Collection<Branch> sourceBranches = sourceBranch.getAncestors();
      Collection<Branch> destBranches = destBranch.getAncestors();
      Branch commonBranch = null;
      for (Branch branch : sourceBranches) {
         if (destBranches.contains(branch)) {
            commonBranch = branch;
            break;
         }
      }
      if (commonBranch == null) {
         throw new OseeCoreException("Cannot find a common ancestor for Branch %s and Branch %s",
            sourceBranch.getShortName(), destBranch.getShortName());
      }
      TransactionRecord sourceTransaction = null;
      TransactionRecord destTransaction = null;
      if (commonBranch.equals(destBranch)) {
         destTransaction = TransactionManager.getHeadTransaction(commonBranch);
      } else {
         for (Branch branch : destBranches) {
            if (branch.getParentBranch().equals(commonBranch)) {
               destTransaction = branch.getBaseTransaction();
               break;
            }
         }
      }
      for (Branch branch : sourceBranches) {
         if (commonBranch.equals(branch.getParentBranch())) {
            sourceTransaction = branch.getBaseTransaction();
            break;
         }
      }

      TransactionRecord toReturn = null;
      if (sourceTransaction == null && destTransaction != null) {
         toReturn = destTransaction;
      } else if (sourceTransaction != null && destTransaction == null) {
         toReturn = sourceTransaction;
      } else {
         toReturn = sourceTransaction.getId() <= destTransaction.getId() ? sourceTransaction : destTransaction;
      }
      return toReturn;
   }

}
