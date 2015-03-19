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
package org.eclipse.osee.framework.core.model.cache;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.model.OseeEnumEntry;
import org.eclipse.osee.framework.core.model.mocks.MockDataFactory;
import org.eclipse.osee.framework.core.model.mocks.ModelAsserts;
import org.eclipse.osee.framework.core.model.type.OseeEnumType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.junit.BeforeClass;

/**
 * Test Case for {@link OseeEnumTypeCache}
 * 
 * @author Roberto E. Escobar
 */
public class OseeEnumTypeCacheTest extends AbstractOseeTypeCacheTest<OseeEnumType> {

   private static List<OseeEnumType> data;
   private static OseeEnumTypeCache cache;

   @BeforeClass
   public static void prepareTestData() throws OseeCoreException {
      data = new ArrayList<OseeEnumType>();

      cache = new OseeEnumTypeCache();

      int typeId = 100;
      for (int index = 0; index < 10; index++) {
         OseeEnumType item = MockDataFactory.createEnumType(index);
         List<OseeEnumEntry> entries = new ArrayList<OseeEnumEntry>();
         for (int j = 1; j <= 5; j++) {
            OseeEnumEntry enumEntry = MockDataFactory.createEnumEntry(index + j);
            entries.add(enumEntry);
         }
         item.setEntries(entries);

         data.add(item);
         item.setId(typeId++);
         cache.cache(item);
      }
   }

   public OseeEnumTypeCacheTest() {
      super(data, cache);
   }

   @Override
   protected void checkEquals(OseeEnumType expected, OseeEnumType actual) throws OseeCoreException {
      ModelAsserts.checkEnumType(expected, actual);
   }

}
