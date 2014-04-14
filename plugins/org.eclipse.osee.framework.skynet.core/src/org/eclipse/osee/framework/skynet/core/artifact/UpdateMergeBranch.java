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
package org.eclipse.osee.framework.skynet.core.artifact;

import static org.eclipse.osee.framework.core.enums.DeletionFlag.INCLUDE_DELETED;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.database.core.DbTransaction;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.database.core.OseeSql;
import org.eclipse.osee.framework.database.core.SQL3DataType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.time.GlobalTime;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * @author Theron Virgin
 */
public class UpdateMergeBranch extends DbTransaction {

   private static final String TX_CURRENT_SETTINGS =
      "CASE" + //
      " WHEN txs1.mod_type = " + ModificationType.DELETED.getValue() + " THEN " + TxChange.DELETED.getValue() + //
      " WHEN txs1.mod_type = " + ModificationType.ARTIFACT_DELETED.getValue() + " THEN " + TxChange.ARTIFACT_DELETED.getValue() + //
      " ELSE " + TxChange.CURRENT.getValue() + //
      " END";

   private static final String UPDATE_ARTIFACTS =
      "INSERT INTO osee_txs (transaction_id, gamma_id, mod_type, tx_current, branch_id) SELECT ?, txs1.gamma_id, txs1.mod_type, " + TX_CURRENT_SETTINGS + ", ? FROM osee_attribute attr1, osee_txs txs1 WHERE attr1.art_id = ? AND txs1.gamma_id = attr1.gamma_id AND txs1.branch_id = ? AND txs1.tx_current <> ? AND NOT EXISTS (SELECT 'x' FROM osee_txs txs2, osee_attribute attr2 WHERE txs2.branch_id = ? AND txs2.transaction_id = ? AND txs2.gamma_id = attr2.gamma_id AND attr2.attr_id = attr1.attr_id)";

   private static final String PURGE_ATTRIBUTE_FROM_MERGE_BRANCH =
      "DELETE from osee_txs txs WHERE EXISTS (SELECT 'x' FROM osee_attribute attr WHERE txs.gamma_id = attr.gamma_id AND txs.branch_id = ? AND attr.art_id = ?)";
   private static final String PURGE_RELATION_FROM_MERGE_BRANCH =
      "DELETE from osee_txs txs WHERE EXISTS (SELECT 'x' FROM osee_relation_link rel WHERE txs.gamma_id = rel.gamma_id AND txs.branch_id = ? AND (rel.a_art_id = ? or rel.b_art_id = ?))";
   private static final String PURGE_ARTIFACT_FROM_MERGE_BRANCH =
      "DELETE from osee_txs txs WHERE EXISTS (SELECT 'x' FROM osee_artifact art WHERE txs.gamma_id = art.gamma_id AND txs.branch_id = ? AND art.art_id = ?)";

   private static final boolean DEBUG =
      "TRUE".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.osee.framework.skynet.core/debug/Merge"));

   private final Branch mergeBranch;
   private final ArrayList<Integer> expectedArtIds;
   private final Branch destBranch;
   private final Branch sourceBranch;

   public UpdateMergeBranch(Branch mergeBranch, ArrayList<Integer> expectedArtIds, Branch destBranch, Branch sourceBranch) {
      this.destBranch = destBranch;
      this.expectedArtIds = expectedArtIds;
      this.mergeBranch = mergeBranch;
      this.sourceBranch = sourceBranch;
   }

