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
package org.eclipse.osee.orcs.rest.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Donald G. Dunne
 */

@XmlRootElement
public class IdeVersion {

   private List<String> versions;

   public List<String> getVersions() {
      if (versions == null) {
         versions = new ArrayList<>();
      }
      return versions;
   }

   public void addVersion(String version) {
      if (!getVersions().contains(version)) {
         getVersions().add(version);
      }
   }

   public void setVersions(List<String> versions) {
      this.versions = versions;
   }
}
