/*********************************************************************
 * Copyright (c) 2013 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.orcs.core.internal.util;

import static org.eclipse.osee.framework.jdk.core.util.Conditions.checkExpressionFailOnTrue;
import static org.eclipse.osee.framework.jdk.core.util.Conditions.checkNotNull;
import org.eclipse.osee.orcs.core.internal.artifact.Artifact;
import org.eclipse.osee.orcs.core.internal.graph.GraphData;

/**
 * @author Roberto E. Escobar
 */
public final class OrcsConditions {

   private OrcsConditions() {
      // Utility class
   }

   public static void checkOnGraph(GraphData graph, Artifact... nodes) {
      checkNotNull(graph, "graph");
      for (Artifact node : nodes) {
         checkNotNull(node, "node");
         GraphData graph2 = node.getGraph();
         checkExpressionFailOnTrue(!graph.equals(graph2), "Error - Node[%s] is on graph[%s] but should be on graph[%s]",
            node, graph2, graph);
      }
   }

   public static void checkBranch(GraphData graph, Artifact... nodes) {
      checkNotNull(graph, "graph");
      for (Artifact node : nodes) {
         checkNotNull(node, "node");
         GraphData graph2 = node.getGraph();
         checkExpressionFailOnTrue(!graph.isOnSameBranch(graph2),
            "Error - Node[%s] is on branch[%d] but should be on branch[%d]", node, graph2.getBranch(),
            graph.getBranch());
      }
   }

   public static void checkBranch(Artifact node1, Artifact node2) {
      boolean areEqual = node1.getBranch().equals(node2.getBranch());
      checkExpressionFailOnTrue(!areEqual, "Cross branch linking is not yet supported.");
   }

   public static void checkRelateSelf(Artifact node1, Artifact node2) {
      checkExpressionFailOnTrue(node1.equals(node2), "Not valid to relate [%s] to itself", node1);
   }
}
