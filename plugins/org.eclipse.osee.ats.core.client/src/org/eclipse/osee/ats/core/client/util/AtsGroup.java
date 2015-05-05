/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.client.util;

import org.eclipse.osee.ats.api.data.AtsArtifactToken;
import org.eclipse.osee.framework.core.data.IArtifactToken;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.jdk.core.type.Identifiable;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.UuidIdentity;
import org.eclipse.osee.framework.skynet.core.OseeGroup;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public enum AtsGroup {

   AtsAdmin(AtsArtifactToken.AtsAdmin), // Admin setting upon startup
   AtsTempAdmin(AtsArtifactToken.AtsTempAdmin); // Allows user to temporarily toggle admin; overrides AtsAdmin

   private final OseeGroup group;

   AtsGroup(IArtifactToken token) {
      group = new OseeGroup(token);
   }

   public Artifact getArtifact() throws OseeCoreException {
      return group.getGroupArtifact();
   }

   public void addMember(User user) throws OseeCoreException {
      group.addMember(user);
   }

   public boolean isMember(User user) throws OseeCoreException {
      return group.isMember(user);
   }

   public boolean isCurrentUserMember() throws OseeCoreException {
      return group.isCurrentUserMember();
   }

   public boolean isTemporaryOverride(User user) {
      return group.isTemporaryOverride(user);
   }

   /**
    * Allow user to temporarily override group membership. Value of "member" will be returned until
    * .removeTemporaryOverride() is called
    */
   public void setTemporaryOverride(boolean member) {
      group.setTemporaryOverride(member);
   }

   public void removeTemporaryOverride() {
      group.removeTemporaryOverride();
   }

   public boolean isMember(Identifiable<String> user) {
      for (Artifact art : group.getGroupArtifact().getRelatedArtifacts(CoreRelationTypes.Users_User)) {
         if (art.getGuid().equals(user.getGuid())) {
            return true;
         }
      }
      return false;
   }

   public boolean isMember(UuidIdentity user) {
      for (Artifact art : group.getGroupArtifact().getRelatedArtifacts(CoreRelationTypes.Users_User)) {
         if (art.getUuid() == user.getUuid()) {
            return true;
         }
      }
      return false;
   }
}
