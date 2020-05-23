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

package org.eclipse.osee.framework.core.model.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.eclipse.osee.framework.core.model.internal.fields.BranchAliasesField;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test Case For {@link BranchAliasesField}
 * 
 * @author Roberto E. Escobar
 */
public class BranchAliasesFieldTest {

   @Test
   public void testSetGet() {
      Collection<String> aliases = new ArrayList<>();
      BranchAliasesField field = new BranchAliasesField(aliases);
      Assert.assertEquals(false, field.isDirty());

      FieldTestUtil.assertSetGet(field, Arrays.asList("A", "B", "C"), Arrays.asList("a", "b", "c"), true);
      field.clearDirty();
      Assert.assertEquals(false, field.isDirty());

      // Same but with changed order and case
      FieldTestUtil.assertSetGet(field, Arrays.asList("C", "a", "b"), Arrays.asList("a", "b", "c"), false);
      Assert.assertEquals(false, field.isDirty());

      // Remove two
      FieldTestUtil.assertSetGet(field, Arrays.asList("C"), Arrays.asList("c"), true);
      field.clearDirty();

      // Add
      FieldTestUtil.assertSetGet(field, Arrays.asList("C", "WhaT!21", "AgAin"), Arrays.asList("c", "what!21", "again"),
         true);
      field.clearDirty();

      // Add Empty
      FieldTestUtil.assertSetGet(field, Collections.<String> emptyList(), Collections.<String> emptyList(), true);
      field.clearDirty();
   }
}