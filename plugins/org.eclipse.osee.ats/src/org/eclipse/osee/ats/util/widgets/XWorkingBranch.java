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
package org.eclipse.osee.ats.util.widgets;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.core.client.branch.AtsBranchUtil;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowManager;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.AtsBranchManager;
import org.eclipse.osee.framework.access.AccessControlData;
import org.eclipse.osee.framework.access.AccessControlManager;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.SystemGroup;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.event.listener.IAccessControlEventListener;
import org.eclipse.osee.framework.skynet.core.event.listener.IArtifactEventListener;
import org.eclipse.osee.framework.skynet.core.event.listener.IBranchEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.AccessControlEvent;
import org.eclipse.osee.framework.skynet.core.event.model.AccessControlEventType;
import org.eclipse.osee.framework.skynet.core.event.model.ArtifactEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEventType;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.explorer.ArtifactExplorer;
import org.eclipse.osee.framework.ui.skynet.widgets.GenericXWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.IArtifactWidget;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * @author Megumi Telles
 * @author Donald G. Dunne
 */
public class XWorkingBranch extends GenericXWidget implements IArtifactWidget, IAccessControlEventListener, IArtifactEventListener, IBranchEventListener {

   private TeamWorkFlowArtifact teamArt;
   private Button createBranchButton;
   private Button showArtifactExplorer;
   private Button showChangeReport;
   private Button deleteBranchButton;
   private Button favoriteBranchButton;
   private Button lockBranchButton;
   private XWorkingBranchEnablement enablement;
   public static String NAME = "Working Branch";
   public static String WIDGET_NAME = "XWorkingBranch";

   private Composite buttonComp;

   public static enum BranchStatus {
      Not_Started("No Working Branch", false),
      Changes_InProgress("Changes In Progress", true),
      Changes_NotPermitted__BranchCommitted("Branch Committed - No Changes Permitted", false),
      Changes_NotPermitted__CreationInProgress("Branch Being Created - No Changes Permitted", false),
      Changes_NotPermitted__CommitInProgress("Branch Being Committed - No Changes Permitted", false);

      private final String displayName;
      private final boolean changesPermitted;

      private BranchStatus(String displayName, boolean changesPermitted) {
         this.displayName = displayName;
         this.changesPermitted = changesPermitted;
      }

      public String getDisplayName() {
         return displayName;
      }

      public boolean isChangesPermitted() {
         return changesPermitted;
      }
   }

   public XWorkingBranch() {
      super(NAME);
      OseeEventManager.addListener(this);
   }

   @Override
   public TeamWorkFlowArtifact getArtifact() {
      return teamArt;
   }

