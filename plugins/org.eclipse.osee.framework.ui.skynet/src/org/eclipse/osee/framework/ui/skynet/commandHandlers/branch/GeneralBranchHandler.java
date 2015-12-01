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
package org.eclipse.osee.framework.ui.skynet.commandHandlers.branch;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.access.AccessControlManager;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.ui.plugin.util.CommandHandler;
import org.eclipse.osee.framework.ui.skynet.commandHandlers.Handlers;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * @author Karol M. Wilk
 */
public abstract class GeneralBranchHandler extends CommandHandler {
   public enum OpTypeEnum {
      DELETE("delete", "Delete Branch"),
      PURGE("purge", "Purge Branch");

      private final String dialogType;
      private final String dialogTitle;

      private OpTypeEnum(String type, String title) {
         dialogType = type;
         dialogTitle = Strings.capitalize(type);
      }
   };
   private final OpTypeEnum type;

   public GeneralBranchHandler(OpTypeEnum type) {
      this.type = type;
   }

   public abstract void performOperation(final List<IOseeBranch> branches);

   @Override
   public Object executeWithException(ExecutionEvent arg0, IStructuredSelection selection) throws OseeCoreException {
      List<IOseeBranch> selectedBranches = Handlers.getBranchesFromStructuredSelection(selection);

      Iterator<IOseeBranch> iterator = selectedBranches.iterator();
      List<BranchId> hasChildren = new LinkedList<>();
      while (iterator.hasNext()) {
         BranchId branch = iterator.next();
         if (BranchManager.hasChildren(branch)) {
            iterator.remove();
            hasChildren.add(branch);
         }
      }

      if (!hasChildren.isEmpty()) {
         StringBuilder children = new StringBuilder();
         children.append(String.format("The following branches have children and cannot be %sd:\n", type.dialogType));
         for (BranchId branch : hasChildren) {
            List<BranchId> branches = new LinkedList<>(BranchManager.getChildBranches(branch, true));
            children.append(
               String.format("Branch Id %s has children: %s\n", branch.getId(), Strings.buildStatment(branches)));
         }
         MessageDialog.openError(Displays.getActiveShell(), type.dialogTitle, children.toString());
      }

      if (!selectedBranches.isEmpty()) {
         StringBuilder branchesStatement = new StringBuilder();
         branchesStatement.append(String.format("Are you sure you want to %s branch(es): ", type.dialogType));
         branchesStatement.append(Strings.buildStatment(selectedBranches));
         branchesStatement.append(" \u003F");

         MessageDialog dialog =
            new MessageDialog(Displays.getActiveShell(), type.dialogTitle, null, branchesStatement.toString(),
               MessageDialog.QUESTION, new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 1);

         if (dialog.open() == 0) {
            performOperation(selectedBranches);
         }
      }

      return null;
   }

   @Override
   public boolean isEnabledWithException(IStructuredSelection structuredSelection) throws OseeCoreException {
      List<? extends BranchId> branches = Handlers.getBranchesFromStructuredSelection(structuredSelection);
      return !branches.isEmpty() && (AccessControlManager.isOseeAdmin() || canEnableBranches(branches));
   }

   private boolean canEnableBranches(List<? extends BranchId> branches) {
      boolean canBeDeleted = true;
      for (BranchId branch : branches) {
         if (!isEnableAllowed(branch)) {
            canBeDeleted = false;
            break;
         }
      }
      return canBeDeleted;
   }

   private boolean isEnableAllowed(BranchId branch) {
      return !BranchManager.isChangeManaged(branch) && BranchManager.getType(branch).isWorkingBranch();
   }
}