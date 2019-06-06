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

import org.eclipse.osee.framework.core.model.internal.fields.AssociatedArtifactField;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test Case For {@link AssociatedArtifactField}
 *
 * @author Roberto E. Escobar
 */
public class AssociatedArtifactFieldTest {

   private static Long art1;
   private static Long art2;

   //   private static IBasicArtifact<?> art3;

   @BeforeClass
   public static void prepareTest() {
      art1 = 123432L;
      art2 = 345421L;
      //      art3 = MockDataFactory.createArtifact(3);
   }

   @Test
   public void test() {
      AssociatedArtifactField field = new AssociatedArtifactField(art1);
      Assert.assertEquals(false, field.isDirty());

      field.set(art2);
      Assert.assertEquals(true, field.isDirty());
      field.clearDirty();

   }
}
