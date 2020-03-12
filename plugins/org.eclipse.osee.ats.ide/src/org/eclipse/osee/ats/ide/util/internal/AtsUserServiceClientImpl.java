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
package org.eclipse.osee.ats.ide.util.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.data.AtsUserGroups;
import org.eclipse.osee.ats.api.user.AtsCoreUsers;
import org.eclipse.osee.ats.api.user.AtsUser;
import org.eclipse.osee.ats.core.users.AbstractAtsUserService;
import org.eclipse.osee.ats.ide.config.IAtsUserServiceClient;
import org.eclipse.osee.ats.ide.internal.AtsClientService;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.UserNotInDatabase;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * Non-artifact base user accessControlService
 *
 * @author Donald G Dunne
 */
public class AtsUserServiceClientImpl extends AbstractAtsUserService implements IAtsUserServiceClient {

   Boolean atsAdmin = null;
   private AtsUser currentUser;

   public AtsUserServiceClientImpl() {
      // For OSGI Instantiation
   }

   @Override
   public void setCurrentUser(AtsUser currentUser) {
      this.currentUser = currentUser;
   }

   @Override
   public void clearCaches() {
      currentUser = null;
   }

   @Override
   public AtsUser getCurrentUser() {
      if (currentUser == null) {
         if (UserManager.isBootstrap()) {
            currentUser = configurationService.getUserByUserId(AtsCoreUsers.BOOTSTRAP_USER.getUserId());
         } else {
            currentUser = configurationService.getUserByLoginId(System.getProperty("user.name"));
         }
      }
      return currentUser;
   }

   @Override
   public AtsUser getCurrentUserNoCache() {
      currentUser = null;
      return getCurrentUser();
   }

   @Override
   public AtsUser getUserFromOseeUser(User user) {
      AtsUser atsUser = configurationService.getUserByUserId(user.getUserId());
      if (atsUser == null) {
         atsUser = createFromArtifact(user);
         configurationService.getConfigurations().addUser(atsUser);
      }
      return atsUser;
   }

   @Override
   public User getOseeUser(AtsUser atsUser) {
      User oseeUser = null;
      if (atsUser.getStoreObject() instanceof User) {
         oseeUser = (User) atsUser.getStoreObject();
      } else {
         oseeUser = getOseeUserById(atsUser.getUserId());
      }
      return oseeUser;
   }

   @Override
   public User getCurrentOseeUser() {
      AtsUser user = getCurrentUser();
      return getOseeUser(user);
   }

   @Override
   public Collection<? extends User> toOseeUsers(Collection<? extends AtsUser> users) {
      List<User> results = new LinkedList<>();
      for (AtsUser user : users) {
         results.add(getOseeUser(user));
      }
      return results;
   }

   @Override
   public Collection<AtsUser> getAtsUsers(Collection<? extends Artifact> artifacts) {
      List<AtsUser> users = new LinkedList<>();
      for (Artifact artifact : artifacts) {
         if (artifact instanceof User) {
            User user = (User) artifact;
            AtsUser atsUser = getUserFromOseeUser(user);
            users.add(atsUser);
         }
      }
      return users;
   }

   @Override
   public Collection<User> getOseeUsers(Collection<? extends AtsUser> users) {
      List<User> results = new LinkedList<>();
      for (AtsUser user : users) {
         results.add(getOseeUser(user));
      }
      return results;
   }

   @Override
   public User getOseeUserById(String userId) {
      return getOseeUser(getUserById(userId));
   }

   @Override
   public List<User> getOseeUsersSorted(Active active) {
      Collection<AtsUser> activeUsers = getUsers(active);
      List<User> oseeUsers = new ArrayList<>();
      oseeUsers.addAll(getOseeUsers(activeUsers));
      Collections.sort(oseeUsers);
      return oseeUsers;
   }

   @Override
   public List<AtsUser> getSubscribed(IAtsWorkItem workItem) {
      ArrayList<AtsUser> arts = new ArrayList<>();
      for (Artifact art : AtsClientService.get().getQueryServiceClient().getArtifact(workItem).getRelatedArtifacts(
         AtsRelationTypes.SubscribedUser_User)) {
         arts.add(getUserById((String) art.getSoleAttributeValue(CoreAttributeTypes.UserId)));
      }
      return arts;
   }

