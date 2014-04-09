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
package org.eclipse.osee.framework.core.message.test.translation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.message.internal.translation.TransactionRecordTranslator;
import org.eclipse.osee.framework.core.message.test.mocks.DataAsserts;
import org.eclipse.osee.framework.core.message.test.mocks.MockOseeDataAccessor;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.model.TransactionRecordFactory;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.mocks.MockDataFactory;
import org.eclipse.osee.framework.core.translation.ITranslator;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test Case For {@link TransactionRecordTranslator}
 * 
 * @author Roberto E. Escobar
 */
@RunWith(Parameterized.class)
public class TransactionRecordTranslatorTest extends BaseTranslatorTest<TransactionRecord> {

   public TransactionRecordTranslatorTest(TransactionRecord data, ITranslator<TransactionRecord> translator) {
      super(data, translator);
   }

   @Override
   protected void checkEquals(TransactionRecord expected, TransactionRecord actual) {
      Assert.assertNotSame(expected, actual);
      DataAsserts.assertEquals(expected, actual);
   }

   @Parameters
   public static Collection<Object[]> data() {
      BranchCache branchCache = new BranchCache(new MockOseeDataAccessor<Long, Branch>());
      ITranslator<TransactionRecord> translator =
         new TransactionRecordTranslator(new TransactionRecordFactory(), branchCache);
      List<Object[]> data = new ArrayList<Object[]>();
      for (int index = 1; index <= 2; index++) {
         data.add(new Object[] {MockDataFactory.createTransaction(index * 10, index * 3), translator});
      }
      return data;
   }
}
