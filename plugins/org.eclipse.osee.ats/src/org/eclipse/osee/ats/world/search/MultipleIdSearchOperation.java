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
package org.eclipse.osee.ats.world.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.osee.ats.AtsOpenOption;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.artifact.SmaWorkflowLabelProvider;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.search.AtsArtifactQuery;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.editor.SMAEditor;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.AtsBranchManager;
import org.eclipse.osee.ats.util.AtsEditor;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.LegacyPCRActions;
import org.eclipse.osee.ats.util.widgets.dialog.AtsObjectNameSorter;
import org.eclipse.osee.ats.world.IWorldEditorConsumer;
import org.eclipse.osee.ats.world.WorldEditor;
import org.eclipse.osee.ats.world.WorldEditorSimpleProvider;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.ArrayTreeContentProvider;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.skynet.ArtifactDecoratorPreferences;
import org.eclipse.osee.framework.ui.skynet.ArtifactLabelProvider;
import org.eclipse.osee.framework.ui.skynet.artifact.massEditor.MassArtifactEditor;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.FilteredCheckboxTreeDialog;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.ui.dialogs.ListDialog;

/**
 * @author Donald G. Dunne
 */
public class MultipleIdSearchOperation extends AbstractOperation implements IWorldEditorConsumer {
   private final Set<Artifact> resultAtsArts = new HashSet<>();
   private final Set<Artifact> resultNonAtsArts = new HashSet<>();
   private final Set<Artifact> artifacts = new HashSet<>();
   private final MultipleIdSearchData data;
   private boolean multiLine;

   public MultipleIdSearchOperation(MultipleIdSearchData data) {
      super(data.getName(), Activator.PLUGIN_ID);
      this.data = data;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      if (!data.hasValidInput()) {
         MultipleIdSearchUi ui = new MultipleIdSearchUi(data);
         ui.setMultiLine(multiLine);
         if (!ui.getInput()) {
            return;
         }
      }
      if (data.getIds().isEmpty()) {
         AWorkbench.popup("Must Enter Valid Id");
         return;
      }
      searchAndSplitResults();
      if (resultAtsArts.isEmpty() && resultNonAtsArts.isEmpty()) {
         AWorkbench.popup("Invalid ID/Guid/Legacy PCR Id(s): " + Collections.toString(", ", data.getIds()));
         return;
      }
      if (resultNonAtsArts.size() > 0) {
         MassArtifactEditor.editArtifacts("Open Artifact(s)", new ArrayList<Artifact>(resultNonAtsArts));
      }
      if (resultAtsArts.size() > 0) {
         // If requested world editor and it's already been opened there,
         // don't process other arts in editors
         if (data.getWorldEditor() != null && data.getAtsEditor() == AtsEditor.WorldEditor) {
            data.getWorldEditor().getWorldComposite().load(getName(), resultAtsArts, TableLoadOption.None);
         } else {
            if (data.getAtsEditor() == AtsEditor.WorkflowEditor) {
               openWorkflowEditor(resultAtsArts);
            } else if (data.getAtsEditor() == AtsEditor.ChangeReport) {
               openChangeReport(resultAtsArts, data.getEnteredIds());
            } else {
               WorldEditor.open(new WorldEditorSimpleProvider(getName(), resultAtsArts));
            }
         }
      }
   }

