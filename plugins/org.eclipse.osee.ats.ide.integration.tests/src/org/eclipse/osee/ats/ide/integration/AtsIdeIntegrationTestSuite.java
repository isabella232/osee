/*********************************************************************
 * Copyright (c) 2012 Boeing
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

package org.eclipse.osee.ats.ide.integration;

import org.eclipse.osee.ats.ide.integration.tests.AtsTest_AllAts_Suite;
import org.eclipse.osee.ats.ide.integration.tests.DemoDbPopulateSuite;
import org.eclipse.osee.ats.ide.integration.tests.DirtyArtifactCacheTest;
import org.eclipse.osee.ats.ide.integration.tests.framework.skynet.core.artifact.SkyentCoreArtifact_Suite;
import org.eclipse.osee.ats.ide.integration.tests.framework.ui.skynet.FrameworkUiSkynetTest_Suite;
import org.eclipse.osee.ats.ide.integration.tests.framework.ui.skynet.dialog.FrameworkUiSkynetTest_Dialog_Suite;
import org.eclipse.osee.ats.ide.integration.tests.orcs.rest.ClientEndpointTest;
import org.eclipse.osee.ats.ide.integration.tests.util.DbInitTest;
import org.eclipse.osee.framework.jdk.core.util.ElapsedTime;
import org.eclipse.osee.framework.jdk.core.util.ElapsedTime.Units;
import org.eclipse.osee.framework.jdk.core.util.OseeProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Roberto E. Escobar
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   DbInitTest.class,
   DemoDbPopulateSuite.class,
   SkyentCoreArtifact_Suite.class,
   AtsTest_AllAts_Suite.class,
   FrameworkUiSkynetTest_Suite.class,
   FrameworkUiSkynetTest_Dialog_Suite.class,
   ClientEndpointTest.class,
   DirtyArtifactCacheTest.class})
public class AtsIdeIntegrationTestSuite {
   // Test Suite

   private static ElapsedTime time;

   @BeforeClass
   public static void setup() {
      time = new ElapsedTime("AtsIdeIntegrationTestSuite", true);
      OseeProperties.setIsInTest(true);
   }

   @AfterClass
   public static void cleanup() {
      time.end(Units.MIN);
      OseeProperties.setIsInTest(false);
   }

}
