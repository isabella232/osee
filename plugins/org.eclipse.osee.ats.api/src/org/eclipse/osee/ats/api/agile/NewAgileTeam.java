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
package org.eclipse.osee.ats.api.agile;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Donald G. Dunne
 */
@XmlRootElement
public class NewAgileTeam {

   private String name;
   private String guid;
   private long uuid;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getGuid() {
      return guid;
   }

   public void setGuid(String guid) {
      this.guid = guid;
   }

   public long getUuid() {
      return uuid;
   }

   /**
    * Provided for deserialization. Setting will not apply to new artifact.
    */
   public void setUuid(long uuid) {
      this.uuid = uuid;
   }

}
