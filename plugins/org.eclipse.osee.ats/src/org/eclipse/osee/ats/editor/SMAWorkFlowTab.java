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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.osee.ats.actions.AddNoteAction;
import org.eclipse.osee.ats.actions.CopyActionDetailsAction;
import org.eclipse.osee.ats.actions.EmailActionAction;
import org.eclipse.osee.ats.actions.FavoriteAction;
import org.eclipse.osee.ats.actions.OpenInArtifactEditorAction;
import org.eclipse.osee.ats.actions.OpenInAtsWorldAction;
import org.eclipse.osee.ats.actions.OpenParentAction;
import org.eclipse.osee.ats.actions.OpenTeamDefinitionAction;
import org.eclipse.osee.ats.actions.OpenVersionArtifactAction;
import org.eclipse.osee.ats.actions.PrivilegedEditAction;
import org.eclipse.osee.ats.actions.ReloadAction;
import org.eclipse.osee.ats.actions.ResourceHistoryAction;
import org.eclipse.osee.ats.actions.ShowChangeReportAction;
import org.eclipse.osee.ats.actions.ShowMergeManagerAction;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.artifact.WorkflowManager;
import org.eclipse.osee.ats.core.client.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.core.client.config.AtsBulkLoad;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowManager;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.client.workflow.note.NoteItem;
import org.eclipse.osee.ats.core.workdef.WorkDefinitionMatch;
import org.eclipse.osee.ats.help.ui.AtsHelpContext;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.walker.action.OpenActionViewAction;
import org.eclipse.osee.ats.workdef.StateXWidgetPage;
import org.eclipse.osee.ats.world.IWorldViewerEventHandler;
import org.eclipse.osee.ats.world.WorldXViewer;
import org.eclipse.osee.ats.world.WorldXViewerEventManager;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.event.model.EventBasicGuidArtifact;
import org.eclipse.osee.framework.skynet.core.event.model.EventModType;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.HelpUtil;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageManager;
import org.eclipse.osee.framework.ui.skynet.XFormToolkit;
import org.eclipse.osee.framework.ui.skynet.artifact.annotation.AnnotationComposite;
import org.eclipse.osee.framework.ui.skynet.artifact.annotation.AttributeAnnotationManager;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.parts.MessageSummaryNote;
import org.eclipse.osee.framework.ui.skynet.util.FormsUtil;
import org.eclipse.osee.framework.ui.skynet.util.LoadingComposite;
import org.eclipse.osee.framework.ui.skynet.widgets.IArtifactStoredWidget;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.ExceptionComposite;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Donald G. Dunne
 */
public class SMAWorkFlowTab extends FormPage implements IWorldViewerEventHandler {
   private final AbstractWorkflowArtifact awa;
   private final List<SMAWorkFlowSection> sections = new ArrayList<SMAWorkFlowSection>();
   private final List<StateXWidgetPage> statePages = new ArrayList<StateXWidgetPage>();
   private static Map<String, Integer> guidToScrollLocation = new HashMap<String, Integer>();
   private SMARelationsHyperlinkComposite smaRelationsComposite;
   private IManagedForm managedForm;
   private Composite bodyComp;
   private Composite atsBody;
   private SMAActionableItemHeader actionableItemHeader;
   private SMAWorkflowMetricsHeader workflowMetricsHeader;
   private SMADetailsSection smaDetailsSection;
   private SMARelationsSection smaRelationsSection;
   private SMAHistorySection smaHistorySection;
   private LoadingComposite loadingComposite;
   public final static String ID = "ats.workflow.tab";
   private final SMAEditor editor;
   private final List<WEUndefinedStateSection> undefinedStateSections = new ArrayList<WEUndefinedStateSection>();

   public SMAWorkFlowTab(SMAEditor editor, AbstractWorkflowArtifact awa) {
      super(editor, ID, "Workflow");
      this.editor = editor;
      this.awa = awa;
   }

