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

package org.eclipse.osee.ats.ide.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.ats.ide.editor.WorkflowEditor;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.ats.ide.internal.AtsApiService;
import org.eclipse.osee.ats.ide.workflow.task.TaskArtifact;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.PurgeArtifacts;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.ui.PlatformUI;

/**
 * @author Donald G. Dunne
 */
public class DeleteTasksAction extends AbstractAtsAction {

   public static interface TaskArtifactProvider {

      List<TaskArtifact> getSelectedArtifacts();

   }

   private final TaskArtifactProvider taskProvider;

   public DeleteTasksAction(TaskArtifactProvider taskProvider) {
      super("Delete Tasks", IAction.AS_PUSH_BUTTON);
      this.taskProvider = taskProvider;
      setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.X_RED));
      setToolTipText(getText());
   }

   public void updateEnablement(Collection<Artifact> selected) {
      for (Artifact art : selected) {
         if (!(art instanceof TaskArtifact)) {
            setEnabled(false);
            return;
         }
      }
      setEnabled(true);
   }

   @Override
   public void run() {
      final List<TaskArtifact> items = taskProvider.getSelectedArtifacts();
      if (items.isEmpty()) {
         AWorkbench.popup("ERROR", "No Tasks Selected");
         return;
      }
      StringBuilder builder = new StringBuilder();
      if (items.size() > 15) {
         builder.append("Are you sure you wish to delete " + items.size() + " Tasks?\n\n");
      } else {
         builder.append("Are you sure you wish to delete ");
         if (items.size() == 1) {
            builder.append("this Task?\n\n");
         } else {
            builder.append("these Tasks?\n\n");
         }
         for (TaskArtifact taskItem : items) {
            builder.append("\"" + taskItem.getName() + "\"\n");
         }

         builder.append("\n\nNote: Workflow will be saved.");

      }
      boolean delete = MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
         "Delete Task", builder.toString());
      if (delete) {
         try {
            SkynetTransaction transaction =
               TransactionManager.createTransaction(AtsApiService.get().getAtsBranch(), "Delete Tasks");
            // Done for concurrent modification purposes
            ArrayList<TaskArtifact> delItems = new ArrayList<>();
            ArrayList<TaskArtifact> tasksNotInDb = new ArrayList<>();
            delItems.addAll(items);
            for (TaskArtifact taskArt : delItems) {
               WorkflowEditor.close(Collections.singleton(taskArt), false);
               if (taskArt.isInDb()) {
                  taskArt.deleteAndPersist(transaction);
               } else {
                  tasksNotInDb.add(taskArt);
               }
            }
            transaction.execute();

            if (tasksNotInDb.size() > 0) {
               Operations.executeWorkAndCheckStatus(new PurgeArtifacts(tasksNotInDb));
            }
         } catch (Exception ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         }
      }
   }

}
