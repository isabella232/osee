/*********************************************************************
 * Copyright (c) 2017 Boeing
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

package org.eclipse.osee.framework.core.data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.jdk.core.type.BaseId;
import org.eclipse.osee.framework.jdk.core.type.Id;
import org.eclipse.osee.framework.jdk.core.type.IdSerializer;

/**
 * @author Ryan D. Brooks
 */
@JsonSerialize(using = IdSerializer.class)
public interface UserId extends ArtifactId {
   UserId SENTINEL = valueOf(Id.SENTINEL);

   public static UserId valueOf(String id) {
      if (id == null) {
         return SystemUser.Anonymous;
      }
      return valueOf(Long.valueOf(id));
   }

   public static UserId valueOf(ArtifactId id) {
      return valueOf(id.getId());
   }

   public static UserId valueOf(Long id) {
      final class UserIdImpl extends BaseId implements UserId {
         public UserIdImpl(Long id) {
            super(id);
         }
      }
      return new UserIdImpl(id);
   }
}