/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.client.internal;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.data.AtsArtifactToken;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.core.client.IAtsClient;
import org.eclipse.osee.ats.core.client.config.AtsBulkLoad;
import org.eclipse.osee.ats.core.client.review.AbstractReviewArtifact;
import org.eclipse.osee.ats.core.client.review.AtsReviewCache;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.AtsTaskCache;
import org.eclipse.osee.ats.core.client.util.AtsUtilClient;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.event.listener.IArtifactEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.ArtifactEvent;
import org.eclipse.osee.framework.skynet.core.event.model.EventBasicGuidArtifact;
import org.eclipse.osee.framework.skynet.core.event.model.EventBasicGuidRelation;
import org.eclipse.osee.framework.skynet.core.event.model.EventModType;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;
import org.eclipse.osee.framework.skynet.core.relation.RelationTypeManager;
import org.eclipse.osee.framework.skynet.core.utility.DbUtil;

/**
 * Updates ATS Cache based on Artifact Events. Registers for service via ats.cache.update.listener osgi registration.
 *
 * @author Donald G. Dunne
 */
public class AtsCacheManagerUpdateListener implements IArtifactEventListener {

   private static List<Long> configReloadRelationTypeGuids =
      Arrays.asList(AtsRelationTypes.ActionableItemLead_Lead.getGuid(),
         AtsRelationTypes.TeamDefinitionToVersion_Version.getGuid(), AtsRelationTypes.TeamActionableItem_Team.getGuid(),
         AtsRelationTypes.PrivilegedMember_Team.getGuid(), AtsRelationTypes.TeamLead_Team.getGuid(),
         AtsRelationTypes.ParallelVersion_Child.getGuid(), AtsRelationTypes.ParallelVersion_Parent.getGuid());
   private static List<Long> configReloadArtifactTypeGuids = Arrays.asList(AtsArtifactTypes.Version.getGuid(),
      AtsArtifactTypes.TeamDefinition.getGuid(), AtsArtifactTypes.ActionableItem.getGuid());

   private IAtsClient atsClient;

   public void setAtsClient(IAtsClient atsClient) {
      this.atsClient = atsClient;
   }

   @Override
   public List<? extends IEventFilter> getEventFilters() {
      return Arrays.asList(AtsUtilClient.getAtsBranchFilter());
   }

   @Override
   public void handleArtifactEvent(ArtifactEvent artifactEvent, Sender sender) {
      if (!DbUtil.isDbInit()) {
         boolean handledConfigReload = processArtifacts(artifactEvent);
         processRelations(artifactEvent, handledConfigReload);
      }
   }

