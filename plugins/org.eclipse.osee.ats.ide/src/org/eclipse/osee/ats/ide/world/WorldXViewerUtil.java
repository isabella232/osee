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
package org.eclipse.osee.ats.ide.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.nebula.widgets.xviewer.core.model.SortDataType;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.osee.ats.api.config.AtsAttributeValueColumn;
import org.eclipse.osee.ats.ide.column.StateAssigneesColumn;
import org.eclipse.osee.ats.ide.column.StateCompletedColumn;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.ats.ide.internal.AtsClientService;
import org.eclipse.osee.ats.ide.util.AtsEditors;
import org.eclipse.osee.ats.ide.util.xviewer.column.XViewerAtsAttributeValueColumn;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IUserGroupArtifactToken;
import org.eclipse.osee.framework.core.exception.OseeTypeDoesNotExist;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.skynet.SkynetXViewerFactory;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.skynet.SkynetXViewerFactory;

/**
 * @author Donald G. Dunne
 */
public class WorldXViewerUtil {

   public static void registerOtherColumns(SkynetXViewerFactory factory) {
      registerAtsAttributeColumns(factory);
      registerPluginColumns(factory);
      registerStateColumns(factory);
      registerConfigurationsColumns(factory);
   }

   public static void registerPluginColumns(SkynetXViewerFactory factory) {
      // Register any columns from other plugins
      try {
         for (IAtsWorldEditorItem item : AtsWorldEditorItems.getItems()) {
            boolean found = false;
            Collection<IUserGroupArtifactToken> userGroups = item.getUserGroups();
            for (ArtifactId myUserGroup : AtsClientService.get().getUserService().getCurrentUser().getUserGroups()) {
               if (userGroups.contains(myUserGroup)) {
                  found = true;
                  break;
               }
            }
            if (found) {
               for (XViewerColumn xCol : item.getXViewerColumns()) {
                  factory.registerColumns(xCol);
               }
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   public static void registerAtsAttributeColumns(SkynetXViewerFactory factory) {
      // Register all ats.* attribute columns
      if (isInUserGroup(factory)) {
         try {
            for (AttributeType attributeType : AttributeTypeManager.getAllTypes()) {
               if (attributeType.getName().startsWith("ats.")) {
                  factory.registerColumns(SkynetXViewerFactory.getAttributeColumn(attributeType));
               }
            }
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
   }

   public static void registerConfigurationsColumns(SkynetXViewerFactory factory) {
      if (isInUserGroup(factory)) {
         List<XViewerAtsAttributeValueColumn> configColumns = getConfigurationColumns();
         for (XViewerAtsAttributeValueColumn col : configColumns) {
            factory.registerColumns(col);
         }
      }
   }

   public static List<XViewerAtsAttributeValueColumn> getConfigurationColumns() {
      List<AtsAttributeValueColumn> columns =
         AtsClientService.get().getConfigService().getConfigurations().getViews().getAttrColumns();
      List<XViewerAtsAttributeValueColumn> configColumns = new ArrayList<>();
      for (AtsAttributeValueColumn column : columns) {
         try {
            AttributeType attrType = null;
            try {
               attrType = AttributeTypeManager.getTypeById(column.getAttrTypeId());
            } catch (OseeTypeDoesNotExist ex) {
               continue;
            }
            XViewerAtsAttributeValueColumn valueColumn = new XViewerAtsAttributeValueColumn(attrType, column.getWidth(),
               AtsEditors.getXViewerAlign(column.getAlign()), column.isVisible(),
               SortDataType.valueOf(column.getSortDataType()), column.isColumnMultiEdit(), column.getDescription());
            valueColumn.setBooleanNotSetShow(column.getBooleanNotSetShow());
            valueColumn.setBooleanOnFalseShow(column.getBooleanOnFalseShow());
            valueColumn.setBooleanOnTrueShow(column.getBooleanOnTrueShow());
            valueColumn.setActionRollup(column.isActionRollup());
            valueColumn.setInheritParent(column.isInheritParent());
            configColumns.add(valueColumn);

         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
      return configColumns;
   }

   public static void registerStateColumns(SkynetXViewerFactory factory) {
      if (isInUserGroup(factory)) {
         for (String stateName : AtsClientService.get().getConfigService().getConfigurations().getValidStateNames()) {
            factory.registerColumns(new StateAssigneesColumn(stateName));
            factory.registerColumns(new StateCompletedColumn(stateName));
         }
      }
   }

   @SuppressWarnings("unchecked")
   private static boolean isInUserGroup(SkynetXViewerFactory factory) {
      Collection<IUserGroupArtifactToken> userGroups = factory.getUserGroups();
      for (ArtifactId myUserGroup : AtsClientService.get().getUserService().getCurrentUser().getUserGroups()) {
         if (userGroups.contains(myUserGroup)) {
            return true;
         }
      }
      return false;
   }

   public static XViewerColumn getConfigColumn(String columnId, List<XViewerAtsAttributeValueColumn> configCols) {
      for (XViewerAtsAttributeValueColumn col : configCols) {
         if (col.getId().equals(columnId)) {
            return col;
         }
      }
      return null;
   }

   public static void addColumn(SkynetXViewerFactory factory, XViewerColumn taskCol, int width, List<XViewerColumn> sprintCols) {
      if (isInUserGroup(factory)) {
         XViewerColumn newCol = taskCol.copy();
         newCol.setShow(true);
         newCol.setWidth(width);
         factory.registerColumns(newCol);
         sprintCols.add(newCol);
      }
   }

}
