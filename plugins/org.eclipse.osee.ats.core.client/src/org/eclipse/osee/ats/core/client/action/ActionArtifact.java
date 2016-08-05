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
package org.eclipse.osee.ats.core.client.action;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.workflow.IAtsAction;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.core.client.internal.Activator;
import org.eclipse.osee.ats.core.client.internal.AtsClientService;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.UuidIdentity;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public class ActionArtifact extends Artifact implements IAtsAction {

   public ActionArtifact(String guid, BranchId branch, IArtifactType artifactType) throws OseeCoreException {
      super(guid, branch, artifactType);
   }

   public Set<IAtsActionableItem> getActionableItems() throws OseeCoreException {
      Set<IAtsActionableItem> aias = new HashSet<>();
      for (TeamWorkFlowArtifact team : getTeams()) {
         aias.addAll(AtsClientService.get().getWorkItemService().getActionableItemService().getActionableItems(team));
      }
      return aias;
   }

   public Collection<TeamWorkFlowArtifact> getTeams() throws OseeCoreException {
      return getRelatedArtifactsUnSorted(AtsRelationTypes.ActionToWorkflow_WorkFlow, TeamWorkFlowArtifact.class);
   }

   public TeamWorkFlowArtifact getFirstTeam() throws OseeCoreException {
      if (getRelatedArtifactsCount(AtsRelationTypes.ActionToWorkflow_WorkFlow) > 0) {
         return getTeams().iterator().next();
      }
      return null;
   }

   @Override
   public Collection<IAtsTeamWorkflow> getTeamWorkflows() throws OseeCoreException {
      return Collections.castAll(getTeams());
   }

   @Override
   public String getAtsId() {
      String toReturn = getGuid();
      try {
         toReturn = getSoleAttributeValueAsString(AtsAttributeTypes.AtsId, toReturn);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.WARNING, ex);
      }
      return toReturn;
   }

   @Override
   public void setAtsId(String atsId) throws OseeCoreException {
      setSoleAttributeFromString(AtsAttributeTypes.AtsId, atsId);
   }

   @Override
   public ArtifactId getStoreObject() {
      return this;
   }

   @Override
   public void setStoreObject(ArtifactId artifact) {
      // do nothing
   }

   @Override
   public Long getUuid() {
      return Long.valueOf(getArtId());
   }

   @Override
   public boolean matches(UuidIdentity... identities) {
      for (UuidIdentity identity : identities) {
         if (equals(identity)) {
            return true;
         }
      }
      return false;
   }

}
