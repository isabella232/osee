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
package org.eclipse.osee.ats.core.client.artifact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.workflow.IAtsAction;
import org.eclipse.osee.ats.api.workflow.IAtsTask;
import org.eclipse.osee.ats.core.client.internal.Activator;
import org.eclipse.osee.ats.core.client.internal.AtsClientService;
import org.eclipse.osee.ats.core.client.search.UserRelatedToAtsObjectSearch;
import org.eclipse.osee.ats.core.client.util.AtsGroup;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCheck;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * Check for certain conditions that must be met to delete an ATS object or User artifact.
 * 
 * @author Donald G. Dunne
 */
public class AtsArtifactChecks extends ArtifactCheck {

   private static final List<Long> Admin_Only_Relation_Type_Ids =
      org.eclipse.osee.framework.jdk.core.util.Collections.getAggregate(
         AtsRelationTypes.TeamWorkflowToReview_Review.getGuid(), AtsRelationTypes.ActionToWorkflow_Action.getGuid());
   private static boolean deletionChecksEnabled = !AtsUtilCore.isInTest();

   @Override
   public IStatus isDeleteableRelation(Artifact artifact, IRelationType relationType) throws OseeCoreException {
      if (deletionChecksEnabled) {
         boolean isAtsAdmin = AtsGroup.AtsAdmin.isCurrentUserMember();
         if (!isAtsAdmin && Admin_Only_Relation_Type_Ids.contains(relationType.getGuid())) {
            return createStatus(String.format(
               "Deletion of relation type [%s] off artifact [%s] is only permitted by ATS Admin", relationType,
               artifact));
         }
      }
      return Status.OK_STATUS;
   }

   @Override
   public IStatus isDeleteable(Collection<Artifact> artifacts) throws OseeCoreException {
      boolean isAtsAdmin = AtsGroup.AtsAdmin.isCurrentUserMember();

      IStatus result = Status.OK_STATUS;

      if (deletionChecksEnabled) {
         if (result.isOK()) {
            result = checkActionableItems(isAtsAdmin, artifacts);
         }

         if (result.isOK()) {
            result = checkTeamDefinitions(isAtsAdmin, artifacts);
         }

         if (result.isOK()) {
            result = checkAtsWorkDefinitions(isAtsAdmin, artifacts);
         }

         if (result.isOK()) {
            result = checkUsers(artifacts);
         }

         if (result.isOK()) {
            result = checkActions(isAtsAdmin, artifacts);
         }
      }

      return result;
   }

   private IStatus checkActions(boolean isAtsAdmin, Collection<Artifact> artifacts) {
      for (Artifact art : artifacts) {
         if (!isAtsAdmin && isWorkflowOrAction(art) && !isTask(art)) {
            return createStatus(String.format("Deletion of [%s] is only permitted by ATS Admin",
               art.getArtifactTypeName()));
         }
      }
      return Status.OK_STATUS;
   }

   private boolean isWorkflowOrAction(Artifact art) {
      return (art instanceof AbstractWorkflowArtifact) || (art instanceof IAtsAction);
   }

   private boolean isTask(Artifact art) {
      return art instanceof IAtsTask;
   }

   private IStatus createStatus(String message) {
      return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
   }

   private IStatus checkActionableItems(boolean isAtsAdmin, Collection<Artifact> artifacts) throws OseeCoreException {
      Set<String> aiaGuids = getActionableItemGuidsWithRecurse(new HashSet<String>(), artifacts);
      if (!aiaGuids.isEmpty()) {
         List<Artifact> teamWfsRelatedToAis =
            ArtifactQuery.getArtifactListFromTypeAndAttribute(AtsArtifactTypes.TeamWorkflow,
               AtsAttributeTypes.ActionableItem, aiaGuids, AtsUtilCore.getAtsBranch(), 10);
         if (!teamWfsRelatedToAis.isEmpty()) {
            return createStatus(String.format(
               "Actionable Items (or children AIs) [%s] selected to delete have related Team Workflows; Delete or re-assign Team Workflows first.",
               aiaGuids));
         }
         if (!isAtsAdmin) {
            return createStatus("Deletion of Actionable Items is only permitted by ATS Admin.");
         }
      }
      return Status.OK_STATUS;
   }

