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
package org.eclipse.osee.orcs.core.internal.graph.impl;

import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.internal.graph.GraphData;
import org.eclipse.osee.orcs.core.internal.graph.GraphFactory;

/**
 * @author Roberto E. Escobar
 */
public class GraphFactoryImpl implements GraphFactory {

   @Override
   public GraphData createGraph(OrcsSession session, Long branch, TransactionId transactionId) {
      return new GraphDataImpl(session, branch, transactionId);
   }

}
