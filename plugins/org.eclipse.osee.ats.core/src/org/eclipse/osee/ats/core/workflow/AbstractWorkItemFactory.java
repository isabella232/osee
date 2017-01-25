/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.workflow;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.agile.IAgileBacklog;
import org.eclipse.osee.ats.api.agile.IAgileItem;
import org.eclipse.osee.ats.api.agile.IAgileSprint;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.team.IAtsWorkItemFactory;
import org.eclipse.osee.ats.api.workflow.WorkItemType;
import org.eclipse.osee.ats.core.agile.AgileBacklog;
import org.eclipse.osee.ats.core.agile.AgileSprint;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public abstract class AbstractWorkItemFactory implements IAtsWorkItemFactory {

   protected final IAtsServices services;

   public AbstractWorkItemFactory(IAtsServices services) {
      this.services = services;
   }

   @Override
   public Collection<IAtsWorkItem> getWorkItems(Collection<? extends ArtifactId> artifacts) {
      List<IAtsWorkItem> workItems = new LinkedList<>();
      for (ArtifactId artifact : artifacts) {
         IAtsWorkItem workItem = getWorkItem(artifact);
         if (workItem != null) {
            workItems.add(workItem);
         }
      }
      return workItems;
   }

   @Override
   public IAtsWorkItem getWorkItem(ArtifactId artifact) {
      IAtsWorkItem workItem = null;
      try {
         if (services.getStoreService().isOfType(artifact, AtsArtifactTypes.TeamWorkflow)) {
            workItem = getTeamWf(artifact);
         } else if (services.getStoreService().isOfType(artifact,
            AtsArtifactTypes.PeerToPeerReview) || services.getStoreService().isOfType(artifact,
               AtsArtifactTypes.DecisionReview)) {
            workItem = getReview(artifact);
         } else if (services.getStoreService().isOfType(artifact, AtsArtifactTypes.Task)) {
            workItem = getTask(artifact);
         } else if (services.getStoreService().isOfType(artifact, AtsArtifactTypes.Goal)) {
            workItem = getGoal(artifact);
         } else if (services.getStoreService().isOfType(artifact, AtsArtifactTypes.AgileSprint)) {
            workItem = getAgileSprint((ArtifactToken) artifact);
         }
      } catch (OseeCoreException ex) {
         services.getLogger().error(ex, "Error getting work item for [%s]", artifact);
      }
      return workItem;
   }

   @Override
   public IAgileSprint getAgileSprint(ArtifactToken artifact) throws OseeCoreException {
      IAgileSprint sprint = null;
      if (artifact instanceof IAgileSprint) {
         sprint = (IAgileSprint) artifact;
      } else {
         sprint = new AgileSprint(services.getLogger(), services, artifact);
      }
      return sprint;
   }

   @Override
   public IAgileBacklog getAgileBacklog(ArtifactToken artifact) throws OseeCoreException {
      IAgileBacklog backlog = null;
      if (artifact instanceof IAgileBacklog) {
         backlog = (IAgileBacklog) artifact;
      } else {
         backlog = new AgileBacklog(services.getLogger(), services, artifact);
      }
      return backlog;
   }

   @Override
   public IAgileItem getAgileItem(ArtifactId artifact) {
      IAgileItem item = null;
      ArtifactId art = services.getArtifact(artifact);
      if (services.getStoreService().isOfType(art, AtsArtifactTypes.AbstractWorkflowArtifact)) {
         item = new org.eclipse.osee.ats.core.agile.AgileItem(services.getLogger(), services, (ArtifactToken) art);
      }
      return item;
   }

   @Override
   public IAtsWorkItem getWorkItemByAtsId(String atsId) {
      return services.getQueryService().createQuery(WorkItemType.WorkItem).andAttr(AtsAttributeTypes.AtsId,
         atsId).getResults().getOneOrNull();
   }

}
