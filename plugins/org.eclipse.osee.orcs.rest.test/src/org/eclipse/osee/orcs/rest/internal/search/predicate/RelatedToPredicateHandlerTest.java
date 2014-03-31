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
package org.eclipse.osee.orcs.rest.internal.search.predicate;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.enums.TokenDelimiterMatch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.orcs.rest.internal.search.artifact.predicate.RelatedToPredicateHandler;
import org.eclipse.osee.orcs.rest.model.search.artifact.Predicate;
import org.eclipse.osee.orcs.rest.model.search.artifact.SearchFlag;
import org.eclipse.osee.orcs.rest.model.search.artifact.SearchMethod;
import org.eclipse.osee.orcs.rest.model.search.artifact.SearchOp;
import org.eclipse.osee.orcs.search.QueryBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author John Misinco
 */
public class RelatedToPredicateHandlerTest {

   @Mock
   private QueryBuilder builder;
   @Captor
   private ArgumentCaptor<Collection<Integer>> idsCaptor;
   @Captor
   private ArgumentCaptor<IRelationTypeSide> rtsCaptor;

   @Before
   public void initialize() {
      MockitoAnnotations.initMocks(this);
   }

   @Test
   public void testRelatedToLocalIds() throws OseeCoreException {
      RelatedToPredicateHandler handler = new RelatedToPredicateHandler();
      List<SearchFlag> emptySearchFlags = Collections.emptyList();
      Predicate testPredicate =
         new Predicate(SearchMethod.RELATED_TO, Arrays.asList("A1", "B2"), SearchOp.EQUALS, emptySearchFlags,
            TokenDelimiterMatch.ANY, Arrays.asList("4", "5"));
      handler.handle(builder, testPredicate);
      verify(builder, times(2)).andRelatedToLocalIds(rtsCaptor.capture(), idsCaptor.capture());
      List<IRelationTypeSide> rts = rtsCaptor.getAllValues();
      Assert.assertEquals(2, rts.size());
      verifyRelationTypeSide(rts.get(0), "A1");
      verifyRelationTypeSide(rts.get(1), "B2");

      List<Collection<Integer>> ids = idsCaptor.getAllValues();
      Assert.assertEquals(2, ids.size());
      ids.containsAll(Arrays.asList(4, 5));
   }

   @Test(expected = UnsupportedOperationException.class)
   public void testUnsupportedOperation() throws OseeCoreException {
      RelatedToPredicateHandler handler = new RelatedToPredicateHandler();
      List<SearchFlag> emptySearchFlags = Collections.emptyList();
      Predicate testPredicate =
         new Predicate(SearchMethod.RELATED_TO, Arrays.asList("A1", "B2"), SearchOp.EQUALS, emptySearchFlags,
            TokenDelimiterMatch.ANY, Arrays.asList(GUID.create()));
      handler.handle(builder, testPredicate);
   }

   private void verifyRelationTypeSide(IRelationTypeSide rts, String input) {
      if (input.startsWith("A")) {
         Assert.assertEquals(RelationSide.SIDE_A, rts.getSide());
      } else {
         Assert.assertEquals(RelationSide.SIDE_B, rts.getSide());
      }
      Assert.assertTrue(rts.getGuid().equals(Long.parseLong(input.substring(1))));
   }
}
