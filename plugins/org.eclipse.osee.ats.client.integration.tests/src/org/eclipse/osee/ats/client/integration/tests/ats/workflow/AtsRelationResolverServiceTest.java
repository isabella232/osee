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
package org.eclipse.osee.ats.client.integration.tests.ats.workflow;

import java.util.Collection;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.data.AtsArtifactToken;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.workdef.IRelationResolver;
import org.eclipse.osee.ats.api.workflow.IAtsTask;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.client.demo.DemoArtifactToken;
import org.eclipse.osee.ats.client.demo.DemoUtil;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class AtsRelationResolverServiceTest {

   private static TeamWorkFlowArtifact sawCodeCommittedWf;
   private static Artifact topAi;
   private static IAtsServices services;
   private static IRelationResolver relationResolver;
   private static TeamWorkFlowArtifact sawCodeUnCommittedWf;

   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
      services = AtsClientService.get();
      sawCodeCommittedWf = DemoUtil.getSawCodeCommittedWf();
      sawCodeUnCommittedWf = DemoUtil.getSawCodeUnCommittedWf();
      topAi = (Artifact) services.getArtifact(AtsArtifactToken.TopActionableItem);
      relationResolver = services.getRelationResolver();
   }

   @Test
   public void testGetRelatedArtifactIdIRelationTypeSide() {
      Assert.assertEquals(6, relationResolver.getRelated(topAi, CoreRelationTypes.Default_Hierarchical__Child).size());
   }

   @Test
   public void testGetRelatedIAtsObjectIRelationTypeSideClassOfT() {
      Assert.assertEquals(8,
         relationResolver.getRelated(sawCodeCommittedWf, AtsRelationTypes.TeamWfToTask_Task).size());
   }

   @Test
   public void testAreRelatedArtifactIdIRelationTypeSideArtifactId() {
      Artifact sawCsciAi = (Artifact) services.getArtifact(DemoArtifactToken.SAW_CSCI_AI);
      Assert.assertTrue(relationResolver.areRelated(topAi, CoreRelationTypes.Default_Hierarchical__Child, sawCsciAi));
      Assert.assertTrue(relationResolver.areRelated(sawCsciAi, CoreRelationTypes.Default_Hierarchical__Parent, topAi));

      Artifact sawTestAi = (Artifact) services.getArtifact(DemoArtifactToken.SAW_Test_AI);
      Assert.assertFalse(relationResolver.areRelated(topAi, CoreRelationTypes.Default_Hierarchical__Child, sawTestAi));
      Assert.assertFalse(relationResolver.areRelated(sawTestAi, CoreRelationTypes.Default_Hierarchical__Parent, topAi));
   }

   @Test
   public void testAreRelatedIAtsObjectIRelationTypeSideIAtsObject() {
      Collection<ArtifactId> related =
         relationResolver.getRelated(sawCodeCommittedWf, AtsRelationTypes.TeamWfToTask_Task);
      ArtifactId firstTask = related.iterator().next();

      Assert.assertTrue(relationResolver.areRelated(sawCodeCommittedWf, AtsRelationTypes.TeamWfToTask_Task, firstTask));
      Assert.assertTrue(
         relationResolver.areRelated(firstTask, AtsRelationTypes.TeamWfToTask_TeamWf, sawCodeCommittedWf));

      // get task from un-related workflow
      Collection<ArtifactId> unRelated =
         relationResolver.getRelated(sawCodeUnCommittedWf, AtsRelationTypes.TeamWfToTask_Task);
      ArtifactId firstUnRelatedTask = unRelated.iterator().next();

      Assert.assertFalse(
         relationResolver.areRelated(sawCodeCommittedWf, AtsRelationTypes.TeamWfToTask_Task, firstUnRelatedTask));
      Assert.assertFalse(
         relationResolver.areRelated(firstUnRelatedTask, AtsRelationTypes.TeamWfToTask_TeamWf, sawCodeCommittedWf));
   }

   @Test
   public void testGetRelatedOrNullArtifactIdIRelationTypeSide() {
      ArtifactId sawTestAi = services.getArtifact(DemoArtifactToken.SAW_Test_AI);
      ArtifactId relatedOrNull =
         relationResolver.getRelatedOrNull(sawTestAi, CoreRelationTypes.Default_Hierarchical__Parent);
      Assert.assertNotNull(relatedOrNull);

      ArtifactId nullParentId =
         relationResolver.getRelatedOrNull(sawCodeCommittedWf, CoreRelationTypes.Default_Hierarchical__Parent);
      Assert.assertNull(nullParentId);
   }

   @Test
   public void testGetRelatedOrNullIAtsObjectIRelationTypeSideClassOfT() {
      Collection<ArtifactId> related =
         relationResolver.getRelated(sawCodeCommittedWf, AtsRelationTypes.TeamWfToTask_Task);
      ArtifactId firstTaskArt = related.iterator().next();
      IAtsTask firstTask = services.getWorkItemFactory().getTask(firstTaskArt);

      IAtsTeamWorkflow teamWf =
         relationResolver.getRelatedOrNull(firstTask, AtsRelationTypes.TeamWfToTask_TeamWf, IAtsTeamWorkflow.class);
      Assert.assertNotNull(teamWf);

      IAtsTeamWorkflow nullChild = relationResolver.getRelatedOrNull(firstTask,
         CoreRelationTypes.Default_Hierarchical__Child, IAtsTeamWorkflow.class);
      Assert.assertNull(nullChild);
   }

}
