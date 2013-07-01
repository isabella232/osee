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
package org.eclipse.osee.orcs.db.internal.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.services.IdentityService;
import org.eclipse.osee.framework.jdk.core.util.PriorityComparator;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.Criteria;
import org.eclipse.osee.orcs.core.ds.CriteriaSet;
import org.eclipse.osee.orcs.core.ds.DataPostProcessorFactory;
import org.eclipse.osee.orcs.core.ds.Options;
import org.eclipse.osee.orcs.db.internal.search.tagger.HasTagProcessor;
import org.eclipse.osee.orcs.db.internal.search.tagger.TagProcessor;

/**
 * @author Roberto E. Escobar
 */
public class SqlHandlerFactoryImpl implements SqlHandlerFactory {

   private static final PriorityComparator comparator = new PriorityComparator();

   private final Map<Class<? extends Criteria<?>>, Class<? extends SqlHandler<?, ?>>> handleMap;
   private final Map<Class<? extends SqlHandler<?, ?>>, DataPostProcessorFactory<?>> factoryMap;

   private final Log logger;
   private final IdentityService idService;
   private final TagProcessor tagProcessor;

   public SqlHandlerFactoryImpl(Log logger, IdentityService idService, Map<Class<? extends Criteria<?>>, Class<? extends SqlHandler<?, ?>>> handleMap) {
      this(logger, idService, null, handleMap, null);
   }

   public SqlHandlerFactoryImpl(Log logger, IdentityService idService, TagProcessor tagProcessor, Map<Class<? extends Criteria<?>>, Class<? extends SqlHandler<?, ?>>> handleMap, Map<Class<? extends SqlHandler<?, ?>>, DataPostProcessorFactory<?>> factoryMap) {
      this.logger = logger;
      this.idService = idService;
      this.handleMap = handleMap;
      this.factoryMap = factoryMap;
      this.tagProcessor = tagProcessor;
   }

   @Override
   public <O extends Options> List<SqlHandler<?, O>> createHandlers(CriteriaSet criteriaSet) throws OseeCoreException {
      List<SqlHandler<?, O>> handlers = new ArrayList<SqlHandler<?, O>>();
      for (Criteria<O> criteria : criteriaSet) {
         SqlHandler<?, O> handler = createHandler(criteria);
         handlers.add(handler);
      }
      Collections.sort(handlers, comparator);
      return handlers;
   }

   @Override
   @SuppressWarnings({"unchecked", "rawtypes"})
   public <O extends Options> SqlHandler<?, O> createHandler(Criteria<O> criteria) throws OseeCoreException {
      Class<? extends Criteria> key = criteria.getClass();
      Class<? extends SqlHandler> item = handleMap.get(key);
      return createHandler(criteria, item);
   }

   @SuppressWarnings("unchecked")
   private <C extends Criteria<?>, O extends Options, H extends SqlHandler<C, O>> SqlHandler<C, O> createHandler(C criteria, Class<H> item) throws OseeCoreException {
      SqlHandler<C, O> handler = null;
      try {
         handler = item.newInstance();
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      }

      handler.setData(criteria);
      handler.setIdentityService(idService);
      handler.setLogger(logger);

      if (factoryMap != null) {
         DataPostProcessorFactory<C> factory = (DataPostProcessorFactory<C>) factoryMap.get(item);
         if (factory != null) {
            HasDataPostProcessorFactory<C> hasFactory = (HasDataPostProcessorFactory<C>) handler;
            hasFactory.setDataPostProcessorFactory(factory);
         }
      }

      if (tagProcessor != null) {
         if (handler instanceof HasTagProcessor) {
            ((HasTagProcessor) handler).setTagProcessor(tagProcessor);
         }
      }
      return handler;
   }
}
