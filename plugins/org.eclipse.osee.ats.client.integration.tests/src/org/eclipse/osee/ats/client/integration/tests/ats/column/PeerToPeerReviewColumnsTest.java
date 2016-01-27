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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.eclipse.osee.ats.api.review.Role;
import org.eclipse.osee.ats.api.review.UserRole;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.ats.client.integration.tests.ats.core.client.AtsTestUtil;
import org.eclipse.osee.ats.client.integration.tests.util.DemoTestUtil;
import org.eclipse.osee.ats.column.ReviewAuthorColumn;
import org.eclipse.osee.ats.column.ReviewModeratorColumn;
import org.eclipse.osee.ats.column.ReviewNumIssuesColumn;
import org.eclipse.osee.ats.column.ReviewNumMajorDefectsColumn;
import org.eclipse.osee.ats.column.ReviewNumMinorDefectsColumn;
import org.eclipse.osee.ats.column.ReviewReviewerColumn;
import org.eclipse.osee.ats.core.client.review.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.core.client.review.PeerToPeerReviewManager;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectItem;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectItem.Disposition;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectItem.InjectionActivity;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectItem.Severity;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectManager;
import org.eclipse.osee.ats.core.client.review.role.UserRoleManager;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.AtsChangeSet;
import org.eclipse.osee.ats.demo.api.DemoUsers;
import org.eclipse.osee.framework.logging.SevereLoggingMonitor;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

/**
 * @tests CancelledDateColumn
 * @author Donald G. Dunne
 */
public class PeerToPeerReviewColumnsTest {

   @AfterClass
   @BeforeClass
   public static void cleanup() throws Exception {
      AtsTestUtil.cleanupSimpleTest(PeerToPeerReviewColumnsTest.class.getSimpleName());
   }

