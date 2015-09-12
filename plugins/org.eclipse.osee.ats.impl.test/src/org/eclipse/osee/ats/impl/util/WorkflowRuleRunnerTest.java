/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.impl.util;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.util.IAtsStoreService;
import org.eclipse.osee.ats.api.workdef.IAtsCreateTaskRuleDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinitionAdmin;
import org.eclipse.osee.ats.api.workdef.NullRuleDefinition;
import org.eclipse.osee.ats.api.workdef.RuleEventType;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.state.IAtsStateManager;
import org.eclipse.osee.ats.core.users.AtsCoreUsers;
import org.eclipse.osee.ats.impl.IAtsServer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test unit for {@link WorkflowRuleRunner}
 *
 * @author Mark Joy
 */
public class WorkflowRuleRunnerTest {

   // @formatter:off
   @Mock IAtsCreateTaskRuleDefinition createTaskRule;
   @Mock NullRuleDefinition nullRuleDef;
   @Mock WorkflowRuleRunner ruleRunner;
   @Mock IAtsWorkItem workItem;
   @Mock IAtsServer atsServer;
   @Mock IAtsStateDefinition atsStateDef;
   @Mock IAtsChangeSet changes;
   @Mock IAtsTeamWorkflow teamWf;
   @Mock IAtsTeamDefinition teamDef;
   @Mock IAtsServices atsServices;
   @Mock IAtsStoreService atsStoreService;
   @Mock IAtsWorkDefinition atsWorkDef;
   @Mock IAtsWorkDefinitionAdmin atsWorkDefAdmin;
   @Mock IAtsStateManager atsStateMgr;

   List<IAtsWorkItem> workflowsCreated = new ArrayList<>();
   Collection<String> rules = new ArrayList<>();
   List<String> ruleList = new ArrayList<>();
   String teamDefRule = "TestCreateTaskRule";
   RuleEventType eventType = RuleEventType.CreateWorkflow;
   List<RuleEventType> eventList = new ArrayList<>();
   String stateName = "Implement";

   @Before
   public void setup() throws Exception {
      MockitoAnnotations.initMocks(this);

      when(atsServer.getStoreService()).thenReturn(atsStoreService);
      when(atsStoreService.createAtsChangeSet("WorkflowRunner", AtsCoreUsers.SYSTEM_USER)).thenReturn(changes);
      when(workItem.isTeamWorkflow()).thenReturn(true);
      when(workItem.getParentTeamWorkflow()).thenReturn(teamWf);
      when(atsServer.getWorkDefAdmin()).thenReturn(atsWorkDefAdmin);
      when(workItem.getParentTeamWorkflow()).thenReturn(teamWf);
      when(teamWf.getTeamDefinition()).thenReturn(teamDef);
      when(createTaskRule.getRuleEvents()).thenReturn(eventList);
      when(workItem.getWorkDefinition()).thenReturn(null);
      when(workItem.getWorkDefinition()).thenReturn(atsWorkDef);
      when(atsServer.getServices()).thenReturn(atsServices);
      when(teamDef.getRules()).thenReturn(rules);
      when(workItem.getStateMgr()).thenReturn(atsStateMgr);
      when(atsStateMgr.getCurrentStateName()).thenReturn(stateName);
      when(atsWorkDef.getStateByName(stateName)).thenReturn(atsStateDef);
      when(atsStateDef.getRules()).thenReturn(ruleList);
   }

   @Test
   public void testRun() {
      when(atsWorkDefAdmin.getRuleDefinition(teamDefRule)).thenReturn(createTaskRule);

      workflowsCreated.add(workItem);
      rules.add(teamDefRule);
      ruleList.add(teamDefRule);
      eventList.add(eventType);

      ruleRunner = new WorkflowRuleRunner(eventType, workflowsCreated, atsServer);
      ruleRunner.run();
      verify(createTaskRule, times(2)).execute(workItem, atsServices, changes, ruleRunner.getRuleResults());

      // Clear the ruleList passed for the StateDef rule check
      ruleList.clear();
      ruleRunner.run();
      verify(createTaskRule, times(3)).execute(workItem, atsServices, changes, ruleRunner.getRuleResults());

      // Clear the rules passed for the Team Def rule check
      rules.clear();
      ruleRunner.run();
      verify(createTaskRule, times(3)).execute(workItem, atsServices, changes, ruleRunner.getRuleResults());
      ruleList.clear();
      eventList.clear();
   }

   @Test
   public void testRunWithNullRule() {
      when(atsWorkDefAdmin.getRuleDefinition(teamDefRule)).thenReturn(nullRuleDef);

      workflowsCreated.add(workItem);
      rules.add(teamDefRule);
      ruleList.add(teamDefRule);
      eventList.add(eventType);

      ruleRunner = new WorkflowRuleRunner(eventType, workflowsCreated, atsServer);
      ruleRunner.run();
      verify(createTaskRule, never()).execute(workItem, atsServices, changes, ruleRunner.getRuleResults());

   }

}