   @Override
   protected void handleTxWork(OseeConnection connection) throws OseeCoreException {
      Collection<Integer> allMergeBranchArtifacts = getAllMergeArtifacts(mergeBranch);
      long time = System.currentTimeMillis();
      Collection<Integer> allMergeBranchArtifactsCopy = new HashSet<Integer>(allMergeBranchArtifacts);
      Collection<Artifact> goodMergeBranchArtifacts =
         ArtifactQuery.getArtifactListFromBranch(mergeBranch, INCLUDE_DELETED);

      if (DEBUG) {
         System.out.println(String.format("        Get artifacts on branch took %s", Lib.getElapseString(time)));
         time = System.currentTimeMillis();
         System.out.println("            Need the following Artifacts on the Merge Branch");
         System.out.print("            ");
         for (Integer integer : expectedArtIds) {
            System.out.print(integer + ", ");
         }
         System.out.print("\n");
      }
      int count = 0;
      //Delete any damaged artifacts (from a source revert) on the merge branch
      for (Artifact artifact : goodMergeBranchArtifacts) {
         allMergeBranchArtifactsCopy.remove(Integer.valueOf(artifact.getArtId()));
      }
      if (!allMergeBranchArtifactsCopy.isEmpty()) {
         for (Integer artifact : allMergeBranchArtifactsCopy) {
            purgeArtifactFromBranch(connection, mergeBranch, artifact.intValue());
            count++;
         }
      }
      if (DEBUG) {
         System.out.println(String.format("          Deleting %d Damaged Artifacts took %s", count,
            Lib.getElapseString(time)));
         time = System.currentTimeMillis();
         count = 0;
      }

      //Delete any artifacts that shouldn't be on the merge branch but are
      for (Integer artid : expectedArtIds) {
         allMergeBranchArtifacts.remove(artid);
      }
      if (!allMergeBranchArtifacts.isEmpty()) {
         for (Integer artifact : allMergeBranchArtifacts) {
            count++;
            purgeArtifactFromBranch(connection, mergeBranch, artifact.intValue());
         }
      }
      if (DEBUG) {
         System.out.println(String.format("          Deleting %d unused Artifacts took %s", count,
            Lib.getElapseString(time)));
         time = System.currentTimeMillis();
         count = 0;
      }
      int numberAttrUpdated = 0;
      //Copy over any missing attributes
      int baselineTransaction = mergeBranch.getBaseTransaction().getId();
      for (Artifact artifact : goodMergeBranchArtifacts) {
         numberAttrUpdated +=
            ConnectionHandler.runPreparedUpdate(connection, UPDATE_ARTIFACTS, baselineTransaction, mergeBranch.getId(),
               artifact.getArtId(), sourceBranch.getId(), TxChange.NOT_CURRENT.getValue(), mergeBranch.getId(),
               baselineTransaction);
      }
      if (DEBUG) {
         System.out.println(String.format("          Adding %d Attributes to Existing Artifacts took %s",
            numberAttrUpdated, Lib.getElapseString(time)));
         time = System.currentTimeMillis();
      }

      //Add any artifacts that should be on the merge branch but aren't
      for (Artifact artifact : goodMergeBranchArtifacts) {
         expectedArtIds.remove(Integer.valueOf(artifact.getArtId()));
      }
      if (!expectedArtIds.isEmpty()) {
         addArtifactsToBranch(connection, sourceBranch, destBranch, mergeBranch, expectedArtIds);
      }

      if (DEBUG) {
         System.out.println(String.format("          Adding %d new Artifacts took %s", expectedArtIds.size(),
            Lib.getElapseString(time)));
         time = System.currentTimeMillis();
      }
   }

   private final static String INSERT_ATTRIBUTE_GAMMAS =
      "INSERT INTO OSEE_TXS (transaction_id, gamma_id, mod_type, tx_current, branch_id) SELECT ?, atr1.gamma_id, txs1.mod_type, ?, ? FROM osee_attribute atr1, osee_txs txs1, osee_join_artifact ald1 WHERE txs1.branch_id = ? AND txs1.tx_current in (1,2) AND txs1.gamma_id = atr1.gamma_id AND atr1.art_id = ald1.art_id and ald1.query_id = ?";
   private final static String INSERT_ARTIFACT_GAMMAS =
      "INSERT INTO OSEE_TXS (transaction_id, gamma_id, mod_type, tx_current, branch_id) SELECT ?, arv1.gamma_id, txs1.mod_type, ?, ? FROM osee_artifact arv1, osee_txs txs1, osee_join_artifact ald1 WHERE txs1.branch_id = ? AND txs1.tx_current in (1,2) AND txs1.gamma_id = arv1.gamma_id AND arv1.art_id = ald1.art_id and ald1.query_id = ?";

