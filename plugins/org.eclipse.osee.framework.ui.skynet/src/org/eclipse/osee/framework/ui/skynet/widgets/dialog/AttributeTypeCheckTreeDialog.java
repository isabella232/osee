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
package org.eclipse.osee.framework.ui.skynet.widgets.dialog;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.ui.plugin.util.ArrayTreeContentProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Donald G. Dunne
 */
public class AttributeTypeCheckTreeDialog extends FilteredCheckboxTreeDialog {

   public AttributeTypeCheckTreeDialog(Collection<IAttributeType> attributeTypes) {
      this("Select Attribyte Types", "Select Attribyte Types", attributeTypes, new AttributeTypeLabelProvider());
   }

   public AttributeTypeCheckTreeDialog(String title, String message, Collection<IAttributeType> attributeTypes, ILabelProvider iLabelProvider) {
      super(title, message, new ArrayTreeContentProvider(), iLabelProvider);
      setInput(attributeTypes);
      if (attributeTypes != null) {
         setInput(attributeTypes);
      }
   }

   public AttributeTypeCheckTreeDialog() {
      this(null);
   }

   public AttributeTypeCheckTreeDialog(String title, String message, Collection<IAttributeType> attributeTypes) {
      this(title, message, attributeTypes, new AttributeTypeLabelProvider());
   }

   public Collection<IAttributeType> getSelection() {
      ArrayList<IAttributeType> arts = new ArrayList<IAttributeType>();
      for (Object obj : getResult()) {
         arts.add((IAttributeType) obj);
      }
      return arts;
   }

   @Override
   protected Control createDialogArea(Composite container) {
      Control c = super.createDialogArea(container);
      getTreeViewer().setSorter(new ViewerSorter() {
         @SuppressWarnings("unchecked")
         @Override
         public int compare(Viewer viewer, Object e1, Object e2) {
            IAttributeType type1 = (IAttributeType) e1;
            IAttributeType type2 = (IAttributeType) e2;
            return getComparator().compare(type1.getName(), type2.getName());
         }
      });
      return c;
   }
}