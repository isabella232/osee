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
package org.eclipse.osee.orcs.core.internal;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import org.eclipse.osee.executor.admin.ExecutorAdmin;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.LazyObject;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.cache.TransactionCache;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.ApplicationContext;
import org.eclipse.osee.orcs.OrcsAdmin;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.OrcsBranch;
import org.eclipse.osee.orcs.OrcsPerformance;
import org.eclipse.osee.orcs.OrcsTypes;
import org.eclipse.osee.orcs.core.SystemPreferences;
import org.eclipse.osee.orcs.core.ds.OrcsDataStore;
import org.eclipse.osee.orcs.core.ds.TempCachingService;
import org.eclipse.osee.orcs.core.internal.artifact.ArtifactBuilderFactoryImpl;
import org.eclipse.osee.orcs.core.internal.artifact.ArtifactFactory;
import org.eclipse.osee.orcs.core.internal.attribute.AttributeClassRegistry;
import org.eclipse.osee.orcs.core.internal.attribute.AttributeClassResolver;
import org.eclipse.osee.orcs.core.internal.attribute.AttributeFactory;
import org.eclipse.osee.orcs.core.internal.indexer.IndexerModule;
import org.eclipse.osee.orcs.core.internal.proxy.ArtifactProxyFactory;
import org.eclipse.osee.orcs.core.internal.relation.RelationFactory;
import org.eclipse.osee.orcs.core.internal.relation.RelationGraphImpl;
import org.eclipse.osee.orcs.core.internal.search.QueryModule;
import org.eclipse.osee.orcs.core.internal.session.SessionContextImpl;
import org.eclipse.osee.orcs.core.internal.transaction.TransactionFactoryImpl;
import org.eclipse.osee.orcs.core.internal.transaction.handler.TxDataHandlerFactoryImpl;
import org.eclipse.osee.orcs.core.internal.types.BranchHierarchyProvider;
import org.eclipse.osee.orcs.core.internal.types.OrcsTypesModule;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.GraphReadable;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.eclipse.osee.orcs.search.QueryIndexer;
import org.eclipse.osee.orcs.transaction.TransactionFactory;
import com.google.common.collect.Sets;

/**
 * @author Roberto E. Escobar
 */
public class OrcsApiImpl implements OrcsApi {

   private Log logger;
   private OrcsDataStore dataStore;
   private AttributeClassRegistry registry;
   private TempCachingService cacheService;

   private ExecutorAdmin executorAdmin;
   private SystemPreferences preferences;

   private ArtifactProxyFactory proxyFactory;
   private ArtifactLoaderFactory loaderFactory;
   private QueryModule queryModule;
   private IndexerModule indexerModule;
   private TxDataHandlerFactoryImpl txUpdateFactory;
   private OrcsTypesModule typesModule;
   private SessionContext systemSession;

   public void setLogger(Log logger) {
      this.logger = logger;
   }

   public void setOrcsDataStore(OrcsDataStore dataStore) {
      this.dataStore = dataStore;
   }

   public void setAttributeClassRegistry(AttributeClassRegistry registry) {
      this.registry = registry;
   }

   public void setCacheService(TempCachingService cacheService) {
      this.cacheService = cacheService;
   }

   public void setExecutorAdmin(ExecutorAdmin executorAdmin) {
      this.executorAdmin = executorAdmin;
   }

   public void setSystemPreferences(SystemPreferences preferences) {
      this.preferences = preferences;
   }

   public void start() {
      systemSession = createSession();

      BranchHierarchyProvider hierarchyProvider = new BranchHierarchyProvider() {

         @Override
         public Iterable<? extends IOseeBranch> getParentHierarchy(IOseeBranch branch) throws OseeCoreException {
            Set<IOseeBranch> branches = Sets.newLinkedHashSet();
            BranchCache branchCache = cacheService.getBranchCache();

            Branch branchPtr = branchCache.get(branch);
            while (branchPtr != null) {
               if (!branches.add(branchPtr)) {
                  logger.error("Cycle detected with branch: [%s]", branchPtr);
                  return Collections.emptyList();
               }
               branchPtr = branchPtr.getParentBranch();
            }

            return branches;
         }
      };

      typesModule = new OrcsTypesModule(logger, dataStore, hierarchyProvider);
      typesModule.start(getSystemSession());

      OrcsTypes orcsTypes = typesModule.createOrcsTypes(getSystemSession());

      RelationFactory relationFactory = new RelationFactory(orcsTypes.getRelationTypes());

      AttributeClassResolver resolver = new AttributeClassResolver(registry, orcsTypes.getAttributeTypes());
      AttributeFactory attributeFactory =
         new AttributeFactory(resolver, dataStore.getDataFactory(), orcsTypes.getAttributeTypes());

      ArtifactFactory artifactFactory =
         new ArtifactFactory(dataStore.getDataFactory(), attributeFactory, relationFactory,
            cacheService.getBranchCache(), orcsTypes.getArtifactTypes());

      proxyFactory = new ArtifactProxyFactory(artifactFactory);

      txUpdateFactory = new TxDataHandlerFactoryImpl(dataStore.getDataFactory());

      ArtifactBuilderFactory builderFactory =
         new ArtifactBuilderFactoryImpl(logger, proxyFactory, artifactFactory, attributeFactory);

      loaderFactory = new ArtifactLoaderFactoryImpl(dataStore.getDataLoaderFactory(), builderFactory);

      queryModule =
         new QueryModule(logger, dataStore.getQueryEngine(), loaderFactory, dataStore.getDataLoaderFactory(),
            orcsTypes.getArtifactTypes(), orcsTypes.getAttributeTypes());

      indexerModule = new IndexerModule(logger, preferences, executorAdmin, dataStore.getQueryEngineIndexer());
      indexerModule.start();
   }

