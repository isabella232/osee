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
package org.eclipse.osee.orcs.core.internal.graph;

import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.internal.artifact.ArtifactFactory;
import org.eclipse.osee.orcs.core.internal.attribute.AttributeFactory;
import org.eclipse.osee.orcs.core.internal.graph.impl.GraphBuilderImpl;
import org.eclipse.osee.orcs.core.internal.relation.RelationFactory;

/**
 * @author Roberto E. Escobar
 */
public class GraphBuilderFactory {

   private final Log logger;
   private final ArtifactFactory artifactFactory;
   private final AttributeFactory attributeFactory;
   private final RelationFactory relationFactory;

   public GraphBuilderFactory(Log logger, ArtifactFactory artifactFactory, AttributeFactory attributeFactory, RelationFactory relationFactory) {
      super();
      this.logger = logger;
      this.artifactFactory = artifactFactory;
      this.attributeFactory = attributeFactory;
      this.relationFactory = relationFactory;
   }

   public GraphBuilder createBuilderForGraph(GraphData graph) {
      return createGraphBuilder(GraphUtil.asProvider(graph));
   }

   public GraphBuilder createGraphBuilder(GraphProvider graphProvider) {
      return new GraphBuilderImpl(logger, artifactFactory, attributeFactory, relationFactory, graphProvider);
   }

}
