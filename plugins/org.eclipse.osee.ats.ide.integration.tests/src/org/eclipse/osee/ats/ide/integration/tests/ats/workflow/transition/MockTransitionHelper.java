/*********************************************************************
 * Copyright (c) 2011 Boeing
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

package org.eclipse.osee.ats.ide.integration.tests.ats.workflow.transition;

import java.util.Collection;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.AtsUser;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.transition.TransitionOption;
import org.eclipse.osee.ats.core.workflow.transition.TransitionHelper;
import org.eclipse.osee.ats.ide.integration.tests.AtsClientService;

/**
 * Allows setting of workingBranchInWork and branchInCommit for testing purposes. If not set, uses default from
 * TransitionHelper
 *
 * @author Donald G. Dunne
 */
public class MockTransitionHelper extends TransitionHelper {
   public Boolean workingBranchInWork = null;
   public Boolean branchInCommit = null;
   public Boolean systemUser = null;
   public Boolean systemUserAssigned = null;
   public Boolean overrideTransitionValidityCheck = null;

   public MockTransitionHelper(String name, Collection<IAtsWorkItem> awas, String toStateName, Collection<AtsUser> toAssignees, String cancellationReason, IAtsChangeSet changes, TransitionOption... transitionOption) {
      super(name, awas, toStateName, toAssignees, cancellationReason, changes, AtsClientService.get().getServices(),
         transitionOption);
   }

   @Override
   public boolean isWorkingBranchInWork(IAtsTeamWorkflow teamWf) {
      if (workingBranchInWork != null) {
         return workingBranchInWork;
      }
      return super.isWorkingBranchInWork(teamWf);
   }

   @Override
   public boolean isBranchInCommit(IAtsTeamWorkflow teamWf) {
      if (branchInCommit != null) {
         return branchInCommit;
      }
      return super.isBranchInCommit(teamWf);
   }

   public Boolean getWorkingBranchInWork() {
      return workingBranchInWork;
   }

   public void setWorkingBranchInWork(Boolean workingBranchInWork) {
      this.workingBranchInWork = workingBranchInWork;
   }

   public Boolean getBranchInCommit() {
      return branchInCommit;
   }

   public void setBranchInCommit(Boolean branchInCommit) {
      this.branchInCommit = branchInCommit;
   }

   @Override
   public boolean isSystemUser() {
      if (systemUser != null) {
         return systemUser;
      }
      return super.isSystemUser();
   }

   public void setSystemUser(Boolean systemUser) {
      this.systemUser = systemUser;
   }

   @Override
   public boolean isSystemUserAssingee(IAtsWorkItem workItem) {
      if (systemUserAssigned != null) {
         return systemUserAssigned;
      }
      return super.isSystemUserAssingee(workItem);
   }

   public void setSystemUserAssigned(Boolean systemUserAssigned) {
      this.systemUserAssigned = systemUserAssigned;
   }

   @Override
   public boolean isOverrideTransitionValidityCheck() {
      if (overrideTransitionValidityCheck != null) {
         return overrideTransitionValidityCheck;
      }
      return super.isOverrideTransitionValidityCheck();
   }

   public void setOverrideTransitionValidityCheck(Boolean overrideTransitionValidityCheck) {
      this.overrideTransitionValidityCheck = overrideTransitionValidityCheck;
   }

}
