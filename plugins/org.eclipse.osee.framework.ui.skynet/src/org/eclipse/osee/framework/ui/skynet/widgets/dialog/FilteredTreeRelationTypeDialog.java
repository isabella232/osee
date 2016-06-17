/*******************************************************************************
 * Copyright (c) 2016 Boeing.
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
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.relation.RelationTypeManager;
import org.eclipse.osee.framework.ui.plugin.util.ArrayTreeContentProvider;
import org.eclipse.osee.framework.ui.plugin.util.StringLabelProvider;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.util.StringNameSorter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Donald G. Dunne
 */
public class FilteredTreeRelationTypeDialog extends FilteredTreeDialog {

   private Collection<? extends IRelationType> selectable;

   public FilteredTreeRelationTypeDialog(String title, String message) {
      this(title, message, RelationTypeManager.getAllTypes(), new StringLabelProvider());
   }

   public FilteredTreeRelationTypeDialog(String title, String message, Collection<? extends IRelationType> selectable, ILabelProvider labelProvider) {
      this(title, message, selectable, new ArrayTreeContentProvider(), labelProvider);
   }

   public FilteredTreeRelationTypeDialog(String title, String message, Collection<? extends IRelationType> selectable, ITreeContentProvider contentProvider, ILabelProvider labelProvider) {
      this(title, message, selectable, contentProvider, labelProvider, new StringNameSorter());
   }

   public FilteredTreeRelationTypeDialog(String title, String message, Collection<? extends IRelationType> selectable, ITreeContentProvider contentProvider, ILabelProvider labelProvider, ViewerSorter sorter) {
      super(title, message, contentProvider, labelProvider, sorter);
      this.selectable = selectable;
   }

   public FilteredTreeRelationTypeDialog(String title, Collection<? extends IRelationType> selectable) {
      this(title, title, selectable, new StringLabelProvider());
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
            return new Result("Must select Relation type.");
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return Result.TrueResult;
   }

   public void setSelectable(Collection<? extends IRelationType> selectable) {
      this.selectable = selectable;
   }

   @Override
   public void setSorter(ViewerSorter sorter) {
      getTreeViewer().getViewer().setSorter(sorter);
   }

}