   public void stop() {
      if (indexerModule != null) {
         indexerModule.stop();
      }
      queryModule = null;
      loaderFactory = null;
      txUpdateFactory = null;
      proxyFactory = null;
   }

   @Override
   public QueryFactory getQueryFactory(ApplicationContext context) {
      SessionContext sessionContext = getSessionContext(context);
      return queryModule.createQueryFactory(sessionContext);
   }

   @Override
   public TransactionCache getTxsCache() {
      return cacheService.getTransactionCache();
   }

   @Override
   public BranchCache getBranchCache() {
      return cacheService.getBranchCache();
   }

   @Override
   public GraphReadable getGraph(ApplicationContext context) {
      SessionContext sessionContext = getSessionContext(context);
      return new RelationGraphImpl(sessionContext, loaderFactory, getOrcsTypes(context).getRelationTypes());
   }

   @Override
   public OrcsBranch getBranchOps(final ApplicationContext context) {
      SessionContext sessionContext = getSessionContext(context);
      LazyObject<ArtifactReadable> systemUser = new LazyObject<ArtifactReadable>() {

         @Override
         protected final FutureTask<ArtifactReadable> createLoaderTask() {
            Callable<ArtifactReadable> callable = new Callable<ArtifactReadable>() {
               @Override
               public ArtifactReadable call() throws Exception {
                  return getQueryFactory(context).fromBranch(CoreBranches.COMMON).andIds(SystemUser.OseeSystem).getResults().getExactlyOne();

               }
            };
            return new FutureTask<ArtifactReadable>(callable);
         }
      };
      return new OrcsBranchImpl(logger, sessionContext, dataStore.getBranchDataStore(), cacheService.getBranchCache(),
         cacheService.getTransactionCache(), systemUser, getOrcsTypes(context));
   }

   @Override
   public TransactionFactory getTransactionFactory(ApplicationContext context) {
      SessionContext sessionContext = getSessionContext(context);
      return new TransactionFactoryImpl(logger, sessionContext, dataStore.getBranchDataStore(), proxyFactory,
         txUpdateFactory);
   }

   @Override
   public OrcsAdmin getAdminOps(ApplicationContext context) {
      SessionContext sessionContext = getSessionContext(context);
      return new OrcsAdminImpl(logger, sessionContext, dataStore.getDataStoreAdmin());
   }

   @Override
   public OrcsPerformance getOrcsPerformance(ApplicationContext context) {
      SessionContext sessionContext = getSessionContext(context);
      return new OrcsPerformanceImpl(logger, sessionContext, queryModule, indexerModule);
   }

   @Override
   public QueryIndexer getQueryIndexer(ApplicationContext context) {
      SessionContext sessionContext = getSessionContext(context);
      return indexerModule.createQueryIndexer(sessionContext);
   }

   private SessionContext getSystemSession() {
      return systemSession;
   }

   private SessionContext getSessionContext(ApplicationContext context) {
      // TODO get sessions from a session context cache - improve this
      String sessionId = null;
      if (context != null) {
         sessionId = context.getSessionId();
      }
      if (!Strings.isValid(sessionId)) {
         sessionId = GUID.create();
      }
      return new SessionContextImpl(sessionId);
   }

   private SessionContext createSession() {
      String sessionId = GUID.create();
      return new SessionContextImpl(sessionId);
   }

   @Override
   public OrcsTypes getOrcsTypes(ApplicationContext context) {
      SessionContext sessionContext = getSessionContext(context);
      return typesModule.createOrcsTypes(sessionContext);
   }

}
