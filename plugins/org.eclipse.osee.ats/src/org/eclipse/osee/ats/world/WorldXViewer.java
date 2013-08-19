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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.nebula.widgets.xviewer.IMultiColumnEditProvider;
import org.eclipse.nebula.widgets.xviewer.IXViewerFactory;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.action.ColumnMultiEditAction;
import org.eclipse.nebula.widgets.xviewer.customize.XViewerCustomMenu;
import org.eclipse.osee.ats.actions.ConvertActionableItemsAction;
import org.eclipse.osee.ats.actions.DeletePurgeAtsArtifactsAction;
import org.eclipse.osee.ats.actions.DeleteTasksAction;
import org.eclipse.osee.ats.actions.DeleteTasksAction.TaskArtifactProvider;
import org.eclipse.osee.ats.actions.EditAssigneeAction;
import org.eclipse.osee.ats.actions.EditStatusAction;
import org.eclipse.osee.ats.actions.EmailActionAction;
import org.eclipse.osee.ats.actions.FavoriteAction;
import org.eclipse.osee.ats.actions.SubscribedAction;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.column.GoalOrderColumn;
import org.eclipse.osee.ats.column.IPersistAltLeftClickProvider;
import org.eclipse.osee.ats.core.client.action.ActionArtifact;
import org.eclipse.osee.ats.core.client.action.ActionArtifactRollup;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.actions.ISelectedAtsArtifacts;
import org.eclipse.osee.ats.core.client.actions.ISelectedTeamWorkflowArtifacts;
import org.eclipse.osee.ats.core.client.artifact.GoalArtifact;
import org.eclipse.osee.ats.core.client.config.AtsBulkLoad;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.team.TeamState;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.notify.ArtifactEmailWizard;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsAttributeColumn;
import org.eclipse.osee.ats.workflow.TransitionToMenu;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ISelectedArtifacts;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.ArtifactDoubleClick;
import org.eclipse.osee.framework.ui.skynet.OpenContributionItem;
import org.eclipse.osee.framework.ui.skynet.artifact.ArtifactPromptChange;
import org.eclipse.osee.framework.ui.skynet.render.IRenderer;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.skynet.results.XResultDataUI;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.skynet.column.IAttributeColumn;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Donald G. Dunne
 */
public class WorldXViewer extends XViewer implements ISelectedAtsArtifacts, IPersistAltLeftClickProvider, ISelectedTeamWorkflowArtifacts, ISelectedArtifacts, IDirtiableEditor {
   private String title;
   private String extendedStatusString = "";
   public static final String MENU_GROUP_ATS_WORLD_EDIT = "ATS WORLD EDIT";
   public static final String MENU_GROUP_ATS_WORLD_OPEN = "ATS WORLD OPEN";
   public static final String MENU_GROUP_ATS_WORLD_OTHER = "ATS WORLD OTHER";
   public static final String ADD_AS_FAVORITE = "Add as Favorite";
   public static final String REMOVE_FAVORITE = "Remove Favorite";
   public static final String SUBSCRIBE = "Subscribe for Notifications";
   public static final String UN_SUBSCRIBE = "Un-Subscribe for Notifications";
   public final WorldXViewer thisXViewer = this;
   public List<IMenuActionProvider> menuActionProviders = new ArrayList<IMenuActionProvider>();
   protected final IDirtiableEditor editor;

   public WorldXViewer(Composite parent, int style, IXViewerFactory xViewerFactory, IDirtiableEditor editor) {
      super(parent, style, xViewerFactory);
      this.editor = editor;
      getTree().addKeyListener(new KeySelectedListener());
   }

   private class KeySelectedListener implements KeyListener {
      @Override
      public void keyPressed(KeyEvent e) {
         // do nothing
      }

