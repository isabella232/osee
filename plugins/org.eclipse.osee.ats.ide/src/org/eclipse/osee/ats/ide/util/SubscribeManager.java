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
package org.eclipse.osee.ats.ide.util;

import java.util.Arrays;
import java.util.Collection;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.user.AtsUser;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.ide.internal.AtsClientService;
import org.eclipse.osee.ats.ide.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public class SubscribeManager {

   public static void addSubscribed(AbstractWorkflowArtifact workflow, AtsUser user, IAtsChangeSet changes) {
      if (!workflow.getRelatedArtifactsUnSorted(AtsRelationTypes.SubscribedUser_User).contains(user.getStoreObject())) {
         changes.relate(workflow, AtsRelationTypes.SubscribedUser_User, user);
      }
   }

   public static void removeSubscribed(AbstractWorkflowArtifact workflow, AtsUser user, IAtsChangeSet changes) {
      changes.unrelate((IAtsObject) workflow, AtsRelationTypes.SubscribedUser_User, (IAtsObject) user);
   }

   public static boolean isSubscribed(AbstractWorkflowArtifact workflow, AtsUser user) {
      return workflow.getRelatedArtifactsUnSorted(AtsRelationTypes.SubscribedUser_User).contains(user.getStoreObject());
   }

   public static boolean amISubscribed(AbstractWorkflowArtifact workflow) {
      try {
         return isSubscribed(workflow, AtsClientService.get().getUserService().getCurrentUser());
      } catch (OseeCoreException ex) {
         return false;
      }
   }

   public static void toggleSubscribe(AbstractWorkflowArtifact awa) {
      toggleSubscribe(Arrays.asList(awa));
   }

   public static void toggleSubscribe(Collection<AbstractWorkflowArtifact> awas) {
      if (SubscribeManager.amISubscribed(awas.iterator().next())) {
         IAtsChangeSet changes = AtsClientService.get().createChangeSet("Toggle Subscribed");
         for (AbstractWorkflowArtifact awa : awas) {
            SubscribeManager.removeSubscribed(awa, AtsClientService.get().getUserService().getCurrentUser(), changes);
         }
         changes.execute();
      } else {
         IAtsChangeSet changes = AtsClientService.get().createChangeSet("Toggle Subscribed");
         for (AbstractWorkflowArtifact awa : awas) {
            SubscribeManager.addSubscribed(awa, AtsClientService.get().getUserService().getCurrentUser(), changes);
         }
         changes.execute();
      }
   }

}
