/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.client.integration.tests.ats.column;

import org.eclipse.osee.ats.client.demo.DemoWorkType;
import org.eclipse.osee.ats.client.integration.tests.util.DemoTestUtil;
import org.eclipse.osee.ats.column.StateColumn;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.workflow.state.TeamState;
import org.eclipse.osee.framework.logging.SevereLoggingMonitor;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.Assert;

/**
 * @tests StateColumn
 * @author Donald G. Dunne
 */
public class StateColumnTest {

   @org.junit.Test
   public void testGetColumnText() throws Exception {
      SevereLoggingMonitor loggingMonitor = TestUtil.severeLoggingStart();

      TeamWorkFlowArtifact reqArt =
         (TeamWorkFlowArtifact) DemoTestUtil.getUncommittedActionWorkflow(DemoWorkType.Requirements);
      Assert.assertEquals(TeamState.Implement.getName(),
         StateColumn.getInstance().getColumnText(reqArt, StateColumn.getInstance(), 0));

      Artifact actionArt = reqArt.getParentActionArtifact();
      Assert.assertEquals(TeamState.Implement.getName(),
         StateColumn.getInstance().getColumnText(actionArt, StateColumn.getInstance(), 0));

      TestUtil.severeLoggingEnd(loggingMonitor);
   }

}
