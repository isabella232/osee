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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.api.workdef.IStateToken;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.util.AtsChangeSet;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.world.AtsMetricsComposite;
import org.eclipse.osee.ats.world.IAtsMetricsProvider;
import org.eclipse.osee.ats.world.WorldEditor;
import org.eclipse.osee.ats.world.WorldEditorParameterSearchItemProvider;
import org.eclipse.osee.ats.world.search.WorldSearchItem.SearchType;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.skynet.OseeStatusContributionItemFactory;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.AbstractArtifactEditor;
import org.eclipse.osee.framework.ui.swt.CursorManager;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * @author Donald G. Dunne
 */
public class TaskEditor extends AbstractArtifactEditor implements IAtsMetricsProvider, IXTaskViewer {
   public static final String EDITOR_ID = "org.eclipse.osee.ats.editor.TaskEditor";
   private int mainPageIndex, metricsPageIndex;
   private TaskEditorXWidgetActionPage taskActionPage;
   private final Collection<TaskArtifact> tasks = new CopyOnWriteArraySet<TaskArtifact>();
   private boolean loading = false;
   public final static int TITLE_MAX_LENGTH = WorldEditor.TITLE_MAX_LENGTH;

   @Override
   public void doSave(IProgressMonitor monitor) {
      try {
         AtsChangeSet changes = new AtsChangeSet("Task Editor Save");
         for (TaskArtifact taskArt : tasks) {
            changes.add(taskArt);
         }
         changes.execute();
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      onDirtied();
   }

   public List<Artifact> getLoadedArtifacts() {
      return taskActionPage.getTaskComposite().getTaskXViewer().getLoadedArtifacts();
   }

   @Override
   public boolean isSaveOnCloseNeeded() {
      return isDirty();
   }

   public static Collection<TaskEditor> getEditors() {
      final List<TaskEditor> editors = new ArrayList<TaskEditor>();
      Displays.pendInDisplayThread(new Runnable() {
         @Override
         public void run() {
            for (IEditorReference editor : AWorkbench.getEditors(EDITOR_ID)) {
               editors.add((TaskEditor) editor.getEditor(false));
            }
         }
      });
      return editors;
   }

   public static void closeAll() {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            for (IEditorReference editor : AWorkbench.getEditors(EDITOR_ID)) {
               AWorkbench.getActivePage().closeEditor(editor.getEditor(false), false);
            }
         }
      });
   }

   public void setTableTitle(final String title, final boolean warning) {
      taskActionPage.setTableTitle(title, warning);
   }

   @Override
   public void dispose() {
      for (TaskArtifact taskArt : tasks) {
         if (taskArt != null && !taskArt.isDeleted() && taskArt.isSMAEditorDirty().isTrue()) {
            taskArt.revertSMA();
         }
      }
      if (taskActionPage != null && taskActionPage.getTaskComposite() != null) {
         taskActionPage.getTaskComposite().disposeComposite();
      }
      super.dispose();
   }

   @Override
   public boolean isDirty() {
      for (TaskArtifact taskArt : tasks) {
         if (taskArt.isDeleted()) {
            continue;
         } else if (taskArt.isSMAEditorDirty().isTrue()) {
            return true;
         }
      }
      return false;
   }

   @Override
   public String toString() {
      return "TaskEditor";
   }

   /**
    * @return the taskActionPage
    */
   public TaskEditorXWidgetActionPage getTaskActionPage() {
      return taskActionPage;
   }

   @Override
   protected void addPages() {

      try {
         OseeStatusContributionItemFactory.addTo(this, true);

         IEditorInput editorInput = getEditorInput();
         if (!(editorInput instanceof TaskEditorInput)) {
            throw new OseeArgumentException("Editor Input not TaskEditorInput");
         }

         createMainTab();
         createMetricsTab();

         setActivePage(mainPageIndex);
         loadTable();

         getSite().setSelectionProvider(getTaskActionPage().getTaskComposite().getTaskXViewer());

      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      } catch (PartInitException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   private void createMainTab() throws PartInitException {
      taskActionPage = new TaskEditorXWidgetActionPage(this);
      mainPageIndex = addPage(taskActionPage);
   }

   private void createMetricsTab() {
      Composite comp = AtsUtil.createCommonPageComposite(getContainer());
      AtsUtil.createCommonToolBar(comp);
      new AtsMetricsComposite(this, comp, SWT.NONE);
      metricsPageIndex = addPage(comp);
      setPageText(metricsPageIndex, "Metrics");
   }

   public ITaskEditorProvider getTaskEditorProvider() {
      TaskEditorInput aei = (TaskEditorInput) getEditorInput();
      return aei.getItaskEditorProvider();
   }

   private void loadTable() throws OseeCoreException {
      ITaskEditorProvider provider = getTaskEditorProvider();
      setPartName(provider.getTaskEditorLabel(SearchType.Search));

      if (provider instanceof TaskEditorParameterSearchItemProvider && ((TaskEditorParameterSearchItemProvider) provider).isFirstTime()) {
         setPartName(provider.getName());
         setTableTitle(WorldEditorParameterSearchItemProvider.ENTER_OPTIONS_AND_SELECT_SEARCH, false);
         return;
      }
      if (provider instanceof TaskEditorParameterSearchItemProvider) {
         Result result =
            ((TaskEditorParameterSearchItemProvider) provider).getWorldSearchItem().isParameterSelectionValid();
         if (result.isFalse()) {
            AWorkbench.popup(result);
            return;
         }
      }
      if (loading) {
         AWorkbench.popup("Already Loading, Please Wait");
         return;
      }
      LoadTableJob job = null;
      job = new LoadTableJob(provider, SearchType.ReSearch, this);
      job.setUser(false);
      job.setPriority(Job.LONG);
      job.schedule();
      if (provider.getTableLoadOptions() != null && provider.getTableLoadOptions().contains(TableLoadOption.ForcePend)) {
         try {
            job.join();
         } catch (InterruptedException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
   }

   @Override
   public void onDirtied() {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            firePropertyChange(PROP_DIRTY);
         }
      });
   }

   public static void open(final ITaskEditorProvider provider) throws OseeCoreException {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            IWorkbenchPage page = AWorkbench.getActivePage();
            try {
               page.openEditor(new TaskEditorInput(provider), EDITOR_ID);
            } catch (PartInitException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      }, (provider.getTableLoadOptions() != null && provider.getTableLoadOptions().contains(TableLoadOption.ForcePend)));
   }

   private static class LoadTableJob extends Job {

      private final ITaskEditorProvider itaskEditorProvider;
      private final TaskEditor taskEditor;
      private final SearchType searchType;

      public LoadTableJob(ITaskEditorProvider itaskEditorProvider, SearchType searchType, TaskEditor taskEditor) throws OseeCoreException {
         super("Loading \"" + itaskEditorProvider.getTaskEditorLabel(searchType) + "\"");
         this.searchType = searchType;
         this.taskEditor = taskEditor;
         taskEditor.setPartName(itaskEditorProvider.getTaskEditorLabel(searchType));
         taskEditor.setTableTitle("Loading \"" + itaskEditorProvider.getTaskEditorLabel(searchType) + "\"", false);
         this.itaskEditorProvider = itaskEditorProvider;
      }

      @Override
      protected IStatus run(IProgressMonitor monitor) {
         if (taskEditor.isLoading()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Already Loading, Please Wait", null);
         }
         try {
            taskEditor.setLoading(true);
            taskEditor.getTaskActionPage().getTaskComposite().getTaskXViewer().clear(true);
            final List<TaskArtifact> taskArts = new ArrayList<TaskArtifact>();
            for (Artifact artifact : itaskEditorProvider.getTaskEditorTaskArtifacts()) {
               if (artifact.isOfType(AtsArtifactTypes.Task)) {
                  taskArts.add((TaskArtifact) artifact);
               }
            }
            taskEditor.tasks.clear();
            taskEditor.tasks.addAll(taskArts);
            Displays.pendInDisplayThread(new Runnable() {
               @Override
               public void run() {
                  try {
                     taskEditor.setPartName(itaskEditorProvider.getTaskEditorLabel(searchType));
                     if (taskArts.isEmpty()) {
                        taskEditor.setTableTitle(
                           "No Results Found - " + itaskEditorProvider.getTaskEditorLabel(searchType), true);
                     } else {
                        taskEditor.setTableTitle(itaskEditorProvider.getTaskEditorLabel(searchType), false);
                     }
                     taskEditor.getTaskActionPage().getTaskComposite().loadTable();
                  } catch (OseeCoreException ex) {
                     OseeLog.log(Activator.class, Level.SEVERE, ex);
                  }
               }
            });
         } catch (final Exception ex) {
            monitor.done();
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Can't load tasks", ex);
         } finally {
            taskEditor.setLoading(false);
         }
         monitor.done();
         return Status.OK_STATUS;
      }
   }

   @Override
   public Collection<? extends Artifact> getMetricsWorkItems() {
      return tasks;
   }

   @Override
   public IAtsVersion getMetricsVersion() throws OseeCoreException {
      for (TaskArtifact taskArt : tasks) {
         if (AtsClientService.get().getVersionService().hasTargetedVersion(taskArt)) {
            return AtsClientService.get().getVersionService().getTargetedVersion(taskArt);
         }
      }
      return null;
   }

   @Override
   public String getCurrentStateName() {
      return "";
   }

   @Override
   public IDirtiableEditor getEditor() {
      return this;
   }

   @Override
   public AbstractWorkflowArtifact getAwa() {
      return null;
   }

   @Override
   public String getTabName() {
      return "Tasks";
   }

   @Override
   public Collection<TaskArtifact> getTaskArtifacts(IStateToken state) {
      return tasks;
   }

   @Override
   public Collection<TaskArtifact> getTaskArtifacts() {
      return tasks;
   }

   @Override
   public boolean isTaskable() {
      return false;
   }

   @Override
   public boolean isTasksEditable() {
      return true;
   }

   @Override
   public boolean isRefreshActionHandled() {
      return true;
   }

   @Override
   public void handleRefreshAction() throws OseeCoreException {
      loadTable();
   }

   @Override
   public double getManHoursPerDayPreference() throws OseeCoreException {
      if (tasks.isEmpty()) {
         return AtsUtilCore.DEFAULT_HOURS_PER_WORK_DAY;
      }
      return tasks.iterator().next().getManHrsPerDayPreference();
   }

   public boolean isLoading() {
      return loading;
   }

   public void setLoading(final boolean loading) {
      this.loading = loading;
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (Widgets.isAccessible(taskActionPage.getTaskComposite())) {
               if (loading) {
                  taskActionPage.getTaskComposite().setCursor(CursorManager.getCursor(SWT.CURSOR_WAIT));
               } else {
                  taskActionPage.getTaskComposite().setCursor(null);
               }
            }
         }
      });
   }

   @Override
   protected void pageChange(int newPageIndex) {
      super.pageChange(newPageIndex);
      if (newPageIndex != -1 && pages.size() > newPageIndex) {
         Object page = pages.get(newPageIndex);
         if (page != null) {
            ISelectionProvider provider = taskActionPage.getTaskComposite().getTaskXViewer();
            String title = getPageText(newPageIndex);
            if (title.equalsIgnoreCase("metrics")) {
               provider = null;
            }
            getSite().setSelectionProvider(provider);
         }
      }
   }
}
