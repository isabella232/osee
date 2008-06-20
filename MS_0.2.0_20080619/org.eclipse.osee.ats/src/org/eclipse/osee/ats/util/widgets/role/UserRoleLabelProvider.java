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
package org.eclipse.osee.ats.util.widgets.role;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.osee.ats.AtsPlugin;
import org.eclipse.osee.ats.util.AtsLib;
import org.eclipse.osee.ats.util.widgets.defect.DefectItem.Severity;
import org.eclipse.osee.framework.skynet.core.SkynetAuthentication;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerCells;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class UserRoleLabelProvider implements ITableLabelProvider {
   Font font = null;

   private final UserRoleXViewer treeViewer;

   public UserRoleLabelProvider(UserRoleXViewer treeViewer) {
      super();
      this.treeViewer = treeViewer;
   }

   public String getColumnText(Object element, int columnIndex) {
      if (element instanceof String) {
         if (columnIndex == 1)
            return (String) element;
         else
            return "";
      }
      UserRole defectItem = ((UserRole) element);
      if (defectItem == null) return "";
      XViewerColumn xCol = treeViewer.getXTreeColumn(columnIndex);
      if (xCol != null) {
         UserRoleColumn aCol = UserRoleColumn.getAtsXColumn(xCol);
         return getColumnText(element, columnIndex, defectItem, xCol, aCol);
      }
      return "";
   }

   /**
    * Provided as optimization of subclassed classes so provider doesn't have to retrieve the same information that has
    * already been retrieved
    * 
    * @param element
    * @param columnIndex
    * @param defectItem
    * @param xCol
    * @param aCol
    * @return column string
    */
   public String getColumnText(Object element, int columnIndex, UserRole defectItem, XViewerColumn xCol, UserRoleColumn aCol) {
      try {
         if (!xCol.isShow()) return ""; // Since not shown, don't display
         if (aCol == UserRoleColumn.User_Col)
            return defectItem.getUser().getName();
         else if (aCol == UserRoleColumn.Hours_Spent_Col)
            return defectItem.getHoursSpent() == null ? "" : AtsLib.doubleToStrString(defectItem.getHoursSpent(), false);
         else if (aCol == UserRoleColumn.Role_Col)
            return defectItem.getRole().name();
         else if (aCol == UserRoleColumn.Completed_Col)
            return String.valueOf(defectItem.isCompleted());
         else if (aCol == UserRoleColumn.Num_Major_Col)
            return treeViewer.getXUserRoleViewer().getReviewArt().getUserRoleManager().getNumMajor(defectItem.getUser()) + "";
         else if (aCol == UserRoleColumn.Num_Minor_Col)
            return treeViewer.getXUserRoleViewer().getReviewArt().getUserRoleManager().getNumMinor(defectItem.getUser()) + "";
         else if (aCol == UserRoleColumn.Num_Issues_Col) return treeViewer.getXUserRoleViewer().getReviewArt().getUserRoleManager().getNumIssues(
               defectItem.getUser()) + "";
      } catch (Exception ex) {
         return XViewerCells.getCellExceptionString(ex);
      }

      return "Unhandled Column";
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

   public UserRoleXViewer getTreeViewer() {
      return treeViewer;
   }

   public Image getColumnImage(Object element, int columnIndex) {
      if (element instanceof String) return null;
      UserRole roleItem = (UserRole) element;
      XViewerColumn xCol = treeViewer.getXTreeColumn(columnIndex);
      if (xCol == null) return null;
      UserRoleColumn dCol = UserRoleColumn.getAtsXColumn(xCol);
      if (!xCol.isShow()) return null; // Since not shown, don't display
      if (dCol == UserRoleColumn.User_Col) {
         if (roleItem.getUser().equals(SkynetAuthentication.getUser()))
            return SkynetGuiPlugin.getInstance().getImage("red_user_sm.gif");
         else
            return SkynetGuiPlugin.getInstance().getImage("user_sm.gif");
      } else if (dCol == UserRoleColumn.Role_Col) {
         return AtsPlugin.getInstance().getImage("role.gif");
      } else if (dCol == UserRoleColumn.Hours_Spent_Col) {
         return SkynetGuiPlugin.getInstance().getImage("clock.gif");
      } else if (dCol == UserRoleColumn.Completed_Col) {
         return roleItem.isCompleted() ? SkynetGuiPlugin.getInstance().getImage("chkbox_enabled.gif") : SkynetGuiPlugin.getInstance().getImage(
               "chkbox_disabled.gif");
      } else if (dCol == UserRoleColumn.Num_Major_Col) {
         return Severity.getImage(Severity.Major);
      } else if (dCol == UserRoleColumn.Num_Minor_Col) {
         return Severity.getImage(Severity.Minor);
      } else if (dCol == UserRoleColumn.Num_Issues_Col) {
         return Severity.getImage(Severity.Issue);
      }
      return null;
   }
}
