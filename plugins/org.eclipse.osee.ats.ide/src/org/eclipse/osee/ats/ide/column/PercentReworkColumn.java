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

package org.eclipse.osee.ats.ide.column;

import org.eclipse.nebula.widgets.xviewer.core.model.SortDataType;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerAlign;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.ide.util.xviewer.column.XViewerAtsAttributeValueColumn;
import org.eclipse.osee.ats.ide.world.WorldXViewerFactory;

/**
 * @author Donald G. Dunne
 */
public class PercentReworkColumn extends XViewerAtsAttributeValueColumn {

   public static PercentReworkColumn instance = new PercentReworkColumn();

   public static PercentReworkColumn getInstance() {
      return instance;
   }

   private PercentReworkColumn() {
      super(AtsAttributeTypes.PercentRework, WorldXViewerFactory.COLUMN_NAMESPACE + ".percentRework",
         AtsAttributeTypes.PercentRework.getUnqualifiedName(), 40, XViewerAlign.Center, false, SortDataType.Percent,
         false, "");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public PercentReworkColumn copy() {
      PercentReworkColumn newXCol = new PercentReworkColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

}
