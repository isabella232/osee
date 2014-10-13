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
package org.eclipse.osee.ats.task;

import java.util.List;
import java.util.logging.Level;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.osee.ats.actions.DeleteTasksAction;
import org.eclipse.osee.ats.actions.DeleteTasksAction.TaskArtifactProvider;
import org.eclipse.osee.ats.actions.ImportTasksViaSimpleList;
import org.eclipse.osee.ats.actions.ImportTasksViaSpreadsheet;
import org.eclipse.osee.ats.actions.NewAction;
import org.eclipse.osee.ats.actions.OpenNewAtsTaskEditorAction;
import org.eclipse.osee.ats.actions.OpenNewAtsTaskEditorSelected;
import org.eclipse.osee.ats.actions.TaskAddAction;
import org.eclipse.osee.ats.core.client.task.AbstractTaskableArtifact;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.editor.SMAEditor;
import org.eclipse.osee.ats.export.AtsExportAction;
import org.eclipse.osee.ats.help.ui.AtsHelpContext;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.world.AtsXWidgetActionFormPage;
import org.eclipse.osee.ats.world.WorldAssigneeFilter;
import org.eclipse.osee.ats.world.WorldCompletedFilter;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.HelpUtil;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageManager;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.action.RefreshAction;
import org.eclipse.osee.framework.ui.skynet.util.DbConnectionUtility;
import org.eclipse.osee.framework.ui.skynet.widgets.util.IDynamicWidgetLayoutListener;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Donald G. Dunne
 */
public class TaskTabXWidgetActionPage extends AtsXWidgetActionFormPage {

   private final SMAEditor smaEditor;
   private TaskComposite taskComposite;
   private final WorldCompletedFilter worldCompletedFilter = new WorldCompletedFilter();
   private WorldAssigneeFilter worldAssigneeFilter = null;
   private Action filterCompletedAction, filterMyAssigneeAction;

   public TaskTabXWidgetActionPage(SMAEditor smaEditor) {
      super(smaEditor, "org.eclipse.osee.ats.actionPage", getTabName(smaEditor.getAwa()));
      this.smaEditor = smaEditor;
   }

   @Override
   public Section createResultsSection(Composite body) throws OseeCoreException {
      resultsSection = toolkit.createSection(body, ExpandableComposite.NO_TITLE);
      resultsSection.setText("Results");
      resultsSection.setLayoutData(new GridData(GridData.FILL_BOTH));

      resultsContainer = toolkit.createClientContainer(resultsSection, 1);
      taskComposite = new TaskComposite(smaEditor, resultsContainer, SWT.BORDER);
      HelpUtil.setHelp(taskComposite, AtsHelpContext.WORKFLOW_EDITOR__TASK_TAB);
      taskComposite.loadTable();
      return resultsSection;
   }

   public TaskComposite getTaskComposite() {
      return taskComposite;
   }

   @Override
   public void createPartControl(Composite parent) {
      super.createPartControl(parent);
      scrolledForm.setImage(ArtifactImageManager.getImage(smaEditor.getAwa()));
      String title = smaEditor.getAwa().getName();
      if (title.length() > 80) {
         title = title.substring(0, 80 - 1) + "...";
      }
      scrolledForm.setText(String.format("Tasks for \"%s\"", title));

      Result result = DbConnectionUtility.areOSEEServicesAvailable();
      if (result.isFalse()) {
         AWorkbench.popup("ERROR", "DB Relation Unavailable");
         return;
      }
   }

