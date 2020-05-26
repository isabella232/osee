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
 * This class implements an integer that can be passed around and modified through a group of methods. It also allows
 * the integer value to be retrieved then post incremented automatically. This is a nice functionality where a common
 * counter needs to be used within calling methods, but it is not convenient to return the latest index via the return
 * value.
 * 
 * @author Robert A. Fisher
 */
public class MutableInteger {
   private int value;

   public MutableInteger(int value) {
      this.value = value;
   }

   public int getValue() {
      return value;
   }

   public int getValueAndInc() {
      return value++;
   }

   public int getValueAndInc(int byAmt) {
      return value += byAmt;
   }

   public void setValue(int value) {
      this.value = value;
   }

   @Override
   public String toString() {
      return Integer.toString(value);
   }
}
