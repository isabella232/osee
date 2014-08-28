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
package org.eclipse.osee.ats.client.demo.navigate;

import java.util.Arrays;
import java.util.Date;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.api.team.ChangeType;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.artifact.GoalManager;
import org.eclipse.osee.ats.client.demo.internal.AtsClientService;
import org.eclipse.osee.ats.core.client.action.ActionArtifact;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.artifact.GoalArtifact;
import org.eclipse.osee.ats.core.client.review.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.core.client.review.PeerToPeerReviewManager;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.AtsChangeSet;
import org.eclipse.osee.ats.core.config.ActionableItems;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.world.WorldEditor;
import org.eclipse.osee.ats.world.WorldEditorSimpleProvider;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * @author Donald G. Dunne
 */
public class CreateGoalTestDemoArtifacts extends XNavigateItemAction {
   private Date createdDate;
   private IAtsUser createdBy;

   public CreateGoalTestDemoArtifacts(XNavigateItem parent) {
      super(parent, "Create Test Goal Artifacts - Demo", AtsImage.GOAL);
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) throws OseeCoreException {
      if (AtsUtil.isProductionDb()) {
         AWorkbench.popup("Can't be run on production");
         return;
      }
      if (!MessageDialog.openConfirm(Displays.getActiveShell(), getName(), getName())) {
         return;
      }
      createdDate = new Date();
      AtsChangeSet changes = new AtsChangeSet(getName());
      createdBy = AtsClientService.get().getUserService().getCurrentUser();
      GoalArtifact sawCodeGoal = GoalManager.createGoal("SAW Code", changes);
      GoalArtifact sawTestGoal = GoalManager.createGoal("SAW Test", changes);
      GoalArtifact toolsTeamGoal = GoalManager.createGoal("Tools Team", changes);
      GoalArtifact facilitiesGoal = GoalManager.createGoal("Facilities Team", changes);
      GoalArtifact cisReqGoal = GoalManager.createGoal("CIS Requirements", changes);

      TeamWorkFlowArtifact teamArt = createAction1(changes, sawCodeGoal);
      createAction2(changes, sawCodeGoal, cisReqGoal);
      createAction3(changes, sawTestGoal, cisReqGoal);
      teamArt = createAction456(changes, sawCodeGoal, facilitiesGoal, teamArt);
      createAction7(changes, facilitiesGoal);

      for (String name : Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
         "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "AA", "BB", "CC", "DD", "EE", "FF", "GG", "HH", "II", "JJ",
         "KK", "LL", "MM", "NN", "OO", "PP", "QQ", "RR")) {
         TaskArtifact taskArt = teamArt.createNewTask("Task " + name, createdDate, createdBy, changes);
         toolsTeamGoal.addMember(taskArt);
         changes.add(taskArt);
      }

      changes.add(toolsTeamGoal);
      changes.execute();
      WorldEditor.open(new WorldEditorSimpleProvider("Goals", Arrays.asList(sawCodeGoal, sawTestGoal, toolsTeamGoal,
         facilitiesGoal, cisReqGoal)));

   }

   private void createAction7(IAtsChangeSet changes, GoalArtifact facilitiesGoal) throws OseeCoreException {
      Artifact action =
         ActionManager.createAction(null, "Add the Improvement", "Description", ChangeType.Improvement, "4", false,
            null, ActionableItems.getActionableItems(Arrays.asList("Network")), createdDate, createdBy, null, changes);
      facilitiesGoal.addMember(action);
      changes.add(facilitiesGoal);
   }

   private TeamWorkFlowArtifact createAction456(IAtsChangeSet changes, GoalArtifact sawCodeGoal, GoalArtifact facilitiesGoal, TeamWorkFlowArtifact teamArt) throws OseeCoreException {
      for (String msaTool : Arrays.asList("Backups", "Computers", "Network")) {
         Artifact action =
            ActionManager.createAction(null, "Fix " + msaTool + " button", "Description", ChangeType.Problem, "4",
               false, null, ActionableItems.getActionableItems(Arrays.asList(msaTool)), createdDate, createdBy, null,
               changes);
         facilitiesGoal.addMember(ActionManager.getFirstTeam(action));
         teamArt = ActionManager.getFirstTeam(action);
         TaskArtifact taskArt = teamArt.createNewTask("Task 1", createdDate, createdBy, changes);
         sawCodeGoal.addMember(taskArt);
         changes.add(taskArt);
         taskArt = teamArt.createNewTask("Task 2", createdDate, createdBy, changes);
         facilitiesGoal.addMember(taskArt);
         changes.add(taskArt);
      }
      return teamArt;
   }

   private void createAction3(IAtsChangeSet changes, GoalArtifact sawCodeGoal, GoalArtifact cisReqGoal) throws OseeCoreException {
      Artifact action =
         ActionManager.createAction(null, "Remove Workflow button", "Description", ChangeType.Problem, "4", false,
            null, ActionableItems.getActionableItems(Arrays.asList("SAW Code", "CIS Requirements")), createdDate,
            createdBy, null, changes);
      sawCodeGoal.addMember(ActionManager.getFirstTeam(action));
      cisReqGoal.addMember(ActionManager.getFirstTeam(action));
   }

   private void createAction2(IAtsChangeSet changes, GoalArtifact sawCodeGoal, GoalArtifact cisReqGoal) throws OseeCoreException {
      ActionArtifact action =
         ActionManager.createAction(null, "Add CDB Check Signals", "Description", ChangeType.Problem, "4", false, null,
            ActionableItems.getActionableItems(Arrays.asList("SAW Code", "CIS Requirements")), createdDate, createdBy,
            null, changes);
      sawCodeGoal.addMember(ActionManager.getFirstTeam(action));
      cisReqGoal.addMember(ActionManager.getFirstTeam(action));
   }

   private TeamWorkFlowArtifact createAction1(IAtsChangeSet changes, GoalArtifact sawCodeGoal) throws OseeCoreException {
      Artifact action =
         ActionManager.createAction(null, "Fix this model", "Description", ChangeType.Problem, "2", false, null,
            ActionableItems.getActionableItems(Arrays.asList("SAW Code")), createdDate, createdBy, null, changes);
      sawCodeGoal.addMember(ActionManager.getFirstTeam(action));
      TeamWorkFlowArtifact teamArt = ActionManager.getFirstTeam(action);
      PeerToPeerReviewArtifact peerReviewArt =
         PeerToPeerReviewManager.createNewPeerToPeerReview(teamArt, "New Review", "Implement", changes);
      sawCodeGoal.addMember(peerReviewArt);
      return teamArt;
   }
}
