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
package org.eclipse.osee.ats.operation;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.task.NewTaskData;
import org.eclipse.osee.ats.api.task.NewTaskDataFactory;
import org.eclipse.osee.ats.api.task.NewTaskDatas;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.AtsUtilClient;
import org.eclipse.osee.ats.editor.WorkflowEditor;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.Import.ExcelAtsTaskArtifactExtractor;
import org.eclipse.osee.ats.util.Import.TaskImportJob;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.blam.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;
import org.eclipse.osee.framework.ui.skynet.widgets.XListDropViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.util.SwtXWidgetRenderer;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Donald G. Dunne
 */
public class ImportTasksFromSpreadsheet extends AbstractBlam {

   public final static String TASK_IMPORT_SPREADSHEET = "Task Import Spreadsheet";
   public final static String TEAM_WORKFLOW = "Taskable Workflow (drop here)";
   public final static String EMAIL_POCS = "Email POCs";
   private TeamWorkFlowArtifact taskableStateMachineArtifact;

   @Override
   public String getName() {
      return "Import Tasks From Spreadsheet";
   }

   @Override
   public void widgetCreated(XWidget xWidget, FormToolkit toolkit, Artifact art, SwtXWidgetRenderer dynamicXWidgetLayout, XModifiedListener modListener, boolean isEditable) throws OseeCoreException {
      super.widgetCreated(xWidget, toolkit, art, dynamicXWidgetLayout, modListener, isEditable);
      if (xWidget.getLabel().equals(TEAM_WORKFLOW) && taskableStateMachineArtifact != null) {
         XListDropViewer viewer = (XListDropViewer) xWidget;
         viewer.setInput(Arrays.asList(taskableStateMachineArtifact));
      }
   }

   @Override
   public String getXWidgetsXml() {
      StringBuffer buffer = new StringBuffer("<xWidgets>");
      buffer.append("<XWidget xwidgetType=\"XListDropViewer\" displayName=\"" + TEAM_WORKFLOW + "\" />");
      buffer.append("<XWidget xwidgetType=\"XFileSelectionDialog\" displayName=\"" + TASK_IMPORT_SPREADSHEET + "\" />");
      buffer.append(
         "<XWidget xwidgetType=\"XCheckBox\" displayName=\"" + EMAIL_POCS + "\" labelAfter=\"true\" horizontalLabel=\"true\"/>");
      buffer.append("</xWidgets>");
      return buffer.toString();
   }

   @Override
   public String getDescriptionUsage() {
      return "Import tasks from spreadsheet into given Team Workflow";
   }

   /**
    * @return the TaskableStateMachineArtifact
    */
   public TeamWorkFlowArtifact getTaskableStateMachineArtifact() {
      return taskableStateMachineArtifact;
   }

   /**
    * @param taskableStateMachineArtifact the TaskableStateMachineArtifact to set
    */
   public void setTaskableStateMachineArtifact(TeamWorkFlowArtifact taskableStateMachineArtifact) {
      this.taskableStateMachineArtifact = taskableStateMachineArtifact;
   }

   @Override
   public void runOperation(final VariableMap variableMap, IProgressMonitor monitor) throws Exception {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            try {
               List<Artifact> artifacts = variableMap.getArtifacts(TEAM_WORKFLOW);
               String filename = variableMap.getString(TASK_IMPORT_SPREADSHEET);
               boolean emailPocs = variableMap.getBoolean(EMAIL_POCS);

               if (artifacts.isEmpty()) {
                  AWorkbench.popup("ERROR", "Must drag in Team Workflow to add tasks.");
                  return;
               }
               if (artifacts.size() > 1) {
                  AWorkbench.popup("ERROR", "Only drag ONE Team Workflow.");
                  return;
               }
               Artifact artifact = artifacts.iterator().next();
               if (!artifact.isOfType(AtsArtifactTypes.TeamWorkflow)) {
                  AWorkbench.popup("ERROR", "Artifact MUST be Team Workflow");
                  return;
               }
               if (!Strings.isValid(filename)) {
                  AWorkbench.popup("ERROR", "Must enter valid filename.");
                  return;
               }
               File file = new File(filename);
               try {

                  AtsUtilClient.setEmailEnabled(emailPocs);
                  NewTaskData newTaskData = NewTaskDataFactory.get("Import Tasks from Spreadsheet",
                     AtsClientService.get().getUserService().getCurrentUser(), (TeamWorkFlowArtifact) artifact);

                  Job job = Jobs.startJob(new TaskImportJob(file,
                     new ExcelAtsTaskArtifactExtractor((TeamWorkFlowArtifact) artifact, newTaskData)));
                  job.join();

                  AtsClientService.get().getTaskService().createTasks(new NewTaskDatas(newTaskData));
               } catch (Exception ex) {
                  log(ex);
                  return;
               } finally {
                  AtsUtilClient.setEmailEnabled(true);
               }

               WorkflowEditor.editArtifact(artifact);
            } catch (Exception ex) {
               log(ex);
            }
         };
      });
   }

   @Override
   public Collection<String> getCategories() {
      return Arrays.asList("ATS");
   }
}