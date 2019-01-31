/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.ide.integration.tests.framework.skynet.core.artifact;

import org.eclipse.osee.ats.ide.demo.DemoUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Donald G. Dunne
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ //
   ArtifactTypeManagerTest.class, //
})
public class SkyentCoreArtifact_Suite {

   @BeforeClass
   public static void setUp() throws Exception {
      DemoUtil.checkDbInitAndPopulateSuccess();
      System.out.println("\n\nBegin " + SkyentCoreArtifact_Suite.class.getSimpleName());
   }

   @AfterClass
   public static void tearDown() throws Exception {
      System.out.println("End " + SkyentCoreArtifact_Suite.class.getSimpleName());
   }
}