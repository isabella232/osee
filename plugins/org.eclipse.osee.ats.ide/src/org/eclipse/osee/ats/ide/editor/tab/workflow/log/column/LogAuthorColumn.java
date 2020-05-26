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

package org.eclipse.osee.ats.ide.editor.tab.workflow.log.column;

import org.eclipse.nebula.widgets.xviewer.XViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.core.model.SortDataType;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerAlign;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.osee.ats.api.workflow.log.IAtsLogItem;
import org.eclipse.osee.ats.core.workflow.log.AtsLogUtility;
import org.eclipse.osee.ats.ide.internal.AtsClientService;

/**
 * @author Donald G. Dunne
 */
public class LogAuthorColumn extends XViewerValueColumn {
   private static LogAuthorColumn instance = new LogAuthorColumn();

   public static LogAuthorColumn getInstance() {
      return instance;
   }

   public LogAuthorColumn() {
      super("ats.log.Author", "Author", 100, XViewerAlign.Left, true, SortDataType.String, false, "");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public LogAuthorColumn copy() {
      LogAuthorColumn newXCol = new LogAuthorColumn();
      copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      if (element instanceof IAtsLogItem) {
         return AtsLogUtility.getUserName(((IAtsLogItem) element).getUserId(), AtsClientService.get().getUserService());
      }
      return "";
   }
}
