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

import java.util.HashSet;
import java.util.Set;
import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerAlign;
import org.eclipse.nebula.widgets.xviewer.core.model.SortDataType;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.util.AtsObjects;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;

/**
 * @author Donald G. Dunne
 */
public class CompletedCancelledByColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static CompletedCancelledByColumn instance = new CompletedCancelledByColumn();

   public static CompletedCancelledByColumn getInstance() {
      return instance;
   }

   private CompletedCancelledByColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".cmpCnclBy", "Completed or Cancelled By", 80, XViewerAlign.Left, false,
         SortDataType.String, false, "User transitioning action to completed or cancelled state.");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public CompletedCancelledByColumn copy() {
      CompletedCancelledByColumn newXCol = new CompletedCancelledByColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof AbstractWorkflowArtifact) {
            if (((AbstractWorkflowArtifact) element).isCompleted()) {
               return CompletedByColumn.getInstance().getColumnText(element, column, columnIndex);
            } else if (((AbstractWorkflowArtifact) element).isCancelled()) {
               return CancelledByColumn.getInstance().getColumnText(element, column, columnIndex);
            }
         } else if (Artifacts.isOfType(element, AtsArtifactTypes.Action)) {
            Set<IAtsUser> users = new HashSet<>();
            for (TeamWorkFlowArtifact team : ActionManager.getTeams(element)) {
               IAtsUser user = team.getCompletedBy();
               if (((AbstractWorkflowArtifact) element).isCompleted()) {
                  user = team.getCompletedBy();
               } else if (((AbstractWorkflowArtifact) element).isCancelled()) {
                  user = team.getCancelledBy();
               }
               if (user != null) {
                  users.add(user);
               }
            }
            return AtsObjects.toString(";", users);

         }
      } catch (OseeCoreException ex) {
         LogUtil.getCellExceptionString(ex);
      }
      return "";
   }
}
