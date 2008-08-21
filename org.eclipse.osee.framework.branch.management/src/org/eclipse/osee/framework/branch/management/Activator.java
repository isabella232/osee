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
package org.eclipse.osee.framework.branch.management;

import org.eclipse.osee.framework.branch.management.exchange.BranchExport;
import org.eclipse.osee.framework.branch.management.exchange.BranchImport;
import org.eclipse.osee.framework.branch.management.impl.BranchCreation;
import org.eclipse.osee.framework.resource.management.IResourceLocatorManager;
import org.eclipse.osee.framework.resource.management.IResourceManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

   private static Activator instance;
   private ServiceRegistration serviceRegistration;
   private ServiceRegistration exportServiceRegistration;
   private ServiceRegistration importServiceRegistration;
   private ServiceTracker resourceManagementTracker;
   private ServiceTracker resourceLocatorManagerTracker;

   /*
    * (non-Javadoc)
    * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
    */
   public void start(BundleContext context) throws Exception {
      Activator.instance = this;
      serviceRegistration = context.registerService(IBranchCreation.class.getName(), new BranchCreation(), null);

      exportServiceRegistration = context.registerService(IBranchExport.class.getName(), new BranchExport(), null);
      importServiceRegistration = context.registerService(IBranchImport.class.getName(), new BranchImport(), null);

      resourceLocatorManagerTracker = new ServiceTracker(context, IResourceLocatorManager.class.getName(), null);
      resourceLocatorManagerTracker.open();

      resourceManagementTracker = new ServiceTracker(context, IResourceManager.class.getName(), null);
      resourceManagementTracker.open();
   }

   /*
    * (non-Javadoc)
    * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
    */
   public void stop(BundleContext context) throws Exception {
      exportServiceRegistration.unregister();
      exportServiceRegistration = null;

      importServiceRegistration.unregister();
      importServiceRegistration = null;

      serviceRegistration.unregister();
      serviceRegistration = null;

      resourceManagementTracker.close();
      resourceManagementTracker = null;

      resourceLocatorManagerTracker.close();
      resourceLocatorManagerTracker = null;

      Activator.instance = null;
   }

   public IResourceManager getResourceManager() {
      return (IResourceManager) resourceManagementTracker.getService();
   }

   public IResourceLocatorManager getResourceLocatorManager() {
      return (IResourceLocatorManager) resourceLocatorManagerTracker.getService();
   }

   public static Activator getInstance() {
      return instance;
   }
}
