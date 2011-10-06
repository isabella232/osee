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
package org.eclipse.osee.orcs.db.mocks;

import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;
import org.eclipse.osee.framework.resource.management.IResource;
import org.eclipse.osee.framework.resource.management.IResourceListener;
import org.eclipse.osee.framework.resource.management.IResourceLocator;
import org.eclipse.osee.framework.resource.management.IResourceManager;
import org.eclipse.osee.framework.resource.management.IResourceProvider;

/**
 * @author Roberto E. Escobar
 */
public class MockResourceManager implements IResourceManager {

   @Override
   public boolean addResourceListener(IResourceListener listener) {
      return false;
   }

   @Override
   public boolean removeResourceListener(IResourceListener listener) {
      return false;
   }

   @Override
   public boolean addResourceProvider(IResourceProvider resourceProvider) {
      return false;
   }

   @Override
   public boolean removeResourceProvider(IResourceProvider resourceProvider) {
      return false;
   }

   @Override
   public IResource acquire(IResourceLocator locator, PropertyStore options) throws OseeCoreException {
      return null;
   }

   @Override
   public boolean exists(IResourceLocator locator) throws OseeCoreException {
      return false;
   }

   @Override
   public IResourceLocator save(IResourceLocator locatorHint, IResource resource, PropertyStore options) throws OseeCoreException {
      return null;
   }

   @Override
   public int delete(IResourceLocator locator) throws OseeCoreException {
      return 0;
   }

}
