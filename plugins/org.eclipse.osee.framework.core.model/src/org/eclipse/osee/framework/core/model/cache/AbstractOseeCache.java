/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.model.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.osee.framework.core.enums.OseeCacheEnum;
import org.eclipse.osee.framework.core.enums.StorageState;
import org.eclipse.osee.framework.core.exception.OseeTypeDoesNotExist;
import org.eclipse.osee.framework.core.model.AbstractOseeType;
import org.eclipse.osee.framework.core.model.TypeUtil;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.jdk.core.type.Identity;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;

/**
 * @author Roberto E. Escobar
 */
public abstract class AbstractOseeCache<T extends AbstractOseeType> implements IOseeCache<T> {
   private final HashCollection<String, T> nameToTypeMap = new HashCollection<>(true, CopyOnWriteArrayList.class);
   private final ConcurrentHashMap<Long, T> idToTypeMap = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<Long, T> guidToTypeMap = new ConcurrentHashMap<>();

   private final OseeCacheEnum cacheId;
   private final boolean uniqueName;

   protected AbstractOseeCache(OseeCacheEnum cacheId, boolean uniqueName) {
      this.cacheId = cacheId;
      this.uniqueName = uniqueName;
   }

   @Override
   public synchronized void decacheAll() {
      clearAdditionalData();
      nameToTypeMap.clear();
      idToTypeMap.clear();
      guidToTypeMap.clear();
   }

   protected void clearAdditionalData() {
      // for subclass overriding
   }

   @Override
   public OseeCacheEnum getCacheId() {
      return cacheId;
   }

   @Override
   public int size() {
      return guidToTypeMap.size();
   }

   public boolean existsByGuid(Long id) throws OseeCoreException {
      ensurePopulated();
      return guidToTypeMap.containsKey(id);
   }

   @Override
   public void decache(T... types) throws OseeCoreException {
      Conditions.checkNotNull(types, "types to de-cache");
      for (T type : types) {
         decache(type);
      }
   }

   @Override
   public void decache(T type) throws OseeCoreException {
      Conditions.checkNotNull(type, "type to de-cache");
      ensurePopulated();
      guidToTypeMap.remove(type.getId());
      decacheByName(type);
      if (type.isIdValid()) {
         idToTypeMap.remove(TypeUtil.getId(type));
      }
   }

   /**
    * this method is intended for use by subclasses only. The calling method must synchronize the use of this view of
    * the views because it is not a copy. This method exists to improve performance for subclasses
    */
   protected synchronized Collection<T> getRawValues() throws OseeCoreException {
      ensurePopulated();
      return guidToTypeMap.values();
   }

   private void decacheByName(T type) {
      Set<String> keysToRemove = new HashSet<>();

      for (String name : nameToTypeMap.keySet()) {
         Collection<T> items = nameToTypeMap.getValues(name);
         if (items != null && items.contains(type)) {
            keysToRemove.add(name);
         }
      }

      for (String key : keysToRemove) {
         nameToTypeMap.removeValue(key, type);
      }
   }

   @Override
   public void cache(T... types) throws OseeCoreException {
      Conditions.checkNotNull(types, "types to cache");
      for (T type : types) {
         cache(type);
      }
   }

   @Override
   public void cache(T type) throws OseeCoreException {
      Conditions.checkNotNull(type, "type to cache");
      ensurePopulated();
      nameToTypeMap.put(type.getName(), type);
      guidToTypeMap.putIfAbsent(type.getId(), type);
      cacheById(type);
      if (isNameUniquenessEnforced()) {
         checkNameUnique(type);
      }
   }

   public boolean isNameUniquenessEnforced() {
      return uniqueName;
   }

