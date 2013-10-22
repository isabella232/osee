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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.coverage.editor.CoverageEditor;
import org.eclipse.osee.coverage.editor.CoverageEditorInput;
import org.eclipse.osee.coverage.event.CoverageEventManager;
import org.eclipse.osee.coverage.event.CoverageEventType;
import org.eclipse.osee.coverage.event.CoveragePackageEvent;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.model.CoverageOptionManagerDefault;
import org.eclipse.osee.coverage.model.CoveragePackage;
import org.eclipse.osee.coverage.store.DbWorkProductTaskProvider;
import org.eclipse.osee.coverage.store.OseeCoveragePackageStore;
import org.eclipse.osee.coverage.util.CoverageImage;
import org.eclipse.osee.coverage.util.CoverageUtil;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.EntryDialog;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.osee.framework.ui.swt.KeyedImage;

/**
 * @author Donald G. Dunne
 */
public class NewCoveragePackageAction extends Action {

   public static KeyedImage OSEE_IMAGE = CoverageImage.COVERAGE_PACKAGE;

   public NewCoveragePackageAction() {
      super("Create New Coverage Package");
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(OSEE_IMAGE);
   }

   @Override
   public void run() {
      try {
         if (!CoverageUtil.getBranchFromUser(false)) {
            return;
         }
         IOseeBranch branch = CoverageUtil.getBranch();
         EntryDialog dialog = new EntryDialog(getText(), "Enter Coverage Package Name");
         if (dialog.open() == 0) {
            CoveragePackage coveragePackage =
               new CoveragePackage(dialog.getEntry(), CoverageOptionManagerDefault.instance(),
                  new DbWorkProductTaskProvider(branch));
            SkynetTransaction transaction =
               TransactionManager.createTransaction(branch, "Add Coverage Package - " + coveragePackage.getName());
            CoveragePackageEvent coverageEvent = new CoveragePackageEvent(coveragePackage, CoverageEventType.Added);
            OseeCoveragePackageStore.get(coveragePackage, branch).save(transaction, coverageEvent,
               coveragePackage.getCoverageOptionManager());
            transaction.execute();
            CoverageEventManager.instance.sendRemoteEvent(coverageEvent);
            CoverageEditor.open(new CoverageEditorInput(dialog.getEntry(), OseeCoveragePackageStore.get(
               coveragePackage, branch).getArtifact(false), coveragePackage, false));
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }
}
