/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.framework.core.event;

import java.util.logging.Level;
import org.eclipse.osee.framework.core.exception.OseeWrappedException;
import org.eclipse.osee.framework.core.util.JsonUtil;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLog;
import org.osgi.service.event.Event;

/**
 * @author Roberto E. Escobar
 */
public final class EventUtil {
   private EventUtil() {
      // Utility Class
   }

   public static String getObjectSafeName(Object object) {
      try {
         return object.toString();
      } catch (Exception ex) {
         return object.getClass().getSimpleName() + " - exception on toString: " + ex.getLocalizedMessage();
      }
   }

   public static void eventLog(String message, Throwable ex) {
      eventLog(ex, message);
   }

   public static void eventLog(Throwable ex, String message, Object... args) {
      try {
         if (isEventDebugConsole()) {
            StringBuilder builder = new StringBuilder();
            builder.append(formatMessage(message, args));
            if (ex != null) {
               builder.append(" <<ERROR>> ");
               builder.append(ex.toString());
            }
         }
         if (ex != null) {
            OseeLog.log(EventUtil.class, Level.SEVERE, formatMessage(message, args), ex);
         } else {
            OseeLog.log(EventUtil.class, Level.FINE, formatMessage(message, args));
         }
      } catch (Throwable th) {
         OseeLog.log(EventUtil.class, Level.SEVERE, th);
      }
   }

   public static void eventLog(String output, Object... args) {
      eventLog(null, output, args);
   }

   private static String formatMessage(String message, Object... args) {
      try {
         return String.format(message, args);
      } catch (RuntimeException ex) {
         return String.format(
            "Exception message could not be formatted: [%s] with the following arguments [%s].  Cause [%s]", message,
            Collections.toString(",", args), ex.toString());
      }
   }

   private static boolean isEventDebugConsole() {
      String debugConsole = System.getProperty("eventDebug", "");
      return "console".equals(debugConsole);
   }

   /**
    * Topic Events can have "json" property set. Desearialize as given class
    */
   public static <T> T getTopicJson(Event event, Class<T> class1) {
      return JsonUtil.readValue((String) event.getProperty("json"), class1);
   }

   /**
    * Create topic event given topic and oject to searialize to json property
    */
   public static TopicEvent createTopic(AbstractTopicEvent topic, Object event) {
      return createTopic(topic.getTopic(), event, topic.getEventType());
   }

   /**
    * Create topic event given topic and oject to searialize to json property
    */
   public static TopicEvent createTopic(String topic, Object event, EventType eventType) {
      try {
         return new TopicEvent(topic, "json", JsonUtil.toJson(event), eventType);
      } catch (Exception ex) {
         throw new OseeWrappedException(ex, "Error reading topic json [%s]", event.toString());
      }
   }
}