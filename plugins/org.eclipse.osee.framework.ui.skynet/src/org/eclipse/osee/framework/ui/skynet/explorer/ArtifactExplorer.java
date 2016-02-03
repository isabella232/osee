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

package org.eclipse.osee.framework.ui.skynet.explorer;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osee.framework.access.AccessControlManager;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.help.ui.OseeHelpContext;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.OseeSystemArtifacts;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.IBranchProvider;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.event.listener.IAccessControlEventListener;
import org.eclipse.osee.framework.skynet.core.event.listener.IBranchEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.AccessControlEvent;
import org.eclipse.osee.framework.skynet.core.event.model.AccessControlEventType;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEventType;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.HelpUtil;
import org.eclipse.osee.framework.ui.plugin.util.SelectionCountChangeListener;
import org.eclipse.osee.framework.ui.skynet.ArtifactContentProvider;
import org.eclipse.osee.framework.ui.skynet.ArtifactDecorator;
import org.eclipse.osee.framework.ui.skynet.ArtifactDoubleClick;
import org.eclipse.osee.framework.ui.skynet.ArtifactLabelProvider;
import org.eclipse.osee.framework.ui.skynet.ArtifactStructuredSelection;
import org.eclipse.osee.framework.ui.skynet.IArtifactExplorerEventHandler;
import org.eclipse.osee.framework.ui.skynet.OseeStatusContributionItemFactory;
import org.eclipse.osee.framework.ui.skynet.explorer.menu.ArtifactExplorerMenu;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.listener.IRebuildMenuListener;
import org.eclipse.osee.framework.ui.skynet.menu.ArtifactTreeViewerGlobalMenuHelper;
import org.eclipse.osee.framework.ui.skynet.menu.IGlobalMenuHelper;
import org.eclipse.osee.framework.ui.skynet.util.DbConnectionExceptionComposite;
import org.eclipse.osee.framework.ui.skynet.util.SkynetViews;
import org.eclipse.osee.framework.ui.skynet.widgets.GenericViewPart;
import org.eclipse.osee.framework.ui.skynet.widgets.XBranchSelectWidget;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.TreeViewerUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Ryan D. Brooks
 */
public class ArtifactExplorer extends GenericViewPart implements IArtifactExplorerEventHandler, IRebuildMenuListener, IAccessControlEventListener, IBranchEventListener, ISelectionProvider, IBranchProvider {
   public static final String VIEW_ID = "org.eclipse.osee.framework.ui.skynet.ArtifactExplorer";
   private static final String ROOT_UUID = "artifact.explorer.last.root_uuid";
   private static final String ROOT_BRANCH = "artifact.explorer.last.root_branch";

   private TreeViewer treeViewer;
   private Artifact explorerRoot;
   private TreeEditor myTreeEditor;
   private XBranchSelectWidget branchSelect;
   private Branch branch;
   private IGlobalMenuHelper globalMenuHelper;

   private ArtifactExplorerDragAndDrop dragAndDropWorker;

   private Composite stackComposite;
   private BranchWarningComposite branchWarningComposite;
   private StackLayout stackLayout;
   private ArtifactDecorator artifactDecorator;
   public ArtifactExplorerMenu artifactExplorerMenu;
   private ArtifactExplorerToolbar artifactExplorerToolbar;

   public static void explore(Collection<Artifact> artifacts) {
      explore(artifacts, AWorkbench.getActivePage());
   }

   private void setErrorString(String str) {
      branchWarningComposite.updateLabel(str);
      stackLayout.topControl = branchWarningComposite;
      stackComposite.layout();
      stackComposite.getParent().layout();
   }

   public static void explore(Collection<Artifact> artifacts, IWorkbenchPage page) {
      Artifact sampleArtifact = null;
      IOseeBranch inputBranch = null;
      if (artifacts != null && !artifacts.isEmpty()) {
         sampleArtifact = artifacts.iterator().next();
         inputBranch = sampleArtifact.getBranch();
      }
      ArtifactExplorer artifactExplorer = ArtifactExplorerUtil.findView(inputBranch, page);

      artifactExplorer.setPartName("Artifacts");
      artifactExplorer.setContentDescription("These artifacts could not be handled");
      artifactExplorer.treeViewer.setInput(artifacts);
      artifactExplorer.initializeSelectionBox();
   }

