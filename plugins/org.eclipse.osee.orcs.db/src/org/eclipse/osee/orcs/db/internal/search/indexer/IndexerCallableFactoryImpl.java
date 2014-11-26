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

import java.util.concurrent.Callable;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.data.AttributeTypes;
import org.eclipse.osee.orcs.db.internal.search.indexer.callable.consumer.IndexingTaskDatabaseTxCallable;
import org.eclipse.osee.orcs.db.internal.search.tagger.TaggingEngine;
import org.eclipse.osee.orcs.search.IndexerCollector;

/**
 * @author Roberto E. Escobar
 */
public class IndexerCallableFactoryImpl implements IndexerCallableFactory {

   private final Log logger;
   private final JdbcClient jdbcClient;
   private final TaggingEngine taggingEngine;
   private final IndexedResourceLoader loader;

   public IndexerCallableFactoryImpl(Log logger, JdbcClient jdbcClient, TaggingEngine taggingEngine, IndexedResourceLoader loader) {
      super();
      this.logger = logger;
      this.jdbcClient = jdbcClient;
      this.taggingEngine = taggingEngine;
      this.loader = loader;
   }

   @Override
   public Callable<?> createIndexerTaskCallable(OrcsSession session, AttributeTypes types, IndexerCollector collector, int queueId) {
      return new IndexingTaskDatabaseTxCallable(logger, session, jdbcClient, loader, taggingEngine, collector, queueId,
         IndexerConstants.INDEXER_CACHE_ALL_ITEMS, IndexerConstants.INDEXER_CACHE_LIMIT, types);
   }

}
