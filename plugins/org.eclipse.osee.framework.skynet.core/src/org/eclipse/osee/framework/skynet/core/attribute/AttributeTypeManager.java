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
package org.eclipse.osee.framework.skynet.core.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeTypeDoesNotExist;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.OseeEnumEntry;
import org.eclipse.osee.framework.core.model.cache.AbstractOseeCache;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.framework.core.services.IOseeCachingService;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.attribute.providers.IAttributeDataProvider;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;

/**
 * @author Ryan D. Brooks
 */
public class AttributeTypeManager {

   private static IOseeCachingService getCacheService() throws OseeCoreException {
      return ServiceUtil.getOseeCacheService();
   }

   public static AbstractOseeCache<Long, AttributeType> getCache() throws OseeCoreException {
      return getCacheService().getAttributeTypeCache();
   }

   public static BranchCache getBranchCache() throws OseeCoreException {
      return getCacheService().getBranchCache();
   }

   public static Collection<IAttributeType> getValidAttributeTypes(IOseeBranch branchToken) throws OseeCoreException {
      Branch branch = getBranchCache().get(branchToken);
      Set<IAttributeType> attributeTypes = new HashSet<IAttributeType>(100);
      for (ArtifactType artifactType : ArtifactTypeManager.getAllTypes()) {
         attributeTypes.addAll(artifactType.getAttributeTypes(branch));
      }
      return attributeTypes;
   }

   public static Collection<AttributeType> getAllTypes() throws OseeCoreException {
      return getCache().getAll();
   }

   public static Collection<IAttributeType> getTaggableTypes() throws OseeCoreException {
      Collection<IAttributeType> taggableTypes = new ArrayList<IAttributeType>();
      for (AttributeType type : getAllTypes()) {
         if (type.isTaggable()) {
            taggableTypes.add(type);
         }
      }
      return taggableTypes;
   }

   public static boolean typeExists(String name) throws OseeCoreException {
      return !getCache().getByName(name).isEmpty();
   }

   /**
    * @return Returns the attribute type matching the guid
    * @param guid attribute type guid to match
    */
   public static AttributeType getTypeByGuid(Long guid) throws OseeCoreException {
      if (guid == null) {
         throw new OseeArgumentException("[%s] is not a valid guid", guid);
      }
      AttributeType attributeType = getCache().getByGuid(guid);
      if (attributeType == null) {
         throw new OseeTypeDoesNotExist("Attribute Type [%s] is not available.", guid);
      }
      return attributeType;
   }

   public static AttributeType getType(IAttributeType type) throws OseeCoreException {
      return getTypeByGuid(type.getGuid());
   }

   /**
    * @return the attribute type with the given name or throws an OseeTypeDoesNotExist if it does not exist.
    */
   public static AttributeType getType(String name) throws OseeCoreException {
      AttributeType attributeType = getCache().getUniqueByName(name);
      if (attributeType == null) {
         throw new OseeTypeDoesNotExist("Attribute Type with name [%s] does not exist.", name);
      }
      return attributeType;
   }

   /**
    * Returns the attribute type with the given type id or throws an IllegalArgumentException if it does not exist.
    */
   public static AttributeType getType(int attrTypeId) throws OseeCoreException {
      AttributeType attributeType = getCache().getById(attrTypeId);
      if (attributeType == null) {
         throw new OseeTypeDoesNotExist("Attribute type: %d is not available.", attrTypeId);
      }

      return attributeType;
   }

   private static Set<String> getEnumerationValues(AttributeType attributeType) throws OseeCoreException {
      return attributeType.getOseeEnumType().valuesAsOrderedStringSet();
   }

   public static Set<String> getEnumerationValues(IAttributeType attributeType) throws OseeCoreException {
      return getEnumerationValues(getType(attributeType));
   }

   public static Map<String, String> getEnumerationValueDescriptions(IAttributeType attributeType) throws OseeCoreException {
      Map<String, String> values = new HashMap<String, String>();
      for (OseeEnumEntry entry : AttributeTypeManager.getType(attributeType).getOseeEnumType().values()) {
         values.put(entry.getName(), entry.getDescription());
      }
      return values;
   }

   public static int getMinOccurrences(IAttributeType attributeType) throws OseeCoreException {
      return getType(attributeType).getMinOccurrences();
   }

   public static int getMaxOccurrences(IAttributeType attributeType) throws OseeCoreException {
      return getType(attributeType).getMaxOccurrences();
   }

   public static Set<String> getEnumerationValues(String attributeName) throws OseeCoreException {
      return getEnumerationValues(getType(attributeName));
   }

   public static void persist() throws OseeCoreException {
      getCache().storeAllModified();
   }

   @SuppressWarnings("rawtypes")
   public static boolean isBaseTypeCompatible(Class<? extends Attribute> baseType, IAttributeType attributeType) throws OseeCoreException {
      return baseType.isAssignableFrom(getAttributeBaseClass(attributeType));
   }

   public static Class<? extends Attribute<?>> getAttributeBaseClass(IAttributeType attributeType) throws OseeCoreException {
      return AttributeExtensionManager.getAttributeClassFor(getType(attributeType).getBaseAttributeTypeId());
   }

   public static Class<? extends IAttributeDataProvider> getAttributeProviderClass(AttributeType attributeType) throws OseeCoreException {
      return AttributeExtensionManager.getAttributeProviderClassFor(attributeType.getAttributeProviderId());
   }
}