/*********************************************************************
 * Copyright (c) 2010 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.framework.skynet.core.revision.acquirer;

import java.util.ArrayList;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.OrcsTokenService;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactTypeId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.GammaId;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.model.TransactionDelta;
import org.eclipse.osee.framework.core.sql.OseeSql;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.change.ChangeBuilder;
import org.eclipse.osee.framework.skynet.core.change.RelationChangeBuilder;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.skynet.core.utility.ConnectionHandler;
import org.eclipse.osee.jdbc.JdbcStatement;

/**
 * @author Jeff C. Phillips
 */
public class RelationChangeAcquirer extends ChangeAcquirer {
   private final OrcsTokenService tokenService;

   public RelationChangeAcquirer(BranchId sourceBranch, TransactionToken transactionId, IProgressMonitor monitor, Artifact specificArtifact, Set<ArtifactId> artIds, ArrayList<ChangeBuilder> changeBuilders, Set<ArtifactId> newAndDeletedArtifactIds, OrcsTokenService tokenService) {
      super(sourceBranch, transactionId, monitor, specificArtifact, artIds, changeBuilders, newAndDeletedArtifactIds);
      this.tokenService = tokenService;
   }

   @Override
   public ArrayList<ChangeBuilder> acquireChanges() {
      JdbcStatement chStmt = ConnectionHandler.getStatement();
      TransactionToken fromTransactionId;
      TransactionToken toTransactionId;

      if (getMonitor() != null) {
         getMonitor().subTask("Gathering Relation Changes");
      }
      try {
         boolean hasBranch = getSourceBranch() != null;

         //Changes per a branch
         if (hasBranch) {
            fromTransactionId = getSourceBaseTransaction();
            toTransactionId = TransactionManager.getHeadTransaction(getSourceBranch());
            chStmt.runPreparedQuery(ServiceUtil.getSql(OseeSql.CHANGE_BRANCH_RELATION), getSourceBranch(),
               fromTransactionId);
         } else {//Changes per a transaction
            toTransactionId = getTransaction();

            if (getSpecificArtifact() != null) {
               chStmt.runPreparedQuery(ServiceUtil.getSql(OseeSql.CHANGE_TX_RELATION_FOR_SPECIFIC_ARTIFACT),
                  toTransactionId.getBranch(), toTransactionId, getSpecificArtifact(), getSpecificArtifact());
               fromTransactionId = toTransactionId;
            } else {
               chStmt.runPreparedQuery(ServiceUtil.getSql(OseeSql.CHANGE_TX_RELATION), toTransactionId.getBranch(),
                  toTransactionId.getId());
               fromTransactionId = TransactionManager.getPriorTransaction(toTransactionId);
            }
         }
         TransactionDelta txDelta = new TransactionDelta(fromTransactionId, toTransactionId);
         while (chStmt.next()) {
            ArtifactId aArtId = ArtifactId.valueOf(chStmt.getLong("a_art_id"));
            ArtifactId bArtId = ArtifactId.valueOf(chStmt.getLong("b_art_id"));
            int relLinkId = chStmt.getInt("rel_link_id");

            if (!getNewAndDeletedArtifactIds().contains(aArtId) && !getNewAndDeletedArtifactIds().contains(bArtId)) {
               ModificationType modificationType = ModificationType.valueOf(chStmt.getInt("mod_type"));
               String rationale = modificationType != ModificationType.DELETED ? chStmt.getString("rationale") : "";
               getArtIds().add(aArtId);
               getArtIds().add(bArtId);

               getChangeBuilders().add(new RelationChangeBuilder(getSourceBranch(),
                  ArtifactTypeId.valueOf(chStmt.getLong("art_type_id")), GammaId.valueOf(chStmt.getLong("gamma_id")),
                  aArtId, txDelta, modificationType, ArtifactId.valueOf(bArtId), relLinkId, rationale,
                  tokenService.getRelationType(chStmt.getLong("rel_link_type_id")), !hasBranch));
            }
         }
         if (getMonitor() != null) {
            getMonitor().worked(25);
         }
      } finally {
         chStmt.close();
      }
      return getChangeBuilders();
   }
}