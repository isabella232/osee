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
package org.eclipse.osee.orcs.db.internal.transaction;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import org.eclipse.osee.framework.core.OrcsTokenService;
import org.eclipse.osee.framework.core.executor.HasCancellation;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.ds.AttributeData;
import org.eclipse.osee.orcs.core.ds.OrcsVisitorAdapter;
import org.eclipse.osee.orcs.core.ds.QueryEngineIndexer;
import org.eclipse.osee.orcs.core.ds.TransactionData;

/**
 * @author Roberto E. Escobar
 */
public class TransactionIndexer implements TransactionProcessor {

   private final Log logger;
   private final QueryEngineIndexer indexer;
   private final OrcsTokenService tokenService;

   public TransactionIndexer(Log logger, QueryEngineIndexer indexer, OrcsTokenService tokenService) {
      super();
      this.logger = logger;
      this.indexer = indexer;
      this.tokenService = tokenService;
   }

   @Override
   public void process(final HasCancellation cancellation, OrcsSession session, TransactionData txData) {
      try {
         final Set<Long> datas = new LinkedHashSet<>();
         txData.getChangeSet().accept(new OrcsVisitorAdapter() {
            @Override
            public <T> void visit(AttributeData<T> data) {
               if (tokenService.getAttributeType(data.getType().getId()).isTaggable()) {
                  datas.add(data.getVersion().getGammaId().getId());
               }
            }
         });

         List<Future<?>> futures = indexer.indexResources(session, tokenService, datas).call();
         for (Future<?> future : futures) {
            if (cancellation != null && cancellation.isCancelled()) {
               future.cancel(true);
            } else {
               // Wait for execution to complete
               future.get();
            }
         }
      } catch (Exception ex) {
         logger.error(ex, "Error indexing transaction [%s]", txData);
      }
   }
}