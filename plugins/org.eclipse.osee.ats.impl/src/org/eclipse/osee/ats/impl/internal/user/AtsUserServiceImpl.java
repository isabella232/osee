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
package org.eclipse.osee.ats.impl.internal.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.ats.api.data.AtsArtifactToken;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.core.users.AbstractAtsUserService;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.impl.internal.util.AtsUtilServer;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * Non-artifact base user service
 * 
 * @author Donald G Dunne
 */
public class AtsUserServiceImpl extends AbstractAtsUserService {

   private static OrcsApi orcsApi;
   private static Map<String, IAtsUser> userIdToAtsUser = new HashMap<String, IAtsUser>(300);
   private static Map<String, Boolean> userIdToAdmin = new HashMap<String, Boolean>(300);
   private static Map<String, IAtsUser> nameToAtsUser = new HashMap<String, IAtsUser>(300);
   private static IAtsUser currentUser = null;
   private boolean loaded = false;

   public static void setOrcsApi(OrcsApi orcsApi) {
      AtsUserServiceImpl.orcsApi = orcsApi;
   }

   public void start() throws OseeCoreException {
      Conditions.checkNotNull(orcsApi, "OrcsApi");
      System.out.println("ATS - AtsUserService started");
   }

   // TODO Replace this once server has user account
   @Override
   public IAtsUser getCurrentUser() throws OseeCoreException {
      if (currentUser == null) {
         currentUser = getUserById(SystemUser.OseeSystem.getUserId());
      }
      return currentUser;
   }

   @Override
   protected IAtsUser loadUserByUserIdFromDb(String userId) {
      IAtsUser atsUser = null;
      ResultSet<ArtifactReadable> results =
         orcsApi.getQueryFactory(AtsUtilServer.getApplicationContext()).fromBranch(AtsUtilCore.getAtsBranch()).andIsOfType(
            CoreArtifactTypes.User).and(CoreAttributeTypes.UserId,
            org.eclipse.osee.framework.core.enums.Operator.EQUAL, userId).getResults();
      if (!results.isEmpty()) {
         ArtifactReadable userArt = results.getExactlyOne();
         atsUser = new AtsUser(userArt);
      }
      return atsUser;
   }

   @Override
   protected IAtsUser loadUserByUserNameFromDb(String name) {
      IAtsUser atsUser = null;
      ArtifactReadable userArt =
         orcsApi.getQueryFactory(AtsUtilServer.getApplicationContext()).fromBranch(AtsUtilCore.getAtsBranch()).andIsOfType(
            CoreArtifactTypes.User).and(CoreAttributeTypes.Name, org.eclipse.osee.framework.core.enums.Operator.EQUAL,
            name).getResults().getExactlyOne();
      if (userArt != null) {
         atsUser = new AtsUser(userArt);
      }
      return atsUser;
   }

   @Override
   public boolean isAtsAdmin(IAtsUser user) {
      ensureLoaded();
      Boolean admin = userIdToAdmin.get(user.getUserId());
      if (admin == null) {
         admin =
            orcsApi.getQueryFactory(null).fromBranch(AtsUtilServer.getAtsBranch()).andGuid(
               AtsArtifactToken.AtsAdmin.getGuid()).andRelatedTo(CoreRelationTypes.Users_User, getUserArt(user)).getCount() == 1;
         userIdToAdmin.put(user.getUserId(), admin);
      }
      return admin;
   }

   private ArtifactReadable getUserArt(IAtsUser user) {
      ensureLoaded();
      if (user.getStoreObject() instanceof ArtifactReadable) {
         return (ArtifactReadable) user.getStoreObject();
      }
      return orcsApi.getQueryFactory(null).fromBranch(AtsUtilServer.getAtsBranch()).andGuid(user.getGuid()).getResults().getExactlyOne();
   }

   public static ArtifactReadable getCurrentUserArt() throws OseeCoreException {
      // TODO Switch to real user
      return orcsApi.getQueryFactory(AtsUtilServer.getApplicationContext()).fromBranch(AtsUtilCore.getAtsBranch()).andIsOfType(
         CoreArtifactTypes.User).and(CoreAttributeTypes.UserId, org.eclipse.osee.framework.core.enums.Operator.EQUAL,
         SystemUser.OseeSystem.getUserId()).getResults().getExactlyOne();
   }

   @Override
   public List<IAtsUser> getUsers(Active active) {
      ensureLoaded();
      List<IAtsUser> users = new ArrayList<IAtsUser>();
      for (ArtifactReadable userArt : orcsApi.getQueryFactory(AtsUtilServer.getApplicationContext()).fromBranch(
         AtsUtilCore.getAtsBranch()).andIsOfType(CoreArtifactTypes.User).getResults()) {
         Boolean activeFlag = userArt.getSoleAttributeValue(AtsAttributeTypes.Active, true);
         if (active == Active.Both || ((active == Active.Active) && activeFlag) || ((active == Active.InActive) && !activeFlag)) {
            users.add(new AtsUser(userArt));
         }
      }
      return users;
   }

   @Override
   protected synchronized void ensureLoaded() {
      if (!loaded) {
         for (ArtifactReadable art : orcsApi.getQueryFactory(null).fromBranch(AtsUtilCore.getAtsBranch()).andIsOfType(
            CoreArtifactTypes.User).getResults()) {
            AtsUser atsUser = new AtsUser(art);
            userIdToAtsUser.put(art.getSoleAttributeValue(CoreAttributeTypes.UserId, ""), atsUser);
            nameToAtsUser.put(art.getName(), atsUser);
         }
         loaded = true;
      }
   }

}
