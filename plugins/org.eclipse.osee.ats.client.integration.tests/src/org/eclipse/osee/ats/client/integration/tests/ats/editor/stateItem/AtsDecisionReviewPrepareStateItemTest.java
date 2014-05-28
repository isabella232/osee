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
package org.eclipse.osee.ats.client.integration.tests.ats.editor.stateItem;

import static org.junit.Assert.assertFalse;
import java.util.Arrays;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.workdef.IStateToken;
import org.eclipse.osee.ats.api.workflow.transition.TransitionResults;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.ats.client.integration.tests.ats.core.client.AtsTestUtil;
import org.eclipse.osee.ats.core.client.review.DecisionReviewArtifact;
import org.eclipse.osee.ats.core.client.review.DecisionReviewManager;
import org.eclipse.osee.ats.core.client.review.DecisionReviewState;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.editor.stateItem.AtsDecisionReviewPrepareStateItem;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test Case for {@link AtsDecisionReviewPrepareStateItem}
 * 
 * @author Donald G. Dunne
 */
public class AtsDecisionReviewPrepareStateItemTest {

   public static DecisionReviewArtifact decRevArt;

   @Before
   public void setUp() throws Exception {
      // This test should only be run on test db
      assertFalse("Test should not be run in production db", AtsUtil.isProductionDb());

      if (decRevArt == null) {
         // setup fake review artifact with decision options set
         decRevArt =
            (DecisionReviewArtifact) ArtifactTypeManager.addArtifact(AtsArtifactTypes.DecisionReview,
               AtsUtilCore.getAtsBranch());
         decRevArt.setName(getClass().getSimpleName());
         decRevArt.persist(getClass().getSimpleName());
      }
   }

   @BeforeClass
   @AfterClass
   public static void testCleanup() throws Exception {
      AtsTestUtil.cleanupSimpleTest(AtsDecisionReviewPrepareStateItemTest.class.getSimpleName());
   }

   @Test
   public void testTransitioning() throws OseeCoreException {
      Assert.assertNotNull(decRevArt);

      // set valid options
      String decisionOptionStr =
         DecisionReviewManager.getDecisionReviewOptionsString(DecisionReviewManager.getDefaultDecisionReviewOptions());
      decRevArt.setSoleAttributeValue(AtsAttributeTypes.DecisionReviewOptions, decisionOptionStr);
      decRevArt.persist(getClass().getSimpleName());

      IStateToken fromState = decRevArt.getWorkDefinition().getStateByName(DecisionReviewState.Prepare.getName());
      IStateToken toState = decRevArt.getWorkDefinition().getStateByName(DecisionReviewState.Decision.getName());

      // make call to state item that should set options based on artifact's attribute value
      AtsDecisionReviewPrepareStateItem stateItem = new AtsDecisionReviewPrepareStateItem();
      TransitionResults results = new TransitionResults();
      stateItem.transitioning(results, decRevArt, fromState, toState, Arrays.asList(AtsClientService.get().getUserService().getCurrentUser()));

      // verify no errors
      Assert.assertTrue(results.toString(), results.isEmpty());

      // set invalid options; NoState is invalid, should only be Completed or FollowUp
      decisionOptionStr = decisionOptionStr.replaceFirst("Completed", "NoState");
      decRevArt.setSoleAttributeValue(AtsAttributeTypes.DecisionReviewOptions, decisionOptionStr);
      decRevArt.persist(getClass().getSimpleName());
      stateItem.transitioning(results, decRevArt, fromState, toState, Arrays.asList(AtsClientService.get().getUserService().getCurrentUser()));
      Assert.assertTrue(results.contains("Invalid Decision Option"));

   }
}
