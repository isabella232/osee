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
package org.eclipse.osee.ats.goal;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerSorter;
import org.eclipse.nebula.widgets.xviewer.customize.CustomizeData;
import org.eclipse.osee.ats.column.AssigneeColumnUI;
import org.eclipse.osee.ats.column.ChangeTypeColumn;
import org.eclipse.osee.ats.column.CreatedDateColumn;
import org.eclipse.osee.ats.column.GoalOrderColumn;
import org.eclipse.osee.ats.column.GoalOrderVoteColumn;
import org.eclipse.osee.ats.column.AtsIdColumn;
import org.eclipse.osee.ats.column.NotesColumn;
import org.eclipse.osee.ats.column.PriorityColumn;
import org.eclipse.osee.ats.column.StateColumn;
import org.eclipse.osee.ats.column.TargetedVersionColumn;
import org.eclipse.osee.ats.column.TitleColumn;
import org.eclipse.osee.ats.column.TypeColumn;
import org.eclipse.osee.ats.core.client.artifact.GoalArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.world.AtsWorldEditorItems;
import org.eclipse.osee.ats.world.IAtsWorldEditorItem;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.ats.world.WorldXViewerSorter;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.skynet.SkynetXViewerFactory;

/**
 * @author Donald G. Dunne
 */
public class GoalXViewerFactory extends SkynetXViewerFactory {

   private GoalArtifact soleGoalArtifact;
   @SuppressWarnings("unchecked")
   public static final List<? extends XViewerColumn> GoalViewerVisibleColumns = Arrays.asList(
      GoalOrderColumn.getInstance(), GoalOrderVoteColumn.getInstance(), TitleColumn.getInstance(),
      TypeColumn.getInstance(), StateColumn.getInstance(), PriorityColumn.getInstance(),
      ChangeTypeColumn.getInstance(), AssigneeColumnUI.getInstance(), new AtsIdColumn(true),
      CreatedDateColumn.getInstance(), TargetedVersionColumn.getInstance(), NotesColumn.getInstance());
   public static Integer[] widths = new Integer[] {
      GoalOrderColumn.getInstance().getWidth(),
      GoalOrderVoteColumn.getInstance().getWidth(),
      250,
      60,
      60,
      20,
      20,
      100,
      50,
      50,
      50,
      80};

   public GoalXViewerFactory(GoalArtifact soleGoalArtifact) {
      super("org.eclipse.osee.ats.GoalXViewer");
      this.soleGoalArtifact = soleGoalArtifact;
      int widthIndex = 0;
      // Create new column from world columns but set show and width for task
      for (XViewerColumn taskCol : GoalViewerVisibleColumns) {
         XViewerColumn newCol = taskCol.copy();
         newCol.setShow(true);
         newCol.setWidth(widths[widthIndex++]);
         registerColumns(newCol);
      }
      // Add remaining columns from world columns
      for (XViewerColumn worldCol : WorldXViewerFactory.WorldViewColumns) {
         if (!GoalViewerVisibleColumns.contains(worldCol)) {
            XViewerColumn newCol = worldCol.copy();
            newCol.setShow(false);
            registerColumns(newCol);
         }
      }
      // Register any columns from other plugins
      try {
         for (IAtsWorldEditorItem item : AtsWorldEditorItems.getItems()) {
            for (XViewerColumn xCol : item.getXViewerColumns()) {
               registerColumns(xCol);
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      registerAllAttributeColumns();
      WorldXViewerFactory.registerStateColumns(this);
   }

   @Override
   public XViewerSorter createNewXSorter(XViewer xViewer) {
      return new WorldXViewerSorter(xViewer);
   }

   @Override
   public CustomizeData getDefaultTableCustomizeData() {
      CustomizeData customizeData = super.getDefaultTableCustomizeData();
      for (XViewerColumn xCol : customizeData.getColumnData().getColumns()) {
         if (xCol.getId().equals(GoalOrderColumn.getInstance().getId())) {
            xCol.setSortForward(true);
         }
      }
      customizeData.getSortingData().setSortingNames(GoalOrderColumn.getInstance().getId());
      return customizeData;
   }

   public GoalArtifact getSoleGoalArtifact() {
      return soleGoalArtifact;
   }

   public void setSoleGoalArtifact(GoalArtifact soleGoalArtifact) {
      this.soleGoalArtifact = soleGoalArtifact;
   }

}
