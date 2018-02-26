/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.orcs.db.internal.search.tagger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.orcs.db.internal.search.SearchAsserts;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test Case for {@link TagEncoder}
 *
 * @author Roberto E. Escobar
 */
@RunWith(Parameterized.class)
public class TagEncoderTest {

   private final List<Long> expected;
   private final String toEncode;
   private final TagEncoder encoder;

   public TagEncoderTest(String toEncode, List<Long> expected) {
      this.toEncode = toEncode;
      this.expected = expected;
      this.encoder = new TagEncoder();
   }

   @Test
   public void testTagEncoder() {
      List<Long> actualTags = new ArrayList<>();
      encoder.encode(toEncode, actualTags::add);
      Assert.assertEquals(expected, actualTags);
   }

   @Parameters
   public static Collection<Object[]> data() {
      List<Object[]> data = new ArrayList<>();
      data.add(new Object[] {"hello", SearchAsserts.asTags("hello", 1520625)});
      data.add(new Object[] {
         "what happens when we have a long string",
         SearchAsserts.asTags("what happens when we have a long string", 2080358399, -545259521, 290692031)});
      return data;
   }
}