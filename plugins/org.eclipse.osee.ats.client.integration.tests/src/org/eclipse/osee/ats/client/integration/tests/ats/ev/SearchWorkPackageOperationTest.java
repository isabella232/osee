/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.client.integration.tests.ats.ev;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.ats.demo.api.DemoArtifactToken;
import org.eclipse.osee.ats.ev.SearchWorkPackageOperation;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test case for {@link SearchWorkPackageOperation}
 *
 * @author Donald G. Dunne
 */
@RunWith(Parameterized.class)
public class SearchWorkPackageOperationTest {

   private final String resultSize;
   private final Collection<Long> teamDefIds;
   private final boolean includeChildrenTeamDefs;
   private final Collection<Long> aiIds;
   private final Active activeOption;
   private final Collection<Long> expectedWpIds;
   private final boolean includeChildrenAIs;

   public SearchWorkPackageOperationTest(String resultSize, Collection<Long> teamDefIds, boolean includeChildrenTeamDefs, Collection<Long> aiIds, boolean includeChildrenAIs, Active activeOption, Collection<Long> expectedWpIds) {
      this.resultSize = resultSize;
      this.teamDefIds = teamDefIds;
      this.includeChildrenTeamDefs = includeChildrenTeamDefs;
      this.aiIds = aiIds;
      this.includeChildrenAIs = includeChildrenAIs;
      this.activeOption = activeOption;
      this.expectedWpIds = expectedWpIds;
   }

   @Parameters
   public static Collection<Object[]> data() {
      List<Object[]> data = new ArrayList<>();
      List<Long> EMPYT_RESULTS = new ArrayList<>();
      Long WP_01 = DemoArtifactToken.SAW_Code_Team_WorkPackage_01.getId();
      Long WP_02 = DemoArtifactToken.SAW_Code_Team_WorkPackage_02.getId();
      Long WP_03 = DemoArtifactToken.SAW_Code_Team_WorkPackage_03.getId();
      Long WP_0A = DemoArtifactToken.SAW_Test_AI_WorkPackage_0A.getId();
      Long WP_0B = DemoArtifactToken.SAW_Test_AI_WorkPackage_0B.getId();
      Long WP_0C = DemoArtifactToken.SAW_Test_AI_WorkPackage_0C.getId();

      // Test Work Packages configured by Team Def
      addTest(data, "no work packages should be returned", Arrays.asList(DemoArtifactToken.SAW_SW.getId()), false,
         new ArrayList<Long>(), false, Active.Both, EMPYT_RESULTS);
      addTest(data, "3 work packages should be returned", Arrays.asList(DemoArtifactToken.SAW_SW.getId()), true,
         new ArrayList<Long>(), false, Active.Both, Arrays.asList(WP_01, WP_02, WP_03));
      addTest(data, "2 active work packages should be returned", Arrays.asList(DemoArtifactToken.SAW_SW.getId()), true,
         new ArrayList<Long>(), false, Active.Active, Arrays.asList(WP_01, WP_02));
      addTest(data, "1 in-active work packages should be returned", Arrays.asList(DemoArtifactToken.SAW_SW.getId()),
         true, new ArrayList<Long>(), false, Active.InActive, Arrays.asList(WP_03));

      // Test Work Packages configured by AI
      addTest(data, "no work packages should be returned", new ArrayList<Long>(), false,
         Arrays.asList(DemoArtifactToken.SAW_CSCI_AI.getId()), false, Active.Both, EMPYT_RESULTS);
      addTest(data, "3 work packages should be returned", new ArrayList<Long>(), false,
         Arrays.asList(DemoArtifactToken.SAW_CSCI_AI.getId()), true, Active.Both, Arrays.asList(WP_0A, WP_0B, WP_0C));
      addTest(data, "2 active work packages should be returned", new ArrayList<Long>(), false,
         Arrays.asList(DemoArtifactToken.SAW_CSCI_AI.getId()), true, Active.Active, Arrays.asList(WP_0A, WP_0B));
      addTest(data, "1 in-active work packages should be returned", new ArrayList<Long>(), false,
         Arrays.asList(DemoArtifactToken.SAW_CSCI_AI.getId()), true, Active.InActive, Arrays.asList(WP_0C));

      // Test configured by both
      addTest(data, "4 active work packages should be returned", Arrays.asList(DemoArtifactToken.SAW_SW.getId()), true,
         Arrays.asList(DemoArtifactToken.SAW_Test_AI.getId()), true, Active.Active,
         Arrays.asList(WP_01, WP_02, WP_0A, WP_0B));

      return data;
   }

   @Test
   public void testSearchResults() {
      List<IAtsTeamDefinition> teamDefs = new ArrayList<>();
      for (Long teamDefId : teamDefIds) {
         IAtsTeamDefinition teamDef = AtsClientService.get().getCache().getAtsObject(teamDefId);
         teamDefs.add(teamDef);
      }

      List<IAtsActionableItem> ais = new ArrayList<>();
      for (Long aiId : aiIds) {
         IAtsActionableItem ai = AtsClientService.get().getCache().getAtsObject(aiId);
         ais.add(ai);
      }

      SearchWorkPackageOperation operation = new SearchWorkPackageOperation("test", teamDefs, includeChildrenTeamDefs,
         ais, includeChildrenAIs, activeOption);
      Operations.executeWorkAndCheckStatus(operation);
      Set<Artifact> resultArtifacts = operation.getResultArtifacts();
      Collection<Long> resultArtifactIds = Artifacts.toIdsL(resultArtifacts);
      Assert.assertEquals(resultSize, expectedWpIds.size(), resultArtifacts.size());
      for (Long expectedId : expectedWpIds) {
         Assert.assertTrue("Expected id " + expectedId + " not found in results",
            resultArtifactIds.contains(expectedId));
      }
   }

   private static void addTest(List<Object[]> testData, String toSearch, Collection<Long> teamDefIds, boolean includeChildrenTeamDefs, Collection<Long> aiIds, boolean includeChildrenAIs, Active both, Collection<Long> expectedWpIds) {
      testData.add(
         new Object[] {toSearch, teamDefIds, includeChildrenTeamDefs, aiIds, includeChildrenAIs, both, expectedWpIds});
   }

}
