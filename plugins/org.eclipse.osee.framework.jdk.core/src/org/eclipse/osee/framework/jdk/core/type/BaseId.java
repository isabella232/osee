/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/

package org.eclipse.osee.framework.jdk.core.type;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonValue;

/**
 * @author Ryan D. Brooks
 */
public class BaseId implements Id, Cloneable {
   private Long id;

   @JsonCreator
   public BaseId(@JsonProperty("id") Long id) {
      this.id = id;
   }

   @Override
   public int hashCode() {
      return id.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Id) {
         return id.equals(((Id) obj).getId());
      }
      if (obj instanceof Identity<?>) {
         return id.equals(((Identity<?>) obj).getGuid());
      }
      if (obj instanceof Long) {
         return id.equals(obj);
      }
      return false;
   }

   @Override
   public String toString() {
      return String.valueOf(id);
   }

   @JsonValue
   @Override
   public Long getId() {
      return id;
   }

   @Override
   public BaseId clone() {
      try {
         return (BaseId) super.clone();
      } catch (CloneNotSupportedException ex) {
         return null;
      }
   }

   /**
    * Use this method to construct an instance of an Id that is of the same type as the object this is called on (likely
    * a subclass of BaseId). It will set the numeric id to the given value.
    */
   public BaseId clone(Long id) {
      BaseId newId = clone();
      newId.id = id;
      return newId;
   }
}