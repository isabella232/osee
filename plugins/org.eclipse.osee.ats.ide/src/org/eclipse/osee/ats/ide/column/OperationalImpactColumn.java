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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.nebula.widgets.xviewer.XViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.core.model.SortDataType;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerAlign;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.core.util.AtsObjects;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.ats.ide.internal.AtsApiService;
import org.eclipse.osee.ats.ide.workflow.task.TaskArtifact;
import org.eclipse.osee.ats.ide.workflow.teamwf.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;

/**
 * @author Donald G. Dunne
 */
public class OperationalImpactColumn extends XViewerValueColumn {

   public static OperationalImpactColumn instance = new OperationalImpactColumn();

   public static OperationalImpactColumn getInstance() {
      return instance;
   }

   private OperationalImpactColumn() {
      super("ats.Operational Impact", "Operational Impact", 80, XViewerAlign.Left, false, SortDataType.String, true,
         "Does this change have an operational impact to the product.");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public OperationalImpactColumn copy() {
      OperationalImpactColumn newXCol = new OperationalImpactColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      if (AtsObjects.isAtsWorkItemOrAction(element)) {
         try {
            return getOperationalImpact(AtsApiService.get().getQueryServiceIde().getArtifact(element));
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
            return LogUtil.getCellExceptionString(ex);
         }
      }
      return "";
   }

   private String getOperationalImpact(Artifact art) {
      if (art.isOfType(AtsArtifactTypes.TeamWorkflow)) {
         return ((TeamWorkFlowArtifact) art).getSoleAttributeValue(AtsAttributeTypes.OperationalImpact, "");
      }
      if (art.isOfType(AtsArtifactTypes.Action)) {
         Set<String> strs = new HashSet<>();
         for (IAtsTeamWorkflow team : AtsApiService.get().getWorkItemService().getTeams(art)) {
            strs.add(getOperationalImpact(AtsApiService.get().getQueryServiceIde().getArtifact(team)));
         }
         return Collections.toString(", ", strs);
      }
      if (art.isOfType(AtsArtifactTypes.Task)) {
         return getOperationalImpact(((TaskArtifact) art).getParentTeamWorkflow());
      }
      return "";
   }

}
