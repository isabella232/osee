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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.rest.internal.search.artifact.predicate.ExistenceTypePredicateHandler;
import org.eclipse.osee.orcs.rest.model.search.artifact.Predicate;
import org.eclipse.osee.orcs.rest.model.search.artifact.SearchMethod;
import org.eclipse.osee.orcs.search.QueryBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author John R. Misinco
 */
public class ExistenceTypePredicateHandlerTest {

   @Mock
   private QueryBuilder builder;

   @Captor
   private ArgumentCaptor<IRelationType> relationTypeCaptor;
   @Captor
   private ArgumentCaptor<Collection<IAttributeType>> attrTypeSideCaptor;

   @Before
   public void initialize() {
      MockitoAnnotations.initMocks(this);
   }

   @Test
   public void testHandleRelationTypeSideA() throws OseeCoreException {
      ExistenceTypePredicateHandler handler = new ExistenceTypePredicateHandler();
      List<String> typeParameters = Collections.singletonList("relType");
      //for relation type sides, first char must be A or B denoting side, followed by relation type uuid
      String relationValue = "12345";
      List<String> values = Collections.singletonList(relationValue);
      Predicate testPredicate = new Predicate(SearchMethod.EXISTS_TYPE, typeParameters, values);
      handler.handle(builder, testPredicate);
      verify(builder).andExists(relationTypeCaptor.capture());
      Assert.assertEquals(1, relationTypeCaptor.getAllValues().size());
      Assert.assertTrue(12345L == relationTypeCaptor.getValue().getGuid());
   }

   @Test
   public void testHandleRelationTypeSideB() throws OseeCoreException {
      ExistenceTypePredicateHandler handler = new ExistenceTypePredicateHandler();
      List<String> typeParameters = Collections.singletonList("relType");
      //no flags for exists type
      String relationValue = "12345";
      List<String> values = Collections.singletonList(relationValue);
      Predicate testPredicate = new Predicate(SearchMethod.EXISTS_TYPE, typeParameters, values);
      handler.handle(builder, testPredicate);

      verify(builder).andExists(relationTypeCaptor.capture());
      Assert.assertEquals(1, relationTypeCaptor.getAllValues().size());
      Assert.assertTrue(12345L == relationTypeCaptor.getValue().getGuid());
   }

   @Test
   public void testHandleRelationTypeSideMultiples() throws OseeCoreException {
      ExistenceTypePredicateHandler handler = new ExistenceTypePredicateHandler();
      List<String> typeParameters = Collections.singletonList("relType");
      //test multiples
      String relationValue1 = "12345";
      String relationValue2 = "34567";
      List<String> values = Arrays.asList(relationValue1, relationValue2);
      Predicate testPredicate = new Predicate(SearchMethod.EXISTS_TYPE, typeParameters, values);

      handler.handle(builder, testPredicate);
      verify(builder, times(2)).andExists(relationTypeCaptor.capture());

      Assert.assertEquals(2, relationTypeCaptor.getAllValues().size());
      IRelationType type = relationTypeCaptor.getAllValues().get(0);
      Assert.assertTrue(12345L == type.getGuid());

      type = relationTypeCaptor.getAllValues().get(1);
      Assert.assertTrue(34567L == type.getGuid());
   }

   @Test
   public void testHandleAttrTypeSingle() throws OseeCoreException {
      ExistenceTypePredicateHandler handler = new ExistenceTypePredicateHandler();
      List<String> typeParameters = Collections.singletonList("attrType");
      //for relation type sides, first char must be A or B denoting side, followed by relation type uuid
      String attrUuid = "12345";
      List<String> values = Collections.singletonList(attrUuid);
      Predicate testPredicate = new Predicate(SearchMethod.EXISTS_TYPE, typeParameters, values);
      handler.handle(builder, testPredicate);
      verify(builder).andExists(attrTypeSideCaptor.capture());
      Assert.assertEquals(1, attrTypeSideCaptor.getAllValues().size());
      List<IAttributeType> attrTypes = new ArrayList<>(attrTypeSideCaptor.getValue());
      Assert.assertTrue(12345L == attrTypes.get(0).getGuid());
   }

   @Test
   public void testHandleAttrTypeMultiple() throws OseeCoreException {
      ExistenceTypePredicateHandler handler = new ExistenceTypePredicateHandler();
      List<String> typeParameters = Collections.singletonList("attrType");
      String attrType1 = "12345";
      String attrType2 = "34567";
      List<String> values = Arrays.asList(attrType1, attrType2);
      Predicate testPredicate =
         new Predicate(SearchMethod.EXISTS_TYPE, typeParameters, values, QueryOption.TOKEN_DELIMITER__ANY);
      handler.handle(builder, testPredicate);

      verify(builder).andExists(attrTypeSideCaptor.capture());
      Assert.assertEquals(1, attrTypeSideCaptor.getAllValues().size());
      Iterator<IAttributeType> iterator = attrTypeSideCaptor.getValue().iterator();
      Assert.assertEquals(Long.valueOf(12345L), iterator.next().getGuid());
      Assert.assertEquals(Long.valueOf(34567L), iterator.next().getGuid());
   }

   @Test
   public void testHandleBadValues() throws OseeCoreException {
      ExistenceTypePredicateHandler handler = new ExistenceTypePredicateHandler();
      List<String> typeParameters = Collections.singletonList("attrType");
      String value = "12A4G";
      List<String> values = Collections.singletonList(value);
      Predicate testPredicate =
         new Predicate(SearchMethod.EXISTS_TYPE, typeParameters, values, QueryOption.TOKEN_DELIMITER__ANY);
      handler.handle(builder, testPredicate);
      verify(builder, never()).andExists(anyCollectionOf(IAttributeType.class));

      value = "12A4G";
      typeParameters = Collections.singletonList("relType");
      values = Collections.singletonList(value);
      testPredicate = new Predicate(SearchMethod.EXISTS_TYPE, typeParameters, values, QueryOption.TOKEN_DELIMITER__ANY);
      handler.handle(builder, testPredicate);
      verify(builder, never()).andExists(any(IRelationTypeSide.class));
   }

   @Test(expected = OseeCoreException.class)
   public void testBadValuesThrowException() throws OseeCoreException {
      ExistenceTypePredicateHandler handler = new ExistenceTypePredicateHandler();
      Predicate testPredicate =
         new Predicate(SearchMethod.ATTRIBUTE_TYPE, Collections.singletonList("relType"),
            Collections.singletonList("A12A4G"), QueryOption.TOKEN_DELIMITER__ANY);
      handler.handle(builder, testPredicate);
   }
}
