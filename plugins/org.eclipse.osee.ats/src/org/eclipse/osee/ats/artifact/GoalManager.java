/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.artifact;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.core.client.artifact.GoalArtifact;
import org.eclipse.osee.ats.core.config.TeamDefinitions;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.goal.MembersLabelProvider;
import org.eclipse.osee.ats.goal.MembersViewerSorter;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.ArrayTreeContentProvider;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.FilteredTreeArtifactDialog;

/**
 * @author Donald G. Dunne
 */
public class GoalManager extends MembersManager<GoalArtifact> {

   /**
    * change goal, prompt if member of two goals
    */
   public GoalArtifact promptChangeGoalOrder(Artifact artifact) throws OseeCoreException {
      if (!isHasCollector(artifact)) {
         AWorkbench.popup(String.format("No Goal set for artifact [%s]", artifact));
         return null;
      }
      Collection<Artifact> goals = getCollectors(artifact, false);
      GoalArtifact goal = null;
      if (goals.size() == 1) {
         goal = (GoalArtifact) goals.iterator().next();
      } else if (goals.size() > 1) {
         FilteredTreeArtifactDialog dialog =
            new FilteredTreeArtifactDialog("Select Goal", "Artifact has multiple Goals\n\nSelect Goal to change order",
               goals, new ArrayTreeContentProvider(), new MembersLabelProvider(), new MembersViewerSorter());
         dialog.setMultiSelect(false);
         if (dialog.open() == 0) {
            goal = (GoalArtifact) dialog.getSelectedFirst();
         } else {
            return null;
         }
      }
      return promptChangeMemberOrder(goal, artifact);
   }

   public static GoalArtifact createGoal(String title, IAtsChangeSet changes) throws OseeCoreException {
      GoalArtifact goalArt =
         (GoalArtifact) ArtifactTypeManager.addArtifact(AtsArtifactTypes.Goal, AtsUtilCore.getAtsBranch(), title);

      AtsClientService.get().getUtilService().setAtsId(AtsClientService.get().getSequenceProvider(), goalArt,
         TeamDefinitions.getTopTeamDefinition(AtsClientService.get().getConfig()), changes);

      // Initialize state machine
      goalArt.initializeNewStateMachine(Arrays.asList(AtsClientService.get().getUserService().getCurrentUser()),
         new Date(), AtsClientService.get().getUserService().getCurrentUser(), changes);

      changes.add(goalArt);
      return goalArt;
   }

   @Override
   public IRelationTypeSide getMembersRelationTypeSide() {
      return AtsRelationTypes.Goal_Member;
   }

   @Override
   public String getItemName() {
      return "Goal";
   }

   @Override
   public IArtifactType getArtifactType() {
      return AtsArtifactTypes.Goal;
   }

   @Override
   public String getMemberOrder(GoalArtifact goalArt, Artifact member) throws OseeCoreException {
      return AtsClientService.get().getGoalMembersCache().getMemberOrder(goalArt, member);
   }

}
