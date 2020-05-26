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

package org.eclipse.osee.ats.core.workflow;

import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.review.IAtsAbstractReview;
import org.eclipse.osee.ats.api.user.AtsUser;
import org.eclipse.osee.ats.api.user.IAtsUserService;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.model.RuleDefinitionOption;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;

/**
 * @author Donald G. Dunne
 */
public class WorkflowManagerCore {

   public static boolean isEditable(AtsUser user, IAtsWorkItem workItem, IAtsStateDefinition stateDef, IAtsUserService userService) {
      return isEditable(workItem, stateDef, user, userService.isAtsAdmin());
   }

   public static boolean isEditable(IAtsWorkItem workItem, IAtsStateDefinition stateDef, AtsUser currentUser, boolean isAtsAdmin) {
      WorkflowManagerCore wmc = new WorkflowManagerCore();
      return wmc.isWorkItemEditable(workItem, stateDef, currentUser, isAtsAdmin);
   }

   protected boolean isWorkItemEditable(IAtsWorkItem workItem, IAtsStateDefinition stateDef, AtsUser currentUser, boolean isAtsAdmin) {
      // must be current state
      return (stateDef == null || workItem.getStateDefinition().getName().equals(stateDef.getName())) &&
      // and one of these
      //
      // page is define to allow anyone to edit
         (workItem.getStateDefinition().hasRule(RuleDefinitionOption.AllowEditToAll.name()) ||
         // team definition has allowed anyone to edit
            teamDefHasRule(workItem, RuleDefinitionOption.AllowEditToAll) ||
            // current user is assigned
            workItem.getAssignees().contains(currentUser) ||
            // current user is ats admin
            isAtsAdmin);
   }

   protected boolean teamDefHasRule(IAtsWorkItem workItem, RuleDefinitionOption option) {
      boolean hasRule = false;
      IAtsTeamWorkflow teamWf = null;
      if (workItem instanceof IAtsTeamWorkflow) {
         teamWf = (IAtsTeamWorkflow) workItem;
      } else if (workItem instanceof IAtsAbstractReview) {
         teamWf = ((IAtsAbstractReview) workItem).getParentTeamWorkflow();
      }
      if (teamWf != null && teamWf.getTeamDefinition() != null) {
         hasRule = teamWf.getTeamDefinition().getAtsApi().getTeamDefinitionService().hasRule(teamWf.getTeamDefinition(),
            option.name());
      }
      return hasRule;
   }

}
