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

import java.io.InputStream;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.io.xml.XmlTextInputStream;
import org.eclipse.osee.orcs.core.annotations.OseeAttribute;

/**
 * @author Ryan D. Brooks
 */
@OseeAttribute("StringAttribute")
public class StringAttribute extends CharacterBackedAttribute<String> {
   public static final String NAME = StringAttribute.class.getSimpleName();

   public StringAttribute(Long id) {
      super(id);
   }

   @Override
   public String convertStringToValue(String value) {
      return value;
   }

   @Override
   public String getDisplayableString() {
      String toReturn = null;
      InputStream inputStream = null;
      try {
         if (getAttributeType().equals(CoreAttributeTypes.WordTemplateContent)) {
            inputStream = new XmlTextInputStream(getValue());
            toReturn = Lib.inputStreamToString(inputStream);
         } else {
            toReturn = getValue();
         }
      } catch (Exception ex) {
         OseeCoreException.wrapAndThrow(ex);
      } finally {
         Lib.close(inputStream);
      }
      return toReturn;
   }
}
