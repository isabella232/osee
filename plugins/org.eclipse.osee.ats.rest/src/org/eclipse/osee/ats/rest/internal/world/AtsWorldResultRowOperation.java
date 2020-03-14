/*********************************************************************
 * Copyright (c) 2020 Boeing
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

package org.eclipse.osee.ats.rest.internal.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.demo.AtsDemoOseeTypes;
import org.eclipse.osee.ats.api.query.AtsSearchData;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.jdk.core.result.ResultRow;
import org.eclipse.osee.framework.jdk.core.result.ResultRows;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.orcs.OrcsApi;

/**
 * @author Donald G. Dunne
 */
public class AtsWorldResultRowOperation {

   private final AtsApi atsApi;
   private final OrcsApi orcsApi;
   private final AtsSearchData atsSearchData;
   private boolean teamWfsInState;

   public AtsWorldResultRowOperation(AtsApi atsApi, OrcsApi orcsApi, AtsSearchData atsSearchData) {
      this.atsApi = atsApi;
      this.orcsApi = orcsApi;
      this.atsSearchData = atsSearchData;
      AtsDemoOseeTypes.Action.getName();
   }

   public ResultRows run() {
      ResultRows rows = new ResultRows();
      if (atsSearchData.getCustomizeData() == null || atsSearchData.getCustomizeData().getColumnData().getColumns().isEmpty()) {
         rows.getRd().error("CustomizeData can not be null or empty.");
      }

      Collection<? extends ArtifactToken> artifacts = getArtifacts();
      List<XViewerColumn> showCols = new ArrayList<>();
      for (XViewerColumn col : atsSearchData.getCustomizeData().getColumnData().getColumns()) {
         if (col.isShow()) {
            showCols.add(col);
         }
      }
      for (ArtifactToken art : artifacts) {
         ResultRow row = new ResultRow(art.getId(), atsApi.getAtsBranch().getId());
         rows.add(row);
         for (XViewerColumn col : showCols) {
            if (col.isShow()) {
               addCellData(atsApi, art, row, col);
            }
         }
      }
      return rows;
   }

   private Collection<? extends ArtifactToken> getArtifacts() {
      if (teamWfsInState) {
         Collection<String> teamDefs = Collections.transform(atsSearchData.getTeamDefIds(), String::valueOf);
         Collection<String> stateTypes = Collections.transform(atsSearchData.getStateTypes(), String::valueOf);

         return orcsApi.getQueryFactory().fromBranch(CoreBranches.COMMON).andAttributeIs(
            AtsAttributeTypes.TeamDefinitionReference, teamDefs).andAttributeIs(AtsAttributeTypes.CurrentStateType,
               stateTypes).asArtifacts();
      } else {
         return atsApi.getQueryService().getArtifacts(atsSearchData, null);
      }
   }

   private void addCellData(AtsApi atsApi, ArtifactToken art, ResultRow row, XViewerColumn xCol) {
      String value = "";
      if (art.isOfType(AtsArtifactTypes.AbstractWorkflowArtifact)) {
         IAtsWorkItem workItem = atsApi.getWorkItemService().getWorkItem(art);
         value = atsApi.getColumnService().getColumnText(xCol.getId(), workItem);
      }
      row.addValue(value);
   }

   public void setNew(boolean teamWfsInState) {
      this.teamWfsInState = teamWfsInState;
   }
}