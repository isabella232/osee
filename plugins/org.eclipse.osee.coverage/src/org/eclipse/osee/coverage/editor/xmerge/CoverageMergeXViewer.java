/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.coverage.editor.xmerge;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.nebula.widgets.xviewer.IXViewerFactory;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.coverage.editor.xcover.CoverageXViewer;
import org.eclipse.osee.coverage.editor.xcover.XCoverageViewer.TableType;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.merge.IMergeItem;
import org.eclipse.osee.coverage.merge.MergeManager;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Donald G. Dunne
 */
public class CoverageMergeXViewer extends CoverageXViewer {

   //   public Map<ICoverage, Boolean> importChecked = new HashMap<ICoverage, Boolean>();
   public Map<ICoverage, XResultData> importError = new HashMap<ICoverage, XResultData>();
   Action toggleImport;
   private final MergeManager mergeManager;
   public static enum ImportType {
      Add,
      Replace,
      Folder,
      Error,
      None
   };

   public CoverageMergeXViewer(MergeManager mergeManager, Composite parent, int style, IXViewerFactory xViewerFactory, XCoverageMergeViewer xCoverageMergeViewer) {
      super(parent, style, xViewerFactory, xCoverageMergeViewer);
      this.mergeManager = mergeManager;
   }

   @Override
   public boolean handleLeftClickInIconArea(TreeColumn treeColumn, TreeItem treeItem) {
      XViewerColumn xCol = (XViewerColumn) treeColumn.getData();
      if (xCol.equals(CoverageMergeXViewerFactoryImport.Import)) {
         try {
            if (treeItem.getData() instanceof IMergeItem && ((IMergeItem) treeItem.getData()).isCheckable()) {
               ((IMergeItem) treeItem.getData()).setChecked(!((IMergeItem) treeItem.getData()).isChecked());
               xCoverageViewer.getXViewer().update(treeItem.getData());
            }
            return true;
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         }
      }
      return super.handleLeftClickInIconArea(treeColumn, treeItem);
   }

   @Override
   public boolean handleLeftClick(TreeColumn treeColumn, TreeItem treeItem) {
      //      XViewerColumn xCol = (XViewerColumn) treeColumn.getData();
      return super.handleLeftClick(treeColumn, treeItem);
   }

   @Override
   public void updateEditMenuActions() {
      MenuManager mm = getMenuManager();
      if (xCoverageViewer.isType(TableType.Merge) && xCoverageViewer.isType(TableType.Import)) {
         mm.insertBefore(MENU_GROUP_PRE, toggleImport);
         toggleImport.setEnabled(isToggleImportEnabled());
      }
      super.updateEditMenuActions();
   }

   private boolean isToggleImportEnabled() {
      if (xCoverageViewer.getSelectedCoverageItems().isEmpty()) {
         return false;
      }
      for (ICoverage item : xCoverageViewer.getSelectedCoverageItems()) {
         if (item.isEditable().isFalse() || !(item instanceof IMergeItem) || !((IMergeItem) item).isImportAllowed()) {
            return false;
         }
      }
      return true;
   }

   @Override
   public void createMenuActions() {
      super.createMenuActions();

      toggleImport = new Action("Toggle Import", IAction.AS_PUSH_BUTTON) {
         @Override
         public void run() {
            try {
               for (ICoverage coverageItem : xCoverageViewer.getSelectedCoverageItems()) {
                  if (coverageItem instanceof IMergeItem && ((IMergeItem) coverageItem).isCheckable()) {
                     ((IMergeItem) coverageItem).setChecked(!((IMergeItem) coverageItem).isChecked());
                     xCoverageViewer.getXViewer().update(coverageItem);
                  }
               }
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      };
   }

   public MergeManager getMergeManager() {
      return mergeManager;
   }

}
