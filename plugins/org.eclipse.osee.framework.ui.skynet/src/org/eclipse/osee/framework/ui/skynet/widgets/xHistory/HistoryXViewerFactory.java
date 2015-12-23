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
package org.eclipse.osee.framework.ui.skynet.widgets.xHistory;

import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn.SortDataType;
import org.eclipse.nebula.widgets.xviewer.XViewerSorter;
import org.eclipse.nebula.widgets.xviewer.customize.CustomizeData;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.skynet.SkynetXViewerFactory;
import org.eclipse.swt.SWT;

/**
 * @author Jeff C. Phillips
 */
public class HistoryXViewerFactory extends SkynetXViewerFactory {
   public final static XViewerColumn transaction = new XViewerColumn("framework.history.transaction", "Transaction", 90,
      SWT.LEFT, true, SortDataType.Integer, false, null);
   public final static XViewerColumn gamma =
      new XViewerColumn("framework.history.gamma", "Gamma", 60, SWT.LEFT, false, SortDataType.Integer, false, null);
   public final static XViewerColumn itemType = new XViewerColumn("framework.history.itemType", "Item Type", 150,
      SWT.LEFT, true, SortDataType.String, false, null);
   public final static XViewerColumn itemChange = new XViewerColumn("framework.history.itemChange", "Item Kind", 100,
      SWT.LEFT, true, SortDataType.String, false, null);
   public final static XViewerColumn modType =
      new XViewerColumn("framework.history.modType", "Mod Type", 70, SWT.LEFT, true, SortDataType.String, false, null);
   public final static XViewerColumn itemId =
      new XViewerColumn("framework.history.itemId", "Item ID", 55, SWT.LEFT, true, SortDataType.Integer, false, null);
   public final static XViewerColumn was =
      new XViewerColumn("framework.history.was", "Was", 150, SWT.LEFT, true, SortDataType.String, false, null);
   public final static XViewerColumn is =
      new XViewerColumn("framework.history.is", "Is", 150, SWT.LEFT, true, SortDataType.String, false, null);
   public final static XViewerColumn timeStamp = new XViewerColumn("framework.history.timeStamp", "Time Stamp", 110,
      SWT.LEFT, true, SortDataType.Date, false, null);
   public final static XViewerColumn author =
      new XViewerColumn("framework.history.author", "Author", 100, SWT.LEFT, true, SortDataType.String, false, null);
   public final static XViewerColumn comment =
      new XViewerColumn("framework.history.comment", "Comment", 300, SWT.LEFT, true, SortDataType.String, false, null);

   public final static String NAMESPACE = "osee.skynet.gui.HisotryXViewer";

   public HistoryXViewerFactory() {
      super(NAMESPACE);
      registerColumns(transaction, gamma, itemType, itemChange, modType, itemId, was, is, timeStamp, author, comment);
      registerAllAttributeColumns();
   }

   @Override
   public XViewerSorter createNewXSorter(XViewer xViewer) {
      return new XViewerSorter(xViewer);
   }

   @Override
   public CustomizeData getDefaultTableCustomizeData() {
      CustomizeData customizeData = super.getDefaultTableCustomizeData();
      for (XViewerColumn xCol : customizeData.getColumnData().getColumns()) {
         if (xCol.getId() == transaction.getId()) {
            xCol.setSortForward(false);
         }
      }
      customizeData.getSortingData().setSortingNames(transaction.getId());
      return customizeData;
   }

}
