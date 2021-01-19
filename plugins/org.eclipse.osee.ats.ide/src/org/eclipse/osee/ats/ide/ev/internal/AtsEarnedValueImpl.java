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

package org.eclipse.osee.ats.ide.ev.internal;

import java.util.Arrays;
import java.util.Collection;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.ev.AtsWorkPackageEndpointApi;
import org.eclipse.osee.ats.api.ev.IAtsWorkPackage;
import org.eclipse.osee.ats.api.ev.JaxWorkPackageData;
import org.eclipse.osee.ats.api.util.AtsTopicEvent;
import org.eclipse.osee.ats.api.workdef.IStateToken;
import org.eclipse.osee.ats.core.util.AtsAbstractEarnedValueImpl;
import org.eclipse.osee.ats.core.util.AtsObjects;
import org.eclipse.osee.ats.ide.internal.AtsApiService;
import org.eclipse.osee.ats.ide.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.event.TopicEvent;
import org.eclipse.osee.framework.jdk.core.result.XResultData;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.logger.Log;

/**
 * @author Donald G. Dunne
 */
public class AtsEarnedValueImpl extends AtsAbstractEarnedValueImpl {

   public AtsEarnedValueImpl(Log logger, AtsApi atsApi) {
      super(logger, atsApi);
   }

   @Override
   public ArtifactId getWorkPackageId(IAtsWorkItem workItem) {
      ArtifactId id = ArtifactId.SENTINEL;
      Artifact artifact = AtsApiService.get().getQueryServiceIde().getArtifact(workItem);
      Conditions.checkNotNull(artifact, "workItem", "Can't Find Work Package matching %s", workItem.toStringWithId());
      if (artifact instanceof AbstractWorkflowArtifact) {
         AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) artifact;
         id = awa.getSoleAttributeValue(AtsAttributeTypes.WorkPackageReference, id);
      }
      return id;
   }

   @Override
   public void setWorkPackage(IAtsWorkPackage workPackage, Collection<IAtsWorkItem> workItems) {
      changeWorkPackage(workPackage, workItems, false);
   }

   @Override
   public void removeWorkPackage(IAtsWorkPackage workPackage, Collection<IAtsWorkItem> workItems) {
      changeWorkPackage(workPackage, workItems, true);
   }

   private void changeWorkPackage(IAtsWorkPackage workPackage, Collection<IAtsWorkItem> workItems, boolean remove) {
      JaxWorkPackageData data = new JaxWorkPackageData();
      data.setAsUserId(atsApi.getUserService().getCurrentUserId());
      for (IAtsWorkItem workItem : workItems) {
         data.getWorkItemIds().add(workItem.getId());
      }

      AtsWorkPackageEndpointApi workPackageEp = AtsApiService.get().getServerEndpoints().getWorkPackageEndpoint();
      if (remove) {
         XResultData rd = workPackageEp.deleteWorkPackageItems(workPackage == null ? 0L : workPackage.getId(), data);
         if (rd.isErrors()) {
            throw new OseeCoreException(rd.toString());
         }
      } else {
         XResultData rd = workPackageEp.setWorkPackage(workPackage.getId(), data);
         if (rd.isErrors()) {
            throw new OseeCoreException(rd.toString());
         }
      }

      TopicEvent event = new TopicEvent(AtsTopicEvent.WORK_ITEM_MODIFIED, AtsTopicEvent.WORK_ITEM_IDS_KEY,
         AtsObjects.toIdsString(";", workItems));
      event.addProperty(AtsTopicEvent.WORK_ITEM_ATTR_TYPE_IDS_KEY,
         Collections.toString(";", Arrays.asList(AtsAttributeTypes.WorkPackage.getIdString(),
            AtsAttributeTypes.WorkPackageReference.getIdString())));
      OseeEventManager.kickTopicEvent(getClass(), event);

   }

   @Override
   public double getEstimatedHoursFromTasks(IAtsWorkItem workItem, IStateToken relatedToState) {
      return 0;
   }

   @Override
   public double getEstimatedHoursTotal(IAtsWorkItem workItem, IStateToken relatedToState) {
      return 0;
   }

}
