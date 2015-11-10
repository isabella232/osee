/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
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
import static org.eclipse.osee.framework.core.enums.DemoBranches.SAW_Bld_1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.client.integration.tests.integration.skynet.core.utils.Asserts;
import org.eclipse.osee.client.test.framework.OseeClientIntegrationRule;
import org.eclipse.osee.client.test.framework.OseeLogMonitorRule;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.PurgeArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.operation.FinishUpdateBranchOperation;
import org.eclipse.osee.framework.skynet.core.artifact.operation.UpdateBranchOperation;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.artifact.update.ConflictResolverOperation;
import org.eclipse.osee.framework.skynet.core.conflict.ConflictManagerExternal;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Roberto E. Escobar
 */
public class BranchStateTest {

   @Rule
   public OseeClientIntegrationRule integration = new OseeClientIntegrationRule(OSEE_CLIENT_DEMO);

   @Rule
   public OseeLogMonitorRule monitorRule = new OseeLogMonitorRule();

   @Before
   public void setUp() throws Exception {
      BranchManager.refreshBranches();
   }

   @Test
   public void testCreateState() throws OseeCoreException {
      String originalBranchName = "Create State Branch";
      Branch workingBranch = null;
      try {
         workingBranch = BranchManager.createWorkingBranch(SAW_Bld_1, originalBranchName);
         assertEquals(BranchState.CREATED, workingBranch.getBranchState());
         assertTrue(BranchManager.isEditable(workingBranch));
      } finally {
         if (workingBranch != null) {
            BranchManager.purgeBranch(workingBranch);
         }
      }
   }

