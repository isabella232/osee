/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.jaxrs.server.internal;

import java.util.Map;
import javax.ws.rs.core.Application;
import org.eclipse.osee.jaxrs.server.internal.applications.JaxRsApplicationRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

/**
 * @author Roberto E. Escobar
 */
public final class JaxRsApplicationManager extends JaxRsDynamicServiceManager<Application> {

   private JaxRsConfiguration config;

   @Override
   public void update(Map<String, Object> props) {
      super.update(props);
      config = JaxRsConfiguration.fromProperties(props).build();
      JaxRsApplicationRegistry registry = getRegistry();
      if (registry != null) {
         registry.configure(config);
      }
   }

   @Override
   public void register(JaxRsApplicationRegistry registry, ServiceReference<Application> reference) {
      String componentName = JaxRsUtils.getComponentName(reference);
      Bundle bundle = reference.getBundle();
      Application application = bundle.getBundleContext().getService(reference);
      String contextName = JaxRsUtils.getApplicationPath(componentName, application);
      registry.register(componentName, contextName, bundle, application);
   }

   @Override
   public void deregister(JaxRsApplicationRegistry registry, ServiceReference<Application> reference) {
      String componentName = JaxRsUtils.getComponentName(reference);
      registry.deregister(componentName);
   }

}
