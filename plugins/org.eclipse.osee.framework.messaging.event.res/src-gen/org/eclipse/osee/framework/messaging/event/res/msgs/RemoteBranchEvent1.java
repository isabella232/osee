//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2012.09.14 at 05:06:42 PM MST
//

package org.eclipse.osee.framework.messaging.event.res.msgs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.messaging.event.res.RemoteEvent;

/**
 * <p>
 * Java class for RemoteBranchEvent1 complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="RemoteBranchEvent1">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="eventTypeGuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="branchGuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="destinationBranchGuid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="networkSender" type="{}RemoteNetworkSender1"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RemoteBranchEvent1", propOrder = {
   "eventTypeGuid",
   "branchGuid",
   "destinationBranchGuid",
   "networkSender"})
public class RemoteBranchEvent1 extends RemoteEvent {

   @XmlElement(required = true)
   protected String eventTypeGuid;
   @XmlElement(required = true)
   protected String branchGuid;
   protected String destinationBranchGuid;
   @XmlElement(required = true)
   protected RemoteNetworkSender1 networkSender;

   /**
    * Gets the value of the eventTypeGuid property.
    *
    * @return possible object is {@link String }
    */
   public String getEventTypeGuid() {
      return eventTypeGuid;
   }

   /**
    * Sets the value of the eventTypeGuid property.
    *
    * @param value allowed object is {@link String }
    */
   public void setEventTypeGuid(String value) {
      this.eventTypeGuid = value;
   }

   public String getBranchGuid() {
      return branchGuid;
   }

   public BranchId getBranch() {
      return TokenFactory.createBranch(Long.valueOf(branchGuid));
   }

   public void setBranch(BranchId banch) {
      this.branchGuid = banch.getId().toString();
   }

   public String getDestinationBranchGuid() {
      return destinationBranchGuid;
   }

   public BranchId getDestinationBranch() {
      return Strings.isNumeric(destinationBranchGuid) ? TokenFactory.createBranch(
         Long.valueOf(destinationBranchGuid)) : null;
   }

   public void setDestinationBranch(BranchId branch) {
      this.destinationBranchGuid = branch == null ? null : branch.getId().toString();
   }

   /**
    * Gets the value of the networkSender property.
    *
    * @return possible object is {@link RemoteNetworkSender1 }
    */
   @Override
   public RemoteNetworkSender1 getNetworkSender() {
      return networkSender;
   }

   /**
    * Sets the value of the networkSender property.
    *
    * @param value allowed object is {@link RemoteNetworkSender1 }
    */
   public void setNetworkSender(RemoteNetworkSender1 value) {
      this.networkSender = value;
   }

}
