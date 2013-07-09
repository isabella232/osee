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
package org.eclipse.osee.orcs.core.internal.search.callable;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.enums.LoadLevel;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.QueryContext;
import org.eclipse.osee.orcs.core.ds.QueryData;
import org.eclipse.osee.orcs.core.ds.QueryEngine;
import org.eclipse.osee.orcs.core.ds.QueryPostProcessor;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaAttributeKeywords;
import org.eclipse.osee.orcs.core.internal.ArtifactLoader;
import org.eclipse.osee.orcs.core.internal.ArtifactLoaderFactory;
import org.eclipse.osee.orcs.core.internal.SessionContext;
import org.eclipse.osee.orcs.core.internal.search.QueryCollector;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.AttributeReadable;
import org.eclipse.osee.orcs.data.AttributeTypes;
import org.eclipse.osee.orcs.search.Match;

/**
 * @author Roberto E. Escobar
 */
public class SearchCountCallable extends AbstractArtifactSearchCallable<Integer> {

   private QueryContext queryContext;

   public SearchCountCallable(Log logger, QueryEngine queryEngine, QueryCollector collector, ArtifactLoaderFactory objectLoader, SessionContext sessionContext, LoadLevel loadLevel, QueryData queryData, AttributeTypes types) {
      super(logger, queryEngine, collector, objectLoader, sessionContext, loadLevel, queryData, types);
   }

   @Override
   protected Integer innerCall() throws Exception {
      int count = -1;

      if (queryData.hasCriteriaType(CriteriaAttributeKeywords.class)) {
         queryContext = queryEngine.create(sessionContext.getSessionId(), queryData);
         checkForCancelled();

         ArtifactLoader loader = objectLoader.fromQueryContext(sessionContext, queryContext);
         loader.setLoadLevel(loadLevel);
         loader.includeDeleted(queryData.getOptions().areDeletedIncluded());
         loader.fromTransaction(queryData.getOptions().getFromTransaction());
         checkForCancelled();

         List<ArtifactReadable> artifacts = loader.load(this);

         checkForCancelled();

         List<ArtifactReadable> results;
         if (!queryContext.getPostProcessors().isEmpty()) {
            results = new ArrayList<ArtifactReadable>();
            for (QueryPostProcessor processor : queryContext.getPostProcessors()) {
               processor.setItemsToProcess(artifacts);
               processor.setAttributeTypes(types);
               checkForCancelled();
               List<Match<ArtifactReadable, AttributeReadable<?>>> matches = processor.call();
               for (Match<ArtifactReadable, AttributeReadable<?>> match : matches) {
                  results.add(match.getItem());
                  checkForCancelled();
               }
            }
         } else {
            results = artifacts;
         }
         count = results.size();
      } else {
         queryContext = queryEngine.createCount(sessionContext.getSessionId(), queryData);
         checkForCancelled();

         count = objectLoader.getCount(this, queryContext);
         checkForCancelled();
      }
      return count;
   }

   @Override
   public void setCancel(boolean isCancelled) {
      super.setCancel(isCancelled);
      if (queryContext != null && !queryContext.getPostProcessors().isEmpty()) {
         for (QueryPostProcessor processor : queryContext.getPostProcessors()) {
            processor.setCancel(true);
         }
      }
   }

   @Override
   protected int getCount(Integer count) {
      return count;
   }
}
