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

package org.eclipse.osee.ats.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osee.ats.api.commit.ICommitConfigItem;
import org.eclipse.osee.ats.core.client.branch.AtsBranchUtil;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.MergeBranch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.skynet.core.revision.ChangeData;
import org.eclipse.osee.framework.skynet.core.revision.ChangeManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.ArrayTreeContentProvider;
import org.eclipse.osee.framework.ui.skynet.change.ChangeReportEditorInput;
import org.eclipse.osee.framework.ui.skynet.change.ChangeUiUtil;
import org.eclipse.osee.framework.ui.skynet.util.TransactionIdLabelProvider;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.FilteredTreeBranchDialog;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.FilteredTreeDialog;
import org.eclipse.osee.framework.ui.skynet.widgets.xmerge.MergeView;
import org.eclipse.ui.PlatformUI;

/**
 * BranchManager contains methods necessary for ATS objects to interact with creation, view and commit of branches.
 *
 * @author Donald G. Dunne
 */
public final class AtsBranchManager {

   private AtsBranchManager() {
      // Utility class
   }

   public static void showMergeManager(TeamWorkFlowArtifact teamArt) {
      try {
         BranchId workingBranch = teamArt.getWorkingBranch();
         List<BranchId> destinationBranches = new ArrayList<>();

         if (workingBranch.isValid()) {
            List<MergeBranch> mergeBranches = BranchManager.getMergeBranches(workingBranch);
            BranchId selectedBranch = null;

            if (!mergeBranches.isEmpty()) {
               if (!BranchManager.getState(workingBranch).isRebaselineInProgress()) {
                  for (MergeBranch mergeBranch : mergeBranches) {
                     destinationBranches.add(mergeBranch.getDestinationBranch());
                  }
                  if (mergeBranches.size() > 1) {
                     FilteredTreeBranchDialog dialog = new FilteredTreeBranchDialog("Select Destination Branch",
                        "Select The Destination Branch for which you want to open the Merge Manager",
                        destinationBranches);
                     int result = dialog.open();
                     if (result == 0) {
                        selectedBranch = (Branch) dialog.getSelectedFirst();
                     }
                  } else {
                     MergeBranch updateFromParentMergeBranch = BranchManager.getFirstMergeBranch(workingBranch);
                     selectedBranch = updateFromParentMergeBranch.getDestinationBranch();
                  }
               } else {
                  // the only merge branch is the Update from parent merge branch
                  MergeBranch updateFromParentMergeBranch = BranchManager.getFirstMergeBranch(workingBranch);
                  selectedBranch = updateFromParentMergeBranch.getDestinationBranch();
               }

               if (selectedBranch != null) {
                  MergeView.openView(workingBranch, selectedBranch, BranchManager.getBaseTransaction(workingBranch));
               }
            } else {
               MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error",
                  "There are no Merge Branches to view");
            }
         } else {
            MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error",
               "This Artifact does not have a working branch");
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   public static void showMergeManager(TeamWorkFlowArtifact teamArt, BranchId destinationBranch) throws OseeCoreException {
      if (AtsClientService.get().getBranchService().isWorkingBranchInWork(teamArt)) {
         IOseeBranch workingBranch = AtsClientService.get().getBranchService().getWorkingBranch(teamArt);
         MergeView.openView(workingBranch, destinationBranch, BranchManager.getBaseTransaction(workingBranch));
      } else if (AtsClientService.get().getBranchService().isCommittedBranchExists(teamArt)) {
         for (TransactionRecord transactionId : AtsClientService.get().getBranchService().getTransactionIds(teamArt,
            true)) {
            if (transactionId.isOnBranch(destinationBranch)) {
               MergeView.openView(transactionId);
            }
         }
      }
   }

   /**
    * If working branch has no changes, allow for deletion.
    */
   public static void deleteWorkingBranch(TeamWorkFlowArtifact teamArt, boolean promptUser) {
      boolean isExecutionAllowed = !promptUser;
      try {
         BranchId branch = AtsClientService.get().getBranchService().getWorkingBranch(teamArt);
         if (promptUser) {
            StringBuilder message = new StringBuilder();
            if (BranchManager.hasChanges(branch)) {
               message.append("Warning: Changes have been made on this branch.\n\n");
            }
            message.append("Are you sure you want to delete the branch: ");
            message.append(branch);

            isExecutionAllowed =
               MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                  "Delete Working Branch", message.toString());
         }

         if (isExecutionAllowed) {
            Exception exception = null;
            Result result = Result.FalseResult;
            try {
               result = AtsBranchUtil.deleteWorkingBranch(teamArt, true);
            } catch (Exception ex) {
               exception = ex;
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Problem deleting branch.", ex);
            }
            if (result != null) {
               if (promptUser) {
                  AWorkbench.popup("Delete Complete",
                     result.isTrue() ? "Branch delete was successful." : "Branch delete failed.\n" + result.getText());
               } else if (result.isFalse()) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, result.getText(), exception);
               }
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Problem deleting branch.", ex);
      }
   }

