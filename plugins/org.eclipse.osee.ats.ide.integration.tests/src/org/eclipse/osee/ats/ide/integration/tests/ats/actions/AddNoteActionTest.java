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

import org.eclipse.jface.action.Action;
import org.eclipse.osee.ats.ide.actions.AddNoteAction;
import org.eclipse.osee.ats.ide.integration.tests.ats.workflow.AtsTestUtil;
import org.eclipse.osee.framework.logging.SevereLoggingMonitor;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class AddNoteActionTest extends AbstractAtsActionTest {

   @Test
   public void testRun() throws Exception {
      SevereLoggingMonitor monitor = TestUtil.severeLoggingStart();
      AddNoteAction action = (AddNoteAction) createAction();
      action.setEmulateUi(true);
      action.runWithException();
      AtsTestUtil.getTeamWf().persist(getClass().getSimpleName());
      TestUtil.severeLoggingEnd(monitor);
   }

   @Override
   public Action createAction() {
      AtsTestUtil.cleanupAndReset(getClass().getSimpleName());
      return new AddNoteAction(AtsTestUtil.getTeamWf(), new IDirtiableEditor() {

         @Override
         public void onDirtied() {
            // do nothing
         }
      });
   }

}
