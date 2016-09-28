/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.widgets.dialog;

import java.util.Collection;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.ui.plugin.util.ArrayTreeContentProvider;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.util.AttributeTypeNameComparator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Donald G. Dunne
 */
public class FilteredTreeAttributeTypeDialog extends FilteredTreeDialog {

   private Collection<? extends IAttributeType> selectable;

   public FilteredTreeAttributeTypeDialog(String title, String message) {
      this(title, message, AttributeTypeManager.getAllTypes(), new AttributeTypeLabelProvider());
   }

   public FilteredTreeAttributeTypeDialog(String title, String message, Collection<? extends IAttributeType> selectable, ILabelProvider labelProvider) {
      this(title, message, selectable, new ArrayTreeContentProvider(), labelProvider);
   }

   public FilteredTreeAttributeTypeDialog(String title, String message, Collection<? extends IAttributeType> selectable, ITreeContentProvider contentProvider, ILabelProvider labelProvider) {
      this(title, message, selectable, contentProvider, labelProvider, new AttributeTypeNameComparator());
   }

   public FilteredTreeAttributeTypeDialog(String title, String message, Collection<? extends IAttributeType> selectable, ITreeContentProvider contentProvider, ILabelProvider labelProvider, ViewerComparator comparator) {
      super(title, message, contentProvider, labelProvider, comparator);
      this.selectable = selectable;
   }

   public FilteredTreeAttributeTypeDialog(String title, Collection<? extends IAttributeType> selectable) {
      this(title, title, selectable, new AttributeTypeLabelProvider());
   }

   @Override
   protected Control createDialogArea(Composite container) {
      Control comp = super.createDialogArea(container);
      try {
         getTreeViewer().getViewer().setInput(selectable);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return comp;
   }

   @Override
   protected Result isComplete() {
      try {
         if (getSelectedFirst() == null) {
            return new Result("Must select Artifact type.");
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return Result.TrueResult;
   }

   public void setSelectable(Collection<? extends IAttributeType> selectable) {
      this.selectable = selectable;
   }

   @Override
   public void setComparator(ViewerComparator comparator) {
      getTreeViewer().getViewer().setComparator(comparator);
   }

}
