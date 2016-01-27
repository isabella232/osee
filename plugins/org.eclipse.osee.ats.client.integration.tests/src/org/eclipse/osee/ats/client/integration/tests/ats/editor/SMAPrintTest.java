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
package org.eclipse.osee.ats.client.integration.tests.ats.editor;

import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.core.client.review.DecisionReviewArtifact;
import org.eclipse.osee.ats.core.client.review.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.core.client.review.ReviewManager;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.demo.api.DemoArtifactTypes;
import org.eclipse.osee.ats.editor.SMAPrint;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.logging.SevereLoggingMonitor;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.skynet.results.XResultDataUI;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Demo test that will run the SMAPrint action against Demo populated actions to ensure that nothing has broken.This
 * test simply runs the html generation portion of SMAPrint, ensures the results are of a reasonable length and looks
 * for exceptions at the end.
 * 
 * @author Donald G. Dunne
 */
public class SMAPrintTest {

   @Test
   public void testSMAPrint() throws Exception {
      SevereLoggingMonitor monitorLog = TestUtil.severeLoggingStart();

      TeamWorkFlowArtifact teamArt =
         (TeamWorkFlowArtifact) ArtifactQuery.getArtifactFromTypeAndName(DemoArtifactTypes.DemoCodeTeamWorkflow,
            "SAW (uncommitted) More Reqt Changes for Diagram View", AtsUtilCore.getAtsBranch());
      Assert.assertNotNull(teamArt);

      SMAPrint smaPrint = new SMAPrint(teamArt);
      XResultData resultData = smaPrint.getResultData();
      Assert.assertNotNull(resultData);
      // Make sure it's a reasonable length
      Assert.assertTrue(XResultDataUI.getReport(resultData, "report").getManipulatedHtml().length() > 5800);

      PeerToPeerReviewArtifact peerArt = (PeerToPeerReviewArtifact) ReviewManager.getReviews(teamArt).iterator().next();
      smaPrint = new SMAPrint(peerArt);
      resultData = smaPrint.getResultData();
      Assert.assertNotNull(resultData);
      // Make sure it's a reasonable length
      Assert.assertTrue(
         "Expected > 3500 chars, was " + XResultDataUI.getReport(resultData, "report").getManipulatedHtml().length(),
         XResultDataUI.getReport(resultData, "report").getManipulatedHtml().length() > 3500);

      TaskArtifact taskArt = null;
      for (TaskArtifact taskArtifact : teamArt.getTaskArtifacts()) {
         if (taskArtifact.getName().equals("Deploy release")) {
            taskArt = taskArtifact;
         }
      }
      Assert.assertNotNull(taskArt);
      smaPrint = new SMAPrint(taskArt);
      resultData = smaPrint.getResultData();
      Assert.assertNotNull(resultData);
      // Make sure it's a reasonable length
      Assert.assertTrue(XResultDataUI.getReport(resultData, "report").getManipulatedHtml().length() > 2600);

      teamArt = (TeamWorkFlowArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.TeamWorkflow,
         "Button S doesn't work on help", AtsUtilCore.getAtsBranch());
      Assert.assertNotNull(teamArt);
      DecisionReviewArtifact decArt = (DecisionReviewArtifact) ReviewManager.getReviews(teamArt).iterator().next();
      smaPrint = new SMAPrint(decArt);
      resultData = smaPrint.getResultData();
      Assert.assertNotNull(resultData);
      // Make sure it's a reasonable length
      Assert.assertTrue(XResultDataUI.getReport(resultData, "report").getManipulatedHtml().length() > 2900);

      TestUtil.severeLoggingEnd(monitorLog);
   }
}
