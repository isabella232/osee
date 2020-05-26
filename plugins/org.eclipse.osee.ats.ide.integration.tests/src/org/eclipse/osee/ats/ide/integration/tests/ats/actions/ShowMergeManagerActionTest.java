/*********************************************************************
 * Copyright (c) 2013 Boeing
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

package org.eclipse.osee.ats.ide.integration.tests.ats.actions;

import org.eclipse.osee.ats.ide.actions.ShowMergeManagerAction;
import org.eclipse.osee.ats.ide.integration.tests.ats.workflow.AtsTestUtil;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.logging.SevereLoggingMonitor;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
@Ignore
public class ShowMergeManagerActionTest extends AbstractAtsActionRunTest {

   private static BranchId createdBranch = null;

   @After
   public void cleanup_closeMergeView() {
      if (createdBranch != null) {
         BranchManager.deleteBranchAndPend(createdBranch);
      }
   }

   @Override
   public ShowMergeManagerAction createAction() {
      AtsTestUtil.cleanupAndReset(getClass().getSimpleName());
      Result result = AtsTestUtil.createWorkingBranchFromTeamWf();
      createdBranch = AtsTestUtil.getTeamWf().getWorkingBranch();
      Assert.assertTrue(result.getText(), result.isTrue());

      ShowMergeManagerAction action = new ShowMergeManagerAction(AtsTestUtil.getTeamWf());
      return action;
   }

   @Override
   @Test
   public void getImageDescriptor() throws Exception {
      SevereLoggingMonitor monitor = TestUtil.severeLoggingStart();
      AtsTestUtil.cleanupAndReset(getClass().getSimpleName());
      ShowMergeManagerAction action = new ShowMergeManagerAction(AtsTestUtil.getTeamWf());
      Assert.assertNotNull("Image should be specified", action.getImageDescriptor());
      TestUtil.severeLoggingEnd(monitor);
   }

}
