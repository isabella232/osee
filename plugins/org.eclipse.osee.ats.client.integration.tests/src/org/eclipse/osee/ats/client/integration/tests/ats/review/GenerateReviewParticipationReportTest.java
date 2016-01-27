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
package org.eclipse.osee.ats.client.integration.tests.ats.review;

import java.util.Set;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.ats.demo.api.DemoUsers;
import org.eclipse.osee.ats.review.GenerateReviewParticipationReport;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test Case for {@link GenerateReviewParticipationReport}
 *
 * @author Donald G. Dunne
 */
public class GenerateReviewParticipationReportTest {

   @Test
   public void test() {
      GenerateReviewParticipationReport report = new GenerateReviewParticipationReport(null);
      report.setSelectedUser(AtsClientService.get().getUserServiceClient().getUserFromToken(DemoUsers.Joe_Smith));
      Set<Artifact> results = report.getResults();
      Assert.assertEquals(6, results.size());
      int decRevCount = 0, peerRevCount = 0;
      for (Artifact review : results) {
         if (review.isOfType(AtsArtifactTypes.DecisionReview)) {
            decRevCount++;
         }
         if (review.isOfType(AtsArtifactTypes.PeerToPeerReview)) {
            peerRevCount++;
         }
      }
      Assert.assertEquals(3, decRevCount);
      Assert.assertEquals(3, peerRevCount);
   }

}
