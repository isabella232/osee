/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.column;

import java.util.Collection;
import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerAlign;
import org.eclipse.nebula.widgets.xviewer.core.model.SortDataType;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.review.IAtsAbstractReview;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.util.PercentCompleteTotalUtil;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;

/**
 * @author Donald G. Dunne
 */
public class PercentCompleteTasksReviewsColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static PercentCompleteTasksReviewsColumn instance = new PercentCompleteTasksReviewsColumn();

   public static PercentCompleteTasksReviewsColumn getInstance() {
      return instance;
   }

   private PercentCompleteTasksReviewsColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".taskReviewPercentComplete", "Task and Review Percent Complete", 40,
         XViewerAlign.Center, false, SortDataType.Percent, false,
         "Percent Complete for the tasks and reviews related to the workflow.\n\nCalculation: total percent of all tasks and reviews / number of tasks and reviews");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public PercentCompleteTasksReviewsColumn copy() {
      PercentCompleteTasksReviewsColumn newXCol = new PercentCompleteTasksReviewsColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof Artifact) {
            return String.valueOf(getPercentCompleteFromTasksAndReviews((Artifact) element));
         }
      } catch (OseeCoreException ex) {
         return LogUtil.getCellExceptionString(ex);
      }
      return "";
   }

   /**
    * Return Percent Complete ONLY on tasks. Total Percent / # Tasks
    */
   public static int getPercentCompleteFromTasksAndReviews(Artifact artifact) throws OseeCoreException {
      if (artifact.isOfType(AtsArtifactTypes.Action)) {
         double percent = 0;
         for (TeamWorkFlowArtifact team : ActionManager.getTeams(artifact)) {
            if (!team.isCancelled()) {
               percent += getPercentCompleteFromTasksAndReviews(team);
            }
         }
         if (percent == 0) {
            return 0;
         }
         Double rollPercent = percent / ActionManager.getTeams(artifact).size();
         return rollPercent.intValue();
      }
      int spent = 0;
      int size = 0;
      if (artifact instanceof TeamWorkFlowArtifact) {
         TeamWorkFlowArtifact TeamWorkFlowArtifact = (TeamWorkFlowArtifact) artifact;
         Collection<TaskArtifact> taskArts = TeamWorkFlowArtifact.getTaskArtifacts();
         for (TaskArtifact taskArt : taskArts) {
            spent += PercentCompleteTotalUtil.getPercentCompleteTotal(taskArt, AtsClientService.get().getServices());
         }
         size = taskArts.size();
      }
      if (artifact instanceof TeamWorkFlowArtifact) {
         TeamWorkFlowArtifact teamWf = (TeamWorkFlowArtifact) artifact;
         Collection<IAtsAbstractReview> reviewArts = AtsClientService.get().getReviewService().getReviews(teamWf);
         for (IAtsAbstractReview reviewArt : reviewArts) {
            spent += PercentCompleteTotalUtil.getPercentCompleteTotal(reviewArt, AtsClientService.get().getServices());
         }
         size += reviewArts.size();
      }
      if (size == 0) {
         return 0;
      }
      return spent / size;
   }

}
