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
package org.eclipse.osee.client.integration.tests.integration.skynet.core;

import static org.eclipse.osee.client.demo.DemoChoice.OSEE_CLIENT_DEMO;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.client.demo.DemoBranches;
import org.eclipse.osee.client.test.framework.OseeClientIntegrationRule;
import org.eclipse.osee.client.test.framework.OseeLogMonitorRule;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.data.IArtifactToken;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.MergeBranch;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.PurgeArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.operation.FinishUpdateBranchOperation;
import org.eclipse.osee.framework.skynet.core.artifact.operation.UpdateBranchOperation;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.artifact.update.ConflictResolverOperation;
import org.eclipse.osee.framework.skynet.core.conflict.ConflictManagerExternal;
import org.eclipse.osee.framework.ui.skynet.commandHandlers.branch.commit.CommitHandler;
import org.eclipse.osee.framework.ui.skynet.util.MergeInProgressHandler;
import org.eclipse.osee.framework.ui.skynet.util.RebaselineInProgressHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

/**
 * @author Angel Avila
 */
public class MergeManagerTest {

   @Rule
   public OseeClientIntegrationRule integration = new OseeClientIntegrationRule(OSEE_CLIENT_DEMO);

   @Rule
   public OseeLogMonitorRule monitorRule = new OseeLogMonitorRule();

   private Branch workingBranch;
   private static Artifact newArt;
   public static IArtifactToken NewArtifactToken = TokenFactory.createArtifactToken("AAABER+3yR4A8O7WYQ+Xaw",
      "ART_NAME", CoreArtifactTypes.SoftwareRequirement);

   private static final int DELETE_MERGE = 2;

   @Before
   public void setUp() throws Exception {
      MockitoAnnotations.initMocks(this);

      if (newArt == null) {
         assertFalse("This test can not be run on Production", ClientSessionManager.isProductionDataStore());
         newArt = ArtifactTypeManager.addArtifact(NewArtifactToken, DemoBranches.SAW_Bld_1);
         newArt.addAttribute(CoreAttributeTypes.WordTemplateContent, "Base Edit");
         newArt.persist("Base Edit");
         //wait for creation of artifact and persist to go through
         Thread.sleep(5000);
      }

      workingBranch = BranchManager.createWorkingBranch(DemoBranches.SAW_Bld_1, "Working Branch");
      // wait for branch creation
      Thread.sleep(3000);
      Artifact artOnWorking = ArtifactQuery.getArtifactFromToken(NewArtifactToken, workingBranch);
      artOnWorking.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent, "Working Edit");
      artOnWorking.persist("Working Edit");