   /**
    * Either return a single commit transaction or user must choose from a list of valid commit transactions
    */
   public static TransactionToken getTransactionIdOrPopupChoose(TeamWorkFlowArtifact teamArt, String title, boolean showMergeManager) throws OseeCoreException {
      Collection<TransactionToken> transactionIds = new HashSet<>();
      Collection<TransactionRecord> transactions =
         AtsClientService.get().getBranchService().getTransactionIds(teamArt, showMergeManager);

      for (TransactionRecord id : transactions) {
         // ignore working branches that have been committed or re-baselined (e.g. update form parent branch)
         boolean workingBranch = BranchManager.getType(id).isWorkingBranch();
         BranchState state = BranchManager.getState(id.getBranch());
         if (!workingBranch || !(state.isRebaselined() && state.isCommitted())) {
            transactionIds.add(id);
         }
      }
      if (transactionIds.size() == 1) {
         return transactionIds.iterator().next();
      }

      ViewerSorter sorter = new ViewerSorter() {
         @Override
         public int compare(Viewer viewer, Object e1, Object e2) {
            if (e1 == null || e2 == null) {
               return 0;
            }
            TransactionId tx1 = (TransactionId) e1;
            TransactionId tx2 = (TransactionId) e2;
            return (int) (tx1.getId() - tx2.getId());
         }
      };
      FilteredTreeDialog dialog = new FilteredTreeDialog(title, "Select Commit Branch", new ArrayTreeContentProvider(),
         new TransactionIdLabelProvider(), sorter);

      dialog.setInput(transactionIds);
      if (dialog.open() == 0) {
         return dialog.getSelectedFirst();
      }
      return TransactionToken.SENTINEL;
   }

