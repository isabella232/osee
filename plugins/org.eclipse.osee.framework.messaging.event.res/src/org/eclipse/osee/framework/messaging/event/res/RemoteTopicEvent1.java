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
package org.eclipse.osee.framework.messaging.event.res;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.osee.framework.messaging.event.res.msgs.RemoteNetworkSender1;

/**
 * @author Donald G. Dunne
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RemoteTopicEvent1", propOrder = {"topic", "properties", "networkSender"})
public class RemoteTopicEvent1 extends RemoteEvent {

   @XmlElement(required = true)
   protected String topic;
   protected Map<String, String> properties = new HashMap<>();
   @XmlElement(required = true)
   protected RemoteNetworkSender1 networkSender;

   @Override
   public RemoteNetworkSender1 getNetworkSender() {
      return networkSender;
   }

   public void setNetworkSender(RemoteNetworkSender1 value) {
      this.networkSender = value;
   }

   public String getTopic() {
      return topic;
   }

   public void setTopic(String topic) {
      this.topic = topic;
   }

   public Map<String, String> getProperties() {
      return properties;
   }

   public void setProperties(Map<String, String> properties) {
      this.properties = properties;
   }

}
