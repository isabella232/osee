/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.orcs.core.internal.attribute.primitives;

import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.core.annotations.OseeAttribute;

/**
 * @author Ryan D. Brooks
 */
@OseeAttribute("FloatingPointAttribute")
public class FloatingPointAttribute extends CharacterBackedAttribute<Double> {
   public static final String NAME = FloatingPointAttribute.class.getSimpleName();
   private static final Double DEFAULT_DOUBLE = Double.MIN_VALUE;

   public FloatingPointAttribute(Long id) {
      super(id);
   }

   @Override
   public Double convertStringToValue(String value) {
      Double toReturn = null;
      if (isValidDouble(value)) {
         toReturn = Double.valueOf(value);
      } else {
         toReturn = getDefaultValue();
      }
      return toReturn;
   }

   public Double getDefaultValue() {
      Double toReturn = DEFAULT_DOUBLE;
      String defaultValue = getDefaultValueFromMetaData();
      if (isValidDouble(defaultValue)) {
         toReturn = Double.valueOf(defaultValue);
      }
      return toReturn;
   }

   private boolean isValidDouble(String value) {
      boolean result = false;
      if (Strings.isValid(value)) {
         try {
            Double.parseDouble(value);
            result = true;
         } catch (NumberFormatException ex) {
            // Do Nothing;
         }
      }
      return result;
   }
}