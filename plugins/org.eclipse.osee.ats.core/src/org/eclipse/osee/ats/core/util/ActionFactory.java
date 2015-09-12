/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.notify.AtsNotificationEventFactory;
import org.eclipse.osee.ats.api.notify.AtsNotifyType;
import org.eclipse.osee.ats.api.team.ChangeType;
import org.eclipse.osee.ats.api.team.CreateTeamOption;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.team.IAtsWorkItemFactory;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.user.IAtsUserService;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.util.IAtsUtilService;
import org.eclipse.osee.ats.api.util.ISequenceProvider;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.IAttributeResolver;
import org.eclipse.osee.ats.api.workdef.IRelationResolver;
import org.eclipse.osee.ats.api.workflow.IAtsAction;
import org.eclipse.osee.ats.api.workflow.IAtsGoal;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.IAtsWorkItemService;
import org.eclipse.osee.ats.api.workflow.INewActionListener;
import org.eclipse.osee.ats.api.workflow.log.LogType;
import org.eclipse.osee.ats.api.workflow.state.IAtsStateFactory;
import org.eclipse.osee.ats.api.workflow.state.IAtsStateManager;
import org.eclipse.osee.ats.core.ai.ActionableItemManager;
import org.eclipse.osee.ats.core.config.IAtsConfig;
import org.eclipse.osee.ats.core.config.TeamDefinitions;
import org.eclipse.osee.ats.core.users.AtsCoreUsers;
import org.eclipse.osee.ats.core.workflow.state.StateManagerUtility;
import org.eclipse.osee.ats.core.workflow.transition.TransitionManager;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Donald G. Dunne
 */
public class ActionFactory implements IAtsActionFactory {

   private final IAtsWorkItemFactory workItemFactory;
   private final IAtsUtilService utilService;
   private final ISequenceProvider sequenceProvider;
   private final IAtsWorkItemService workItemService;
   private final ActionableItemManager actionableItemManager;
   private final IAtsUserService userService;
   private final IAttributeResolver attrResolver;
   private final IAtsStateFactory stateFactory;
   private final IAtsConfig config;
   private final IAtsServices atsServices;

   public ActionFactory(IAtsWorkItemFactory workItemFactory, IAtsUtilService utilService, ISequenceProvider sequenceProvider, IAtsWorkItemService workItemService, ActionableItemManager actionableItemManager, IAtsUserService userService, IAttributeResolver attrResolver, IAtsStateFactory stateFactory, IAtsConfig config, IAtsServices atsServices) {
      this.workItemFactory = workItemFactory;
      this.utilService = utilService;
      this.sequenceProvider = sequenceProvider;
      this.workItemService = workItemService;
      this.actionableItemManager = actionableItemManager;
      this.userService = userService;
      this.attrResolver = attrResolver;
      this.stateFactory = stateFactory;
      this.config = config;
      this.atsServices = atsServices;
   }

   @Override
   public Pair<IAtsAction, Collection<IAtsTeamWorkflow>> createAction(IAtsUser user, String title, String desc, ChangeType changeType, String priority, boolean validationRequired, Date needByDate, Collection<IAtsActionableItem> actionableItems, Date createdDate, IAtsUser createdBy, INewActionListener newActionListener, IAtsChangeSet changes) throws OseeCoreException {
      Conditions.checkNotNullOrEmptyOrContainNull(actionableItems, "actionableItems");
      // if "tt" is title, this is an action created for development. To
      // make it easier, all fields are automatically filled in for ATS developer

      Object actionArt = changes.createArtifact(AtsArtifactTypes.Action, title);
      IAtsAction action = workItemFactory.getAction((ArtifactId) actionArt);
      IAtsTeamDefinition topTeamDefinition = TeamDefinitions.getTopTeamDefinition(config);
      utilService.setAtsId(sequenceProvider, action, topTeamDefinition, changes);
      changes.add(action);
      setArtifactIdentifyData(action, title, desc, changeType, priority, validationRequired, needByDate, changes);

      // Retrieve Team Definitions corresponding to selected Actionable Items
      Collection<IAtsTeamDefinition> teamDefs = TeamDefinitions.getImpactedTeamDefs(actionableItems);
      if (teamDefs.isEmpty()) {
         StringBuffer sb = new StringBuffer("No teams returned for Action's selected Actionable Items\n");
         for (IAtsActionableItem aia : actionableItems) {
            sb.append("Selected AI \"" + aia + "\" " + aia.getUuid() + "\n");
         }
         throw new OseeStateException(sb.toString());
      }

      // Create team workflow artifacts
      List<IAtsTeamWorkflow> teamWfs = new ArrayList<IAtsTeamWorkflow>();
      for (IAtsTeamDefinition teamDef : teamDefs) {
         List<IAtsUser> leads = new LinkedList<IAtsUser>(teamDef.getLeads(actionableItems));
         IAtsTeamWorkflow teamWf =
            createTeamWorkflow(action, teamDef, actionableItems, leads, changes, createdDate, createdBy,
               newActionListener);
         teamWfs.add(teamWf);
         changes.add(teamWf);
      }

      // Notify listener of action creation
      if (newActionListener != null) {
         newActionListener.actionCreated(action);
      }

      changes.add(action);
      return new Pair<IAtsAction, Collection<IAtsTeamWorkflow>>(action, teamWfs);
   }