   @Override
   protected void createControls(Composite parent, int horizontalSpan) {
      if (horizontalSpan < 2) {
         horizontalSpan = 2;
      }
      Composite mainComp = new Composite(parent, SWT.NONE);
      mainComp.setLayout(new GridLayout(1, false));
      mainComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      if (toolkit != null) {
         toolkit.adapt(mainComp);
      }
      if (!getLabel().equals("")) {
         labelWidget = new Label(mainComp, SWT.NONE);
         labelWidget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      }

      buttonComp = new Composite(mainComp, SWT.NONE);
      buttonComp.setLayout(new GridLayout(6, false));
      buttonComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      if (toolkit != null) {
         toolkit.adapt(buttonComp);
      }

      createBranchButton = createNewButton(buttonComp);
      createBranchButton.setToolTipText("Create Working Branch");
      createBranchButton.addListener(SWT.Selection, new Listener() {
         @Override
         public void handleEvent(Event e) {
            try {
               enablement.disableAll();
            } catch (Exception ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
            refreshEnablement();
            // Create working branch
            Result result = AtsBranchUtil.createWorkingBranch_Validate(teamArt);
            if (result.isFalse()) {
               AWorkbench.popup(result);
               return;
            }
            try {
               IOseeBranch parentBranch =
                  AtsClientService.get().getBranchService().getConfiguredBranchForWorkflow(teamArt);
               // Retrieve parent branch to create working branch from
               if (!MessageDialog.openConfirm(
                  Displays.getActiveShell(),
                  "Create Working Branch",
                  "Create a working branch from parent branch\n\n\"" + parentBranch.getName() + "\"?\n\n" + "NOTE: Working branches are necessary when OSEE Artifact changes " + "are made during implementation.")) {
                  enablement.refresh();
                  refreshEnablement();
                  return;
               }
               AtsBranchUtil.createWorkingBranch_Create(teamArt);
               Thread.sleep(2000);
            } catch (Exception ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
            enablement.refresh();
            refreshEnablement();
         }
      });

      showArtifactExplorer = createNewButton(buttonComp);
      showArtifactExplorer.setToolTipText("Show Artifact Explorer");
      showArtifactExplorer.addListener(SWT.Selection, new Listener() {
         @Override
         public void handleEvent(Event e) {
            try {
               ArtifactExplorer.exploreBranch(teamArt.getWorkingBranch());
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      });

      showChangeReport = createNewButton(buttonComp);
      showChangeReport.setToolTipText("Show Change Report");
      showChangeReport.addListener(SWT.Selection, new Listener() {
         @Override
         public void handleEvent(Event e) {
            AtsBranchManager.showChangeReport(teamArt);
         }
      });

      deleteBranchButton = createNewButton(buttonComp);
      deleteBranchButton.setToolTipText("Delete Working Branch");
      deleteBranchButton.addListener(SWT.Selection, new Listener() {
         @Override
         public void handleEvent(Event e) {
            try {
               enablement.disableAll();
            } catch (Exception ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
            refreshEnablement();
            AtsBranchManager.deleteWorkingBranch(teamArt, true);
            enablement.refresh();
            refreshEnablement();
         }
      });

      favoriteBranchButton = createNewButton(buttonComp);
      favoriteBranchButton.setToolTipText("Toggle Working Branch as Favorite");
      favoriteBranchButton.addListener(SWT.Selection, new Listener() {
         @Override
         public void handleEvent(Event e) {
            markWorkingBranchAsFavorite();
         }
      });

      lockBranchButton = createNewButton(buttonComp);
      lockBranchButton.setToolTipText("Toggle Working Branch Access Control");
      lockBranchButton.addListener(SWT.Selection, new Listener() {
         @Override
         public void handleEvent(Event e) {
            toggleWorkingBranchLock();
         }
      });

      createBranchButton.setImage(ImageManager.getImage(FrameworkImage.BRANCH));
      deleteBranchButton.setImage(ImageManager.getImage(FrameworkImage.TRASH));
      favoriteBranchButton.setImage(ImageManager.getImage(AtsImage.FAVORITE));
      showArtifactExplorer.setImage(ImageManager.getImage(FrameworkImage.ARTIFACT_EXPLORER));
      showChangeReport.setImage(ImageManager.getImage(FrameworkImage.BRANCH_CHANGE));
      refreshLockImage();
      refreshLabel();
      refreshEnablement();
   }

   private void refreshLockImage() {
      boolean noBranch = false, someAccessControlSet = false;
      Branch branch = null;
      try {
         branch = teamArt.getWorkingBranch();
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      // just show normal icon if no branch yet
      if (branch == null) {
         noBranch = true;
      } else {
         someAccessControlSet = !AccessControlManager.getAccessControlList(branch).isEmpty();
      }
      lockBranchButton.setImage(ImageManager.getImage((noBranch || someAccessControlSet) ? FrameworkImage.LOCK_LOCKED : FrameworkImage.LOCK_UNLOCKED));
      lockBranchButton.redraw();
      lockBranchButton.getParent().redraw();
   }

   private void markWorkingBranchAsFavorite() {
      try {
         User user =
            AtsClientService.get().getUserServiceClient().getOseeUser(
               AtsClientService.get().getUserService().getCurrentUser());
         if (user.isSystemUser()) {
            AWorkbench.popup("Can't set preference as System User = " + user);
            return;
         }
         Branch branch = teamArt.getWorkingBranch();
         if (branch == null) {
            AWorkbench.popup("Working branch doesn't exist");
            return;
         }
         boolean isFavorite = user.isFavoriteBranch(branch);
         String message =
            String.format("Working branch is currently [%s]\n\nToggle favorite?",
               isFavorite ? "Favorite" : "NOT Favorite");
         if (MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Toggle Branch as Favorite", message)) {
            user.toggleFavoriteBranch(branch);
            OseeEventManager.kickBranchEvent(this, new BranchEvent(BranchEventType.FavoritesUpdated, branch.getUuid()));
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   private void toggleWorkingBranchLock() {
      try {
         Branch branch = teamArt.getWorkingBranch();
         if (branch == null) {
            AWorkbench.popup("Working branch doesn't exist");
            return;
         }
         boolean isLocked = false, manuallyLocked = false;
         Collection<AccessControlData> datas = AccessControlManager.getAccessControlList(branch);
         if (datas.size() > 1) {
            manuallyLocked = true;
         } else if (datas.isEmpty()) {
            isLocked = false;
         } else {
            AccessControlData data = datas.iterator().next();
            if (data.getSubject().equals(SystemGroup.Everyone.getArtifact()) && data.getBranchPermission() == PermissionEnum.READ) {
               isLocked = true;
            } else {
               manuallyLocked = true;
            }
         }
         if (manuallyLocked) {
            AWorkbench.popup("Manual access control applied to branch.  Can't override.\n\nUse Access Control option of Branch Manager");
            return;
         }
         String message =
            String.format("Working branch is currently [%s]\n\n%s the Branch?", isLocked ? "Locked" : "NOT Locked",
               isLocked ? "UnLock" : "Lock");
         if (MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Toggle Branch Lock", message)) {
            if (isLocked) {
               AccessControlManager.removeAccessControlDataIf(true, datas.iterator().next());
            } else {
               AccessControlManager.setPermission(SystemGroup.Everyone.getArtifact(), branch, PermissionEnum.READ);
            }
            AccessControlEvent event = new AccessControlEvent();
            event.setEventType(AccessControlEventType.BranchAccessControlModified);
            OseeEventManager.kickAccessControlArtifactsEvent(this, event);
            AWorkbench.popup(String.format("Branch set to [%s]", !isLocked ? "Locked" : "NOT Locked"));
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   public Button createNewButton(Composite comp) {
      if (toolkit != null) {
         return toolkit.createButton(comp, null, SWT.PUSH);
      }
      return new Button(comp, SWT.PUSH);
   }

   public void refreshLabel() {
      if (labelWidget != null && Widgets.isAccessible(labelWidget) && !getLabel().equals("")) {
         try {
            Branch workBranch = enablement.getWorkingBranch();
            String labelStr =
               getLabel() + ": " + enablement.getStatus().getDisplayName() + (workBranch != null ? " - " + workBranch.getShortName() : "");
            labelWidget.setText(labelStr);
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
         if (getToolTip() != null) {
            labelWidget.setToolTipText(getToolTip());
         }
         labelWidget.getParent().redraw();
         if (getManagedForm() != null) {
            getManagedForm().reflow(true);
         }
      }
   }

   public void refreshEnablement() {
      createBranchButton.setEnabled(enablement.isCreateBranchButtonEnabled());
      showArtifactExplorer.setEnabled(enablement.isShowArtifactExplorerButtonEnabled());
      showChangeReport.setEnabled(enablement.isShowChangeReportButtonEnabled());
      deleteBranchButton.setEnabled(enablement.isDeleteBranchButtonEnabled());
      favoriteBranchButton.setEnabled(enablement.isFavoriteBranchButtonEnabled());
      lockBranchButton.setEnabled(enablement.isDeleteBranchButtonEnabled());
   }

   public static boolean isPurgeBranchButtonEnabled(TeamWorkFlowArtifact teamArt) throws OseeCoreException {
      return AtsClientService.get().getBranchService().isWorkingBranchInWork(teamArt);
   }

   @Override
   public void dispose() {
      OseeEventManager.removeListener(this);
   }

   @Override
   public Control getControl() {
      return labelWidget;
   }

   @Override
   public IStatus isValid() {
      // Need this cause it removes all error items of this namespace
      return new Status(IStatus.OK, getClass().getSimpleName(), "");
   }

   public void refreshOnBranchEvent() {
      if (teamArt == null || labelWidget == null || labelWidget.isDisposed()) {
         return;
      }
      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            try {
               enablement.refresh();
               enablement.getStatus();
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
            Displays.ensureInDisplayThread(new Runnable() {
               @Override
               public void run() {
                  if (Widgets.isAccessible(createBranchButton)) {
                     refreshEnablement();
                     refreshLabel();
                     refreshLockImage();
                  }
               }
            });
         }
      };
      Thread thread = new Thread(runnable);
      thread.start();
   }

   @Override
   public Result isDirty() {
      return Result.FalseResult;
   }

   @Override
   public void revert() {
      // do nothing
   }

   @Override
   public void saveToArtifact() {
      // do nothing
   }

   @Override
   public void setArtifact(Artifact artifact) {
      if (artifact.isOfType(AtsArtifactTypes.TeamWorkflow)) {
         this.teamArt = TeamWorkFlowManager.cast(artifact);
      }
      enablement = new XWorkingBranchEnablement(teamArt);
   }

   @Override
   public String toString() {
      return String.format("%s", getLabel());
   }

   @Override
   public List<? extends IEventFilter> getEventFilters() {
      return null;
   }

   @Override
   public void handleArtifactEvent(ArtifactEvent artifactEvent, Sender sender) {
      refreshOnBranchEvent();
   }

   @Override
   public void handleBranchEvent(Sender sender, BranchEvent branchEvent) {
      refreshOnBranchEvent();
   }

   @Override
   public void handleAccessControlArtifactsEvent(Sender sender, AccessControlEvent accessControlEvent) {
      if (accessControlEvent.getEventType() == AccessControlEventType.BranchAccessControlModified) {
         refreshOnBranchEvent();
      }
   }

}