   private Set<String> getActionableItemGuidsWithRecurse(HashSet<String> aiaGuids, Collection<Artifact> artifacts) {
      for (Artifact art : artifacts) {
         if (art.isOfType(AtsArtifactTypes.ActionableItem)) {
            IAtsActionableItem aia =
               AtsClientService.get().getConfig().getSoleByGuid(art.getGuid(), IAtsActionableItem.class);
            if (aia != null) {
               aiaGuids.add(aia.getGuid());
               Collection<Artifact> childArts = art.getChildren();
               if (!aia.getChildrenActionableItems().isEmpty()) {
                  getActionableItemGuidsWithRecurse(aiaGuids, childArts);
               }
            }
         }
      }
      return aiaGuids;
   }

   private IStatus checkTeamDefinitions(boolean isAtsAdmin, Collection<Artifact> artifacts) throws OseeCoreException {
      List<String> guids = new ArrayList<String>();
      for (Artifact art : artifacts) {
         if (art.isOfType(AtsArtifactTypes.TeamDefinition)) {
            guids.add(art.getGuid());
         }
      }
      if (!guids.isEmpty()) {
         List<Artifact> artifactListFromIds =
            ArtifactQuery.getArtifactListFromAttributeValues(AtsAttributeTypes.TeamDefinition, guids,
               AtsUtilCore.getAtsBranch(), 5);
         if (artifactListFromIds.size() > 0) {
            return createStatus(String.format(
               "Team Definition (or children Team Definitions) [%s] selected to delete have related Team Workflows; Delete or re-assign Team Workflows first.",
               guids));
         }
         if (!isAtsAdmin) {
            return createStatus("Deletion of Team Definitions is only permitted by ATS Admin.");
         }
      }
      return Status.OK_STATUS;
   }

   private IStatus checkAtsWorkDefinitions(boolean isAtsAdmin, Collection<Artifact> artifacts) throws OseeCoreException {
      for (Artifact art : artifacts) {
         if (art.isOfType(AtsArtifactTypes.WorkDefinition)) {
            List<Artifact> artifactListFromTypeAndAttribute =
               ArtifactQuery.getArtifactListFromTypeAndAttribute(AtsArtifactTypes.WorkDefinition,
                  AtsAttributeTypes.WorkflowDefinition, art.getName(), AtsUtilCore.getAtsBranch());
            if (artifactListFromTypeAndAttribute.size() > 0) {
               return createStatus(String.format(
                  "ATS WorkDefinition [%s] selected to delete has ats.WorkDefinition attributes set to it's name in %d artifact.  These must be changed first.",
                  art, artifactListFromTypeAndAttribute.size()));
            }
            if (!isAtsAdmin) {
               return createStatus("Deletion of Work Definitions is only permitted by ATS Admin.");
            }
         }
      }
      return Status.OK_STATUS;
   }

   private IStatus checkUsers(Collection<Artifact> artifacts) throws OseeCoreException {
      Set<User> users = new HashSet<User>();
      for (Artifact art : artifacts) {
         if (art instanceof User) {
            users.add((User) art);
         }
      }
      for (User user : users) {
         UserRelatedToAtsObjectSearch srch =
            new UserRelatedToAtsObjectSearch(AtsClientService.get().getUserServiceClient().getUserFromOseeUser(user), false);
         if (srch.getResults().size() > 0) {
            return createStatus(String.format(
               "User name: \"%s\" userId: \"%s\" selected to delete has related ATS Objects; Un-relate to ATS first before deleting.",
               user.getName(), user.getUserId()));
         }
      }
      return Status.OK_STATUS;
   }

   public static void setDeletionChecksEnabled(boolean deletionChecksEnabled) {
      AtsArtifactChecks.deletionChecksEnabled = deletionChecksEnabled;
   }
}