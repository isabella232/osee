/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.change;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionDelta;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.database.core.DatabaseTransactions;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.change.view.ChangeReportEditor;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;

public final class ChangeUiUtil {

   public static void open(IOseeBranch branch) throws OseeCoreException {
      Branch heavyBranch = BranchManager.getBranch(branch);
      Conditions.checkNotNull(branch, "Branch");
      if (heavyBranch.getBranchType() == BranchType.BASELINE) {
         if (!MessageDialog.openConfirm(AWorkbench.getActiveShell(), "Show Change Report",
            "You have chosen to show a change report for a BASLINE branch.\n\n" + //
            "This could be a very long running task and consume large resources.\n\nAre you sure?")) {
            return;
         }
      }
      open(createInput(heavyBranch, true));
   }

   public static void open(TransactionRecord transactionId) throws OseeCoreException {
      Conditions.checkNotNull(transactionId, "TransactionId");
      open(createInput(transactionId, true));
   }

   public static ChangeReportEditorInput createInput(TransactionRecord transactionId, boolean loadOnOpen) throws OseeCoreException {
      TransactionRecord startTx = TransactionManager.getPriorTransaction(transactionId);
      TransactionRecord endTx = transactionId;
      TransactionDelta txDelta = new TransactionDelta(startTx, endTx);
      return createInput(CompareType.COMPARE_SPECIFIC_TRANSACTIONS, txDelta, loadOnOpen);
   }

   public static ChangeReportEditorInput createInput(Branch branch, boolean loadOnOpen) throws OseeCoreException {
      Branch parentBranch = branch.getParentBranch();
      TransactionRecord startTx = TransactionManager.getHeadTransaction(branch);
      TransactionRecord endTx = TransactionManager.getHeadTransaction(parentBranch);
      TransactionDelta txDelta = new TransactionDelta(startTx, endTx);
      return createInput(CompareType.COMPARE_CURRENTS_AGAINST_PARENT, txDelta, loadOnOpen);
   }

   public static ChangeReportEditorInput createInput(CompareType compareType, TransactionDelta txDelta, boolean loadOnOpen) {
      ChangeUiData uiData = new ChangeUiData(compareType, txDelta);
      uiData.setLoadOnOpen(loadOnOpen);
      return new ChangeReportEditorInput(uiData);
   }

   public static void open(final ChangeReportEditorInput editorInput) {
      Job job = new UIJob("Open Change Report") {

         @Override
         public IStatus runInUIThread(IProgressMonitor monitor) {
            IStatus status = Status.OK_STATUS;
            try {
               AWorkbench.getActivePage().openEditor(editorInput, ChangeReportEditor.EDITOR_ID);
            } catch (PartInitException ex) {
               status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error opening change report", ex);
            }
            return status;
         }
      };
      Jobs.startJob(job, true);
   }
}
