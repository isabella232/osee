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
package org.eclipse.osee.coverage.editor.xcover;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.xviewer.IXViewerFactory;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.coverage.action.CreateWorkProductTaskAction;
import org.eclipse.osee.coverage.action.EditAssigneesAction;
import org.eclipse.osee.coverage.action.EditCoverageMethodAction;
import org.eclipse.osee.coverage.action.EditCoverageNotesAction;
import org.eclipse.osee.coverage.action.EditRationaleAction;
import org.eclipse.osee.coverage.action.IRefreshable;
import org.eclipse.osee.coverage.action.ISelectedCoverageEditorItem;
import org.eclipse.osee.coverage.action.OpenWorkProductTaskAction;
import org.eclipse.osee.coverage.action.RemoveWorkProductTaskAction;
import org.eclipse.osee.coverage.action.ViewSourceAction;
import org.eclipse.osee.coverage.editor.xcover.XCoverageViewer.TableType;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.model.CoverageImport;
import org.eclipse.osee.coverage.model.CoverageItem;
import org.eclipse.osee.coverage.model.CoverageOptionManager;
import org.eclipse.osee.coverage.model.CoverageUnit;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.coverage.model.IWorkProductTaskProvider;
import org.eclipse.osee.coverage.util.ISaveable;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Donald G. Dunne
 */
public class CoverageXViewer extends XViewer implements ISelectedCoverageEditorItem, ISaveable, IRefreshable {

   protected final XCoverageViewer xCoverageViewer;
   Action editRationaleAction, editMethodAction, viewSourceAction, editAssigneesAction, editCoverageStatusAction,
      deleteCoverUnitAction, createWorkProductTaskAction, removeWorkProductTaskAction, openWorkProductTaskAction;

   public CoverageXViewer(Composite parent, int style, XCoverageViewer xCoverageViewer) {
      this(parent, style, new CoverageXViewerFactory(), xCoverageViewer);
   }

   public CoverageXViewer(Composite parent, int style, IXViewerFactory xViewerFactory, XCoverageViewer xCoverageViewer) {
      super(parent, style, xViewerFactory, false, false);
      this.xCoverageViewer = xCoverageViewer;
   }

   @Override
   protected void createSupportWidgets(Composite parent) {
      super.createSupportWidgets(parent);
      createMenuActions();
   }

   public void createMenuActions() {
      if (viewSourceAction == null) {
         viewSourceAction = new ViewSourceAction(this);
         editMethodAction = new EditCoverageMethodAction(this, this, this, this);
         editAssigneesAction = new EditAssigneesAction(this, this, this);
         editCoverageStatusAction = new EditCoverageNotesAction(this, this, this);
         editRationaleAction = new EditRationaleAction(this, this, this);
         //         deleteCoverUnitAction = new DeleteCoverUnitAction(this, this, this);
         createWorkProductTaskAction = new CreateWorkProductTaskAction(this, this, this);
         removeWorkProductTaskAction = new RemoveWorkProductTaskAction(this, this, this);
         openWorkProductTaskAction = new OpenWorkProductTaskAction(this);
      }
   }

   public CoverageOptionManager getCoverageOptionManager() {
      return xCoverageViewer.getCoverageOptionManager();
   }

   private boolean isEditRationaleEnabled() {
      if (xCoverageViewer.getSelectedCoverageItems().isEmpty()) {
         return false;
      }
      for (ICoverage item : xCoverageViewer.getSelectedCoverageItems()) {
         if (!(item instanceof CoverageItem)) {
            return false;
         }
      }
      return true;
   }

   private boolean isEditMethodEnabled() {
      if (xCoverageViewer.getSelectedCoverageItems().isEmpty()) {
         return false;
      }
      for (ICoverage item : xCoverageViewer.getSelectedCoverageItems()) {
         if (!(item instanceof CoverageItem)) {
            return false;
         }
      }
      return true;
   }

   private boolean isCreateWorkProductTaskActionEnabled() {
      if (xCoverageViewer.getSelectedCoverageItems().isEmpty()) {
         return false;
      }
      return true;
   }

   public IWorkProductTaskProvider getWorkProductTaskProvider() {
      return xCoverageViewer.getWorkProductTaskProvider();
   }

   private boolean isEditMetricsEnabled() {
      if (xCoverageViewer.getSelectedCoverageItems().isEmpty()) {
         return false;
      }
      for (ICoverage item : xCoverageViewer.getSelectedCoverageItems()) {
         if (!(item instanceof CoverageUnit)) {
            return false;
         }
      }
      return true;
   }

   public void updateEditMenuActions() {
      MenuManager mm = getMenuManager();
      // EDIT MENU BLOCK
      if (xCoverageViewer.isType(TableType.Package)) {
         mm.insertBefore(MENU_GROUP_PRE, editRationaleAction);
         editRationaleAction.setEnabled(isEditRationaleEnabled());

         mm.insertBefore(MENU_GROUP_PRE, editMethodAction);
         editMethodAction.setEnabled(isEditMethodEnabled());

         mm.insertBefore(MENU_GROUP_PRE, editAssigneesAction);
         editAssigneesAction.setEnabled(isEditMetricsEnabled());

         mm.insertBefore(MENU_GROUP_PRE, editCoverageStatusAction);
         editCoverageStatusAction.setEnabled(isEditMetricsEnabled());

         //         mm.insertBefore(MENU_GROUP_PRE, deleteCoverUnitAction);
         //         deleteCoverUnitAction.setEnabled(isDeleteCoverageUnitEnabled());

         mm.insertBefore(MENU_GROUP_PRE, new Separator());

         if (xCoverageViewer.getWorkProductTaskProvider() != null) {
            mm.insertBefore(MENU_GROUP_PRE, createWorkProductTaskAction);
            createWorkProductTaskAction.setEnabled(isCreateWorkProductTaskActionEnabled());

            mm.insertBefore(MENU_GROUP_PRE, removeWorkProductTaskAction);
            removeWorkProductTaskAction.setEnabled(isCreateWorkProductTaskActionEnabled());

            mm.insertBefore(MENU_GROUP_PRE, openWorkProductTaskAction);
            openWorkProductTaskAction.setEnabled(isCreateWorkProductTaskActionEnabled());
         }

         mm.insertBefore(MENU_GROUP_PRE, new Separator());

      }
      mm.insertBefore(MENU_GROUP_PRE, viewSourceAction);
      editMethodAction.setEnabled(isEditMethodEnabled());
   }

