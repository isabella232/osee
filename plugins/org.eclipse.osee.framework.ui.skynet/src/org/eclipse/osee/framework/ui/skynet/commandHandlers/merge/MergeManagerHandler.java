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

package org.eclipse.osee.framework.ui.skynet.commandHandlers.merge;

import java.util.List;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.revision.ConflictManagerInternal;
import org.eclipse.osee.framework.ui.plugin.util.CommandHandler;
import org.eclipse.osee.framework.ui.skynet.commandHandlers.Handlers;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.internal.ServiceUtil;
import org.eclipse.osee.framework.ui.skynet.widgets.xBranch.BranchView;
import org.eclipse.osee.framework.ui.skynet.widgets.xmerge.MergeView;

/**
 * @author Jeff C. Phillips
 * @author Theron Virgin
 */
public class MergeManagerHandler extends CommandHandler {

   @Override
   public Object executeWithException(ExecutionEvent event, IStructuredSelection selection) throws OseeCoreException {
      if (!selection.isEmpty()) {
         List<Branch> branches = Handlers.getBranchesFromStructuredSelection(selection);

         if (!branches.isEmpty()) {
            Branch selectedBranch = branches.iterator().next();
            Branch toBranch = BranchManager.getBranch(Long.parseLong(event.getParameter(BranchView.BRANCH_ID)));
            if (selectedBranch != null && toBranch != null) {
               MergeView.openView(selectedBranch, toBranch, selectedBranch.getBaseTransaction());
            }
         }
      }
      return null;
   }

   @Override
   public boolean isEnabledWithException(IStructuredSelection structuredSelection) {
      boolean enabled = false;
      if (!structuredSelection.isEmpty()) {
         List<Branch> branches = Handlers.getBranchesFromStructuredSelection(structuredSelection);

         if (!branches.isEmpty()) {
            BranchId selectedBranch = branches.iterator().next();
            try {
               if (selectedBranch != null && !ConflictManagerInternal.getDestinationBranchesMerged(
                  selectedBranch.getUuid()).isEmpty()) {
                  enabled = true;
               } else {
                  enabled = selectedBranch != null && !ServiceUtil.getOseeCmService().isPcrArtifact(
                     BranchManager.getAssociatedArtifact(selectedBranch));
               }
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      }

      return enabled;
   }
}
