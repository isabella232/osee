/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.skynet.core.event.model;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.framework.core.event.AbstractTopicEvent;
import org.eclipse.osee.framework.core.event.EventType;

/**
 * @author Donald G. Dunne
 */
public class TopicEvent implements FrameworkEvent, HasNetworkSender {

   EventType eventType;
   private String topic;
   private final Map<String, String> properties;
   private NetworkSender networkSender;

   public TopicEvent(String topic) {
      this.topic = topic;
      properties = new HashMap<>();
   }

   public TopicEvent(AbstractTopicEvent topic, String key, String value) {
      this(topic.getTopic(), key, value, topic.getEventType());
   }

   public TopicEvent(String topic, String key, String value, EventType eventType) {
      this(topic);
      this.eventType = eventType;
      properties.put(key, value);
   }

   public String getTopic() {
      return topic;
   }

   public void setTopic(String topic) {
      this.topic = topic;
   }

   public void put(String key, String value) {
      properties.put(key, value);
   }

   @Override
   public NetworkSender getNetworkSender() {
      return networkSender;
   }

   @Override
   public void setNetworkSender(NetworkSender networkSender) {
      this.networkSender = networkSender;
   }

   public Map<String, String> getProperties() {
      return properties;
   }

   public EventType getEventType() {
      return eventType;
   }

   public void setEventType(EventType eventType) {
      this.eventType = eventType;
   }

}
