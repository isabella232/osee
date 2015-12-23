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
package org.eclipse.osee.orcs.db.internal.search;

import static org.eclipse.osee.orcs.db.internal.search.handlers.SqlHandlerFactoryUtil.createArtifactSqlHandlerFactory;
import static org.eclipse.osee.orcs.db.internal.search.handlers.SqlHandlerFactoryUtil.createBranchSqlHandlerFactory;
import static org.eclipse.osee.orcs.db.internal.search.handlers.SqlHandlerFactoryUtil.createObjectSqlHandlerFactory;
import static org.eclipse.osee.orcs.db.internal.search.handlers.SqlHandlerFactoryUtil.createTxSqlHandlerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.osee.executor.admin.ExecutorAdmin;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.resource.management.IResourceManager;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.BranchData;
import org.eclipse.osee.orcs.core.ds.DataLoaderFactory;
import org.eclipse.osee.orcs.core.ds.LoadDataHandler;
import org.eclipse.osee.orcs.core.ds.LoadDataHandlerDecorator;
import org.eclipse.osee.orcs.core.ds.QueryEngineIndexer;
import org.eclipse.osee.orcs.core.ds.TxOrcsData;
import org.eclipse.osee.orcs.data.AttributeTypes;
import org.eclipse.osee.orcs.db.internal.IdentityLocator;
import org.eclipse.osee.orcs.db.internal.SqlProvider;
import org.eclipse.osee.orcs.db.internal.search.QuerySqlContext.ObjectQueryType;
import org.eclipse.osee.orcs.db.internal.search.engines.AbstractSimpleQueryCallableFactory;
import org.eclipse.osee.orcs.db.internal.search.engines.ArtifactQuerySqlContextFactoryImpl;
import org.eclipse.osee.orcs.db.internal.search.engines.ObjectQueryCallableFactory;
import org.eclipse.osee.orcs.db.internal.search.engines.ObjectQuerySqlContextFactoryImpl;
import org.eclipse.osee.orcs.db.internal.search.engines.QueryFilterFactoryImpl;
import org.eclipse.osee.orcs.db.internal.search.engines.QuerySqlContextFactoryImpl;
import org.eclipse.osee.orcs.db.internal.search.indexer.IndexedResourceLoader;
import org.eclipse.osee.orcs.db.internal.search.indexer.IndexerCallableFactory;
import org.eclipse.osee.orcs.db.internal.search.indexer.IndexerCallableFactoryImpl;
import org.eclipse.osee.orcs.db.internal.search.indexer.IndexingTaskConsumer;
import org.eclipse.osee.orcs.db.internal.search.indexer.IndexingTaskConsumerImpl;
import org.eclipse.osee.orcs.db.internal.search.indexer.QueryEngineIndexerImpl;
import org.eclipse.osee.orcs.db.internal.search.indexer.data.GammaQueueIndexerDataSourceLoader;
import org.eclipse.osee.orcs.db.internal.search.language.EnglishLanguage;
import org.eclipse.osee.orcs.db.internal.search.tagger.StreamMatcher;
import org.eclipse.osee.orcs.db.internal.search.tagger.TagEncoder;
import org.eclipse.osee.orcs.db.internal.search.tagger.TagProcessor;
import org.eclipse.osee.orcs.db.internal.search.tagger.Tagger;
import org.eclipse.osee.orcs.db.internal.search.tagger.TaggingEngine;
import org.eclipse.osee.orcs.db.internal.search.tagger.TextStreamTagger;
import org.eclipse.osee.orcs.db.internal.search.tagger.XmlTagger;
import org.eclipse.osee.orcs.db.internal.search.util.AttributeDataMatcher;
import org.eclipse.osee.orcs.db.internal.search.util.MatcherFactory;
import org.eclipse.osee.orcs.db.internal.sql.SqlHandlerFactory;
import org.eclipse.osee.orcs.db.internal.sql.TableEnum;
import org.eclipse.osee.orcs.db.internal.sql.join.SqlJoinFactory;

/**
 * @author Roberto E. Escobar
 */
public final class Engines {

   private Engines() {
      //
   }

   public static ObjectQueryCallableFactory newArtifactQueryEngine(Log logger, SqlJoinFactory joinFactory, IdentityLocator idService, SqlProvider sqlProvider, TaggingEngine taggingEngine, ExecutorAdmin executorAdmin, DataLoaderFactory objectLoader, AttributeTypes attrTypes) {
      SqlHandlerFactory handlerFactory =
         createArtifactSqlHandlerFactory(logger, idService, taggingEngine.getTagProcessor());
      QuerySqlContextFactory sqlContextFactory =
         new ArtifactQuerySqlContextFactoryImpl(logger, joinFactory, sqlProvider, handlerFactory);
      AttributeDataMatcher matcher = new AttributeDataMatcher(logger, taggingEngine, attrTypes);
      QueryFilterFactoryImpl filterFactory = new QueryFilterFactoryImpl(logger, executorAdmin, matcher);
      return new ObjectQueryCallableFactory(logger, objectLoader, sqlContextFactory, filterFactory);
   }

