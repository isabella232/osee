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

package org.eclipse.osee.ats.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.actions.AccessControlAction;
import org.eclipse.osee.ats.actions.DirtyReportAction;
import org.eclipse.osee.ats.actions.IDirtyReportable;
import org.eclipse.osee.ats.actions.ResourceHistoryAction;
import org.eclipse.osee.ats.agile.SprintMemberProvider;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.review.IAtsPeerToPeerReview;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.core.client.actions.ISelectedAtsArtifacts;
import org.eclipse.osee.ats.core.client.artifact.GoalArtifact;
import org.eclipse.osee.ats.core.client.artifact.SprintArtifact;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.AtsUtilClient;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.util.AtsObjects;
import org.eclipse.osee.ats.goal.GoalMemberProvider;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.navigate.VisitedItems;
import org.eclipse.osee.ats.task.TaskComposite;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.world.AtsMetricsComposite;
import org.eclipse.osee.ats.world.IAtsMetricsProvider;
import org.eclipse.osee.framework.access.AccessControlManager;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.enums.PresentationType;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageManager;
import org.eclipse.osee.framework.ui.skynet.AttributesComposite;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.OseeStatusContributionItemFactory;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.AbstractArtifactEditor;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * @author Donald G. Dunne
 */
public class WorkflowEditor extends AbstractArtifactEditor implements IDirtyReportable, IWfeEventHandler, ISelectedAtsArtifacts, IAtsMetricsProvider {
   public static final String EDITOR_ID = "org.eclipse.osee.ats.editor.WorkflowEditor";
   private AbstractWorkflowArtifact awa;
   private WfeWorkFlowTab workFlowTab;
   private WfeMembersTab membersTab;
   private WfeDefectsTab defectsTab;
   private WfeTasksTab taskTab;
   int attributesPageIndex;
   private AttributesComposite attributesComposite;
   private boolean privilegedEditModeEnabled = false;
   private final List<IWfeEditorListener> editorListeners = new ArrayList<>();
   WfeOutlinePage outlinePage;