   @Override
   public IAtsTeamWorkflow createTeamWorkflow(IAtsAction action, IAtsTeamDefinition teamDef, Collection<IAtsActionableItem> actionableItems, List<? extends IAtsUser> assignees, IAtsChangeSet changes, Date createdDate, IAtsUser createdBy, INewActionListener newActionListener, CreateTeamOption... createTeamOption) throws OseeCoreException {
      IArtifactType teamWorkflowArtifactType = getTeamWorkflowArtifactType(teamDef);

      // NOTE: The persist of the workflow will auto-email the assignees
      IAtsTeamWorkflow teamWf =
         createTeamWorkflow(action, teamDef, actionableItems, assignees, createdDate, createdBy, null,
            teamWorkflowArtifactType, newActionListener, changes, createTeamOption);
      return teamWf;
   }

   private IArtifactType getTeamWorkflowArtifactType(IAtsTeamDefinition teamDef) throws OseeCoreException {
      Conditions.checkNotNull(teamDef, "teamDef");
      IArtifactType teamWorkflowArtifactType = AtsArtifactTypes.TeamWorkflow;
      if (teamDef.getStoreObject() != null) {
         String artifactTypeName =
            attrResolver.getSoleAttributeValue(teamDef, AtsAttributeTypes.TeamWorkflowArtifactType, null);
         if (Strings.isValid(artifactTypeName)) {
            boolean found = false;
            for (IArtifactType type : atsServices.getArtifactTypes()) {
               if (type.getName().equals(artifactTypeName)) {
                  teamWorkflowArtifactType = type;
                  found = true;
                  break;
               }
            }
            if (!found) {
               throw new OseeArgumentException(
                  "Team Workflow Artifact Type name [%s] off Team Definition %s could not be found.", artifactTypeName,
                  teamDef.toStringWithId());
            }
         }
      }
      return teamWorkflowArtifactType;
   }

   @Override
   public IAtsTeamWorkflow createTeamWorkflow(IAtsAction action, IAtsTeamDefinition teamDef, Collection<IAtsActionableItem> actionableItems, List<? extends IAtsUser> assignees, Date createdDate, IAtsUser createdBy, String guid, IArtifactType artifactType, INewActionListener newActionListener, IAtsChangeSet changes, CreateTeamOption... createTeamOption) throws OseeCoreException {

      if (!Collections.getAggregate(createTeamOption).contains(CreateTeamOption.Duplicate_If_Exists)) {
         // Make sure team doesn't already exist
         for (IAtsTeamWorkflow teamArt : workItemService.getTeams(action)) {
            if (teamArt.getTeamDefinition().equals(teamDef)) {
               throw new OseeArgumentException("Team [%s] already exists for Action [%s]", teamDef,
                  atsServices.getAtsId(action));
            }
         }
      }

      IAtsTeamWorkflow teamWf = null;
      if (guid == null) {
         teamWf = workItemFactory.getTeamWf(changes.createArtifact(artifactType, ""));
      } else {
         teamWf = workItemFactory.getTeamWf(changes.createArtifact(artifactType, "", guid));
      }

      setArtifactIdentifyData(action, teamWf, changes);

      // Relate Workflow to ActionableItems (by guid) if team is responsible
      // for that AI
      for (IAtsActionableItem aia : actionableItems) {
         IAtsTeamDefinition teamDefinitionInherited = aia.getTeamDefinitionInherited();
         if (teamDefinitionInherited != null && teamDef.getUuid() == teamDefinitionInherited.getUuid()) {
            actionableItemManager.addActionableItem(teamWf, aia, changes);
         }
      }

      // Relate WorkFlow to Team Definition (by guid due to relation loading issues)
      changes.setSoleAttributeValue(teamWf, AtsAttributeTypes.TeamDefinition, AtsUtilCore.getGuid(teamDef));

      utilService.setAtsId(sequenceProvider, teamWf, teamWf.getTeamDefinition(), changes);

      // If work def id is specified by listener, set as attribute
      if (newActionListener != null) {
         String overrideWorkDefId = newActionListener.getOverrideWorkDefinitionId(teamWf);
         if (Strings.isValid(overrideWorkDefId)) {
            changes.setSoleAttributeValue(teamWf, AtsAttributeTypes.WorkflowDefinition, overrideWorkDefId);
         }
      }

      // Initialize state machine
      String workDefinitionName = getWorkDefinitionName(teamDef);
      if (!Strings.isValid(workDefinitionName)) {
         throw new OseeStateException("Work Definition for Team Def [%s] does not exist", teamDef);
      }
      initializeNewStateMachine(teamWf, assignees, createdDate, createdBy, changes);

      // Notify listener of team creation
      if (newActionListener != null) {
         newActionListener.teamCreated(action, teamWf, changes);
      }

      // Relate Action to WorkFlow
      changes.relate(action, AtsRelationTypes.ActionToWorkflow_WorkFlow, teamWf);

      // Auto-add actions to configured goals
      addActionToConfiguredGoal(teamDef, teamWf, actionableItems, changes);

      changes.add(teamWf);

      changes.getNotifications().addWorkItemNotificationEvent(
         AtsNotificationEventFactory.getWorkItemNotificationEvent(AtsCoreUsers.SYSTEM_USER, teamWf,
            AtsNotifyType.SubscribedTeamOrAi));

      return teamWf;
   }

