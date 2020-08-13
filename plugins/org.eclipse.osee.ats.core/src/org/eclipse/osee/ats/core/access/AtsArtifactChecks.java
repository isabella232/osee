/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.ats.core.access;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.util.AtsUtil;
import org.eclipse.osee.ats.api.workflow.IAtsAction;
import org.eclipse.osee.ats.api.workflow.IAtsTask;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.core.internal.AtsApiService;
import org.eclipse.osee.framework.core.access.ArtifactCheck;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.data.RelationTypeToken;
import org.eclipse.osee.framework.core.data.UserToken;
import org.eclipse.osee.framework.jdk.core.result.XResultData;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * Check for certain conditions that must be met to delete an ATS object or User artifact.
 *
 * @author Donald G. Dunne
 */
public class AtsArtifactChecks extends ArtifactCheck {

   private static final List<Long> Admin_Only_Relation_Type_Ids = Arrays.asList(
      AtsRelationTypes.TeamWorkflowToReview_Review.getId(), AtsRelationTypes.ActionToWorkflow_Action.getId());
   private static boolean deletionChecksEnabled = true;

   private boolean isDeletionChecksEnabled() {
      return deletionChecksEnabled && !AtsUtil.isInTest();
   }

   @Override
   public XResultData isDeleteableRelation(ArtifactToken artifact, RelationTypeToken relationType, XResultData results) {
      if (isDeletionChecksEnabled()) {
         if (Admin_Only_Relation_Type_Ids.contains(
            relationType.getId()) && !AtsApiService.get().getUserService().isAtsAdmin()) {
            results.errorf("Deletion of relation type [%s] off artifact [%s] is only permitted by ATS Admin",
               relationType, artifact);
         }
      }
      return results;
   }

   @Override
   public XResultData isDeleteable(Collection<ArtifactToken> artifacts, XResultData results) {

      if (isDeletionChecksEnabled()) {
         AtsApi atsApi = AtsApiService.get();
         Set<ArtifactToken> allArtifacts = getAllArtifacts(atsApi, artifacts, new HashSet<>());

         if (allArtifacts.isEmpty()) {
            return results;
         }

         boolean isAtsAdmin = atsApi.getUserService().isAtsAdmin();

         checkActionableItems(isAtsAdmin, atsApi, allArtifacts, results);
         checkTeamDefinitions(isAtsAdmin, atsApi, allArtifacts, results);
         checkAtsWorkDefinitions(isAtsAdmin, atsApi, allArtifacts, results);
         checkUsers(atsApi, allArtifacts, results);
         checkActions(isAtsAdmin, atsApi, allArtifacts, results);
         checkWorkPackages(isAtsAdmin, atsApi, allArtifacts, results);
      }

      return results;
   }

   // Get all artifacts and recurse down default hierarchy
   private Set<ArtifactToken> getAllArtifacts(AtsApi atsApi, Collection<ArtifactToken> artifacts, Set<ArtifactToken> allArtifacts) {
      for (ArtifactToken art : artifacts) {
         if (art.getBranch().equals(atsApi.getAtsBranch())) {
            allArtifacts.addAll(atsApi.getStoreService().getDescendants(art));
         }
      }
      return allArtifacts;
   }

   private IStatus checkActions(boolean isAtsAdmin, AtsApi atsApi, Collection<ArtifactToken> artifacts, XResultData results) {
      for (ArtifactToken art : artifacts) {
         if (!isAtsAdmin && isWorkflowOrAction(art) && !isTask(art)) {
            results.errorf("Deletion of [%s] is only permitted by ATS Admin; %s invalid",
               atsApi.getStoreService().getArtifactTypeName(art), art.toStringWithId());
         }
         String error = isWorkflowOrActionPermittedByAnyone(atsApi, art, artifacts);
         if (Strings.isValid(error)) {
            results.errorf("Deletion of artifact type [%s] object %s is not permitted. Error: [%s]",
               atsApi.getStoreService().getArtifactTypeName(art), art.toStringWithId(), error);
         }
      }
      return Status.OK_STATUS;
   }

