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

package org.eclipse.osee.framework.jdk.core.type;

/**
 * This class implements an double that can be passed around and modified through a group of methods. It also allows the
 * double value to be retrieved then post incremented automatically. This is a nice functionality where a common counter
 * needs to be used within calling methods, but it is not convenient to return the latest index via the return value.
 * 
 * @author Donald G. Dunne
 */
public class MutableDouble {
   private double value;

   public MutableDouble(double value) {
      this.value = value;
   }

   public double getValue() {
      return value;
   }

   public double getValueAndInc(double byAmmount) {
      return value += byAmmount;
   }

   public void setValue(double value) {
      this.value = value;
   }

   @Override
   public String toString() {
      return Double.toString(value);
   }
}