   public String getWorkDefinitionName(IAtsTeamDefinition teamDef) throws OseeCoreException {
      String workDefName =
         attrResolver.getSoleAttributeValueAsString(teamDef, AtsAttributeTypes.WorkflowDefinition, null);
      if (Strings.isValid(workDefName)) {
         return workDefName;
      }

      IAtsTeamDefinition parentTeamDef = teamDef.getParentTeamDef();
      if (parentTeamDef == null) {
         return "WorkDef_Team_Default";
      }
      return getWorkDefinitionName(parentTeamDef);
   }

   @Override
   public void initializeNewStateMachine(IAtsWorkItem workItem, List<? extends IAtsUser> assignees, Date createdDate, IAtsUser createdBy, IAtsChangeSet changes) throws OseeCoreException {
      Conditions.checkNotNull(createdDate, "createdDate");
      Conditions.checkNotNull(createdBy, "createdBy");
      Conditions.checkNotNull(changes, "changes");
      IAtsStateDefinition startState = workItem.getWorkDefinition().getStartState();
      IAtsStateManager stateManager = stateFactory.getStateManager(workItem);
      workItem.setStateManager(stateManager);
      StateManagerUtility.initializeStateMachine(workItem.getStateMgr(), startState, assignees,
         (createdBy == null ? userService.getCurrentUser() : createdBy), changes);
      IAtsUser user = createdBy == null ? userService.getCurrentUser() : createdBy;
      setCreatedBy(workItem, user, true, createdDate, changes);
      TransitionManager.logStateStartedEvent(workItem, startState, createdDate, user);
   }

   private void logCreatedByChange(IAtsWorkItem workItem, IAtsUser user, Date date) throws OseeCoreException {
      if (attrResolver.getSoleAttributeValue(workItem, AtsAttributeTypes.CreatedBy, null) == null) {
         workItem.getLog().addLog(LogType.Originated, "", "", date, user.getUserId());
      } else {
         workItem.getLog().addLog(LogType.Originated, "", "Changed by " + userService.getCurrentUser().getName(), date,
            user.getUserId());
         workItem.getLog().internalResetOriginator(user);
      }
   }

   public void setCreatedBy(IAtsWorkItem workItem, IAtsUser user, boolean logChange, Date date, IAtsChangeSet changes) throws OseeCoreException {
      if (logChange) {
         logCreatedByChange(workItem, user, date);
      }

      if (attrResolver.isAttributeTypeValid(workItem, AtsAttributeTypes.CreatedBy)) {
         changes.setSoleAttributeValue(workItem, AtsAttributeTypes.CreatedBy, user.getUserId());
      }
      if (attrResolver.isAttributeTypeValid(workItem, AtsAttributeTypes.CreatedDate)) {
         changes.setSoleAttributeValue(workItem, AtsAttributeTypes.CreatedDate, date);
      }
      changes.getNotifications().addWorkItemNotificationEvent(
         AtsNotificationEventFactory.getWorkItemNotificationEvent(userService.getCurrentUser(), workItem,
            AtsNotifyType.Originator));
   }

