/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.skynet.core.event.model;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.model.event.DefaultBasicGuidArtifact;

public class AccessControlEvent implements FrameworkEvent, HasNetworkSender, HasEventType<AccessControlEventType> {

   private AccessControlEventType eventType;
   private List<DefaultBasicGuidArtifact> artifacts;
   private NetworkSender networkSender;

   /**
    * Gets the value of the artifacts property.
    * <p>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the artifacts property.
    * <p>
    * For example, to add a new item, do as follows:
    *
    * <pre>
    * getArtifacts().add(newItem);
    * </pre>
    * <p>
    * Objects of the following type(s) are allowed in the list {@link DefaultBasicGuidArtifact }
    */
   public List<DefaultBasicGuidArtifact> getArtifacts() {
      if (artifacts == null) {
         artifacts = new ArrayList<>();
      }
      return this.artifacts;
   }

   /**
    * Gets the value of the networkSender property.
    *
    * @return possible object is {@link NetworkSender }
    */
   @Override
   public NetworkSender getNetworkSender() {
      return networkSender;
   }

   /**
    * Sets the value of the networkSender property.
    *
    * @param value allowed object is {@link NetworkSender }
    */
   @Override
   public void setNetworkSender(NetworkSender value) {
      this.networkSender = value;
   }

   @Override
   public AccessControlEventType getEventType() {
      return eventType;
   }

   public void setEventType(AccessControlEventType eventType) {
      this.eventType = eventType;
   }

   public boolean isForBranch(IOseeBranch branch) {
      for (DefaultBasicGuidArtifact guidArt : getArtifacts()) {
         if (guidArt.isOnBranch(branch)) {
            return true;
         }
      }
      return false;
   }

   @Override
   public String toString() {
      return "AccessControlEvent [type=" + eventType + ", artifacts=" + artifacts + ", sender=" + networkSender + "]";
   }

}
