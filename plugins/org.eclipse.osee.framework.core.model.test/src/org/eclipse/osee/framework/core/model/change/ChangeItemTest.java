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
package org.eclipse.osee.framework.core.model.change;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.model.mocks.ChangeTestUtility;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test Case for {@link ChangeItem}
 *
 * @author Roberto E. Escobar
 */
@RunWith(Parameterized.class)
public class ChangeItemTest {
   private final ChangeItem item;
   private final ChangeVersion base;
   private final ChangeVersion first;
   private final ChangeVersion current;
   private final ChangeVersion destination;
   private final ChangeVersion net;
   private final Long itemId;

   public ChangeItemTest(Long itemId, ChangeVersion base, ChangeVersion first, ChangeVersion current, ChangeVersion destination, ChangeVersion net) {
      this.itemId = itemId;
      this.base = base;
      this.first = first;
      this.current = current;
      this.destination = destination;
      this.net = net;
      item = ChangeTestUtility.createItem(itemId, base, first, current, destination, net);
   }

   @Test
   public void testGetItemId() {
      Long actual = item.getItemId().getId();
      assertEquals(itemId, actual);
   }

   @Test
   public void testGetBaselineVersion() {
      ChangeTestUtility.checkChange(base, item.getBaselineVersion());
   }

   @Test
   public void testGetFirstNonCurrentVersion() {
      ChangeTestUtility.checkChange(first, item.getFirstNonCurrentChange());
   }

   @Test
   public void testGetCurrentVersion() {
      ChangeTestUtility.checkChange(current, item.getCurrentVersion());
   }

   @Test
   public void testGetDestinationVersion() {
      ChangeTestUtility.checkChange(destination, item.getDestinationVersion());
   }

   @Test
   public void testGetNetVersion() {
      ChangeTestUtility.checkChange(net, item.getNetChange());
   }

   @Test
   public void testGetItemTypeId() {
      Long expected = item.getItemId().getId() * 10;
      Long actual = item.getItemTypeId().getId();
      assertEquals(expected, actual);
   }

   @Test
   public void testGetArtId() {
      Long expected = item.getItemId().getId() * 100;
      Long actual = item.getArtId().getId();
      assertEquals(expected, actual);
   }

   @Parameters
   public static List<Object[]> getData() {
      List<Object[]> data = new ArrayList<>();

      ChangeVersion base = ChangeTestUtility.createChange(1111L, ModificationType.NEW);
      ChangeVersion first = ChangeTestUtility.createChange(2222L, ModificationType.MODIFIED);
      ChangeVersion current = ChangeTestUtility.createChange(3333L, ModificationType.INTRODUCED);
      ChangeVersion destination = ChangeTestUtility.createChange(4444L, ModificationType.MERGED);
      ChangeVersion net = ChangeTestUtility.createChange(5555L, ModificationType.DELETED);

      data.add(new Object[] {200L, base, first, current, destination, net});
      return data;
   }
}
