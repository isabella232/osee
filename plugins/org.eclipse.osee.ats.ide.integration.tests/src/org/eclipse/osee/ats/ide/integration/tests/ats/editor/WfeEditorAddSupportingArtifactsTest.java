/*********************************************************************
 * Copyright (c) 2016 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.ats.ide.integration.tests.ats.editor;

import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import java.util.Arrays;
import org.eclipse.osee.ats.ide.editor.tab.workflow.header.WfeEditorAddSupportingArtifacts;
import org.eclipse.osee.ats.ide.integration.tests.ats.workflow.AtsTestUtil;
import org.eclipse.osee.ats.ide.workflow.teamwf.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.core.enums.CoreArtifactTokens;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link WfeEditorAddSupportingArtifacts}
 *
 * @author Donald G. Dunne
 */
public class WfeEditorAddSupportingArtifactsTest {

   @After
   public void cleanup() {
      AtsTestUtil.cleanup();
   }

   @Test
   public void testValidateAndRun() {
      AtsTestUtil.cleanupAndReset(getClass().getSimpleName());
      TeamWorkFlowArtifact teamWf = AtsTestUtil.getTeamWf();

      Artifact rootArt = ArtifactQuery.getArtifactFromId(CoreArtifactTokens.DefaultHierarchyRoot, COMMON);
      Artifact firstArt = null, secondArt = null;
      for (Artifact child : rootArt.getChildren()) {
         if (firstArt == null) {
            firstArt = child;
         } else {
            secondArt = child;
            break;
         }
      }

      WfeEditorAddSupportingArtifacts job =
         new WfeEditorAddSupportingArtifacts(teamWf, Arrays.asList(firstArt, secondArt));
      job.validate();
      job.run(null);

      Assert.assertTrue(teamWf.getRelatedArtifacts(CoreRelationTypes.SupportingInfo_SupportingInfo).contains(firstArt));
      Assert.assertTrue(
         teamWf.getRelatedArtifacts(CoreRelationTypes.SupportingInfo_SupportingInfo).contains(secondArt));
   }

}
