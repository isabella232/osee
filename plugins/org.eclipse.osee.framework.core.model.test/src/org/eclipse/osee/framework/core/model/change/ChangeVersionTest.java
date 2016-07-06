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

import org.eclipse.osee.framework.core.data.ApplicabilityId;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.model.mocks.ChangeTestUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test Case for {@link ChangeVersion}
 *
 * @author Roberto E. Escobar
 */
public class ChangeVersionTest {

   @Test
   public void testConstruction() {
      ChangeVersion actual = new ChangeVersion();
      ChangeVersion expected = new ChangeVersion();
      ChangeTestUtility.checkChange(expected, actual);

      actual = new ChangeVersion(45L, ModificationType.NEW, ApplicabilityId.valueOf(1L));
      expected.setValue(null);
      expected.setGammaId(45L);
      expected.setModType(ModificationType.NEW);
      expected.setApplicabilityId(ApplicabilityId.valueOf(1L));
      ChangeTestUtility.checkChange(expected, actual);

      actual = new ChangeVersion("hello", 47L, ModificationType.MERGED, ApplicabilityId.valueOf(1L));
      expected.setValue("hello");
      expected.setGammaId(47L);
      expected.setModType(ModificationType.MERGED);
      expected.setApplicabilityId(ApplicabilityId.valueOf(1L));
      ChangeTestUtility.checkChange(expected, actual);
   }

   @Test
   public void testCopy() {
      ChangeVersion expected = new ChangeVersion("hello", 47L, ModificationType.MERGED, ApplicabilityId.valueOf(1L));
      ChangeVersion actual = new ChangeVersion();
      actual.copy(expected);
      ChangeTestUtility.checkChange(expected, actual);

      expected = new ChangeVersion(null, 47L, ModificationType.MERGED, ApplicabilityId.valueOf(1L));
      actual.copy(expected);
      ChangeTestUtility.checkChange(expected, actual);
   }

   @Test
   public void testExists() {
      ChangeVersion actual = new ChangeVersion();
      Assert.assertFalse(actual.isValid());

      actual.setGammaId(45L);
      Assert.assertFalse(actual.isValid());

      actual.setModType(ModificationType.MODIFIED);
      Assert.assertTrue(actual.isValid());
   }

   @Test
   public void testEquals() {
      ChangeVersion actual1 = new ChangeVersion("hello", 47L, ModificationType.MERGED, ApplicabilityId.valueOf(1L));
      ChangeVersion actual2 = new ChangeVersion("hello", 47L, ModificationType.MERGED, ApplicabilityId.valueOf(1L));
      ChangeVersion expected = new ChangeVersion();

      Assert.assertEquals(actual2, actual1);
      Assert.assertTrue(actual2.hashCode() == actual1.hashCode());

      Assert.assertTrue(!expected.equals(actual1));
      Assert.assertTrue(!expected.equals(actual2));

      Assert.assertTrue(expected.hashCode() != actual1.hashCode());
      Assert.assertTrue(expected.hashCode() != actual2.hashCode());

      expected.copy(actual1);
      Assert.assertEquals(expected, actual1);
      Assert.assertEquals(expected, actual2);

   }

   @Test
   public void testToString() {
      ChangeVersion actual1 = new ChangeVersion("hello", 47L, ModificationType.MERGED, ApplicabilityId.valueOf(1L));
      Assert.assertEquals("[47,MERGED,1]", actual1.toString());

      ChangeVersion expected = new ChangeVersion();
      Assert.assertEquals("[null,null,null]", expected.toString());

   }
}
