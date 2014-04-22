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
package org.eclipse.osee.framework.core.message.test.data;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.osee.framework.core.message.PurgeBranchRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test Case for {@link PurgeBranchRequest}
 * 
 * @author Megumi Telles
 * @author Jeff C. Phillips
 */
@RunWith(Parameterized.class)
public class PurgeBranchRequestTest {

   private final PurgeBranchRequest request;
   private final int branchUuid;

   public PurgeBranchRequestTest(int expectedBranchId) {
      this.request = new PurgeBranchRequest(expectedBranchId, false);
      this.branchUuid = expectedBranchId;
   }

   @Test
   public void testGetBranchId() {
      Assert.assertEquals(branchUuid, request.getBranchId());
   }

   @Parameters
   public static Collection<Object[]> getData() {
      Collection<Object[]> data = new ArrayList<Object[]>();
      for (int index = 1; index <= 2; index++) {
         int branchUuid = index * 7;
         data.add(new Object[] {branchUuid});
      }
      return data;
   }

}
