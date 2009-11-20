/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.model;

import org.eclipse.osee.framework.core.cache.AbstractOseeCache;
import org.eclipse.osee.framework.core.cache.IOseeTypeFactory;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Conditions;

/**
 * @author Roberto E. Escobar
 */
public class AttributeTypeFactory implements IOseeTypeFactory {

   public AttributeTypeFactory() {
   }

   public AttributeType create(String guid, String name, String baseAttributeTypeId, String attributeProviderNameId, String fileTypeExtension, String defaultValue, OseeEnumType oseeEnumType, int minOccurrences, int maxOccurrences, String tipText, String taggerId) throws OseeCoreException {
      Conditions.checkNotNullOrEmpty(name, "attribute type name");
      //      checkNameUnique(cache, name);
      Conditions.checkNotNullOrEmpty(baseAttributeTypeId, "attribute base type id");
      Conditions.checkNotNullOrEmpty(attributeProviderNameId, "attribute provider id");
      Conditions.checkExpressionFailOnTrue(minOccurrences > 0 && defaultValue == null,
            "DefaultValue must be set for attribute [%s] with minOccurrences ", name, minOccurrences);

      Conditions.checkExpressionFailOnTrue(minOccurrences < 0, "minOccurrences must be greater than or equal to zero");
      Conditions.checkExpressionFailOnTrue(maxOccurrences < minOccurrences,
            "maxOccurences can not be less than minOccurences");

      String checkedGuid = Conditions.checkGuidCreateIfNeeded(guid);
      return new AttributeType(checkedGuid, name, baseAttributeTypeId, attributeProviderNameId, fileTypeExtension,
            defaultValue, oseeEnumType, minOccurrences, maxOccurrences, tipText, taggerId);
   }

   public AttributeType createOrUpdate(AbstractOseeCache<AttributeType> cache, String guid, String typeName, String baseAttributeTypeId, String attributeProviderNameId, String fileTypeExtension, String defaultValue, OseeEnumType oseeEnumType, int minOccurrences, int maxOccurrences, String description, String taggerId) throws OseeCoreException {
      AttributeType attributeType = cache.getByGuid(guid);

      String resolvedBaseAttributeType = null;
      String resolvedProviderType = null;

      if (attributeType == null) {
         attributeType =
               create(guid, typeName, resolvedBaseAttributeType, resolvedProviderType, fileTypeExtension, defaultValue,
                     oseeEnumType, minOccurrences, maxOccurrences, description, taggerId);
      } else {
         cache.decache(attributeType);
         attributeType.setFields(typeName, resolvedBaseAttributeType, resolvedProviderType, fileTypeExtension,
               defaultValue, oseeEnumType, minOccurrences, maxOccurrences, description, taggerId);
      }
      cache.cache(attributeType);
      return attributeType;
   }

   public AttributeType createOrUpdate(AbstractOseeCache<AttributeType> cache, int uniqueId, ModificationType modificationType, String guid, String typeName, String baseAttributeTypeId, String attributeProviderNameId, String fileTypeExtension, String defaultValue, OseeEnumType oseeEnumType, int minOccurrences, int maxOccurrences, String description, String taggerId) throws OseeCoreException {
      AttributeType attributeType = cache.getByGuid(guid);
      if (attributeType == null) {
         attributeType =
               create(guid, typeName, baseAttributeTypeId, attributeProviderNameId, fileTypeExtension, defaultValue,
                     oseeEnumType, minOccurrences, maxOccurrences, description, taggerId);
         attributeType.setId(uniqueId);
         attributeType.setModificationType(modificationType);
      } else {
         cache.decache(attributeType);
         attributeType.setFields(typeName, baseAttributeTypeId, attributeProviderNameId, fileTypeExtension,
               defaultValue, oseeEnumType, minOccurrences, maxOccurrences, description, taggerId);
      }
      cache.cache(attributeType);
      return attributeType;
   }
}
