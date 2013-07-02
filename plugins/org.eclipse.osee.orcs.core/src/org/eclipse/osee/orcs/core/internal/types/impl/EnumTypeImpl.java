/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.internal.types.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.framework.core.data.NamedIdentity;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.data.EnumEntry;
import org.eclipse.osee.orcs.data.EnumType;

/**
 * @author Roberto E. Escobar
 */
public final class EnumTypeImpl extends NamedIdentity<Long> implements EnumType {

   private final List<EnumEntry> entries;

   public EnumTypeImpl(Long uuid, String name, List<EnumEntry> entries) {
      super(uuid, name);
      this.entries = entries;
   }

   private List<EnumEntry> getValues() {
      return entries;
   }

   @Override
   public String getDescription() {
      return Strings.emptyString();
   }

   @Override
   public EnumEntry[] values() {
      List<EnumEntry> values = getValues();
      return values.toArray(new EnumEntry[values.size()]);
   }

   @Override
   public EnumEntry getEntryByGuid(String entryGuid) {
      EnumEntry toReturn = null;
      for (EnumEntry entry : getValues()) {
         if (entry.getGuid().equals(entryGuid)) {
            toReturn = entry;
            break;
         }
      }
      return toReturn;
   }

   @Override
   public Set<String> valuesAsOrderedStringSet() {
      Set<String> values = new LinkedHashSet<String>();
      for (EnumEntry oseeEnumEntry : values()) {
         values.add(oseeEnumEntry.getName());
      }
      return values;
   }

   @Override
   public EnumEntry valueOf(int ordinal) throws OseeCoreException {
      EnumEntry toReturn = null;
      for (EnumEntry entry : values()) {
         if (entry.ordinal() == ordinal) {
            toReturn = entry;
         }
      }
      Conditions.checkNotNull(toReturn, "enumEntry", "No enum const [%s].[%s]", getName(), ordinal);
      return toReturn;
   }

   @Override
   public EnumEntry valueOf(String entryName) throws OseeCoreException {
      EnumEntry toReturn = null;
      for (EnumEntry entry : values()) {
         if (entry.getName().equals(entryName)) {
            toReturn = entry;
         }
      }
      Conditions.checkNotNull(toReturn, "enumEntry", "No enum const [%s].[%s]", getName(), entryName);
      return toReturn;
   }

}
