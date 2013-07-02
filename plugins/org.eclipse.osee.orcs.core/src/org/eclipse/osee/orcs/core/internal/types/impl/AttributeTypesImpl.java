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
package org.eclipse.osee.orcs.core.internal.types.impl;

import java.util.Collection;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XAttributeType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XOseeEnumType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.data.AttributeTypes;
import org.eclipse.osee.orcs.data.EnumType;

/**
 * @author Roberto E. Escobar
 */
public class AttributeTypesImpl implements AttributeTypes {

   public static interface AttributeTypeIndexProvider {
      AttributeTypeIndex getAttributeTypeIndex() throws OseeCoreException;
   }

   public static interface EnumTypeIndexProvider {
      EnumTypeIndex getEnumTypeIndex() throws OseeCoreException;
   }

   private static final String ATTRIBUTE_OCCURRENCE_UNLIMITED = "unlimited";
   private static final String BASE_TYPE_NAMESPACE = "org.eclipse.osee.framework.skynet.core.";

   private final AttributeTypeIndexProvider provider;
   private final EnumTypeIndexProvider enumTypeIndexProvider;

   public AttributeTypesImpl(AttributeTypeIndexProvider provider, EnumTypeIndexProvider enumTypeIndexProvider) {
      this.provider = provider;
      this.enumTypeIndexProvider = enumTypeIndexProvider;
   }

   private XAttributeType getType(IAttributeType attrType) throws OseeCoreException {
      Conditions.checkNotNull(attrType, "attributeType");
      return provider.getAttributeTypeIndex().getDslTypeByToken(attrType);
   }

   @Override
   public Collection<? extends IAttributeType> getAll() throws OseeCoreException {
      return provider.getAttributeTypeIndex().getAllTokens();
   }

   @Override
   public IAttributeType getByUuid(Long uuid) throws OseeCoreException {
      Conditions.checkNotNull(uuid, "uuid");
      return provider.getAttributeTypeIndex().getTokenByUuid(uuid);
   }

   private String getQualifiedTypeName(String id) {
      String value = !Strings.isValid(id) ? Strings.emptyString() : id;
      if (!value.contains(".")) {
         value = BASE_TYPE_NAMESPACE + id;
      }
      return value;
   }

   @Override
   public String getBaseAttributeTypeId(IAttributeType attrType) throws OseeCoreException {
      XAttributeType type = getType(attrType);
      return getQualifiedTypeName(type.getBaseAttributeType());
   }

   @Override
   public String getAttributeProviderId(IAttributeType attrType) throws OseeCoreException {
      XAttributeType type = getType(attrType);
      return getQualifiedTypeName(type.getDataProvider());
   }

   @Override
   public String getDefaultValue(IAttributeType attrType) throws OseeCoreException {
      XAttributeType type = getType(attrType);
      return type.getDefaultValue();
   }

   @Override
   public int getMaxOccurrences(IAttributeType attrType) throws OseeCoreException {
      XAttributeType type = getType(attrType);
      String maxValue = type.getMax();
      int max = Integer.MAX_VALUE;
      if (!ATTRIBUTE_OCCURRENCE_UNLIMITED.equals(maxValue)) {
         if (Strings.isValid(maxValue)) {
            max = Integer.parseInt(maxValue);
         }
      }
      return max;
   }

   @Override
   public int getMinOccurrences(IAttributeType attrType) throws OseeCoreException {
      XAttributeType type = getType(attrType);
      String minValue = type.getMin();
      int min = 0;
      if (Strings.isValid(minValue)) {
         min = Integer.parseInt(minValue);
      }
      return min;
   }

   @Override
   public String getFileTypeExtension(IAttributeType attrType) throws OseeCoreException {
      XAttributeType type = getType(attrType);
      String value = type.getFileExtension();
      return Strings.isValid(value) ? value : Strings.emptyString();
   }

   @Override
   public String getTaggerId(IAttributeType attrType) throws OseeCoreException {
      XAttributeType type = getType(attrType);
      String value = type.getTaggerId();
      return Strings.isValid(value) ? value : Strings.emptyString();
   }

   @Override
   public boolean isTaggable(IAttributeType attrType) throws OseeCoreException {
      boolean toReturn = false;
      String taggerId = getTaggerId(attrType);
      if (taggerId != null) {
         toReturn = Strings.isValid(taggerId.trim());
      }
      return toReturn;
   }

   @Override
   public boolean isEnumerated(IAttributeType attrType) throws OseeCoreException {
      XAttributeType type = getType(attrType);
      XOseeEnumType enumType = type.getEnumType();
      return enumType != null;
   }

   @Override
   public EnumType getEnumType(IAttributeType attrType) throws OseeCoreException {
      EnumType toReturn = null;
      XAttributeType type = getType(attrType);
      XOseeEnumType enumType = type.getEnumType();
      if (enumType != null) {
         toReturn = enumTypeIndexProvider.getEnumTypeIndex().getTokenByDslType(enumType);
      }
      return toReturn;
   }

   @Override
   public String getDescription(IAttributeType attrType) throws OseeCoreException {
      XAttributeType type = getType(attrType);
      String value = type.getDescription();
      return Strings.isValid(value) ? value : Strings.emptyString();
   }

   @Override
   public boolean isEmpty() throws OseeCoreException {
      return provider.getAttributeTypeIndex().isEmpty();
   }

   @Override
   public int size() throws OseeCoreException {
      return provider.getAttributeTypeIndex().size();
   }

   @Override
   public Collection<? extends IAttributeType> getAllTaggable() throws OseeCoreException {
      return provider.getAttributeTypeIndex().getAllTaggable();
   }

   @Override
   public boolean exists(IAttributeType item) throws OseeCoreException {
      return provider.getAttributeTypeIndex().existsByUuid(item.getGuid());
   }

   @Override
   public String getMediaType(IAttributeType attrType) throws OseeCoreException {
      XAttributeType type = getType(attrType);
      String value = type.getMediaType();
      return Strings.isValid(value) ? value : Strings.emptyString();
   }

   @Override
   public boolean hasMediaType(IAttributeType attrType) throws OseeCoreException {
      boolean toReturn = false;
      String mediaType = getMediaType(attrType);
      if (mediaType != null) {
         toReturn = Strings.isValid(mediaType.trim());
      }
      return toReturn;
   }
}
