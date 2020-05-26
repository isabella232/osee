/*********************************************************************
 * Copyright (c) 2016 Boeing
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

package org.eclipse.osee.framework.jdk.core.type;

/**
 * @author Ryan D. Brooks
 */
public class NamedIdBase extends BaseId implements NamedId {
   private String name;

   public NamedIdBase(Long id, String name) {
      super(id);
      this.name = name;
   }

   public NamedIdBase(int id, String name) {
      super(Long.valueOf(id));
      this.name = name;
   }

   public NamedIdBase() {
      super(Id.SENTINEL);
      this.name = Named.SENTINEL;
   }

   @Override
   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   @Override
   public String toString() {
      return name == null ? super.toString() : name;
   }

   public static <T extends Named> T fromName(String name, T[] tokens) {
      for (T token : tokens) {
         if (token.getName().equals(name)) {
            return token;
         }
      }
      throw new OseeArgumentException("Value with name [%s] does not exist", name);
   }

   public static <T extends NamedId> T valueOf(Long id, T[] tokens) {
      for (T token : tokens) {
         if (token.getId().equals(id)) {
            return token;
         }
      }
      throw new OseeArgumentException("Value with id [%s] does not exist", id);
   }
}