   private void openChangeReport(Set<Artifact> artifacts, final String enteredIds) throws OseeCoreException {
      final Set<Artifact> addedArts = new HashSet<>();
      for (Artifact artifact : artifacts) {
         if (artifact.isOfType(AtsArtifactTypes.Action)) {
            for (TeamWorkFlowArtifact team : ActionManager.getTeams(artifact)) {
               if (AtsClientService.get().getBranchService().isCommittedBranchExists(
                  team) || AtsClientService.get().getBranchService().isWorkingBranchInWork(team)) {
                  addedArts.add(team);
               }
            }
         }
         if (artifact.isOfType(AtsArtifactTypes.TeamWorkflow)) {
            TeamWorkFlowArtifact teamArt = (TeamWorkFlowArtifact) artifact;
            if (AtsClientService.get().getBranchService().isCommittedBranchExists(
               teamArt) || AtsClientService.get().getBranchService().isWorkingBranchInWork(teamArt)) {
               addedArts.add(artifact);
            }
         }
      }
      if (addedArts.size() == 1) {
         Displays.ensureInDisplayThread(new Runnable() {
            @Override
            public void run() {
               for (Artifact art : addedArts) {
                  if (art.isOfType(AtsArtifactTypes.TeamWorkflow)) {
                     AtsBranchManager.showChangeReport(((TeamWorkFlowArtifact) art));
                  }
               }
            }
         });
      } else if (addedArts.size() > 0) {
         Displays.ensureInDisplayThread(new Runnable() {
            @Override
            public void run() {
               ArtifactDecoratorPreferences artDecorator = new ArtifactDecoratorPreferences();
               artDecorator.setShowArtBranch(true);
               artDecorator.setShowArtType(true);
               artDecorator.setShowRelations(false);
               FilteredCheckboxTreeDialog dialog = new FilteredCheckboxTreeDialog("Select Available Change Reports",
                  "Select available Change Reports to run.", new ArrayTreeContentProvider(),
                  new ArtifactLabelProvider(artDecorator), new AtsObjectNameSorter());
               dialog.setInput(addedArts);
               if (dialog.open() == 0) {
                  if (dialog.getResult().length == 0) {
                     return;
                  }
                  for (Object obj : dialog.getResult()) {
                     AtsBranchManager.showChangeReport(((TeamWorkFlowArtifact) obj));
                  }
               }
            }
         });
      } else {
         Displays.ensureInDisplayThread(new Runnable() {
            @Override
            public void run() {
               MessageDialog.openInformation(Displays.getActiveShell(), "Open Change Reports",
                  "No change report exists for " + enteredIds);
            }
         });
      }
   }

   private void openWorkflowEditor(final Set<Artifact> resultAtsArts) {
      Displays.ensureInDisplayThread(new Runnable() {

         @Override
         public void run() {
            Artifact artifact = null;
            if (resultAtsArts.size() == 1) {
               artifact = resultAtsArts.iterator().next();
            } else {
               ListDialog ld = new ListDialog(Displays.getActiveShell());
               ld.setContentProvider(new ArrayContentProvider());
               ld.setLabelProvider(new SmaWorkflowLabelProvider());
               ld.setTitle("Select Workflow");
               ld.setMessage("Select Workflow");
               ld.setInput(resultAtsArts);
               if (ld.open() == 0) {
                  artifact = (Artifact) ld.getResult()[0];
               } else {
                  return;
               }
            }
            if (artifact.isOfType(AtsArtifactTypes.Action)) {
               AtsUtil.openATSAction(artifact, AtsOpenOption.OpenOneOrPopupSelect);
            } else {
               try {
                  SMAEditor.editArtifact(artifact);
               } catch (OseeCoreException ex) {
                  OseeLog.log(Activator.class, Level.SEVERE, ex);
               }
            }
         }
      });
   }

   private void searchAndSplitResults() throws OseeCoreException {
      resultAtsArts.addAll(
         LegacyPCRActions.getTeamsTeamWorkflowArtifacts(data.getIds(), (Collection<IAtsTeamDefinition>) null));

      // This does artId search
      if (data.isIncludeArtIds() && data.getBranchForIncludeArtIds() != null) {
         for (Artifact art : ArtifactQuery.getArtifactListFromIds(Lib.stringToIntegerList(data.getEnteredIds()),
            data.getBranchForIncludeArtIds())) {
            artifacts.add(art);
         }
      }

      // This does id/guid search
      List<String> validGuidsAndIds = data.getIds();
      if (!validGuidsAndIds.isEmpty()) {
         for (Artifact art : AtsArtifactQuery.getArtifactListFromIds(validGuidsAndIds)) {
            artifacts.add(art);
         }
      }

      for (Artifact art : artifacts) {
         if (art.isOfType(AtsArtifactTypes.AtsArtifact)) {
            resultAtsArts.add(art);
         } else {
            resultNonAtsArts.add(art);
         }
      }
   }

   @Override
   public void setWorldEditor(WorldEditor worldEditor) {
      data.setWorldEditor(worldEditor);
   }

   @Override
   public WorldEditor getWorldEditor() {
      return data.getWorldEditor();
   }

   @Override
   public String getName() {
      if (Strings.isValid(data.getEnteredIds())) {
         return String.format("%s - [%s]", super.getName(),
            Strings.truncate(data.getEnteredIds(), WorldEditor.TITLE_MAX_LENGTH));
      }
      return super.getName();
   }

   public void setMultiLine(boolean multiLine) {
      this.multiLine = multiLine;
   }

}
