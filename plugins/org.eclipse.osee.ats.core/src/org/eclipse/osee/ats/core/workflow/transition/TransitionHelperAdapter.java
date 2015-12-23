/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.workflow.transition;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.transition.ITransitionHelper;
import org.eclipse.osee.ats.api.workflow.transition.TransitionResult;
import org.eclipse.osee.ats.api.workflow.transition.TransitionResults;
import org.eclipse.osee.ats.core.users.AtsCoreUsers;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;

/**
 * @author Donald G. Dunne
 */
public abstract class TransitionHelperAdapter implements ITransitionHelper {

   private final IAtsServices services;
   private IAtsUser transitionUser;
   private boolean workflowsReloaded = false;

   public TransitionHelperAdapter(IAtsServices services) {
      this.services = services;
   }

   @Override
   public boolean isPrivilegedEditEnabled() {
      return false;
   }

   @Override
   public boolean isOverrideTransitionValidityCheck() {
      return false;
   }

   @Override
   public boolean isOverrideAssigneeCheck() {
      return false;
   }

   @Override
   public boolean isWorkingBranchInWork(IAtsTeamWorkflow teamWf) throws OseeCoreException {
      return services.getBranchService().isWorkingBranchInWork(teamWf);
   }

   @Override
   public boolean isBranchInCommit(IAtsTeamWorkflow teamWf) throws OseeCoreException {
      return services.getBranchService().isBranchInCommit(teamWf);
   }

   @Override
   public boolean isSystemUser() throws OseeCoreException {
      return AtsCoreUsers.isAtsCoreUser(getTransitionUser());
   }

   @Override
   public boolean isSystemUserAssingee(IAtsWorkItem workItem) throws OseeCoreException {
      return workItem.getStateMgr().getAssignees().contains(
         AtsCoreUsers.ANONYMOUS_USER) || workItem.getStateMgr().getAssignees().contains(AtsCoreUsers.SYSTEM_USER);
   }

   @Override
   public boolean isExecuteChanges() {
      return false;
   }

   @Override
   public IAtsUser getTransitionUser() throws OseeStateException, OseeCoreException {
      IAtsUser user = transitionUser;
      if (user == null) {
         user = services.getUserService().getCurrentUser();
      }
      return user;
   }

   @Override
   public void setTransitionUser(IAtsUser user) throws OseeCoreException {
      transitionUser = user;
   }

   @Override
   public abstract Collection<? extends IAtsWorkItem> getWorkItems();

   @Override
   public void handleWorkflowReload(TransitionResults results) {
      if (!workflowsReloaded) {
         services.getStoreService().reload(new ArrayList<>(getWorkItems()));
         for (IAtsWorkItem workItem : getWorkItems()) {
            if (services.getStoreService().isDeleted(workItem)) {
               results.addResult(workItem, TransitionResult.WORKITEM_DELETED);
            }
         }
         workflowsReloaded = true;
      }
   }

}
