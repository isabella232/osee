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
package org.eclipse.osee.framework.core.model.cache;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.enums.OseeCacheEnum;
import org.eclipse.osee.framework.core.model.AbstractOseeType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Roberto E. Escobar
 */
public abstract class AbstractOseeLoadingCache<T extends AbstractOseeType> extends AbstractOseeCache<T> implements IOseeLoadingCache<T> {
   private final IOseeDataAccessor<T> dataAccessor;
   private final AtomicBoolean wasLoaded;
   private long lastLoaded;

   protected AbstractOseeLoadingCache(OseeCacheEnum cacheId, IOseeDataAccessor<T> dataAccessor, boolean uniqueName) {
      super(cacheId, uniqueName);
      this.lastLoaded = 0;
      this.wasLoaded = new AtomicBoolean(false);
      this.dataAccessor = dataAccessor;
   }

   @Override
   public long getLastLoaded() {
      return lastLoaded;
   }

   private void setLastLoaded(long lastLoaded) {
      this.lastLoaded = lastLoaded;
   }

   @Override
   public boolean isLoaded() {
      return wasLoaded.get();
   }

   @Override
   public void invalidate() {
      wasLoaded.set(false);
   }

   @Override
   public synchronized void decacheAll() {
      super.decacheAll();
      invalidate();
   }

   @Override
   public synchronized void ensurePopulated() throws OseeCoreException {
      if (wasLoaded.compareAndSet(false, true)) {
         try {
            reloadCache();
         } catch (OseeCoreException ex) {
            wasLoaded.set(false);
            throw ex;
         }
      }
   }

   @Override
   public synchronized boolean reloadCache() throws OseeCoreException {
      dataAccessor.load(this);
      OseeLog.log(this.getClass(), Level.INFO, "Loaded " + getCacheId().toString().toLowerCase());
      setLastLoaded(System.currentTimeMillis());
      wasLoaded.set(true);
      return true;
   }

   @Override
   protected void store(Collection<T> toStore) throws OseeCoreException {
      dataAccessor.store(toStore);
   }

}
