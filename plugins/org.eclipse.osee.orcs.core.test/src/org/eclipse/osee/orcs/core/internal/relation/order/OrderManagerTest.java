/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.internal.relation.order;

import static org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes.LEXICOGRAPHICAL_ASC;
import static org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes.UNORDERED;
import static org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes.USER_DEFINED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.eclipse.osee.framework.core.data.RelationSorter;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.jdk.core.type.Identifiable;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.core.internal.relation.sorter.SorterProvider;
import org.eclipse.osee.orcs.data.RelationTypes;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test Case for {@link OrderManager}
 * 
 * @author Roberto E. Escobar
 */
public class OrderManagerTest {

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   // @formatter:off
   @Mock private OrderAccessor accessor;
   @Mock private RelationTypes relationTypeCache;

   @Mock private IRelationTypeSide typeSide1;
   @Mock private IRelationTypeSide typeSide2;
   @Mock private IRelationTypeSide typeSide3;
   
   @Mock private OrderData orderData1;
   @Mock private OrderData orderData2;
   @Mock private OrderData orderData3;
   
   @Mock private Identifiable<String> mock1;
   @Mock private Identifiable<String> mock2;
   @Mock private Identifiable<String> mock3;
   // @formatter:on

   private OrderManager orderManager;
   private List<Identifiable<String>> items;

   @Before
   public void setUp() throws OseeCoreException {
      MockitoAnnotations.initMocks(this);

      orderManager = new OrderManager(new SorterProvider(relationTypeCache), accessor);

      items = new ArrayList<>();
      items.add(mock1);
      items.add(mock2);
      items.add(mock3);

      when(typeSide1.getGuid()).thenReturn(11L);
      when(typeSide2.getGuid()).thenReturn(22L);
      when(typeSide3.getGuid()).thenReturn(33L);

      when(typeSide1.getSide()).thenReturn(RelationSide.SIDE_A);
      when(typeSide2.getSide()).thenReturn(RelationSide.SIDE_B);
      when(typeSide3.getSide()).thenReturn(RelationSide.SIDE_A);

      when(relationTypeCache.getDefaultOrderTypeGuid(typeSide1)).thenReturn(USER_DEFINED.getGuid());
      when(relationTypeCache.getDefaultOrderTypeGuid(typeSide2)).thenReturn(UNORDERED.getGuid());
      when(relationTypeCache.getDefaultOrderTypeGuid(typeSide3)).thenReturn(LEXICOGRAPHICAL_ASC.getGuid());
   }

   @Test
   public void testLoad() throws OseeCoreException {
      orderManager.load();
      verify(accessor).load(orderManager);
   }

   @Test
   public void testStore() throws OseeCoreException {
      orderManager.store();
      verify(accessor).store(orderManager, OrderChange.Forced);
   }

   @Test
   public void testAddNull1() throws OseeCoreException {
      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("type and side key cannot be null");
      orderManager.remove(null);
      orderManager.add(null, orderData1);
   }

   @Test
   public void testAddNull2() throws OseeCoreException {
      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("orderData cannot be null");
      orderManager.add(typeSide1, null);
   }

   @Test
   public void testRemoveNull() throws OseeCoreException {
      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("type and side key cannot be null");
      orderManager.remove(null);
   }

   @Test
   public void testAddRemove() throws OseeCoreException {
      orderManager.add(typeSide1, orderData1);
      orderManager.add(typeSide2, orderData2);
      orderManager.add(typeSide3, orderData3);

      assertEquals(3, orderManager.size());
      assertEquals(false, orderManager.isEmpty());

      // Add again
      orderManager.add(typeSide3, orderData3);
      assertEquals(3, orderManager.size());
      assertEquals(false, orderManager.isEmpty());

      Collection<IRelationTypeSide> items = orderManager.getExistingTypes();
      assertTrue(items.contains(typeSide1));
      assertTrue(items.contains(typeSide2));
      assertTrue(items.contains(typeSide3));

      orderManager.remove(typeSide2);
      assertEquals(2, orderManager.size());
      assertEquals(false, orderManager.isEmpty());

      // Add same one again
      orderManager.add(typeSide1, orderData1);
      assertEquals(2, orderManager.size());
      assertEquals(false, orderManager.isEmpty());

      items = orderManager.getExistingTypes();
      assertTrue(items.contains(typeSide1));
      assertFalse(items.contains(typeSide2));
      assertTrue(items.contains(typeSide3));

      orderManager.clear();
      assertEquals(0, orderManager.size());
      assertEquals(true, orderManager.isEmpty());
      assertEquals(true, orderManager.getExistingTypes().isEmpty());
   }

