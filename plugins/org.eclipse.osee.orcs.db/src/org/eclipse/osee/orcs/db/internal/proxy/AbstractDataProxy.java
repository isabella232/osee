/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.orcs.db.internal.proxy;

import org.eclipse.osee.framework.core.data.GammaId;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.Attribute;
import org.eclipse.osee.orcs.core.ds.DataProxy;
import org.eclipse.osee.orcs.core.ds.ResourceNameResolver;

/**
 * @author Roberto E. Escobar
 */
public abstract class AbstractDataProxy<T> implements DataProxy<T> {
   private Storage storage;
   private Log logger;
   private GammaId gammaId;
   private boolean isNewGammaId;
   private ResourceNameResolver resolver;
   private Attribute<T> attribute;

   @Override
   public void setAttribute(Attribute<T> attribute) {
      this.attribute = attribute;
   }

   protected Attribute<T> getAttribute() {
      return attribute;
   }

   @Override
   public String getUri() {
      return getStorage().getLocator();
   }

   @Override
   public void setGamma(GammaId gammaId, boolean isNewGammaId) {
      this.gammaId = gammaId;
      this.isNewGammaId = isNewGammaId;
   }

   @Override
   public GammaId getGammaId() {
      return gammaId;
   }

   @Override
   public void persist() {
      if (isNewGammaId) {
         storage.persist(gammaId.getId());
      }
   }

   @Override
   public void rollBack() {
      if (isNewGammaId) {
         purge();
      }
   }

   protected Storage getStorage() {
      return storage;
   }

   protected void setStorage(Storage storage) {
      this.storage = storage;
   }

   protected Log getLogger() {
      return logger;
   }

   protected void setLogger(Log logger) {
      this.logger = logger;
   }

   @Override
   public void setResolver(ResourceNameResolver resolver) {
      this.resolver = resolver;
   }

   protected ResourceNameResolver getResolver() {
      return resolver;
   }

   @Override
   public boolean isInMemory() {
      return storage.isInitialized() && storage.isDataValid();
   }

   @Override
   public String toString() {
      return String.format("%s [value:[%s]]", getClass().getSimpleName(), getDisplayableString());
   }

   @Override
   public void purge() {
      storage.purge();
   }
}