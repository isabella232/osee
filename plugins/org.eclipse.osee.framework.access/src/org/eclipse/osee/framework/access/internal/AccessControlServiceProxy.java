/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.access.internal;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import org.eclipse.osee.framework.access.IAccessProvider;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.model.IBasicArtifact;
import org.eclipse.osee.framework.core.model.access.AccessDataQuery;
import org.eclipse.osee.framework.core.services.IAccessControlService;
import org.eclipse.osee.framework.core.services.IOseeCachingService;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.lifecycle.ILifecycleService;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.event.OseeEventService;
import org.eclipse.osee.framework.skynet.core.event.listener.EventQosType;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public final class AccessControlServiceProxy implements IAccessControlService {

   private final List<ServiceReference<IAccessProvider>> registered =
      new CopyOnWriteArrayList<ServiceReference<IAccessProvider>>();

   private final List<ServiceReference<IAccessProvider>> pendingProviders =
      new CopyOnWriteArrayList<ServiceReference<IAccessProvider>>();

   private IOseeDatabaseService dbService;
   private IOseeCachingService cachingService;
   private OseeEventService eventService;
   private ILifecycleService lifecycleService;

   private AccessControlService accessService;
   private AccessEventListener accessEventListener;
   private Thread thread;

   public void setDbService(IOseeDatabaseService dbService) {
      this.dbService = dbService;
   }

   public void setCachingService(IOseeCachingService cachingService) {
      this.cachingService = cachingService;
   }

   public void setEventService(OseeEventService eventService) {
      this.eventService = eventService;
   }

   public void setLifecycleService(ILifecycleService service) {
      this.lifecycleService = service;
   }

   public void addAccessProvider(ServiceReference<IAccessProvider> reference) {
      if (isReady()) {
         register(reference);
         registered.add(reference);
      } else {
         pendingProviders.add(reference);
      }
   }

   public void removeAccessProvider(ServiceReference<IAccessProvider> reference) {
      if (isReady()) {
         deregister(reference);
         registered.remove(reference);
      } else {
         pendingProviders.remove(reference);
      }
   }

   private IAccessProvider getService(ServiceReference<IAccessProvider> reference) {
      Bundle bundle = reference.getBundle();
      BundleContext bundleContext = bundle.getBundleContext();
      return bundleContext.getService(reference);
   }

   private void register(ServiceReference<IAccessProvider> reference) {
      try {
         IAccessProvider accessProvider = getService(reference);
         lifecycleService.addHandler(AccessProviderVisitor.TYPE, accessProvider);
      } catch (OseeCoreException ex) {
         OseeLog.log(AccessControlHelper.class, Level.SEVERE, ex);
      }
   }

   private void deregister(ServiceReference<IAccessProvider> reference) {
      try {
         IAccessProvider accessProvider = getService(reference);
         lifecycleService.removeHandler(AccessProviderVisitor.TYPE, accessProvider);
      } catch (OseeCoreException ex) {
         OseeLog.log(AccessControlHelper.class, Level.SEVERE, ex);
      }
   }

   private synchronized void ensureInitialized() {
      if (accessService != null && !pendingProviders.isEmpty()) {
         for (ServiceReference<IAccessProvider> reference : pendingProviders) {
            register(reference);
         }
         pendingProviders.clear();
      }
   }

   public AccessControlService getProxiedObject() {
      ensureInitialized();
      return accessService;
   }

   public void reloadCache() {
      try {
         getProxiedObject().reloadCache();
      } catch (OseeCoreException ex) {
         OseeLog.log(AccessControlServiceProxy.class, Level.WARNING, ex);
      }
   }

   private boolean isReady() {
      return accessService != null && lifecycleService != null;
   }

   public void start() {
      accessService = new AccessControlService(dbService, cachingService, eventService);

      accessEventListener = new AccessEventListener(accessService, new AccessControlCacheHandler());
      if (eventService != null) {
         eventService.addListener(EventQosType.PRIORITY, accessEventListener);
      }
      accessService.start();
   }

   public void stop() {
      if (thread != null) {
         thread.interrupt();
         thread = null;
      }

      for (ServiceReference<IAccessProvider> provider : registered) {
         deregister(provider);
      }
      registered.clear();

      if (accessEventListener != null) {
         if (eventService != null) {
            eventService.removeListener(EventQosType.PRIORITY, accessEventListener);
         }
         accessEventListener = null;
      }

      if (accessService != null) {
         accessService.stop();
         accessService = null;
      }
   }

   private void checkInitialized() throws OseeCoreException {
      Conditions.checkNotNull(getProxiedObject(), "accessService", "Access Service not properly initialized");
   }

   @Override
   public boolean hasPermission(Object object, PermissionEnum permission) throws OseeCoreException {
      checkInitialized();
      return getProxiedObject().hasPermission(object, permission);
   }

   @Override
   public void removePermissions(IOseeBranch branch) throws OseeCoreException {
      checkInitialized();
      getProxiedObject().removePermissions(branch);
   }

   @Override
   public AccessDataQuery getAccessData(IBasicArtifact<?> userArtifact, Collection<?> itemsToCheck) throws OseeCoreException {
      checkInitialized();
      return getProxiedObject().getAccessData(userArtifact, itemsToCheck);
   }
}
