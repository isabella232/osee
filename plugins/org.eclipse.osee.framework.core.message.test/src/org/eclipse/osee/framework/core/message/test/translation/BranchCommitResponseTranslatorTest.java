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
import org.eclipse.osee.framework.core.enums.CoreTranslatorId;
import org.eclipse.osee.framework.core.message.BranchCommitResponse;
import org.eclipse.osee.framework.core.message.internal.DataTranslationService;
import org.eclipse.osee.framework.core.message.internal.translation.BranchCommitResponseTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.TransactionRecordTranslator;
import org.eclipse.osee.framework.core.message.test.mocks.DataAsserts;
import org.eclipse.osee.framework.core.message.test.mocks.MockOseeDataAccessor;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.model.TransactionRecordFactory;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.mocks.MockDataFactory;
import org.eclipse.osee.framework.core.translation.IDataTranslationService;
import org.eclipse.osee.framework.core.translation.ITranslator;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test Case for {@link BranchCommitResponseTranslator}
 * 
 * @author Megumi Telles
 */
@RunWith(Parameterized.class)
public class BranchCommitResponseTranslatorTest extends BaseTranslatorTest<BranchCommitResponse> {

   public BranchCommitResponseTranslatorTest(BranchCommitResponse data, ITranslator<BranchCommitResponse> translator) {
      super(data, translator);
   }

   @Override
   protected void checkEquals(BranchCommitResponse expected, BranchCommitResponse actual) {
      DataAsserts.assertEquals(expected, actual);
   }

   @Parameters
   public static Collection<Object[]> data() throws OseeCoreException {

      List<Object[]> data = new ArrayList<Object[]>();
      IDataTranslationService service = new DataTranslationService();
      BranchCache branchCache = new BranchCache(new MockOseeDataAccessor<String, Branch>());
      service.addTranslator(new TransactionRecordTranslator(new TransactionRecordFactory(), branchCache),
         CoreTranslatorId.TRANSACTION_RECORD);

      ITranslator<BranchCommitResponse> translator = new BranchCommitResponseTranslator(service);
      for (int index = 1; index <= 2; index++) {
         TransactionRecord tx = MockDataFactory.createTransaction(index, index * 3);

         BranchCommitResponse response = new BranchCommitResponse();
         response.setTransaction(tx);
         data.add(new Object[] {response, translator});
      }
      data.add(new Object[] {new BranchCommitResponse(), translator});
      return data;
   }
}