      // Create conflict by editing on Parent as well
      newArt.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent, "Parent Edit");
      newArt.persist("Parent Edit");
      // wait for persists
      Thread.sleep(1000);
   }

   @After
   public void tearDown() throws OseeCoreException, InterruptedException {
      List<MergeBranch> mergeBranches = BranchManager.getMergeBranches(workingBranch);
      for (MergeBranch mergeBranch : mergeBranches) {
         BranchManager.purgeBranch(mergeBranch);
      }

      Artifact artOnWorking = ArtifactQuery.getArtifactFromToken(NewArtifactToken, workingBranch);
      Operations.executeWorkAndCheckStatus(new PurgeArtifacts(Collections.singleton(artOnWorking)));

      BranchManager.purgeBranch(workingBranch);
      Thread.sleep(1000);
   }

   @Test
   public void testRebaselineWithConflictsAbandon() throws Exception {

      ConflictResolverOperation resolverOperation =
         new ConflictResolverOperation("Test Resolver", MergeManagerTest.class.getCanonicalName()) {

            @Override
            protected void doWork(IProgressMonitor monitor) throws Exception {
               assertTrue("This code should have been executed since there should be conflicts.", wasExecuted());
            }
         };
      UpdateBranchOperation update = new UpdateBranchOperation(workingBranch, resolverOperation);
      Operations.executeWorkAndCheckStatus(update);

      assertTrue("No Merge Branch was created", BranchManager.getMergeBranches(workingBranch).size() == 1);
      assertTrue("Branch is not in Rebaseline In Progress", workingBranch.getBranchState().isRebaselineInProgress());

      // Shouldn't be allowed to commit
      boolean committed =
         CommitHandler.commitBranch(new ConflictManagerExternal(DemoBranches.SAW_Bld_2, workingBranch), false, true);
      assertTrue("Branch Committed while in Rebaseline In Progress", !committed);
      assertTrue("An additional Merge Branch was created", BranchManager.getMergeBranches(workingBranch).size() == 1);

      // Abandon 
      RebaselineInProgressHandler.cancelCurrentUpdate(workingBranch, true);
      // wait on operation
      Thread.sleep(1000);
      BranchManager.persist(workingBranch);

      // Now we can commit
      committed =
         CommitHandler.commitBranch(new ConflictManagerExternal(DemoBranches.SAW_Bld_2, workingBranch), false, true);
      assertTrue("Branch should have been comitted", committed);

      // make sure we can't rebase now since we've done a commit
      update = new UpdateBranchOperation(workingBranch, resolverOperation);
      Operations.executeWorkAndCheckStatus(update);
      assertTrue(
         "Branch should not be updating",
         !workingBranch.getBranchState().isRebaselineInProgress() && !workingBranch.getBranchState().isRebaselineInProgress());

      // Purge art from SAW 2 since we did a commit
      Artifact artOnSaw2 = ArtifactQuery.getArtifactFromToken(NewArtifactToken, DemoBranches.SAW_Bld_2);
      Operations.executeWorkAndCheckStatus(new PurgeArtifacts(Collections.singleton(artOnSaw2)));
      // wait on operation
      Thread.sleep(1000);

   }

   @Test
   public void testMultipleRebaselineRequests() throws Exception {
      ConflictResolverOperation resolverOperation =
         new ConflictResolverOperation("Test Resolver", MergeManagerTest.class.getCanonicalName()) {

            @Override
            protected void doWork(IProgressMonitor monitor) throws Exception {
               assertTrue("This code should have been executed since there should be conflicts.", wasExecuted());
            }
         };

      UpdateBranchOperation update = new UpdateBranchOperation(workingBranch, resolverOperation);
      Operations.executeWorkAndCheckStatus(update);

      List<MergeBranch> mergeBranches = BranchManager.getMergeBranches(workingBranch);
      Branch branchForUpdate = mergeBranches.get(0).getDestinationBranch();
      assertTrue("No Merge Branch was created", mergeBranches.size() == 1);
      assertTrue("Branch is not in Rebaseline In Progress", workingBranch.getBranchState().isRebaselineInProgress());

      // Try doing another Rebaseline, no addtional branches should be created
      UpdateBranchOperation update2 = new UpdateBranchOperation(workingBranch, resolverOperation);
      Operations.executeWorkAndCheckStatus(update2);

      List<MergeBranch> mergeBranchesSecondAttempt = BranchManager.getMergeBranches(workingBranch);
      Branch branchForUpdateSecondAttempt = mergeBranchesSecondAttempt.get(0).getDestinationBranch();
      assertTrue("Branch is not in Rebaseline In Progress", workingBranch.getBranchState().isRebaselineInProgress());
      assertTrue("Addional Merge Branch was created during second rebaseline attempt", mergeBranches.size() == 1);
      assertTrue("Addional Branch for Update was created during second rebaseline attempt",
         branchForUpdate.equals(branchForUpdateSecondAttempt));

      // Clean up this test, mainly the update branch
      RebaselineInProgressHandler.cancelCurrentUpdate(workingBranch, true);
   }

   @Test
   public void testRebaselineWithConflictsFinish() throws Exception {
      ConflictResolverOperation resolverOperation =
         new ConflictResolverOperation("Test Resolver", MergeManagerTest.class.getCanonicalName()) {

            @Override
            protected void doWork(IProgressMonitor monitor) throws Exception {
               assertTrue("This code should have been executed since there should be conflicts.", wasExecuted());
            }
         };

      UpdateBranchOperation update = new UpdateBranchOperation(workingBranch, resolverOperation);
      Operations.executeWorkAndCheckStatus(update);

      Branch branchForUpdate = BranchManager.getFirstMergeBranch(workingBranch).getDestinationBranch(); // this will be future working branch

      // Shouldn't be allowed to commit working branch
      boolean committed =
         CommitHandler.commitBranch(new ConflictManagerExternal(DemoBranches.SAW_Bld_2, workingBranch), false, true);
      assertTrue("Branch Committed while in Rebaseline In Progress", !committed);

      // Finish Rebaseline
      FinishUpdateBranchOperation finishUpdateOperation =
         new FinishUpdateBranchOperation(resolverOperation.getConflictManager(), true, true);
      Operations.executeWorkAndCheckStatus(finishUpdateOperation);

      // Make sure the state is now Rebaselined
      assertTrue("Branch is not in Rebaselined", workingBranch.getBranchState().isRebaselined());

      // Shouldn't be allowed to commit original working branch
      committed =
         CommitHandler.commitBranch(new ConflictManagerExternal(DemoBranches.SAW_Bld_2, workingBranch), false, true);
      assertTrue("Branch Committed after in Rebaseline was finished", !committed);

      // Should be allowed to commit to new working branch
      committed =
         CommitHandler.commitBranch(new ConflictManagerExternal(DemoBranches.SAW_Bld_2, branchForUpdate), false, true);
      assertTrue("Branch was not committed into new, rebaselined working branch", committed);

      // Clean up this test
      // Purge art from new Updated Branch
      Artifact artOnSaw2 = ArtifactQuery.getArtifactFromToken(NewArtifactToken, DemoBranches.SAW_Bld_2);
      Artifact artOnUpdateBranch = ArtifactQuery.getArtifactFromToken(NewArtifactToken, branchForUpdate);
      Operations.executeWorkAndCheckStatus(new PurgeArtifacts(Arrays.asList(artOnSaw2, artOnUpdateBranch)));
      // wait on operation
      Thread.sleep(1000);
      BranchManager.purgeBranch(branchForUpdate);
   }

   @Test
   public void testCommitWithMergeAbandon() throws Exception {
      ConflictResolverOperation resolverOperation =
         new ConflictResolverOperation("Test Resolver", MergeManagerTest.class.getCanonicalName()) {

            @Override
            protected void doWork(IProgressMonitor monitor) throws Exception {
               assertTrue("This code should have been executed since there should be conflicts.", wasExecuted());
            }
         };

      // Can't commit since there are conflicts
      boolean committed =
         CommitHandler.commitBranch(new ConflictManagerExternal(DemoBranches.SAW_Bld_1, workingBranch), false, true);
      assertTrue("Branch Committed with unresolved conflicts", !committed);

      List<MergeBranch> mergeBranches = BranchManager.getMergeBranches(workingBranch);
      assertTrue("Exactly one Merge Branch was not found", mergeBranches.size() == 1);

      MergeBranch mergeBranchFromFirstCommit = mergeBranches.get(0);
      assertTrue("Merge Branch is not for working branch",
         mergeBranchFromFirstCommit.getSourceBranch().equals(workingBranch));

      // Try Doing commit again, no new Merge Branches should be created
      boolean committed2 =
         CommitHandler.commitBranch(new ConflictManagerExternal(DemoBranches.SAW_Bld_1, workingBranch), false, true);
      assertTrue("Branch Committed with unresolved conflicts", !committed2);

      List<MergeBranch> mergeBranches2 = BranchManager.getMergeBranches(workingBranch);
      assertTrue("Exactly one Merge Branch was not found", mergeBranches2.size() == 1);

      MergeBranch mergeBranchFromSecondCommit = mergeBranches2.get(0);
      assertTrue("Merge Branches are not equal", mergeBranchFromSecondCommit.equals(mergeBranchFromFirstCommit));

      // Shouldn't not be able to update from parent since we are in the process of handling a Merge from a Commit
      UpdateBranchOperation update = new UpdateBranchOperation(workingBranch, resolverOperation);
      Operations.executeWorkAndCheckStatus(update);

      assertTrue(
         "Branch should not be updating",
         !workingBranch.getBranchState().isRebaselineInProgress() && !workingBranch.getBranchState().isRebaselineInProgress());

      // Abandon 
      MergeInProgressHandler.handleCommitInProgressPostPrompt(new ConflictManagerExternal(DemoBranches.SAW_Bld_1,
         workingBranch), DELETE_MERGE, true);
      assertTrue("Merge Branch still present", !BranchManager.hasMergeBranches(workingBranch));

      // Now we should be to do an update
      UpdateBranchOperation update2 = new UpdateBranchOperation(workingBranch, resolverOperation);
      Operations.executeWorkAndCheckStatus(update2);

      assertTrue("Branch is not updating", workingBranch.getBranchState().isRebaselineInProgress());

      Branch branchForUpdate = BranchManager.getFirstMergeBranch(workingBranch);

      FinishUpdateBranchOperation finishUpdateOperation =
         new FinishUpdateBranchOperation(resolverOperation.getConflictManager(), true, true);
      Operations.executeWorkAndCheckStatus(finishUpdateOperation);

      // Make sure the state is now Rebaselined
      assertTrue("Branch is not in Rebaselined", workingBranch.getBranchState().isRebaselined());

      // Clean up this test
      // Purge art from new Updated Branch
      Artifact artOnUpdateBranch = ArtifactQuery.getArtifactFromToken(NewArtifactToken, branchForUpdate);
      Operations.executeWorkAndCheckStatus(new PurgeArtifacts(Arrays.asList(artOnUpdateBranch)));
      // wait on operation
      Thread.sleep(1000);
      BranchManager.purgeBranch(branchForUpdate);
   }

   @Test
   public void testCommitWithMergeFinish() throws Exception {
      ConflictResolverOperation resolverOperation =
         new ConflictResolverOperation("Test Resolver", MergeManagerTest.class.getCanonicalName()) {

            @Override
            protected void doWork(IProgressMonitor monitor) throws Exception {
               assertTrue("This code should have been executed since there should be conflicts.", wasExecuted());
            }
         };

      // Try committing into SAW BLD 1
      boolean committed =
         CommitHandler.commitBranch(new ConflictManagerExternal(DemoBranches.SAW_Bld_1, workingBranch), false, true);
      assertTrue("Branch Committed with unresolved conflicts", !committed);

      // Shouldn't be able to rebase
      UpdateBranchOperation update = new UpdateBranchOperation(workingBranch, resolverOperation);
      Operations.executeWorkAndCheckStatus(update);

      assertTrue(
         "Branch should not be updating",
         !workingBranch.getBranchState().isRebaselineInProgress() && !workingBranch.getBranchState().isRebaselineInProgress());

      // Commit into another branch other than SAW_BLD_1 so there are no conflicts
      committed =
         CommitHandler.commitBranch(new ConflictManagerExternal(DemoBranches.SAW_Bld_2, workingBranch), false, true);
      assertTrue("Branch was not committed", committed);

      // Even if I abandon first Merge, still shouldn't be able to rebase since I already completed on Commit
      MergeInProgressHandler.handleCommitInProgressPostPrompt(new ConflictManagerExternal(DemoBranches.SAW_Bld_1,
         workingBranch), DELETE_MERGE, true);

      update = new UpdateBranchOperation(workingBranch, resolverOperation);
      Operations.executeWorkAndCheckStatus(update);

      assertTrue(
         "Branch should not be updating",
         !workingBranch.getBranchState().isRebaselineInProgress() && !workingBranch.getBranchState().isRebaselineInProgress());

      // Clean up this test
      Artifact artOnSaw2 = ArtifactQuery.getArtifactFromToken(NewArtifactToken, DemoBranches.SAW_Bld_2);
      Operations.executeWorkAndCheckStatus(new PurgeArtifacts(Arrays.asList(artOnSaw2)));
      // wait on operation
      Thread.sleep(1000);
   }
}
