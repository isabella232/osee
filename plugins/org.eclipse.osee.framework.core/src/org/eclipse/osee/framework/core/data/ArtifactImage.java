/*********************************************************************
 * Copyright (c) 2017 Boeing
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

package org.eclipse.osee.framework.core.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * @author Donald G. Dunne
 */
public class ArtifactImage {

   private ArtifactTypeToken artifactType;
   @JsonSerialize(using = ToStringSerializer.class)
   private Long artifactTypeId;
   private String artifactTypeName;
   private String imageName;
   private String baseUrl;

   public ArtifactImage(ArtifactTypeToken artifactType, String imageName, String baseUrl) {
      this.artifactType = artifactType;
      this.imageName = imageName;
      this.baseUrl = baseUrl;
   }

   @JsonIgnore
   public ArtifactTypeToken getArtifactType() {
      return artifactType;
   }

   public String getImageName() {
      return imageName;
   }

   public void setArtifactType(ArtifactTypeToken artifactType) {
      this.artifactType = artifactType;
   }

   public void setImageName(String imageName) {
      this.imageName = imageName;
   }

   public static ArtifactImage construct(ArtifactTypeToken artifactType, String imageName) {
      return construct(artifactType, imageName, null);
   }

   public static ArtifactImage construct(ArtifactTypeToken artifactType, String imageName, String baseUrl) {
      return new ArtifactImage(artifactType, imageName, baseUrl);
   }

   public String getBaseUrl() {
      return baseUrl;
   }

   public void setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
   }

   public Long getArtifactTypeId() {
      if (artifactType != null) {
         return artifactType.getId();
      }
      return artifactTypeId;
   }

   public void setArtifactTypeId(Long artifactTypeId) {
      this.artifactTypeId = artifactTypeId;
   }

   public String getArtifactTypeName() {
      if (artifactType != null) {
         return artifactType.getName();
      }
      return artifactTypeName;
   }

   public void setArtifactTypeName(String artifactTypeName) {
      this.artifactTypeName = artifactTypeName;
   }

}
