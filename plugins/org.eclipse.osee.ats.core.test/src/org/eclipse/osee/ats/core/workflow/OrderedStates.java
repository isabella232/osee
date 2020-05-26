/*********************************************************************
 * Copyright (c) 2011 Boeing
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

package org.eclipse.osee.ats.core.workflow;

import java.util.List;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.ats.api.workdef.StateTypeAdapter;

/**
 * @author Donald G. Dunne
 */
public class OrderedStates extends StateTypeAdapter {

   public static OrderedStates One = new OrderedStates("One", StateType.Working);
   public static OrderedStates Two = new OrderedStates("Two", StateType.Working);
   public static OrderedStates Three = new OrderedStates("Three", StateType.Working);
   public static OrderedStates Four = new OrderedStates("Four", StateType.Working);
   public static OrderedStates Five = new OrderedStates("Five", StateType.Working);
   public static OrderedStates Six = new OrderedStates("Six", StateType.Working);
   public static OrderedStates Cancelled = new OrderedStates("Cancelled", StateType.Cancelled);
   public static OrderedStates Completed = new OrderedStates("Completed", StateType.Completed);

   public OrderedStates(String pageName, StateType StateType) {
      super(OrderedStates.class, pageName, StateType);
   }

   public static OrderedStates valueOf(String pageName) {
      return StateTypeAdapter.valueOfPage(OrderedStates.class, pageName);
   }

   public static List<OrderedStates> values() {
      return StateTypeAdapter.pages(OrderedStates.class);
   }

}
