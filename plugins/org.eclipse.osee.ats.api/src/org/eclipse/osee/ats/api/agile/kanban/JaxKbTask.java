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
package org.eclipse.osee.ats.api.agile.kanban;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JaxKbTask {

   private String name;
   private String guid;
   private Map<String, String> attributeMap = new HashMap<String, String>();
   private String branchGuid;
   private boolean canEdit;
   private String artifactType;

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

   public Map<String, String> getAttributeMap() {
      return attributeMap;
   }

   public void setAttributeMap(Map<String, String> attributeMap) {
      this.attributeMap = attributeMap;
   }

   public String getBranchGuid() {
      return branchGuid;
   }

   public void setBranchGuid(String branchGuid) {
      this.branchGuid = branchGuid;
   }

   public boolean isCanEdit() {
      return canEdit;
   }

   public void setCanEdit(boolean canEdit) {
      this.canEdit = canEdit;
   }

   public String getArtifactType() {
      return artifactType;
   }

   public void setArtifactType(String artifactType) {
      this.artifactType = artifactType;
   }

}
