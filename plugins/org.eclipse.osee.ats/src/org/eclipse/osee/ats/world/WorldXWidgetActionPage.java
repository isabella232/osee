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
package org.eclipse.osee.ats.world;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.actions.NewAction;
import org.eclipse.osee.ats.actions.OpenNewAtsWorldEditorAction;
import org.eclipse.osee.ats.actions.OpenNewAtsWorldEditorSelectedAction;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.artifact.GoalManager;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.review.ReviewManager;
import org.eclipse.osee.ats.core.client.task.AbstractTaskableArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.WorkflowMetrics;
import org.eclipse.osee.ats.world.search.WorldSearchItem.SearchType;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.action.CollapseAllAction;
import org.eclipse.osee.framework.ui.skynet.action.ExpandAllAction;
import org.eclipse.osee.framework.ui.skynet.action.RefreshAction;
import org.eclipse.osee.framework.ui.skynet.util.DbConnectionUtility;
import org.eclipse.osee.framework.ui.skynet.widgets.util.IDynamicWidgetLayoutListener;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Donald G. Dunne
 */
public class WorldXWidgetActionPage extends AtsXWidgetActionFormPage {

   public static final String ID = "org.eclipse.osee.ats.actionPage";
   public static final String MENU_GROUP_PRE = "world.menu.group.pre";
   private final WorldEditor worldEditor;
   private WorldComposite worldComposite;
   private Action filterCompletedAction, filterMyAssigneeAction, selectionMetricsAction, toAction, toGoal, toReview,
      toWorkFlow, toTask;
   private final WorldCompletedFilter worldCompletedFilter = new WorldCompletedFilter();
   private WorldAssigneeFilter worldAssigneeFilter = null;
   protected Label showReleaseMetricsLabel;

   public WorldComposite getWorldComposite() {
      return worldComposite;
   }

   public WorldXWidgetActionPage(WorldEditor worldEditor) {
      super(worldEditor, ID, "Actions");
      this.worldEditor = worldEditor;
   }

