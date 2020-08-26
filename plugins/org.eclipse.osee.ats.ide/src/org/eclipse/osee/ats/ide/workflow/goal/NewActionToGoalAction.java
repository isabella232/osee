/*********************************************************************
 * Copyright (c) 2016 Boeing
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

package org.eclipse.osee.ats.ide.workflow.goal;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.workflow.IAtsAction;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.INewActionListener;
import org.eclipse.osee.ats.ide.AtsImage;
import org.eclipse.osee.ats.ide.actions.ISelectedAtsArtifacts;
import org.eclipse.osee.ats.ide.actions.wizard.NewActionWizard;
import org.eclipse.osee.ats.ide.editor.tab.members.IMemberProvider;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.ats.ide.internal.AtsApiService;
import org.eclipse.osee.ats.ide.workflow.CollectorArtifact;
import org.eclipse.osee.ats.ide.workflow.sprint.SprintArtifact;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.ui.PlatformUI;

/**
 * @author Donald G. Dunne
 */
public class NewActionToGoalAction extends Action {

   private final CollectorArtifact collectorArt;
   private final ISelectedAtsArtifacts selectedAtsArtifacts;
   private final IMemberProvider memberProvider;

   public static interface RemovedFromCollectorHandler {
      void removedFromCollector(Collection<? extends Artifact> removed);
   }

   public NewActionToGoalAction(IMemberProvider memberProvider, CollectorArtifact collectorArt, ISelectedAtsArtifacts selectedAtsArtifacts) {
      super("Create New Action At This Location");
      this.memberProvider = memberProvider;
      this.collectorArt = collectorArt;
      this.selectedAtsArtifacts = selectedAtsArtifacts;
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(AtsImage.NEW_ACTION);
   }

   @Override
   public void run() {
      try {
         Collection<? extends Artifact> selected = selectedAtsArtifacts.getSelectedAtsArtifacts();
         final Artifact dropTarget = selected.isEmpty() ? null : selected.iterator().next();
         List<IAtsActionableItem> ais = new LinkedList<>();
         if (memberProvider.isBacklog() || memberProvider.isSprint()) {
            Artifact agileTeam = null;
            if (memberProvider.isBacklog()) {
               agileTeam = collectorArt.getRelatedArtifact(AtsRelationTypes.AgileTeamToBacklog_AgileTeam);
            } else {
               agileTeam = collectorArt.getRelatedArtifact(AtsRelationTypes.AgileTeamToSprint_AgileTeam);
            }
            List<Artifact> atsTeams = agileTeam.getRelatedArtifacts(AtsRelationTypes.AgileTeamToAtsTeam_AtsTeam);
            for (Artifact atsTeam : atsTeams) {
               for (Artifact ai : atsTeam.getRelatedArtifacts(AtsRelationTypes.TeamActionableItem_ActionableItem)) {
                  ais.add(AtsApiService.get().getActionableItemService().getActionableItemById(ai));
               }
            }
         }
         NewActionWizard wizard = new NewActionWizard();
         if (!ais.isEmpty()) {
            wizard.getSelectableAis(ais);
         }
         wizard.setNewActionListener(new INewActionListener() {

            @Override
            public void teamCreated(IAtsAction action, IAtsTeamWorkflow teamWf, IAtsChangeSet changes) {
               List<Artifact> related = collectorArt.getRelatedArtifacts(memberProvider.getMemberRelationTypeSide());
               if (!related.contains(teamWf.getStoreObject())) {
                  changes.relate(collectorArt, memberProvider.getMemberRelationTypeSide(), teamWf.getStoreObject());
               }
               if (dropTarget != null) {
                  collectorArt.setRelationOrder(memberProvider.getMemberRelationTypeSide(), dropTarget, false,
                     AtsApiService.get().getQueryServiceIde().getArtifact(teamWf));
                  if (collectorArt.isOfType(AtsArtifactTypes.Goal)) {
                     AtsApiService.get().getGoalMembersCache().decache((GoalArtifact) collectorArt);
                  } else if (memberProvider.isSprint()) {
                     AtsApiService.get().getSprintItemsCache().decache((SprintArtifact) collectorArt);
                  }
               }

               changes.add(collectorArt);
            }

         });
         wizard.setOpenOnComplete(false);
         WizardDialog dialog =
            new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
         dialog.create();
         dialog.open();
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public String getText() {
      return selectedAtsArtifacts.getSelectedWorkflowArtifacts().isEmpty() ? "Create New Action at End" : "Create New Action At This Location";
   }

   public void refreshText() {
      setText(getText());
   }
}
