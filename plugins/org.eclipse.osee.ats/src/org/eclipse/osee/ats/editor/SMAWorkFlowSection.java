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
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osee.ats.api.workdef.IStateToken;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.ats.api.workflow.log.IAtsLogItem;
import org.eclipse.osee.ats.core.client.task.AbstractTaskableArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.workflow.WorkflowManagerCore;
import org.eclipse.osee.ats.core.workflow.log.AtsLogUtility;
import org.eclipse.osee.ats.editor.stateItem.AtsStateItemManager;
import org.eclipse.osee.ats.editor.stateItem.IAtsStateItem;
import org.eclipse.osee.ats.editor.widget.ReviewInfoXWidget;
import org.eclipse.osee.ats.editor.widget.StateHoursSpentXWidget;
import org.eclipse.osee.ats.editor.widget.StatePercentCompleteXWidget;
import org.eclipse.osee.ats.editor.widget.TaskInfoXWidget;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.XCancellationReasonTextWidget;
import org.eclipse.osee.ats.workdef.StateXWidgetPage;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.XFormToolkit;
import org.eclipse.osee.framework.ui.skynet.widgets.IArtifactStoredWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.XComboViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.XLabelValue;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.util.SwtXWidgetRenderer;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.FontManager;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Donald G. Dunne
 */
public class SMAWorkFlowSection extends SectionPart {

   protected final AbstractWorkflowArtifact sma;
   private final StateXWidgetPage statePage;
   private final boolean isEditable, isCurrentState, isGlobalEditable;
   private Composite mainComp;
   private final List<XWidget> allXWidgets = new ArrayList<XWidget>();
   private boolean sectionCreated = false;
   private Section section;
   private final SMAEditor editor;
   private WETransitionComposite workflowTransitionComposite;

   public SMAWorkFlowSection(Composite parent, int style, StateXWidgetPage page, AbstractWorkflowArtifact sma, final SMAEditor editor) throws OseeCoreException {
      super(parent, editor.getToolkit(), style | ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
      this.statePage = page;
      this.sma = sma;
      this.editor = editor;

      isEditable =
         WorkflowManagerCore.isEditable(AtsClientService.get().getUserService().getCurrentUser(), sma,
            page.getStateDefinition(), editor.isPrivilegedEditModeEnabled());
      isGlobalEditable = !sma.isReadOnly() && sma.isAccessControlWrite() && editor.isPrivilegedEditModeEnabled();
      isCurrentState = sma.isInState(page);
      // parent.setBackground(Displays.getSystemColor(SWT.COLOR_CYAN));
   }

   @Override
   public void initialize(final IManagedForm form) {
      super.initialize(form);

      section = getSection();
      try {
         section.setText(getCurrentStateTitle(sma, statePage.getName(), isEditable, isCurrentState,
            statePage.getStateType().isCancelledState()));
         if (sma.isInState(statePage)) {
            section.setTitleBarForeground(Displays.getSystemColor(SWT.COLOR_DARK_GREEN));
            section.setBackground(AtsUtil.ACTIVE_COLOR);
         }
         section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         // section.setBackground(Displays.getSystemColor(SWT.COLOR_MAGENTA));

         boolean isCurrentSectionExpanded = isCurrentSectionExpanded(statePage);

         if (isCurrentSectionExpanded) {
            createSection(section);
         }
         // Only load when users selects section
         section.addListener(SWT.Activate, new Listener() {

            @Override
            public void handleEvent(Event e) {
               try {
                  createSection(section);
               } catch (OseeCoreException ex) {
                  OseeLog.log(Activator.class, Level.SEVERE, ex);
               }
            }
         });

         section.layout();
         section.setExpanded(isCurrentSectionExpanded);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   /**
    * Override to apply different algorithm to current section expansion.
    */
   public boolean isCurrentSectionExpanded(IStateToken state) {
      return sma.isInState(state);
   }

   private synchronized void createSection(Section section) throws OseeCoreException {
      if (sectionCreated) {
         return;
      }

      mainComp = editor.getToolkit().createClientContainer(section, 2);
      mainComp.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING));
      mainComp.setLayout(ALayout.getZeroMarginLayout(1, false));
      // mainComp.setBackground(Displays.getSystemColor(SWT.COLOR_DARK_YELLOW));
      mainComp.layout();

      SMAWorkFlowTab.createStateNotesHeader(mainComp, editor.getToolkit(), sma, 2, statePage.getName());

      Composite workComp = createWorkArea(mainComp, statePage, editor.getToolkit());

      if (isCurrentState) {
         workflowTransitionComposite = new WETransitionComposite(mainComp, this, editor, isEditable);
      }

      GridData gridData = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING);
      gridData.widthHint = 400;
      workComp.setLayoutData(gridData);
      sectionCreated = true;
   }

