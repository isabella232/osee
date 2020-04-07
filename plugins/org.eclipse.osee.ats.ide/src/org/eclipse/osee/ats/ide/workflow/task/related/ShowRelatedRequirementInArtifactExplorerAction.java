/*******************************************************************************
 * Copyright (c) 2017 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.ide.workflow.task.related;

import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osee.ats.api.task.related.TaskRelatedData;
import org.eclipse.osee.ats.api.workflow.IAtsTask;
import org.eclipse.osee.ats.ide.actions.ISelectedAtsArtifacts;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.ats.ide.internal.AtsClientService;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.jdk.core.type.MutableInteger;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.explorer.ArtifactExplorerUtil;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * @author Donald G. Dunne
 */
public class ShowRelatedRequirementInArtifactExplorerAction extends ShowTaskOptionActionBase {

   public ShowRelatedRequirementInArtifactExplorerAction(ISelectedAtsArtifacts selectedAtsArtifacts) {
      super("Show Related Requirement in Artifact Explorer", selectedAtsArtifacts);
   }

   @Override
   public void run() {
      final Collection<IAtsTask> tasks = Collections.castAll(getSelectedTasks());
      if (!isAutoGenTasks(tasks)) {
         return;
      }

      final MutableInteger answer = new MutableInteger(-1);

      AbstractOperation openOp =
         new AbstractOperation("Show Related Requirement in Artifact Explorer", Activator.PLUGIN_ID) {

            @Override
            protected void doWork(IProgressMonitor monitor) throws Exception {
               Displays.ensureInDisplayThread(new Runnable() {

                  @Override
                  public void run() {

                     String taskStr =
                        tasks.size() > 1 ? "these " + tasks.size() + " tasks" : tasks.iterator().next().getName();
                     String dialogMessage = String.format(
                        "Are you sure you want to open the latest requirement(s) for %s?\nNote: Deleted requirements will default to historical version.",
                        taskStr);

                     MessageDialog dialog = new MessageDialog(Displays.getActiveShell(), "Show Related Requirement",
                        null, dialogMessage, MessageDialog.NONE, new String[] {"OK", "Historical", "Cancel"}, 0);

                     answer.setValue(dialog.open());

                  }
               }, true);

               boolean loadLatest = true;
               if (answer.getValue() == 1) {
                  loadLatest = false;
               } else if (answer.getValue() == 2) {
                  return;
               }
               showRequirements(tasks, loadLatest);
            }
         };

      Operations.executeAsJob(openOp, true);
   }

   private void showRequirements(final Collection<IAtsTask> tasks, final boolean loadLatest) {

      for (final IAtsTask task : tasks) {
         TaskRelatedData reqData = AtsClientService.get().getTaskRelatedService().getTaskRelatedData(task);
         if (reqData != null) {
            if (reqData.getResults().isErrors()) {
               AWorkbench.popup(reqData.getResults().toString());
            } else {
               Artifact toOpen = null;
               if (reqData.isDeleted()) {
                  toOpen = AtsClientService.get().getQueryServiceClient().getArtifact(reqData.getHeadArtifact());
               } else {
                  if (loadLatest) {
                     toOpen = AtsClientService.get().getQueryServiceClient().getArtifact(reqData.getLatestArt());
                  } else {
                     toOpen = AtsClientService.get().getQueryServiceClient().getArtifact(reqData.getHeadArtifact());
                  }
               }
               ArtifactExplorerUtil.revealArtifact(toOpen);
            }
         }
      }
   }

   LabelProvider relatedArtifactLabelProvider = new LabelProvider() {
      @Override
      public String getText(Object element) {
         Artifact art = AtsClientService.get().getQueryServiceClient().getArtifact(element);
         if (art != null) {
            if (art.isHistorical()) {
               return "[Rev:" + art.getTransaction() + "] - " + art.getName();
            }
            return "[Rev:Head] - " + art.getName();
         }
         return "";
      }
   };

}
