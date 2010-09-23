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
package org.eclipse.osee.coverage.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.osee.coverage.event.CoverageEventManager;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.model.CoveragePackage;
import org.eclipse.osee.coverage.model.CoveragePackageBase;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.coverage.store.OseeCoveragePackageStore;
import org.eclipse.osee.coverage.util.CoverageImage;
import org.eclipse.osee.coverage.util.CoverageUtil;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.IActionable;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;
import org.eclipse.osee.framework.skynet.core.utility.ElapsedTime;
import org.eclipse.osee.framework.ui.plugin.OseeUiActions;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.OseeStatusContributionItemFactory;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.part.MultiPageEditorPart;

/**
 * @author Donald G. Dunne
 */
public class CoverageEditor extends FormEditor implements IActionable {
   public static final String EDITOR_ID = "org.eclipse.osee.coverage.editor.CoverageEditor";
   private Integer startPage = null;
   private CoverageEditorImportTab coverageEditorImportTab = null;
   private CoverageEditorCoverageTab coverageEditorCoverageTab = null;
   private CoverageEditorOverviewTab coverageEditorOverviewTab = null;
   private CoverageEditorLoadingTab coverageEditorLoadingTab = null;
   private CoverageEditorWorkProductTab coverageEditorWorkProductTab = null;