   @Override
   public void createPartControl(Composite parent) {
      super.createPartControl(parent);
      scrolledForm.setImage(ImageManager.getImage(AtsImage.GLOBE));
      GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
      gd.widthHint = 200;
      parent.setLayoutData(gd);

      Result result = DbConnectionUtility.areOSEEServicesAvailable();
      if (result.isFalse()) {
         AWorkbench.popup("ERROR", "DB Relation Unavailable");
         return;
      }

      try {
         worldEditor.getWorldEditorProvider().run(worldEditor, SearchType.Search, false);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   protected void createToolBar(IToolBarManager toolBarManager) {

      toolBarManager.add(new GroupMarker(MENU_GROUP_PRE));
      toolBarManager.add(worldComposite.getXViewer().getCustomizeAction());
      toolBarManager.add(new Separator());
      toolBarManager.add(new OpenNewAtsWorldEditorAction(worldComposite));
      toolBarManager.add(new OpenNewAtsWorldEditorSelectedAction(worldComposite));
      toolBarManager.add(new Separator());
      toolBarManager.add(new ExpandAllAction(worldComposite.getXViewer()));
      toolBarManager.add(new CollapseAllAction(worldComposite.getXViewer()));
      toolBarManager.add(new RefreshAction(worldComposite));
      toolBarManager.add(new Separator());
      toolBarManager.add(new NewAction());
      toolBarManager.add(new Separator());

      createDropDownMenuActions();
      toolBarManager.add(new DropDownAction());

      try {
         if (worldEditor.getWorldEditorProvider() instanceof IWorldEditorParameterProvider) {
            ((IWorldEditorParameterProvider) worldEditor.getWorldEditorProvider()).createToolbar(toolBarManager);
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }

   }

   @Override
   public Section createResultsSection(Composite body) {

      resultsSection = toolkit.createSection(body, ExpandableComposite.NO_TITLE);
      resultsSection.setText("Results");
      resultsSection.setLayoutData(new GridData(GridData.FILL_BOTH));

      resultsContainer = toolkit.createClientContainer(resultsSection, 1);

      showReleaseMetricsLabel = toolkit.createLabel(resultsContainer, "");
      showReleaseMetricsLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      worldComposite = new WorldComposite("world.editor.main", worldEditor, resultsContainer, SWT.BORDER);
      toolkit.adapt(worldComposite);
      return resultsSection;
   }

   @Override
   public IDynamicWidgetLayoutListener getDynamicWidgetLayoutListener() throws OseeArgumentException {
      if (worldEditor.getWorldEditorProvider() instanceof IWorldEditorParameterProvider) {
         return ((IWorldEditorParameterProvider) worldEditor.getWorldEditorProvider()).getDynamicWidgetLayoutListener();
      }
      return null;
   }

   @Override
   public Result isResearchSearchValid() {
      return worldEditor.isDirty() ? new Result("Changes un-saved. Save first.") : Result.TrueResult;
   }

   public void reSearch() throws OseeCoreException {
      Result result = isResearchSearchValid();
      if (result.isFalse()) {
         AWorkbench.popup(result);
         return;
      }
      reSearch(false);
   }

   /*
    * Mainly for testing purposes
    */
   public void reSearch(boolean forcePend) throws OseeCoreException {
      worldEditor.getWorldEditorProvider().run(worldEditor, SearchType.ReSearch, forcePend);
   }

   @Override
   public String getXWidgetsXml() throws OseeCoreException {
      if (worldEditor.getWorldEditorProvider() instanceof IWorldEditorParameterProvider) {
         return ((IWorldEditorParameterProvider) worldEditor.getWorldEditorProvider()).getParameterXWidgetXml();
      }
      return null;
   }

   @Override
   public void handleSearchButtonPressed() {
      try {
         reSearch();
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   public class DropDownAction extends Action implements IMenuCreator {
      private Menu fMenu;

      public DropDownAction() {
         setText("Other");
         setMenuCreator(this);
         setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.GEAR));
         addKeyListener();
         addSelectionListener();
      }

      @Override
      public Menu getMenu(Control parent) {
         if (fMenu != null) {
            fMenu.dispose();
         }

         fMenu = new Menu(parent);
         addActionToMenu(fMenu, selectionMetricsAction);
         addActionToMenu(fMenu, filterCompletedAction);
         addActionToMenu(fMenu, filterMyAssigneeAction);
         new MenuItem(fMenu, SWT.SEPARATOR);
         addActionToMenu(fMenu, toAction);
         addActionToMenu(fMenu, toGoal);
         addActionToMenu(fMenu, toWorkFlow);
         addActionToMenu(fMenu, toTask);
         addActionToMenu(fMenu, toReview);

         worldEditor.createToolBarPulldown(fMenu);

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

      @Override
      public void run() {
         // provided for subclass implementation
      }

      /**
       * Get's rid of the menu, because the menu hangs on to * the searches, etc.
       */
      void clear() {
         dispose();
      }

      private void addKeyListener() {
         Tree tree = worldComposite.getXViewer().getTree();
         GridData gridData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL);
         gridData.heightHint = 100;
         gridData.widthHint = 100;
         tree.setLayoutData(gridData);
         tree.setHeaderVisible(true);
         tree.setLinesVisible(true);

         worldComposite.getXViewer().getTree().addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent event) {
               // do nothing
            }

            @Override
            public void keyReleased(KeyEvent event) {
               // if CTRL key is already pressed
               if ((event.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL) {
                  if (event.keyCode == 'a') {
                     worldComposite.getXViewer().getTree().setSelection(
                        worldComposite.getXViewer().getTree().getItems());
                  } else if (event.keyCode == 'x') {
                     selectionMetricsAction.setChecked(!selectionMetricsAction.isChecked());
                     selectionMetricsAction.run();
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

   private void addSelectionListener() {
      worldComposite.getXViewer().getTree().addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            if (selectionMetricsAction != null) {
               if (selectionMetricsAction.isChecked()) {
                  selectionMetricsAction.run();
               } else {
                  if (worldComposite != null) {
                     showReleaseMetricsLabel.setText("");
                  }
               }
            }
         }
      });
   }

   public void updateExtraInfoLine() throws OseeCoreException {
      if (selectionMetricsAction != null && selectionMetricsAction.isChecked()) {
         if (worldComposite.getXViewer() != null && worldComposite.getXViewer().getSelectedSMAArtifacts() != null && !worldComposite.getXViewer().getSelectedSMAArtifacts().isEmpty()) {
            showReleaseMetricsLabel.setText(
               WorkflowMetrics.getEstRemainMetrics(worldComposite.getXViewer().getSelectedSMAArtifacts(), null,
                  worldComposite.getXViewer().getSelectedSMAArtifacts().iterator().next().getManHrsPerDayPreference(),
                  null));
         } else {
            showReleaseMetricsLabel.setText("");
         }
      } else {
         showReleaseMetricsLabel.setText("");
      }
      showReleaseMetricsLabel.getParent().layout();
   }

   protected void createDropDownMenuActions() {
      try {
         worldAssigneeFilter = new WorldAssigneeFilter();
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }

      selectionMetricsAction = new Action("Show Release Metrics by Selection - Ctrl-X", IAction.AS_CHECK_BOX) {
         @Override
         public void run() {
            try {
               updateExtraInfoLine();
            } catch (Exception ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      };
      selectionMetricsAction.setToolTipText("Show Release Metrics by Selection - Ctrl-X");
      selectionMetricsAction.setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.PAGE));

      filterCompletedAction = new Action("Filter Out Completed/Cancelled - Ctrl-F", IAction.AS_CHECK_BOX) {

         @Override
         public void run() {
            if (filterCompletedAction.isChecked()) {
               worldComposite.getXViewer().addFilter(worldCompletedFilter);
            } else {
               worldComposite.getXViewer().removeFilter(worldCompletedFilter);
            }
            updateExtendedStatusString();
            worldComposite.getXViewer().refresh();
         }
      };
      filterCompletedAction.setToolTipText("Filter Out Completed/Cancelled - Ctrl-F");
      filterCompletedAction.setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.GREEN_PLUS));

      filterMyAssigneeAction = new Action("Filter My Assignee - Ctrl-G", IAction.AS_CHECK_BOX) {

         @Override
         public void run() {
            if (filterMyAssigneeAction.isChecked()) {
               worldComposite.getXViewer().addFilter(worldAssigneeFilter);
            } else {
               worldComposite.getXViewer().removeFilter(worldAssigneeFilter);
            }
            updateExtendedStatusString();
            worldComposite.getXViewer().refresh();
         }
      };
      filterMyAssigneeAction.setToolTipText("Filter My Assignee - Ctrl-G");
      filterMyAssigneeAction.setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.USER));

      toAction = new Action("Re-display as Actions", IAction.AS_PUSH_BUTTON) {

         @Override
         public void run() {
            redisplayAsAction();
         }
      };
      toAction.setToolTipText("Re-display as Actions");
      toAction.setImageDescriptor(ImageManager.getImageDescriptor(AtsImage.ACTION));

      toGoal = new Action("Re-display as Goals", IAction.AS_PUSH_BUTTON) {

         @Override
         public void run() {
            redisplayAsGoals();
         }
      };
      toGoal.setToolTipText("Re-display as Goals");
      toGoal.setImageDescriptor(ImageManager.getImageDescriptor(AtsImage.GOAL));

      toWorkFlow = new Action("Re-display as WorkFlows", IAction.AS_PUSH_BUTTON) {

         @Override
         public void run() {
            redisplayAsWorkFlow();
         }
      };
      toWorkFlow.setToolTipText("Re-display as WorkFlows");
      toWorkFlow.setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.WORKFLOW));