   private void checkNameUnique(T type) throws OseeCoreException {
      ensurePopulated();
      Collection<T> cachedTypes = getByName(type.getName());
      Set<String> itemsFound = new HashSet<>();
      // TODO Need to revisit this based on deleted types
      //      for (T cachedType : cachedTypes) {
      //         if (!cachedType.getGuid().equals(type.getGuid()) && !cachedType.getModificationType().isDeleted()) {
      //            itemsFound.add(String.format("[%s:%s]", cachedType.getName(), cachedType.getGuid()));
      //         }
      //      }
      if (cachedTypes.size() > 1) {
         throw new OseeStateException("Item [%s:%s] does not have a unique name. Matching types [%s]", type.getName(),
            type.getId(), itemsFound);
      }
   }

   private void cacheById(T type) throws OseeCoreException {
      Conditions.checkNotNull(type, "type to cache");
      ensurePopulated();
      if (type.isIdValid()) {
         idToTypeMap.putIfAbsent(TypeUtil.getId(type), type);
      }
   }

   @Override
   public Collection<T> getAll() throws OseeCoreException {
      ensurePopulated();
      return new ArrayList<T>(guidToTypeMap.values());
   }

   @Override
   public T getById(Number typeId) throws OseeCoreException {
      ensurePopulated();
      return idToTypeMap.get(typeId.longValue());
   }

   public T getUniqueByName(String typeName) throws OseeCoreException {
      ensurePopulated();
      Collection<T> values = getByName(typeName);
      if (values.size() > 1) {
         throw new OseeStateException("Multiple items matching [%s] name exist", typeName);
      }
      return values.isEmpty() ? null : values.iterator().next();
   }

   public Collection<T> getByName(String typeName) throws OseeCoreException {
      ensurePopulated();
      Collection<T> types = new ArrayList<>();
      Collection<T> values = nameToTypeMap.getValues(typeName);
      if (values != null) {
         types.addAll(values);
      }
      return types;
   }

   public T getBySoleName(String typeName) throws OseeCoreException {
      ensurePopulated();
      Collection<T> types = getByName(typeName);
      if (types.size() != 1) {
         throw new OseeArgumentException("AbstractOseeCache expected 1 type but found [%d] types for [%s]",
            types.size(), typeName);
      }
      return types.iterator().next();
   }

   @Override
   public T getByGuid(Long guid) throws OseeCoreException {
      ensurePopulated();
      return guidToTypeMap.get(guid);
   }

   public T get(Identity<Long> token) throws OseeCoreException {
      ensurePopulated();
      return getByGuid(token.getGuid());
   }

   @Override
   public Collection<T> getAllDirty() throws OseeCoreException {
      ensurePopulated();
      Collection<T> dirtyItems = new HashSet<>();
      for (T type : guidToTypeMap.values()) {
         if (type.isDirty()) {
            dirtyItems.add(type);
         }
      }
      return dirtyItems;
   }

   @Override
   public void storeAllModified() throws OseeCoreException {
      storeItems(getAllDirty());
   }

   public void storeByGuid(Collection<Long> guids) throws OseeCoreException {
      ensurePopulated();
      Conditions.checkNotNull(guids, "guids to store");
      Collection<T> items = new HashSet<>();
      for (Long guid : guids) {
         T type = getByGuid(guid);
         if (type == null) {
            throw new OseeTypeDoesNotExist(String.format("Item was not found [%s]", guid));
         }
         items.add(type);
      }
      storeItems(items);
   }

   @Override
   public void storeItems(T... items) throws OseeCoreException {
      storeItems(Arrays.asList(items));
   }

   @Override
   public void storeItems(Collection<T> toStore) throws OseeCoreException {
      Conditions.checkDoesNotContainNulls(toStore, "items to store");
      if (!toStore.isEmpty()) {
         store(toStore);
         synchronized (this) {
            for (T type : toStore) {
               decache(type);
               if (StorageState.PURGED != type.getStorageState()) {
                  cache(type);
               }
            }
         }
      }
   }

   public void cacheFrom(AbstractOseeCache<T> source) throws OseeCoreException {
      for (T type : source.getAll()) {
         cache(type);
      }
   }

   protected void ensurePopulated() throws OseeCoreException {
      // Do nothing
   }

   protected void store(Collection<T> toStore) throws OseeCoreException {
      // Do nothing
   }
}
