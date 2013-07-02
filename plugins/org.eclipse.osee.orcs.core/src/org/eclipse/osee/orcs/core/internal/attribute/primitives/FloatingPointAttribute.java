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
package org.eclipse.osee.orcs.core.internal.attribute.primitives;

import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.core.annotations.OseeAttribute;
import org.eclipse.osee.orcs.core.internal.attribute.CharacterBackedAttribute;

/**
 * @author Ryan D. Brooks
 */
@OseeAttribute("FloatingPointAttribute")
public class FloatingPointAttribute extends CharacterBackedAttribute<Double> {

   private static final Double DEFAULT_DOUBLE = Double.MIN_VALUE;

   @Override
   public Double getValue() throws OseeCoreException {
      return convertStringToValue(getDataProxy().getValueAsString());
   }

   @Override
   public boolean subClassSetValue(Double value) throws OseeCoreException {
      if (value == null) {
         throw new OseeArgumentException("Attribute value was null");
      }
      return getDataProxy().setValue(String.valueOf(value));
   }

   @Override
   protected Double convertStringToValue(String value) throws OseeCoreException {
      Double toReturn = null;
      if (isValidDouble(value)) {
         toReturn = Double.valueOf(value);
      } else {
         toReturn = getDefaultValue();
      }
      return toReturn;
   }

   public Double getDefaultValue() throws OseeCoreException {
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