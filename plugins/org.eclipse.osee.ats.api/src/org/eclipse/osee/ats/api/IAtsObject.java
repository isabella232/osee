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
package org.eclipse.osee.ats.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import org.eclipse.osee.ats.api.config.WorkType;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.data.ArtifactTypeId;
import org.eclipse.osee.framework.core.data.ArtifactTypeToken;
import org.eclipse.osee.framework.jdk.core.type.HasDescription;
import org.eclipse.osee.framework.jdk.core.type.NamedId;

/**
 * @author Donald G. Dunne
 */
public interface IAtsObject extends NamedId, HasDescription {

   default ArtifactToken getStoreObject() {
      return null;
   }

   default void setStoreObject(ArtifactToken artifact) {
      // do nothing
   }

   @Override
   default String getDescription() {
      return getName();
   }

   ArtifactTypeToken getArtifactType();

   default boolean isOfType(ArtifactTypeId... otherTypes) {
      for (ArtifactTypeId otherType : otherTypes) {
         if (getArtifactType().inheritsFrom(otherType)) {
            return true;
         }
      }
      return false;
   }

   default boolean isTypeEqual(ArtifactTypeId... artifactTypes) {
      ArtifactTypeId artifactType = getArtifactType();
      for (ArtifactTypeId artType : artifactTypes) {
         if (artifactType.equals(artType)) {
            return true;
         }
      }
      return false;
   }

   default ArtifactId getArtifactId() {
      return ArtifactId.valueOf(getId());
   }

   @JsonIgnore
   default ArtifactToken getArtifactToken() {
      return getStoreObject();
   }

   public AtsApi getAtsApi();

   public Collection<WorkType> getWorkTypes();

   public boolean isWorkType(WorkType workType);

   public Collection<String> getTags();

   public boolean hasTag(String tag);

}