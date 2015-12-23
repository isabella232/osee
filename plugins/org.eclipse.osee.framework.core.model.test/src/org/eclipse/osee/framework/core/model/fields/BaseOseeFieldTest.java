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
package org.eclipse.osee.framework.core.model.fields;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.osee.framework.core.model.OseeField;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Compare;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Roberto E. Escobar
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class BaseOseeFieldTest {
   private final OseeFieldTestData<?> test;

   protected BaseOseeFieldTest(OseeFieldTestData<?> test) {
      this.test = test;
   }

   @Test
   public void testInitialValues() throws OseeCoreException {
      String message = "InitTest";
      checkObjects(message, test.getInitExpectedValue(), test.getField().get());
      Assert.assertEquals(message, test.isInitExpectedDirty(), test.getField().isDirty());
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   @Test
   public void testSettterGetter() throws OseeCoreException {
      int index = 0;
      for (FieldGetSetTestData testData : test.getTestDatas()) {
         String message = String.format("Test Data [%s]", ++index);
         if (testData.isClearBeforeRun()) {
            test.getField().clearDirty();
            Assert.assertFalse(message, test.getField().isDirty());
         }

         if (testData.throwsError()) {
            try {
               test.doSetValue(testData);
               Assert.assertNull(message, "This line should never be executed");
            } catch (Exception ex) {
               Class<?> clazz = testData.getError();
               Assert.assertTrue(message, ex.getClass().isAssignableFrom(clazz));
            }
         } else {
            test.doSetValue(testData);
         }

         checkObjects(message, testData.getExpectedValue(), test.getField().get());
         Assert.assertEquals(message, testData.isExpectedDirty(), test.getField().isDirty());
      }
   }

   private void checkObjects(String message, Object expected, Object actual) {
      Assert.assertFalse(String.format("%s expected[%s] actual[%s]", message, expected, actual),
         Compare.isDifferent(expected, actual));
   }

   @SuppressWarnings("unchecked")
   @Parameters
   public static Collection<Object[]> data() {
      Collection<Object[]> data = new ArrayList<>();

      data.add(new Object[] {//
         new OseeFieldTestData<Object>(new OseeField<Object>(), //
            null, false, //
            new FieldGetSetTestData<Object>(true, "one test", "one test", true), //
            new FieldGetSetTestData<Object>(false, "two test", "two test", true), //
            new FieldGetSetTestData<Object>(true, "three test", "three test", true)//
         )});//

      data.add(new Object[] { //
         new OseeFieldTestData<Integer>(new OseeField<Integer>(), //
            null, false, //
            new FieldGetSetTestData<Integer>(true, 1, 1, true), //
            new FieldGetSetTestData<Integer>(false, 5, 5, true), //
            new FieldGetSetTestData<Integer>(true, Integer.MIN_VALUE, Integer.MIN_VALUE, true)//
         )});

      data.add(new Object[] { //
         new OseeFieldTestData<Boolean>(new OseeField<Boolean>(), //
            null, false, //
            new FieldGetSetTestData<Boolean>(true, true, true, true), //
            new FieldGetSetTestData<Boolean>(false, true, true, true), //
            new FieldGetSetTestData<Boolean>(true, false, false, true), //
            new FieldGetSetTestData<Boolean>(false, false, false, true)//
         )});

      data.add(new Object[] {new OseeFieldTestData<Object>(new OseeField<Object>("string1"), "string1", true)});
      data.add(new Object[] {
         new OseeFieldTestData<Integer>(new OseeField<Integer>(Integer.MIN_VALUE), Integer.MIN_VALUE, true)});
      data.add(new Object[] {new OseeFieldTestData<Boolean>(new OseeField<Boolean>(true), true, true)});
      data.add(new Object[] {new OseeFieldTestData<Boolean>(new OseeField<Boolean>(false), false, true)});

      data.add(new Object[] {//
         new OseeFieldTestData<String>(new OseeField<String>("string2"), "string2", true, //
            new FieldGetSetTestData<String>(false, "another", "another", true), //
            new FieldGetSetTestData<String>(true, "something", "something", true)//
         )});

      //      IOseeTypeFactory factory = new OseeTypeFactory();
      //      data.add(createAssociatedArtifactTest(factory));
      return data;
   }

}
