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
package org.eclipse.osee.ats.navigate;

import java.util.logging.Level;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.ats.actions.MyFavoritesAction;
import org.eclipse.osee.ats.actions.MyWorldAction;
import org.eclipse.osee.ats.actions.NewAction;
import org.eclipse.osee.ats.actions.NewGoal;
import org.eclipse.osee.ats.actions.OpenChangeReportByIdAction;
import org.eclipse.osee.ats.actions.OpenWorkflowByIdAction;
import org.eclipse.osee.ats.actions.OpenWorldByIdAction;
import org.eclipse.osee.ats.core.client.config.AtsBulkLoad;
import org.eclipse.osee.ats.core.client.util.AtsUtilClient;
import org.eclipse.osee.ats.help.ui.AtsHelpContext;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.search.AtsQuickSearchComposite;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.operation.OperationBuilder;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.util.HelpUtil;
import org.eclipse.osee.framework.ui.plugin.xnavigate.IXNavigateEventListener;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateEventManager;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.skynet.OseeStatusContributionItemFactory;
import org.eclipse.osee.framework.ui.skynet.action.CollapseAllAction;
import org.eclipse.osee.framework.ui.skynet.action.ExpandAllAction;
import org.eclipse.osee.framework.ui.skynet.notify.OseeNotificationManager;
import org.eclipse.osee.framework.ui.skynet.util.DbConnectionExceptionComposite;
import org.eclipse.osee.framework.ui.skynet.util.LoadingComposite;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Donald G. Dunne
 */
public class NavigateView extends ViewPart implements IXNavigateEventListener {

   public static final String VIEW_ID = "org.eclipse.osee.ats.navigate.NavigateView";
   private static final String INPUT = "filter";
   private static final String FILTER_STR = "filterStr";

   private String savedFilterStr;
   private AtsNavigateComposite xNavComp;
   private Composite parent;
   private LoadingComposite loadingComposite;

   @Override
   public void createPartControl(Composite parent) {
      this.parent = parent;
      loadingComposite = new LoadingComposite(parent);
      refreshData();
   }

   public void refreshData() {
      OperationBuilder builder = Operations.createBuilder("Load ATS Navigator");
      builder.addAll(AtsBulkLoad.getConfigLoadingOperations());
      builder.addOp(new AtsNavigateViewItemsOperation());
      Operations.executeAsJob(builder.build(), false, Job.LONG, new ReloadJobChangeAdapter(this));
   }

   private final class ReloadJobChangeAdapter extends JobChangeAdapter {

      private final NavigateView navView;

      private ReloadJobChangeAdapter(NavigateView navView) {
         this.navView = navView;
      }

