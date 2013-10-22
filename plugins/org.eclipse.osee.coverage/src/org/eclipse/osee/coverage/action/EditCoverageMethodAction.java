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
package org.eclipse.osee.coverage.action;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.coverage.editor.xcover.CoverageXViewer;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.model.CoverageItem;
import org.eclipse.osee.coverage.model.CoverageOption;
import org.eclipse.osee.coverage.model.CoverageOptionManager.EnabledOption;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.coverage.util.CoverageUtil;
import org.eclipse.osee.coverage.util.ISaveable;
import org.eclipse.osee.coverage.util.dialog.CoverageMethodSingleSelectListDialog;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.ImageManager;

/**
 * @author Donald G. Dunne
 */
public class EditCoverageMethodAction extends Action {

   private final ISelectedCoverageEditorItem selectedCoverageEditorItem;
   private final ISaveable saveable;
   private final IRefreshable refreshable;
   private final CoverageXViewer coverageXViewer;

   public EditCoverageMethodAction(CoverageXViewer coverageXViewer, ISelectedCoverageEditorItem selectedCoverageEditorItem, IRefreshable refreshable, ISaveable saveable) {
      super("Edit Coverage Method");
      this.coverageXViewer = coverageXViewer;
      this.selectedCoverageEditorItem = selectedCoverageEditorItem;
      this.refreshable = refreshable;
      this.saveable = saveable;
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(FrameworkImage.EDIT);
   }

   @Override
   public void run() {
      if (selectedCoverageEditorItem.getSelectedCoverageEditorItems().isEmpty()) {
         AWorkbench.popup("Select Coverage Item(s)");
         return;
      }
      for (ICoverage coverage : selectedCoverageEditorItem.getSelectedCoverageEditorItems()) {
         if (!(coverage instanceof CoverageItem)) {
            AWorkbench.popup("Coverage Method can only be set on Coverage Items");
            return;
         }
         CoverageItem item = (CoverageItem) coverage;
         if (!item.getCoverageMethod().isEnabled()) {
            AWorkbench.popup(String.format("Invalid to change locked Coverage Method [%s] for Coveage Item:\n\n[%s]",
               item.getCoverageMethod().getName(), item));
            return;
         }
      }

      Result result = saveable.isEditable();
      if (result.isFalse()) {
         AWorkbench.popup(result);
         return;
      }

      CoverageMethodSingleSelectListDialog dialog =
         new CoverageMethodSingleSelectListDialog(coverageXViewer.getCoverageOptionManager(), EnabledOption.Write);
      if (dialog.open() == 0) {
         Set<ICoverage> coveragesToSave = new HashSet<ICoverage>();
         for (ICoverage coverageItem : selectedCoverageEditorItem.getSelectedCoverageEditorItems()) {
            if (coverageItem instanceof CoverageItem) {
               ((CoverageItem) coverageItem).setCoverageMethod((CoverageOption) dialog.getResult()[0]);
               refreshable.update(coverageItem);
               coveragesToSave.add(coverageItem);
            }
         }
         try {
            saveable.save(coveragesToSave,
               CoverageUtil.getParentCoveragePackageBase(coveragesToSave.iterator().next()).getName());
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            return;
         }
      }
   }
}