   public static QueryCallableFactory newBranchQueryEngine(Log logger, SqlJoinFactory joinFactory, IdentityLocator idService, SqlProvider sqlProvider, DataLoaderFactory objectLoader) {
      QuerySqlContextFactory sqlContextFactory =
         newBranchSqlContextFactory(logger, joinFactory, idService, sqlProvider);
      return new AbstractSimpleQueryCallableFactory(logger, objectLoader, sqlContextFactory) {
         @Override
         protected LoadDataHandler createCountingHandler(final AtomicInteger counter, LoadDataHandler handler) {
            return new LoadDataHandlerDecorator(handler) {
               @Override
               public void onData(BranchData data) throws OseeCoreException {
                  counter.getAndIncrement();
                  super.onData(data);
               }
            };
         }
      };
   }

   public static QueryCallableFactory newTxQueryEngine(Log logger, SqlJoinFactory joinFactory, IdentityLocator idService, SqlProvider sqlProvider, DataLoaderFactory objectLoader) {
      QuerySqlContextFactory sqlContextFactory = newTxSqlContextFactory(logger, joinFactory, idService, sqlProvider);
      return new AbstractSimpleQueryCallableFactory(logger, objectLoader, sqlContextFactory) {
         @Override
         protected LoadDataHandler createCountingHandler(final AtomicInteger counter, LoadDataHandler handler) {
            return new LoadDataHandlerDecorator(handler) {
               @Override
               public void onData(TxOrcsData data) throws OseeCoreException {
                  counter.getAndIncrement();
                  super.onData(data);
               }
            };
         }
      };
   }

   public static QueryCallableFactory newQueryEngine(Log logger, SqlJoinFactory joinFactory, //
   IdentityLocator idService, SqlProvider sqlProvider, TaggingEngine taggingEngine, //
   ExecutorAdmin executorAdmin, DataLoaderFactory objectLoader, AttributeTypes attrTypes) {

      SqlHandlerFactory handlerFactory =
         createObjectSqlHandlerFactory(logger, idService, taggingEngine.getTagProcessor());
      QuerySqlContextFactory sqlContextFactory =
         new ObjectQuerySqlContextFactoryImpl(logger, joinFactory, sqlProvider, handlerFactory);
      AttributeDataMatcher matcher = new AttributeDataMatcher(logger, taggingEngine, attrTypes);
      QueryFilterFactoryImpl filterFactory = new QueryFilterFactoryImpl(logger, executorAdmin, matcher);
      return new ObjectQueryCallableFactory(logger, objectLoader, sqlContextFactory, filterFactory);
   }

   public static TaggingEngine newTaggingEngine(Log logger) {
      TagProcessor tagProcessor = new TagProcessor(new EnglishLanguage(logger), new TagEncoder());
      Map<String, Tagger> taggers = new HashMap<>();

      StreamMatcher matcher = MatcherFactory.createMatcher();
      taggers.put("DefaultAttributeTaggerProvider", new TextStreamTagger(tagProcessor, matcher));
      taggers.put("XmlAttributeTaggerProvider", new XmlTagger(tagProcessor, matcher));

      return new TaggingEngine(taggers, tagProcessor);
   }

   public static QuerySqlContextFactory newSqlContextFactory(Log logger, SqlJoinFactory joinFactory, SqlProvider sqlProvider, TableEnum table, String idColumn, SqlHandlerFactory handlerFactory, ObjectQueryType type) {
      return new QuerySqlContextFactoryImpl(logger, joinFactory, sqlProvider, handlerFactory, table, idColumn, type);
   }

   public static QuerySqlContextFactory newBranchSqlContextFactory(Log logger, SqlJoinFactory joinFactory, IdentityLocator idService, SqlProvider sqlProvider) {
      SqlHandlerFactory handlerFactory = createBranchSqlHandlerFactory(logger, idService);
      return newSqlContextFactory(logger, joinFactory, sqlProvider, TableEnum.BRANCH_TABLE, "branch_id", handlerFactory,
         ObjectQueryType.BRANCH);
   }

   public static QuerySqlContextFactory newTxSqlContextFactory(Log logger, SqlJoinFactory joinFactory, IdentityLocator idService, SqlProvider sqlProvider) {
      SqlHandlerFactory handlerFactory = createTxSqlHandlerFactory(logger, idService);
      return newSqlContextFactory(logger, joinFactory, sqlProvider, TableEnum.TX_DETAILS_TABLE, "transaction_id",
         handlerFactory, ObjectQueryType.TX);
   }

   public static QueryEngineIndexer newIndexingEngine(Log logger, JdbcClient jdbcClient, SqlJoinFactory sqlJoinFactory, TaggingEngine taggingEngine, ExecutorAdmin executorAdmin, IResourceManager resourceManager) {
      IndexedResourceLoader resourceLoader = new GammaQueueIndexerDataSourceLoader(logger, jdbcClient, resourceManager);
      IndexerCallableFactory callableFactory =
         new IndexerCallableFactoryImpl(logger, jdbcClient, taggingEngine, resourceLoader);
      IndexingTaskConsumer indexConsumer = new IndexingTaskConsumerImpl(executorAdmin, callableFactory);
      return new QueryEngineIndexerImpl(logger, jdbcClient, sqlJoinFactory, indexConsumer);
   }

}
