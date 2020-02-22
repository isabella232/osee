/*********************************************************************
 * Copyright (c) 2014 Boeing
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

package org.eclipse.osee.ats.api.config;

import org.eclipse.osee.ats.api.column.AtsValueColumn;
import org.eclipse.osee.ats.api.util.ColumnType;
import org.eclipse.osee.framework.core.data.AttributeTypeGeneric;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Donald G. Dunne
 */
public class AtsAttributeValueColumn extends AtsValueColumn {

   private AttributeTypeGeneric<?> attributeType;

   public AtsAttributeValueColumn() {
      // For JaxRs Instantiation
   }

   public AtsAttributeValueColumn(AttributeTypeGeneric<?> attributeType, String id, String name, int width, String align, boolean show, ColumnType sortDataType, boolean multiColumnEditable, String description, Boolean actionRollup, Boolean inheritParent) {
      super(id, name, width, align, show, sortDataType, multiColumnEditable, description, actionRollup, inheritParent);
      this.attributeType = attributeType;
   }

   public AtsAttributeValueColumn(AttributeTypeGeneric<?> attributeType) {
      // For JaxRs Instantiation
      this.attributeType = attributeType;
   }

   public AttributeTypeGeneric<?> getAttributeType() {
      return attributeType;
   }

   public void setAttributeType(AttributeTypeGeneric<?> attributeType) {
      this.attributeType = attributeType;
   }

   @Override
   public String toString() {
      return "AtsAttributeValueColumn [name=" + getName() + ", namespace=" + getNamespace() + ", attributeType=" + attributeType + "]";
   }

   @Override
   public String getId() {
      String result = null;
      if (Strings.isValid(super.getId())) {
         result = super.getId();
      } else if (attributeType != null) {
         result = attributeType.getName();
      }
      return result;
   }
}