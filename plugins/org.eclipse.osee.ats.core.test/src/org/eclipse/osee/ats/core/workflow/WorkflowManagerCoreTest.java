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
package org.eclipse.osee.ats.core.workflow;

import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.ats.api.review.IAtsAbstractReview;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.RuleDefinitionOption;
import org.eclipse.osee.ats.api.workflow.IAtsTask;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Donald G. Dunne
 */
public class WorkflowManagerCoreTest {

   // @formatter:off
   @Mock private IAtsTeamWorkflow teamWf;
   @Mock private IAtsAbstractReview review;
   @Mock private IAtsTeamDefinition teamDef;
   @Mock private IAtsStateDefinition analyzeState, implementState;
   @Mock private IAtsTask task;
   @Mock private IAtsUser Joe, Mary;
   // @formatter:on
   List<IAtsUser> assignees = new ArrayList<>();

   @Before
   public void setup() {
      MockitoAnnotations.initMocks(this);

      when(teamWf.getTeamDefinition()).thenReturn(teamDef);
      when(review.getParentTeamWorkflow()).thenReturn(teamWf);
      when(teamWf.getStateDefinition()).thenReturn(analyzeState);
      when(analyzeState.getName()).thenReturn("analyze");
      when(implementState.getName()).thenReturn("implement");
      when(teamWf.getAssignees()).thenReturn(assignees);
   }

   @Test
   public void testTeamDefHasRule() {
      WorkflowManagerCore wmc = new WorkflowManagerCore();
      RuleDefinitionOption option = RuleDefinitionOption.AllowEditToAll;

      Assert.assertFalse(wmc.teamDefHasRule(teamWf, option));
      Assert.assertFalse(wmc.teamDefHasRule(task, option));
      Assert.assertFalse(wmc.teamDefHasRule(review, option));

      when(teamDef.hasRule(RuleDefinitionOption.AllowEditToAll.name())).thenReturn(true);

      Assert.assertTrue(wmc.teamDefHasRule(teamWf, option));
      Assert.assertTrue(wmc.teamDefHasRule(review, option));
      Assert.assertFalse(wmc.teamDefHasRule(task, option));

      when(review.getParentTeamWorkflow()).thenReturn(null);
      Assert.assertFalse(wmc.teamDefHasRule(review, option));
   }

   @Test
   public void testIsWorkItemEditable() {
      WorkflowManagerCore wmc = new WorkflowManagerCore();

      // current state equals state
      Assert.assertFalse(wmc.isWorkItemEditable(teamWf, null, false, Mary, false));
      Assert.assertFalse(wmc.isWorkItemEditable(teamWf, analyzeState, false, Mary, false));
      Assert.assertTrue(wmc.isWorkItemEditable(teamWf, analyzeState, false, Mary, true));
      Assert.assertFalse(wmc.isWorkItemEditable(teamWf, implementState, false, Mary, true));

      // assignee is current user
      assignees.add(Mary);
      Assert.assertTrue(wmc.isWorkItemEditable(teamWf, analyzeState, false, Mary, false));
      assignees.add(Joe);
      Assert.assertTrue(wmc.isWorkItemEditable(teamWf, analyzeState, false, Mary, false));
      assignees.remove(Mary);
      Assert.assertFalse(wmc.isWorkItemEditable(teamWf, analyzeState, false, Mary, false));

      // isAtsAdmin
      assignees.clear();
      Assert.assertTrue(wmc.isWorkItemEditable(teamWf, analyzeState, false, Mary, true));

      // privilegedEditEnabled
      assignees.clear();
      Assert.assertTrue(wmc.isWorkItemEditable(teamWf, analyzeState, true, Mary, false));

      // state has rule
      when(analyzeState.hasRule(RuleDefinitionOption.AllowEditToAll.name())).thenReturn(false);
      Assert.assertFalse(wmc.isWorkItemEditable(teamWf, analyzeState, false, Mary, false));
      when(analyzeState.hasRule(RuleDefinitionOption.AllowEditToAll.name())).thenReturn(true);
      Assert.assertTrue(wmc.isWorkItemEditable(teamWf, analyzeState, false, Mary, false));
      when(analyzeState.hasRule(RuleDefinitionOption.AllowEditToAll.name())).thenReturn(false);

      // teamDef has rule
      when(teamDef.hasRule(RuleDefinitionOption.AllowEditToAll.name())).thenReturn(true);
      Assert.assertTrue(wmc.isWorkItemEditable(teamWf, analyzeState, false, Mary, false));
      when(teamDef.hasRule(RuleDefinitionOption.AllowEditToAll.name())).thenReturn(false);
      Assert.assertFalse(wmc.isWorkItemEditable(teamWf, analyzeState, false, Mary, false));

      // statics
      assignees.add(Mary);
      Assert.assertTrue(WorkflowManagerCore.isEditable(teamWf, analyzeState, false, Mary, false));
   }
}
