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
package org.eclipse.osee.orcs.core.ds.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.orcs.core.ds.Criteria;
import org.eclipse.osee.orcs.core.ds.QueryOptions;
import org.eclipse.osee.orcs.data.AttributeTypes;

/**
 * @author Roberto E. Escobar
 */
public class CriteriaAttributeKeywords extends Criteria<QueryOptions> {

   private final AttributeTypes attributeTypeCache;
   private final Collection<? extends IAttributeType> attributeType;
   private final Collection<String> values;
   private final QueryOption[] options;
   private final boolean includeAllTypes;

   public CriteriaAttributeKeywords(boolean includeAllTypes, Collection<? extends IAttributeType> attributeType, AttributeTypes attributeTypeCache, Collection<String> values, QueryOption... options) {
      super();
      this.includeAllTypes = includeAllTypes;
      this.attributeTypeCache = attributeTypeCache;
      this.attributeType = attributeType;
      this.values = values;
      this.options = options;
   }

   public CriteriaAttributeKeywords(boolean includeAllTypes, Collection<? extends IAttributeType> attributeType, AttributeTypes attributeTypeCache, String value, QueryOption... options) {
      this(includeAllTypes, attributeType, attributeTypeCache, java.util.Collections.singleton(value), options);
   }

   public boolean isIncludeAllTypes() {
      return includeAllTypes;
   }

   public Collection<? extends IAttributeType> getTypes() {
      return attributeType;
   }

   public Collection<String> getValues() {
      return values;
   }

   public QueryOption[] getOptions() {
      return options;
   }

   @Override
   public void checkValid(QueryOptions options) throws OseeCoreException {
      super.checkValid(options);
      Conditions.checkNotNullOrEmpty(getValues(), "search value");
      Conditions.checkNotNullOrEmpty(getTypes(), "attribute types");
      checkMultipleValues();
      checkNotTaggable();
   }

   @Override
   public String toString() {
      return String.format("CriteriaAttributeKeyword [attributeType=%s, value=%s, options=%s]", attributeType,
         Collections.toString(",", values), Collections.toString(",", Arrays.asList(options)));
   }

   private void checkMultipleValues() throws OseeCoreException {
      if (getTypes().size() > 1 && getValues().size() > 1) {
         throw new OseeArgumentException("Multiple values is not valid with multiple types");
      }
   }

   public void checkNotTaggable() throws OseeCoreException {
      if (!includeAllTypes) {
         ArrayList<String> notTaggable = new ArrayList<String>();
         if (attributeTypeCache != null) {
            for (IAttributeType type : attributeType) {
               if (!attributeTypeCache.isTaggable(type)) {
                  notTaggable.add((attributeTypeCache.getByUuid(type.getGuid())).getName());
               }
            }
            if (!notTaggable.isEmpty()) {
               throw new OseeArgumentException("Attribute types [%s] is not taggable", notTaggable.toString());
            }
         }
      }
   }
}
