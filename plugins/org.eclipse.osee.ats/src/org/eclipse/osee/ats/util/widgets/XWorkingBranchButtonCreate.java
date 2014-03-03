/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.util.widgets;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.ats.core.client.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @author Shawn F. Cook
 */
public class XWorkingBranchButtonCreate extends XWorkingBranchButtonAbstract {

   public static String WIDGET_NAME = "XWorkingBranchButtonCreate";

   @Override
   protected void initButton(final Button button) {
      button.setToolTipText("Create Working Branch");
      button.setImage(ImageManager.getImage(FrameworkImage.BRANCH));
      button.addListener(SWT.Selection, new Listener() {
         @Override
         public void handleEvent(Event e) {
            disableAll = true;
            refreshEnablement(button);
            // Create working branch
            Result result = AtsBranchManagerCore.createWorkingBranch_Validate(getTeamArt());
            if (result.isFalse()) {
               AWorkbench.popup(result);
               return;
            }
            try {
               IOseeBranch parentBranch = AtsBranchManagerCore.getConfiguredBranchForWorkflow(getTeamArt());
               // Retrieve parent branch to create working branch from
               if (!MessageDialog.openConfirm(
                  Displays.getActiveShell(),
                  "Create Working Branch",
                  "Create a working branch from parent branch\n\n\"" + parentBranch.getName() + "\"?\n\n" + "NOTE: Working branches are necessary when OSEE Artifact changes " + "are made during implementation.")) {
                  disableAll = false;
                  refreshEnablement(button);
                  return;
               }
               AtsBranchManagerCore.createWorkingBranch_Create(getTeamArt());
               Thread.sleep(2000);
            } catch (Exception ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
            disableAll = false;
            refreshEnablement(button);
         }
      });
   }

   @Override
   protected void refreshEnablement(Button button) {
      button.setEnabled(!disableAll && !isWorkingBranchCreationInProgress() && !isWorkingBranchCommitInProgress() && !isWorkingBranchInWork() && !isCommittedBranchExists());
   }

}
