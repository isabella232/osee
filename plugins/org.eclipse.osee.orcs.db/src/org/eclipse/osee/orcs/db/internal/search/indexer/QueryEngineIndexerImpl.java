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
package org.eclipse.osee.orcs.db.internal.search.indexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import org.eclipse.osee.activity.api.ActivityLog;
import org.eclipse.osee.executor.admin.CancellableCallable;
import org.eclipse.osee.executor.admin.ExecutorAdmin;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.resource.management.IResourceManager;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.jdbc.JdbcConnection;
import org.eclipse.osee.jdbc.JdbcConstants;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.ds.IndexerData;
import org.eclipse.osee.orcs.core.ds.QueryEngineIndexer;
import org.eclipse.osee.orcs.data.AttributeTypes;
import org.eclipse.osee.orcs.data.BranchReadable;
import org.eclipse.osee.orcs.db.internal.search.indexer.callable.DeleteTagSetDatabaseTxCallable;
import org.eclipse.osee.orcs.db.internal.search.indexer.callable.IndexerCallable;
import org.eclipse.osee.orcs.db.internal.search.indexer.callable.IndexerDatabaseStatisticsCallable;
import org.eclipse.osee.orcs.db.internal.search.indexer.callable.PurgeAllTagsDatabaseCallable;
import org.eclipse.osee.orcs.db.internal.search.indexer.callable.producer.IndexAllInQueueCallable;
import org.eclipse.osee.orcs.db.internal.search.indexer.callable.producer.IndexBranchesDatabaseCallable;
import org.eclipse.osee.orcs.db.internal.search.indexer.callable.producer.IndexerDatabaseCallable;
import org.eclipse.osee.orcs.db.internal.search.indexer.data.AttributeLoader;
import org.eclipse.osee.orcs.db.internal.search.tagger.TaggingEngine;
import org.eclipse.osee.orcs.db.internal.sql.join.SqlJoinFactory;
import org.eclipse.osee.orcs.search.IndexerCollector;

/**
 * @author Roberto E. Escobar
 */
public class QueryEngineIndexerImpl implements QueryEngineIndexer {

   private final Log logger;
   private final JdbcClient jdbcClient;
   private final SqlJoinFactory joinFactory;
   private final IndexingTaskConsumer consumer;
   private final TaggingEngine taggingEngine;
   private final IResourceManager resourceManager;
   private final IndexerCollectorNotifier systemCollector;
   private final ExecutorAdmin executorAdmin;

   public QueryEngineIndexerImpl(Log logger, JdbcClient jdbcClient, SqlJoinFactory joinFactory, IndexingTaskConsumer indexingConsumer, TaggingEngine taggingEngine, IResourceManager resourceManager, ExecutorAdmin executorAdmin) {
      this.logger = logger;
      this.jdbcClient = jdbcClient;
      this.joinFactory = joinFactory;
      this.consumer = indexingConsumer;
      this.systemCollector = new IndexerCollectorNotifier(logger);
      this.taggingEngine = taggingEngine;
      this.resourceManager = resourceManager;
      this.executorAdmin = executorAdmin;
   }

   @Override
   public CancellableCallable<Integer> deleteIndexByQueryId(OrcsSession session, int queueId) {
      return new DeleteTagSetDatabaseTxCallable(logger, session, jdbcClient, queueId);
   }

   @Override
   public CancellableCallable<Integer> purgeAllIndexes(OrcsSession session) {
      return new PurgeAllTagsDatabaseCallable(logger, session, jdbcClient);
   }

   @Override
   public CancellableCallable<IndexerData> getIndexerData(OrcsSession session) {
      return new IndexerDatabaseStatisticsCallable(logger, session, jdbcClient);
   }

   @Override
   public CancellableCallable<Integer> indexBranches(OrcsSession session, AttributeTypes types, Collection<? extends IAttributeType> typeToTag, Set<BranchReadable> branches, boolean indexOnlyMissing, IndexerCollector... collector) {
      return new IndexBranchesDatabaseCallable(logger, session, jdbcClient, joinFactory, types, consumer,
         merge(collector), typeToTag, branches, indexOnlyMissing);
   }

   @Override
   public CancellableCallable<Integer> indexAllFromQueue(OrcsSession session, AttributeTypes types, IndexerCollector... collector) {
      return new IndexAllInQueueCallable(logger, session, jdbcClient, joinFactory, types, consumer, merge(collector));
   }

   @Override
   public CancellableCallable<List<Future<?>>> indexResources(OrcsSession session, AttributeTypes types, Iterable<Long> datas, IndexerCollector... collector) {
      return new IndexerDatabaseCallable(logger, session, jdbcClient, joinFactory, types, consumer, merge(collector),
         IndexerConstants.INDEXER_CACHE_ALL_ITEMS, IndexerConstants.INDEXER_CACHE_LIMIT, datas);
   }

   private IndexerCollector merge(IndexerCollector... collectors) {
      IndexerCollector toReturn = systemCollector;
      if (collectors != null && collectors.length > 0) {
         IndexerCollectorNotifier notifier = new IndexerCollectorNotifier(logger);
         notifier.addCollector(systemCollector);
         for (IndexerCollector collector : collectors) {
            notifier.addCollector(collector);
         }
      }
      return toReturn;
   }

   @Override
   public void addCollector(IndexerCollector collector) {
      systemCollector.addCollector(collector);
   }

   @Override
   public void removeCollector(IndexerCollector collector) {
      systemCollector.removeCollector(collector);
   }

   @Override
   public void indexAllBranches(AttributeTypes types, Collection<? extends IAttributeType> typesToTag, ActivityLog activityLog) {
      JdbcConnection connection = jdbcClient.getConnection();
      AttributeLoader loader =
         new AttributeLoader(jdbcClient, connection, typesToTag, resourceManager, joinFactory,
            JdbcConstants.JDBC__MAX_FETCH_SIZE);
      try {
         loader.init();

         int callableCount = loader.getOptimalNumberOfThreads();
         ArrayList<Future<Void>> futures = new ArrayList<Future<Void>>(callableCount);

         for (int i = 0; i < callableCount; i++) {
            TagCollectorAndStorage collector = new TagCollectorAndStorage(jdbcClient, connection, 50000, activityLog);
            IndexerCallable indexer =
               new IndexerCallable(taggingEngine, loader, collector, resourceManager, activityLog);
            futures.add(executorAdmin.schedule(IndexerConstants.INDEXING_EXECUTOR_ID, indexer));
         }
         for (Future<Void> future : futures) {
            future.get();
         }
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      } finally {
         loader.cleanup();
         connection.close();
      }
      System.out.println("Finished Tagging!");
   }
}