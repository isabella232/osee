/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.notify;

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.IAtsUser;

/**
 * @author Donald G. Dunne
 */
public class AtsNotificationEventFactory {

   public static AtsNotificationEvent getNotificationEvent(IAtsUser fromUser, Collection<IAtsUser> users, String id, String type, String description) {
      AtsNotificationEvent event = new AtsNotificationEvent();
      event.setType(type);
      event.setId(id);
      event.setDescription(description);
      event.setFromUserId(fromUser.getUserId());
      for (IAtsUser user : users) {
         event.getUserIds().add(user.getUserId());
      }
      return event;
   }

   public static AtsNotificationEvent getNotificationEvent(IAtsUser fromUser, Collection<IAtsUser> users, String id, String type, String url, String description) {
      AtsNotificationEvent event = getNotificationEvent(fromUser, users, id, type, description);
      event.setUrl(url);
      return event;
   }

   public static AtsNotificationEvent getNotificationEventFactory(IAtsUser fromUser, Collection<IAtsUser> users, String id, String type, String description, String url) {
      AtsNotificationEvent event = getNotificationEvent(fromUser, users, id, type, description);
      event.setUrl(url);
      return event;
   }

   public static AtsNotificationEvent getNotificationEventByUserIds(IAtsUser fromUser, Collection<String> userIds, String id, String type, String description) {
      AtsNotificationEvent event = new AtsNotificationEvent();
      event.setType(type);
      event.setId(id);
      event.setDescription(description);
      event.getUserIds().addAll(userIds);
      event.setFromUserId(fromUser.getUserId());
      return event;
   }

   public static AtsNotificationEvent getNotificationEventByUserIds(IAtsUser fromUser, Collection<String> userIds, String id, String type, String description, String url) {
      AtsNotificationEvent event = getNotificationEventByUserIds(fromUser, userIds, id, type, description);
      event.setUrl(url);
      return event;
   }

   public static AtsWorkItemNotificationEvent getWorkItemNotificationEvent(IAtsUser fromUser, IAtsWorkItem workItem, List<IAtsUser> users, AtsNotifyType... notifyType) {
      AtsWorkItemNotificationEvent event = getWorkItemNotificationEvent(fromUser, workItem, notifyType);
      for (IAtsUser user : users) {
         event.getUserIds().add(user.getUserId());
      }
      return event;
   }

   public static AtsWorkItemNotificationEvent getWorkItemNotificationEvent(IAtsUser fromUser, IAtsWorkItem workItem, AtsNotifyType... notifyType) {
      AtsWorkItemNotificationEvent event = new AtsWorkItemNotificationEvent();
      event.setFromUserId(fromUser.getUserId());
      event.getAtsIds().add(workItem.getAtsId());
      event.setNotifyType(notifyType);
      return event;
   }
}
