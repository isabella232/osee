/*******************************************************************************
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

package org.eclipse.osee.framework.ui.skynet.widgets.xHistory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.xviewer.customize.XViewerCustomMenu;
import org.eclipse.osee.framework.access.AccessControlManager;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.help.ui.OseeHelpContext;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.change.AttributeChange;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.event.listener.IBranchEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEventType;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.HelpUtil;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.OpenContributionItem;
import org.eclipse.osee.framework.ui.skynet.OseeStatusContributionItemFactory;
import org.eclipse.osee.framework.ui.skynet.action.EditTransactionComment;
import org.eclipse.osee.framework.ui.skynet.action.ITransactionRecordSelectionProvider;
import org.eclipse.osee.framework.ui.skynet.action.WasIsCompareEditorAction;
import org.eclipse.osee.framework.ui.skynet.change.ChangeUiUtil;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.listener.IRebuildMenuListener;
import org.eclipse.osee.framework.ui.skynet.menu.CompareArtifactAction;
import org.eclipse.osee.framework.ui.skynet.util.SkynetViews;
import org.eclipse.osee.framework.ui.skynet.widgets.GenericViewPart;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

/**
 * Displays persisted changes made to an artifact.
 * 
 * @author Jeff C. Phillips
 */
public class HistoryView extends GenericViewPart implements IBranchEventListener, ITransactionRecordSelectionProvider, IRebuildMenuListener {

   public static final String VIEW_ID = "org.eclipse.osee.framework.ui.skynet.widgets.xHistory.HistoryView";
   private XHistoryWidget xHistoryWidget;
   private Artifact artifact;

   public static void open(Artifact artifact) throws OseeArgumentException {
      if (artifact == null) {
         throw new OseeArgumentException("Artifact can't be null");
      }
      HistoryView.openViewUpon(artifact, true);
   }

   private static void openViewUpon(final Artifact artifact, final Boolean loadHistory) {
      Job job = new Job("Open History: " + artifact.getName()) {

         @Override
         protected IStatus run(final IProgressMonitor monitor) {
            Displays.ensureInDisplayThread(new Runnable() {
               @Override
               public void run() {
                  try {
                     IWorkbenchPage page = AWorkbench.getActivePage();
                     HistoryView historyView =
                        (HistoryView) page.showView(VIEW_ID, artifact.getGuid() + artifact.getBranch().getGuid(),
                           IWorkbenchPage.VIEW_ACTIVATE);

                     historyView.explore(artifact, loadHistory);
                  } catch (Exception ex) {
                     OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
                  }
               }
            });
            monitor.done();
            return Status.OK_STATUS;
         }
      };

      Jobs.startJob(job);
   }

   /**
    * @see IWorkbenchPart#createPartControl(Composite)
    */
   @Override
   public void createPartControl(Composite parent) {
      /*
       * Create a grid layout object so the text and treeviewer are layed out the way I want.
       */
      GridLayout layout = new GridLayout();
      layout.numColumns = 1;
      layout.verticalSpacing = 0;
      layout.marginWidth = 0;
      layout.marginHeight = 0;
      parent.setLayout(layout);
      parent.setLayoutData(new GridData(GridData.FILL_BOTH));

      xHistoryWidget = new XHistoryWidget(this);
      xHistoryWidget.setDisplayLabel(false);
      xHistoryWidget.createWidgets(parent, 1);

      MenuManager menuManager = new MenuManager();
      menuManager.setRemoveAllWhenShown(true);
      menuManager.addMenuListener(new IMenuListener() {
         @Override
         public void menuAboutToShow(IMenuManager manager) {
            MenuManager menuManager = (MenuManager) manager;
            menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
         }
      });

      menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

      xHistoryWidget.getXViewer().getTree().setMenu(
         menuManager.createContextMenu(xHistoryWidget.getXViewer().getTree()));

      getSite().registerContextMenu(VIEW_ID, menuManager, xHistoryWidget.getXViewer());
      getSite().setSelectionProvider(xHistoryWidget.getXViewer());

      HelpUtil.setHelp(parent, OseeHelpContext.HISTORY_VIEW);

      OseeStatusContributionItemFactory.addTo(this, true);

      setupMenus();

      setFocusWidget(xHistoryWidget.getXViewer().getControl());
   }

