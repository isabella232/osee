/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.client.branch.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.core.util.AbstractAtsBranchService;
import org.eclipse.osee.ats.core.workflow.ITeamWorkflowProvidersLazy;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.model.cache.BranchFilter;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;

/**
 * @author Donald G. Dunne
 */
public class AtsBranchServiceImpl extends AbstractAtsBranchService {

   public AtsBranchServiceImpl(IAtsServices atsServices, ITeamWorkflowProvidersLazy teamWorkflowProvidersLazy) {
      super(atsServices, teamWorkflowProvidersLazy);
   }

   /**
    * Return working branch associated with SMA, even if it's been archived; This data is cached across all workflows
    * with the cache being updated by local and remote events. Filters out rebaseline branches (which are working
    * branches also).
    */
   @Override
   public IOseeBranch getWorkingBranchExcludeStates(IAtsTeamWorkflow teamWf, BranchState... negatedBranchStates) {
      BranchFilter branchFilter = new BranchFilter(BranchType.WORKING, BranchType.BASELINE);
      branchFilter.setNegatedBranchStates(negatedBranchStates);
      branchFilter.setAssociatedArtifact((Artifact) teamWf.getStoreObject());

      return BranchManager.getBranch(branchFilter);
   }

   @Override
   public IOseeBranch getCommittedWorkingBranch(IAtsTeamWorkflow teamWf) {
      BranchFilter branchFilter = new BranchFilter(BranchType.WORKING);
      branchFilter.setBranchStates(BranchState.COMMITTED);
      branchFilter.setAssociatedArtifact((Artifact) teamWf.getStoreObject());
      return BranchManager.getBranch(branchFilter);
   }

   @Override
   public BranchType getBranchType(BranchId branch) {
      return BranchManager.getType(branch);
   }

   @Override
   public BranchState getBranchState(BranchId branch) {
      return BranchManager.getState(branch);
   }

   @Override
   public Collection<TransactionToken> getCommittedArtifactTransactionIds(IAtsTeamWorkflow teamWf) {
      List<TransactionToken> transactions = new ArrayList<>();
      for (TransactionRecord trans : TransactionManager.getCommittedArtifactTransactionIds(
         (Artifact) teamWf.getStoreObject())) {
         transactions.add(trans);
      }
      return transactions;
   }

   /**
    * Method available for optimized checking of merge branches so don't have to re-acquire working branch if already
    * have
    */
   @Override
   public boolean isMergeBranchExists(IAtsTeamWorkflow teamWf, BranchId workingBranch, BranchId destinationBranch) throws OseeCoreException {
      if (workingBranch == null) {
         return false;
      }
      return BranchManager.doesMergeBranchExist(workingBranch, destinationBranch);
   }

   @Override
   public IOseeBranch getBranchByUuid(long branchUuid) {
      return BranchManager.getBranchToken(branchUuid);
   }

   @Override
   public boolean branchExists(long branchUuid) {
      return BranchManager.branchExists(branchUuid);
   }

   @Override
   public boolean isArchived(BranchId branch) {
      return BranchManager.isArchived(branch);
   }

   @Override
   public BranchId getParentBranch(BranchId branch) {
      return BranchManager.getParentBranch(branch);
   }

   @Override
   public TransactionToken getBaseTransaction(BranchId branch) {
      return BranchManager.getBaseTransaction(branch);
   }
}