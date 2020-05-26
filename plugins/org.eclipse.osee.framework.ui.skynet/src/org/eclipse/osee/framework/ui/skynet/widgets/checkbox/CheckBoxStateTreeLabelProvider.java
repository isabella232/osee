/*********************************************************************
 * Copyright (c) 2018 Boeing
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

package org.eclipse.osee.framework.ui.skynet.widgets.checkbox;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osee.framework.ui.skynet.widgets.checkbox.images.CheckBoxStateImageCache;
import org.eclipse.swt.graphics.Image;

/**
 * @author Donald G. Dunne
 */
public class CheckBoxStateTreeLabelProvider extends LabelProvider {

   private ICheckBoxStateTreeViewer treeViewer;

   public CheckBoxStateTreeLabelProvider(ICheckBoxStateTreeViewer treeViewer) {
      this.treeViewer = treeViewer;
   }

   @Override
   public Image getImage(Object element) {
      Image image = CheckBoxStateImageCache.getImage("chkbox_unchecked.gif");
      if (isEnabled(element)) {
         if (treeViewer.isChecked(element)) {
            image = CheckBoxStateImageCache.getImage("chkbox_checked.gif");
         }
      } else {
         image = CheckBoxStateImageCache.getImage("chkbox_disabled.gif");
      }
      return image;
   }

   /**
    * Override to provide different implementation
    */
   protected boolean isEnabled(Object element) {
      return treeViewer.isEnabled(element);
   }

   public void setTreeViewer(ICheckBoxStateTreeViewer treeViewer) {
      this.treeViewer = treeViewer;
   }

}
