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
package org.eclipse.osee.ats.api.team;

import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public interface ITeamWorkflowProvider {

   /**
    * Notification that a teamWorkflow is being duplicated. This allows the extension to do necessary changes to
    * duplicated workflow.
    */
   public void teamWorkflowDuplicating(IAtsTeamWorkflow teamWf, IAtsTeamWorkflow dupTeamWf) throws OseeCoreException;

   public String getWorkflowDefinitionId(IAtsWorkItem workItem) throws OseeCoreException;

   public String getRelatedTaskWorkflowDefinitionId(IAtsTeamWorkflow teamWf) throws OseeCoreException;

   /**
    * Assigned or computed Id that will show at the top of the editor
    */
   public String getPcrId(IAtsTeamWorkflow teamWf) throws OseeCoreException;

   /**
    * 5-9 character short name for UI and display purposes
    */
   public String getArtifactTypeShortName(IAtsTeamWorkflow teamWf);

   public String getBranchName(IAtsTeamWorkflow teamWf, String defaultBranchName);

   public boolean isResponsibleFor(IAtsWorkItem workItem);
}
