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

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.eclipse.osee.ats.api.data.AtsArtifactToken;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.user.JaxAtsUser;
import org.eclipse.osee.ats.core.users.AbstractAtsUserService;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;

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
   public boolean isAtsAdmin(IAtsUser user) {
      if (atsAdminArt == null) {
         atsAdminArt = orcsApi.getQueryFactory().fromBranch(AtsUtilCore.getAtsBranch()).andIds(
            AtsArtifactToken.AtsAdmin).getResults().getAtMostOneOrNull();
      }
      return atsAdminArt.areRelated(CoreRelationTypes.User_Grouping__Members, (ArtifactReadable) user.getStoreObject());
   }

   private final Supplier<List<IAtsUser>> usersCache =
      Suppliers.memoizeWithExpiration(getConfigurationsSupplier(), 5, TimeUnit.MINUTES);

   private Supplier<List<IAtsUser>> getConfigurationsSupplier() {
      return new Supplier<List<IAtsUser>>() {
         @Override
         public List<IAtsUser> get() {
            List<IAtsUser> users = new ArrayList<>();
            for (ArtifactId art : orcsApi.getQueryFactory().fromBranch(AtsUtilCore.getAtsBranch()).andIsOfType(
               CoreArtifactTypes.User).getResults()) {
               ArtifactReadable userArt = (ArtifactReadable) art;
               JaxAtsUser atsUser = createFromArtifact(userArt);
               userIdToAtsUser.put(atsUser.getUserId(), atsUser);
               nameToAtsUser.put(atsUser.getName(), atsUser);
               users.add(atsUser);
            }
            return users;
         }
      };
   }

   private JaxAtsUser createFromArtifact(ArtifactReadable userArt) {
      JaxAtsUser atsUser = new JaxAtsUser();
      atsUser.setName(userArt.getName());
      atsUser.setStoreObject(userArt);
      atsUser.setUserId(userArt.getSoleAttributeValue(CoreAttributeTypes.UserId, ""));
      atsUser.setEmail(userArt.getSoleAttributeValue(CoreAttributeTypes.Email, ""));
      atsUser.setActive(userArt.getSoleAttributeValue(CoreAttributeTypes.Active, true));
      atsUser.setUuid(userArt.getId());
      return atsUser;
   }

   @Override
   public List<IAtsUser> getUsers() {
      return usersCache.get();
   }

   @Override
   protected IAtsUser loadUserFromDbByUserId(String userId) {
      ArtifactReadable userArt =
         orcsApi.getQueryFactory().fromBranch(AtsUtilCore.getAtsBranch()).andIsOfType(CoreArtifactTypes.User).and(
            CoreAttributeTypes.UserId, userId).getResults().getAtMostOneOrNull();
      if (userArt != null) {
         return createFromArtifact(userArt);
      }
      return null;
   }

   @Override
   protected IAtsUser loadUserFromDbByUserName(String name) {
      ArtifactReadable userArt =
         orcsApi.getQueryFactory().fromBranch(AtsUtilCore.getAtsBranch()).andIsOfType(CoreArtifactTypes.User).and(
            CoreAttributeTypes.Name, name).getResults().getAtMostOneOrNull();
      if (userArt != null) {
         return createFromArtifact(userArt);
      }
      return null;
   }

   @Override
   protected IAtsUser loadUserByAccountId(Long accountId) {
      IAtsUser user = null;
      ArtifactId userArt = orcsApi.getQueryFactory().fromBranch(AtsUtilCore.getAtsBranch()).andUuid(
         accountId).getResults().getAtMostOneOrNull();
      if (userArt != null) {
         user = createFromArtifact((ArtifactReadable) userArt);
      }
      return user;
   }

   @Override
   public IAtsUser getUserByArtifactId(ArtifactId id) {
      ArtifactReadable userArt =
         orcsApi.getQueryFactory().fromBranch(AtsUtilCore.getAtsBranch()).andId(id).getResults().getExactlyOne();
      return createFromArtifact(userArt);
   }
}