   @Override
   protected void addPages() {
      WfeInput input = getWfeInput();
      try {
         if (input.getArtifact() != null) {
            if (input.getArtifact() instanceof AbstractWorkflowArtifact) {
               awa = (AbstractWorkflowArtifact) input.getArtifact();
            } else {
               throw new OseeArgumentException("WfeInput artifact must be StateMachineArtifact");
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         return;
      }

      if (!input.isReload() && awa == null) {
         MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Open Error",
            "Can't Find Action in DB");
         return;
      }
      try {
         if (input.isReload()) {
            createReloadTab();
         } else {
            WfeArtifactEventManager.add(this);
            WfeBranchEventManager.add(this);

            setContentDescription(privilegedEditModeEnabled ? " PRIVILEGED EDIT MODE ENABLED" : "");

            createMembersTab();
            createWorkflowTab();
            createTaskTab();
            createDefectsTab();
            createAttributesTab();
            createMetricsTab();
         }
         updatePartName();

         setActivePage(0);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   public WfeInput getWfeInput() {
      WfeInput aei = null;
      IEditorInput editorInput = getEditorInput();
      if (editorInput instanceof WfeInput) {
         aei = (WfeInput) editorInput;
      } else {
         throw new OseeArgumentException("Editor Input not WfeInput");
      }
      return aei;
   }

   @SuppressWarnings("unchecked")
   @Override
   public <T> T getAdapter(Class<T> type) {
      if (type != null && type.isAssignableFrom(IContentOutlinePage.class)) {
         WfeOutlinePage page = getOutlinePage();
         page.setInput(this);
         return (T) page;
      }
      return super.getAdapter(type);
   }

   public WfeOutlinePage getOutlinePage() {
      if (outlinePage == null) {
         outlinePage = new WfeOutlinePage();
      }
      return outlinePage;
   }

   /**
    * Do not throw exception here, want to create other tabs if this one fails
    */
   private void createWorkflowTab() {
      try {
         workFlowTab = new WfeWorkFlowTab(this, awa);
         addPage(workFlowTab);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   private void createReloadTab() throws PartInitException {
      addPage(new WfeReloadTab(this));
   }

   private void createMembersTab() throws PartInitException {
      if (awa instanceof GoalArtifact) {
         membersTab = new WfeMembersTab(this, new GoalMemberProvider((GoalArtifact) awa));
         addPage(membersTab);
      } else if (awa instanceof SprintArtifact) {
         membersTab = new WfeMembersTab(this, new SprintMemberProvider((SprintArtifact) awa));
         addPage(membersTab);
      }
   }

   private void createTaskTab() throws PartInitException {
      if (isTaskable()) {
         taskTab = new WfeTasksTab(this, (IAtsTeamWorkflow) awa, AtsClientService.get());
         addPage(taskTab);
      }
   }

   private void createDefectsTab() throws PartInitException {
      if (awa.isOfType(AtsArtifactTypes.PeerToPeerReview)) {
         defectsTab = new WfeDefectsTab(this, (IAtsPeerToPeerReview) awa);
         addPage(defectsTab);
      }
   }

   private void updatePartName() throws OseeCoreException {
      setPartName(getTitleStr());
   }

   public String getTitleStr() throws OseeCoreException {
      return getWfeInput().getName();
   }

   @Override
   public void doSave(IProgressMonitor monitor) {
      try {
         if (awa.isHistoricalVersion()) {
            AWorkbench.popup("Historical Error",
               "You can not change a historical version of " + awa.getArtifactTypeName() + ":\n\n" + awa);
         } else if (!awa.isAccessControlWrite()) {
            AWorkbench.popup("Authentication Error",
               "You do not have permissions to save " + awa.getArtifactTypeName() + ":" + awa);
         } else {
            try {
               if (attributesComposite != null && getActivePage() == attributesPageIndex) {
                  awa.persist("Workflow Editor - Attributes Tab - Save");
               } else {
                  IAtsChangeSet changes = AtsClientService.get().createChangeSet("Workflow Editor - Save");
                  // If change was made on Attribute tab, persist awa separately.  This is cause attribute
                  // tab changes conflict with XWidget changes
                  // Save widget data to artifact
                  workFlowTab.saveXWidgetToArtifact();
                  awa.save(changes);
                  changes.execute();
               }
            } catch (Exception ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
            onDirtied();
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   public static void setLabelFonts(Control parent, Font font) {
      if (parent instanceof Label) {
         Label label = (Label) parent;
         label.setFont(font);
      }
      if (parent instanceof Composite) {
         Composite container = (Composite) parent;
         for (Control child : container.getChildren()) {
            setLabelFonts(child, font);
         }
         container.layout();
      }
   }

   @Override
   public boolean isSaveOnCloseNeeded() {
      return isDirty();
   }

   @Override
   public void dispose() {
      for (IWfeEditorListener listener : editorListeners) {
         listener.editorDisposing();
      }
      WfeArtifactEventManager.remove(this);
      WfeBranchEventManager.remove(this);
      if (awa != null && !awa.isDeleted() && awa.isWfeDirty().isTrue()) {
         awa.revert();
      }
      if (workFlowTab != null) {
         workFlowTab.dispose();
      }
      if (membersTab != null) {
         membersTab.dispose();
      }
      if (taskTab != null) {
         taskTab.dispose();
      }
      super.dispose();
   }

   @Override
   public boolean isDirty() {
      return isDirtyResult().isTrue();
   }

   @Override
   public Result isDirtyResult() {
      if (getWfeInput().isReload() || awa.isDeleted()) {
         return Result.FalseResult;
      }
      try {
         Result result = workFlowTab.isXWidgetDirty();
         if (result.isTrue()) {
            return result;
         }
         result = ((AbstractWorkflowArtifact) ((WfeInput) getEditorInput()).getArtifact()).isWfeDirty();
         if (result.isTrue()) {
            return result;
         }

         String rString = null;
         for (Attribute<?> attribute : awa.internalGetAttributes()) {
            if (attribute.isDirty()) {
               rString = "Attribute: " + attribute.getNameValueDescription();
               break;
            }
         }

         if (rString == null) {
            rString = RelationManager.reportHasDirtyLinks(awa);
         }

         return new Result(rString != null, rString);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         return new Result(true, ex.getLocalizedMessage());
      }
   }

   @Override
   public String toString() {
      return "WorkflowEditor - " + awa.getAtsId() + " - " + awa.getArtifactTypeName() + " named \"" + awa.getName() + "\"";
   }

   @Override
   protected void createPages() {
      super.createPages();
      OseeStatusContributionItemFactory.addTo(this, true);
   }

   private void createMetricsTab() {
      try {
         Composite composite = AtsUtil.createCommonPageComposite(getContainer());
         createToolBar(composite);
         new AtsMetricsComposite(this, composite, SWT.NONE);
         int metricsPageIndex = addPage(composite);
         setPageText(metricsPageIndex, "Metrics");
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }

   }

   private void createAttributesTab() {
      try {
         if (!AtsUtilClient.isAtsAdmin()) {
            return;
         }

         // Create Attributes tab
         Composite composite = AtsUtil.createCommonPageComposite(getContainer());
         ToolBar toolBar = createToolBar(composite);

         ToolItem item = new ToolItem(toolBar, SWT.PUSH);
         item.setImage(ImageManager.getImage(FrameworkImage.SAVE));
         item.setToolTipText("Save attributes changes only");
         item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
               try {
                  awa.persist(getClass().getSimpleName());
               } catch (Exception ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         });

         ToolItem refresh = new ToolItem(toolBar, SWT.PUSH);
         refresh.setImage(ImageManager.getImage(FrameworkImage.REFRESH));
         refresh.setToolTipText("Reload Table");
         refresh.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
               try {
                  awa.reloadAttributesAndRelations();
               } catch (Exception ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         });

         Label label = new Label(composite, SWT.NONE);
         label.setText("  NOTE: Changes made on this page MUST be saved through save icon on this page");
         label.setForeground(Displays.getSystemColor(SWT.COLOR_RED));

         attributesComposite = new AttributesComposite(this, composite, SWT.NONE, awa);
         attributesPageIndex = addPage(composite);
         setPageText(attributesPageIndex, "Attributes");
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   private ToolBar createToolBar(Composite parent) {
      ToolBar toolBar = AtsUtil.createCommonToolBar(parent);

      AtsUtil.actionToToolItem(toolBar, new ResourceHistoryAction(awa), FrameworkImage.EDIT_BLUE);
      AtsUtil.actionToToolItem(toolBar, new AccessControlAction(awa), FrameworkImage.AUTHENTICATED);
      AtsUtil.actionToToolItem(toolBar, new DirtyReportAction(this), FrameworkImage.DIRTY);
      new ToolItem(toolBar, SWT.SEPARATOR);
      Text artifactInfoLabel = new Text(toolBar.getParent(), SWT.END);
      artifactInfoLabel.setEditable(false);
      artifactInfoLabel.setText("Type: \"" + awa.getArtifactTypeName() + "\"   ATS: " + awa.getAtsId());
      artifactInfoLabel.setToolTipText("The human readable id and database id for this artifact");

      return toolBar;
   }

   public void refreshPages() {
      try {
         if (getContainer() == null || getContainer().isDisposed()) {
            return;
         }
         if (workFlowTab != null) {
            workFlowTab.refresh();
         }
         if (membersTab != null) {
            membersTab.refresh();
         }
         if (taskTab != null) {
            taskTab.refresh();
         }
         if (attributesComposite != null) {
            attributesComposite.refreshArtifact(awa);
         }
         onDirtied();
         updatePartName();
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   public static void editArtifact(Artifact artifact) throws OseeCoreException {
      if (artifact == null) {
         return;
      }
      if (artifact.isDeleted()) {
         AWorkbench.popup("ERROR", "Artifact has been deleted");
         return;
      }
      if (artifact instanceof AbstractWorkflowArtifact) {
         editArtifact((AbstractWorkflowArtifact) artifact);
      } else {
         RendererManager.open(artifact, PresentationType.GENERALIZED_EDIT);
      }
   }

   public static void editArtifact(final AbstractWorkflowArtifact workflow) {
      if (workflow == null) {
         return;
      }
      if (workflow.isDeleted()) {
         AWorkbench.popup("ERROR", "Artifact has been deleted");
         return;
      }
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            IWorkbenchPage page = AWorkbench.getActivePage();
            try {
               page.openEditor(new WfeInput(workflow), EDITOR_ID);
               VisitedItems.addVisited(workflow);
            } catch (PartInitException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      });

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

   public static void close(Set<IAtsTeamWorkflow> singleton, boolean save) {
      close(org.eclipse.osee.framework.jdk.core.util.Collections.castAll(AbstractWorkflowArtifact.class,
         AtsObjects.getArtifacts(singleton)), save);
   }

   public static void close(final Collection<? extends AbstractWorkflowArtifact> artifacts, boolean save) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IEditorReference editors[] = page.getEditorReferences();
            for (int j = 0; j < editors.length; j++) {
               IEditorReference editor = editors[j];
               if (editor.getPart(false) instanceof WorkflowEditor && artifacts.contains(
                  ((WorkflowEditor) editor.getPart(false)).getAwa())) {
                  ((WorkflowEditor) editor.getPart(false)).closeEditor();
               }
            }
         }
      });
   }

   public static void closeAll() {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IEditorReference editors[] = page.getEditorReferences();
            for (int j = 0; j < editors.length; j++) {
               IEditorReference editor = editors[j];
               if (editor.getPart(false) instanceof WorkflowEditor) {
                  ((WorkflowEditor) editor.getPart(false)).closeEditor();
               }
            }
         }
      });
   }

   public static WorkflowEditor getWorkflowEditor(AbstractWorkflowArtifact artifact) {
      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      IEditorReference editors[] = page.getEditorReferences();
      for (int j = 0; j < editors.length; j++) {
         IEditorReference editor = editors[j];
         if (editor.getPart(
            false) instanceof WorkflowEditor && ((WorkflowEditor) editor.getPart(false)).getAwa().equals(artifact)) {
            return (WorkflowEditor) editor.getPart(false);
         }
      }
      return null;
   }

   public static List<WorkflowEditor> getWorkflowEditors() {
      List<WorkflowEditor> results = new ArrayList<>();
      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      IEditorReference editors[] = page.getEditorReferences();
      for (int j = 0; j < editors.length; j++) {
         IEditorReference editor = editors[j];
         if (editor.getPart(false) instanceof WorkflowEditor) {
            results.add((WorkflowEditor) editor.getPart(false));
         }
      }
      return results;
   }

   public void closeEditor() {
      final MultiPageEditorPart editor = this;
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            AWorkbench.getActivePage().closeEditor(editor, false);
         }
      });
   }

   public boolean isTaskable() {
      return awa instanceof TeamWorkFlowArtifact;
   }

   public boolean isTasksEditable() {
      boolean editable = true;
      if (!(awa instanceof TeamWorkFlowArtifact) || awa.isCompletedOrCancelled()) {
         editable = false;
      }
      return editable;
   }

   public boolean isPrivilegedEditModeEnabled() {
      return privilegedEditModeEnabled;
   }

   /**
    * @param privilegedEditMode the privilegedEditMode to set s
    */
   public void setPrivilegedEditMode(boolean enabled) {
      this.privilegedEditModeEnabled = enabled;
      doSave(null);
      if (workFlowTab != null) {
         workFlowTab.refresh();
      }
      if (membersTab != null) {
         membersTab.refresh();
      }
   }

   public boolean isAccessControlWrite() throws OseeCoreException {
      return AccessControlManager.hasPermission(awa, PermissionEnum.WRITE);
   }

   @Override
   public Collection<? extends Artifact> getMetricsWorkItems() {
      if (awa.isOfType(AtsArtifactTypes.Goal)) {
         return ((GoalArtifact) awa).getMembers();
      }
      return Arrays.asList(awa);
   }

   @Override
   public IAtsVersion getMetricsVersion() throws OseeCoreException {
      return AtsClientService.get().getVersionService().getTargetedVersion(awa);
   }

   @Override
   public double getManHoursPerDayPreference() throws OseeCoreException {
      return awa.getManHrsPerDayPreference();
   }

   public WfeWorkFlowTab getWorkFlowTab() {
      return workFlowTab;
   }

   public TaskComposite getTaskComposite() {
      return taskTab.getTaskComposite();
   }

   @Override
   public Set<Artifact> getSelectedWorkflowArtifacts() {
      return Collections.singleton(awa);
   }

   @Override
   public IEditorPart getActiveEditor() {
      return this;
   }

   @Override
   public WorkflowEditor getWorkflowEditor() {
      return this;
   }

   @Override
   public boolean isDisposed() {
      return getContainer() == null || getContainer().isDisposed();
   }

   public void addEditorListeners(IWfeEditorListener listener) {
      editorListeners.add(listener);
   }

   @Override
   public List<Artifact> getSelectedAtsArtifacts() {
      return Collections.<Artifact> singletonList(awa);
   }

   @Override
   public Image getTitleImage() {
      Image image = null;
      if (getWfeInput().isReload()) {
         image = ImageManager.getImage(AtsImage.TEAM_WORKFLOW);
      } else if (getWfeInput().isBacklog()) {
         image = ImageManager.getImage(AtsImage.AGILE_BACKLOG);
      } else {
         image = ArtifactImageManager.getImage(awa);
      }
      return image;
   }

   @Override
   protected void pageChange(int newPageIndex) {
      super.pageChange(newPageIndex);
      if (newPageIndex != -1 && pages.size() > newPageIndex) {
         Object page = pages.get(newPageIndex);
         if (page != null) {
            ISelectionProvider provider = getDefaultSelectionProvider();
            if (page.equals(workFlowTab)) {
               provider = getDefaultSelectionProvider();
            } else if (page.equals(membersTab)) {
               if (membersTab != null && membersTab.getMembersSection() != null) {
                  provider = membersTab.getWorldXViewer();
               }
            } else if (page.equals(taskTab)) {
               if (taskTab.getTaskComposite() != null) {
                  provider = taskTab.getTaskComposite().getWorldXViewer();
               }
            } else {
               String title = getPageText(newPageIndex);
               if (title.equalsIgnoreCase("metrics")) {
                  provider = null;
               } else if (title.equalsIgnoreCase("attributes")) {
                  provider = attributesComposite.getTableViewer();
               }
            }
            getSite().setSelectionProvider(provider);
         }
      }
   }

   @Override
   public List<TaskArtifact> getSelectedTaskArtifacts() {
      if (awa instanceof TaskArtifact) {
         return Arrays.asList((TaskArtifact) awa);
      }
      return java.util.Collections.emptyList();
   }

   public AbstractWorkflowArtifact getAwa() {
      return awa;
   }

   public static void edit(IAtsTeamWorkflow team) {
      editArtifact((TeamWorkFlowArtifact) team.getStoreObject());
   }

}