   @Override
   protected void createFormContent(IManagedForm managedForm) {
      super.createFormContent(managedForm);

      this.managedForm = managedForm;
      try {
         managedForm.getForm().getVerticalBar().addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
               storeScrollLocation();
            }

         });
         updateTitleBar();

         bodyComp = managedForm.getForm().getBody();
         GridLayout gridLayout = new GridLayout(1, false);
         bodyComp.setLayout(gridLayout);
         GridData gd = new GridData(SWT.LEFT, SWT.LEFT, true, false);
         gd.widthHint = 300;
         bodyComp.setLayoutData(gd);

         setLoading(true);
         if (awa.isOfType(AtsArtifactTypes.DecisionReview)) {
            HelpUtil.setHelp(managedForm.getForm(), AtsHelpContext.DECISION_REVIEW);

         } else if (awa.isOfType(AtsArtifactTypes.PeerToPeerReview)) {
            HelpUtil.setHelp(managedForm.getForm(), AtsHelpContext.PEER_TO_PEER_REVIEW);

         } else {
            HelpUtil.setHelp(managedForm.getForm(), AtsHelpContext.WORKFLOW_EDITOR__WORKFLOW_TAB);
         }

         refreshData();
         WorldXViewerEventManager.add(this);
      } catch (Exception ex) {
         handleException(ex);
      }
   }

   private void updateTitleBar() throws OseeCoreException {
      if (managedForm != null && Widgets.isAccessible(managedForm.getForm())) {
         String titleString = editor.getTitleStr();
         String displayableTitle = Strings.escapeAmpersands(titleString);
         managedForm.getForm().setText(displayableTitle);
         managedForm.getForm().setImage(ArtifactImageManager.getImage(awa));
      }
   }

   @Override
   public void showBusy(boolean busy) {
      super.showBusy(busy);
      IManagedForm managedForm = getManagedForm();
      if (managedForm != null && Widgets.isAccessible(getManagedForm().getForm())) {
         getManagedForm().getForm().getForm().setBusy(busy);
      }
   }

   public void refreshData() {
      List<IOperation> ops = new ArrayList<IOperation>();
      ops.addAll(AtsBulkLoad.getConfigLoadingOperations());
      IOperation operation = Operations.createBuilder("Load Workflow Tab").addAll(ops).build();
      Operations.executeAsJob(operation, false, Job.LONG, new ReloadJobChangeAdapter(editor));
   }
   private final class ReloadJobChangeAdapter extends JobChangeAdapter {

      private final SMAEditor editor;

      private ReloadJobChangeAdapter(SMAEditor editor) {
         this.editor = editor;
         showBusy(true);
      }

      @Override
      public void done(IJobChangeEvent event) {
         super.done(event);
         Job job = new UIJob("Draw Workflow Tab") {

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
               try {
                  if (managedForm != null && Widgets.isAccessible(managedForm.getForm())) {
                     updateTitleBar();
                     refreshToolbar();
                     setLoading(false);
                     createAtsBody();
                     addMessageDecoration(managedForm.getForm());
                     FormsUtil.addHeadingGradient(editor.getToolkit(), managedForm.getForm(), true);
                     editor.onDirtied();
                  }
               } catch (OseeCoreException ex) {
                  handleException(ex);
               } finally {
                  showBusy(false);
               }

               return Status.OK_STATUS;
            }
         };
         Operations.scheduleJob(job, false, Job.SHORT, null);
      }
   }

   private void handleException(Exception ex) {
      setLoading(false);
      if (Widgets.isAccessible(atsBody)) {
         atsBody.dispose();
      }
      OseeLog.log(Activator.class, Level.SEVERE, ex);
      new ExceptionComposite(bodyComp, ex);
      bodyComp.layout();
   }

   private void setLoading(boolean set) {
      if (set) {
         loadingComposite = new LoadingComposite(bodyComp);
         bodyComp.layout();
      } else {
         if (Widgets.isAccessible(loadingComposite)) {
            loadingComposite.dispose();
         }
      }
      showBusy(set);
   }

   private void createAtsBody() throws OseeStateException {
      if (Widgets.isAccessible(atsBody)) {
         if (getManagedForm() != null && getManagedForm().getMessageManager() != null) {
            getManagedForm().getMessageManager().removeAllMessages();
         }
         atsBody.dispose();
      }
      atsBody = editor.getToolkit().createComposite(bodyComp);
      atsBody.setLayoutData(new GridData(GridData.FILL_BOTH));
      atsBody.setLayout(new GridLayout(1, false));

      StateXWidgetPage page = WorkflowManager.getCurrentAtsWorkPage(awa);
      if (page == null) {
         OseeLog.logf(Activator.class, OseeLevel.SEVERE_POPUP,
            "Can't retrieve current page from current state [%s] of work definition [%s]", awa.getCurrentStateName(),
            awa.getWorkDefinition().getName());
      }
      createHeaderSection(WorkflowManager.getCurrentAtsWorkPage(awa));
      createPageSections();
      createUndefinedStateSections();
      createHistorySection();
      createRelationsSection();
      createOperationsSection();
      createDetailsSection();

      atsBody.layout();
      atsBody.setFocus();
      // Jump to scroll location if set
      Integer selection = guidToScrollLocation.get(awa.getGuid());
      if (selection != null) {
         JumpScrollbarJob job = new JumpScrollbarJob("");
         job.schedule(500);
      }

   }

   private void createDetailsSection() {
      try {
         smaDetailsSection = new SMADetailsSection(editor, atsBody, editor.getToolkit(), SWT.NONE);
         managedForm.addPart(smaDetailsSection);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   private void createOperationsSection() {
      try {
         SMAOperationsSection smaOperationsSection =
            new SMAOperationsSection(editor, atsBody, editor.getToolkit(), SWT.NONE);
         managedForm.addPart(smaOperationsSection);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   private void createRelationsSection() {
      try {
         smaRelationsSection = new SMARelationsSection(editor, atsBody, editor.getToolkit(), SWT.NONE);
         managedForm.addPart(smaRelationsSection);

      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   private void createUndefinedStateSections() {
      try {
         if (WEUndefinedStateSection.hasUndefinedStates(editor.getAwa())) {
            for (String stateName : WEUndefinedStateSection.getUndefinedStateNames(awa)) {
               WEUndefinedStateSection section =
                  new WEUndefinedStateSection(stateName, editor, atsBody, editor.getToolkit(), SWT.NONE);
               managedForm.addPart(section);
               undefinedStateSections.add(section);
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   private void createHistorySection() {
      try {
         smaHistorySection = new SMAHistorySection(editor, atsBody, editor.getToolkit(), SWT.NONE);
         managedForm.addPart(smaHistorySection);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   private void createPageSections() {
      try {
         // Only display current or past states
         for (StateXWidgetPage statePage : WorkflowManager.getStatePagesOrderedByOrdinal(awa)) {
            try {
               if (awa.isInState(statePage) || awa.getStateMgr().isStateVisited(statePage)) {
                  SMAWorkFlowSection section = new SMAWorkFlowSection(atsBody, SWT.NONE, statePage, awa, editor);
                  managedForm.addPart(section);
                  control = section.getMainComp();
                  sections.add(section);
                  statePages.add(statePage);
               }
            } catch (Exception ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   private void createHeaderSection(StateXWidgetPage currentStateXWidgetPage) {
      Composite headerComp = editor.getToolkit().createComposite(atsBody);
      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.widthHint = 100;
      headerComp.setLayoutData(gd);
      headerComp.setLayout(ALayout.getZeroMarginLayout(1, false));

      // Display relations
      try {
         createCurrentStateAndTeamHeaders(headerComp, editor.getToolkit());
         createTargetVersionAndAssigneeHeader(headerComp, currentStateXWidgetPage, editor.getToolkit());

         createLatestHeader(headerComp, editor.getToolkit());
         if (awa.isTeamWorkflow()) {
            actionableItemHeader = new SMAActionableItemHeader(headerComp, editor.getToolkit(), awa, editor);
         }
         workflowMetricsHeader =
            new SMAWorkflowMetricsHeader(headerComp, editor.getToolkit(), awa, editor, managedForm);
         int headerCompColumns = 4;
         createWorkDefHeader(headerComp, editor.getToolkit(), awa, headerCompColumns);
         createSMANotesHeader(headerComp, editor.getToolkit(), awa, headerCompColumns);
         createStateNotesHeader(headerComp, editor.getToolkit(), awa, headerCompColumns, null);
         createAnnotationsHeader(headerComp, editor.getToolkit());

         sections.clear();
         statePages.clear();

         if (SMARelationsHyperlinkComposite.relationExists(awa)) {
            smaRelationsComposite = new SMARelationsHyperlinkComposite(atsBody, SWT.NONE, editor);
            smaRelationsComposite.create(awa);
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   protected boolean isShowTargetedVersion() throws OseeCoreException {
      return awa.isTargetedVersionable();
   }

   private void createTargetVersionAndAssigneeHeader(Composite parent, StateXWidgetPage page, XFormToolkit toolkit) throws OseeCoreException {
      boolean isShowTargetedVersion = isShowTargetedVersion();
      boolean isCurrentNonCompleteCanceledState = page.isCurrentNonCompleteCancelledState(awa);
      if (!isShowTargetedVersion && !isCurrentNonCompleteCanceledState) {
         return;
      }

      Composite comp = toolkit.createContainer(parent, 6);
      comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      comp.setLayout(ALayout.getZeroMarginLayout(6, false));

      // Targeted Version
      if (isShowTargetedVersion) {
         new SMATargetedVersionHeader(comp, SWT.NONE, awa, editor);
         toolkit.createLabel(comp, "    ");
      }

      // Create Privileged Edit label
      if (editor.isPrivilegedEditModeEnabled()) {
         Label label = toolkit.createLabel(comp, "(Privileged Edit Enabled)");
         label.setForeground(Displays.getSystemColor(SWT.COLOR_RED));
         label.setToolTipText("Privileged Edit Mode is Enabled.  Editing any field in any state is authorized.  Select icon to disable");
      }

      // Current Assignees
      if (isCurrentNonCompleteCanceledState) {
         boolean editable = WorkflowManager.isAssigneeEditable(awa, editor.isPrivilegedEditModeEnabled());

         new SMAAssigneesHeader(comp, SWT.NONE, awa, editable, editor);
      }
   }

   private void addMessageDecoration(ScrolledForm form) {
      form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter() {

         @Override
         public void linkActivated(HyperlinkEvent e) {
            String title = e.getLabel();
            Object href = e.getHref();
            if (href instanceof IMessage[]) {
               Point noteLocation = ((Control) e.widget).toDisplay(0, 0);
               noteLocation.x += 10;
               noteLocation.y += 10;

               MessageSummaryNote note = new MessageSummaryNote(getManagedForm(), title, (IMessage[]) href);
               note.setLocation(noteLocation);
               note.open();
            }
         }

      });
   }

   private void refreshToolbar() throws OseeCoreException {
      IToolBarManager toolBarMgr = managedForm.getForm().getToolBarManager();
      toolBarMgr.removeAll();

      if (awa.isTeamWorkflow() && (AtsBranchManagerCore.isCommittedBranchExists(((TeamWorkFlowArtifact) awa)) || AtsBranchManagerCore.isWorkingBranchInWork(((TeamWorkFlowArtifact) awa)))) {
         toolBarMgr.add(new ShowMergeManagerAction((TeamWorkFlowArtifact) awa));
         toolBarMgr.add(new ShowChangeReportAction((TeamWorkFlowArtifact) awa));
      }
      toolBarMgr.add(new FavoriteAction(editor));
      if (awa.getParentAWA() != null) {
         toolBarMgr.add(new OpenParentAction(awa));
      }
      toolBarMgr.add(new EmailActionAction(editor));
      toolBarMgr.add(new AddNoteAction(awa, editor));
      toolBarMgr.add(new OpenInAtsWorldAction(awa));
      toolBarMgr.add(new OpenActionViewAction());
      if (AtsUtilCore.isAtsAdmin()) {
         toolBarMgr.add(new OpenInArtifactEditorAction(editor));
      }
      toolBarMgr.add(new OpenVersionArtifactAction(awa));
      if (awa instanceof TeamWorkFlowArtifact) {
         toolBarMgr.add(new OpenTeamDefinitionAction((TeamWorkFlowArtifact) awa));
      }
      toolBarMgr.add(new CopyActionDetailsAction(awa));
      toolBarMgr.add(new PrivilegedEditAction(awa, editor));
      toolBarMgr.add(new ResourceHistoryAction(awa));
      toolBarMgr.add(new ReloadAction(awa));

      managedForm.getForm().updateToolBar();
   }

   public Result isXWidgetDirty() throws OseeCoreException {
      for (SMAWorkFlowSection section : sections) {
         Result result = section.isXWidgetDirty();
         if (result.isTrue()) {
            return result;
         }
      }
      return Result.FalseResult;
   }

   public Result isXWidgetSavable() {
      for (SMAWorkFlowSection section : sections) {
         Result result = section.isXWidgetSavable();
         if (result.isFalse()) {
            return result;
         }
      }
      return Result.TrueResult;
   }

   public void saveXWidgetToArtifact() throws OseeCoreException {
      List<IArtifactStoredWidget> artWidgets = new ArrayList<IArtifactStoredWidget>();
      // Collect all dirty widgets first (so same attribute shown on different sections don't colide
      for (SMAWorkFlowSection section : sections) {
         section.getDirtyIArtifactWidgets(artWidgets);
      }
      for (IArtifactStoredWidget widget : artWidgets) {
         widget.saveToArtifact();
      }
   }

   @Override
   public void dispose() {
      if (actionableItemHeader != null) {
         actionableItemHeader.dispose();
      }
      if (workflowMetricsHeader != null) {
         workflowMetricsHeader.dispose();
      }
      if (smaDetailsSection != null) {
         smaDetailsSection.dispose();
      }
      for (WEUndefinedStateSection section : undefinedStateSections) {
         section.dispose();
      }
      if (smaHistorySection != null) {
         smaHistorySection.dispose();
      }
      if (smaRelationsSection != null) {
         smaRelationsSection.dispose();
      }
      for (SMAWorkFlowSection section : sections) {
         section.dispose();
      }

      if (editor.getToolkit() != null) {
         editor.getToolkit().dispose();
      }
   }

   private Control control = null;

   private void storeScrollLocation() {
      if (managedForm != null && managedForm.getForm() != null) {
         Integer selection = managedForm.getForm().getVerticalBar().getSelection();
         guidToScrollLocation.put(awa.getGuid(), selection);
      }
   }

   private class JumpScrollbarJob extends Job {
      public JumpScrollbarJob(String name) {
         super(name);
      }

      @Override
      protected IStatus run(IProgressMonitor monitor) {
         Displays.ensureInDisplayThread(new Runnable() {
            @Override
            public void run() {
               Integer selection = guidToScrollLocation.get(awa.getGuid());

               // Find the ScrolledComposite operating on the control.
               ScrolledComposite sComp = null;
               if (control == null || control.isDisposed()) {
                  return;
               }
               Composite parent = control.getParent();
               while (parent != null) {
                  if (parent instanceof ScrolledComposite) {
                     sComp = (ScrolledComposite) parent;
                     break;
                  }
                  parent = parent.getParent();
               }

               if (sComp != null) {
                  sComp.setOrigin(0, selection);
               }
            }
         });
         return Status.OK_STATUS;

      }
   }

   private void createCurrentStateAndTeamHeaders(Composite comp, XFormToolkit toolkit) {
      Composite topLineComp = new Composite(comp, SWT.NONE);
      topLineComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      topLineComp.setLayout(ALayout.getZeroMarginLayout(3, false));
      toolkit.adapt(topLineComp);

      try {
         FormsUtil.createLabelText(toolkit, topLineComp, "Current State: ", awa.getStateMgr().getCurrentStateName());
         FormsUtil.createLabelText(toolkit, topLineComp, "Created: ", DateUtil.getMMDDYYHHMM(awa.getCreatedDate()));
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }

      new SMAOriginatorHeader(topLineComp, SWT.NONE, awa, editor);

      try {
         if (awa.isTeamWorkflow()) {
            FormsUtil.createLabelText(toolkit, topLineComp, "Team: ", ((TeamWorkFlowArtifact) awa).getTeamName());
         } else if ((awa.isTask() || awa.isReview()) && awa.getParentAWA() != null) {
            String pcrId = TeamWorkFlowManager.getPcrId(awa.getParentAWA());
            FormsUtil.createLabelText(toolkit, topLineComp, "Parent Id: ", pcrId);
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      FormsUtil.createLabelText(toolkit, topLineComp, awa.getArtifactSuperTypeName() + " Id: ",
         awa.getAtsId());

      try {
         String pcrId = TeamWorkFlowManager.getPcrId(awa);
         if (Strings.isValid(pcrId)) {
            FormsUtil.createLabelText(toolkit, topLineComp, " Id: ", pcrId);
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   private void createLatestHeader(Composite comp, XFormToolkit toolkit) {
      if (awa.isHistoricalVersion()) {
         Label label =
            toolkit.createLabel(
               comp,
               "This is a historical version of this " + awa.getArtifactTypeName() + " and can not be edited; Select \"Open Latest\" to view/edit latest version.");
         label.setForeground(Displays.getSystemColor(SWT.COLOR_RED));
      }
   }

   private void createAnnotationsHeader(Composite comp, XFormToolkit toolkit) {
      try {
         if (AttributeAnnotationManager.getAnnotations(awa).size() > 0) {
            new AnnotationComposite(toolkit, comp, SWT.None, awa);
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, "Exception resolving annotations", ex);
      }
   }

   public static void createWorkDefHeader(Composite comp, XFormToolkit toolkit, AbstractWorkflowArtifact sma, int horizontalSpan) {
      // Display SMA Note
      WorkDefinitionMatch workDefMatch = sma.getWorkDefinitionMatch();
      Label label =
         FormsUtil.createLabelValue(toolkit, comp, "Work Definition: ", workDefMatch.getWorkDefinition().getName());
      label.addListener(SWT.MouseDoubleClick, new Listener() {

         @Override
         public void handleEvent(Event event) {
            IWorkbenchPage page = AWorkbench.getActivePage();
            try {
               page.showView("org.eclipse.ui.views.ContentOutline", null, IWorkbenchPage.VIEW_ACTIVATE);
            } catch (PartInitException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      });
   }

   public static void createSMANotesHeader(Composite comp, XFormToolkit toolkit, AbstractWorkflowArtifact sma, int horizontalSpan) throws OseeCoreException {
      // Display SMA Note
      String note = sma.getSoleAttributeValue(AtsAttributeTypes.SmaNote, "");
      if (!note.equals("")) {
         FormsUtil.createLabelOrHyperlink(comp, toolkit, horizontalSpan, "Note: " + note);
      }
   }

   public static void createStateNotesHeader(Composite comp, XFormToolkit toolkit, AbstractWorkflowArtifact sma, int horizontalSpan, String forStateName) {
      // Display global Notes
      for (NoteItem noteItem : sma.getNotes().getNoteItems()) {
         if (forStateName == null || noteItem.getState().equals(forStateName)) {
            FormsUtil.createLabelOrHyperlink(comp, toolkit, horizontalSpan, noteItem.toString());
         }
      }
   }

   public void refresh() {
      if (editor != null && !awa.isInTransition()) {
         // remove all pages
         for (SMAWorkFlowSection section : sections) {
            section.dispose();
         }
         // add pages back
         refreshData();
      }
   }

   public List<StateXWidgetPage> getPages() {
      return statePages;
   }

   public List<SMAWorkFlowSection> getSections() {
      return sections;
   }

   public SMAWorkFlowSection getSectionForCurrentState() {
      for (SMAWorkFlowSection section : sections) {
         if (section.isCurrentState()) {
            return section;
         }
      }
      return null;
   }

   @Override
   public WorldXViewer getWorldXViewer() {
      // do nothing
      return null;
   }

   @Override
   public void removeItems(Collection<? extends Object> objects) {
      for (Object obj : objects) {
         if (obj instanceof EventBasicGuidArtifact) {
            EventBasicGuidArtifact guidArt = (EventBasicGuidArtifact) obj;
            if (guidArt.getModType() == EventModType.Purged) {
               refresh();
               return;
            }
         }
      }
   }

   @Override
   public void relationsModifed(Collection<Artifact> relModifiedArts) {
      if (relModifiedArts.contains(awa)) {
         refresh();
      }
   }

   @Override
   public boolean isDisposed() {
      return editor.isDisposed();
   }

}