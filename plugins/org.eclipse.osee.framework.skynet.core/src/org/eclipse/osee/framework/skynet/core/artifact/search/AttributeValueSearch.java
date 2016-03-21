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
package org.eclipse.osee.framework.skynet.core.artifact.search;

import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.framework.jdk.core.util.Conditions;

/**
 * @author Robert A. Fisher
 */
public class AttributeValueSearch implements ISearchPrimitive {
   private final IAttributeType attributeType;
   private final String attributeValue;
   private final static String TOKEN = ";";

   public AttributeValueSearch(IAttributeType attributeType, String attributeValue) {
      Conditions.checkNotNull(attributeType, "attributeType");
      Conditions.checkNotNullOrEmpty(attributeValue, "attributeValue");
      this.attributeType = attributeType;
      this.attributeValue = attributeValue;
   }

   @Override
   public String getStorageString() {
      return attributeType.getGuid().toString() + TOKEN + attributeValue;
   }

   public static AttributeValueSearch getPrimitive(String storageString) {
      String[] values = storageString.split(TOKEN);
      if (values.length != 2) {
         throw new IllegalStateException("Value for " + AttributeValueSearch.class.getSimpleName() + " not parsable");
      }

      IAttributeType type = TokenFactory.createAttributeType(Long.valueOf(values[0]), "SearchAttrType");
      return new AttributeValueSearch(type, values[1]);
   }

   @Override
   public void addToQuery(QueryBuilderArtifact builder) {
      builder.and(attributeType, attributeValue, QueryOption.CONTAINS_MATCH_OPTIONS);
   }

   @Override
   public String toString() {
      return "AttributeValueSearch [attributeType=" + attributeType + ", value=" + attributeValue + "]";
   }

}