      @Override
      public void done(IJobChangeEvent event) {
         Job job = new UIJob("Load ATS Navigator") {

            private Label userLabel;

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
               try {
                  showBusy(false);
                  if (Widgets.isAccessible(loadingComposite)) {
                     loadingComposite.dispose();
                  } else if (Widgets.isAccessible(xNavComp)) {
                     getViewSite().getActionBars().getToolBarManager().removeAll();
                     xNavComp.dispose();
                  }

                  if (!DbConnectionExceptionComposite.dbConnectionIsOk(parent)) {
                     parent.getParent().layout(true);
                     parent.layout(true);

                     return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Osee Services are NOT available");
                  } else {

                     if (Widgets.isAccessible(parent)) {

                        xNavComp = new AtsNavigateComposite(AtsNavigateViewItems.getInstance(), parent, SWT.NONE);

                        XNavigateEventManager.register(navView);
                        HelpUtil.setHelp(xNavComp, AtsHelpContext.NAVIGATOR);
                        createToolBar();

                        // add search text box
                        AtsQuickSearchComposite composite = new AtsQuickSearchComposite(xNavComp, SWT.NONE);
                        composite.addDisposeListener(new DisposeListener() {

                           @Override
                           public void widgetDisposed(DisposeEvent e) {
                              OseeNotificationManager.getInstance().sendNotifications();
                           }
                        });

                        userLabel = new Label(xNavComp, SWT.None);
                        userLabel.addListener(SWT.MouseDoubleClick, new Listener() {
                           @Override
                           public void handleEvent(Event event) {
                              ToggleAtsAdmin.run();
                           }
                        });
                        refreshUserLabel();

                        GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
                        gridData.heightHint = 15;
                        userLabel.setLayoutData(gridData);

                        if (savedFilterStr != null) {
                           xNavComp.getFilteredTree().getFilterControl().setText(savedFilterStr);
                        }
                        xNavComp.refresh();
                        xNavComp.getFilteredTree().getFilterControl().setFocus();

                        parent.getParent().layout(true);
                        parent.layout(true);

                        OseeStatusContributionItemFactory.addTo(navView, false);
                        addExtensionPointListenerBecauseOfWorkspaceLoading();
                     }
                  }
               } catch (Exception ex) {
                  OseeLog.log(Activator.class, Level.SEVERE, ex);
               }
               return Status.OK_STATUS;
            }

            public void refreshUserLabel() {
               String str = getWhoAmI();
               if (AtsUtilClient.isAtsAdmin()) {
                  str += " - Admin";
               }
               if (!str.equals("")) {
                  if (AtsUtilClient.isAtsAdmin()) {
                     userLabel.setForeground(Displays.getSystemColor(SWT.COLOR_RED));
                  } else {
                     userLabel.setForeground(Displays.getSystemColor(SWT.COLOR_BLUE));
                  }
               }
               userLabel.setText(str);
               userLabel.setToolTipText(str);
            }
         };
         Operations.scheduleJob(job, false, Job.SHORT, null);
      }
   }

   @Override
   public void refresh(XNavigateItem item) {
      if (xNavComp != null && Widgets.isAccessible(xNavComp.getFilteredTree()) && Widgets.isAccessible(xNavComp.getFilteredTree().getViewer().getTree())) {
         xNavComp.getFilteredTree().getViewer().refresh(item);
      }
   }

   private void addExtensionPointListenerBecauseOfWorkspaceLoading() {
      IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
      extensionRegistry.addListener(new IRegistryEventListener() {
         @Override
         public void added(IExtension[] extensions) {
            xNavComp.refresh();
         }

         @Override
         public void added(IExtensionPoint[] extensionPoints) {
            xNavComp.refresh();
         }

         @Override
         public void removed(IExtension[] extensions) {
            xNavComp.refresh();
         }

         @Override
         public void removed(IExtensionPoint[] extensionPoints) {
            xNavComp.refresh();
         }
      }, "org.eclipse.osee.framework.ui.skynet.BlamOperation");
   }

   private String getWhoAmI() {
      try {
         String userName = AtsClientService.get().getUserAdmin().getCurrentUser().getName();
         return String.format("%s - %s:%s", userName, ClientSessionManager.getDataStoreName(),
            ClientSessionManager.getDataStoreLoginName());
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return "Exception: " + ex.getLocalizedMessage();
      }
   }

   protected void createToolBar() {
      IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
      toolbarManager.add(new MyWorldAction());
      toolbarManager.add(new MyFavoritesAction());
      toolbarManager.add(new CollapseAllAction(xNavComp.getFilteredTree().getViewer()));
      toolbarManager.add(new ExpandAllAction(xNavComp.getFilteredTree().getViewer()));
      toolbarManager.add(new OpenChangeReportByIdAction());
      toolbarManager.add(new OpenWorldByIdAction());
      toolbarManager.add(new OpenWorkflowByIdAction());
      toolbarManager.add(new NewAction());
      getViewSite().getActionBars().updateActionBars();

      IActionBars bars = getViewSite().getActionBars();
      IMenuManager mm = bars.getMenuManager();
      mm.add(new NewAction());
      mm.add(new NewGoal());

      toolbarManager.update(true);
   }

   public static NavigateView getNavigateView() {
      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      try {
         return (NavigateView) page.showView(NavigateView.VIEW_ID);
      } catch (PartInitException e1) {
         MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Launch Error",
            "Couldn't Launch OSEE ATS NavigateView " + e1.getMessage());
      }
      return null;
   }

   @Override
   public void saveState(IMemento memento) {
      super.saveState(memento);
      memento = memento.createChild(INPUT);

      if (xNavComp != null && xNavComp.getFilteredTree().getFilterControl() != null && !xNavComp.getFilteredTree().isDisposed()) {
         String filterStr = xNavComp.getFilteredTree().getFilterControl().getText();
         memento.putString(FILTER_STR, filterStr);
      }
   }

   @Override
   public void init(IViewSite site, IMemento memento) throws PartInitException {
      super.init(site, memento);

      // set the context (org.eclipse.ui.contexts) to be osee to make the osee hotkeys available
      IContextService contextService = (IContextService) getSite().getService(IContextService.class);
      contextService.activateContext("org.eclipse.osee.contexts.window");

      try {
         if (memento != null) {
            memento = memento.getChild(INPUT);
            if (memento != null) {
               savedFilterStr = memento.getString(FILTER_STR);
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.WARNING, "NavigateView error on init", ex);
      }
   }

   @Override
   public void setFocus() {
      if (loadingComposite != null && !loadingComposite.isDisposed()) {
         loadingComposite.setFocus();
      }
   }

}