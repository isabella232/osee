/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributostmt:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.disposition.model;

import org.codehaus.jackson.annotate.JsonValue;

/**
 * @author Angel Avila
 */
public enum DispoSetStatus {
   NO_CHANGE(0, "No Change"),
   OK(1, "OK"),
   WARNINGS(2, "Warnings"),
   FAILED(3, "Failed");

   private Integer value;
   private String name;

   DispoSetStatus() {

   }

   DispoSetStatus(int value, String name) {
      this.value = value;
      this.name = name;
   }

   public int getValue() {
      return value;
   }

   @JsonValue
   public String getName() {
      return name;
   }

   public void setValue(Integer value) {
      this.value = value;
   }

   public void setName(String name) {
      this.name = name;
   }

   public boolean isFailed() {
      return value == FAILED.value;
   }
}
