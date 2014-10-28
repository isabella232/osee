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

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.util.ArrayTreeContentProvider;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.util.ArtifactTypeLabelProvider;
import org.eclipse.osee.framework.ui.skynet.util.ArtifactTypeNameSorter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Donald G. Dunne
 */
public class ArtifactTypeFilteredTreeDialog extends FilteredCheckboxTreeDialog {
   private IArtifactType selection;

   public ArtifactTypeFilteredTreeDialog(String title, String message) {
      super(title, message, new ArrayTreeContentProvider(), new ArtifactTypeLabelProvider());
      setMultiSelect(false);
   }

   @Override
   protected Control createDialogArea(Composite container) {
      Control comp = super.createDialogArea(container);
      try {
         getTreeViewer().getViewer().setSorter(new ArtifactTypeNameSorter());
         getTreeViewer().getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
               IStructuredSelection sel = (IStructuredSelection) getTreeViewer().getViewer().getSelection();
               if (sel.isEmpty()) {
                  selection = null;
               } else {
                  selection =
                     (IArtifactType) ((IStructuredSelection) getTreeViewer().getViewer().getSelection()).getFirstElement();
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
         gd.heightHint = 500;
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
            return new Result("Must select Artifact type.");
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return Result.TrueResult;
   }

   /**
    * @return the selection
    */
   public IArtifactType getSelection() {
      return selection;
   }
}
