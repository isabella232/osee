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

package org.eclipse.osee.disposition.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;

/**
 * @author Angel Avila
 */

@JsonDeserialize(using = DispoSummarySeverityDeserializer.class)
public enum DispoSummarySeverity {
   IGNORE("Ignore"),
   WARNING("Warning"),
   ERROR("Error"),
   UPDATE("Update"),
   NEW("New");

   private String name;

   DispoSummarySeverity(String name) {
      this.name = name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getName() {
      return name;
   }

   public static DispoSummarySeverity forVal(String name) {
      for (DispoSummarySeverity value : values()) {
         if (value.name.equals(name)) {
            return value;
         }
      }
      throw new OseeArgumentException("No enum with name [%s]", name);
   }

   @JsonValue
   public String toValue() {
      return name;
   }
}
