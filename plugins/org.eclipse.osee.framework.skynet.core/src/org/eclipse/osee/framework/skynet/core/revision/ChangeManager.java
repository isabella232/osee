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

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.model.TransactionDelta;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.sql.OseeSql;
import org.eclipse.osee.framework.jdk.core.type.CompositeKeyHashMap;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.change.ArtifactDelta;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.skynet.core.utility.ArtifactJoinQuery;
import org.eclipse.osee.framework.skynet.core.utility.ConnectionHandler;
import org.eclipse.osee.framework.skynet.core.utility.JoinUtility;
import org.eclipse.osee.jdbc.JdbcStatement;

/**
 * Public API class for access to change data from branches and transactionIds
 *
 * @author Jeff C. Phillips
 * @author Donald G. Dunne
 */
public final class ChangeManager {

   private final static RevisionChangeLoader revsionChangeLoader = new RevisionChangeLoader();

   private ChangeManager() {
      // this private empty constructor exists to prevent the default constructor from allowing public construction
   }

   public static Collection<ArtifactDelta> getCompareArtifacts(Collection<Change> changes) {
      Collection<ArtifactDelta> toReturn = new ArrayList<>(changes.size());
      for (Change change : changes) {
         toReturn.add(change.getDelta());
      }
      return toReturn;
   }

   /**
    * Acquires changes for a particular artifact
    */
   public static Collection<Change> getChangesPerArtifact(Artifact artifact, IProgressMonitor monitor) throws OseeCoreException {
      return revsionChangeLoader.getChangesPerArtifact(artifact, monitor);
   }

   /**
    * Acquires artifact, relation and attribute changes from a source branch since its creation.
    */
   public static IOperation comparedToPreviousTx(TransactionToken transactionId, Collection<Change> changes) throws OseeCoreException {
      TransactionToken startTx = TransactionManager.getPriorTransaction(transactionId);
      TransactionToken endTx = transactionId;

      TransactionDelta txDelta = new TransactionDelta(startTx, endTx);
      return new ChangeDataLoader(changes, txDelta);
   }

   /**
    * Acquires artifact, relation and attribute changes from a source branch since its creation.
    */
   public static IOperation comparedToParent(BranchId branch, Collection<Change> changes) throws OseeCoreException {
      TransactionRecord startTx = TransactionManager.getHeadTransaction(branch);
      TransactionRecord endTx = TransactionManager.getHeadTransaction(BranchManager.getParentBranch(branch));

      TransactionDelta txDelta = new TransactionDelta(startTx, endTx);
      return new ChangeDataLoader(changes, txDelta);
   }

   public static IOperation compareTwoBranchesHead(BranchId sourceBranch, BranchId destinationBranch, Collection<Change> changes) throws OseeCoreException {
      TransactionRecord startTx = TransactionManager.getHeadTransaction(sourceBranch);
      TransactionRecord endTx = TransactionManager.getHeadTransaction(destinationBranch);
      return new ChangeDataLoader(changes, new TransactionDelta(startTx, endTx));
   }

   /**
    * For the given list of artifacts determine which transactions (on that artifact's branch) affected that artifact.
    * The branch's baseline transaction is excluded.
    *
    * @return a map of artifact to collection of TransactionIds which affected the given artifact
    */
   public static HashCollection<Artifact, TransactionRecord> getModifingTransactions(Collection<Artifact> artifacts) throws OseeCoreException {
      ArtifactJoinQuery joinQuery = JoinUtility.createArtifactJoinQuery();
      CompositeKeyHashMap<Integer, BranchId, Artifact> artifactMap = new CompositeKeyHashMap<>();
      for (Artifact artifact : artifacts) {
         BranchId branch = artifact.getBranch();
         artifactMap.put(artifact.getArtId(), branch, artifact);
         TransactionId transaction = TransactionManager.getHeadTransaction(branch);
         joinQuery.add(artifact.getArtId(), branch.getUuid(), transaction);

         // for each combination of artifact and its branch hierarchy
         while (!branch.equals(CoreBranches.SYSTEM_ROOT)) {
            transaction = BranchManager.getSourceTransaction(branch);
            branch = BranchManager.getParentBranch(branch);
            joinQuery.add(artifact.getArtId(), branch.getUuid(), transaction);
         }
      }

      HashCollection<Artifact, TransactionRecord> transactionMap = new HashCollection<>();
      try {
         joinQuery.store();
         JdbcStatement chStmt = ConnectionHandler.getStatement();
         try {
            chStmt.runPreparedQuery(joinQuery.size() * 2, ServiceUtil.getSql(OseeSql.CHANGE_TX_MODIFYING),
               joinQuery.getQueryId());
            while (chStmt.next()) {
               BranchId branch = TokenFactory.createBranch(chStmt.getLong("branch_id"));
               Artifact artifact = artifactMap.get(chStmt.getInt("art_id"), branch);
               transactionMap.put(artifact, TransactionManager.getTransactionId(chStmt.getLong("transaction_id")));
            }
         } finally {
            chStmt.close();
         }
      } finally {
         joinQuery.delete();
      }
      return transactionMap;
   }

   /**
    * For the given list of artifacts determine which branches (in the branch hierarchy for that artifact) affected that
    * artifact.
    *
    * @return a map of artifact to collection of branches which affected the given artifact
    */
   public static HashCollection<Artifact, BranchId> getModifingBranches(Collection<Artifact> artifacts) throws OseeCoreException {
      ArtifactJoinQuery joinQuery = JoinUtility.createArtifactJoinQuery();

      CompositeKeyHashMap<Integer, BranchId, Artifact> artifactMap =
         new CompositeKeyHashMap<Integer, BranchId, Artifact>();
      for (Artifact artifact : artifacts) {
         artifactMap.put(artifact.getArtId(), artifact.getBranch(), artifact);
         // for each combination of artifact and all working branches in its hierarchy
         for (BranchId workingBranch : BranchManager.getBranches(BranchArchivedState.UNARCHIVED, BranchType.WORKING)) {
            if (artifact.isOnBranch(BranchManager.getParentBranch(workingBranch))) {
               joinQuery.add(artifact.getArtId(), workingBranch.getUuid());
            }
         }
      }

      HashCollection<Artifact, BranchId> branchMap = new HashCollection<>();
      try {
         joinQuery.store();
         JdbcStatement chStmt = ConnectionHandler.getStatement();
         try {
            chStmt.runPreparedQuery(joinQuery.size() * 2, ServiceUtil.getSql(OseeSql.CHANGE_BRANCH_MODIFYING),
               joinQuery.getQueryId());
            while (chStmt.next()) {
               if (chStmt.getInt("tx_count") > 0) {
                  BranchId branch = TokenFactory.createBranch(chStmt.getLong("branch_id"));
                  Artifact artifact = artifactMap.get(chStmt.getInt("art_id"), BranchManager.getParentBranch(branch));
                  branchMap.put(artifact, branch);
               }
            }
         } finally {
            chStmt.close();
         }
      } finally {
         joinQuery.delete();
      }
      return branchMap;
   }
}