      @Override
      public void keyReleased(KeyEvent e) {
         if (e.keyCode == SWT.F5) {
            try {
               List<Artifact> artifacts = getSelectedArtifacts();
               for (IRenderer renderer : RendererManager.getCommonRenderers(artifacts, PresentationType.F5_DIFF)) {
                  for (Artifact art : artifacts) {
                     if (renderer.getApplicabilityRating(PresentationType.F5_DIFF, art) == IRenderer.SPECIALIZED_KEY_MATCH) {
                        RendererManager.open(art, PresentationType.F5_DIFF);
                     }
                  }
               }
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      }
   }

   @Override
   protected void createSupportWidgets(Composite parent) {
      super.createSupportWidgets(parent);
      parent.addDisposeListener(new DisposeListener() {
         @Override
         public void widgetDisposed(DisposeEvent e) {
            ((WorldContentProvider) getContentProvider()).clear(false);
         }
      });
      createMenuActions();
   }

   Action editActionableItemsAction;
   protected EditStatusAction editStatusAction;
   EditAssigneeAction editAssigneeAction;
   ConvertActionableItemsAction convertActionableItemsAction;

   FavoriteAction favoritesAction;
   SubscribedAction subscribedAction;
   DeletePurgeAtsArtifactsAction deletePurgeAtsObjectAction;
   EmailActionAction emailAction;
   Action resetActionArtifactAction;
   DeleteTasksAction deleteTasksAction;

   public void createMenuActions() {

      convertActionableItemsAction = new ConvertActionableItemsAction(this);
      favoritesAction = new FavoriteAction(this);
      subscribedAction = new SubscribedAction(this);
      deletePurgeAtsObjectAction = new DeletePurgeAtsArtifactsAction(this, false);
      emailAction = new EmailActionAction(this);
      editStatusAction = new EditStatusAction(this, this, this);
      editAssigneeAction = new EditAssigneeAction(this, this);
      TaskArtifactProvider taskProvider = new TaskArtifactProvider() {

         @Override
         public List<TaskArtifact> getSelectedArtifacts() {
            return getSelectedTaskArtifacts();
         }

      };

      deleteTasksAction = new DeleteTasksAction(taskProvider);

      new Action("Edit", IAction.AS_PUSH_BUTTON) {
         @Override
         public void run() {
            try {
               new ColumnMultiEditAction(thisXViewer).run();
            } catch (Exception ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      };

      editActionableItemsAction = new Action("Edit Actionable Item(s)", IAction.AS_PUSH_BUTTON) {
         @Override
         public void run() {
            try {
               if (getSelectedActionArtifacts().size() == 1) {
                  ActionArtifact actionArt = getSelectedActionArtifacts().iterator().next();
                  AtsUtil.editActionableItems(actionArt);
                  refresh(getSelectedArtifactItems().iterator().next());
               } else {
                  TeamWorkFlowArtifact teamArt = getSelectedTeamWorkflowArtifacts().iterator().next();
                  AtsUtil.editActionableItems(teamArt);
                  refresh(getSelectedArtifactItems().toArray()[0]);
               }
            } catch (Exception ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      };

      resetActionArtifactAction = new Action("Reset Action off Children", IAction.AS_PUSH_BUTTON) {
         @Override
         public void run() {
            SkynetTransaction transaction;
            try {
               transaction = TransactionManager.createTransaction(AtsUtil.getAtsBranch(), "Reset Action off Children");
               for (ActionArtifact actionArt : getSelectedActionArtifacts()) {
                  ActionArtifactRollup rollup = new ActionArtifactRollup(actionArt, transaction);
                  rollup.resetAttributesOffChildren();
               }
               transaction.execute();
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }

         }
      };
   }

   @Override
   public void handleColumnMultiEdit(TreeColumn treeColumn, Collection<TreeItem> treeItems) {
      super.handleColumnMultiEdit(treeColumn, treeItems);
      handleColumnMultiEdit(treeColumn, treeItems, true);
   }

   public void handleColumnMultiEdit(TreeColumn treeColumn, Collection<TreeItem> treeItems, final boolean persist) {
      if (treeColumn.getData() instanceof IMultiColumnEditProvider) {
         return;
      }
      if (!(treeColumn.getData() instanceof IAttributeColumn) && !(treeColumn.getData() instanceof XViewerAtsAttributeColumn)) {
         AWorkbench.popup("ERROR", "Column is not attribute and thus not multi-editable " + treeColumn.getText());
         return;
      }

      XResultData rData = new XResultData();
      IAttributeType attributeType = null;
      if (treeColumn.getData() instanceof IAttributeColumn) {
         attributeType = ((IAttributeColumn) treeColumn.getData()).getAttributeType();
      }
      if (attributeType == null) {
         AWorkbench.popup("ERROR", "Can't retrieve attribute name from attribute column " + treeColumn.getText());
         return;
      }
      final Set<Artifact> useArts = new HashSet<Artifact>();
      for (TreeItem item : treeItems) {
         Artifact art = (Artifact) item.getData();
         try {
            if (art.isAttributeTypeValid(attributeType)) {
               useArts.add(art);
            } else {
               rData.logError(attributeType + " not valid for artifact " + art.getGuid() + " - " + art.getName());
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
            rData.logError(ex.getLocalizedMessage());
         }
      }

      try {
         if (!rData.isEmpty()) {
            XResultDataUI.report(rData, "Column Multi Edit Errors");
            return;
         }
         if (useArts.size() > 0) {
            ArtifactPromptChange.promptChangeAttribute(attributeType, useArts, persist);
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public boolean isColumnMultiEditable(TreeColumn treeColumn, Collection<TreeItem> treeItems) {
      if (!(treeColumn.getData() instanceof XViewerColumn)) {
         return false;
      }
      if (!((XViewerColumn) treeColumn.getData()).isMultiColumnEditable()) {
         return false;
      }
      if (((XViewerColumn) treeColumn.getData()) instanceof IMultiColumnEditProvider) {
         return true;
      }
      IAttributeType attributeType = null;
      // Currently don't know how to multi-edit anything but attribute
      if (treeColumn.getData() instanceof IAttributeColumn) {
         attributeType = ((IAttributeColumn) treeColumn.getData()).getAttributeType();
      } else {
         return false;
      }

      if (attributeType == null) {
         AWorkbench.popup("ERROR", "Can't retrieve attribute name from attribute column " + treeColumn.getText());
         return false;
      }
      for (TreeItem item : treeItems) {
         if (Artifacts.isOfType(item.getData(), AtsArtifactTypes.Action)) {
            return false;
         }
         try {
            Artifact artifact = (Artifact) item.getData();
            if (!artifact.isAttributeTypeValid(attributeType)) {
               return false;
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
            return false;
         }
      }
      return true;
   }

   @Override
   public boolean isColumnMultiEditEnabled() {
      return true;
   }

   public void handleEmailSelectedAtsObject() throws OseeCoreException {
      Artifact art = getSelectedArtifacts().iterator().next();
      if (art.isOfType(AtsArtifactTypes.Action)) {
         if (ActionManager.getTeams(art).size() > 1) {
            art = AtsUtil.promptSelectTeamWorkflow(art);
            if (art == null) {
               return;
            }
         } else {
            art = ActionManager.getFirstTeam(art);
         }
      }
      if (art != null) {
         ArtifactEmailWizard ew = new ArtifactEmailWizard((AbstractWorkflowArtifact) art);
         WizardDialog dialog = new WizardDialog(Displays.getActiveShell(), ew);
         dialog.create();
         dialog.open();
      }
   }

   public AbstractWorkflowArtifact getSelectedSMA() {
      Object obj = null;
      if (getSelectedArtifactItems().isEmpty()) {
         return null;
      }
      obj = getTree().getSelection()[0].getData();
      return obj != null && obj instanceof AbstractWorkflowArtifact ? (AbstractWorkflowArtifact) obj : null;
   }

   /**
    * Create Edit menu at top to make easier for users to see and eventually enable menu to get rid of all separate edit
    * items
    */
   public MenuManager updateEditMenu(MenuManager mm) {
      final Collection<TreeItem> selectedTreeItems = Arrays.asList(thisXViewer.getTree().getSelection());
      Set<TreeColumn> editableColumns = ColumnMultiEditAction.getEditableTreeColumns(thisXViewer, selectedTreeItems);

      return XViewerCustomMenu.createEditMenuManager(thisXViewer, "Edit", selectedTreeItems, editableColumns);
   }

   public void updateEditMenuActions() {
      MenuManager mm = getMenuManager();

      // EDIT MENU BLOCK
      MenuManager editMenuManager = updateEditMenu(mm);
      mm.insertBefore(MENU_GROUP_PRE, editMenuManager);

      final Collection<TreeItem> selectedTreeItems = Arrays.asList(thisXViewer.getTree().getSelection());
      mm.insertBefore(MENU_GROUP_PRE,
         TransitionToMenu.createTransitionToMenuManager(thisXViewer, "Transition-To", selectedTreeItems));

      mm.insertBefore(MENU_GROUP_PRE, editStatusAction);
      editStatusAction.setEnabled(getSelectedSMAArtifacts().size() > 0);

      mm.insertBefore(MENU_GROUP_PRE, editAssigneeAction);
      editAssigneeAction.setEnabled(getSelectedSMAArtifacts().size() > 0);

      mm.insertBefore(MENU_GROUP_PRE, editActionableItemsAction);
      editActionableItemsAction.setEnabled(getSelectedActionArtifacts().size() == 1 || getSelectedTeamWorkflowArtifacts().size() == 1);

      mm.insertBefore(MENU_GROUP_PRE, convertActionableItemsAction);
      convertActionableItemsAction.updateEnablement();

   }

   @Override
   public void updateMenuActionsForTable() {
      MenuManager mm = getMenuManager();

      // OPEN MENU BLOCK
      OpenContributionItem contrib = new OpenContributionItem(getClass().getSimpleName() + ".open");
      contrib.fill(mm.getMenu(), -1);
      mm.insertBefore(XViewer.MENU_GROUP_PRE, contrib);
      mm.insertBefore(XViewer.MENU_GROUP_PRE, new Separator());

      mm.insertBefore(XViewer.MENU_GROUP_PRE, new GroupMarker(MENU_GROUP_ATS_WORLD_EDIT));

      updateEditMenuActions();

      if (AtsUtilCore.isAtsAdmin()) {
         mm.insertBefore(XViewer.MENU_GROUP_PRE, new Separator());
         mm.insertBefore(XViewer.MENU_GROUP_PRE, deletePurgeAtsObjectAction);
         deletePurgeAtsObjectAction.setEnabled(getSelectedAtsArtifacts().size() > 0);
      }

      mm.insertBefore(XViewer.MENU_GROUP_PRE, deleteTasksAction);
      deleteTasksAction.updateEnablement(getSelectedArtifacts());

      mm.insertBefore(XViewer.MENU_GROUP_PRE, new GroupMarker(MENU_GROUP_ATS_WORLD_OPEN));
      mm.insertBefore(XViewer.MENU_GROUP_PRE, new Separator());

      // OTHER MENU BLOCK
      mm.insertBefore(XViewer.MENU_GROUP_PRE, favoritesAction);
      favoritesAction.updateEnablement();

      mm.insertBefore(XViewer.MENU_GROUP_PRE, subscribedAction);
      subscribedAction.updateEnablement();

      mm.insertBefore(XViewer.MENU_GROUP_PRE, emailAction);
      emailAction.setEnabled(getSelectedSMAArtifacts().size() == 1);

      mm.insertBefore(XViewer.MENU_GROUP_PRE, resetActionArtifactAction);
      resetActionArtifactAction.setEnabled(getSelectedActionArtifacts().size() > 0);

      mm.insertBefore(XViewer.MENU_GROUP_PRE, new GroupMarker(MENU_GROUP_ATS_WORLD_OTHER));
      mm.insertBefore(XViewer.MENU_GROUP_PRE, new Separator());

      for (IMenuActionProvider provider : menuActionProviders) {
         provider.updateMenuActionsForTable();
      }
   }

   @Override
   public void handleDoubleClick() {
      ArtifactDoubleClick.openArtifact(getSelection());
   }

   public List<Artifact> getLoadedArtifacts() {
      List<Artifact> arts = new ArrayList<Artifact>();
      if (getRoot() != null) {
         for (Object artifact : (Collection<?>) getRoot()) {
            if (artifact instanceof Artifact) {
               arts.add((Artifact) artifact);
            }
         }
      }
      return arts;
   }

   public void clear(boolean forcePend) {
      ((WorldContentProvider) getContentProvider()).clear(forcePend);
   }

   public void insert(Artifact toInsert, int position) {
      insert(getRoot(), toInsert, position);
   }

   /**
    * Release resources
    */
   @Override
   public void dispose() {
      // Dispose of the table objects is done through separate dispose listener off tree
      // Tell the label provider to release its resources
      getLabelProvider().dispose();
      super.dispose();
   }

   @Override
   public List<Artifact> getSelectedArtifacts() {
      List<Artifact> arts = new ArrayList<Artifact>();
      TreeItem items[] = getTree().getSelection();
      if (items.length > 0) {
         for (TreeItem item : items) {
            arts.add((Artifact) item.getData());
         }
      }
      return arts;
   }

   @Override
   public List<TaskArtifact> getSelectedTaskArtifacts() {
      List<TaskArtifact> arts = new ArrayList<TaskArtifact>();
      TreeItem items[] = getTree().getSelection();
      if (items.length > 0) {
         for (TreeItem item : items) {
            if (Artifacts.isOfType(item.getData(), AtsArtifactTypes.Task)) {
               arts.add((TaskArtifact) item.getData());
            }
         }
      }
      return arts;
   }

   /**
    * @return true if all selected are Workflow OR are Actions with single workflow
    */
   public boolean isSelectedTeamWorkflowArtifacts() {
      TreeItem items[] = getTree().getSelection();
      if (items.length > 0) {
         for (TreeItem item : items) {
            if (Artifacts.isOfType(item.getData(), AtsArtifactTypes.Action)) {
               try {
                  if (ActionManager.getTeams(item.getData()).size() != 1) {
                     return false;
                  }
               } catch (OseeCoreException ex) {
                  // Do Nothing
               }
            } else if (!Artifacts.isOfType(item.getData(), AtsArtifactTypes.TeamWorkflow)) {
               return false;
            }
         }
      }
      return true;
   }

   /**
    * @return all selected Workflow and any workflow that have Actions with single workflow
    */
   @Override
   public Set<TeamWorkFlowArtifact> getSelectedTeamWorkflowArtifacts() {
      Set<TeamWorkFlowArtifact> teamArts = new HashSet<TeamWorkFlowArtifact>();
      TreeItem items[] = getTree().getSelection();
      if (items.length > 0) {
         for (TreeItem item : items) {
            if (Artifacts.isOfType(item.getData(), AtsArtifactTypes.TeamWorkflow)) {
               teamArts.add((TeamWorkFlowArtifact) item.getData());
            }
            if (Artifacts.isOfType(item.getData(), AtsArtifactTypes.Action)) {
               try {
                  if (ActionManager.getTeams(item.getData()).size() == 1) {
                     teamArts.addAll(ActionManager.getTeams(item.getData()));
                  }
               } catch (OseeCoreException ex) {
                  // Do Nothing
               }
            }
         }
      }
      return teamArts;
   }

   /**
    * @return all selected Workflow and any workflow that have Actions with single workflow
    */
   @Override
   public Set<AbstractWorkflowArtifact> getSelectedSMAArtifacts() {
      Set<AbstractWorkflowArtifact> smaArts = new HashSet<AbstractWorkflowArtifact>();
      try {
         Iterator<?> i = ((IStructuredSelection) getSelection()).iterator();
         while (i.hasNext()) {
            Object obj = i.next();
            if (obj instanceof AbstractWorkflowArtifact) {
               smaArts.add((AbstractWorkflowArtifact) obj);
            } else if (Artifacts.isOfType(obj, AtsArtifactTypes.Action)) {
               smaArts.addAll(ActionManager.getTeams(obj));
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return smaArts;
   }

   public Set<ActionArtifact> getSelectedActionArtifacts() {
      Set<ActionArtifact> actionArts = new HashSet<ActionArtifact>();
      TreeItem items[] = getTree().getSelection();
      if (items.length > 0) {
         for (TreeItem item : items) {
            if (Artifacts.isOfType(item.getData(), AtsArtifactTypes.Action)) {
               actionArts.add((ActionArtifact) item.getData());
            }
         }
      }
      return actionArts;
   }

   public void setCancelledNotification() {
      TreeItem item = getTree().getItem(0);
      if (item.getData() instanceof String) {
         item.setData(TeamState.Cancelled.getName());
      }
      refresh(item.getData());
   }

   /**
    * @param title string to be used in reporting
    */
   public void setReportingTitle(String title) {
      this.title = title;
   }

   /**
    * @return Returns the title.
    */
   public String getTitle() {
      return title;
   }

   @Override
   public void load(Collection<Object> objects) {
      Set<Artifact> arts = new HashSet<Artifact>();
      for (Object obj : objects) {
         if (AtsUtil.isAtsArtifact(obj)) {
            arts.add((Artifact) obj);
         }
      }
      try {
         AtsBulkLoad.bulkLoadArtifacts(arts);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      setInput(arts);
   }

   public List<Artifact> getSelectedArtifactItems() {
      List<Artifact> arts = new ArrayList<Artifact>();
      TreeItem items[] = getTree().getSelection();
      if (items.length > 0) {
         for (TreeItem item : items) {
            arts.add((Artifact) item.getData());
         }
      }
      return arts;
   }

   @Override
   public String getStatusString() {
      return extendedStatusString;
   }

   public String getExtendedStatusString() {
      return extendedStatusString;
   }

   public void setExtendedStatusString(String extendedStatusString) {
      this.extendedStatusString = extendedStatusString;
      updateStatusLabel();
   }

   /**
    * store off parent goalItem in label provider so it can determine parent when providing order of goal member
    */
   @Override
   protected void doUpdateItem(Item item, Object element) {
      if (item instanceof TreeItem) {
         GoalArtifact parentGoalArtifact = GoalOrderColumn.getParentGoalArtifact((TreeItem) item);
         if (parentGoalArtifact != null) {
            ((WorldLabelProvider) getLabelProvider()).setParentGoal(parentGoalArtifact);
         }
      }
      super.doUpdateItem(item, element);
   }

   public void addMenuActionProvider(IMenuActionProvider provider) {
      menuActionProviders.add(provider);
   }

   @Override
   public List<Artifact> getSelectedAtsArtifacts() {
      List<Artifact> artifacts = new ArrayList<Artifact>();
      Iterator<?> i = ((IStructuredSelection) getSelection()).iterator();
      while (i.hasNext()) {
         Object obj = i.next();
         if (obj instanceof AbstractWorkflowArtifact) {
            artifacts.add((AbstractWorkflowArtifact) obj);
         }
      }
      return artifacts;
   }

   @Override
   public boolean isAltLeftClickPersist() {
      return true;
   }

   @Override
   public void onDirtied() {
      if (editor != null) {
         editor.onDirtied();
      }
   }
}
