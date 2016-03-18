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

import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import java.util.List;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.access.AccessControlManager;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.plugin.util.CommandHandler;
import org.eclipse.osee.framework.ui.skynet.commandHandlers.Handlers;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.EntryDialog;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * @author Jeff C. Phillips
 */
public class SetAssociatedBranchArtifactHandler extends CommandHandler {

   @Override
   public Object executeWithException(ExecutionEvent event, IStructuredSelection selection) throws OseeCoreException {
      Branch selectedBranch = Handlers.getBranchesFromStructuredSelection(selection).iterator().next();

      EntryDialog ed = new EntryDialog("Set Associated Artifact",
         "Set Associated Artifact for Branch\n\n\"" + selectedBranch.getName() + "\"" + (selectedBranch.getAssociatedArtifactId() != null ? "\n\nCurrently: " + selectedBranch.getAssociatedArtifactId() : "") + "\n\nEnter new Artifact Id to associate:");
      ed.setEntry(String.valueOf(selectedBranch.getAssociatedArtifactId()));
      if (ed.open() == 0) {
         String artId = ed.getEntry();
         Artifact associatedArtifact = ArtifactQuery.getArtifactFromId(Integer.parseInt(artId), COMMON);
         if (MessageDialog.openConfirm(Displays.getActiveShell(), "Set Associated Artifact",
            "Set Associated Artifact for Branch\n\n\"" + selectedBranch.getName() + "\"\nto\nArtifact: " + associatedArtifact)) {
            selectedBranch.setAssociatedArtifactId(Integer.parseInt(artId));
            BranchManager.setAssociatedArtifactId(selectedBranch, Integer.parseInt(artId));
         }
      }

      return null;
   }

   @Override
   public boolean isEnabledWithException(IStructuredSelection structuredSelection) throws OseeCoreException {
      List<? extends IOseeBranch> branches = Handlers.getBranchesFromStructuredSelection(structuredSelection);
      return branches.size() == 1 && AccessControlManager.isOseeAdmin();
   }
}