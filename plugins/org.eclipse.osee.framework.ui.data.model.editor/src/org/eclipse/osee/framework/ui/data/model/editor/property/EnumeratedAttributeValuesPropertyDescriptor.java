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
package org.eclipse.osee.framework.ui.data.model.editor.property;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.attribute.OseeEnumTypeManager;
import org.eclipse.osee.framework.ui.data.model.editor.internal.Activator;
import org.eclipse.osee.framework.ui.plugin.views.property.PropertyId;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;

/**
 * @author Roberto E. Escobar
 */
public class EnumeratedAttributeValuesPropertyDescriptor extends ComboBoxPropertyDescriptor {

   private static final List<String> items = new ArrayList<String>();
   static {
      try {
         items.addAll(OseeEnumTypeManager.getAllTypeNames());
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   public EnumeratedAttributeValuesPropertyDescriptor(PropertyId propertyId) {
      super(propertyId, propertyId.getDisplayName(), items.toArray(new String[items.size()]));
      setCategory(propertyId.getCategoryName());
   }

   public static Integer fromModel(Object object) {
      String value = (String) object;
      int index = items.indexOf(value);
      if (index < 0) {
         items.add(value);
         index = items.size() - 1;
      }
      return index;
   }

   public static String toModel(Object object) {
      if (object != null) {
         int index = ((Integer) object).intValue();
         if (index > -1) {
            return items.get(index);
         }
      }
      return null;
   }
}
