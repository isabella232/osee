/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.editor.stateItem;

import static org.junit.Assert.assertFalse;
import java.util.Arrays;
import org.eclipse.osee.ats.core.client.AtsTestUtil;
import org.eclipse.osee.ats.core.client.team.TeamState;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.AtsUsersClient;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.DemoTestUtil;
import org.eclipse.osee.ats.workdef.api.IAtsStateDefinition;
import org.eclipse.osee.ats.workdef.api.IStateToken;
import org.eclipse.osee.ats.workdef.api.RuleDefinitionOption;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test Case for {@link AtsForceAssigneesToTeamLeadsStateItem}
 * 
 * @author Donald G. Dunne
 */
public class AtsForceAssigneesToTeamLeadsStateItemTest {

   private static final String JOE_SMITH = "Joe Smith";
   private static final String ALEX_KAY = "Alex Kay";

   private static TeamWorkFlowArtifact teamArt;

   @Before
   public void setUp() throws Exception {
      // This test should only be run on test db
      assertFalse("Test should not be run in production db", AtsUtil.isProductionDb());

      if (teamArt == null) {
         SkynetTransaction transaction =
            TransactionManager.createTransaction(AtsUtil.getAtsBranch(), getClass().getSimpleName());
         teamArt = DemoTestUtil.createSimpleAction(getClass().getSimpleName(), transaction);
         transaction.execute();
      }
   }

   @BeforeClass
   @AfterClass
   public static void testCleanup() throws Exception {
      // Test adds the atsForceAssigneesToTeamLeads; remove it before and after test
      if (teamArt != null) {
         IAtsStateDefinition authStateDef =
            teamArt.getWorkDefinition().getStateByName(TeamState.Authorize.getName());
         authStateDef.removeRule(RuleDefinitionOption.ForceAssigneesToTeamLeads.name());
      }

      AtsTestUtil.cleanupSimpleTest(AtsForceAssigneesToTeamLeadsStateItemTest.class.getSimpleName());
   }

   @Test
   public void testTransitioned() throws OseeCoreException {
      Assert.assertNotNull(teamArt);

      // assignee should be Joe Smith
      Assert.assertEquals(1, teamArt.getStateMgr().getAssignees().size());
      Assert.assertEquals(AtsUsersClient.getUserByName(JOE_SMITH),
         teamArt.getStateMgr().getAssignees().iterator().next());

      // set assignee to Alex Kay
      teamArt.getStateMgr().setAssignee(AtsUsersClient.getUserByName(ALEX_KAY));
      teamArt.persist(getClass().getSimpleName());
      Assert.assertEquals(1, teamArt.getStateMgr().getAssignees().size());
      Assert.assertEquals(AtsUsersClient.getUserByName(ALEX_KAY),
         teamArt.getStateMgr().getAssignees().iterator().next());

      IStateToken fromState = teamArt.getWorkDefinition().getStateByName(TeamState.Analyze.getName());
      IStateToken toState = teamArt.getWorkDefinition().getStateByName(TeamState.Authorize.getName());

      IAtsStateDefinition authStateDef = teamArt.getWorkDefinition().getStateByName(TeamState.Authorize.getName());
      authStateDef.getRules().add(RuleDefinitionOption.ForceAssigneesToTeamLeads.name());

      // make call to state item that should set options based on artifact's attribute value
      AtsForceAssigneesToTeamLeadsStateItem stateItem = new AtsForceAssigneesToTeamLeadsStateItem();
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranch(), getClass().getSimpleName());
      stateItem.transitioned(teamArt, fromState, toState, Arrays.asList(AtsUsersClient.getUser()), transaction);
      transaction.execute();

      // assignee should be Joe Smith
      Assert.assertEquals(1, teamArt.getStateMgr().getAssignees().size());
      Assert.assertEquals(AtsUsersClient.getUserByName(JOE_SMITH),
         teamArt.getStateMgr().getAssignees().iterator().next());
   }
}
