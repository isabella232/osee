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
package org.eclipse.osee.ats.core.workflow.state;

import org.eclipse.osee.ats.api.workdef.StateType;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test Unit for {@link TeamState}
 * 
 * @author Donald G. Dunne
 */
public class TeamStateTest {

   @Test
   public void test() {
      Assert.assertEquals(StateType.Working, TeamState.Endorse.getStateType());
      Assert.assertEquals("Endorse", TeamState.Endorse.getName());
      Assert.assertEquals(StateType.Working, TeamState.Analyze.getStateType());
      Assert.assertEquals("Analyze", TeamState.Analyze.getName());
      Assert.assertEquals(StateType.Working, TeamState.Authorize.getStateType());
      Assert.assertEquals("Authorize", TeamState.Authorize.getName());
      Assert.assertEquals(StateType.Working, TeamState.Implement.getStateType());
      Assert.assertEquals("Implement", TeamState.Implement.getName());
      Assert.assertEquals(StateType.Completed, TeamState.Completed.getStateType());
      Assert.assertEquals("Completed", TeamState.Completed.getName());
      Assert.assertEquals(StateType.Cancelled, TeamState.Cancelled.getStateType());
      Assert.assertEquals("Cancelled", TeamState.Cancelled.getName());
   }

   @Test
   public void testValueOf() {
      Assert.assertEquals(TeamState.Analyze, TeamState.valueOf("Analyze"));
   }

   @Test
   public void testValues() {
      Assert.assertEquals(6, TeamState.values().size());
   }

}
