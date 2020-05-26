/*********************************************************************
 * Copyright (c) 2013 Boeing
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

package org.eclipse.osee.ats.ide.util;

import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.workflow.IAtsAction;
import org.eclipse.osee.ats.core.workflow.util.ChangeTypeUtil;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.ats.ide.internal.AtsClientService;
import org.eclipse.osee.ats.ide.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.ide.workflow.task.TaskArtifact;
import org.eclipse.osee.ats.ide.workflow.teamwf.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public class CopyActionDetails {

   private final AbstractWorkflowArtifact awa;
   private static final String USE_DEVELOPER_CHANGE_TYPES = "UseDeveloperChangeTypes";

   public CopyActionDetails(AbstractWorkflowArtifact awa) {
      this.awa = awa;
   }

   public String getDetailsString() {
      String detailsStr = "";
      try {
         if (awa.getParentTeamWorkflow() != null) {
            IAtsTeamDefinition teamDef = awa.getParentTeamWorkflow().getTeamDefinition();
            String formatStr = getFormatStr(teamDef);
            if (Strings.isValid(formatStr)) {
               detailsStr = formatStr;
               IAtsAction action = awa.getParentAction();
               if (action != null) {
                  detailsStr = detailsStr.replaceAll("<actionatsid>", action.getAtsId());
               }
               String legacyPcrId = awa.getSoleAttributeValue(AtsAttributeTypes.LegacyPcrId, null);
               if (Strings.isValid(legacyPcrId)) {
                  detailsStr = detailsStr.replaceAll("<legacypcrid>", " - [" + legacyPcrId + "]");
               } else {
                  detailsStr = detailsStr.replaceAll("<legacypcrid>", "");
               }
               detailsStr = detailsStr.replaceAll("<atsid>", awa.getAtsId());
               detailsStr = detailsStr.replaceAll("<name>", awa.getName());
               detailsStr = detailsStr.replaceAll("<artType>", awa.getArtifactTypeName());
               detailsStr = detailsStr.replaceAll("<changeType>", getChangeTypeOrObjectType(awa));
            }
         }
         if (!Strings.isValid(detailsStr)) {
            detailsStr = "\"" + awa.getArtifactTypeName() + "\" - " + awa.getAtsId() + " - \"" + awa.getName() + "\"";
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return detailsStr;
   }

   private String getChangeTypeOrObjectType(AbstractWorkflowArtifact awa) {
      String result = "";
      if (awa instanceof TeamWorkFlowArtifact) {
         TeamWorkFlowArtifact teamArt = (TeamWorkFlowArtifact) awa;
         result = ChangeTypeUtil.getChangeTypeStr(awa, AtsClientService.get());
         if (teamArt.getTeamDefinition().hasTag(USE_DEVELOPER_CHANGE_TYPES)) {
            if (result.equals("Improvement")) {
               result = "feature";
            } else if (result.equals("Problem")) {
               result = "bug";
            } else if (result.equals("Refinement")) {
               result = "refinement";
            }
         }
      } else if (awa instanceof TaskArtifact) {
         result = "Task";
      } else if (awa.isOfType(AtsArtifactTypes.AbstractReview)) {
         result = "Review";
      } else if (awa.isTypeEqual(AtsArtifactTypes.Goal)) {
         result = "Goal";
      } else if (awa.isTypeEqual(AtsArtifactTypes.AgileBacklog)) {
         result = "Backlog";
      }
      if (!Strings.isValid(result)) {
         result = "unknown";
      }
      return result;
   }

   private String getFormatStr(IAtsTeamDefinition teamDef) {
      if (teamDef != null) {
         Artifact artifact = AtsClientService.get().getQueryServiceClient().getArtifact(teamDef);
         if (artifact != null) {
            String formatStr = artifact.getSoleAttributeValue(AtsAttributeTypes.ActionDetailsFormat, "");
            if (Strings.isValid(formatStr)) {
               return formatStr;
            }
         }
         if (AtsClientService.get().getTeamDefinitionService().getParentTeamDef(teamDef) != null) {
            return getFormatStr(AtsClientService.get().getTeamDefinitionService().getParentTeamDef(teamDef));
         }
      }
      return null;
   }
}
