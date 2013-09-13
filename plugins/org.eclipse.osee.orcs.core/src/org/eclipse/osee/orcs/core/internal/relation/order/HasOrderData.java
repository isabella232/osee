/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.internal.relation.order;

import java.util.Map.Entry;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Roberto E. Escobar
 */
public interface HasOrderData extends Iterable<Entry<IRelationTypeSide, OrderData>> {

   void add(IRelationTypeSide typeAndSide, OrderData data) throws OseeCoreException;

   void remove(IRelationTypeSide typeAndSide) throws OseeCoreException;

   void clear();

   boolean isEmpty();

   int size();

}
