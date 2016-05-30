/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.client.integration.tests.ats.core.client.branch;

import static org.eclipse.osee.framework.core.enums.DemoBranches.SAW_Bld_1;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import org.eclipse.osee.ats.api.commit.ICommitConfigItem;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.ats.client.integration.tests.ats.core.client.AtsTestUtil;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.OseeProperties;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test unit for {@AtsBranchServiceImpl}
 *
 * @author Shawn F. Cook
 */
public class AtsBranchServiceImplTest {

   @BeforeClass
   public static void setUp() throws Exception {
      OseeProperties.setIsInTest(true);
      TestUtil.setIsInTest(true);
   }

   @AfterClass
   public static void tearDown() throws Exception {
      AtsTestUtil.cleanup();
      OseeProperties.setIsInTest(false);
      TestUtil.setIsInTest(false);
   }

   @Test
   public void testGetCommitTransactionsAndConfigItemsForTeamWf_teamDef() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset(
         AtsBranchServiceImplTest.class.getSimpleName() + ".testGetCommitTransactionsAndConfigItemsForTeamWf_teamDef");
      TeamWorkFlowArtifact teamArt = AtsTestUtil.getTeamWf();

      //Test Team Def-base Team Arts
      IAtsTeamDefinition teamDef = teamArt.getTeamDefinition();
      // clear versions to config item is from teamDef
      teamDef.getVersions().clear();
      teamDef.setBaselineBranchUuid(SAW_Bld_1.getUuid());
      Collection<Object> commitObjs =
         AtsClientService.get().getBranchService().getCommitTransactionsAndConfigItemsForTeamWf(teamArt);
      assertTrue("commitObjs has wrong size", commitObjs.size() == 1);
      assertTrue("commitObjs is missing teamDef", commitObjs.contains(teamDef));
   }

   @Test
   public void testGetCommitTransactionsAndConfigItemsForTeamWf_versions() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset(
         AtsBranchServiceImplTest.class.getSimpleName() + ".testGetCommitTransactionsAndConfigItemsForTeamWf_versions");
      TeamWorkFlowArtifact teamArt = AtsTestUtil.getTeamWf();
      //Test Version-based Team Arts
      IAtsVersion verArt1 = AtsTestUtil.getVerArt1();
      IAtsVersion verArt2 = AtsTestUtil.getVerArt2();
      verArt1.getParallelVersions().add(verArt2);
      AtsClientService.get().getVersionService().setTargetedVersion(teamArt, verArt1);
      Collection<Object> commitObjs =
         AtsClientService.get().getBranchService().getCommitTransactionsAndConfigItemsForTeamWf(teamArt);
      assertTrue("commitObjs has wrong size", commitObjs.size() == 2);
      assertTrue("commitObjs is missing verArt1", commitObjs.contains(verArt1));
      assertTrue("commitObjs is missing verArt2", commitObjs.contains(verArt2));
   }

   @Test
   public void testGetCommitTransactionsAndConfigItemsForTeam_txRecords() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset(
         AtsBranchServiceImplTest.class.getSimpleName() + ".testGetCommitTransactionsAndConfigItemsForTeam_txRecords");
      TeamWorkFlowArtifact teamArt = AtsTestUtil.getTeamWf();
      IAtsTeamDefinition teamDef = teamArt.getTeamDefinition();
      teamDef.setBaselineBranchUuid(SAW_Bld_1.getUuid());
      // clear versions to config item is from teamDef
      teamDef.getVersions().clear();
      //Test TxRecords
      TransactionRecord txRecord = new TransactionRecord(1234, SAW_Bld_1, "comment", new Date(),
         UserManager.getUser().getArtId(), UserManager.getUser().getArtId(), TransactionDetailsType.Baselined);
      Collection<TransactionId> commitTxs = new ArrayList<>();
      Collection<ICommitConfigItem> configArtSet = new HashSet<>();
      commitTxs.add(txRecord);
      Collection<Object> commitObjs =
         AtsClientService.get().getBranchService().combineCommitTransactionsAndConfigItems(configArtSet, commitTxs);
      assertTrue("commitObjs has wrong size", commitObjs.size() == 1);

      Collection<ICommitConfigItem> configArtifactsConfiguredToCommitTo =
         AtsClientService.get().getBranchService().getConfigArtifactsConfiguredToCommitTo(teamArt);
      configArtSet.add(configArtifactsConfiguredToCommitTo.iterator().next());
      commitObjs =
         AtsClientService.get().getBranchService().combineCommitTransactionsAndConfigItems(configArtSet, commitTxs);
      assertTrue("commitObjs has wrong size", commitObjs.size() == 1);
      assertTrue("commitObjs has wrong object", commitObjs.contains(teamDef));
   }
}
