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
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.core.client.action.ActionArtifact;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.client.workflow.ChangeType;
import org.eclipse.osee.ats.editor.SMAEditor;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.AXml;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
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
public class CreateActionFromTaskBlam extends AbstractBlam {

   private final static String TASKS = "Tasks (drop here)";
   private final static String TITLE = "New Title (blank for same title)";
   private final static String ACTIONABLE_ITEMS = "Actionable Item(s)";
   private final static String CHANGE_TYPE = "Change Type";
   private final static String PRIORITY = "Priority";
   private Collection<TaskArtifact> taskArtifacts;

   public CreateActionFromTaskBlam() {
      // do nothing
   }

   @Override
   public void runOperation(final VariableMap variableMap, final IProgressMonitor monitor) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            try {
               List<Artifact> artifacts = variableMap.getArtifacts(TASKS);
               String title = variableMap.getString(TITLE);
               Collection<IAtsActionableItem> aiasArts =
                  variableMap.getCollection(IAtsActionableItem.class, ACTIONABLE_ITEMS);
               String changeTypeStr = variableMap.getString(CHANGE_TYPE);
               if (changeTypeStr == null || changeTypeStr.equals("--select--")) {
                  AWorkbench.popup("ERROR", "Must select a Change Type");
                  return;
               }
               ChangeType changeType = ChangeType.valueOf(changeTypeStr);
               String priority = variableMap.getString(PRIORITY);
               if (priority == null || priority.equals("--select--")) {
                  AWorkbench.popup("ERROR", "Must select a Priority");
                  return;
               }

               if (artifacts.isEmpty()) {
                  AWorkbench.popup("ERROR", "Must drag in Tasks to create Actions.");
                  return;
               }
               Artifact artifact = artifacts.iterator().next();
               if (!(artifact.isOfType(AtsArtifactTypes.Task))) {
                  AWorkbench.popup("ERROR", "Artifact MUST be Task");
                  return;
               }
               if (aiasArts.isEmpty()) {
                  AWorkbench.popup("ERROR", "Must select Actionable Item(s)");
                  return;
               }
               try {
                  AtsUtilCore.setEmailEnabled(false);
                  Collection<TaskArtifact> taskArts = Collections.castAll(artifacts);
                  Collection<IAtsActionableItem> aias = Collections.castAll(aiasArts);
                  handleCreateActions(taskArts, title, aias, changeType, priority, monitor);
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

   private void handleCreateActions(Collection<TaskArtifact> tasks, String title, Collection<IAtsActionableItem> aias, ChangeType changeType, String priority, IProgressMonitor monitor) throws OseeCoreException {
      Set<TeamWorkFlowArtifact> newTeamArts = new HashSet<TeamWorkFlowArtifact>();
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranch(), "Create Actions from Tasks");
      for (TaskArtifact task : tasks) {
         String useTitle = title;
         if (!Strings.isValid(useTitle)) {
            useTitle = task.getName();
         }
         ActionArtifact action =
            ActionManager.createAction(monitor, useTitle, getDescription(task), changeType, priority, false, null,
               aias, new Date(), AtsClientService.get().getUserAdmin().getCurrentUser(), null, transaction);

         for (TeamWorkFlowArtifact teamArt : action.getTeams()) {
            newTeamArts.add(teamArt);
            teamArt.addRelation(CoreRelationTypes.SupportingInfo_SupportingInfo, task);
            teamArt.persist(transaction);
         }
      }
      transaction.execute();
      if (newTeamArts.size() == 1) {
         SMAEditor.editArtifact(newTeamArts.iterator().next());
      } else {
         AtsUtil.openInAtsWorldEditor("Created Tasks from Actions", newTeamArts);
      }

   }

   private String getDescription(TaskArtifact taskArt) {
      if (Strings.isValid(taskArt.getDescription())) {
         return String.format("Create from task [%s]\n\n[%s]", taskArt.toStringWithId(), taskArt.getDescription());
      }
      return String.format("Created from task [%s]", taskArt.toStringWithId());
   }

   @Override
   public void widgetCreated(XWidget xWidget, FormToolkit toolkit, Artifact art, SwtXWidgetRenderer dynamicXWidgetLayout, XModifiedListener modListener, boolean isEditable) throws OseeCoreException {
      super.widgetCreated(xWidget, toolkit, art, dynamicXWidgetLayout, modListener, isEditable);
      if (xWidget.getLabel().equals(TASKS) && taskArtifacts != null) {
         XListDropViewer viewer = (XListDropViewer) xWidget;
         viewer.setInput(taskArtifacts);
      }
   }

   @Override
   public String getXWidgetsXml() throws OseeCoreException {
      return "<xWidgets><XWidget xwidgetType=\"XListDropViewer\" displayName=\"" + TASKS + "\" />" +
      //
      "<XWidget xwidgetType=\"XHyperlabelActionableItemSelection\" displayName=\"" + ACTIONABLE_ITEMS + "\" horizontalLabel=\"true\"/>" +
      //
      "<XWidget xwidgetType=\"XText\" displayName=\"" + TITLE + "\" horizontalLabel=\"true\" defaultValue=\"" + getDefaultTitle() + "\"/>" +
      //
      "<XWidget displayName=\"" + CHANGE_TYPE + "\" xwidgetType=\"XCombo(" + Collections.toString(",",
         AttributeTypeManager.getEnumerationValues(AtsAttributeTypes.ChangeType)) + ")\" required=\"true\" horizontalLabel=\"true\" toolTip=\"" + AtsAttributeTypes.ChangeType.getDescription() + "\"/>" +
      //
      "<XWidget displayName=\"" + PRIORITY + "\" xwidgetType=\"XCombo(" + Collections.toString(",",
         AttributeTypeManager.getEnumerationValues(AtsAttributeTypes.PriorityType)) + ")\" required=\"true\" horizontalLabel=\"true\"/>" +
      //
      "</xWidgets>";
   }

   /**
    * Return "Copy of"-title if all titles of workflows are the same, else ""
    */
   private String getDefaultTitle() {
      String title = "";
      if (taskArtifacts != null) {
         for (TaskArtifact taskArt : taskArtifacts) {
            if (title.equals("")) {
               title = taskArt.getName();
            } else if (!title.equals(taskArt.getName())) {
               return "";
            }
         }
      }
      return AXml.textToXml(title);
   }

   @Override
   public String getDescriptionUsage() {
      return "Create Action from task and relate using supporting information relation.";
   }

   /**
    * @return the defaultTeamWorkflows
    */
   public Collection<TaskArtifact> getDefaultTeamWorkflows() {
      return taskArtifacts;
   }

   /**
    * @param taskArtifacts the defaultTeamWorkflows to set
    */
   public void setDefaultTeamWorkflows(Collection<? extends TaskArtifact> taskArtifacts) {
      this.taskArtifacts = new LinkedList<TaskArtifact>();
      this.taskArtifacts.addAll(taskArtifacts);
   }

   @Override
   public String getName() {
      return "Create Actions from Tasks";
   }

   @Override
   public Collection<String> getCategories() {
      return Arrays.asList("ATS");
   }

}