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
package org.eclipse.osee.ats.client.integration.tests.ats.core.client.review;

import java.util.Date;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.ats.client.integration.tests.ats.core.client.AtsTestUtil;
import org.eclipse.osee.ats.core.client.review.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.core.client.review.PeerToPeerReviewManager;
import org.eclipse.osee.ats.core.client.review.PeerToPeerReviewState;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.artifact.search.QueryOptions;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

/**
 * Test unit for {@link PeerToPeerReviewManager}
 * 
 * @author Donald G. Dunne
 */
public class PeerToPeerReviewManagerTest extends PeerToPeerReviewManager {

   @BeforeClass
   @AfterClass
   public static void cleanup() throws Exception {
      AtsTestUtil.cleanup();
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtilCore.getAtsBranch(),
            PeerToPeerReviewManagerTest.class.getSimpleName());
      for (Artifact art : ArtifactQuery.getArtifactListFromTypeAndName(AtsArtifactTypes.PeerToPeerReview,
         "PeerToPeerReviewManagerTest", AtsUtil.getAtsBranchToken(), QueryOptions.CONTAINS_MATCH_OPTIONS)) {
         if (art.getName().contains("StandAlone")) {
            art.deleteAndPersist(transaction);
         }
      }
      transaction.execute();
   }

   @org.junit.Test
   public void testCreateNewPeerToPeerReview__Base() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset("PeerToPeerReviewManagerTest - Base");
      TeamWorkFlowArtifact teamArt = AtsTestUtil.getTeamWf();

      // create and transition peer review
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtilCore.getAtsBranch(), getClass().getSimpleName());
      String reviewTitle = "Test Review - " + teamArt.getName();

      PeerToPeerReviewArtifact peerArt =
         PeerToPeerReviewManager.createNewPeerToPeerReview(teamArt, reviewTitle,
            AtsTestUtil.getAnalyzeStateDef().getName(), new Date(),
            AtsClientService.get().getUserAdmin().getCurrentUser(), transaction);
      transaction.execute();

      Assert.assertNotNull(peerArt);
      Assert.assertFalse(
         String.format("PeerToPeer Review artifact should not be dirty [%s]", Artifacts.getDirtyReport(peerArt)),
         peerArt.isDirty());
      Assert.assertEquals(PeerToPeerReviewState.Prepare.getName(), peerArt.getCurrentStateName());
      Assert.assertEquals("Joe Smith", peerArt.getStateMgr().getAssigneesStr());
      Assert.assertEquals("Joe Smith", peerArt.getCreatedBy().getName());
      Assert.assertEquals(AtsTestUtil.getAnalyzeStateDef().getName(),
         peerArt.getSoleAttributeValue(AtsAttributeTypes.RelatedToState));

   }

   @org.junit.Test
   public void testCreateNewPeerToPeerReview__Simple() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset("PeerToPeerReviewManagerTest - Simple");
      TeamWorkFlowArtifact teamArt = AtsTestUtil.getTeamWf();

      // create and transition peer review
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtilCore.getAtsBranch(), getClass().getSimpleName());
      String reviewTitle = "Test Review - " + teamArt.getName();

      PeerToPeerReviewArtifact peerArt =
         PeerToPeerReviewManager.createNewPeerToPeerReview(teamArt, reviewTitle,
            AtsTestUtil.getAnalyzeStateDef().getName(), transaction);
      transaction.execute();

      Assert.assertNotNull(peerArt);
      Assert.assertFalse(
         String.format("PeerToPeer Review artifact should not be dirty [%s]", Artifacts.getDirtyReport(peerArt)),
         peerArt.isDirty());
      Assert.assertEquals(PeerToPeerReviewState.Prepare.getName(), peerArt.getCurrentStateName());
      Assert.assertEquals("Joe Smith", peerArt.getStateMgr().getAssigneesStr());
      Assert.assertEquals(AtsTestUtil.getAnalyzeStateDef().getName(),
         peerArt.getSoleAttributeValue(AtsAttributeTypes.RelatedToState));

   }

   @org.junit.Test
   public void testCreateNewPeerToPeerReview__StandAlone() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset("PeerToPeerReviewManagerTest - StandAlone");
      IAtsActionableItem testAi = AtsTestUtil.getTestAi();

      // create and transition peer review
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtilCore.getAtsBranch(), getClass().getSimpleName());
      String reviewTitle = "Test Review - " + testAi;

      PeerToPeerReviewArtifact peerArt =
         PeerToPeerReviewManager.createNewPeerToPeerReview(testAi, reviewTitle, null, new Date(),
            AtsClientService.get().getUserAdmin().getCurrentUser(), transaction);
      transaction.execute();

      Assert.assertNotNull(peerArt);
      Assert.assertFalse(
         String.format("PeerToPeer Review artifact should not be dirty [%s]", Artifacts.getDirtyReport(peerArt)),
         peerArt.isDirty());
      Assert.assertEquals(PeerToPeerReviewState.Prepare.getName(), peerArt.getCurrentStateName());
      Assert.assertEquals("Joe Smith", peerArt.getStateMgr().getAssigneesStr());
      Assert.assertEquals(peerArt.getSoleAttributeValue(AtsAttributeTypes.ActionableItem), testAi.getGuid());
   }

}
