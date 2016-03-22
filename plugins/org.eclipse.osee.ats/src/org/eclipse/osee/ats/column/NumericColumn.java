/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.column;

import org.eclipse.nebula.widgets.xviewer.core.model.XViewerAlign;
import org.eclipse.nebula.widgets.xviewer.core.model.SortDataType;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsAttributeValueColumn;
import org.eclipse.osee.framework.core.data.IAttributeType;

/**
 * @author Donald G. Dunne
 */
public class NumericColumn extends XViewerAtsAttributeValueColumn {

   public static NumericColumn numeric1 = new NumericColumn(AtsAttributeTypes.Numeric1);
   public static NumericColumn numeric2 = new NumericColumn(AtsAttributeTypes.Numeric2);

   public static NumericColumn getNumeric1Instance() {
      return numeric1;
   }

   public static NumericColumn getNumeric2Instance() {
      return numeric2;
   }

   public NumericColumn(IAttributeType attributeType) {
      super(attributeType, 40, XViewerAlign.Left, false, SortDataType.Float, true, "");
   }

   private NumericColumn() {
      super();
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public NumericColumn copy() {
      NumericColumn newXCol = new NumericColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

}
