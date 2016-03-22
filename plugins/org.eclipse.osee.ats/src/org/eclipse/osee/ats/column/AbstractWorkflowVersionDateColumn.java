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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerAlign;
import org.eclipse.nebula.widgets.xviewer.core.model.SortDataType;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsAttributeValueColumn;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;

/**
 * This class provides for a Date where the value is either stored in Workflow or Version or both.
 */
public abstract class AbstractWorkflowVersionDateColumn extends XViewerAtsAttributeValueColumn {

   public AbstractWorkflowVersionDateColumn(String id, IAttributeType attributeType) {
      super(attributeType, id, attributeType.getUnqualifiedName(), 80, XViewerAlign.Left, false, SortDataType.Date, true, "");
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (Artifacts.isOfType(element, AtsArtifactTypes.Action)) {
            Set<String> strs = new HashSet<>();
            for (TeamWorkFlowArtifact team : ActionManager.getTeams(element)) {
               String str = getColumnText(team, column, columnIndex);
               if (Strings.isValid(str)) {
                  strs.add(str);
               }
            }
            return Collections.toString(";", strs);

         } else if (element instanceof AbstractWorkflowArtifact) {
            return getDateStr(getAttributeType(), (AbstractWorkflowArtifact) element);
         }
      } catch (Exception ex) {
         return LogUtil.getCellExceptionString(ex);
      }
      return super.getColumnText(element, column, columnIndex);
   }

   public static Date getDateFromWorkflow(IAttributeType attributeType, Object object) throws OseeCoreException {
      if (Artifacts.isOfType(object, AtsArtifactTypes.TeamWorkflow)) {
         return ((TeamWorkFlowArtifact) object).getSoleAttributeValue(attributeType, null);
      } else if (object instanceof AbstractWorkflowArtifact) {
         TeamWorkFlowArtifact teamArt = ((AbstractWorkflowArtifact) object).getParentTeamWorkflow();
         if (teamArt != null) {
            return getDateFromWorkflow(attributeType, teamArt);
         }
      }
      return null;
   }

   public static Date getDateFromTargetedVersion(IAttributeType attributeType, Object object) throws OseeCoreException {
      if (Artifacts.isOfType(object, AtsArtifactTypes.TeamWorkflow)) {
         TeamWorkFlowArtifact teamArt = (TeamWorkFlowArtifact) object;
         IAtsVersion verArt = AtsClientService.get().getVersionService().getTargetedVersion(teamArt);
         if (verArt != null) {
            if (attributeType == AtsAttributeTypes.ReleaseDate) {
               return verArt.getReleaseDate();
            } else if (attributeType == AtsAttributeTypes.EstimatedReleaseDate) {
               return verArt.getEstimatedReleaseDate();
            }
         }
      } else if (object instanceof AbstractWorkflowArtifact) {
         TeamWorkFlowArtifact teamArt = ((AbstractWorkflowArtifact) object).getParentTeamWorkflow();
         if (teamArt != null) {
            return getDateFromTargetedVersion(attributeType, teamArt);
         }
      }
      return null;
   }

   public static String getDateStrFromWorkflow(IAttributeType attributeType, AbstractWorkflowArtifact artifact) throws OseeCoreException {
      return DateUtil.getMMDDYY(getDateFromWorkflow(attributeType, artifact));
   }

   public static String getDateStrFromTargetedVersion(IAttributeType attributeType, AbstractWorkflowArtifact artifact) throws OseeCoreException {
      return DateUtil.getMMDDYY(getDateFromTargetedVersion(attributeType, artifact));
   }

   public static String getDateStr(IAttributeType attributeType, AbstractWorkflowArtifact artifact) throws OseeCoreException {
      String workflowDate = getDateStrFromWorkflow(attributeType, artifact);
      String versionDate = getDateStrFromTargetedVersion(attributeType, artifact);
      if (Strings.isValid(workflowDate) && Strings.isValid(versionDate)) {
         return String.format("%s; [%s - %s]", workflowDate,
            AtsClientService.get().getVersionService().getTargetedVersion(artifact), versionDate);
      } else if (Strings.isValid(workflowDate)) {
         return workflowDate;
      } else if (Strings.isValid(versionDate)) {
         return versionDate;
      }
      return "";
   }
}
