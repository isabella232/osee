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
package org.eclipse.osee.framework.skynet.core.artifact.search;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.rest.client.QueryBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author John Misinco
 */
public class RelationCriteriaTest {

   @Test
   public void testAddRelationTypeSideToQueryBuilder() throws OseeCoreException {
      RelationCriteria criteria = new RelationCriteria(CoreRelationTypes.Allocation__Component);
      QueryBuilder builder = mock(QueryBuilder.class);
      criteria.addToQueryBuilder(builder);
      verify(builder).andExists(CoreRelationTypes.Allocation__Component);

      reset(builder);
      criteria = new RelationCriteria(4, CoreRelationTypes.Allocation__Component, RelationSide.SIDE_A);
      criteria.addToQueryBuilder(builder);
      ArgumentCaptor<IRelationTypeSide> rtsCaptor = ArgumentCaptor.forClass(IRelationTypeSide.class);
      verify(builder).andRelatedToLocalIds(rtsCaptor.capture(), eq(4));
      Assert.assertEquals(CoreRelationTypes.Allocation__Component.getGuid(), rtsCaptor.getValue().getGuid());
      Assert.assertEquals(RelationSide.SIDE_A, rtsCaptor.getValue().getSide());
   }

   @Test
   public void testAddRelationTypeToQueryBuilder() throws OseeCoreException {
      RelationCriteria criteria = new RelationCriteria((IRelationType) CoreRelationTypes.Allocation__Component);
      QueryBuilder builder = mock(QueryBuilder.class);
      criteria.addToQueryBuilder(builder);
      verify(builder).andExists((IRelationType) CoreRelationTypes.Allocation__Component);

      reset(builder);
      criteria = new RelationCriteria(4, CoreRelationTypes.Allocation__Component, RelationSide.SIDE_A);
      criteria.addToQueryBuilder(builder);
      ArgumentCaptor<IRelationTypeSide> rtsCaptor = ArgumentCaptor.forClass(IRelationTypeSide.class);
      verify(builder).andRelatedToLocalIds(rtsCaptor.capture(), eq(4));
      Assert.assertEquals(CoreRelationTypes.Allocation__Component.getGuid(), rtsCaptor.getValue().getGuid());
      Assert.assertEquals(RelationSide.SIDE_A, rtsCaptor.getValue().getSide());
   }
}
