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
package org.eclipse.osee.orcs.db.internal.search.indexer.callable.consumer;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.jdbc.JdbcConnection;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.ds.IndexedResource;
import org.eclipse.osee.orcs.core.ds.OrcsDataHandler;
import org.eclipse.osee.orcs.data.AttributeTypes;
import org.eclipse.osee.orcs.db.internal.search.indexer.IndexedResourceLoader;
import org.eclipse.osee.orcs.db.internal.search.tagger.TagCollector;
import org.eclipse.osee.orcs.db.internal.search.tagger.Tagger;
import org.eclipse.osee.orcs.db.internal.search.tagger.TaggingEngine;
import org.eclipse.osee.orcs.search.IndexerCollectorAdapter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

/**
 * @author Marc A. Potter
 */

public class IndexingTaskDatabaseTxCallableTest {

   // @formatter:off
   @Mock private Log logger;
   @Mock private OrcsSession session;
   @Mock private JdbcClient jdbcClient;
   @Mock private IndexerCollectorAdapter collector;
   @Mock private AttributeTypes types; 
   @Mock private IndexedResource resource1;
   @Mock private IndexedResource resource2;
   @Mock private TaggingEngine engine;
   @Mock private DatabaseMetaData metaData;
   @Mock private Tagger tagger;
   @Mock private JdbcConnection connection;
   // @formatter:on

   private IndexingTaskDatabaseTxCallable txCallable;

   @Before
   public void setUp() {
      initMocks(this);
      IndexedResourceLoader loader = new IndexedResourceLoader() {
         @Override
         public void loadSource(OrcsDataHandler<IndexedResource> handler, int tagQueueQueryId) {
            handler.onData(resource1);
            handler.onData(resource2);
         }

         @Override
         public void cleanupSource(JdbcConnection connection, int tagQueueQueryId) {
         }
      };
      txCallable =
         new IndexingTaskDatabaseTxCallable(logger, session, jdbcClient, loader, engine, collector, -1, false, 10000,
            types);
   }

   @Test
   public void testTagging() throws Exception {
      ArrayList<IndexedResource> sources = new ArrayList<IndexedResource>();
      sources.add(resource1);
      sources.add(resource2);
      when(resource1.getTypeUuid()).thenReturn(CoreAttributeTypes.Name.getGuid());
      when(resource2.getTypeUuid()).thenReturn(CoreAttributeTypes.QualificationMethod.getGuid());
      when(resource1.getGammaId()).thenReturn(1L);
      when(resource2.getGammaId()).thenReturn(2L);
      when(engine.hasTagger("")).thenReturn(false);
      when(engine.hasTagger("Tag")).thenReturn(true);
      when(engine.getTagger("Tag")).thenReturn(tagger);

      when(metaData.getDatabaseProductName()).thenReturn("h2");
      when(types.getByUuid(CoreAttributeTypes.Name.getGuid())).thenReturn(CoreAttributeTypes.Name);
      when(types.getByUuid(CoreAttributeTypes.QualificationMethod.getGuid())).thenReturn(
         CoreAttributeTypes.QualificationMethod);
      when(types.getTaggerId(CoreAttributeTypes.Name)).thenReturn("Tag");
      when(types.getTaggerId(CoreAttributeTypes.QualificationMethod)).thenReturn(null);

      txCallable.handleTxWork(connection);

      verify(logger, times(1)).error("Field has invalid tagger[%s] provider and cannot be tagged - [Gamma: %s]", null,
         2L);
      verify(tagger, times(1)).tagIt(-1L, Matchers.eq(resource1), Matchers.any(TagCollector.class));
   }

}
