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

package org.eclipse.osee.coverage.util.dialog;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.model.WorkProductTask;
import org.eclipse.osee.coverage.util.WorkProductActionLabelProvider;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.util.ArrayTreeContentProvider;
import org.eclipse.osee.framework.ui.skynet.util.StringNameSorter;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.FilteredCheckboxTreeDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Donald G. Dunne
 */
public class WorkProductTaskFilteredTreeDialog extends FilteredCheckboxTreeDialog {
   private WorkProductTask selection;

   public WorkProductTaskFilteredTreeDialog(String title, String message) {
      super(title, message, new ArrayTreeContentProvider(), new WorkProductActionLabelProvider());
      setMultiSelect(false);
   }

   @Override
   protected Control createDialogArea(Composite container) {
      Control comp = super.createDialogArea(container);
      try {
         getTreeViewer().getViewer().setSorter(new StringNameSorter());
         getTreeViewer().getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
               IStructuredSelection sel = (IStructuredSelection) getTreeViewer().getViewer().getSelection();
               if (sel.isEmpty()) {
                  selection = null;
               } else {
                  selection =
                     (WorkProductTask) ((IStructuredSelection) getTreeViewer().getViewer().getSelection()).getFirstElement();
               }
            }
         });
         getTreeViewer().getViewer().getTree().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
               super.widgetSelected(e);
               updateStatusLabel();
            }
         });
         GridData gd = new GridData(GridData.FILL_BOTH);
         gd.heightHint = 400;
         getTreeViewer().getViewer().getTree().setLayoutData(gd);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return comp;
   }

   @Override
   protected Result isComplete() {
      try {
         if (selection == null) {
            return new Result("Must select Work Product Task.");
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return Result.TrueResult;
   }

   public WorkProductTask getSelection() {
      return selection;
   }
}