   @Test
   public void testGetSorterIdNull() throws OseeCoreException {
      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("type and side key cannot be null");

      orderManager.getSorterId(null);
   }

   @Test
   public void testGetSorterId() throws OseeCoreException {
      when(orderData1.getSorterId()).thenReturn(RelationOrderBaseTypes.USER_DEFINED);
      when(orderData2.getSorterId()).thenReturn(RelationOrderBaseTypes.LEXICOGRAPHICAL_ASC);

      orderManager.add(typeSide1, orderData1);
      orderManager.add(typeSide2, orderData2);

      assertEquals(RelationOrderBaseTypes.USER_DEFINED, orderManager.getSorterId(typeSide1));

      assertEquals(RelationOrderBaseTypes.LEXICOGRAPHICAL_ASC, orderManager.getSorterId(typeSide2));

      RelationSorter actual = orderManager.getSorterId(typeSide3);
      assertEquals(RelationOrderBaseTypes.LEXICOGRAPHICAL_ASC, actual);
   }

   @Test
   public void testGetOrderIdsNull() throws OseeCoreException {
      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("type and side key cannot be null");

      orderManager.getOrderIds(null);
   }

   @Test
   public void testGetOrderIds() throws OseeCoreException {
      List<String> relatives1 = Arrays.asList("A", "B", "C");
      List<String> relatives2 = Arrays.asList("1", "2", "3");

      when(orderData1.getOrderIds()).thenReturn(relatives1);
      when(orderData2.getOrderIds()).thenReturn(relatives2);

      orderManager.add(typeSide1, orderData1);
      orderManager.add(typeSide2, orderData2);

      assertEquals(relatives1, orderManager.getOrderIds(typeSide1));
      assertEquals(relatives2, orderManager.getOrderIds(typeSide2));

      // Not Exists
      assertEquals(Collections.emptyList(), orderManager.getOrderIds(typeSide3));
   }

   @Test
   public void testIterator() throws OseeCoreException {
      List<String> relatives1 = Arrays.asList("Z", "A", "X");
      List<String> relatives2 = Arrays.asList("3", "2", "1");
      List<String> relatives3 = Arrays.asList("c", "b", "a");

      when(orderData1.getOrderIds()).thenReturn(relatives1);
      when(orderData2.getOrderIds()).thenReturn(relatives2);
      when(orderData3.getOrderIds()).thenReturn(relatives3);
      when(orderData1.getSorterId()).thenReturn(RelationOrderBaseTypes.USER_DEFINED);
      when(orderData2.getSorterId()).thenReturn(RelationOrderBaseTypes.USER_DEFINED);
      when(orderData3.getSorterId()).thenReturn(RelationOrderBaseTypes.USER_DEFINED);

      IRelationTypeSide typeSide4 = mock(IRelationTypeSide.class);
      IRelationTypeSide typeSide5 = mock(IRelationTypeSide.class);
      IRelationTypeSide typeSide6 = mock(IRelationTypeSide.class);

      when(typeSide4.getGuid()).thenReturn(11L);
      when(typeSide5.getGuid()).thenReturn(11L);
      when(typeSide6.getGuid()).thenReturn(11L);

      when(typeSide4.getSide()).thenReturn(RelationSide.SIDE_B);
      when(typeSide5.getSide()).thenReturn(RelationSide.SIDE_A);
      when(typeSide6.getSide()).thenReturn(RelationSide.SIDE_B);

      orderManager.add(typeSide4, orderData1);
      orderManager.add(typeSide5, orderData2);
      orderManager.add(typeSide6, orderData3);

      Iterator<Entry<IRelationTypeSide, OrderData>> iterator = orderManager.iterator();
      Entry<IRelationTypeSide, OrderData> actual1 = iterator.next();
      Entry<IRelationTypeSide, OrderData> actual2 = iterator.next();
      Entry<IRelationTypeSide, OrderData> actual3 = iterator.next();

      assertEquals(typeSide5, actual1.getKey());
      assertEquals(typeSide4, actual2.getKey());
      assertEquals(typeSide6, actual3.getKey());

      assertEquals(orderData2, actual1.getValue());
      assertEquals(orderData1, actual2.getValue());
      assertEquals(orderData3, actual3.getValue());
   }

