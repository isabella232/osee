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
package org.eclipse.osee.framework.ui.skynet.commandHandlers;

import java.util.Collection;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osee.framework.access.AccessControlManager;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.OperationBuilder;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.skynet.core.revision.LoadChangeType;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.blam.operation.ReplaceArtifactWithBaselineOperation;
import org.eclipse.osee.framework.ui.skynet.blam.operation.ReplaceAttributeWithBaselineOperation;
import org.eclipse.osee.framework.ui.skynet.change.view.ChangeReportEditor;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.replace.ReplaceWithBaselineVersionDialog;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;

/**
 * @author Paul K. Waldfogel
 * @author Jeff C. Phillips
 */
public class ReplaceWithBaselineHandler extends AbstractHandler {
   private Collection<Change> changes;
   private IStructuredSelection structuredSelection;

   private final JobChangeAdapter adapter = new JobChangeAdapter() {
      @Override
      public void done(IJobChangeEvent event) {
         IStatus status = event.getResult();
         if (status != null && status.getSeverity() == IStatus.ERROR) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, status.getException());
         }
      }
   };

   @Override
   public boolean isEnabled() {
      boolean isEnabled = false;
      setSelectedData();

      LoadChangeType lastChangeType = null;

      //Only use the item selected for accessControl
      for (Change change : Handlers.getArtifactChangesFromStructuredSelection(structuredSelection)) {

         if (lastChangeType == null) {
            lastChangeType = change.getChangeType();
         }
         isEnabled = lastChangeType == change.getChangeType();

         try {
            isEnabled =
               isEnabled && AccessControlManager.hasPermission(change.getChangeArtifact(), PermissionEnum.WRITE);
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Error loading changes for change report handler");
         }
         if (!isEnabled) {
            break;
         }
      }
      return isEnabled;
   }

   private void setSelectedData() {
      IWorkbench workbench = PlatformUI.getWorkbench();
      if (!workbench.isClosing() || !workbench.isStarting()) {
         IWorkbenchPage page = AWorkbench.getActivePage();

         if (page != null) {
            IWorkbenchPart part = page.getActivePart();

            if (part != null) {
               IWorkbenchSite site = part.getSite();

               if (part instanceof ChangeReportEditor) {
                  ChangeReportEditor changeReportEditor = (ChangeReportEditor) part;
                  changes = changeReportEditor.getChanges().getChanges();
               }
               if (site != null) {

                  ISelectionProvider selectionProvider = site.getSelectionProvider();
                  if (selectionProvider != null) {
                     ISelection selection = selectionProvider.getSelection();
                     if (selection instanceof IStructuredSelection) {
                        structuredSelection = (IStructuredSelection) selection;
                     }
                  }
               }
            }
         }
      }
   }

   private boolean enableButtons(IStructuredSelection selection, LoadChangeType changeType) {
      boolean attrEnabled = false;

      Collection<Change> changes = Handlers.getArtifactChangesFromStructuredSelection(selection);

      for (Change change : changes) {
         attrEnabled = change.getChangeType() == changeType;

         if (!attrEnabled) {
            break;
         }
      }
      return attrEnabled;
   }

   @Override
   public Object execute(ExecutionEvent event) throws ExecutionException {
      try {
         boolean attrEnabled = enableButtons(structuredSelection, LoadChangeType.attribute);
         boolean artEnabled = enableButtons(structuredSelection, LoadChangeType.artifact);

         ReplaceWithBaselineVersionDialog dialog = new ReplaceWithBaselineVersionDialog(artEnabled, attrEnabled);
         if (dialog.open() == Window.OK) {
            OperationBuilder builder = Operations.createBuilder("Replace with Baseline Version");
            IOperation op = dialog.isAttributeSelected() ? new ReplaceAttributeWithBaselineOperation(
               Handlers.getArtifactChangesFromStructuredSelection(
                  structuredSelection)) : new ReplaceArtifactWithBaselineOperation(changes,
                     Handlers.getArtifactsFromStructuredSelection(structuredSelection));
            builder.addOp(op);

            IOperation finishDialog = new ReplaceBaselineFinishDialog();
            builder.addOp(finishDialog);

            Operations.executeAsJob(builder.build(), true, Job.LONG, adapter);
         }

      } catch (Exception ex) {
         throw new ExecutionException(ex.getMessage());
      }
      return null;
   }

   private static final class ReplaceBaselineFinishDialog extends AbstractOperation {

      public ReplaceBaselineFinishDialog() {
         super("Replace with Baseline Dialog", Activator.PLUGIN_ID);
      }

      @Override
      protected void doWork(IProgressMonitor monitor) throws Exception {
         Displays.ensureInDisplayThread(new Runnable() {

            @Override
            public void run() {
               AWorkbench.popup("Replace with Baseline Version...",
                  "The operation successfully completed, please refresh any associated change reports.");
            }
         });
      }

   }
}
