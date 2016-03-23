/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.internal.column.ev;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.config.AtsAttributeValueColumn;
import org.eclipse.osee.ats.api.config.AtsConfigurations;
import org.eclipse.osee.ats.core.column.ActionableItemsColumn;
import org.eclipse.osee.ats.core.column.AssigneeColumn;
import org.eclipse.osee.ats.core.column.AtsAttributeValueColumnHandler;
import org.eclipse.osee.ats.core.column.AtsColumnId;
import org.eclipse.osee.ats.core.column.AtsColumnToken;
import org.eclipse.osee.ats.core.column.AtsIdColumn;
import org.eclipse.osee.ats.core.column.AttributeColumn;
import org.eclipse.osee.ats.core.column.IAtsColumn;
import org.eclipse.osee.ats.core.column.IAtsColumnId;
import org.eclipse.osee.ats.core.column.IAtsColumnProvider;
import org.eclipse.osee.ats.core.column.IAtsColumnService;
import org.eclipse.osee.ats.core.column.PercentCompleteTasksColumn;
import org.eclipse.osee.ats.core.column.StateColumn;
import org.eclipse.osee.ats.core.column.TitleColumn;
import org.eclipse.osee.ats.core.column.UuidColumn;
import org.eclipse.osee.ats.core.internal.column.TeamColumn;
import org.eclipse.osee.framework.core.data.IAttributeType;

/**
 * @author Donald G. Dunne
 */
public class AtsColumnService implements IAtsColumnService {

   public static final String CELL_ERROR_PREFIX = "!Error";
   private Map<String, IAtsColumn> columnIdToAtsColumn;
   private final IAtsServices services;

   public AtsColumnService(IAtsServices services) {
      this.services = services;
   }

   @Override
   public IAtsColumn getColumn(AtsConfigurations configurations, String id) {
      if (columnIdToAtsColumn == null) {
         columnIdToAtsColumn = new HashMap<String, IAtsColumn>();
      }
      IAtsColumn column = columnIdToAtsColumn.get(id);
      if (column == null) {
         for (AtsAttributeValueColumn attrCol : configurations.getViews().getAttrColumns()) {
            if (id.equals(attrCol.getId())) {
               column = new AtsAttributeValueColumnHandler(attrCol, services);
               add(id, column);
               break;
            }
         }
      }
      if (column == null) {
         if (id.equals(AtsColumnId.ActionableItem.getId())) {
            column = new ActionableItemsColumn(services);
         } else if (id.equals(AtsColumnId.LegacyPcrId.getId())) {
            column = new AtsAttributeValueColumnHandler(AtsColumnToken.LegacyPcrIdColumn, services);
         } else if (id.equals(AtsColumnId.Team.getId())) {
            column = new TeamColumn(services.getReviewService());
         } else if (id.equals(AtsColumnId.Assignees.getId())) {
            column = new AssigneeColumn(services);
         } else if (id.equals(AtsColumnId.AtsId.getId())) {
            column = new AtsIdColumn(services);
         } else if (id.equals(AtsColumnId.ActivityId.getId())) {
            column = new ActivityIdColumn(services.getEarnedValueServiceProvider());
         } else if (id.equals(AtsColumnId.State.getId())) {
            column = new StateColumn(services);
         } else if (id.equals(AtsColumnId.PercentCompleteWorkflow.getId())) {
            column = new AtsAttributeValueColumnHandler(AtsColumnToken.PercentCompleteWorkflowColumn, services);
         } else if (id.equals(AtsColumnId.PercentCompleteTasks.getId())) {
            column = new PercentCompleteTasksColumn(services);
         } else if (id.equals(AtsColumnId.WorkPackageName.getId())) {
            column = new WorkPackageNameColumn(services.getEarnedValueServiceProvider());
         } else if (id.equals(AtsColumnId.WorkPackageId.getId())) {
            column = new WorkPackageIdColumn(services.getEarnedValueServiceProvider());
         } else if (id.equals(AtsColumnId.WorkPackageType.getId())) {
            column = new WorkPackageTypeColumn(services.getEarnedValueServiceProvider());
         } else if (id.equals(AtsColumnId.WorkPackageProgram.getId())) {
            column = new WorkPackageProgramColumn(services.getEarnedValueServiceProvider());
         } else if (id.equals(AtsColumnId.WorkPackageGuid.getId())) {
            column = new WorkPackageGuidColumn(services.getEarnedValueServiceProvider());
         } else if (id.equals(AtsColumnId.Name.getId())) {
            column = new TitleColumn(services);
         } else if (id.equals(AtsColumnId.Title.getId())) {
            column = new TitleColumn(services);
         } else if (id.equals(AtsColumnId.Uuid.getId())) {
            column = new UuidColumn(services);
         }
      }
      // Add columns provided through OSGI services
      if (column == null) {
         for (IAtsColumnProvider provider : AtsColumnProviderCollector.getColumnProviders()) {
            column = provider.getColumn(id, services);
            if (column != null) {
               break;
            }
         }
      }
      // Add columns defined as attribute, if valid attribute
      if (column == null) {
         if (id.startsWith("attribute.")) {
            IAttributeType attrType = services.getStoreService().getAttributeType(id.replaceFirst("attribute\\.", ""));
            if (attrType != null) {
               column = new AttributeColumn(services, attrType);
            }
         }
      }
      // Add to cache even if not found so don't need to look again
      add(id, column);
      return column;
   }

   @Override
   public String getColumnText(IAtsColumnId column, IAtsObject atsObject) {
      return getColumnText(column.getId(), atsObject);
   }

   @Override
   public String getColumnText(String id, IAtsObject atsObject) {
      String result = "";
      IAtsColumn column = getColumn(id);
      if (column == null) {
         result = "column not supported";
      } else {
         result = column.getColumnText(atsObject);
      }
      return result;
   }

   @Override
   public String getColumnText(AtsConfigurations configurations, IAtsColumnId column, IAtsObject atsObject) {
      return getColumnText(configurations, column.getId(), atsObject);
   }

   @Override
   public String getColumnText(AtsConfigurations configurations, String id, IAtsObject atsObject) {
      String result = "";
      IAtsColumn column = getColumn(configurations, id);
      if (column == null) {
         result = "column not supported";
      } else {
         result = column.getColumnText(atsObject);
      }
      return result;
   }

   @Override
   public IAtsColumn getColumn(String id) {
      return getColumn(services.getConfigurations(), id);
   }

   @Override
   public void add(String id, IAtsColumn column) {
      columnIdToAtsColumn.put(id, column);
   }

   @Override
   public IAtsColumn getColumn(IAtsColumnId columnId) {
      return getColumn(columnId.getId());
   }

}