   @Test
   public void testSort() throws OseeCoreException {
      List<String> relatives1 = Arrays.asList("2", "1", "3");

      when(orderData1.getOrderIds()).thenReturn(relatives1);
      orderManager.add(typeSide1, orderData1);

      when(mock1.getGuid()).thenReturn("3");
      when(mock2.getGuid()).thenReturn("2");
      when(mock3.getGuid()).thenReturn("1");

      when(mock1.getName()).thenReturn("a");
      when(mock2.getName()).thenReturn("1");
      when(mock3.getName()).thenReturn("c");

      when(orderData1.getSorterId()).thenReturn(RelationOrderBaseTypes.UNORDERED);
      orderManager.sort(typeSide1, items);
      assertOrdered(items, mock1, mock2, mock3);

      when(orderData1.getSorterId()).thenReturn(RelationOrderBaseTypes.USER_DEFINED);
      Collections.shuffle(items);
      orderManager.sort(typeSide1, items);
      assertOrdered(items, mock2, mock3, mock1);

      when(orderData1.getSorterId()).thenReturn(RelationOrderBaseTypes.LEXICOGRAPHICAL_ASC);
      Collections.shuffle(items);
      orderManager.sort(typeSide1, items);
      assertOrdered(items, mock2, mock1, mock3);

      when(orderData1.getSorterId()).thenReturn(RelationOrderBaseTypes.LEXICOGRAPHICAL_DESC);
      Collections.shuffle(items);
      orderManager.sort(typeSide1, items);
      assertOrdered(items, mock3, mock1, mock2);
   }

   @Test
   public void testSetOrder() throws OseeCoreException {
      List<String> relatives1 = Arrays.asList("2", "1", "3");

      when(mock1.getGuid()).thenReturn("2");
      when(mock2.getGuid()).thenReturn("1");
      when(mock3.getGuid()).thenReturn("3");

      when(orderData1.getSorterId()).thenReturn(RelationOrderBaseTypes.USER_DEFINED);
      when(orderData1.getOrderIds()).thenReturn(relatives1);

      orderManager.add(typeSide1, orderData1);

      orderManager.setOrder(typeSide1, RelationOrderBaseTypes.USER_DEFINED, items);
      verify(accessor).store(orderManager, OrderChange.NoChange);

      orderManager.setOrder(typeSide1, RelationOrderBaseTypes.LEXICOGRAPHICAL_DESC,
         Collections.<Identifiable<String>> emptyList());
      verify(accessor).store(orderManager, OrderChange.OrderRequest);
      verify(orderData1).setSorterId(RelationOrderBaseTypes.LEXICOGRAPHICAL_DESC);
      verify(orderData1).setOrderIds(Collections.<String> emptyList());

      Collections.shuffle(items);
      when(orderData1.getSorterId()).thenReturn(RelationOrderBaseTypes.LEXICOGRAPHICAL_DESC);
      orderManager.setOrder(typeSide1, RelationOrderBaseTypes.USER_DEFINED, items);
      verify(accessor).store(orderManager, OrderChange.SetToDefault);
      assertEquals(0, orderManager.size());
   }

   private void assertOrdered(List<Identifiable<String>> items, Identifiable<String>... expecteds) {
      int index = 0;
      assertEquals(expecteds.length, items.size());
      for (Identifiable<String> identifiable : expecteds) {
         assertEquals(identifiable, items.get(index++));
      }
   }

}