   @Test
   public void testModifiedState() throws OseeCoreException {
      String originalBranchName = "Modified State Branch";
      Branch workingBranch = null;
      try {
         workingBranch = BranchManager.createWorkingBranch(SAW_Bld_1, originalBranchName);
         assertEquals(BranchState.CREATED, workingBranch.getBranchState());
         assertTrue(BranchManager.isEditable(workingBranch));

         Artifact change = ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, workingBranch,
            "Test Object on Working Branch");
         change.persist(getClass().getSimpleName());

         assertEquals(BranchState.MODIFIED, workingBranch.getBranchState());
         assertTrue(BranchManager.isEditable(workingBranch));
      } finally {
         if (workingBranch != null) {
            BranchManager.purgeBranch(workingBranch);
         }
      }
   }

   @Test
   public void testDeleteState() throws OseeCoreException, InterruptedException {
      String originalBranchName = "Deleted State Branch";
      Branch workingBranch = null;
      try {
         workingBranch = BranchManager.createWorkingBranch(SAW_Bld_1, originalBranchName);
         assertEquals(BranchState.CREATED, workingBranch.getBranchState());
         assertTrue(BranchManager.isEditable(workingBranch));

         Job job = BranchManager.deleteBranch(workingBranch);
         job.join();
         assertEquals(BranchState.DELETED, workingBranch.getBranchState());
         assertTrue(BranchManager.isArchived(workingBranch));
         assertTrue(!BranchManager.isEditable(workingBranch));
         assertTrue(workingBranch.getBranchState().isDeleted());
      } finally {
         if (workingBranch != null) {
            // needed to allow for archiving to occur
            Thread.sleep(5000);
            BranchManager.purgeBranch(workingBranch);
         }
      }
   }

   @Test
   public void testPurgeState() throws OseeCoreException, InterruptedException {
      String originalBranchName = "Purged State Branch";
      Branch workingBranch = null;
      boolean branchPurged = false;
      try {
         workingBranch = BranchManager.createWorkingBranch(SAW_Bld_1, originalBranchName);
         assertEquals(BranchState.CREATED, workingBranch.getBranchState());
         assertTrue(BranchManager.isEditable(workingBranch));

         BranchManager.purgeBranch(workingBranch);
         branchPurged = true;

         assertEquals(BranchState.PURGED, workingBranch.getBranchState());
         assertTrue(BranchManager.isArchived(workingBranch));
         assertTrue(!BranchManager.isEditable(workingBranch));
         assertTrue(workingBranch.getBranchState().isPurged());
      } finally {
         if (workingBranch != null && !branchPurged) {
            // needed to allow for archiving to occur
            Thread.sleep(5000);
            BranchManager.purgeBranch(workingBranch);
         }
      }
   }

   @Test
   public void testCommitState() throws OseeCoreException, InterruptedException {
      String originalBranchName = "Commit State Branch";
      Branch workingBranch = null;
      Artifact change = null;
      try {
         workingBranch = BranchManager.createWorkingBranch(SAW_Bld_1, originalBranchName);
         assertEquals(BranchState.CREATED, workingBranch.getBranchState());
         assertTrue(BranchManager.isEditable(workingBranch));

         change =
            ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, workingBranch, "A commit change");
         change.persist(getClass().getSimpleName());

         assertEquals(BranchState.MODIFIED, workingBranch.getBranchState());
         assertTrue(BranchManager.isEditable(workingBranch));

         ConflictManagerExternal conflictManager = new ConflictManagerExternal(SAW_Bld_1, workingBranch);
         BranchManager.commitBranch(null, conflictManager, true, false);

         assertEquals(BranchState.COMMITTED, workingBranch.getBranchState());
         assertTrue(BranchManager.isArchived(workingBranch));
         assertTrue(!BranchManager.isEditable(workingBranch));
      } finally {
         if (workingBranch != null) {
            // needed to allow for archiving to occur
            Thread.sleep(5000);
            BranchManager.purgeBranch(workingBranch);
         }
      }
   }

   @Test
   public void testRebaselineBranchNoChanges() throws Exception {
      String originalBranchName = "UpdateBranch No Changes Test";
      Branch workingBranch = null;
      try {
         workingBranch = BranchManager.createWorkingBranch(SAW_Bld_1, originalBranchName);

         // Update the branch
         ConflictResolverOperation resolverOperation =
            new ConflictResolverOperation("Test 1 Resolver", BranchStateTest.class.getCanonicalName()) {

               @Override
               protected void doWork(IProgressMonitor monitor) throws Exception {
                  assertFalse("This code should not be executed since there shouldn't be any conflicts.",
                     wasExecuted());
               }
            };

         IOperation operation = new UpdateBranchOperation(workingBranch, resolverOperation);
         Asserts.assertOperation(operation, IStatus.OK);

         Assert.assertEquals(BranchState.DELETED, workingBranch.getBranchState());
         Assert.assertEquals(UserManager.getUser(SystemUser.OseeSystem).getArtId(),
            BranchManager.getAssociatedArtifact(workingBranch).getArtId());

         IOseeBranch newWorkingBranch = BranchManager.getBranch(originalBranchName);
         assertTrue(!workingBranch.getUuid().equals(newWorkingBranch.getUuid()));
         assertEquals(originalBranchName, newWorkingBranch.getName());
         assertTrue("New Working branch was not editable", BranchManager.isEditable(newWorkingBranch));
         assertFalse("New Working branch was editable", BranchManager.isEditable(workingBranch));
      } finally {
         cleanup(originalBranchName, workingBranch, null);
      }
   }

   @Test
   public void testRebaselineWithoutConflicts() throws Exception {
      String originalBranchName = "UpdateBranch Test 1";
      Artifact baseArtifact = null;
      Branch workingBranch = null;
      Artifact change = null;
      try {
         baseArtifact =
            ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, SAW_Bld_1, "Test Object");
         baseArtifact.setSoleAttributeFromString(CoreAttributeTypes.Annotation, "This is the base annotation");
         baseArtifact.persist(getClass().getSimpleName());

         workingBranch = BranchManager.createWorkingBranch(SAW_Bld_1, originalBranchName);

         // Add a new artifact on the working branch
         change = ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, workingBranch,
            "Test Object on Working Branch");
         change.persist(getClass().getSimpleName());

         // Make a change on the parent
         baseArtifact.setSoleAttributeFromString(CoreAttributeTypes.Annotation, "This is the updated annotation");
         baseArtifact.persist(getClass().getSimpleName());

         // Update the branch
         ConflictResolverOperation resolverOperation =
            new ConflictResolverOperation("Test 1 Resolver", BranchStateTest.class.getCanonicalName()) {

               @Override
               protected void doWork(IProgressMonitor monitor) throws Exception {
                  assertFalse("This code should not be executed since there shouldn't be any conflicts.",
                     wasExecuted());
               }
            };

         IOperation operation = new UpdateBranchOperation(workingBranch, resolverOperation);
         Asserts.assertOperation(operation, IStatus.OK);
         assertFalse("Resolver was executed", resolverOperation.wasExecuted());

         checkBranchWasRebaselined(originalBranchName, workingBranch);
         // Check that the associated artifact remained unchanged
         assertEquals((long) BranchManager.getAssociatedArtifactId(workingBranch),
            SystemUser.OseeSystem.getUuid().longValue());

         Collection<IOseeBranch> branches = BranchManager.getBranchesByName(originalBranchName);
         assertEquals("Check only 1 original branch", 1, branches.size());

         IOseeBranch newWorkingBranch = branches.iterator().next();
         assertTrue(!workingBranch.getUuid().equals(newWorkingBranch.getUuid()));
         assertEquals(originalBranchName, newWorkingBranch.getName());
         assertTrue("New Working branch is editable", BranchManager.isEditable(newWorkingBranch));
      } finally {
         cleanup(originalBranchName, workingBranch, null, change, baseArtifact);
      }
   }

   @Test
   public void testRebaselineWithConflicts() throws Exception {
      String originalBranchName = "UpdateBranch Test 2";
      Artifact baseArtifact = null;
      Branch workingBranch = null;
      Branch mergeBranch = null;
      Artifact sameArtifact = null;
      try {
         baseArtifact =
            ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, SAW_Bld_1, "Test Object");
         baseArtifact.setSoleAttributeFromString(CoreAttributeTypes.Annotation, "This is the base annotation");
         baseArtifact.persist(getClass().getSimpleName());

         workingBranch = BranchManager.createWorkingBranch(SAW_Bld_1, originalBranchName);

         // Modify same artifact on working branch
         sameArtifact = ArtifactQuery.getArtifactFromId(baseArtifact.getGuid(), workingBranch);
         sameArtifact.setSoleAttributeFromString(CoreAttributeTypes.Annotation,
            "This is the working branch update annotation");
         sameArtifact.persist(getClass().getSimpleName());

         // Make a change on the parent
         baseArtifact.setSoleAttributeFromString(CoreAttributeTypes.Annotation, "This is the updated annotation");
         baseArtifact.persist(getClass().getSimpleName());

         ConflictResolverOperation resolverOperation =
            new ConflictResolverOperation("Test 2 Resolver", BranchStateTest.class.getCanonicalName()) {

               @Override
               protected void doWork(IProgressMonitor monitor) throws Exception {
                  assertTrue("This code should have been executed since there should be conflicts.", wasExecuted());
               }
            };

         IOperation operation = new UpdateBranchOperation(workingBranch, resolverOperation);
         Asserts.assertOperation(operation, IStatus.OK);

         assertTrue("Resolver not executed", resolverOperation.wasExecuted());

         assertTrue("Branch was archived", !BranchManager.isArchived(workingBranch));
         assertTrue("Branch was not marked as rebaseline in progress",
            workingBranch.getBranchState().isRebaselineInProgress());
         assertTrue("Branch was not editable", BranchManager.isEditable(workingBranch));
         assertTrue("Branch state was set to rebaselined before complete",
            !workingBranch.getBranchState().isRebaselined());

         assertEquals("Branch name was changed before update was complete", originalBranchName,
            workingBranch.getName());

         // Check that a new destination branch exists
         Branch destinationBranch = resolverOperation.getConflictManager().getDestinationBranch();
         assertTrue("Branch name not set correctly",
            destinationBranch.getName().startsWith(String.format("%s - for update -", originalBranchName)));
         assertTrue("Branch was not editable", BranchManager.isEditable(destinationBranch));

         // Check that we have a merge branch
         mergeBranch = BranchManager.getMergeBranch(workingBranch, destinationBranch);
         assertTrue("MergeBranch was not editable", BranchManager.isEditable(mergeBranch));
         assertEquals("Merge Branch should be in Created State", BranchState.CREATED, mergeBranch.getBranchState());

         // Run FinishBranchUpdate and check
         FinishUpdateBranchOperation finishUpdateOperation =
            new FinishUpdateBranchOperation(resolverOperation.getConflictManager(), true, true);
         Asserts.assertOperation(finishUpdateOperation, IStatus.OK);

         checkBranchWasRebaselined(originalBranchName, workingBranch);

         Collection<IOseeBranch> branches = BranchManager.getBranchesByName(originalBranchName);
         assertEquals("Check only 1 original branch", 1, branches.size());

         IOseeBranch newWorkingBranch = branches.iterator().next();
         assertTrue(!workingBranch.getUuid().equals(newWorkingBranch.getUuid()));
         assertEquals(originalBranchName, newWorkingBranch.getName());
         assertTrue("New Working branch is editable", BranchManager.isEditable(newWorkingBranch));

         // Swapped successfully
         assertEquals(destinationBranch.getUuid(), newWorkingBranch.getUuid());
      } catch (Exception ex) {
         throw ex;
      } finally {
         cleanup(originalBranchName, workingBranch, mergeBranch, sameArtifact, baseArtifact);

      }
   }

   private void cleanup(String originalBranchName, Branch workingBranch, Branch mergeBranch, Artifact... toDelete) {
      try {
         if (mergeBranch != null) {
            BranchManager.purgeBranch(mergeBranch);
         }
         if (workingBranch != null) {
            purgeBranchAndChildren(workingBranch);
         }
         for (BranchId branch : BranchManager.getBranchesByName(originalBranchName)) {
            purgeBranchAndChildren(branch);
         }
         if (toDelete != null && toDelete.length > 0) {
            Operations.executeWorkAndCheckStatus(new PurgeArtifacts(Arrays.asList(toDelete)));
         }
      } catch (Exception ex) {
         // Do Nothing;
      }
   }

   private void purgeBranchAndChildren(BranchId branch) throws OseeCoreException {
      for (Branch child : BranchManager.getBranch(branch).getChildBranches(true)) {
         BranchManager.purgeBranch(child);
      }
      BranchManager.purgeBranch(branch);
   }

   private void checkBranchWasRebaselined(String originalBranchName, Branch branchToCheck) {
      assertTrue("Branch was not archived", BranchManager.isArchived(branchToCheck));
      assertTrue("Branch was still editable", !BranchManager.isEditable(branchToCheck));
      assertTrue("Branch state was not set as rebaselined", branchToCheck.getBranchState().isRebaselined());
      assertTrue("Branch name not set correctly",
         branchToCheck.getName().startsWith(String.format("%s - moved by update on -", originalBranchName)));
   }

}