   private static String getTabName(AbstractWorkflowArtifact awa) {
      try {
         if (awa instanceof AbstractTaskableArtifact) {
            return String.format("Tasks (%d)", ((AbstractTaskableArtifact) awa).getTaskArtifacts().size());
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return "Tasks";
   }

   @Override
   public IDynamicWidgetLayoutListener getDynamicWidgetLayoutListener() {
      return null;
   }

   @Override
   public Result isResearchSearchValid() {
      return smaEditor.isDirty() ? new Result("Changes un-saved. Save first.") : Result.TrueResult;
   }

   @Override
   public String getXWidgetsXml() {
      return null;
   }

   @Override
   public void handleSearchButtonPressed() {
      // do nothing
   }

   @Override
   protected void createToolBar(IToolBarManager toolBarManager) {
      super.createToolBar(toolBarManager);

      try {
         if (taskComposite.getIXTaskViewer().isTasksEditable()) {
            toolBarManager.add(new TaskAddAction(taskComposite));
            TaskArtifactProvider taskProvider = new TaskArtifactProvider() {

               @Override
               public List<TaskArtifact> getSelectedArtifacts() {
                  return taskComposite.getSelectedTaskArtifactItems();
               }
            };
            toolBarManager.add(new DeleteTasksAction(taskProvider));
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      toolBarManager.add(new Separator());
      toolBarManager.add(taskComposite.getTaskXViewer().getCustomizeAction());
      toolBarManager.add(new Separator());
      toolBarManager.add(new OpenNewAtsTaskEditorAction(taskComposite));
      toolBarManager.add(new OpenNewAtsTaskEditorSelected(taskComposite));
      toolBarManager.add(new Separator());
      toolBarManager.add(new RefreshAction(taskComposite));
      toolBarManager.add(new Separator());
      toolBarManager.add(new NewAction());
      toolBarManager.add(new Separator());
      createDropDownMenuActions();
      toolBarManager.add(new DropDownAction());

   }

   public class DropDownAction extends Action implements IMenuCreator {
      private Menu fMenu;

      public DropDownAction() {
         setText("Other");
         setMenuCreator(this);
         setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.GEAR));
         addKeyListener();
      }

      @Override
      public Menu getMenu(Control parent) {
         if (fMenu != null) {
            fMenu.dispose();
         }

         fMenu = new Menu(parent);

         addActionToMenu(fMenu, filterCompletedAction);
         addActionToMenu(fMenu, filterMyAssigneeAction);
         new MenuItem(fMenu, SWT.SEPARATOR);
         addActionToMenu(fMenu, new AtsExportAction(taskComposite.getTaskXViewer()));
         try {
            if (taskComposite.getIXTaskViewer().isTasksEditable()) {
               addActionToMenu(fMenu, new ImportTasksViaSpreadsheet(
                  (AbstractTaskableArtifact) taskComposite.getIXTaskViewer().getAwa(), null));
               addActionToMenu(fMenu, new ImportTasksViaSimpleList(
                  (AbstractTaskableArtifact) taskComposite.getIXTaskViewer().getAwa(), null));

            }
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         }

         return fMenu;
      }

      @Override
      public void dispose() {
         if (fMenu != null) {
            fMenu.dispose();
            fMenu = null;
         }
      }

      @Override
      public Menu getMenu(Menu parent) {
         return null;
      }

      protected void addActionToMenu(Menu parent, Action action) {
         ActionContributionItem item = new ActionContributionItem(action);
         item.fill(parent, -1);
      }

      void clear() {
         dispose();
      }

      private void addKeyListener() {
         taskComposite.getTaskXViewer().getTree().addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent event) {
               // do nothing
            }

            @Override
            public void keyReleased(KeyEvent event) {
               if ((event.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL) {
                  if (event.keyCode == 'a') {
                     taskComposite.getTaskXViewer().getTree().setSelection(
                        taskComposite.getTaskXViewer().getTree().getItems());
                  } else if (event.keyCode == 'f') {
                     filterCompletedAction.setChecked(!filterCompletedAction.isChecked());
                     filterCompletedAction.run();
                  } else if (event.keyCode == 'g') {
                     filterMyAssigneeAction.setChecked(!filterMyAssigneeAction.isChecked());
                     filterMyAssigneeAction.run();
                  } else if (event.keyCode == 'd') {
                     filterMyAssigneeAction.setChecked(!filterMyAssigneeAction.isChecked());
                     filterCompletedAction.setChecked(!filterCompletedAction.isChecked());
                     filterCompletedAction.run();
                     filterMyAssigneeAction.run();
                  }
               }
            }
         });
      }
   }

   public void updateExtendedStatusString() {
      taskComposite.getTaskXViewer().setExtendedStatusString(
      //
         (filterCompletedAction.isChecked() ? "[Complete/Cancel Filter]" : "") +
         //
         (filterMyAssigneeAction.isChecked() ? "[My Assignee Filter]" : ""));
   }

   protected void createDropDownMenuActions() {
      try {
         worldAssigneeFilter = new WorldAssigneeFilter();
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }

      filterCompletedAction = new Action("Filter Out Completed/Cancelled - Ctrl-F", IAction.AS_CHECK_BOX) {

         @Override
         public void run() {
            if (filterCompletedAction.isChecked()) {
               taskComposite.getTaskXViewer().addFilter(worldCompletedFilter);
            } else {
               taskComposite.getTaskXViewer().removeFilter(worldCompletedFilter);
            }
            updateExtendedStatusString();
            taskComposite.getTaskXViewer().refresh();
         }
      };
      filterCompletedAction.setToolTipText("Filter Out Completed/Cancelled - Ctrl-F");
      filterCompletedAction.setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.GREEN_PLUS));

      filterMyAssigneeAction = new Action("Filter My Assignee - Ctrl-G", IAction.AS_CHECK_BOX) {

         @Override
         public void run() {
            if (filterMyAssigneeAction.isChecked()) {
               taskComposite.getTaskXViewer().addFilter(worldAssigneeFilter);
            } else {
               taskComposite.getTaskXViewer().removeFilter(worldAssigneeFilter);
            }
            updateExtendedStatusString();
            taskComposite.getTaskXViewer().refresh();
         }
      };
      filterMyAssigneeAction.setToolTipText("Filter My Assignee - Ctrl-G");
      filterMyAssigneeAction.setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.USER));
   }

   @Override
   public void handleSaveButtonPressed() {
      // do nothing
   }

   @Override
   public boolean isSaveButtonAvailable() {
      return false;
   }

}
