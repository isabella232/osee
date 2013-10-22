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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.team.CreateTeamOption;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workflow.log.LogType;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.task.AbstractTaskableArtifact;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowManager;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.client.workflow.ITeamWorkflowProvider;
import org.eclipse.osee.ats.editor.SMAEditor;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.AXml;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
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
public class DuplicateWorkflowBlam extends AbstractBlam {

   private final static String TEAM_WORKFLOW = "Team Workflow (drop here)";
   private final static String DUPLICATE_WORKFLOW =
      "Duplicate Workflow - creates carbon copy with all fields and assignees intact.";
   private final static String CREATE_NEW_WORFLOW_IN_START_STATE =
      "Create new Workflow - creates new workflow in start state with current assignees.";
   private final static String DUPLICATE_TASKS = "Duplicate Tasks - only valid for Duplicate Workflow";
   private final static String DUPLICATE_METHOD = "Duplicate Method";
   private final static String TITLE = "New Title (blank for same title)";
   private Collection<? extends TeamWorkFlowArtifact> defaultTeamWorkflows;

   public DuplicateWorkflowBlam() {
      // do nothing
   }

   @Override
   public void runOperation(final VariableMap variableMap, IProgressMonitor monitor) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            try {
               List<Artifact> artifacts = variableMap.getArtifacts(TEAM_WORKFLOW);
               boolean duplicateTasks = variableMap.getBoolean(DUPLICATE_TASKS);
               boolean createNewWorkflow =
                  variableMap.getString(DUPLICATE_METHOD).equals(CREATE_NEW_WORFLOW_IN_START_STATE);
               boolean duplicateWorkflow = variableMap.getString(DUPLICATE_METHOD).equals(DUPLICATE_WORKFLOW);
               String title = variableMap.getString(TITLE);

               if (artifacts.isEmpty()) {
                  AWorkbench.popup("ERROR", "Must drag in Team Workflow to duplicate.");
                  return;
               }
               if (!createNewWorkflow && !duplicateWorkflow) {
                  AWorkbench.popup("ERROR", "Please select \"Duplicate Method\".");
                  return;
               }
               if (duplicateTasks && createNewWorkflow) {
                  AWorkbench.popup("ERROR", "Can not create workflow as new and duplicate tasks.");
                  return;
               }
               Artifact artifact = artifacts.iterator().next();
               if (!(artifact.isOfType(AtsArtifactTypes.TeamWorkflow))) {
                  AWorkbench.popup("ERROR", "Artifact MUST be Team Workflow");
                  return;
               }
               try {
                  AtsUtilCore.setEmailEnabled(false);
                  Collection<TeamWorkFlowArtifact> teamArts = Collections.castAll(artifacts);
                  if (createNewWorkflow) {
                     handleCreateNewWorkflow(teamArts, title);
                  } else {
                     handleCreateDuplicate(teamArts, duplicateTasks, title);
                  }
               } catch (Exception ex) {
                  log(ex);
               } finally {
                  AtsUtilCore.setEmailEnabled(true);
               }

            } catch (Exception ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         };
      });
   }

   private void handleCreateNewWorkflow(Collection<TeamWorkFlowArtifact> teamArts, String title) throws OseeCoreException {
      Set<TeamWorkFlowArtifact> newTeamArts = new HashSet<TeamWorkFlowArtifact>();
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranch(), "Duplicate Workflow");
      Date createdDate = new Date();
      IAtsUser createdBy = AtsClientService.get().getUserAdmin().getCurrentUser();
      for (TeamWorkFlowArtifact teamArt : teamArts) {
         List<IAtsUser> assignees = new LinkedList<IAtsUser>();
         assignees.addAll(teamArt.getStateMgr().getAssignees());
         if (!assignees.contains(AtsClientService.get().getUserAdmin().getCurrentUser())) {
            assignees.add(AtsClientService.get().getUserAdmin().getCurrentUser());
         }
         TeamWorkFlowArtifact newTeamArt =
            ActionManager.createTeamWorkflow(teamArt.getParentActionArtifact(), teamArt.getTeamDefinition(),
               teamArt.getActionableItemsDam().getActionableItems(), assignees, transaction, createdDate, createdBy,
               null, CreateTeamOption.Duplicate_If_Exists);
         if (Strings.isValid(title)) {
            newTeamArt.setName(title);
         }
         newTeamArt.persist(transaction);
         newTeamArts.add(newTeamArt);
      }
      transaction.execute();
      for (TeamWorkFlowArtifact newTeamArt : newTeamArts) {
         SMAEditor.editArtifact(newTeamArt);
      }
   }

   private void handleCreateDuplicate(Collection<TeamWorkFlowArtifact> teamArts, boolean duplicateTasks, String title) throws OseeCoreException {
      Set<TeamWorkFlowArtifact> newTeamArts = new HashSet<TeamWorkFlowArtifact>();
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranch(), "Duplicate Workflow");
      for (TeamWorkFlowArtifact teamArt : teamArts) {
         TeamWorkFlowArtifact dupArt = (TeamWorkFlowArtifact) teamArt.duplicate(AtsUtil.getAtsBranch());
         if (Strings.isValid(title)) {
            dupArt.setName(title);
         }
         dupArt.addRelation(AtsRelationTypes.ActionToWorkflow_Action, teamArt.getParentActionArtifact());
         dupArt.getLog().addLog(LogType.Note, null, "Workflow duplicated from " + teamArt.getAtsId());
         if (duplicateTasks) {
            for (TaskArtifact taskArt : teamArt.getTaskArtifacts()) {
               TaskArtifact dupTaskArt = (TaskArtifact) taskArt.duplicate(AtsUtil.getAtsBranch());
               dupTaskArt.getLog().addLog(LogType.Note, null, "Task duplicated from " + taskArt.getAtsId());
               dupArt.addRelation(AtsRelationTypes.TeamWfToTask_Task, dupTaskArt);
               dupArt.persist(transaction);
            }
         }
         newTeamArts.add(dupArt);
         dupArt.persist(transaction);
         // Notify all extension points that workflow is being duplicated in case they need to add, remove
         // attributes or relations
         for (ITeamWorkflowProvider teamExtension : TeamWorkFlowManager.getTeamWorkflowProviders()) {
            teamExtension.teamWorkflowDuplicating(teamArt, dupArt);
         }
      }
      transaction.execute();
      for (TeamWorkFlowArtifact newTeamArt : newTeamArts) {
         SMAEditor.editArtifact(newTeamArt);
      }
   }

   @Override
   public void widgetCreated(XWidget xWidget, FormToolkit toolkit, Artifact art, SwtXWidgetRenderer dynamicXWidgetLayout, XModifiedListener modListener, boolean isEditable) throws OseeCoreException {
      super.widgetCreated(xWidget, toolkit, art, dynamicXWidgetLayout, modListener, isEditable);
      if (xWidget.getLabel().equals(TEAM_WORKFLOW) && defaultTeamWorkflows != null) {
         XListDropViewer viewer = (XListDropViewer) xWidget;
         viewer.setInput(defaultTeamWorkflows);
      }
   }

   @Override
   public String getXWidgetsXml() {
      return "<xWidgets><XWidget xwidgetType=\"XListDropViewer\" displayName=\"" + TEAM_WORKFLOW + "\" />" +
      //
      "<XWidget xwidgetType=\"XCombo(" + CREATE_NEW_WORFLOW_IN_START_STATE + "," + DUPLICATE_WORKFLOW + ")\" required=\"true\" displayName=\"" + DUPLICATE_METHOD + "\" horizontalLabel=\"true\" defaultValue=\"false\"/>" +
      //
      "<XWidget xwidgetType=\"XCheckBox\" displayName=\"" + DUPLICATE_TASKS + "\" horizontalLabel=\"true\" defaultValue=\"false\"/>" +
      //
      "<XWidget xwidgetType=\"XText\" displayName=\"" + TITLE + "\" horizontalLabel=\"true\" defaultValue=\"" + getDefaultTitle() + "\"/>" +
      //
      "</xWidgets>";
   }

   /**
    * Return "Copy of"-title if all titles of workflows are the same, else ""
    */
   private String getDefaultTitle() {
      String title = "";
      if (defaultTeamWorkflows != null) {
         for (TeamWorkFlowArtifact teamArt : defaultTeamWorkflows) {
            if (title.equals("")) {
               title = teamArt.getName();
            } else if (!title.equals(teamArt.getName())) {
               return "";
            }
         }
      }
      return AXml.textToXml("Copy of " + title);
   }

   @Override
   public String getDescriptionUsage() {
      return "Duplicate team workflow(s) as a carbon copy (all fields/states/assignees will be exactly as they are) or as new workflows in start state.";
   }

   /**
    * @return the defaultTeamWorkflows
    */
   public Collection<? extends AbstractTaskableArtifact> getDefaultTeamWorkflows() {
      return defaultTeamWorkflows;
   }

   /**
    * @param defaultTeamWorkflows the defaultTeamWorkflows to set
    */
   public void setDefaultTeamWorkflows(Collection<? extends TeamWorkFlowArtifact> defaultTeamWorkflows) {
      this.defaultTeamWorkflows = defaultTeamWorkflows;
   }

   @Override
   public String getName() {
      return "Duplicate Workflow";
   }

   @Override
   public Collection<String> getCategories() {
      return Arrays.asList("ATS");
   }

}