   /**
    * Display change report associated with the branch, if exists, or transaction, if branch has been committed.
    */
   public static void showChangeReport(TeamWorkFlowArtifact teamArt) {
      try {
         if (AtsClientService.get().getBranchService().isWorkingBranchInWork(teamArt)) {
            BranchId parentBranch = AtsClientService.get().getBranchService().getConfiguredBranchForWorkflow(teamArt);
            Conditions.assertNotNull(parentBranch,
               "Parent Branch can not be null. Set Targeted Version or configure Team for Parent Branch");
            IOseeBranch workingBranch = AtsClientService.get().getBranchService().getWorkingBranch(teamArt);
            ChangeReportEditorInput input = ChangeUiUtil.createInput(workingBranch, parentBranch, true);
            ChangeUiUtil.open(input);
         } else if (AtsClientService.get().getBranchService().isCommittedBranchExists(teamArt)) {
            TransactionToken transactionId = getTransactionIdOrPopupChoose(teamArt, "Show Change Report", false);
            if (transactionId == null) {
               return;
            }
            ChangeUiUtil.open(transactionId);
         } else {
            AWorkbench.popup("ERROR", "No Branch or Committed Transaction Found.");
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Can't show change report.", ex);
      }
   }

   /**
    * Grab the change report for the indicated branch
    */
   public static void showChangeReportForBranch(TeamWorkFlowArtifact teamArt, BranchId destinationBranch) {
      try {
         for (TransactionToken transactionId : AtsClientService.get().getBranchService().getTransactionIds(teamArt,
            false)) {
            if (transactionId.isOnBranch(destinationBranch)) {
               ChangeUiUtil.open(transactionId);
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Can't show change report.", ex);
      }
   }

   /**
    * @param commitPopup if true, pop-up errors associated with results
    * @param overrideStateValidation if true, don't do checks to see if commit can be performed. This should only be
    * used for developmental testing or automation
    */
   public static IOperation commitWorkingBranch(final TeamWorkFlowArtifact teamArt, final boolean commitPopup, final boolean overrideStateValidation, BranchId destinationBranch, boolean archiveWorkingBranch) throws OseeCoreException {
      if (AtsClientService.get().getBranchService().isBranchInCommit(teamArt)) {
         throw new OseeCoreException("Branch is currently being committed.");
      }
      return new AtsBranchCommitOperation(teamArt, commitPopup, overrideStateValidation, destinationBranch,
         archiveWorkingBranch);
   }

   public static ChangeData getChangeDataFromEarliestTransactionId(TeamWorkFlowArtifact teamArt) throws OseeCoreException {
      return getChangeData(teamArt, null);
   }

   /**
    * Return ChangeData represented by commit to commitConfigArt or earliest commit if commitConfigArt == null
    *
    * @param commitConfigArt that configures commit or null
    */
   public static ChangeData getChangeData(TeamWorkFlowArtifact teamArt, ICommitConfigItem commitConfigArt) throws OseeCoreException {
      if (commitConfigArt != null && !isBaselinBranchConfigured(commitConfigArt)) {
         throw new OseeArgumentException("Parent Branch not configured for [%s]", commitConfigArt);
      }
      Collection<Change> changes = new ArrayList<>();

      IOperation operation = null;
      if (AtsClientService.get().getBranchService().isWorkingBranchInWork(teamArt)) {
         operation = ChangeManager.comparedToParent(AtsClientService.get().getBranchService().getWorkingBranch(teamArt),
            changes);
         Operations.executeWorkAndCheckStatus(operation);
      } else {
         if (AtsClientService.get().getBranchService().isCommittedBranchExists(teamArt)) {
            TransactionToken transactionId = null;
            if (commitConfigArt == null) {
               transactionId = AtsClientService.get().getBranchService().getEarliestTransactionId(teamArt);
            } else {
               Collection<TransactionRecord> transIds =
                  AtsClientService.get().getBranchService().getTransactionIds(teamArt, false);
               if (transIds.size() == 1) {
                  transactionId = transIds.iterator().next();
               } else {
                  /*
                   * First, attempt to compare the currently configured commitConfigArt parent branch with transaction
                   * id's branch.
                   */
                  for (TransactionRecord transId : transIds) {
                     if (transId.isOnBranch(commitConfigArt.getBaselineBranchId())) {
                        transactionId = transId;
                     }
                  }
                  /*
                   * Otherwise, fallback to getting the lowest transaction id number. This could happen if branches were
                   * rebaselined cause previous transId branch would not match currently configured parent branch. This
                   * could also happen if workflow not targeted for version and yet commits happened
                   */
                  if (transactionId == null) {
                     TransactionRecord transactionRecord = null;
                     for (TransactionRecord transId : transIds) {
                        if (transactionRecord == null || transId.getId() < transactionRecord.getId()) {
                           transactionRecord = transId;
                        }
                     }
                     transactionId = transactionRecord;
                  }
               }
            }
            if (transactionId == null) {
               throw new OseeStateException("Unable to determine transaction id for [%s]", commitConfigArt);
            }
            operation = ChangeManager.comparedToPreviousTx(transactionId, changes);
            Operations.executeWorkAndCheckStatus(operation);
         }
      }
      return new ChangeData(changes);
   }

   private static boolean isBaselinBranchConfigured(ICommitConfigItem commitConfigArt) {
      return commitConfigArt.getBaselineBranchId().isValid();
   }
}