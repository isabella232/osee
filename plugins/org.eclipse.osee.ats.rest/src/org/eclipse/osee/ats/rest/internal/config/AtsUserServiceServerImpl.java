/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.rest.internal.config;

import java.util.Collection;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.data.AtsUserGroups;
import org.eclipse.osee.ats.api.user.AtsCoreUsers;
import org.eclipse.osee.ats.api.user.AtsUser;
import org.eclipse.osee.ats.core.users.AbstractAtsUserService;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.search.QueryBuilder;

/**
 * @author Donald G. Dunne
 */
public class AtsUserServiceServerImpl extends AbstractAtsUserService {

   private OrcsApi orcsApi;
   ArtifactReadable atsAdminArt;

   public void setOrcsApi(OrcsApi orcsApi) {
      this.orcsApi = orcsApi;
   }

   @Override
   public String getCurrentUserId() {
      return SystemUser.OseeSystem.getUserId();
   }

   @Override
   public AtsUser getCurrentUser() {
      return AtsCoreUsers.SYSTEM_USER;
   }

   @Override
   public AtsUser getCurrentUserNoCache() {
      return getCurrentUser();
   }

   @Override
   public boolean isAtsAdmin(AtsUser user) {
      if (atsAdminArt == null) {
         atsAdminArt = getArtifact(AtsUserGroups.AtsAdmin);
      }
      return atsAdminArt.areRelated(CoreRelationTypes.Users_User, getArtifact((IAtsObject) user));
   }

   private ArtifactReadable getArtifact(IAtsObject atsObject) {
      if (atsObject.getStoreObject() instanceof ArtifactReadable) {
         return (ArtifactReadable) atsObject.getStoreObject();
      }
      return getArtifact(atsObject.getArtifactId());
   }

   private ArtifactReadable getArtifact(ArtifactId artifactId) {
      return getQuery().andId(artifactId).getResults().getExactlyOne();
   }

   private ArtifactReadable getArtifactOrSentinel(ArtifactId artifactId) {
      return getQuery().andId(artifactId).getResults().getAtMostOneOrDefault(ArtifactReadable.SENTINEL);
   }

   private QueryBuilder getQuery() {
      return orcsApi.getQueryFactory().fromBranch(CoreBranches.COMMON);
   }

   @Override
   public boolean isAtsAdmin(boolean useCache) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean isAtsAdmin() {
      throw new UnsupportedOperationException();
   }

   public static AtsUser valueOf(ArtifactReadable userArt) {
      AtsUser atsUser = new AtsUser();
      atsUser.setName(userArt.getName());
      atsUser.setStoreObject(userArt);
      atsUser.setUserId(userArt.getSoleAttributeAsString(CoreAttributeTypes.UserId, ""));
      atsUser.setEmail(userArt.getSoleAttributeAsString(CoreAttributeTypes.Email, ""));
      atsUser.setActive(userArt.getSoleAttributeValue(CoreAttributeTypes.Active, true));
      atsUser.setId(userArt.getId());
      atsUser.getLoginIds().addAll(userArt.getAttributeValues(CoreAttributeTypes.LoginId));
      for (ArtifactToken userGroup : userArt.getRelated(CoreRelationTypes.Users_Artifact)) {
         atsUser.getUserGroups().add(userGroup);
      }
      return atsUser;
   }

   @Override
   public Collection<AtsUser> getUsers() {
      return configurationService.getConfigurations().getUsers();
   }

   @Override
   public boolean isLoadValid() {
      return true;
   }

   @Override
   protected AtsUser loadUserByUserId(String userId) {
      ArtifactReadable userArt =
         getQuery().andTypeEquals(CoreArtifactTypes.User).andAttributeIs(CoreAttributeTypes.UserId,
            userId).getResults().getAtMostOneOrDefault(ArtifactReadable.SENTINEL);
      if (userArt.isValid()) {
         return valueOf(userArt);
      }
      return null;
   }

   @Override
   protected AtsUser loadUserByUserName(String name) {
      ArtifactReadable userArt =
         getQuery().andTypeEquals(CoreArtifactTypes.User).andNameEquals(name).getResults().getAtMostOneOrDefault(
            ArtifactReadable.SENTINEL);
      if (userArt.isValid()) {
         return valueOf(userArt);
      }
      return null;
   }

   @Override
   protected AtsUser loadUserByUserId(Long accountId) {
      AtsUser user = null;
      ArtifactId userArt = getArtifactOrSentinel(ArtifactId.valueOf(accountId));
      if (userArt.isValid()) {
         user = valueOf((ArtifactReadable) userArt);
      }
      return user;
   }

   @Override
   public AtsUser getUserById(ArtifactId id) {
      ArtifactReadable userArt = null;
      if (id instanceof ArtifactReadable) {
         userArt = (ArtifactReadable) id;
      } else {
         userArt = getQuery().andId(id).getResults().getExactlyOne();
      }
      return valueOf(userArt);
   }

   @Override
   public void setCurrentUser(AtsUser user) {
      // TBD
   }

   @Override
   public void clearCaches() {
      // do nothing
   }
}
