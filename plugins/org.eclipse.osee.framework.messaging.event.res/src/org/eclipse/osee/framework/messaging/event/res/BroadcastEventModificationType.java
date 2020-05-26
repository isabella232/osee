/*********************************************************************
 * Copyright (c) 2010 Boeing
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

package org.eclipse.osee.framework.messaging.event.res;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Donald G. Dunne
 */
public class BroadcastEventModificationType {

   private final String guid;
   private static Map<String, BroadcastEventModificationType> guidToEventType =
      new HashMap<>(15);
   public static BroadcastEventModificationType Message = new BroadcastEventModificationType("Aylfa1sC4iArrIaXxugA");
   public static BroadcastEventModificationType Shutdown = new BroadcastEventModificationType("Aylfa1swuASdF_H2OYQA");

   public BroadcastEventModificationType(String guid) {
      this.guid = guid;
      guidToEventType.put(guid, this);
   }

   public static Collection<BroadcastEventModificationType> getTypes() {
      return guidToEventType.values();
   }

   public static BroadcastEventModificationType getType(String guid) {
      return guidToEventType.get(guid);
   }

   public String getGuid() {
      return guid;
   }
}
