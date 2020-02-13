/*******************************************************************************
 * Copyright (c) 2018 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.ide.integration.tests;

import org.eclipse.osee.ats.ide.demo.DemoUtil;
import org.junit.Assert;
import org.junit.BeforeClass;

/**
 * @author Donald G. Dunne
 */
public class AtsClientServiceTest {

   @BeforeClass
   public static void setUp() throws Exception {
      DemoUtil.checkDbInitAndPopulateSuccess();
   }

   @org.junit.Test
   public void testHealthEndpoint() {
      Assert.assertTrue(AtsClientService.get().getServerEndpoints().getHealthEndpoint().alive());
   }
}
