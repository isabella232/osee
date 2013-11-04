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
package org.eclipse.osee.framework.database.core;

import java.sql.Timestamp;
import java.util.List;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.database.test.mocks.MockJoinAccessor;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test Case for {@link IdJoinQuery}
 * 
 * @author Roberto E. Escobar
 */
public class IdJoinQueryTest {

   @Test
   public void testAdd() throws OseeCoreException {
      MockJoinAccessor joinAccessor = new MockJoinAccessor();
      IdJoinQuery join = new IdJoinQuery(joinAccessor, 999);
      Assert.assertEquals(0, join.size());
      Assert.assertEquals(true, join.isEmpty());

      join.add(1234);
      Assert.assertEquals(1, join.size());
      Assert.assertEquals(false, join.isEmpty());

      join.add(1234);
      Assert.assertEquals(1, join.size());

      Assert.assertEquals(false, join.wasStored());
      join.store();
      Assert.assertEquals(true, join.wasStored());

      Assert.assertNull(joinAccessor.getConnection());
      Assert.assertEquals(999, joinAccessor.getQueryId());

      List<Object[]> data = joinAccessor.getDataList();
      Assert.assertEquals(1, data.size());

      Object[] entry = data.get(0);
      Assert.assertEquals(3, entry.length);
      Assert.assertEquals(999, entry[0]);
      Assert.assertTrue(entry[1] instanceof Timestamp);
      Assert.assertEquals(1234L, entry[2]);

   }

   @Test(expected = OseeDataStoreException.class)
   public void testStoreTwice() throws OseeCoreException {
      MockJoinAccessor joinAccessor = new MockJoinAccessor();
      IdJoinQuery join = new IdJoinQuery(joinAccessor, 1000);

      Assert.assertEquals(false, join.wasStored());
      join.store();
      Assert.assertEquals(true, join.wasStored());

      Assert.assertNull(joinAccessor.getConnection());
      Assert.assertEquals(1000, joinAccessor.getQueryId());

      join.store();
   }
}
