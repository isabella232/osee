/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.client.internal.user;

import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.UuidIdentity;
import org.eclipse.osee.framework.skynet.core.User;

/**
 * @author Donald G. Dunne
 */
public class AtsUser implements IAtsUser {

   private User user;

   public AtsUser(User user) {
      this.user = user;
   }

   @Override
   public String getName() {
      return user.getName();
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 0;
      int userIdHashCode = 0;
      try {
         userIdHashCode = getUserId() == null ? 0 : getUserId().hashCode();
      } catch (OseeCoreException ex) {
         // Do nothing;
      }
      result = prime * result + userIdHashCode;
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      try {
         String objUserId = null;
         if (obj instanceof IAtsUser) {
            objUserId = ((IAtsUser) obj).getUserId();
         } else if (obj instanceof User) {
            objUserId = ((User) obj).getUserId();
         } else {
            return false;
         }
         String thisUserId = getUserId();
         if (thisUserId == null) {
            if (objUserId != null) {
               return false;
            }
         } else if (!thisUserId.equals(objUserId)) {
            return false;
         }
      } catch (OseeCoreException ex) {
         return false;
      }
      return true;
   }

   @Override
   public boolean matches(UuidIdentity... identities) {
      for (UuidIdentity identity : identities) {
         if (equals(identity)) {
            return true;
         }
      }
      return false;
   }

   @Override
   public String getUserId() throws OseeCoreException {
      return user.getUserId();
   }

   @Override
   public String getEmail() throws OseeCoreException {
      return user.getEmail();
   }

   @Override
   public boolean isActive() throws OseeCoreException {
      return user.isActive();
   }

   @Override
   public String toString() {
      try {
         return getName();
      } catch (Exception ex) {
         return "Exception: " + ex.getLocalizedMessage();
      }
   }

   @Override
   public String toStringWithId() {
      return String.format("[%s][%d]", getName(), getUuid());
   }

   @Override
   public ArtifactId getStoreObject() {
      return user;
   }

   @Override
   public void setStoreObject(ArtifactId artifact) {
      if (artifact instanceof User) {
         this.user = (User) artifact;
      }
   }

   @Override
   public Long getUuid() {
      return Long.valueOf(user.getArtId());
   }

}
