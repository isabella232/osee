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
package org.eclipse.osee.framework.ui.skynet.commandHandlers.branch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.commands.Command;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.ui.plugin.util.CompoundContributionProvider;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.commandHandlers.Handlers;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 * @author Jeff C. Phillips
 */
public class ArchiveBranchCompoundContributionItem extends CompoundContributionProvider {
   private ICommandService commandService;

   public ArchiveBranchCompoundContributionItem() {
      this.commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
   }

   public ArchiveBranchCompoundContributionItem(String id) {
      super(id);
   }

   @Override
   protected IContributionItem[] getContributionItems() {
      ISelectionProvider selectionProvider = getSelectionProvider();
      ArrayList<IContributionItem> contributionItems = new ArrayList<>(40);

      if (selectionProvider != null && selectionProvider.getSelection() instanceof IStructuredSelection) {
         IStructuredSelection structuredSelection = (IStructuredSelection) selectionProvider.getSelection();
         List<Branch> branches = Handlers.getBranchesFromStructuredSelection(structuredSelection);

         if (!branches.isEmpty()) {
            Branch selectedBranch = branches.iterator().next();
            if (selectedBranch != null) {
               String commandId = "org.eclipse.osee.framework.ui.skynet.branch.BranchView.archiveBranch";
               Command command = commandService.getCommand(commandId);
               CommandContributionItem contributionItem = null;
               BranchArchivedState archivedState = selectedBranch.getArchiveState();
               String label = (archivedState.isArchived() ? "Unarchive" : "Archive") + " Branch(s)";
               ImageDescriptor descriptor = archivedState.isArchived() ? ImageManager.getImageDescriptor(
                  FrameworkImage.UN_ARCHIVE) : ImageManager.getImageDescriptor(FrameworkImage.ARCHIVE);
               contributionItem = createCommand(label, selectedBranch, commandId, descriptor);

               if (command != null && command.isEnabled()) {
                  contributionItems.add(contributionItem);
               }
            }
         }
      }
      return contributionItems.toArray(new IContributionItem[0]);
   }

   private CommandContributionItem createCommand(String label, Branch branch, String commandId, ImageDescriptor descriptor) {
      CommandContributionItem contributionItem;

      contributionItem = new CommandContributionItem(
         new CommandContributionItemParameter(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), label, commandId,
            Collections.EMPTY_MAP, descriptor, null, null, label, null, null, SWT.NONE, null, false));

      return contributionItem;
   }
}
