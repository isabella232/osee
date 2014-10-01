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

import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.data.HasBranch;
import org.eclipse.osee.orcs.data.HasLocalId;
import org.eclipse.osee.orcs.data.HasTransaction;

/**
 * @author Roberto E. Escobar
 */
public interface GraphData extends HasBranch, HasTransaction {

   @Override
   IOseeBranch getBranch();

   <T extends GraphNode> T getNode(HasLocalId<Integer> data);

   <T extends GraphNode> T getNode(Integer id);

   void addNode(GraphNode node) throws OseeCoreException;

   <T extends GraphNode> T removeNode(HasLocalId<Integer> node);

   <T extends GraphNode> T removeNode(Integer id);

   <T extends GraphAdjacencies> T getAdjacencies(HasLocalId<Integer> node);

   <T extends GraphAdjacencies> T getAdjacencies(Integer id);

   void addAdjacencies(HasLocalId<Integer> node, GraphAdjacencies adjacencies);

   void addAdjacencies(Integer id, GraphAdjacencies adjacencies);

   <T extends GraphAdjacencies> T removeAdjacencies(HasLocalId<Integer> node);

   <T extends GraphAdjacencies> T removeAdjacencies(Integer id);

   OrcsSession getSession();

}
