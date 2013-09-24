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
package org.eclipse.osee.orcs.db.internal.proxy;

import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.DataProxy;
import org.eclipse.osee.orcs.core.ds.ResourceNameResolver;

/**
 * @author Roberto E. Escobar
 */
public abstract class AbstractDataProxy implements DataProxy {
   private Storage storage;
   private Log logger;

   public AbstractDataProxy() {
      super();
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
      storage.setResolver(resolver);
   }

   @Override
   public ResourceNameResolver getResolver() {
      return storage.getResolver();
   }

   @Override
   public boolean isInMemory() {
      return storage.isInitialized() && storage.isDataValid();
   }

}