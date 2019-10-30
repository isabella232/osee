/*******************************************************************************
 * Copyright (c) 2019 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.data;

/**
 * @author Ryan D. Brooks
 */
public final class ArtifactTypeAttributeTypeMetaData {
   private final Multiplicity multiplicity;
   private final Long enumTypeId;
   private final String defaultValue;

   public ArtifactTypeAttributeTypeMetaData(Multiplicity multiplicity, String defaultValue) {
      this(multiplicity, defaultValue, null);
   }

   public ArtifactTypeAttributeTypeMetaData(Multiplicity multiplicity, String defaultValue, Long enumTypeId) {
      this.multiplicity = multiplicity;
      this.defaultValue = defaultValue;
      this.enumTypeId = enumTypeId;
   }

   public String getDefaultValue() {
      return defaultValue;
   }

   public Long getEnumTypeId() {
      return enumTypeId;
   }

   public Multiplicity getMultiplicity() {
      return multiplicity;
   }
}