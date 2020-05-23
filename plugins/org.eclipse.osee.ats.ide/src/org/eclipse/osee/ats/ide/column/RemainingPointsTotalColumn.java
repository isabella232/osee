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

import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.core.util.PercentCompleteTotalUtil;
import org.eclipse.osee.ats.ide.internal.AtsClientService;
import org.eclipse.osee.ats.ide.world.WorldXViewerFactory;

/**
 * @author Donald G. Dunne
 */
public class RemainingPointsTotalColumn extends AbstractNumericTotalColumn {

   private static final String CALCULATION_STR =
      "Points - (Points * (Percent Complete from Workflow Rollup (Team Wf, Tasks and Reviews) / 100))";
   private static RemainingPointsTotalColumn instance = new RemainingPointsTotalColumn();

   public static RemainingPointsTotalColumn getInstance() {
      return instance;
   }

   private RemainingPointsTotalColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".remainingPointsTotal", "Remaining Points - Total",
         "Points that remain to complete the changes based on percent complete from workflow rollup.", CALCULATION_STR,
         AtsAttributeTypes.Points);
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public RemainingPointsTotalColumn copy() {
      RemainingPointsTotalColumn newXCol = new RemainingPointsTotalColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   protected int getPercentComplete(IAtsWorkItem workItem) {
      return PercentCompleteTotalUtil.getPercentCompleteTotal(workItem, AtsClientService.get());
   }

}
