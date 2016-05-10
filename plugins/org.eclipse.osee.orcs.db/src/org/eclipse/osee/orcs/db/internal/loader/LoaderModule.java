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
package org.eclipse.osee.orcs.db.internal.loader;

import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsTypes;
import org.eclipse.osee.orcs.core.ds.DataFactory;
import org.eclipse.osee.orcs.core.ds.DataLoaderFactory;
import org.eclipse.osee.orcs.data.ArtifactTypes;
import org.eclipse.osee.orcs.data.AttributeTypes;
import org.eclipse.osee.orcs.db.internal.IdentityManager;
import org.eclipse.osee.orcs.db.internal.OrcsObjectFactory;
import org.eclipse.osee.orcs.db.internal.loader.data.OrcsObjectFactoryImpl;
import org.eclipse.osee.orcs.db.internal.loader.handlers.LoaderSqlHandlerFactoryUtil;
import org.eclipse.osee.orcs.db.internal.loader.processor.DynamicLoadProcessor;
import org.eclipse.osee.orcs.db.internal.sql.SqlHandlerFactory;
import org.eclipse.osee.orcs.db.internal.sql.join.SqlJoinFactory;

/**
 * @author Roberto E. Escobar
 */
public class LoaderModule {

   private final Log logger;
   private final JdbcClient jdbcClient;
   private final IdentityManager idFactory;
   private final DataProxyFactoryProvider proxyProvider;
   private final SqlJoinFactory joinFactory;

   public LoaderModule(Log logger, JdbcClient jdbcClient, IdentityManager idFactory, DataProxyFactoryProvider proxyProvider, SqlJoinFactory joinFactory) {
      super();
      this.logger = logger;
      this.jdbcClient = jdbcClient;
      this.idFactory = idFactory;
      this.proxyProvider = proxyProvider;
      this.joinFactory = joinFactory;
   }

   public ProxyDataFactory createProxyDataFactory(AttributeTypes attributeTypes) {
      return new AttributeDataProxyFactory(proxyProvider, jdbcClient, attributeTypes);
   }

   public OrcsObjectFactory createOrcsObjectFactory(ProxyDataFactory proxyFactory) {
      return new OrcsObjectFactoryImpl(proxyFactory);
   }

   public DataFactory createDataFactory(OrcsObjectFactory factory, ArtifactTypes artifactTypes) {
      return new DataFactoryImpl(idFactory, factory, artifactTypes);
   }

   public DataLoaderFactory createDataLoaderFactory(OrcsObjectFactory objectFactory, DynamicLoadProcessor dynamicLoadProcessor) {
      SqlObjectLoader sqlObjectLoader = createSqlObjectLoader(objectFactory, dynamicLoadProcessor);
      return createDataLoaderFactory(sqlObjectLoader);
   }

   public DynamicLoadProcessor createDynamicLoadProcessor(OrcsTypes orcsTypes, ProxyDataFactory proxyFactory) {
      return new DynamicLoadProcessor(logger, orcsTypes, proxyFactory);
   }

   public DataLoaderFactory createDataLoaderFactory(SqlObjectLoader sqlObjectLoader) {
      return new DataLoaderFactoryImpl(logger, jdbcClient, sqlObjectLoader, joinFactory);
   }

   protected SqlObjectLoader createSqlObjectLoader(OrcsObjectFactory objectFactory, DynamicLoadProcessor dynamicLoadProcessor) {
      SqlHandlerFactory handlerFactory = LoaderSqlHandlerFactoryUtil.createHandlerFactory(logger, idFactory);
      return new SqlObjectLoader(logger, jdbcClient, joinFactory, handlerFactory, objectFactory, dynamicLoadProcessor);
   }

}
