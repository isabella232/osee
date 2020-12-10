/*********************************************************************
 * Copyright (c) 2012 Boeing
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

package org.eclipse.osee.client.integration.tests.integration.skynet.core;

import static org.eclipse.osee.client.demo.DemoChoice.OSEE_CLIENT_DEMO;
import static org.eclipse.osee.framework.core.enums.DemoBranches.SAW_Bld_1;
import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.client.test.framework.OseeClientIntegrationRule;
import org.eclipse.osee.client.test.framework.OseeLogMonitorRule;
import org.eclipse.osee.client.test.framework.TestInfo;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.jdk.core.result.XResultData;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.DeleteBranchOperation;
import org.eclipse.osee.framework.ui.skynet.internal.ServiceUtil;
import org.eclipse.osee.orcs.rest.model.BranchEndpoint;
import org.eclipse.osee.orcs.rest.model.NewBranch;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test Case for {@link CreateBranchOperation}
 *
 * @author Ryan D. Brooks
 */
public class CreateBranchOperationTest {

   @Rule
   public OseeClientIntegrationRule integration = new OseeClientIntegrationRule(OSEE_CLIENT_DEMO);

   @Rule
   public OseeLogMonitorRule monitorRule = new OseeLogMonitorRule();

   @Rule
   public TestInfo method = new TestInfo();

   @Test
   public void test_checkPreconditions_AllowWorkingBranchCreationIfDeleted() {
      Artifact folder = ArtifactTypeManager.addArtifact(CoreArtifactTypes.Folder, CoreBranches.COMMON);
      folder.persist("");

      IOseeBranch workingBranch = BranchManager.createWorkingBranch(SAW_Bld_1, method.getQualifiedTestName(), folder);
      BranchManager.setState(workingBranch, BranchState.DELETED);

      IOseeBranch workingBranch2 =
         BranchManager.createWorkingBranch(workingBranch, getName(workingBranch, "child"), folder);

      Operations.executeWorkAndCheckStatus(new DeleteBranchOperation(workingBranch));
      Operations.executeWorkAndCheckStatus(new DeleteBranchOperation(workingBranch2));

      folder.deleteAndPersist(getClass().getSimpleName());
   }

   @Test
   public void test_checkPreconditions_AllowWorkingBranchCreationIfRebaselined() {
      Artifact folder = ArtifactTypeManager.addArtifact(CoreArtifactTypes.Folder, CoreBranches.COMMON);
      folder.persist("");

      IOseeBranch workingBranch = BranchManager.createWorkingBranch(SAW_Bld_1, method.getQualifiedTestName(), folder);
      BranchManager.setState(workingBranch, BranchState.REBASELINED);

      IOseeBranch workingBranch2 =
         BranchManager.createWorkingBranch(workingBranch, getName(workingBranch, "child"), folder);

      Operations.executeWorkAndCheckStatus(new DeleteBranchOperation(workingBranch));
      Operations.executeWorkAndCheckStatus(new DeleteBranchOperation(workingBranch2));

      folder.deleteAndPersist(getClass().getSimpleName());
   }

   /**
    * Expecting OseeStateException server-side, forwarding XResultData errors client-side
    */
   @Test
   public void test_checkPreconditions_DisallowWorkingBranchCreation() {
      Set<BranchState> subset = new HashSet<>(Arrays.asList(BranchState.values()));

      Collection<BranchState> allowedStates = Arrays.asList(BranchState.DELETED, BranchState.REBASELINED);
      subset.removeAll(allowedStates);

      int errorsCaught = 0;
      for (BranchState state : subset) {
         Artifact folder = ArtifactTypeManager.addArtifact(CoreArtifactTypes.Folder, CoreBranches.COMMON);
         folder.persist("");

         IOseeBranch workingBranch =
            BranchManager.createWorkingBranch(SAW_Bld_1, method.getQualifiedTestName(), folder);
         BranchManager.setState(workingBranch, state);

         BranchEndpoint branchEndpoint = ServiceUtil.getOseeClient().getBranchEndpoint();

         NewBranch newBranch = new NewBranch();
         newBranch.setBranchName(getClass().getSimpleName());
         newBranch.setAssociatedArtifact(folder);
         newBranch.setParentBranchId(workingBranch);
         newBranch.setBranchType(BranchType.WORKING);
         newBranch.setSourceTransactionId(TransactionToken.SENTINEL);

         XResultData rd = null;
         try {
            rd = branchEndpoint.createBranchValidation(newBranch);
         } catch (Exception ex) {
            //Do nothing.
         } finally {
            if (rd == null || rd.isErrors()) {
               errorsCaught++;
               Operations.executeWorkAndCheckStatus(new DeleteBranchOperation(workingBranch));
               folder.deleteAndPersist(getClass().getSimpleName());
            }
         }
         continue;
      }

      String errorMessage = String.format("CreateBranchOperation.checkPreconditions() should throw [%s] exceptions",
         subset.size() - allowedStates.size());
      assertEquals(errorMessage, errorsCaught, subset.size());
   }

   private String getName(IOseeBranch branch, String value) {
      return String.format("%s_%s", branch.getName(), value);
   }

}