   /**
    * Auto-add actions to a goal configured with relations to the given ActionableItem or Team Definition
    */
   @Override
   public void addActionToConfiguredGoal(IAtsTeamDefinition teamDef, IAtsTeamWorkflow teamWf, Collection<IAtsActionableItem> actionableItems, IAtsChangeSet changes) throws OseeCoreException {
      // Auto-add this team artifact to configured goals
      IRelationResolver relationResolver = atsServices.getRelationResolver();
      for (IAtsGoal goal : relationResolver.getRelated(teamDef, AtsRelationTypes.AutoAddActionToGoal_Goal,
         IAtsGoal.class)) {
         if (!relationResolver.areRelated(goal, AtsRelationTypes.Goal_Member, teamWf)) {
            changes.relate(goal, AtsRelationTypes.Goal_Member, teamWf);
            changes.add(goal);
         }
      }

      // Auto-add this actionable item to configured goals
      for (IAtsActionableItem aia : actionableItems) {
         for (IAtsGoal goal : relationResolver.getRelated(aia, AtsRelationTypes.AutoAddActionToGoal_Goal,
            IAtsGoal.class)) {
            if (!relationResolver.areRelated(goal, AtsRelationTypes.Goal_Member, teamWf)) {
               changes.relate(goal, AtsRelationTypes.Goal_Member, teamWf);
               changes.add(goal);
            }
         }
      }
   }

   /**
    * Set Team Workflow attributes off given action artifact
    */
   public void setArtifactIdentifyData(IAtsAction fromAction, IAtsTeamWorkflow toTeam, IAtsChangeSet changes) throws OseeCoreException {
      Conditions.checkNotNull(fromAction, "fromAction");
      Conditions.checkNotNull(toTeam, "toTeam");
      Conditions.checkNotNull(changes, "changes");
      setArtifactIdentifyData(toTeam, fromAction.getName(),
         attrResolver.getSoleAttributeValue(fromAction, AtsAttributeTypes.Description, ""),
         atsServices.getChangeType(fromAction),
         attrResolver.getSoleAttributeValue(fromAction, AtsAttributeTypes.PriorityType, ""),
         attrResolver.getSoleAttributeValue(fromAction, AtsAttributeTypes.ValidationRequired, false),
         attrResolver.getSoleAttributeValue(fromAction, AtsAttributeTypes.NeedBy, (Date) null), changes);
   }

   /**
    * Since there is no shared attribute yet, action and workflow arts are all populate with identify data
    */
   public void setArtifactIdentifyData(IAtsObject atsObject, String title, String desc, ChangeType changeType, String priority, Boolean validationRequired, Date needByDate, IAtsChangeSet changes) throws OseeCoreException {
      changes.setSoleAttributeValue(atsObject, CoreAttributeTypes.Name, title);
      if (Strings.isValid(desc)) {
         changes.addAttribute(atsObject, AtsAttributeTypes.Description, desc);
      }
      atsServices.setChangeType(atsObject, changeType, changes);
      if (Strings.isValid(priority)) {
         changes.addAttribute(atsObject, AtsAttributeTypes.PriorityType, priority);
      }
      if (needByDate != null) {
         changes.addAttribute(atsObject, AtsAttributeTypes.NeedBy, needByDate);
      }
      if (validationRequired) {
         changes.addAttribute(atsObject, AtsAttributeTypes.ValidationRequired, true);
      }
   }

   @Override
   public Collection<IAtsTeamWorkflow> getSiblingTeamWorkflows(IAtsTeamWorkflow teamWf) {
      List<IAtsTeamWorkflow> teams = new LinkedList<IAtsTeamWorkflow>();
      IAtsAction action = getAction(teamWf);
      for (IAtsTeamWorkflow teamArt : atsServices.getRelationResolver().getRelated(action,
         AtsRelationTypes.ActionToWorkflow_WorkFlow, IAtsTeamWorkflow.class)) {
         if (!teamArt.equals(teamWf)) {
            teams.add(atsServices.getWorkItemFactory().getTeamWf((ArtifactId) teamArt));
         }
      }
      return teams;
   }

   @Override
   public IAtsAction getAction(IAtsTeamWorkflow teamWf) {
      return atsServices.getRelationResolver().getRelatedOrNull(teamWf, AtsRelationTypes.ActionToWorkflow_Action,
         IAtsAction.class);
   }

}