      toTask = new Action("Re-display as Tasks", IAction.AS_PUSH_BUTTON) {

         @Override
         public void run() {
            redisplayAsTask();
         }
      };
      toTask.setToolTipText("Re-display as Tasks");
      toTask.setImageDescriptor(ImageManager.getImageDescriptor(AtsImage.TASK));

      toReview = new Action("Re-display as Reviews", IAction.AS_PUSH_BUTTON) {

         @Override
         public void run() {
            redisplayAsReviews();
         }
      };
      toReview.setToolTipText("Re-display as Reviews");
      toReview.setImageDescriptor(ImageManager.getImageDescriptor(AtsImage.REVIEW));

   }

   public void redisplayAsAction() {
      final List<Artifact> artifacts = worldComposite.getXViewer().getLoadedArtifacts();
      Job job = new Job("Re-display as Actions") {
         @Override
         protected IStatus run(IProgressMonitor monitor) {
            try {
               final Set<Artifact> arts = new HashSet<>();
               for (Artifact art : artifacts) {
                  if (art.isOfType(AtsArtifactTypes.Action)) {
                     arts.add(art);
                  } else if (art instanceof AbstractWorkflowArtifact) {
                     Artifact parentArt = ((AbstractWorkflowArtifact) art).getParentActionArtifact();
                     if (parentArt != null) {
                        arts.add(parentArt);
                     }
                  }
               }
               worldComposite.load(worldEditor.getWorldXWidgetActionPage().getCurrentTitleLabel(), arts);
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
            return Status.OK_STATUS;
         }
      };
      Jobs.startJob(job, true);
   }

   public void redisplayAsGoals() {
      final List<Artifact> artifacts = worldComposite.getXViewer().getLoadedArtifacts();
      Job job = new Job("Re-display as Goals") {
         @Override
         protected IStatus run(IProgressMonitor monitor) {
            try {
               final Set<Artifact> goals = new HashSet<>();
               new GoalManager().getCollectors(artifacts, goals, true);
               worldComposite.load(worldEditor.getWorldXWidgetActionPage().getCurrentTitleLabel(), goals);
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
            return Status.OK_STATUS;
         }
      };
      Jobs.startJob(job, true);
   }

   public void redisplayAsWorkFlow() {
      final List<Artifact> artifacts = worldComposite.getXViewer().getLoadedArtifacts();
      Job job = new Job("Re-display as Workflows") {
         @Override
         protected IStatus run(IProgressMonitor monitor) {
            try {
               final Set<Artifact> arts = new HashSet<>();
               for (Artifact art : artifacts) {
                  if (art.isOfType(AtsArtifactTypes.Action)) {
                     arts.addAll(ActionManager.getTeams(art));
                  } else if (art instanceof AbstractWorkflowArtifact) {
                     Artifact parentArt = ((AbstractWorkflowArtifact) art).getParentTeamWorkflow();
                     if (parentArt != null) {
                        arts.add(parentArt);
                     }
                  }
               }
               worldComposite.load(worldEditor.getWorldXWidgetActionPage().getCurrentTitleLabel(), arts);
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
            return Status.OK_STATUS;
         }
      };
      Jobs.startJob(job, true);
   }

   public void redisplayAsTask() {
      final List<Artifact> artifacts = worldComposite.getXViewer().getLoadedArtifacts();
      Job job = new Job("Re-display as Tasks") {
         @Override
         protected IStatus run(IProgressMonitor monitor) {
            try {
               final Set<Artifact> arts = new HashSet<>();
               for (Artifact art : artifacts) {
                  if (art.isOfType(AtsArtifactTypes.Action)) {
                     for (TeamWorkFlowArtifact team : ActionManager.getTeams(art)) {
                        arts.addAll(team.getTaskArtifacts());
                     }
                  } else if (art instanceof AbstractTaskableArtifact) {
                     arts.addAll(((AbstractTaskableArtifact) art).getTaskArtifacts());
                  }
               }
               worldComposite.load(worldEditor.getWorldXWidgetActionPage().getCurrentTitleLabel(), arts);
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
            return Status.OK_STATUS;
         }
      };
      Jobs.startJob(job, true);
   }

   public void redisplayAsReviews() {
      final List<Artifact> artifacts = worldComposite.getXViewer().getLoadedArtifacts();
      Job job = new Job("Re-display as Reviews") {
         @Override
         protected IStatus run(IProgressMonitor monitor) {
            try {
               final Set<Artifact> arts = new HashSet<>();
               for (Artifact art : artifacts) {
                  if (art.isOfType(AtsArtifactTypes.Action)) {
                     for (TeamWorkFlowArtifact team : ActionManager.getTeams(art)) {
                        arts.addAll(ReviewManager.getReviews(team));
                     }
                  } else if (art.isOfType(AtsArtifactTypes.TeamWorkflow)) {
                     arts.addAll(ReviewManager.getReviews((TeamWorkFlowArtifact) art));
                  }
               }
               worldComposite.load(worldEditor.getWorldXWidgetActionPage().getCurrentTitleLabel(), arts);
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
            return Status.OK_STATUS;
         }
      };
      Jobs.startJob(job, true);
   }

   public void updateExtendedStatusString() {
      worldComposite.getXViewer().setExtendedStatusString(
         //
         (filterCompletedAction.isChecked() ? "[Complete/Cancel Filter]" : "") +
         //
         (filterMyAssigneeAction.isChecked() ? "[My Assignee Filter]" : ""));
   }

   @Override
   public void handleSaveButtonPressed() {
      try {
         if (isSaveButtonAvailable() && worldEditor.getWorldEditorProvider() instanceof IWorldEditorParameterProvider) {
            ((IWorldEditorParameterProvider) worldEditor.getWorldEditorProvider()).handleSaveButtonPressed();
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public boolean isSaveButtonAvailable() {
      try {
         if (worldEditor.getWorldEditorProvider() instanceof IWorldEditorParameterProvider) {
            return ((IWorldEditorParameterProvider) worldEditor.getWorldEditorProvider()).isSaveButtonAvailable();
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return false;
   }

   @Override
   public void createParametersSectionCompleted(IManagedForm managedForm, Composite mainComp) {
      try {
         if (worldEditor.getWorldEditorProvider() instanceof IWorldEditorParameterProvider) {
            IWorldEditorParameterProvider provider =
               (IWorldEditorParameterProvider) worldEditor.getWorldEditorProvider();
            provider.createParametersSectionCompleted(managedForm, mainComp);
            String editorTitle = provider.getSelectedName(SearchType.Search);
            if (Strings.isValid(editorTitle)) {
               ((WorldEditor) getEditor()).setEditorTitle(editorTitle);
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

}
