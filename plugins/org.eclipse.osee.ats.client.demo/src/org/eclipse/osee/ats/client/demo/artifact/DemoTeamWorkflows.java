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
package org.eclipse.osee.ats.client.demo.artifact;

import java.util.logging.Level;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.team.TeamWorkflowProviderAdapter;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.client.demo.DemoArtifactTypes;
import org.eclipse.osee.ats.client.demo.internal.Activator;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Donald G. Dunne
 */
public class DemoTeamWorkflows extends TeamWorkflowProviderAdapter {

   @Override
   public boolean isResponsibleFor(IAtsWorkItem workItem) {
      try {
         TeamWorkFlowArtifact teamWf = (TeamWorkFlowArtifact) workItem.getParentTeamWorkflow();
         if (teamWf != null) {
            return (teamWf.isOfType(DemoArtifactTypes.DemoCodeTeamWorkflow) || teamWf.isOfType(DemoArtifactTypes.DemoReqTeamWorkflow) || teamWf.isOfType(DemoArtifactTypes.DemoTestTeamWorkflow));
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return false;
   }

   @Override
   public String getBranchName(IAtsTeamWorkflow teamWf, String defaultBranchName) {
      try {
         if (teamWf.getTeamDefinition().getName().contains("SAW Test")) {
            return "SAW Test - " + defaultBranchName;
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return null;
   }

}