   private String isWorkflowOrActionPermittedByAnyone(AtsApi atsApi, ArtifactToken art, Collection<ArtifactToken> allArtifacts) {
      if (art.isOfType(AtsArtifactTypes.Action)) {
         for (IAtsTeamWorkflow teamWf : atsApi.getWorkItemService().getTeams(art)) {
            if (!allArtifacts.contains(teamWf.getStoreObject())) {
               return String.format("Can't delete action %s without deleting workflow %s, use ATS World Editor",
                  art.toStringWithId(), teamWf.toStringWithId());
            }
         }
      }
      if (art.isOfType(AtsArtifactTypes.TeamWorkflow)) {
         IAtsTeamWorkflow teamWf = atsApi.getWorkItemService().getTeamWf(art);
         if (!allArtifacts.contains(teamWf.getParentAction().getStoreObject())) {
            return String.format("Can't delete workflow %s without deleting action %s, use ATS World Editor",
               teamWf.toStringWithId(), teamWf.getParentAction().getStoreObject().toStringWithId());
         }
      }
      return null;
   }

   private boolean isWorkflowOrAction(ArtifactToken art) {
      return art instanceof IAtsWorkItem || art instanceof IAtsAction;
   }

   private boolean isTask(ArtifactToken art) {
      return art instanceof IAtsTask;
   }

   private IStatus checkActionableItems(boolean isAtsAdmin, AtsApi atsApi, Collection<ArtifactToken> artifacts, XResultData results) {
      Set<ArtifactToken> aiIds = getActionableItemIdsWithRecurse(new HashSet<>(), artifacts, atsApi, results);
      if (!aiIds.isEmpty()) {
         List<ArtifactToken> teamWfsRelatedToAis = atsApi.getQueryService().getArtifactListFromTypeAndAttribute(
            AtsArtifactTypes.TeamWorkflow, AtsAttributeTypes.ActionableItemReference, aiIds, atsApi.getAtsBranch());
         if (!teamWfsRelatedToAis.isEmpty()) {
            results.errorf(
               "Actionable Items (or children AIs) [%s] selected to delete have related Team Workflows; Delete or re-assign Team Workflows first.",
               aiIds);
         }
         if (!isAtsAdmin) {
            results.error("Deletion of Actionable Items is only permitted by ATS Admin.");
         }
      }
      return Status.OK_STATUS;
   }

   private Set<ArtifactToken> getActionableItemIdsWithRecurse(HashSet<ArtifactToken> aiIds, Collection<ArtifactToken> artifacts, AtsApi atsApi, XResultData results) {
      for (ArtifactToken art : artifacts) {
         if (art.isOfType(AtsArtifactTypes.ActionableItem)) {
            IAtsActionableItem ai = atsApi.getActionableItemService().getActionableItemById(art);
            if (ai != null) {
               aiIds.add(ArtifactToken.valueOf(ai.getId(), ai.getName()));
               Collection<ArtifactToken> childArts = atsApi.getRelationResolver().getChildren(ai.getStoreObject());
               if (!ai.getChildrenActionableItems().isEmpty()) {
                  getActionableItemIdsWithRecurse(aiIds, childArts, atsApi, results);
               }
            }
         }
      }
      return aiIds;
   }

   private IStatus checkTeamDefinitions(boolean isAtsAdmin, AtsApi atsApi, Collection<ArtifactToken> artifacts, XResultData results) {
      List<String> ids = new ArrayList<>();
      for (ArtifactToken art : artifacts) {
         if (art.isOfType(AtsArtifactTypes.TeamDefinition)) {
            ids.add(art.getIdString());
         }
      }
      if (!ids.isEmpty()) {
         List<ArtifactToken> artifactListFromIds = atsApi.getQueryService().getArtifactListFromAttributeValues(
            AtsAttributeTypes.TeamDefinitionReference, ids, atsApi.getAtsBranch(), 5);
         if (artifactListFromIds.size() > 0) {
            results.errorf(
               "Team Definition (or children Team Definitions) [%s] selected to delete have related Team Workflows; Delete or re-assign Team Workflows first.",
               ids);
         }
         if (!isAtsAdmin) {
            results.error("Deletion of Team Definitions is only permitted by ATS Admin.");
         }
      }
      return Status.OK_STATUS;
   }

