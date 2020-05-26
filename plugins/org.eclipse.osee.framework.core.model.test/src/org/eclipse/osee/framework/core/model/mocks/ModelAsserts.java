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

package org.eclipse.osee.framework.core.model.mocks;

import java.lang.reflect.Method;
import org.eclipse.osee.framework.core.model.AbstractOseeType;
import org.eclipse.osee.framework.core.model.OseeEnumEntry;
import org.eclipse.osee.framework.core.model.access.AccessDetail;
import org.eclipse.osee.framework.core.model.type.OseeEnumType;
import org.junit.Assert;

/**
 * @author Roberto E. Escobar
 */
public class ModelAsserts {

   private ModelAsserts() {
      // Utility Class
   }

   public static void assertTypeSetGet(AbstractOseeType type, String fieldName, String getMethodName, String setMethodName, Object expectedValue, Object newValue) throws Exception {
      Method getMethod = type.getClass().getMethod(getMethodName);
      Method setMethod = type.getClass().getMethod(setMethodName, expectedValue.getClass());

      Assert.assertEquals(expectedValue, getMethod.invoke(type));

      type.clearDirty();
      Assert.assertFalse(type.isDirty());
      Assert.assertFalse(type.areFieldsDirty(fieldName));

      // Check reassign doesn't mark as dirty
      setMethod.invoke(type, expectedValue);
      Assert.assertFalse(type.areFieldsDirty(fieldName));

      setMethod.invoke(type, newValue);
      Assert.assertEquals(newValue, getMethod.invoke(type));

      Assert.assertTrue(type.isDirty());
      Assert.assertTrue(type.areFieldsDirty(fieldName));

      type.clearDirty();
      Assert.assertFalse(type.isDirty());
      Assert.assertFalse(type.areFieldsDirty(fieldName));

      setMethod.invoke(type, expectedValue);
      type.clearDirty();
   }

   public static void checkEnumType(OseeEnumType expected, OseeEnumType actual) {
      OseeEnumEntry[] expectedValues = expected.values();
      OseeEnumEntry[] actualValues = actual.values();
      Assert.assertEquals(expectedValues.length, actualValues.length);

      for (int index = 0; index < expectedValues.length; index++) {
         checkEnumEntry(expectedValues[index], actualValues[index]);
      }
   }

   public static void checkEnumEntry(OseeEnumEntry expected, OseeEnumEntry actual) {
      Assert.assertEquals(expected.getName(), actual.getName());
      Assert.assertEquals(expected.ordinal(), actual.ordinal());
   }

   public static void assertEquals(AccessDetail<?> expected, AccessDetail<?> actual) {
      Assert.assertEquals(expected, actual);
      Assert.assertEquals(expected.getPermission(), actual.getPermission());
      Assert.assertEquals(expected.getAccessObject(), actual.getAccessObject());
      Assert.assertEquals(expected.getReason(), actual.getReason());
   }
}
