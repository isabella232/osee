/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.ds.criteria;

import org.eclipse.osee.framework.core.data.RelationTypeSide;
import org.eclipse.osee.orcs.core.ds.RelationTypeCriteria;

/**
 * @author John Misinco
 */
public final class CriteriaRelationTypeSideNotExists extends RelationTypeCriteria<RelationTypeSide> {

   public CriteriaRelationTypeSideNotExists(RelationTypeSide relationTypeSide) {
      super(relationTypeSide);
   }
}