   protected Composite createWorkArea(Composite comp, StateXWidgetPage statePage, XFormToolkit toolkit) throws OseeCoreException {
      statePage.generateLayoutDatas(sma);

      if (statePage.getStateDefinition().getDescription() != null) {
         Composite labelComp = toolkit.createContainer(comp, 1);
         labelComp.setLayoutData(new GridData(GridData.FILL_BOTH));
         labelComp.setLayout(ALayout.getZeroMarginLayout());

         Label descLabel =
            editor.getToolkit().createLabel(labelComp,
               " State Description: " + statePage.getStateDefinition().getDescription());
         GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false);
         descLabel.setBackground(AtsUtil.ACTIVE_COLOR);
         descLabel.setLayoutData(gd);
      }

      // Create Page
      Composite workComp = toolkit.createContainer(comp, 1);
      workComp.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING));
      // workComp.setBackground(Displays.getSystemColor(SWT.COLOR_GREEN));

      createMetricsHeader(workComp);

      // Add any dynamic XWidgets declared for page by IAtsStateItem extensions
      for (IAtsStateItem item : AtsStateItemManager.getStateItems()) {
         for (XWidget xWidget : item.getDynamicXWidgetsPreBody(sma, statePage.getName())) {
            xWidget.createWidgets(workComp, 2);
            allXWidgets.add(xWidget);
            allXWidgets.addAll(xWidget.getChildrenXWidgets());
         }
      }

      if (statePage.getStateType().isCompletedOrCancelledState()) {
         Composite completeComp = new Composite(workComp, SWT.None);
         GridLayout layout = new GridLayout(1, false);
         completeComp.setLayout(layout);
         completeComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         if (statePage.getStateType().isCancelledState()) {
            createCancelledPageWidgets(completeComp);
         } else if (statePage.getStateType().isCompletedState()) {
            createCompletedPageWidgets(completeComp);
         }
      }

      // Create dynamic XWidgets
      SwtXWidgetRenderer dynamicXWidgetLayout =
         statePage.createBody(getManagedForm(), workComp, sma, xModListener, isEditable || isGlobalEditable);
      for (XWidget xWidget : dynamicXWidgetLayout.getXWidgets()) {
         allXWidgets.add(xWidget);
         allXWidgets.addAll(xWidget.getChildrenXWidgets());
      }

      // Add any dynamic XWidgets declared for page by IAtsStateItem extensions
      for (IAtsStateItem item : AtsStateItemManager.getStateItems()) {
         for (XWidget xWidget : item.getDynamicXWidgetsPostBody(sma, statePage.getName())) {
            xWidget.createWidgets(workComp, 2);
            allXWidgets.add(xWidget);
            allXWidgets.addAll(xWidget.getChildrenXWidgets());
         }
      }

      createTaskFooter(workComp, statePage);
      createReviewFooter(workComp, statePage);

      // Set all XWidget labels to bold font
      for (XWidget xWidget : allXWidgets) {
         if (xWidget.getLabelWidget() != null) {
            SMAEditor.setLabelFonts(xWidget.getLabelWidget(), FontManager.getDefaultLabelFont());
         }
      }

      return workComp;
   }

   private void createCancelledPageWidgets(Composite parent) throws OseeCoreException {
      XWidget xWidget = null;
      xWidget = new XLabelValue("Cancelled from State", sma.getCancelledFromState());
      xWidget.createWidgets(parent, 1);
      allXWidgets.add(xWidget);

      if (editor.isPrivilegedEditModeEnabled()) {
         xWidget = new XCancellationReasonTextWidget(sma);
         xWidget.addXModifiedListener(xModListener);
      } else {
         xWidget = new XLabelValue("Cancellation Reason", sma.getCancelledReason());
      }
      xWidget.createWidgets(parent, 1);
      allXWidgets.add(xWidget);
   }

   private void createCompletedPageWidgets(Composite parent) throws OseeCoreException {
      XWidget xWidget = null;
      xWidget = new XLabelValue("Completed from State", sma.getCompletedFromState());
      xWidget.createWidgets(parent, 1);
      allXWidgets.add(xWidget);
   }

   private void createMetricsHeader(Composite parent) {
      if (!statePage.getStateType().isCompletedOrCancelledState()) {
         Composite comp = new Composite(parent, SWT.None);
         GridLayout layout = ALayout.getZeroMarginLayout(4, false);
         layout.marginLeft = 2;
         comp.setLayout(layout);
         comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         try {
            if (AtsClientService.get().getWorkDefinitionAdmin().isStateWeightingEnabled(sma.getWorkDefinition())) {
               allXWidgets.add(new StatePercentCompleteXWidget(getManagedForm(), statePage, sma, comp, 2, xModListener,
                  isCurrentState, editor));
            }
         } catch (OseeStateException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
         allXWidgets.add(new StateHoursSpentXWidget(getManagedForm(), statePage, sma, comp, 2, xModListener,
            isCurrentState, editor));
      }
   }

   private void createReviewFooter(Composite parent, IStateToken forState) {
      if (isShowReviewInfo() && sma.isTeamWorkflow()) {
         Composite comp = new Composite(parent, SWT.None);
         GridLayout layout = new GridLayout(1, false);
         comp.setLayout(layout);
         comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         allXWidgets.add(new ReviewInfoXWidget(this, editor.getToolkit(), (TeamWorkFlowArtifact) sma, forState, comp, 1));
      }
   }

   private void createTaskFooter(Composite parent, IStateToken state) {
      if (sma instanceof AbstractTaskableArtifact) {
         Composite comp = new Composite(parent, SWT.None);
         GridLayout layout = new GridLayout(6, false);
         comp.setLayout(layout);
         comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         allXWidgets.add(new TaskInfoXWidget(getManagedForm(), ((AbstractTaskableArtifact) sma), state, comp, 2));
      }
   }

   protected boolean isShowReviewInfo() {
      return sma.isTeamWorkflow();
   }

   public Result isXWidgetSavable() {
      for (XWidget widget : allXWidgets) {
         if (widget instanceof IArtifactStoredWidget) {
            IStatus status = widget.isValid();
            if (!status.isOK()) {
               return new Result(false, status.getMessage());
            }
         }
      }
      return Result.TrueResult;
   }

   @Override
   public String toString() {
      return statePage + " for " + getSma();
   }

   public Result isXWidgetDirty() throws OseeCoreException {
      for (XWidget widget : allXWidgets) {
         if (widget instanceof IArtifactStoredWidget) {
            IArtifactStoredWidget artifactStoredWidget = (IArtifactStoredWidget) widget;
            Result result = artifactStoredWidget.isDirty();
            if (result.isTrue()) {
               return result;
            }
         }
      }
      return Result.FalseResult;
   }

   public void getDirtyIArtifactWidgets(List<IArtifactStoredWidget> widgets) throws OseeCoreException {
      for (XWidget widget : allXWidgets) {
         if (widget instanceof IArtifactStoredWidget) {
            IArtifactStoredWidget artifactStoredWidget = (IArtifactStoredWidget) widget;
            if (artifactStoredWidget.isDirty().isTrue()) {
               widgets.add(artifactStoredWidget);
            }
         }
      }
   }

   protected static String getCurrentStateTitle(AbstractWorkflowArtifact sma, String statePageName, boolean isEditable, boolean isCurrentState, boolean isCancelledState) throws OseeCoreException {
      StringBuffer sb = new StringBuffer(statePageName);
      if (isEditable && !sma.isCompleted() && !sma.isCancelled()) {
         sb.append(" - Current State");
      }
      if (sma.isCancelled()) {
         if (isCancelledState) {
            if (Strings.isValid(sma.getCancelledReason())) {
               sb.append(" - Reason: ");
               sb.append(Strings.truncate(sma.getCancelledReason(), 50, true));
            }
         }
      }
      if (isCurrentState) {
         if (sma.isCompleted()) {
            if (!sma.getCurrentStateName().equals(StateType.Completed.toString())) {
               sb.append(" (Completed)");
            }
            sb.append(" - ");
            sb.append(DateUtil.getMMDDYYHHMM(sma.getCompletedDate()));
            IAtsLogItem item = sma.getStateMgr().getStateStartedData(statePageName);
            if (item != null) {
               sb.append(" by ");
               sb.append(AtsLogUtility.getUserName(item.getUserId()));
            }
         } else if (sma.isCancelled()) {
            if (!sma.getCurrentStateName().equals(StateType.Cancelled.toString())) {
               sb.append(" (Cancelled)");
            }
            sb.append(" - ");
            sb.append(DateUtil.getMMDDYYHHMM(sma.internalGetCancelledDate()));
            IAtsLogItem item = sma.getStateMgr().getStateStartedData(statePageName);
            if (item != null) {
               sb.append(" by ");
               sb.append(AtsLogUtility.getUserName(item.getUserId()));
            }
         }
         if (sma.getStateMgr().getAssignees().size() > 0) {
            sb.append(" assigned to ");
            sb.append(sma.getStateMgr().getAssigneesStr(80));
         }
      } else {
         IAtsLogItem item = null;
         if (sma.isCancelled() && sma.getCancelledFromState().equals(statePageName)) {
            item = sma.getStateCancelledData(statePageName);
            sb.append(" - State Cancelled ");
         } else {
            item = sma.getStateCompletedData(statePageName);
            sb.append(" - State Completed ");
         }
         if (item != null) {
            sb.append(item.getDate(DateUtil.MMDDYYHHMM));
            sb.append(" by ");
            sb.append(AtsLogUtility.getUserName(item.getUserId()));
         }
      }
      return sb.toString();
   }

   @Override
   public void dispose() {
      for (XWidget xWidget : allXWidgets) {
         if (xWidget != null) {
            xWidget.dispose();
         }
      }
      statePage.dispose();
      super.dispose();
   }

   final SMAWorkFlowSection fSection = this;
   final XModifiedListener xModListener = new XModifiedListener() {
      @Override
      public void widgetModified(XWidget xWidget) {
         try {
            if (sma.isDeleted()) {
               return;
            }
            // Notify extensions of widget modified
            for (IAtsStateItem item : AtsStateItemManager.getStateItems()) {
               try {
                  item.widgetModified(
                     xWidget,
                     editor.getToolkit(),
                     sma.getStateDefinition(),
                     sma,
                     WorkflowManagerCore.isEditable(AtsClientService.get().getUserService().getCurrentUser(), sma,
                        sma.getStateDefinition(), false));
               } catch (Exception ex) {
                  OseeLog.log(Activator.class, Level.SEVERE, ex);
               }
            }
            if (workflowTransitionComposite != null) {
               workflowTransitionComposite.updateTransitionToState();
               workflowTransitionComposite.updateTransitionToAssignees();
            }
            editor.onDirtied();
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
   };

   @Override
   public void refresh() {
      if (!Widgets.isAccessible(mainComp)) {
         return;
      }
      super.refresh();
      try {
         if (workflowTransitionComposite != null) {
            workflowTransitionComposite.refresh();
         }
         editor.onDirtied();
         for (XWidget xWidget : allXWidgets) {
            xWidget.refresh();
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   public boolean isCurrentState() {
      return isCurrentState;
   }

   public XComboViewer getTransitionToStateCombo() {
      if (workflowTransitionComposite != null) {
         return workflowTransitionComposite.getTransitionToStateCombo();
      }
      return null;
   }

   public AbstractWorkflowArtifact getSma() {
      return sma;
   }

   public StateXWidgetPage getPage() {
      return statePage;
   }

   public Composite getMainComp() {
      return mainComp;
   }

   public List<XWidget> getXWidgets(Class<?> clazz) {
      List<XWidget> widgets = new ArrayList<XWidget>();
      for (XWidget widget : allXWidgets) {
         if (clazz.isInstance(widget)) {
            widgets.add(widget);
         }
      }
      return widgets;
   }

   public boolean isEditable() {
      return isEditable;
   }

   public StateXWidgetPage getStatePage() {
      return statePage;
   }

   public SMAEditor getEditor() {
      return editor;
   }

}