   private void processRelations(ArtifactEvent artifactEvent, boolean handledConfigReload) {
      if (!handledConfigReload) {
         // update cache
         for (EventBasicGuidRelation guidRel : artifactEvent.getRelations()) {
            try {
               RelationType typeByGuid = RelationTypeManager.getTypeByGuid(guidRel.getRelTypeGuid());
               if (configReloadRelationTypeGuids.contains(typeByGuid.getGuid())) {
                  AtsBulkLoad.reloadConfig(false);
                  break;
               }
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }

         }
      }

      for (EventBasicGuidRelation guidRel : artifactEvent.getRelations()) {
         try {
            if (guidRel.is(AtsRelationTypes.TeamWorkflowTargetedForVersion_Workflow)) {
               for (TeamWorkFlowArtifact teamWf : ArtifactCache.getActive(guidRel, TeamWorkFlowArtifact.class)) {
                  // Just remove teamWf from cache; reload will occur upon next call to get version
                  atsClient.getVersionService().invalidateVersionCache(teamWf);
               }
            }
            if (guidRel.is(AtsRelationTypes.TeamWfToTask_Task)) {
               for (TaskArtifact taskArt : ArtifactCache.getActive(guidRel, TaskArtifact.class)) {
                  if (!taskArt.isDeleted()) {
                     AtsTaskCache.decache(taskArt.getParentAWA());
                  }
               }
               for (Artifact artifact : ArtifactCache.getActive(guidRel)) {
                  if (artifact instanceof TeamWorkFlowArtifact) {
                     AtsTaskCache.decache(artifact);
                  }
               }
            }
            if (guidRel.is(AtsRelationTypes.TeamWorkflowToReview_Review)) {
               for (AbstractReviewArtifact review : ArtifactCache.getActive(guidRel, AbstractReviewArtifact.class)) {
                  if (!review.isDeleted()) {
                     AtsReviewCache.decache(review.getParentAWA());
                  }
               }
               for (Artifact artifact : ArtifactCache.getActive(guidRel)) {
                  if (artifact instanceof AbstractReviewArtifact) {
                     AtsReviewCache.decache(artifact);
                  }
               }
            }

         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
   }

   private boolean processArtifacts(ArtifactEvent artifactEvent) {
      boolean reload = false;
      for (EventBasicGuidArtifact guidArt : artifactEvent.getArtifacts()) {
         if (configReloadArtifactTypeGuids.contains(guidArt.getArtTypeGuid())) {
            reload = true;
            break;
         }
      }

      if (reload) {
         try {
            AtsBulkLoad.reloadConfig(false);
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }

      for (EventBasicGuidArtifact guidArt : artifactEvent.getArtifacts()) {
         try {
            if (guidArt.is(EventModType.Deleted, EventModType.Purged)) {
               handleCachesForDeletedPurged(guidArt);
            }
            if (guidArt.is(EventModType.Added, EventModType.Modified)) {
               handleCachesForAddedModified(guidArt);
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
      return reload;
   }

   private void handleCachesForAddedModified(EventBasicGuidArtifact guidArt) throws OseeCoreException {
      // Only process if in cache
      Artifact artifact = ArtifactCache.getActive(guidArt);
      if (artifact != null && guidArt.is(EventModType.Added)) {
         if (artifact.isOfType(AtsArtifactTypes.Task) && !artifact.isDeleted()) {
            AtsTaskCache.decache(((TaskArtifact) artifact).getParentAWA());
         }
         if (artifact instanceof TeamWorkFlowArtifact) {
            AtsTaskCache.decache(artifact);
         }
         if (artifact.isOfType(AtsArtifactTypes.ReviewArtifact) && !artifact.isDeleted()) {
            AtsReviewCache.decache(((AbstractReviewArtifact) artifact).getParentAWA());
         }
         if (artifact instanceof AbstractReviewArtifact) {
            AtsReviewCache.decache(artifact);
         }
      }
      if (artifact instanceof AbstractWorkflowArtifact) {
         AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) artifact;
         awa.clearCaches();
      }
      if (artifact != null && artifact.equals(AtsArtifactToken.RuleDefinitions)) {
         AtsClientService.get().getWorkDefinitionAdmin().clearRuleDefinitionsCache();
      }
   }

   private void handleCachesForDeletedPurged(EventBasicGuidArtifact guidArt) throws OseeCoreException {
      if (guidArt.is(AtsArtifactTypes.Task) && guidArt.is(EventModType.Deleted)) {
         Artifact artifact = ArtifactCache.getActive(guidArt);
         if (artifact != null && !artifact.isDeleted()) {
            AtsTaskCache.decache(((TaskArtifact) artifact).getParentAWA());
         }
      }
      if (guidArt.is(AtsArtifactTypes.ReviewArtifact) && guidArt.is(EventModType.Deleted)) {
         Artifact artifact = ArtifactCache.getActive(guidArt);
         if (artifact != null && !artifact.isDeleted()) {
            AtsReviewCache.decache(((AbstractReviewArtifact) artifact).getParentAWA());
         }
      }
      Artifact artifact = ArtifactCache.getActive(guidArt);
      if (artifact instanceof TeamWorkFlowArtifact) {
         AtsTaskCache.decache(artifact);
      }
      if (artifact instanceof AbstractReviewArtifact) {
         AtsReviewCache.decache(artifact);
      }
   }

}
