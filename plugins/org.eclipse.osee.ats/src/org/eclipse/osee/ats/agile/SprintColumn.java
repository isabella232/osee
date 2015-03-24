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
package org.eclipse.osee.ats.agile;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.xviewer.IAltLeftClickProvider;
import org.eclipse.nebula.widgets.xviewer.IMultiColumnEditProvider;
import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.api.agile.IAgileSprint;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Donald G. Dunne
 */
public class SprintColumn extends XViewerAtsColumn implements IXViewerValueColumn, IAltLeftClickProvider, IMultiColumnEditProvider {

   public static SprintColumn instance = new SprintColumn();

   public static SprintColumn getInstance() {
      return instance;
   }

   private SprintColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".sprint", "Sprint", 100, SWT.LEFT, false, SortDataType.String,
         true, "Sprint");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public SprintColumn copy() {
      SprintColumn newXCol = new SprintColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public boolean handleAltLeftClick(TreeColumn treeColumn, TreeItem treeItem) {
      try {
         if (treeItem.getData() instanceof Artifact) {
            Artifact useArt = (Artifact) treeItem.getData();
            if (!(useArt.isOfType(AtsArtifactTypes.AbstractWorkflowArtifact))) {
               return false;
            }

            boolean modified = promptChangeSprint(useArt, isPersistViewer());

            XViewer xViewer = ((XViewerColumn) treeColumn.getData()).getTreeViewer();
            if (modified && isPersistViewer(xViewer)) {
               useArt.persist("persist sprints via alt-left-click");
            }
            if (modified) {
               xViewer.update(useArt, null);
               return true;
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }

      return false;
   }

   public static boolean promptChangeSprint(Artifact awa, boolean persist) throws OseeCoreException {
      return promptChangeSprint(Arrays.asList(awa), persist);
   }

   public static boolean promptChangeSprint(final Collection<? extends Artifact> awas, boolean persist) throws OseeCoreException {
      // verify that all awas belong to the same backlog
      SprintItems items = new SprintItems(awas);

      if (items.isNoBacklogDetected()) {
         AWorkbench.popup("Workflow(s) must belong to a Backlog to set their Sprint.");
         return false;
      }
      if (items.isMultipleBacklogsDetected()) {
         AWorkbench.popup("All workflows must belong to same Backlog.");
         return false;
      }

      Artifact backlogArt = (Artifact) items.getCommonBacklog().getStoreObject();
      Artifact agileTeamArt = null;
      try {
         agileTeamArt = backlogArt.getRelatedArtifact(AtsRelationTypes.AgileTeamToBacklog_AgileTeam);
      } catch (ArtifactDoesNotExist ex) {
         // do nothing
      }
      if (agileTeamArt == null) {
         AWorkbench.popup("No Agile Team for Agile Backlog [%s]", backlogArt.toStringWithId());
      }

      Set<IAgileSprint> activeSprints = getActiveSprints(agileTeamArt);

      if (activeSprints.isEmpty()) {
         AWorkbench.popup("No Active Sprints available for the Agile Team [%s]", agileTeamArt.toStringWithId());
         return false;
      }

      SprintFilteredListDialog dialog = createDialog(items, activeSprints);

      if (dialog.open() == 0) {
         if (dialog.isRemoveFromSprint()) {
            for (Artifact awa : awas) {
               Collection<Artifact> relatedSprintArts = AgileUtilClient.getRelatedSprints(awa);
               for (Artifact relatedSprint : relatedSprintArts) {
                  awa.deleteRelation(AtsRelationTypes.AgileSprintToItem_Sprint, relatedSprint);
               }
            }
            Artifacts.persistInTransaction("Remove Sprint", awas);

         } else {
            IAgileSprint selectedSprint = dialog.getSelectedFirst();
            for (Artifact awa : awas) {
               Artifact newSprintArt = (Artifact) selectedSprint.getStoreObject();
               Collection<Artifact> relatedSprintArts = AgileUtilClient.getRelatedSprints(awa);
               for (Artifact relatedSprint : relatedSprintArts) {
                  awa.deleteRelation(AtsRelationTypes.AgileSprintToItem_Sprint, relatedSprint);
               }
               awa.addRelation(AtsRelationTypes.AgileSprintToItem_Sprint, newSprintArt);
            }
            Artifacts.persistInTransaction("Set Sprint", awas);
         }
         return true;
      }
      return false;
   }

   private static SprintFilteredListDialog createDialog(SprintItems items, Set<IAgileSprint> activeSprints) {
      SprintFilteredListDialog dialog = new SprintFilteredListDialog("Select Sprint", "Select Sprint", activeSprints);
      Window.setDefaultImage(ImageManager.getImage(AtsImage.AGILE_SPRINT));
      dialog.setInput(activeSprints);
      if (items.isCommonSelectedSprint() && items.getMultipleSprints().size() == 1) {
         dialog.setInitialSelections(Arrays.asList(items.getMultipleSprints().iterator().next()));
      }
      return dialog;
   }

   private static Set<IAgileSprint> getActiveSprints(Artifact agileTeamArt) {
      Set<IAgileSprint> activeSprints = new HashSet<IAgileSprint>();
      for (Artifact sprintArt : agileTeamArt.getRelatedArtifacts(AtsRelationTypes.AgileTeamToSprint_Sprint)) {
         IAgileSprint agileSprint = AtsClientService.get().getWorkItemFactory().getAgileSprint(sprintArt);
         if (agileSprint.isActive()) {
            activeSprints.add(agileSprint);
         }
      }
      return activeSprints;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof Artifact) {
            return Artifacts.toString("; ",
               ((Artifact) element).getRelatedArtifacts(AtsRelationTypes.AgileSprintToItem_Sprint));
         }
      } catch (OseeCoreException ex) {
         return LogUtil.getCellExceptionString(ex);
      }
      return "";
   }

   @Override
   public void handleColumnMultiEdit(TreeColumn treeColumn, Collection<TreeItem> treeItems) {
      try {
         Set<AbstractWorkflowArtifact> awas = new HashSet<AbstractWorkflowArtifact>();
         for (TreeItem item : treeItems) {
            Artifact art = (Artifact) item.getData();
            if (art instanceof AbstractWorkflowArtifact) {
               awas.add((AbstractWorkflowArtifact) art);
            }
         }
         promptChangeSprint(awas, true);
         return;
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

}
