/*********************************************************************
 * Copyright (c) 2010 Boeing
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

package org.eclipse.osee.ats.ide.editor.tab.workflow.log;

import org.eclipse.nebula.widgets.xviewer.XViewerFactory;
import org.eclipse.nebula.widgets.xviewer.core.model.CustomizeData;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.osee.ats.ide.editor.tab.workflow.log.column.LogAuthorColumn;
import org.eclipse.osee.ats.ide.editor.tab.workflow.log.column.LogDateColumn;
import org.eclipse.osee.ats.ide.editor.tab.workflow.log.column.LogEventColumn;
import org.eclipse.osee.ats.ide.editor.tab.workflow.log.column.LogMessageColumn;
import org.eclipse.osee.ats.ide.editor.tab.workflow.log.column.LogStateColumn;

/**
 * @author Donald G. Dunne
 */
public class LogXViewerFactory extends XViewerFactory {

   public LogXViewerFactory() {
      super("ats.log");
      registerColumns(LogEventColumn.getInstance(), LogStateColumn.getInstance(), LogMessageColumn.getInstance(),
         LogDateColumn.getInstance(), LogAuthorColumn.getInstance());
   }

   @Override
   public boolean isAdmin() {
      return false;
   }

   @Override
   public CustomizeData getDefaultTableCustomizeData() {
      CustomizeData customizeData = super.getDefaultTableCustomizeData();
      for (XViewerColumn xCol : customizeData.getColumnData().getColumns()) {
         if (xCol.getId().equals(LogDateColumn.getInstance().getId())) {
            xCol.setSortForward(true);
         }
      }
      customizeData.getSortingData().setSortingNames(LogDateColumn.getInstance().getId());
      return customizeData;
   }

}