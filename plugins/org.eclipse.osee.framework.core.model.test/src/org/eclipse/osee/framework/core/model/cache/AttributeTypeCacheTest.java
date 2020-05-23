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

package org.eclipse.osee.framework.core.model.cache;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.model.mocks.MockDataFactory;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.junit.BeforeClass;

/**
 * Test Case for {@link AttributeTypeCache}
 *
 * @author Roberto E. Escobar
 */
public class AttributeTypeCacheTest extends AbstractOseeTypeCacheTest<AttributeType> {

   private static List<AttributeType> attributeTypes;
   private static AttributeTypeCache attrCache;

   @BeforeClass
   public static void prepareTestData() {
      attributeTypes = new ArrayList<>();

      attrCache = new AttributeTypeCache();

      long typeId = 100;
      for (int index = 0; index < 10; index++) {
         AttributeType item = MockDataFactory.createAttributeType(index, null, typeId++);
         attributeTypes.add(item);
         attrCache.cache(item);
      }
   }

   public AttributeTypeCacheTest() {
      super(attributeTypes, attrCache);
   }

}