   private void setupMenus() {
      Menu popupMenu = new Menu(xHistoryWidget.getXViewer().getTree().getParent());

      OpenContributionItem contributionItem = new OpenContributionItem(getClass().getSimpleName() + ".open");
      contributionItem.fill(popupMenu, -1);
      new MenuItem(popupMenu, SWT.SEPARATOR);

      createChangeReportMenuItem(popupMenu);

      new MenuItem(popupMenu, SWT.SEPARATOR);
      createReplaceAttributeWithVersionMenuItem(popupMenu);

      IAction action = new CompareArtifactAction("Compare two Artifacts", xHistoryWidget.getXViewer());
      (new ActionContributionItem(action)).fill(popupMenu, 3);

      (new ActionContributionItem(new EditTransactionComment(this))).fill(popupMenu, 3);
      (new ActionContributionItem(new WasIsCompareEditorAction())).fill(popupMenu, 3);

      // Setup generic xviewer menu items
      XViewerCustomMenu xMenu = new XViewerCustomMenu(xHistoryWidget.getXViewer());
      new MenuItem(popupMenu, SWT.SEPARATOR);
      xMenu.createTableCustomizationMenuItem(popupMenu);
      xMenu.createViewTableReportMenuItem(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      xMenu.addCopyViewMenuBlock(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      xMenu.addFilterMenuBlock(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      xHistoryWidget.getXViewer().getTree().setMenu(popupMenu);
   }

   private void createReplaceAttributeWithVersionMenuItem(Menu popupMenu) {
      final MenuItem replaceWithMenu = new MenuItem(popupMenu, SWT.CASCADE);
      replaceWithMenu.setText("&Replace Attribute with Version");
      try {
         replaceWithMenu.setEnabled(AccessControlManager.isOseeAdmin());
      } catch (Exception ex) {
         replaceWithMenu.setEnabled(false);
      }
      popupMenu.addMenuListener(new MenuAdapter() {

         @Override
         public void menuShown(MenuEvent e) {
            List<?> selections = ((IStructuredSelection) xHistoryWidget.getXViewer().getSelection()).toList();
            replaceWithMenu.setEnabled(selections.size() == 1 && selections.iterator().next() instanceof AttributeChange);
         }

      });

      replaceWithMenu.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) xHistoryWidget.getXViewer().getSelection();
            Object selectedObject = selection.getFirstElement();

            if (selectedObject instanceof AttributeChange) {
               try {
                  AttributeChange attributeChange = (AttributeChange) selectedObject;
                  Artifact artifact =
                     ArtifactQuery.getArtifactFromId(attributeChange.getArtId(), attributeChange.getBranch());

                  for (Attribute<?> attribute : artifact.getAttributes(attributeChange.getAttributeType())) {
                     if (attribute.getId() == attributeChange.getAttrId()) {
                        attribute.replaceWithVersion((int) attributeChange.getGamma());
                        break;
                     }
                  }

                  artifact.persist("Replace attribute with version");
                  artifact.reloadAttributesAndRelations();

               } catch (OseeCoreException ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         }

      });
   }

   private void createChangeReportMenuItem(Menu popupMenu) {
      final MenuItem changeReportMenuItem = new MenuItem(popupMenu, SWT.CASCADE);
      changeReportMenuItem.setText("&Change Report");
      changeReportMenuItem.setImage(ImageManager.getImage(FrameworkImage.BRANCH_CHANGE));
      popupMenu.addMenuListener(new MenuAdapter() {

         @Override
         public void menuShown(MenuEvent e) {
            List<?> selections = ((IStructuredSelection) xHistoryWidget.getXViewer().getSelection()).toList();
            changeReportMenuItem.setEnabled(selections.size() == 1 && ((Change) selections.iterator().next()).getTxDelta().getStartTx().getTxType() != TransactionDetailsType.Baselined);
         }

      });

      changeReportMenuItem.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) xHistoryWidget.getXViewer().getSelection();
            Object selectedObject = selection.getFirstElement();

            if (selectedObject instanceof Change) {
               try {
                  ChangeUiUtil.open(((Change) selectedObject).getTxDelta().getStartTx());
               } catch (OseeCoreException ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         }

      });
   }

   private void explore(final Artifact artifact, boolean loadHistory) {
      if (xHistoryWidget != null) {
         this.artifact = artifact;

         setPartName("History: " + artifact.getName());
         xHistoryWidget.setInputData(artifact, loadHistory);
      }
   }

   private static final String INPUT = "input";
   private static final String ART_GUID = "artifactGuid";
   private static final String BRANCH_ID = "branchId";

   @Override
   public void saveState(IMemento memento) {
      super.saveState(memento);
      memento = memento.createChild(INPUT);
      if (artifact != null) {
         memento.putString(ART_GUID, artifact.getGuid());
         memento.putString(BRANCH_ID, artifact.getBranch().getGuid());
         SkynetViews.addDatabaseSourceId(memento);
      }
   }

   @Override
   public void init(IViewSite site, IMemento memento) throws PartInitException {
      super.init(site, memento);
      try {
         if (memento != null) {
            memento = memento.getChild(INPUT);
            if (memento != null) {
               if (SkynetViews.isSourceValid(memento)) {
                  String guid = memento.getString(ART_GUID);
                  String branchId = memento.getString(BRANCH_ID);
                  Artifact artifact = ArtifactQuery.getArtifactFromId(guid, BranchManager.getBranchByGuid(branchId));
                  openViewUpon(artifact, false);
               } else {
                  closeView();
               }
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.WARNING, "History View error on init", ex);
      }
   }

   private void handleBranchEvent(BranchEventType branchModType) {
      if (branchModType == BranchEventType.Deleting || branchModType == BranchEventType.Deleted || branchModType == BranchEventType.Purging || branchModType == BranchEventType.Purged) {
         Displays.ensureInDisplayThread(new Runnable() {
            @Override
            public void run() {
               closeView();
            }
         });
         return;
      } else if (branchModType == BranchEventType.Committed) {
         Displays.ensureInDisplayThread(new Runnable() {
            @Override
            public void run() {
               try {
                  explore(artifact, true);
               } catch (Exception ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         });
         // refresh view with new branch and transaction id
      }
   }

   private void closeView() {
      SkynetViews.closeView(VIEW_ID, getViewSite().getSecondaryId());
   }

   @Override
   public void rebuildMenu() {
      setupMenus();
   }

   @Override
   public void handleBranchEvent(Sender sender, BranchEvent branchEvent) {
      handleBranchEvent(branchEvent.getEventType());
   }

   @Override
   public List<? extends IEventFilter> getEventFilters() {
      if (artifact != null) {
         return OseeEventManager.getEventFiltersForBranch(artifact.getBranch());
      }
      return null;
   }

   @Override
   public ArrayList<TransactionRecord> getSelectedTransactionRecords() {
      return xHistoryWidget.getSelectedTransactionRecords();
   }

   @Override
   public void refreshUI(ArrayList<TransactionRecord> records) {
      setPartName("History: " + artifact.getName());
      xHistoryWidget.refresh();
   }

   public void refreshTitle() {
      setPartName("History: " + artifact.getName());
   }
}