   @Override
   public AtsUser getUserById(long accountId) {
      return getUserFromOseeUser(UserManager.getUserByArtId(accountId));
   }

   @Override
   public String getCurrentUserId() {
      return UserManager.getUser().getUserId();
   }

   @Override
   public boolean isAtsAdmin(AtsUser user) {
      return configurationService.getConfigurations().getAtsAdmins().contains(user.getStoreObject());
   }

   @Override
   public boolean isAtsAdmin(boolean useCache) {
      if (!useCache) {
         getCurrentUser().getUserGroups().contains(AtsUserGroups.AtsAdmin);
      }
      return isAtsAdmin();
   }

   @Override
   public boolean isAtsAdmin() {
      if (atsAdmin == null) {
         atsAdmin = getCurrentUser().getUserGroups().contains(AtsUserGroups.AtsAdmin);
      }
      return atsAdmin;
   }

   @Override
   public Collection<AtsUser> getUsers() {
      return configurationService.getConfigurations().getUsers();
   }

   @Override
   protected AtsUser loadUserFromDbByUserId(String userId) {
      AtsUser user = null;
      Artifact userArt = null;
      try {
         userArt = UserManager.getUserByUserId(userId);
      } catch (UserNotInDatabase ex) {
         // do nothing
      }
      if (userArt == null) {
         try {
            userArt = ArtifactQuery.getArtifactFromTypeAndAttribute(CoreArtifactTypes.User, CoreAttributeTypes.UserId,
               userId, AtsClientService.get().getAtsBranch());
            user = createFromArtifact(userArt);
         } catch (ArtifactDoesNotExist ex) {
            // do nothing
         }
      } else {
         user = createFromArtifact(userArt);
      }
      return user;
   }

   private AtsUser createFromArtifact(Artifact userArt) {
      AtsUser atsUser = new AtsUser();
      atsUser.setName(userArt.getName());
      atsUser.setStoreObject(userArt);
      atsUser.setUserId(userArt.getSoleAttributeValue(CoreAttributeTypes.UserId, ""));
      atsUser.setEmail(userArt.getSoleAttributeValue(CoreAttributeTypes.Email, ""));
      atsUser.setActive(userArt.getSoleAttributeValue(CoreAttributeTypes.Active, true));
      atsUser.setId(userArt.getId());
      for (ArtifactToken userGroup : userArt.getRelatedArtifacts(CoreRelationTypes.Users_Artifact)) {
         atsUser.getUserGroups().add(userGroup);
      }
      return atsUser;
   }

   @Override
   protected AtsUser loadUserFromDbByUserName(String name) {
      return createFromArtifact(ArtifactQuery.checkArtifactFromTypeAndName(CoreArtifactTypes.User, name,
         AtsClientService.get().getAtsBranch()));
   }

   @Override
   public AtsUser getUserByArtifactId(ArtifactId artifact) {
      return getUserFromOseeUser((User) artifact);
   }

   @Override
   protected AtsUser loadUserByAccountId(Long accountId) {
      AtsUser user = null;
      ArtifactId userArt = ArtifactQuery.getArtifactFromId(accountId, AtsClientService.get().getAtsBranch());
      if (userArt != null) {
         user = createFromArtifact(AtsClientService.get().getQueryServiceClient().getArtifact(userArt));
      }
      return user;
   }

   @Override
   public List<AtsUser> getUsersFromDb() {
      List<AtsUser> users = new ArrayList<>();
      for (ArtifactId userArt : ArtifactQuery.getArtifactListFromType(CoreArtifactTypes.User, CoreBranches.COMMON)) {
         AtsUser atsUser = createFromArtifact(AtsClientService.get().getQueryServiceClient().getArtifact(userArt));
         users.add(atsUser);
      }
      return users;
   }

   @Override
   public AtsUser getUserByAccountId(HttpHeaders httpHeaders) {
      throw new UnsupportedOperationException();
   }

}
