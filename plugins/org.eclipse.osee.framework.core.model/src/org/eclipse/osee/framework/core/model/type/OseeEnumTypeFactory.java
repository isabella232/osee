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
package org.eclipse.osee.framework.core.model.type;

import org.eclipse.osee.framework.core.enums.StorageState;
import org.eclipse.osee.framework.core.model.OseeEnumEntry;
import org.eclipse.osee.framework.core.model.cache.AbstractOseeCache;
import org.eclipse.osee.framework.core.model.cache.IOseeCache;
import org.eclipse.osee.framework.core.model.cache.IOseeTypeFactory;
import org.eclipse.osee.framework.core.model.cache.OseeEnumTypeCache;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;

/**
 * @author Roberto E. Escobar
 */
public class OseeEnumTypeFactory implements IOseeTypeFactory {

   public OseeEnumType createEnumType(Long guid, String name) throws OseeCoreException {
      Conditions.checkNotNullOrEmpty(name, "osee enum type name");
      return new OseeEnumType(guid, name);
   }

   public OseeEnumEntry createEnumEntry(String guid, String name, int ordinal, String description) throws OseeCoreException {
      Conditions.checkNotNullOrEmpty(name, "osee enum entry name");
      Conditions.checkExpressionFailOnTrue(ordinal < 0, "ordinal must be greater than or equal to zero");
      String checkedGuid = Conditions.checkGuidCreateIfNeeded(guid);
      return new OseeEnumEntry(checkedGuid, name, ordinal, description);
   }

   public OseeEnumType createOrUpdate(IOseeCache<Long, OseeEnumType> cache, long enumTypeId, StorageState storageState, Long guid, String enumTypeName) throws OseeCoreException {
      Conditions.checkNotNull(cache, "OseeEnumTypeCache");
      OseeEnumType oseeEnumType = cache.getById(enumTypeId);
      if (oseeEnumType == null) {
         oseeEnumType = createEnumType(guid, enumTypeName);
         oseeEnumType.setId(enumTypeId);
         oseeEnumType.setStorageState(storageState);
         cache.cache(oseeEnumType);
      } else {
         oseeEnumType.setName(enumTypeName);
      }
      return oseeEnumType;
   }

   public OseeEnumType createOrUpdate(OseeEnumTypeCache cache, Long guid, String enumTypeName) throws OseeCoreException {
      Conditions.checkNotNull(cache, "OseeEnumTypeCache");
      OseeEnumType oseeEnumType = cache.getByGuid(guid);
      if (oseeEnumType == null) {
         oseeEnumType = createEnumType(guid, enumTypeName);
         cache.cache(oseeEnumType);
      } else {
         oseeEnumType.setName(enumTypeName);
      }
      return oseeEnumType;
   }

   public OseeEnumEntry createOrUpdate(IOseeCache<Long, OseeEnumType> cache, Long enumTypeGuid, String enumEntryGuid, String enumEntryName, int ordinal, String enumEntryDescription) throws OseeCoreException {
      Conditions.checkNotNull(cache, "OseeEnumTypeCache");
      OseeEnumType oseeEnumType = ((AbstractOseeCache<Long, OseeEnumType>) cache).getByGuid(enumTypeGuid);
      OseeEnumEntry enumEntry = oseeEnumType.getEntryByGuid(enumEntryGuid);
      if (enumEntry == null) {
         enumEntry = createEnumEntry(enumEntryGuid, enumEntryName, ordinal, enumEntryDescription);
         oseeEnumType.addEntry(enumEntry);
      } else {
         enumEntry.setName(enumEntryName);
         enumEntry.setOrdinal(ordinal);
         enumEntry.setDescription(enumEntryDescription);
      }
      return enumEntry;
   }
}
