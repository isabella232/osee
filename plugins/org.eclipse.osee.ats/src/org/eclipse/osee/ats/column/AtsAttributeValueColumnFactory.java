/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.column;

import org.eclipse.osee.ats.api.config.AtsAttributeValueColumn;
import org.eclipse.osee.ats.api.config.ColumnAlign;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsAttributeValueColumn;

public class AtsAttributeValueColumnFactory {

   public static AtsAttributeValueColumn get(String namespace, XViewerAtsAttributeValueColumn inCol) {
      AtsAttributeValueColumn col = new AtsAttributeValueColumn();
      col.setName(inCol.getName());
      col.setNamespace(namespace);
      col.setAttrTypeId(inCol.getAttributeType().getId());
      col.setAttrTypeName(inCol.getAttributeType().getName());
      ColumnAlign colAlign = AtsUtil.getColumnAlign(inCol.getAlign());
      col.setAlign(colAlign);
      col.setColumnMultiEdit(inCol.isMultiColumnEditable());
      col.setDescription(inCol.getDescription());
      col.setSortDataType(inCol.getSortDataType().name());
      col.setBooleanOnTrueShow(inCol.getBooleanOnTrueShow());
      col.setBooleanOnFalseShow(inCol.getBooleanOnFalseShow());
      col.setBooleanNotSetShow(inCol.getBooleanNotSetShow());
      col.setVisible(inCol.isShow());
      col.setWidth(inCol.getWidth());

      return col;
   }

}
