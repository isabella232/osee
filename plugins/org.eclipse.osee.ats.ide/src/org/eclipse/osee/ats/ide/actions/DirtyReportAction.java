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

package org.eclipse.osee.ats.ide.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.ats.api.util.AtsUtil;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.ImageManager;

/**
 * @author Donald G. Dunne
 */
public class DirtyReportAction extends AbstractAtsAction {

   private final IDirtyReportable reportable;

   public DirtyReportAction(IDirtyReportable reportable) {
      super("Show Artifact Dirty Report");
      this.reportable = reportable;
      setToolTipText("Show what attribute or relation making editor dirty.");
   }

   @Override
   public void runWithException() {
      Result result = reportable.isDirtyResult();
      if (AtsUtil.isInTest()) {
         throw new OseeStateException("Dirty Report", result.isFalse() ? "Not Dirty" : "Dirty -> " + result.getText());
      } else {
         AWorkbench.popup("Dirty Report", result.isFalse() ? "Not Dirty" : "Dirty -> " + result.getText());
      }
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(FrameworkImage.DIRTY);
   }

}
