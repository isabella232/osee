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
@OseeAttribute("IntegerAttribute")
public class IntegerAttribute extends CharacterBackedAttribute<Integer> {

   private static final Integer DEFAULT_INTEGER = Integer.MIN_VALUE;

   @Override
   public Integer getValue() throws OseeCoreException {
      return convertStringToValue(getDataProxy().getValueAsString());
   }

   @Override
   public boolean subClassSetValue(Integer value) throws OseeCoreException {
      if (value == null) {
         throw new OseeArgumentException("Attribute value was null");
      }
      return getDataProxy().setValue(String.valueOf(value));
   }

   @Override
   protected Integer convertStringToValue(String value) throws OseeCoreException {
      Integer toReturn = null;
      if (isValidInteger(value)) {
         toReturn = Integer.valueOf(value);
      } else {
         toReturn = getDefaultValue();
      }
      return toReturn;
   }

   public Integer getDefaultValue() throws OseeCoreException {
      Integer toReturn = DEFAULT_INTEGER;
      String defaultValue = getDefaultValueFromMetaData();
      if (isValidInteger(defaultValue)) {
         toReturn = Integer.valueOf(defaultValue);
      }
      return toReturn;
   }

   private boolean isValidInteger(String value) {
      boolean result = false;
      if (Strings.isValid(value)) {
         try {
            Integer.parseInt(value);
            result = true;
         } catch (NumberFormatException ex) {
            // Do Nothing;
         }
      }
      return result;
   }
}