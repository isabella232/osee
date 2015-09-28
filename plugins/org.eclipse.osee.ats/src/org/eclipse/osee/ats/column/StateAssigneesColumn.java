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
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
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
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class StateAssigneesColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   private final String stateName;

   public StateAssigneesColumn(String stateName) {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + "." + stateName + ".stateAssignee", String.format(
         "State [%s] Assignees", stateName), 80, SWT.LEFT, false, SortDataType.String, false, String.format(
         "User assigned to state [%s]", stateName));
      this.stateName = stateName;
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public StateAssigneesColumn copy() {
      StateAssigneesColumn newXCol = new StateAssigneesColumn(this.getStateName());
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof AbstractWorkflowArtifact) {
            AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) element;
            Set<IAtsUser> users = new HashSet<>();
            users.addAll(awa.getStateMgr().getAssignees(stateName));
            return AtsObjects.toString(";", users);
         } else if (Artifacts.isOfType(element, AtsArtifactTypes.Action)) {
            Set<IAtsUser> users = new HashSet<>();
            for (TeamWorkFlowArtifact team : ActionManager.getTeams(element)) {
               users.addAll(team.getStateMgr().getAssignees(stateName));
            }
            return AtsObjects.toString(";", users);

         }
      } catch (OseeCoreException ex) {
         LogUtil.getCellExceptionString(ex);
      }
      return "";
   }

   public String getStateName() {
      return stateName;
   }
}