   private IStatus checkWorkPackages(boolean isAtsAdmin, AtsApi atsApi, Collection<ArtifactToken> artifacts, XResultData results) {
      List<ArtifactToken> ids = new ArrayList<>();
      for (ArtifactToken art : artifacts) {
         if (art.isOfType(AtsArtifactTypes.WorkPackage)) {
            ids.add(art);
         }
      }
      if (!ids.isEmpty()) {
         List<ArtifactToken> artifactListFromIds = atsApi.getQueryService().getArtifactListFromAttributeValues(
            AtsAttributeTypes.WorkPackageReference, ids, atsApi.getAtsBranch());
         if (artifactListFromIds.size() > 0) {
            results.errorf(
               "Work Packages [%s] selected to delete have related Work Items; Delete or re-assign Work Packages first.",
               ids);
         }
      }
      return Status.OK_STATUS;
   }

   private IStatus checkAtsWorkDefinitions(boolean isAtsAdmin, AtsApi atsApi, Collection<ArtifactToken> artifacts, XResultData results) {
      for (ArtifactToken art : artifacts) {
         // legacy work definition check (remove after 26.0)
         if (art.isOfType(AtsArtifactTypes.WorkDefinition)) {
            List<ArtifactToken> artifactListFromTypeAndAttribute =
               atsApi.getQueryService().getArtifactListFromTypeAndAttribute(AtsArtifactTypes.WorkDefinition,
                  AtsAttributeTypes.WorkflowDefinition, art.getName(), atsApi.getAtsBranch());
            if (artifactListFromTypeAndAttribute.size() > 0) {
               results.errorf(
                  "ATS WorkDefinition [%s] selected to delete has ats.WorkDefinition attributes set to it's name in %d artifact.  These must be changed first.",
                  art, artifactListFromTypeAndAttribute.size());
            }
            if (!isAtsAdmin) {
               results.error("Deletion of Work Definitions is only permitted by ATS Admin.");
            }
         }
         if (art.isOfType(AtsArtifactTypes.WorkDefinition)) {
            List<ArtifactToken> artifactListFromTypeAndAttribute =
               atsApi.getQueryService().getArtifactListFromTypeAndAttribute(AtsArtifactTypes.WorkDefinition,
                  AtsAttributeTypes.WorkflowDefinitionReference, art.getIdString(), atsApi.getAtsBranch());
            if (artifactListFromTypeAndAttribute.size() > 0) {
               results.errorf(
                  "ATS WorkDefinition [%s] selected to delete has ats.WorkDefinitionReference attributes set to it's name in %d artifact.  These must be changed first.",
                  art, artifactListFromTypeAndAttribute.size());
            }
            if (!isAtsAdmin) {
               results.error("Deletion of Work Definitions is only permitted by ATS Admin.");
            }
         }

      }
      return Status.OK_STATUS;
   }

   private IStatus checkUsers(AtsApi atsApi, Collection<ArtifactToken> artifacts, XResultData results) {
      Set<UserToken> users = new HashSet<>();
      for (ArtifactId art : artifacts) {
         if (art instanceof UserToken) {
            users.add((UserToken) art);
         }
      }
      for (UserToken user : users) {
         UserRelatedToAtsObjectSearch srch =
            new UserRelatedToAtsObjectSearch(atsApi.getUserService().getUserById(user), false, atsApi);
         if (srch.getResults().size() > 0) {
            results.errorf(
               "User name: \"%s\" userId: \"%s\" selected to delete has related ATS Objects; Un-relate to ATS first before deleting.",
               user.getName(), user.getUserId());
         }
      }
      return Status.OK_STATUS;
   }

   public static void setDeletionChecksEnabled(boolean deletionChecksEnabled) {
      AtsArtifactChecks.deletionChecksEnabled = deletionChecksEnabled;
   }
}