   @Override
   protected void addPages() {
      try {
         OseeStatusContributionItemFactory.addTo(this, true);
         String loadingStr = "Loading Coverage Package \"" + getCoverageEditorInput().getPreLoadName() + "\" ...";
         coverageEditorLoadingTab = new CoverageEditorLoadingTab(loadingStr, this);
         addFormPage(coverageEditorLoadingTab);
         setPartName("Loading " + getCoverageEditorInput().getPreLoadName());
         setTitleImage(ImageManager.getImage(CoverageImage.COVERAGE));
         setActivePage(startPage);
         if (getCoverageEditorInput().isInTest()) {
            new LoadCoverage(loadingStr).doWork(null);
         } else {
            Operations.executeAsJob(new LoadCoverage(loadingStr), true);
         }
         CoverageEventManager.getInstance().register(this);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   private class LoadCoverage extends AbstractOperation {

      public LoadCoverage(String operationName) {
         super(operationName, Activator.PLUGIN_ID);
      }

      @Override
      protected void doWork(IProgressMonitor monitor) throws Exception {
         @SuppressWarnings("unused")
         Collection<Artifact> artifactLoadCache = null;
         if (getCoverageEditorInput().getCoveragePackageArtifact() != null) {
            try {
               ElapsedTime elapsedTime = new ElapsedTime("Coverage - bulk load");
               artifactLoadCache =
                  RelationManager.getRelatedArtifacts(
                     Collections.singleton(getCoverageEditorInput().getCoveragePackageArtifact()), 8,
                     CoreRelationTypes.Default_Hierarchical__Child);
               // TODO Need to bulk load binary attributes also; Some Coverage Items are binary attributes
               // that are not bulk loaded with attributes.  This was mitigated by moving test units to seperate table
               // and only referencing their ids in Coverage Items.
               elapsedTime.end();
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
         if (getCoverageEditorInput().getCoveragePackageArtifact() != null) {
            ElapsedTime elapsedTime = new ElapsedTime("Coverage - load model");
            CoveragePackage coveragePackage =
               OseeCoveragePackageStore.get(getCoverageEditorInput().getCoveragePackageArtifact());
            getCoverageEditorInput().setCoveragePackageBase(coveragePackage);
            elapsedTime.end();
         }

         if (getCoverageEditorInput().isInTest()) {
            addPagesAfterLoad();
         } else {
            Displays.ensureInDisplayThread(new Runnable() {
               @Override
               public void run() {
                  addPagesAfterLoad();
               }
            });
         }
      }
   };

   protected void addPagesAfterLoad() {
      try {
         // remove loading page
         removePage(0);
         coverageEditorOverviewTab = new CoverageEditorOverviewTab("Overview", this, getCoveragePackageBase());
         addFormPage(coverageEditorOverviewTab);
         coverageEditorCoverageTab = new CoverageEditorCoverageTab("Coverage Items", this, getCoveragePackageBase());
         addFormPage(coverageEditorCoverageTab);
         if (getCoveragePackageBase() instanceof CoveragePackage) {
            coverageEditorWorkProductTab =
               new CoverageEditorWorkProductTab("Work Product Tracking", this,
                  (CoveragePackage) getCoveragePackageBase());
         }
         addFormPage(coverageEditorWorkProductTab);
         if (getCoveragePackageBase().isImportAllowed()) {
            coverageEditorImportTab = new CoverageEditorImportTab(this);
            addFormPage(coverageEditorImportTab);
         }
         setPartName(getCoveragePackageBase().getName());
         setTitleImage(ImageManager.getImage(CoverageUtil.getCoveragePackageBaseImage(getCoveragePackageBase())));

         setActivePage(startPage);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   public void simulateImport(String importName) throws OseeCoreException {
      if (coverageEditorImportTab == null) {
         throw new OseeStateException("Import page == null");
      }
      setActivePage(CoverageEditorImportTab.PAGE_ID);
      coverageEditorImportTab.simulateImport(importName);
   }

   public void simulateImportPostRun() throws OseeCoreException {
      setActivePage(CoverageEditorMergeTab.PAGE_ID);
      coverageEditorImportTab.simulateImportSearch();
   }

   public int addFormPage(FormPage page) {
      int pageIndex = 0;
      try {
         pageIndex = addPage(page);
         if (startPage == null) {
            startPage = pageIndex;
         }
      } catch (PartInitException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return pageIndex;
   }

   public static void addToToolBar(IToolBarManager manager, CoverageEditor coverageEditor) {
      manager.add(OseeUiActions.createBugAction(SkynetGuiPlugin.getInstance(), coverageEditor, EDITOR_ID,
         "Lba Code Promote"));
      manager.update(true);
   }

   public void setEditorTitle(final String str) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            setPartName(str);
            firePropertyChange(IWorkbenchPart.PROP_TITLE);
         }
      });
   }

   public CoveragePackageBase getCoveragePackageBase() throws OseeCoreException {
      return getCoverageEditorInput().getCoveragePackageBase();
   }

   public CoverageEditorInput getCoverageEditorInput() throws OseeCoreException {
      IEditorInput editorInput = getEditorInput();
      if (!(editorInput instanceof CoverageEditorInput)) {
         throw new OseeArgumentException("Editor Input not CoverageEditorInput");
      }
      return (CoverageEditorInput) getEditorInput();
   }

   @Override
   public void doSave(IProgressMonitor monitor) {
      // do nothing
   }

   @Override
   public boolean isSaveOnCloseNeeded() {
      return isDirty();
   }

   public void refreshTitle() {
      firePropertyChange(IWorkbenchPart.PROP_TITLE);
   }

   @Override
   public void dispose() {
      CoverageEventManager.getInstance().unregister(this);
      super.dispose();
   }

   @Override
   public boolean isDirty() {
      return false;
   }

   public static void open(final CoverageEditorInput coverageEditorInput) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            IWorkbenchPage page = AWorkbench.getActivePage();
            try {
               page.openEditor(coverageEditorInput, EDITOR_ID);
            } catch (PartInitException ex) {
               OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      });
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

   public static Collection<CoverageEditor> getEditors() {
      final List<CoverageEditor> editors = new ArrayList<CoverageEditor>();
      Displays.pendInDisplayThread(new Runnable() {
         @Override
         public void run() {
            for (IEditorReference editor : AWorkbench.getEditors(EDITOR_ID)) {
               editors.add((CoverageEditor) editor.getEditor(false));
            }
         }
      });
      return editors;
   }

   public void refresh(ICoverage coverage) {
      coverageEditorCoverageTab.refresh(coverage);
      coverageEditorOverviewTab.refreshActionHandler();
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

   @SuppressWarnings("rawtypes")
   @Override
   public Object getAdapter(Class adapter) {
      if (IActionable.class.equals(adapter)) {
         return new IActionable() {
            @Override
            public String getActionDescription() {
               return "";
            }
         };
      }
      return super.getAdapter(adapter);
   }

   @Override
   public String getActionDescription() {
      return null;
   }

   @Override
   public void doSaveAs() {
      // do nothing
   }

   @Override
   public boolean isSaveAsAllowed() {
      return false;
   }

   public CoverageEditorImportTab getCoverageEditorImportTab() {
      return coverageEditorImportTab;
   }

   public CoverageEditorOverviewTab getCoverageEditorOverviewTab() {
      return coverageEditorOverviewTab;
   }

   public Branch getBranch() throws OseeCoreException {
      Artifact artifact = getCoverageEditorInput().getCoveragePackageArtifact();
      if (artifact != null) {
         return artifact.getBranch();
      }
      return null;
   }
}
