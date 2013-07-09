/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.internal.types;

import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsTypes;
import org.eclipse.osee.orcs.core.ds.OrcsTypesDataStore;
import org.eclipse.osee.orcs.core.internal.SessionContext;
import org.eclipse.osee.orcs.core.internal.types.impl.OrcsTypesImpl;
import org.eclipse.osee.orcs.core.internal.types.impl.OrcsTypesIndexProviderImpl;
import org.eclipse.osee.orcs.core.internal.types.impl.OrcsTypesLoaderFactoryImpl;

/**
 * @author Roberto E. Escobar
 */
public class OrcsTypesModule {

   private final Log logger;
   private final OrcsTypesDataStore dataStore;
   private final BranchHierarchyProvider hierarchy;

   private OrcsTypesLoaderFactory factory;
   private OrcsTypesIndexProvider indexer;

   public OrcsTypesModule(Log logger, OrcsTypesDataStore dataStore, BranchHierarchyProvider hierarchy) {
      this.logger = logger;
      this.dataStore = dataStore;
      this.hierarchy = hierarchy;
   }

   public void start(SessionContext session) {
      factory = createFactory();
      indexer = createIndexer(factory.createTypesLoader(session, dataStore));
   }

   public void stop() {
      factory = null;
      indexer = null;
   }

   protected OrcsTypesLoaderFactory createFactory() {
      return new OrcsTypesLoaderFactoryImpl(logger, hierarchy);
   }

   protected OrcsTypesIndexProvider createIndexer(OrcsTypesLoader loader) {
      return new OrcsTypesIndexProviderImpl(loader);
   }

   public OrcsTypes createOrcsTypes(SessionContext session) {
      return new OrcsTypesImpl(logger, session, dataStore, factory, indexer);
   }

}