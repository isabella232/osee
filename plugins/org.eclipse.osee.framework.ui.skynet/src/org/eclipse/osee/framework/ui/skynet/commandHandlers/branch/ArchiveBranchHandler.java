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

import java.util.Collection;
import java.util.List;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.access.AccessControlManager;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEventType;
import org.eclipse.osee.framework.ui.plugin.util.CommandHandler;
import org.eclipse.osee.framework.ui.skynet.commandHandlers.Handlers;

/**
 * @author Jeff C. Phillips
 */
public class ArchiveBranchHandler extends CommandHandler {

   @Override
   public boolean isEnabledWithException(IStructuredSelection structuredSelection) throws OseeCoreException {
      List<? extends IOseeBranch> branches = Handlers.getBranchesFromStructuredSelection(structuredSelection);
      return !branches.isEmpty() && AccessControlManager.isOseeAdmin();
   }

   @Override
   public Object executeWithException(ExecutionEvent event, IStructuredSelection selection) throws OseeCoreException {
      Collection<Branch> branches = Handlers.getBranchesFromStructuredSelection(selection);

      for (Branch branch : branches) {
         BranchArchivedState state = branch.getArchiveState();
         branch.setArchived(!state.isArchived());
      }
      BranchManager.persist(branches);

      for (Branch branch : branches) {
         OseeEventManager.kickBranchEvent(this, new BranchEvent(BranchEventType.Committed, branch.getGuid()));
      }
      return null;
   }
}