   @Override
   public void createPartControl(Composite parent) {
      try {
         if (DbConnectionExceptionComposite.dbConnectionIsOk(parent)) {

            // TODO: Trigger User Loading to prevent lock up -- Need to remove this once service based
            UserManager.getUser();
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
            gridData.heightHint = 1000;
            gridData.widthHint = 1000;

            parent.setLayout(new GridLayout(1, false));
            parent.setLayoutData(gridData);

            branchSelect = new XBranchSelectWidget("");
            branchSelect.setDisplayLabel(false);
            branchSelect.setSelection(branch);
            branchSelect.createWidgets(parent, 1);

            branchSelect.addListener(new Listener() {
               @Override
               public void handleEvent(Event event) {
                  try {
                     IOseeBranch selectedBranch = branchSelect.getData();
                     if (selectedBranch != null) {
                        branch = BranchManager.getBranch(selectedBranch);
                        dragAndDropWorker.updateBranch(selectedBranch);
                        explore(OseeSystemArtifacts.getDefaultHierarchyRootArtifact(branch));
                     }
                  } catch (Exception ex) {
                     setErrorString("Error loading branch (see error log for details): " + ex.getLocalizedMessage());
                     OseeLog.log(getClass(), Level.SEVERE, ex);
                  }
               }

            });

            stackComposite = new Composite(parent, SWT.NONE);
            stackLayout = new StackLayout();
            stackComposite.setLayout(stackLayout);
            stackComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            branchWarningComposite = new BranchWarningComposite(stackComposite);

            treeViewer = new TreeViewer(stackComposite);
            Tree tree = treeViewer.getTree();
            final ArtifactExplorer fArtExplorere = this;
            tree.addDisposeListener(new DisposeListener() {

               @Override
               public void widgetDisposed(DisposeEvent e) {
                  ArtifactExplorerEventManager.remove(fArtExplorere);
               }
            });
            artifactDecorator = new ArtifactDecorator(Activator.ARTIFACT_EXPLORER_ATTRIBUTES_PREF);

            treeViewer.setContentProvider(new ArtifactContentProvider(artifactDecorator));
            treeViewer.setLabelProvider(new ArtifactLabelProvider(artifactDecorator));
            treeViewer.addDoubleClickListener(new ArtifactDoubleClick());
            treeViewer.getControl().setLayoutData(gridData);
            treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

               @Override
               public void selectionChanged(SelectionChangedEvent event) {
                  if (treeViewer != null) {
                     Tree viewer = treeViewer.getTree();
                     if (viewer != null && !viewer.isDisposed()) {
                        viewer.redraw();
                     }
                     Control control = treeViewer.getControl();
                     if (control != null && !control.isDisposed()) {
                        control.redraw();
                     }
                  }
               }

            });

            /**
             * We can not use the hash lookup because an artifact may not have a good equals. This can be added back
             * once the content provider is converted over to use job node.
             */
            treeViewer.setUseHashlookup(false);

            treeViewer.addSelectionChangedListener(new SelectionCountChangeListener(getViewSite()));
            globalMenuHelper = new ArtifactTreeViewerGlobalMenuHelper(treeViewer);

            artifactExplorerToolbar = new ArtifactExplorerToolbar(this);
            artifactExplorerToolbar.createToolbar();

            artifactDecorator.setViewer(treeViewer);
            artifactDecorator.addActions(getViewSite().getActionBars().getMenuManager(), this);

            getSite().setSelectionProvider(treeViewer);
            addExploreSelection();

            artifactExplorerMenu = new ArtifactExplorerMenu(this);
            artifactExplorerMenu.create();
            artifactExplorerMenu.setupPopupMenu();

            myTreeEditor = new TreeEditor(getTreeViewer().getTree());
            myTreeEditor.horizontalAlignment = SWT.LEFT;
            myTreeEditor.grabHorizontal = true;
            myTreeEditor.minimumWidth = 50;

            dragAndDropWorker = new ArtifactExplorerDragAndDrop(treeViewer, VIEW_ID, this, branch);

            OseeStatusContributionItemFactory.addTo(this, false);

            artifactExplorerToolbar.updateEnablement();
            HelpUtil.setHelp(treeViewer.getControl(), OseeHelpContext.ARTIFACT_EXPLORER);

            refreshBranchWarning();

            getViewSite().getActionBars().updateActionBars();
            setFocusWidget(treeViewer.getControl());

            OseeEventManager.addListener(this);
            ArtifactExplorerEventManager.add(this);
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }

   }

   public void refreshBranchWarning() {
      ArtifactExplorerUtil.refreshBranchWarning(this, treeViewer, globalMenuHelper, getBranch(),
         branchWarningComposite);
   }

   /**
    * Reveal an artifact in the viewer and select it.
    */
   public static void exploreBranch(IOseeBranch branch) {
      if (branch != null) {
         IWorkbenchPage page = AWorkbench.getActivePage();
         ArtifactExplorerUtil.findView(branch, page);
      }
   }

   public void explore(Artifact artifact) throws OseeCoreException {
      if (artifact == null) {
         throw new IllegalArgumentException("Can not explore a null artifact.");
      }

      setPartName("Artifact Explorer: " + artifact.getFullBranch().getShortName());
      if (branch != null && branch != artifact.getBranch()) {
         explore(Arrays.asList(artifact));
         return;
      }

      explorerRoot = artifact;
      branch = artifact.getFullBranch();

      if (dragAndDropWorker != null) {
         dragAndDropWorker.updateBranch(branch);
      }

      refreshBranchWarning();

      initializeSelectionBox();

      if (treeViewer != null) {
         Object objects[] = treeViewer.getExpandedElements();
         treeViewer.setInput(explorerRoot);
         artifactExplorerMenu.setupPopupMenu();
         artifactExplorerToolbar.updateEnablement();
         // Attempt to re-expand what was expanded
         treeViewer.setExpandedElements(objects);
      }
   }

   public void setExpandedArtifacts(Object... artifacts) {
      if (treeViewer != null) {
         treeViewer.setExpandedElements(artifacts);
      }
   }

   /**
    * Add the selection from the define explorer
    */
   private void addExploreSelection() {
      if (explorerRoot != null) {
         try {
            treeViewer.setInput(explorerRoot);
            initializeSelectionBox();
         } catch (IllegalArgumentException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
   }

   @Override
   public void init(IViewSite site, IMemento memento) throws PartInitException {
      super.init(site, memento);
      if (DbConnectionExceptionComposite.dbConnectionIsOk()) {
         try {
            if (memento != null && memento.getString(ROOT_UUID) != null && memento.getString(ROOT_BRANCH) != null) {
               Branch branch = BranchManager.getBranch(Long.parseLong(memento.getString(ROOT_BRANCH)));

               if (!branch.getArchiveState().isArchived() || AccessControlManager.isOseeAdmin()) {
                  Artifact previousArtifact =
                     ArtifactQuery.checkArtifactFromId(Long.valueOf(memento.getString(ROOT_UUID)), branch);
                  if (previousArtifact != null) {
                     explore(previousArtifact);
                  } else {
                     /*
                      * simply means that the previous artifact that was used as the root for the artiactExplorer does
                      * not exist because it was deleted or this workspace was last used with a different branch or
                      * database, so let the logic below get the default hierarchy root artifact
                      */
                  }
                  return;
               }
            }
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
   }

   @Override
   public void saveState(IMemento memento) {
      super.saveState(memento);
      if (DbConnectionExceptionComposite.dbConnectionIsOk()) {
         if (explorerRoot != null) {
            memento.putString(ROOT_UUID, String.valueOf(explorerRoot.getUuid()));
            try {
               memento.putString(ROOT_BRANCH, String.valueOf(explorerRoot.getBranchId()));
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      }
   }

   @Override
   public void dispose() {
      OseeEventManager.removeListener(this);
      ArtifactExplorerEventManager.remove(this);
      artifactExplorerMenu.dispose();
      super.dispose();
   }

   @Override
   public void addSelectionChangedListener(ISelectionChangedListener listener) {
      treeViewer.addSelectionChangedListener(listener);
   }

   @Override
   public ArtifactStructuredSelection getSelection() {
      final List<Artifact> selectedItems = new LinkedList<>();
      TreeViewerUtility.getPreorderSelection(treeViewer, selectedItems);
      return new ArtifactStructuredSelection(selectedItems);
   }

   @Override
   public void removeSelectionChangedListener(ISelectionChangedListener listener) {
      treeViewer.removeSelectionChangedListener(listener);
   }

   @Override
   public void setSelection(ISelection selection) {
      treeViewer.setSelection(selection);
   }

   @Override
   public void handleAccessControlArtifactsEvent(Sender sender, AccessControlEvent accessControlEvent) {
      try {
         if (!accessControlEvent.isForBranch(branch)) {
            return;
         }
         if (accessControlEvent.getEventType() == AccessControlEventType.UserAuthenticated ||
         //
         accessControlEvent.getEventType() == AccessControlEventType.ArtifactsUnlocked ||
         //
         accessControlEvent.getEventType() == AccessControlEventType.ArtifactsLocked) {
            Displays.ensureInDisplayThread(new Runnable() {
               @Override
               public void run() {
                  treeViewer.refresh();
                  refreshBranchWarning();
               }
            });
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   @Override
   public void rebuildMenu() {
      artifactExplorerMenu.setupPopupMenu();
   }

   public void setBranch(Branch branch) {
      this.branch = branch;
   }

   public void initializeSelectionBox() {
      if (branch != null && branchSelect != null && !branch.equals(branchSelect.getData())) {
         branchSelect.setSelection(branch);
         refreshBranchWarning();
      }
   }

   @Override
   public IOseeBranch getBranch(IProgressMonitor monitor) {
      return branch;
   }

   public Branch getBranch() {
      return branch;
   }

   /**
    * Reveal an artifact in the viewer and select it.
    */
   public static void revealArtifact(Artifact artifact) {
      final ArtifactData data = new ArtifactData(artifact);
      IOperation operation = new CheckArtifactBeforeReveal(data);
      Operations.executeAsJob(operation, true, Job.SHORT, new JobChangeAdapter() {

         @Override
         public void done(IJobChangeEvent event) {
            IStatus status = event.getResult();
            if (status.isOK()) {
               Job uiJob = new UIJob("Reveal in Artifact Explorer") {

                  @Override
                  public IStatus runInUIThread(IProgressMonitor monitor) {
                     Artifact artifact = data.getArtifact();
                     IWorkbenchPage page = AWorkbench.getActivePage();
                     ArtifactExplorer artifactExplorer = ArtifactExplorerUtil.findView(artifact.getBranch(), page);
                     artifactExplorer.treeViewer.setSelection(new StructuredSelection(artifact), true);
                     return Status.OK_STATUS;
                  }
               };
               Jobs.startJob(uiJob);
            }
         }
      });

   }

   @Override
   public void handleBranchEvent(Sender sender, final BranchEvent branchEvent) {
      if (branch == null) {
         return;
      }
      if (branch.getUuid().equals(branchEvent.getBranchUuid())) {
         if (branchEvent.getEventType() == BranchEventType.Committing || branchEvent.getEventType() == BranchEventType.Committed) {
            SkynetViews.closeView(VIEW_ID, getViewSite().getSecondaryId());
         } else {
            refreshBranchWarning();
         }
      } else if (branch.getUuid().equals(branchEvent.getDestinationBranchUuid())) {
         if (branchEvent.getEventType() == BranchEventType.Committed) {
            Displays.ensureInDisplayThread(new Runnable() {
               @Override
               public void run() {
                  getTreeViewer().refresh();
               }
            });
         }
      }
   }

   public TreeViewer getTreeViewer() {
      return treeViewer;
   }

   public void setTreeViewer(TreeViewer treeViewer) {
      this.treeViewer = treeViewer;
   }

   @Override
   public ArtifactExplorer getArtifactExplorer() {
      return this;
   }

   @Override
   public boolean isDisposed() {
      return treeViewer.getTree() == null || treeViewer.getTree().isDisposed();
   }

   @Override
   public List<? extends IEventFilter> getEventFilters() {
      if (branch != null) {
         return OseeEventManager.getEventFiltersForBranch(branch);
      }
      return null;
   }

   public Artifact getExplorerRoot() {
      return explorerRoot;
   }

   public TreeEditor getMyTreeEditor() {
      return myTreeEditor;
   }

   public Label getBranchWarningLabel() {
      return branchWarningComposite.getBranchWarningLabel();
   }

   public StackLayout getStackLayout() {
      return stackLayout;
   }

   public Composite getStackComposite() {
      return stackComposite;
   }

}
