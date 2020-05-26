/*********************************************************************
 * Copyright (c) 2010 Boeing
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

package org.eclipse.osee.framework.jdk.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class HashCollectionTest {

   /**
    * Test method for
    * {@link org.eclipse.osee.framework.jdk.core.type.HashCollection#put(java.lang.Object, java.lang.Object)}.
    */
   @Test
   public void testPutKV() {
      HashCollection<String, String> collection = new HashCollection<>();

      Assert.assertEquals(0, collection.size());
      Assert.assertEquals(0, collection.getValues().size());
      Assert.assertNull(collection.getValues("this"));

      for (int x = 1; x <= 5; x++) {
         collection.put("key", "value " + x);
      }

      Assert.assertEquals(5, collection.size());
      Assert.assertEquals(5, collection.getValues().size());
      Assert.assertNull(collection.getValues("this"));
      Assert.assertEquals(5, collection.getValues("key").size());
   }

   /**
    * Test method for
    * {@link org.eclipse.osee.framework.jdk.core.type.HashCollection#put(java.lang.Object, java.util.Collection)} .
    */
   @Test
   public void testPutKCollectionOfV() {
      HashCollection<String, String> collection = new HashCollection<>();
      List<String> values = new ArrayList<>();

      for (int x = 1; x <= 5; x++) {
         values.add("value " + x);
      }
      collection.put("key", values);

      Assert.assertEquals(5, collection.size());
      Assert.assertEquals(5, collection.getValues().size());
      Assert.assertNull(collection.getValues("this"));
      Assert.assertEquals(5, collection.getValues("key").size());
   }

   /**
    * Test method for
    * {@link org.eclipse.osee.framework.jdk.core.type.HashCollection#removeValue(java.lang.Object, java.lang.Object)} .
    */
   @Test
   public void testRemoveValue() {
      HashCollection<String, String> collection = new HashCollection<>();
      for (int x = 1; x <= 5; x++) {
         collection.put("key", "value " + x);
      }

      Assert.assertEquals(5, collection.getValues("key").size());
      collection.removeValue("key", "value 3");
      Assert.assertEquals(4, collection.getValues("key").size());
   }

   /**
    * Test method for {@link org.eclipse.osee.framework.jdk.core.type.HashCollection#removeValues(java.lang.Object)} .
    */
   @Test
   public void testRemoveValues() {
      HashCollection<String, String> collection = new HashCollection<>();
      for (int x = 1; x <= 5; x++) {
         collection.put("key", "value " + x);
      }
      Assert.assertEquals(5, collection.getValues("key").size());
      Collection<String> removedValues = collection.removeValues("key");
      Assert.assertNull(collection.getValues("key"));
      Assert.assertEquals(5, removedValues.size());
   }

   /**
    * Test method for {@link org.eclipse.osee.framework.jdk.core.type.HashCollection#keySet()}.
    */
   @Test
   public void testKeySet() {
      HashCollection<String, String> collection = new HashCollection<>();
      for (int x = 1; x <= 5; x++) {
         collection.put("key", "value " + x);
      }
      for (int x = 1; x <= 3; x++) {
         collection.put("key2", "value " + x);
      }
      Assert.assertEquals(2, collection.keySet().size());
   }

   /**
    * Test method for {@link org.eclipse.osee.framework.jdk.core.type.HashCollection#clear()}.
    */
   @Test
   public void testClear() {
      HashCollection<String, String> collection = new HashCollection<>();
      for (int x = 1; x <= 5; x++) {
         collection.put("key", "value " + x);
      }
      Assert.assertEquals(5, collection.getValues().size());
      collection.clear();
      Assert.assertEquals(0, collection.getValues().size());
      Assert.assertEquals(0, collection.keySet().size());
   }

   /**
    * Test method for {@link org.eclipse.osee.framework.jdk.core.type.HashCollection#containsKey(java.lang.Object)}.
    */
   @Test
   public void testContainsKey() {
      HashCollection<String, String> collection = new HashCollection<>();
      for (int x = 1; x <= 5; x++) {
         collection.put("key", "value " + x);
      }
      Assert.assertTrue(collection.containsKey("key"));
   }

   /**
    * Test method for {@link org.eclipse.osee.framework.jdk.core.type.HashCollection#isEmpty()}.
    */
   @Test
   public void testIsEmpty() {
      HashCollection<String, String> collection = new HashCollection<>();
      Assert.assertTrue(collection.isEmpty());
      collection.put("key", "value 1");
      Assert.assertFalse(collection.isEmpty());
      collection.clear();
      Assert.assertTrue(collection.isEmpty());
   }

   /**
    * Test method for {@link org.eclipse.osee.framework.jdk.core.type.HashCollection#containsValue(java.lang.Object)}.
    */
   @Test
   public void testContainsValue() {
      HashCollection<String, String> collection = new HashCollection<>();
      for (int x = 1; x <= 5; x++) {
         collection.put("key", "value " + x);
      }
      Assert.assertTrue(collection.containsValue("value 3"));

   }

   @Test
   public void testHashCollectionObject() {
      HashCollection<Object, String> collection = new HashCollection<>();

      Assert.assertEquals(0, collection.size());
      Assert.assertEquals(0, collection.getValues().size());
      Assert.assertNull(collection.getValues(new Object()));

      Object keyObject = new Object();
      for (int x = 1; x <= 5; x++) {
         collection.put(keyObject, "value " + x);
      }

      Assert.assertEquals(5, collection.size());
      Assert.assertEquals(5, collection.getValues().size());
      Assert.assertNull(collection.getValues(new Object()));
      Assert.assertEquals(5, collection.getValues(keyObject).size());
   }

}
