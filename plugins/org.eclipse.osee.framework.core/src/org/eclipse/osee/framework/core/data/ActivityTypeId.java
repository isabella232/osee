/*******************************************************************************
 * Copyright (c) 2017 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.data;

import org.eclipse.osee.framework.jdk.core.type.BaseId;
import org.eclipse.osee.framework.jdk.core.type.Id;

/**
 * @author Ryan D. Brooks
 */
public interface ActivityTypeId extends Id {
   ActivityTypeId SENTINEL = valueOf(Id.SENTINEL);

   public static ActivityTypeId valueOf(String id) {
      return Id.valueOf(id, ActivityTypeId::valueOf);
   }

   public static ActivityTypeId valueOf(Long id) {
      final class ActivityTypeIdImpl extends BaseId implements ActivityTypeId {
         public ActivityTypeIdImpl(Long id) {
            super(id);
         }
      }
      return new ActivityTypeIdImpl(id);
   }
}