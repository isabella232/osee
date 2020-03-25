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
package org.eclipse.osee.orcs.core.internal.relation;

import org.eclipse.osee.framework.core.OrcsTokenService;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.internal.relation.impl.RelationManagerImpl;
import org.eclipse.osee.orcs.core.internal.relation.impl.RelationResolverImpl;
import org.eclipse.osee.orcs.core.internal.relation.order.OrderManagerFactory;
import org.eclipse.osee.orcs.core.internal.relation.order.OrderParser;
import org.eclipse.osee.orcs.core.internal.relation.sorter.SorterProvider;
import org.eclipse.osee.orcs.core.internal.search.QueryModule.QueryModuleProvider;

/**
 * @author Roberto E. Escobar
 */
public final class RelationManagerFactory {

   private RelationManagerFactory() {
      // Static Factory
   }

   public static RelationManager createRelationManager(Log logger, OrcsTokenService tokenService, RelationFactory relationFactory, RelationNodeLoader loader, QueryModuleProvider provider) {
      OrderParser orderParser = new OrderParser(tokenService);
      SorterProvider sorterProvider = new SorterProvider();
      OrderManagerFactory orderManagerFactory = new OrderManagerFactory(orderParser, sorterProvider);

      RelationResolver resolver = new RelationResolverImpl(loader);
      RelationTypeValidity validity = new RelationTypeValidity(tokenService);

      return new RelationManagerImpl(logger, validity, resolver, relationFactory, orderManagerFactory, provider);
   }
}
