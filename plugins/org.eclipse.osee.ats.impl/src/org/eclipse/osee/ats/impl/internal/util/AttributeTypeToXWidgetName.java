package org.eclipse.osee.ats.impl.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.EnumEntry;

/**
 * @author Donald G. Dunne
 */
public class AttributeTypeToXWidgetName {

   public static String getXWidgetName(OrcsApi orcsApi, IAttributeType attributeType) throws OseeCoreException {
      int minOccurrence = orcsApi.getOrcsTypes().getAttributeTypes().getMinOccurrences(attributeType);
      int maxOccurrence = orcsApi.getOrcsTypes().getAttributeTypes().getMaxOccurrences(attributeType);
      String xWidgetName = "";
      String baseType = orcsApi.getOrcsTypes().getAttributeTypes().getBaseAttributeTypeId(attributeType);
      if (baseType != null) {
         baseType = baseType.toLowerCase();
         if (attributeType.equals(CoreAttributeTypes.AccessContextId)) {
            xWidgetName = "XTextFlatDam";
         } else if (baseType.contains("enum")) {
            if (maxOccurrence == 1) {
               xWidgetName =
                  "XComboDam(" + Collections.toString(",", getEnumerationValues(orcsApi, attributeType)) + ")";
            } else {
               xWidgetName =
                  "XSelectFromMultiChoiceDam(" + Collections.toString(",", getEnumerationValues(orcsApi, attributeType)) + ")";
            }
         } else if (baseType.contains("boolean")) {
            if (minOccurrence == 1) {
               xWidgetName = "XCheckBoxDam";
            } else {
               xWidgetName = "XComboBooleanDam";
            }
         } else if (baseType.contains("date")) {
            xWidgetName = "XDateDam";
         } else if (baseType.contains("integer")) {
            xWidgetName = "XIntegerDam";
         } else if (baseType.contains("floating")) {
            xWidgetName = "XFloatDam";
         } else if (baseType.contains("binary")) {
            xWidgetName = "XLabelDam";
         } else if (baseType.contains("branchreference")) {
            xWidgetName = "XBranchSelectWidget";
         } else if (baseType.contains("artifactreference")) {
            xWidgetName = "XListDropViewWithSave";
         } else if (baseType.contains("string")) {
            if (maxOccurrence == 1) {
               xWidgetName = "XTextDam";
            } else {
               xWidgetName = "XStackedDam";
            }
         } else {
            xWidgetName = "XStackedDam";
         }
      }
      return xWidgetName;
   }

   private static Collection<String> getEnumerationValues(OrcsApi orcsApi, IAttributeType attributeType) {
      List<String> values = new ArrayList<>();
      for (EnumEntry entry : orcsApi.getOrcsTypes().getAttributeTypes().getEnumType(attributeType).values()) {
         values.add(entry.getName());
      }
      return values;
   }
}
