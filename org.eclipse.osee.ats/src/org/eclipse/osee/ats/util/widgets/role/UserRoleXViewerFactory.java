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

import java.util.ArrayList;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerSorter;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.customize.CustomizeData;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.skynet.SkynetXViewerFactory;

/**
 * @author Donald G. Dunne
 */
public class UserRoleXViewerFactory extends SkynetXViewerFactory {

   private static String NAMESPACE = "osee.ats.UserRoleXViewer";

   /**
    * 
    */
   public UserRoleXViewerFactory() {
      super(NAMESPACE);
   }

   public XViewerSorter createNewXSorter(XViewer xViewer) {
      return new XViewerSorter(xViewer);
   }

   public CustomizeData getDefaultTableCustomizeData() {
      CustomizeData custData = new CustomizeData();
      ArrayList<XViewerColumn> cols = new ArrayList<XViewerColumn>();
      for (UserRoleColumn atsXCol : UserRoleColumn.values()) {
         XViewerColumn newCol = atsXCol.getXViewerColumn(atsXCol);
         cols.add(newCol);
      }
      custData.getColumnData().setColumns(cols);
      return custData;
   }

   /*
    * (non-Javadoc)
    * 
    * @see osee.skynet.gui.widgets.xviewer.IXViewerFactory#getDefaultXViewerColumn()
    */
   public XViewerColumn getDefaultXViewerColumn(String id) {
      for (UserRoleColumn atsXCol : UserRoleColumn.values()) {
         if (atsXCol.getName().equals(id)) {
            return atsXCol.getXViewerColumn(atsXCol);
         }
      }
      return null;
   }

}
