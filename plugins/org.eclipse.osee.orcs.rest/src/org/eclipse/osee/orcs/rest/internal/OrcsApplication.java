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
package org.eclipse.osee.orcs.rest.internal;

import java.util.HashSet;
import java.util.Set;
import javax.script.ScriptEngine;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.eclipse.osee.activity.api.ActivityLog;
import org.eclipse.osee.framework.resource.management.IResourceManager;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.rest.admin.LinkUpdateResource;
import org.eclipse.osee.orcs.rest.internal.writer.OrcsWriterEndpointImpl;

/**
 * Get application.wadl at this context to get rest documentation
 *
 * @author Roberto E. Escobar
 */
@ApplicationPath("orcs")
public class OrcsApplication extends Application {

   private final Set<Object> resources = new HashSet<>();
   private final Set<Class<?>> classes = new HashSet<>();
   private static OrcsApi orcsApi;

   private IResourceManager resourceManager;
   private ActivityLog activityLog;

   public void setOrcsApi(OrcsApi orcsApi) {
      OrcsApplication.orcsApi = orcsApi;
   }

   public void setActivityLog(ActivityLog activityLog) {
      this.activityLog = activityLog;
   }

   public void setResourceManager(IResourceManager resourceManager) {
      this.resourceManager = resourceManager;
   }

   public static OrcsApi getOrcsApi() {
      return orcsApi;
   }

   public void start() {
      ScriptEngine engine = orcsApi.getScriptEngine();
      resources.add(new OrcsScriptResource(engine));

      classes.add(BranchesResource.class);
      resources.add(new IdeClientEndpointImpl());

      resources.add(new BranchEndpointImpl(orcsApi, resourceManager));
      resources.add(new OrcsWriterEndpointImpl(orcsApi));
      resources.add(new TransactionEndpointImpl(orcsApi));
      resources.add(new TypesEndpointImpl(orcsApi));

      resources.add(new IndexerEndpointImpl(orcsApi));
      resources.add(new ResourcesEndpointImpl(resourceManager));
      resources.add(new DatastoreEndpointImpl(orcsApi, activityLog));

      resources.add(new LinkUpdateResource(orcsApi));
   }

   public void stop() {
      resources.clear();
      classes.clear();
   }

   @Override
   public Set<Class<?>> getClasses() {
      return classes;
   }

   @Override
   public Set<Object> getSingletons() {
      return resources;
   }

}
