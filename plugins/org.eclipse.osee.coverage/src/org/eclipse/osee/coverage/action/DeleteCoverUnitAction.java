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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.coverage.event.CoverageEventManager;
import org.eclipse.osee.coverage.event.CoverageEventType;
import org.eclipse.osee.coverage.event.CoveragePackageEvent;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.model.CoveragePackage;
import org.eclipse.osee.coverage.model.CoverageUnit;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.coverage.model.ICoverageUnitProvider;
import org.eclipse.osee.coverage.store.OseeCoverageUnitStore;
import org.eclipse.osee.coverage.util.CoverageUtil;
import org.eclipse.osee.coverage.util.ISaveable;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.ImageManager;

/**
 * @author Donald G. Dunne
 */
public class DeleteCoverUnitAction extends Action {
   private final ISelectedCoverageEditorItem selectedCoverageEditorItem;
   private final ISaveable saveable;
   private final IRefreshable refreshable;

   public DeleteCoverUnitAction(ISelectedCoverageEditorItem selectedCoverageEditorItem, IRefreshable refreshable, ISaveable saveable) {
      super("Delete Coverage Unit");
      this.selectedCoverageEditorItem = selectedCoverageEditorItem;
      this.refreshable = refreshable;
      this.saveable = saveable;
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(FrameworkImage.DELETE);
   }

   @Override
   public void run() {
      if (selectedCoverageEditorItem.getSelectedCoverageEditorItems().isEmpty()) {
         AWorkbench.popup("Select Coverage Unit");
         return;
      }
      for (ICoverage item : selectedCoverageEditorItem.getSelectedCoverageEditorItems()) {
         if (!(item instanceof CoverageUnit)) {
            AWorkbench.popup("Can only delete Coverage Units");
            return;
         }
      }
      Result result = saveable.isEditable();
      if (result.isFalse()) {
         AWorkbench.popup(result);
         return;
      }
      CoveragePackage coveragePackage = null;
      if (MessageDialog.openConfirm(Displays.getActiveShell(), "Delete Coverage Unit", "Delete Coverage Units")) {
         try {
            ICoverage coverage = selectedCoverageEditorItem.getSelectedCoverageEditorItems().iterator().next();
            coveragePackage = (CoveragePackage) CoverageUtil.getParentCoveragePackageBase(coverage);
            SkynetTransaction transaction =
               TransactionManager.createTransaction(saveable.getBranch(),
                  "Coverage - Delete Coverage Unit - " + coveragePackage.getName());
            CoveragePackageEvent coverageEvent = new CoveragePackageEvent(coveragePackage, CoverageEventType.Modified);
            List<ICoverage> deleteItems = new ArrayList<ICoverage>();
            for (ICoverage coverageItem : selectedCoverageEditorItem.getSelectedCoverageEditorItems()) {
               if (coverageItem.getParent() instanceof ICoverageUnitProvider) {
                  ((ICoverageUnitProvider) coverageItem.getParent()).removeCoverageUnit((CoverageUnit) coverageItem);
                  deleteItems.add(coverageItem);
                  new OseeCoverageUnitStore((CoverageUnit) coverageItem, saveable.getBranch()).delete(transaction,
                     coverageEvent, false);
               }
            }
            transaction.execute();
            CoverageEventManager.instance.sendRemoteEvent(coverageEvent);
            for (ICoverage coverageItem : deleteItems) {
               refreshable.remove(coverageItem);
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            return;
         }
      }
      try {
         saveable.save(coveragePackage.getName(), coveragePackage.getCoverageOptionManager());
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         return;
      }
   }
}