   @org.junit.Test
   public void testGetColumnText() throws Exception {
      SevereLoggingMonitor loggingMonitor = TestUtil.severeLoggingStart();
      AtsChangeSet changes = new AtsChangeSet(PeerToPeerReviewColumnsTest.class.getSimpleName());

      TeamWorkFlowArtifact teamArt =
         DemoTestUtil.createSimpleAction(PeerToPeerReviewColumnsTest.class.getSimpleName(), changes);
      PeerToPeerReviewArtifact peerArt = PeerToPeerReviewManager.createNewPeerToPeerReview(teamArt,
         getClass().getSimpleName(), teamArt.getStateMgr().getCurrentStateName(), changes);
      changes.add(peerArt);
      changes.execute();

      Assert.assertEquals("0", ReviewNumIssuesColumn.getInstance().getColumnText(peerArt, null, 0));
      Assert.assertEquals("0", ReviewNumMajorDefectsColumn.getInstance().getColumnText(peerArt, null, 0));
      Assert.assertEquals("0", ReviewNumMinorDefectsColumn.getInstance().getColumnText(peerArt, null, 0));
      Assert.assertEquals("", ReviewAuthorColumn.getInstance().getColumnText(peerArt, null, 0));
      Assert.assertEquals("", ReviewModeratorColumn.getInstance().getColumnText(peerArt, null, 0));
      Assert.assertEquals("", ReviewReviewerColumn.getInstance().getColumnText(peerArt, null, 0));

      changes.clear();
      ReviewDefectItem item = new ReviewDefectItem(AtsClientService.get().getUserService().getCurrentUser(),
         Severity.Issue, Disposition.None, InjectionActivity.Code, "description", "resolution", "location", new Date());
      ReviewDefectManager defectManager = new ReviewDefectManager(peerArt);
      defectManager.addOrUpdateDefectItem(item);
      item = new ReviewDefectItem(AtsClientService.get().getUserService().getCurrentUser(), Severity.Issue,
         Disposition.None, InjectionActivity.Code, "description 2", "resolution", "location", new Date());
      defectManager.addOrUpdateDefectItem(item);
      item = new ReviewDefectItem(AtsClientService.get().getUserService().getCurrentUser(), Severity.Issue,
         Disposition.None, InjectionActivity.Code, "description 3", "resolution", "location", new Date());
      defectManager.addOrUpdateDefectItem(item);
      item = new ReviewDefectItem(AtsClientService.get().getUserService().getCurrentUser(), Severity.Issue,
         Disposition.None, InjectionActivity.Code, "description 34", "resolution", "location", new Date());
      defectManager.addOrUpdateDefectItem(item);
      item = new ReviewDefectItem(AtsClientService.get().getUserService().getCurrentUser(), Severity.Major,
         Disposition.None, InjectionActivity.Code, "description 4", "resolution", "location", new Date());
      defectManager.addOrUpdateDefectItem(item);
      item = new ReviewDefectItem(AtsClientService.get().getUserService().getCurrentUser(), Severity.Minor,
         Disposition.None, InjectionActivity.Code, "description 5", "resolution", "location", new Date());
      defectManager.addOrUpdateDefectItem(item);
      item = new ReviewDefectItem(AtsClientService.get().getUserService().getCurrentUser(), Severity.Minor,
         Disposition.None, InjectionActivity.Code, "description 6", "resolution", "location", new Date());
      defectManager.addOrUpdateDefectItem(item);
      item = new ReviewDefectItem(AtsClientService.get().getUserService().getCurrentUser(), Severity.Minor,
         Disposition.None, InjectionActivity.Code, "description 6", "resolution", "location", new Date());
      defectManager.addOrUpdateDefectItem(item);
      defectManager.saveToArtifact(peerArt);

      UserRole role =
         new UserRole(Role.Author, AtsClientService.get().getUserServiceClient().getUserFromToken(DemoUsers.Alex_Kay));
      UserRoleManager roleMgr = new UserRoleManager(peerArt);
      roleMgr.addOrUpdateUserRole(role, peerArt);

      role = new UserRole(Role.Moderator,
         AtsClientService.get().getUserServiceClient().getUserFromToken(DemoUsers.Jason_Michael));
      roleMgr.addOrUpdateUserRole(role, peerArt);

      role = new UserRole(Role.Reviewer,
         AtsClientService.get().getUserServiceClient().getUserFromToken(DemoUsers.Joe_Smith));
      roleMgr.addOrUpdateUserRole(role, peerArt);
      role = new UserRole(Role.Reviewer,
         AtsClientService.get().getUserServiceClient().getUserFromToken(DemoUsers.Kay_Jones));
      roleMgr.addOrUpdateUserRole(role, peerArt);
      roleMgr.saveToArtifact(changes);
      changes.add(peerArt);
      changes.execute();

      Assert.assertEquals("4", ReviewNumIssuesColumn.getInstance().getColumnText(peerArt, null, 0));
      Assert.assertEquals("1", ReviewNumMajorDefectsColumn.getInstance().getColumnText(peerArt, null, 0));
      Assert.assertEquals("3", ReviewNumMinorDefectsColumn.getInstance().getColumnText(peerArt, null, 0));
      Assert.assertEquals(DemoUsers.Alex_Kay.getName(),
         ReviewAuthorColumn.getInstance().getColumnText(peerArt, null, 0));
      Assert.assertEquals(DemoUsers.Jason_Michael.getName(),
         ReviewModeratorColumn.getInstance().getColumnText(peerArt, null, 0));
      List<String> results = Arrays.asList(DemoUsers.Kay_Jones.getName() + "; " + DemoUsers.Joe_Smith.getName(),
         DemoUsers.Joe_Smith.getName() + "; " + DemoUsers.Kay_Jones.getName());
      Assert.assertTrue(results.contains(ReviewReviewerColumn.getInstance().getColumnText(peerArt, null, 0)));

      TestUtil.severeLoggingEnd(loggingMonitor);
   }
}
