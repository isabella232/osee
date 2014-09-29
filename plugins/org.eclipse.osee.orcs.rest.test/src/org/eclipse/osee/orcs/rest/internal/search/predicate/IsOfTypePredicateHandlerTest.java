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

import static org.mockito.Mockito.verify;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.rest.internal.search.artifact.predicate.IsOfTypePredicateHandler;
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
public class IsOfTypePredicateHandlerTest {

   @Mock
   private QueryBuilder builder;

   @Captor
   private ArgumentCaptor<Collection<IArtifactType>> artifactTypesCaptor;

   @Before
   public void initialize() {
      MockitoAnnotations.initMocks(this);
   }

   @Test
   public void testHandleSingle() throws OseeCoreException {
      IsOfTypePredicateHandler handler = new IsOfTypePredicateHandler();
      //no type params, op, or flags for ids - any passed are ignored

      String id1 = "12345";
      List<String> values = Collections.singletonList(id1);
      Predicate testPredicate = new Predicate(SearchMethod.IS_OF_TYPE, null, values);
      handler.handle(builder, testPredicate);
      verify(builder).andIsOfType(artifactTypesCaptor.capture());
      Assert.assertEquals(1, artifactTypesCaptor.getValue().size());
      // :-)
      Assert.assertTrue(artifactTypesCaptor.getValue().iterator().next().getGuid().toString().equals(id1));
   }

   @Test
   public void testHandleMultiple() throws OseeCoreException {
      IsOfTypePredicateHandler handler = new IsOfTypePredicateHandler();
      String id1 = "12345";
      String id2 = "45678";
      List<String> values = Arrays.asList(id1, id2);

      Predicate testPredicate = new Predicate(SearchMethod.IS_OF_TYPE, null, values);
      handler.handle(builder, testPredicate);
      verify(builder).andIsOfType(artifactTypesCaptor.capture());
      Assert.assertEquals(2, artifactTypesCaptor.getValue().size());
      List<IArtifactType> types = new LinkedList<IArtifactType>(artifactTypesCaptor.getValue());
      Assert.assertEquals(id2, types.get(0).getGuid().toString());
      Assert.assertEquals(id1, types.get(1).getGuid().toString());
   }

   @Test(expected = OseeArgumentException.class)
   public void testHandleBadValues() throws OseeCoreException {
      IsOfTypePredicateHandler handler = new IsOfTypePredicateHandler();
      Predicate testPredicate = new Predicate(SearchMethod.IS_OF_TYPE, null, null);
      handler.handle(builder, testPredicate);
   }

   @Test(expected = OseeArgumentException.class)
   public void testBadSearchMethod() throws OseeCoreException {
      IsOfTypePredicateHandler handler = new IsOfTypePredicateHandler();
      String id1 = "12345";
      List<String> values = Collections.singletonList(id1);
      Predicate testPredicate = new Predicate(SearchMethod.ATTRIBUTE_TYPE, null, values);
      handler.handle(builder, testPredicate);
   }
}
