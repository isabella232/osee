/*********************************************************************
 * Copyright (c) 2010 Boeing
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

package org.eclipse.osee.ats.ide.column;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.core.model.SortDataType;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerAlign;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.ats.ide.internal.AtsApiService;
import org.eclipse.osee.ats.ide.util.widgets.dialog.StateListDialog;
import org.eclipse.osee.ats.ide.util.xviewer.column.XViewerAtsAttributeValueColumn;
import org.eclipse.osee.ats.ide.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.ide.workflow.task.TaskArtifact;
import org.eclipse.osee.ats.ide.workflow.teamwf.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.ide.world.WorldXViewerFactory;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Donald G. Dunne
 */
public class RelatedToStateColumn extends XViewerAtsAttributeValueColumn {

   public static RelatedToStateColumn instance = new RelatedToStateColumn();
   public static String NONE = "<empty>";
   public static String RELATED_TO_STATE_SELECTION = "State task must be completed or empty for completed state";

   public static RelatedToStateColumn getInstance() {
      return instance;
   }

   private RelatedToStateColumn() {
      super(AtsAttributeTypes.RelatedToState, WorldXViewerFactory.COLUMN_NAMESPACE + ".relatedToState",
         AtsAttributeTypes.RelatedToState.getUnqualifiedName(), 80, XViewerAlign.Left, false, SortDataType.String, true,
         "");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public RelatedToStateColumn copy() {
      RelatedToStateColumn newXCol = new RelatedToStateColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   public static boolean promptChangeRelatedToState(AbstractWorkflowArtifact sma) {
      if (sma.isTask()) {
         return promptChangeRelatedToState(Arrays.asList((TaskArtifact) sma));
      } else {
         AWorkbench.popup("Select Tasks to change Related-to-State");
      }
      return false;
   }

   public static boolean promptChangeRelatedToState(final Collection<? extends TaskArtifact> tasks) {
      if (tasks.isEmpty()) {
         AWorkbench.popup("Select Tasks to change Related-to-State");
         return false;
      }
      try {
         List<String> validStates = new ArrayList<>();
         validStates.add(NONE);
         validStates.addAll(RelatedToStateColumn.getValidInWorkStates(tasks.iterator().next().getParentTeamWorkflow()));
         final StateListDialog dialog =
            new StateListDialog("Change Related-to-State", RELATED_TO_STATE_SELECTION, validStates);
         if (tasks.size() == 1) {
            String selectedState = tasks.iterator().next().getSoleAttributeValue(AtsAttributeTypes.RelatedToState, "");
            if (Strings.isValid(selectedState)) {
               dialog.setInitialSelections(new Object[] {selectedState});
            }
         }
         if (dialog.open() == 0) {
            String selectedState = dialog.getSelectedState();
            if (selectedState.isEmpty()) {
               AWorkbench.popup("No Related-to-State selected");
               return false;
            } else if (selectedState.equals(NONE)) {
               selectedState = "";
            }
            IAtsChangeSet changes = AtsApiService.get().createChangeSet("ATS Prompt Change Related-to-State");
            for (TaskArtifact task : tasks) {
               String state = task.getSoleAttributeValue(AtsAttributeTypes.RelatedToState, "");
               if (!state.equals(selectedState)) {
                  if (Strings.isInValid(selectedState)) {
                     changes.deleteAttributes((IAtsWorkItem) task, AtsAttributeTypes.RelatedToState);
                  } else {
                     changes.setSoleAttributeValue((IAtsWorkItem) task, AtsAttributeTypes.RelatedToState,
                        selectedState);
                  }
               }
            }
            changes.executeIfNeeded();
         }
         return true;
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Can't change Related-to-State", ex);
         return false;
      }
   }

   public static List<String> getValidInWorkStates(TeamWorkFlowArtifact teamArt) {
      List<String> names = new ArrayList<>();
      for (String state : AtsApiService.get().getWorkDefinitionService().getStateNames(teamArt.getWorkDefinition())) {
         if (teamArt.getStateDefinitionByName(state).getStateType().isWorkingState()) {
            names.add(state);
         }
      }
      Collections.sort(names);
      return names;
   }

   @Override
   public boolean handleAltLeftClick(TreeColumn treeColumn, TreeItem treeItem) {
      try {
         if (Artifacts.isOfType(treeItem.getData(), AtsArtifactTypes.Task)) {
            TaskArtifact taskArt = (TaskArtifact) treeItem.getData();
            boolean modified = promptChangeRelatedToState(taskArt);
            XViewer xViewer = (XViewer) ((XViewerColumn) treeColumn.getData()).getXViewer();
            if (modified && isPersistViewer(xViewer)) {
               taskArt.persist("persist related-to-state via alt-left-click");
            }
            if (modified) {
               xViewer.update(taskArt, null);
               return true;
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return false;
   }

   @Override
   public void handleColumnMultiEdit(TreeColumn treeColumn, Collection<TreeItem> treeItems) {
      Set<TaskArtifact> tasks = new HashSet<>();
      for (TreeItem item : treeItems) {
         if (item.getData() instanceof Artifact) {
            Artifact art = AtsApiService.get().getQueryServiceIde().getArtifact(item);
            if (art.isOfType(AtsArtifactTypes.Task)) {
               tasks.add((TaskArtifact) art);
            }
         }
      }
      if (tasks.isEmpty()) {
         AWorkbench.popup("Invalid selection for setting related-to-state.");
         return;
      }
      promptChangeRelatedToState(tasks);
   }

}
