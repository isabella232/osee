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
import java.util.logging.Level;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerValueColumn;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class OperationalImpactColumn extends XViewerValueColumn {

   public static OperationalImpactColumn instance = new OperationalImpactColumn();

   public static OperationalImpactColumn getInstance() {
      return instance;
   }

   private OperationalImpactColumn() {
      super("ats.Operational Impact", "Operational Impact", 80, SWT.LEFT, false, SortDataType.String, true,
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
      if (AtsUtil.isAtsArtifact(element)) {
         try {
            return getOperationalImpact((Artifact) element);
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
            return LogUtil.getCellExceptionString(ex);
         }
      }
      return "";
   }

   private String getOperationalImpact(Artifact art) throws OseeCoreException {
      if (art.isOfType(AtsArtifactTypes.TeamWorkflow)) {
         return ((TeamWorkFlowArtifact) art).getSoleAttributeValue(AtsAttributeTypes.OperationalImpact, "");
      }
      if (art.isOfType(AtsArtifactTypes.Action)) {
         Set<String> strs = new HashSet<String>();
         for (TeamWorkFlowArtifact team : ActionManager.getTeams(art)) {
            strs.add(getOperationalImpact(team));
         }
         return Collections.toString(", ", strs);
      }
      if (art.isOfType(AtsArtifactTypes.Task)) {
         return getOperationalImpact(((TaskArtifact) art).getParentTeamWorkflow());
      }
      return "";
   }

}
