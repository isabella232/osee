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
package org.eclipse.osee.framework.ui.skynet.widgets.xmerge;

import java.sql.SQLException;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.osee.framework.skynet.core.transactionChange.TransactionArtifactChange;
import org.eclipse.osee.framework.skynet.core.transactionChange.TransactionAttributeChange;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class XMergeLabelProvider implements ITableLabelProvider {
   Font font = null;

   private final MergeXViewer mergeXViewer;

   public XMergeLabelProvider(MergeXViewer mergeXViewer) {
      super();
      this.mergeXViewer = mergeXViewer;
   }

   public String getColumnText(Object element, int columnIndex) {
      if (element instanceof String) {
         if (columnIndex == 1)
            return (String) element;
         else
            return "";
      }

      XViewerColumn xCol = mergeXViewer.getXTreeColumn(columnIndex);
      if (xCol != null) {
         MergeColumn aCol = MergeColumn.getAtsXColumn(xCol);
         try {
            return getColumnText(element, columnIndex, xCol, aCol);
         } catch (SQLException ex) {
            OSEELog.logException(SkynetGuiPlugin.class, ex, false);
         }
      }
      return "";
   }

   /**
    * Provided as optimization of subclassed classes so provider doesn't have to retrieve the same information that has
    * already been retrieved
    * 
    * @param element
    * @param columnIndex
    * @param branch
    * @param xCol
    * @param aCol
    * @return column string
    * @throws SQLException
    */
   public String getColumnText(Object element, int columnIndex,
			XViewerColumn xCol, MergeColumn aCol) throws SQLException {
		if (!xCol.isShow())
			return ""; // Since not shown, don't display

		if (element instanceof TransactionArtifactChange) {
			if (aCol == MergeColumn.Attribute_Name) {
				return ((TransactionArtifactChange) element).getArtifact()
						.getDescriptiveName();
			}
		}

		if (element instanceof TransactionAttributeChange) {
			TransactionAttributeChange attributeChange = (TransactionAttributeChange) element;
			if (aCol == MergeColumn.Attribute_Name) {
				return attributeChange.getDynamicAttributeDescriptor().getName();
			} else if (aCol == MergeColumn.From_Parent_Version) {
				return attributeChange.getSourceValue() != null? attributeChange.getSourceValue():"Stream data";
			} else if (aCol == MergeColumn.To_Branch_Version)
				return attributeChange.getDestValue() != null? attributeChange.getDestValue():"Stream data";
			else if (aCol == MergeColumn.Merged_Version)
				return "Merge";

		}
		return "";
	}

   public void dispose() {
      if (font != null) font.dispose();
      font = null;
   }

   public boolean isLabelProperty(Object element, String property) {
      return false;
   }

   public void addListener(ILabelProviderListener listener) {
   }

   public void removeListener(ILabelProviderListener listener) {
   }

   public MergeXViewer getTreeViewer() {
      return mergeXViewer;
   }

   public Image getColumnImage(Object element, int columnIndex) {
      if (element instanceof String) return null;
      XViewerColumn xCol = mergeXViewer.getXTreeColumn(columnIndex);
      if (xCol == null) return null;
      MergeColumn dCol = MergeColumn.getAtsXColumn(xCol);
      if (!xCol.isShow()) return null; // Since not shown, don't display
      
      if(element instanceof TransactionArtifactChange){
    	  if (dCol == MergeColumn.Attribute_Name) {
    		  return ((TransactionArtifactChange)element).getImage();
    	  }
      }
      
      if (dCol == MergeColumn.To_Branch_Version) {
//         return SkynetGuiPlugin.getInstance().getImage("branch.gif");
    	  return null;
      }
      return null;
   }

}
