/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.rest.model.search;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.osee.framework.jdk.core.type.MatchLocation;

/**
 * @author John Misinco
 */
@XmlRootElement(name = "SearchMatch")
public class SearchMatch {

   private int artId;
   private int attrId;

   @XmlTransient
   private List<MatchLocation> locations;

   public SearchMatch() {
      // default constructor
   }

   public SearchMatch(int artId, int attrId, List<MatchLocation> locations) {
      this.artId = artId;
      this.attrId = attrId;
      this.locations = locations;
   }

   public int getArtId() {
      return artId;
   }

   public void setArtId(int artId) {
      this.artId = artId;
   }

   public int getAttrId() {
      return attrId;
   }

   public void setAttrId(int attrId) {
      this.attrId = attrId;
   }

   public List<MatchLocation> getLocations() {
      return locations;
   }

   public void setLocations(List<MatchLocation> locations) {
      this.locations = locations;
   }

}
