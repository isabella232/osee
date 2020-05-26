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
public class TwoStates extends StateTypeAdapter {

   public static TwoStates Endorse = new TwoStates("Endorse", StateType.Working);
   public static TwoStates Cancelled = new TwoStates("Cancelled", StateType.Cancelled);
   public static TwoStates Completed = new TwoStates("Completed", StateType.Completed);

   public TwoStates(String pageName, StateType StateType) {
      super(TwoStates.class, pageName, StateType);
   }

   public static TwoStates valueOf(String pageName) {
      return StateTypeAdapter.valueOfPage(TwoStates.class, pageName);
   }

   public static List<TwoStates> values() {
      return StateTypeAdapter.pages(TwoStates.class);
   }

}
