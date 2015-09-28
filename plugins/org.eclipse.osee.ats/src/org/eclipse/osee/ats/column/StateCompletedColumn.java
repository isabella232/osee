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
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workflow.log.IAtsLogItem;
import org.eclipse.osee.ats.api.workflow.log.LogType;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.util.AtsObjects;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class StateCompletedColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   private final String stateName;

   public StateCompletedColumn(String stateName) {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + "." + stateName + ".stateCompleted", String.format(
         "State [%s] Completed", stateName), 80, SWT.LEFT, false, SortDataType.String, false, String.format(
         "Date state [%s] was completed", stateName));
      this.stateName = stateName;
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public StateCompletedColumn copy() {
      StateCompletedColumn newXCol = new StateCompletedColumn(this.getStateName());
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof AbstractWorkflowArtifact) {
            AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) element;
            IAtsStateDefinition state = awa.getStateDefinitionByName(stateName);
            if (state != null) {
               String date = getCompletedDateByState(awa, state);
               return date;
            }
         } else if (Artifacts.isOfType(element, AtsArtifactTypes.Action)) {
            Set<String> dates = new HashSet<>();
            for (TeamWorkFlowArtifact team : ActionManager.getTeams(element)) {
               IAtsStateDefinition state = team.getStateDefinitionByName(stateName);
               if (state != null) {
                  String date = getCompletedDateByState(team, state);
                  if (Strings.isValid(date)) {
                     dates.add(date);
                  }
               }
            }
            return AtsObjects.toString(";", dates);

         }
      } catch (OseeCoreException ex) {
         LogUtil.getCellExceptionString(ex);
      }
      return "";
   }

   String getCompletedDateByState(IAtsWorkItem workItem, IAtsStateDefinition state) throws OseeCoreException {
      IAtsLogItem stateEvent = workItem.getLog().getStateEvent(LogType.StateComplete, state.getName());
      if (stateEvent != null && stateEvent.getDate() != null) {
         return DateUtil.getMMDDYYHHMM(stateEvent.getDate());
      }
      return "";
   }

   public String getStateName() {
      return stateName;
   }
}