   @Override
   public void updateMenuActionsForTable() {
      MenuManager mm = getMenuManager();
      updateEditMenuActions();
      mm.insertBefore(MENU_GROUP_PRE, new Separator());
      mm.insertBefore(MENU_GROUP_PRE,
         new org.eclipse.osee.framework.ui.skynet.action.ExpandAllAction(xCoverageViewer.getXViewer(), true));
   }

   @Override
   public void dispose() {
      // Dispose of the table objects is done through separate dispose listener off tree
      // Tell the label provider to release its resources
      getLabelProvider().dispose();
   }

   @Override
   public ArrayList<ICoverage> getSelectedCoverageEditorItems() {
      ArrayList<ICoverage> arts = new ArrayList<ICoverage>();
      TreeItem items[] = getTree().getSelection();
      if (items.length > 0) {
         for (TreeItem item : items) {
            arts.add((ICoverage) item.getData());
         }
      }
      return arts;
   }

   @Override
   public void handleColumnMultiEdit(TreeColumn treeColumn, Collection<TreeItem> treeItems) {
      if (!xCoverageViewer.isEditable()) {
         return;
      }
      ArrayList<ICoverage> coverageItems = new ArrayList<ICoverage>();
      for (TreeItem item : treeItems) {
         coverageItems.add((ICoverage) item.getData());
      }
      promptChangeData((XViewerColumn) treeColumn.getData(), coverageItems, isColumnMultiEditEnabled());
   }

   @Override
   public boolean handleLeftClickInIconArea(TreeColumn treeColumn, TreeItem treeItem) {
      return false;
   }

   @Override
   public boolean handleAltLeftClick(TreeColumn treeColumn, TreeItem treeItem) {
      if (!xCoverageViewer.isEditable()) {
         return false;
      }
      createMenuActions();
      try {
         XViewerColumn xCol = (XViewerColumn) treeColumn.getData();
         if (xCol.equals(CoverageXViewerFactory.Assignees_Col)) {
            editAssigneesAction.run();
         } else if (xCol.equals(CoverageXViewerFactory.Notes_Col)) {
            editCoverageStatusAction.run();
         } else if (xCol.equals(CoverageXViewerFactory.Coverage_Method)) {
            editMethodAction.run();
         } else if (xCol.equals(CoverageXViewerFactory.Coverage_Rationale)) {
            editRationaleAction.run();
         } else if (xCol.equals(CoverageXViewerFactory.Work_Product_Task)) {
            createWorkProductTaskAction.run();
         } else if (xCol.equals(CoverageXViewerFactory.Name)) {
            viewSourceAction.run();
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return false;
   }

   @Override
   public void handleDoubleClick() {
      createMenuActions();
      if (getSelectedCoverageEditorItems().size() > 0) {
         viewSourceAction.run();
      }
   }

   public Result isEditable(Collection<ICoverage> coverageItems) {
      for (ICoverage item : coverageItems) {
         if (item.isEditable().isFalse()) {
            return item.isEditable();
         }
      }
      return Result.TrueResult;
   }

   public boolean promptChangeData(XViewerColumn xCol, Collection<ICoverage> coverageItems, boolean colMultiEdit) {
      boolean modified = false;
      if (coverageItems != null && !coverageItems.isEmpty()) {
         //         ICoverage coverageItem = (ICoverage) coverageItems.toArray()[0];

         if (isEditable(coverageItems).isFalse()) {
            MessageDialog.openInformation(Displays.getActiveShell(), "Coverage Item",
               "Read-Only Field - One or more selected Coverage Items is Read-Only");
         }
      }
      if (modified) {
         //         return executeTransaction(promoteItems);
      }
      return false;
   }

   @Override
   public void update(Object element) {
      //      ElapsedTime elapsedTime = new ElapsedTime(getClass().getSimpleName() + " - update");
      xCoverageViewer.getXViewer().update(element, null);
      //      elapsedTime.end();
   }

   @Override
   public Result isEditable() {
      return xCoverageViewer.getSaveable().isEditable();
   }

   @Override
   public Result save(String saveName, CoverageOptionManager coverageOptionManager) throws OseeCoreException {
      return xCoverageViewer.getSaveable().save(saveName, coverageOptionManager);
   }

   @Override
   public void setSelectedCoverageEditorItem(ICoverage item) {
      xCoverageViewer.getXViewer().setSelection(new StructuredSelection(item));
      xCoverageViewer.getXViewer().reveal(new StructuredSelection(item));
      xCoverageViewer.getXViewer().getTree().setFocus();
   }

   @Override
   public Result save(Collection<ICoverage> coverages, String saveName) throws OseeCoreException {
      return xCoverageViewer.getSaveable().save(coverages, saveName);
   }

   @Override
   public Result saveImportRecord(SkynetTransaction transaction, CoverageImport coverageImport) {
      return new Result(false, "Invalid for this.");
   }

   @Override
   public IOseeBranch getBranch() throws OseeCoreException {
      return xCoverageViewer.getSaveable().getBranch();
   }
}