   private void addArtifactsToBranch(OseeConnection connection, Branch sourceBranch, Branch destBranch, Branch mergeBranch, Collection<Integer> artIds) throws OseeCoreException {
      if (artIds == null || artIds.isEmpty()) {
         throw new IllegalArgumentException("Artifact IDs can not be null or empty");
      }

      List<Object[]> datas = new LinkedList<Object[]>();
      int queryId = ArtifactLoader.getNewQueryId();
      Timestamp insertTime = GlobalTime.GreenwichMeanTimestamp();

      for (int artId : artIds) {
         datas.add(new Object[] {queryId, insertTime, artId, sourceBranch.getId(), SQL3DataType.INTEGER});
      }
      try {
         ArtifactLoader.insertIntoArtifactJoin(datas);
         Integer startTransactionNumber = mergeBranch.getBaseTransaction().getId();
         insertGammas(connection, INSERT_ATTRIBUTE_GAMMAS, startTransactionNumber, queryId, sourceBranch, mergeBranch);
         insertGammas(connection, INSERT_ARTIFACT_GAMMAS, startTransactionNumber, queryId, sourceBranch, mergeBranch);
      } catch (OseeCoreException ex) {
         throw new OseeCoreException("Source Branch %s Artifact Ids: %s", sourceBranch.getUuid(), Collections.toString(
            ",", artIds));
      } finally {
         ArtifactLoader.clearQuery(connection, queryId);
      }
   }

   private static void insertGammas(OseeConnection connection, String sql, int baselineTransactionNumber, int queryId, Branch sourceBranch, Branch mergeBranch) throws OseeCoreException {
      ConnectionHandler.runPreparedUpdate(connection, sql, baselineTransactionNumber, TxChange.CURRENT.getValue(),
         mergeBranch.getId(), sourceBranch.getId(), queryId);
   }

   private static Collection<Integer> getAllMergeArtifacts(Branch branch) throws OseeCoreException {
      Collection<Integer> artSet = new HashSet<Integer>();
      long time = System.currentTimeMillis();

      IOseeStatement chStmt = ConnectionHandler.getStatement();
      try {
         chStmt.runPreparedQuery(ClientSessionManager.getSql(OseeSql.MERGE_GET_ARTIFACTS_FOR_BRANCH), branch.getId());
         while (chStmt.next()) {
            artSet.add(chStmt.getInt("art_id"));
         }
         if (DEBUG) {
            System.out.println(String.format(
               "          Getting Artifacts that are on the Merge Branch Completed in %s", Lib.getElapseString(time)));
            time = System.currentTimeMillis();
         }

         chStmt.runPreparedQuery(ClientSessionManager.getSql(OseeSql.MERGE_GET_ATTRIBUTES_FOR_BRANCH), branch.getId());
         while (chStmt.next()) {
            artSet.add(chStmt.getInt("art_id"));
         }
         if (DEBUG) {
            System.out.println(String.format(
               "          Getting Attributes that are on the Merge Branch Completed in %s", Lib.getElapseString(time)));
            time = System.currentTimeMillis();
         }

         chStmt.runPreparedQuery(ClientSessionManager.getSql(OseeSql.MERGE_GET_RELATIONS_FOR_BRANCH), branch.getId());
         while (chStmt.next()) {
            artSet.add(chStmt.getInt("a_art_id"));
            artSet.add(chStmt.getInt("b_art_id"));
         }
      } finally {
         chStmt.close();
      }
      if (DEBUG) {
         System.out.println(String.format("          Getting Relations that are on the Merge Branch Completed in %s",
            Lib.getElapseString(time)));
         System.out.println("            Found the following Artifacts on the Merge Branch");
         System.out.print("            ");
         for (Integer integer : artSet) {
            System.out.print(integer + ", ");
         }
         System.out.print("\n");
      }
      return artSet;
   }

   /**
    * Removes an artifact, it's attributes and any relations that have become invalid from the removal of this artifact
    * from the database. It also removes all history associated with this artifact (i.e. all transactions and gamma ids
    * will also be removed from the database only for the branch it is on).
    */
   private static void purgeArtifactFromBranch(OseeConnection connection, Branch branch, int artId) throws OseeCoreException {
      long branchId = branch.getId();

      //Remove from Baseline
      ConnectionHandler.runPreparedUpdate(connection, PURGE_ATTRIBUTE_FROM_MERGE_BRANCH, branchId, artId);
      ConnectionHandler.runPreparedUpdate(connection, PURGE_RELATION_FROM_MERGE_BRANCH, branchId, artId, artId);
      ConnectionHandler.runPreparedUpdate(connection, PURGE_ARTIFACT_FROM_MERGE_BRANCH, branchId, artId);
   }

}