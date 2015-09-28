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
package org.eclipse.osee.ats.column;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.PromptChangeUtil;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsAttributeValueColumn;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Donald G. Dunne
 */
public class PriorityColumn extends XViewerAtsAttributeValueColumn {

   public static PriorityColumn instance = new PriorityColumn();

   public static PriorityColumn getInstance() {
      return instance;
   }

   private PriorityColumn() {
      super(AtsAttributeTypes.PriorityType, 20, SWT.LEFT, true, SortDataType.String, true, "");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public PriorityColumn copy() {
      PriorityColumn newXCol = new PriorityColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   public static boolean promptChangePriority(final Collection<? extends TeamWorkFlowArtifact> teams, boolean persist) {

      try {
         for (TeamWorkFlowArtifact team : teams) {
            if (AtsClientService.get().getVersionService().isReleased(team) || AtsClientService.get().getVersionService().isVersionLocked(
               team)) {
               AWorkbench.popup("ERROR",
                  "Team Workflow\n \"" + team.getName() + "\"\n version is locked or already released.");
               return false;
            }
         }
         PromptChangeUtil.promptChangeAttribute(teams, AtsAttributeTypes.PriorityType, persist, false);
         return true;
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Can't change priority", ex);
         return false;
      }
   }

   @Override
   public boolean handleAltLeftClick(TreeColumn treeColumn, TreeItem treeItem) {
      try {
         // Only prompt change for sole attribute types
         if (AttributeTypeManager.getMaxOccurrences(getAttributeType()) != 1) {
            return false;
         }
         if (treeItem.getData() instanceof Artifact) {
            Artifact useArt = (Artifact) treeItem.getData();
            if (useArt.isOfType(AtsArtifactTypes.Action)) {
               if (ActionManager.getTeams(useArt).size() == 1) {
                  useArt = ActionManager.getFirstTeam(useArt);
               } else {
                  return false;
               }
            }
            if (!(useArt.isOfType(AtsArtifactTypes.TeamWorkflow))) {
               return false;
            }
            TeamWorkFlowArtifact team = (TeamWorkFlowArtifact) useArt;
            if (AtsClientService.get().getVersionService().isReleased(team) || AtsClientService.get().getVersionService().isVersionLocked(team)) {
               AWorkbench.popup("ERROR",
                  "Team Workflow\n \"" + team.getName() + "\"\n version is locked or already released.");
               return false;
            }

            boolean modified = promptChangePriority(Arrays.asList((TeamWorkFlowArtifact) useArt), isPersistViewer());
            XViewer xViewer = ((XViewerColumn) treeColumn.getData()).getTreeViewer();
            if (modified && isPersistViewer(xViewer)) {
               useArt.persist("persist priority via alt-left-click");
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

   @Override
   public void handleColumnMultiEdit(TreeColumn treeColumn, Collection<TreeItem> treeItems) {
      Set<TeamWorkFlowArtifact> awas = new HashSet<>();
      for (TreeItem item : treeItems) {
         Artifact art = (Artifact) item.getData();
         if (art.isOfType(AtsArtifactTypes.TeamWorkflow)) {
            awas.add((TeamWorkFlowArtifact) art);
         }
      }
      if (awas.isEmpty()) {
         AWorkbench.popup("Must select Team Workflows");
         return;
      }
      promptChangePriority(awas, true);
      return;
   }

}
