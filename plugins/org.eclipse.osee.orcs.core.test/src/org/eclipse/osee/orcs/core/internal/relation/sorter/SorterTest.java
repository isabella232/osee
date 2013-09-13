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
package org.eclipse.osee.orcs.core.internal.relation.sorter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.data.AbstractIdentity;
import org.eclipse.osee.framework.core.data.IRelationSorterId;
import org.eclipse.osee.framework.core.data.Identifiable;
import org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.orcs.utility.SortOrder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test Case for {@link LexicographicalSorter}, {@link UnorderedSorter}, {@link UserDefinedSorter}, and
 * {@link UserDefinedComparator}
 * 
 * @author Roberto E. Escobar
 */
@RunWith(Parameterized.class)
public class SorterTest {

   private final String message;
   private final Sorter sorter;
   private final IRelationSorterId expectedOrderId;
   private final List<Identifiable> expectedOrder;
   private final List<String> currentItems;
   private final List<Identifiable> itemsToOrder;

   public SorterTest(String message, Sorter sorter, IRelationSorterId expectedOrderId, List<String> currentItems, List<Identifiable> itemsToOrder, List<Identifiable> expectedOrder) {
      this.sorter = sorter;
      this.message = message;
      this.expectedOrderId = expectedOrderId;
      this.currentItems = currentItems;
      this.itemsToOrder = itemsToOrder;
      this.expectedOrder = expectedOrder;
   }

   @Test
   public void testSorterId() {
      Assert.assertNotNull(message, sorter.getId());
      Assert.assertEquals(message, expectedOrderId.getGuid(), sorter.getId().getGuid());
      Assert.assertEquals(message, expectedOrderId.getName(), sorter.getId().getName());
   }

   @Test
   public void testSort() {
      List<Identifiable> actualToOrder = new ArrayList<Identifiable>();
      actualToOrder.addAll(itemsToOrder);
      sorter.sort(actualToOrder, currentItems);

      Assert.assertEquals(message, expectedOrder.size(), actualToOrder.size());
      for (int index = 0; index < expectedOrder.size(); index++) {
         Assert.assertEquals(message + " - index:" + index, expectedOrder.get(index), actualToOrder.get(index));
      }
   }

   @Parameters
   public static Collection<Object[]> data() {
      Collection<Object[]> data = new ArrayList<Object[]>();
      data.add(createUnorderedSortTest("4", "2", "1", "5"));
      data.add(createUnorderedSortTest("$", "a", "!", "2"));
      data.add(createUserDefinedTest("1", "2", "3", "4"));

      data.add(createLexicographicalTest(SortOrder.ASCENDING, "1", "2", "3", "4"));
      data.add(createLexicographicalTest(SortOrder.ASCENDING, "a", "b", "c", "d"));
      data.add(createLexicographicalTest(SortOrder.ASCENDING, "!", "1", "a", "b"));

      data.add(createLexicographicalTest(SortOrder.DESCENDING, "4", "3", "2", "1"));
      data.add(createLexicographicalTest(SortOrder.DESCENDING, "d", "c", "b", "a"));
      data.add(createLexicographicalTest(SortOrder.DESCENDING, "b", "a", "1", "!"));

      return data;
   }

   private static Object[] createUnorderedSortTest(String... names) {
      Identifiable art1 = createItem(names[0]);
      Identifiable art2 = createItem(names[1]);
      Identifiable art3 = createItem(names[2]);
      Identifiable art4 = createItem(names[3]);

      List<Identifiable> artifacts = Arrays.asList(art1, art2, art3, art4);
      return new Object[] {
         "Unordered Test",
         new UnorderedSorter(),
         RelationOrderBaseTypes.UNORDERED,
         null,
         artifacts,
         artifacts};
   }

   private static Object[] createLexicographicalTest(SortOrder mode, String... names) {
      Identifiable art1 = createItem(names[0]);
      Identifiable art2 = createItem(names[1]);
      Identifiable art3 = createItem(names[2]);
      Identifiable art4 = createItem(names[3]);

      IRelationSorterId orderId;
      if (mode.isAscending()) {
         orderId = RelationOrderBaseTypes.LEXICOGRAPHICAL_ASC;
      } else {
         orderId = RelationOrderBaseTypes.LEXICOGRAPHICAL_DESC;
      }

      List<Identifiable> itemsToOrder = Arrays.asList(art3, art1, art4, art2);
      List<Identifiable> expectedOrder = Arrays.asList(art1, art2, art3, art4);
      return new Object[] {
         "Lex Test " + mode.name(),
         new LexicographicalSorter(mode),
         orderId,
         null,
         itemsToOrder,
         expectedOrder};
   }

   private static Object[] createUserDefinedTest(String... names) {
      Identifiable art1 = createItem(names[0]);
      Identifiable art2 = createItem(names[1]);
      Identifiable art3 = createItem(names[2]);
      Identifiable art4 = createItem(names[3]);

      List<Identifiable> itemsToOrder = Arrays.asList(art2, art1, art3, art4);
      List<Identifiable> expectedOrder = Arrays.asList(art1, art2, art3, art4);

      List<String> relatives = new ArrayList<String>();
      for (Identifiable item : Arrays.asList(art1, art2, art3, art4)) {
         relatives.add(item.getGuid());
      }
      return new Object[] {
         "UserDefined",
         new UserDefinedSorter(),
         RelationOrderBaseTypes.USER_DEFINED,
         relatives,
         itemsToOrder,
         expectedOrder};
   }

   private static Identifiable createItem(String name) {
      return new TestItem(GUID.create(), name);
   }

   private static final class TestItem extends AbstractIdentity<String> implements Identifiable {

      private final String guid;
      private final String name;

      public TestItem(String guid, String name) {
         super();
         this.guid = guid;
         this.name = name;
      }

      @Override
      public String getGuid() {
         return guid;
      }

      @Override
      public String getName() {
         return name;
      }

   }
}
