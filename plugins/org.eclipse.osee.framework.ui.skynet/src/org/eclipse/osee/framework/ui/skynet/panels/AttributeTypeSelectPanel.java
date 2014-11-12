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
package org.eclipse.osee.framework.ui.skynet.panels;

import java.util.Collection;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.AttributeTypeLabelProvider;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.FilteredCheckboxAttributeTypeDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Roberto E. Escobar
 */
public class AttributeTypeSelectPanel extends AbstractItemSelectPanel<Collection<IAttributeType>> {

   private Collection<IAttributeType> attributeTypes;
   private String title;
   private String message;

   public AttributeTypeSelectPanel() {
      super(new AttributeTypeLabelProvider(), new ArrayContentProvider());
      this.title = "";
      this.message = "";
   }

   public void setAllowedAttributeTypes(Collection<IAttributeType> attributeTypes) {
      this.attributeTypes = attributeTypes;
   }

   public void setDialogTitle(String title) {
      this.title = title;
   }

   public void setDialogMessage(String message) {
      this.message = message;
   }

   @Override
   protected Dialog createSelectDialog(Shell shell, Collection<IAttributeType> lastSelected) {
      FilteredCheckboxAttributeTypeDialog dialog =
         new FilteredCheckboxAttributeTypeDialog(title, message, attributeTypes);
      if (lastSelected != null) {
         dialog.setInitialSelections(lastSelected);
      }
      return dialog;
   }

   @Override
   protected boolean updateFromDialogResult(Dialog dialog) {
      boolean wasUpdated = false;
      FilteredCheckboxAttributeTypeDialog castedDialog = (FilteredCheckboxAttributeTypeDialog) dialog;
      Collection<IAttributeType> artifactTypes = castedDialog.getChecked();
      if (artifactTypes != null) {
         setSelected(artifactTypes);
         wasUpdated = true;
      }
      return wasUpdated;
   }
}
