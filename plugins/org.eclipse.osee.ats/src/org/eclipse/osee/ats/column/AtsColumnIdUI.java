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
package org.eclipse.osee.ats.column;

import java.util.Collection;
import java.util.Map;
import org.eclipse.nebula.widgets.xviewer.IXViewerPreComputedColumn;
import org.eclipse.nebula.widgets.xviewer.core.model.SortDataType;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerAlign;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.column.AtsColumnIdValueColumn;
import org.eclipse.osee.ats.api.config.ColumnAlign;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;

/**
 * XViewerAtsColumn for columns that provide their text through AtsColumnService and are not strictly attribute based.
 *
 * @author Donald G. Dunne
 */
public class AtsColumnIdUI extends XViewerAtsColumn implements IXViewerPreComputedColumn {

   private final AtsColumnIdValueColumn columnIdColumn;
   private final IAtsServices services;

   public AtsColumnIdUI(AtsColumnIdValueColumn columnIdColumn, IAtsServices services) {
      super(columnIdColumn.getId(), columnIdColumn.getName(), columnIdColumn.getWidth(),
         getXViewerAlign(columnIdColumn.getAlign()), columnIdColumn.isVisible(),
         SortDataType.valueOf(columnIdColumn.getSortDataType()), columnIdColumn.isColumnMultiEdit(),
         columnIdColumn.getDescription());
      this.columnIdColumn = columnIdColumn;
      this.services = services;
      setInheritParent(columnIdColumn.isInheritParent());
      setActionRollup(columnIdColumn.isActionRollup());
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public AtsColumnIdUI copy() {
      AtsColumnIdUI newXCol = new AtsColumnIdUI(columnIdColumn, services);
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public Long getKey(Object obj) {
      Long result = 0L;
      if (obj instanceof IAtsWorkItem) {
         result = ((IAtsObject) obj).getUuid();
      }
      return result;
   }

   @Override
   public void populateCachedValues(Collection<?> objects, Map<Long, String> preComputedValueMap) {
      for (Object element : objects) {
         String value = "";
         try {
            if (element instanceof IAtsObject) {
               value = services.getColumnService().getColumnText(columnIdColumn.getColumnId(), (IAtsObject) element);
            }
         } catch (Exception ex) {
            value = LogUtil.getCellExceptionString(ex);
         }
         preComputedValueMap.put(getKey(element), value);
      }
   }

   @Override
   public String getText(Object obj, Long key, String cachedValue) {
      return cachedValue;
   }

   public static XViewerAlign getXViewerAlign(ColumnAlign align) {
      if (align == ColumnAlign.Center) {
         return XViewerAlign.Center;
      }
      if (align == ColumnAlign.Right) {
         return XViewerAlign.Right;
      }
      return XViewerAlign.Left;
   }

}
