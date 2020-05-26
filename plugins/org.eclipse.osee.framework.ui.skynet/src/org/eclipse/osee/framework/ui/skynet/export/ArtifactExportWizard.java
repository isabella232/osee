/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.framework.ui.skynet.export;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * @author Ryan D. Brooks
 */
public class ArtifactExportWizard extends Wizard implements IExportWizard {
   private ArtifactExportPage mainPage;

   @Override
   public boolean performFinish() {
      try {
         Jobs.startJob(new ArtifactExportJob(mainPage.getExportPath(), mainPage.getExportArtifacts()));
      } catch (Exception ex) {
         ErrorDialog.openError(getShell(), "Define Export Error", ex.getLocalizedMessage(), new Status(IStatus.ERROR,
            "org.eclipse.osee.framework.jdk.core", IStatus.ERROR, ex.getLocalizedMessage(), ex));
      }
      return true;
   }

   @Override
   public void init(IWorkbench workbench, IStructuredSelection selection) {
      mainPage = new ArtifactExportPage(selection);
   }

   @Override
   public void addPages() {
      addPage(mainPage);
   }
}