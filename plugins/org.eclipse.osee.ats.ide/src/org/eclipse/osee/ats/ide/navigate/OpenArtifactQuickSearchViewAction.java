/*********************************************************************
 * Copyright (c) 2017 Boeing
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

package org.eclipse.osee.ats.ide.navigate;

import org.eclipse.jface.action.Action;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.search.QuickSearchView;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * @author Donald G. Dunne
 */
public class OpenArtifactQuickSearchViewAction extends Action {

   public OpenArtifactQuickSearchViewAction() {
      super("Open Artifact Quick Search");
      setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.ARTIFACT_SEARCH));
   }

   @Override
   public void run() {
      try {
         PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(QuickSearchView.VIEW_ID);
      } catch (PartInitException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Unable to open", ex);
      }
   }

}
