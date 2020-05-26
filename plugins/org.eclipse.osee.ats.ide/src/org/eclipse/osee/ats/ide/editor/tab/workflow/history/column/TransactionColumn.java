/*********************************************************************
 * Copyright (c) 2011 Boeing
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

package org.eclipse.osee.ats.ide.editor.tab.workflow.history.column;

import org.eclipse.nebula.widgets.xviewer.XViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.core.model.SortDataType;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerAlign;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.osee.framework.skynet.core.change.Change;

/**
 * @author Donald G. Dunne
 */
public class TransactionColumn extends XViewerValueColumn {

   private static TransactionColumn instance = new TransactionColumn();

   public static TransactionColumn getInstance() {
      return instance;
   }

   public TransactionColumn() {
      super("ats.history.Transaction", "Transaction", 80, XViewerAlign.Left, true, SortDataType.Integer, false, "");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public TransactionColumn copy() {
      TransactionColumn newXCol = new TransactionColumn();
      copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      if (element instanceof Change) {
         return String.valueOf(((Change) element).getTxDelta().getEndTx().getId());
      }
      return "";
   }
}
