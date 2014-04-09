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
package org.eclipse.osee.framework.skynet.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import org.eclipse.osee.cache.admin.CacheAdmin;
import org.eclipse.osee.framework.core.data.IUserToken;
import org.eclipse.osee.framework.core.exception.UserNotInDatabase;
import org.eclipse.osee.framework.jdk.core.type.LazyObject;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;
import org.eclipse.osee.framework.skynet.core.internal.users.UserAdminImpl;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Roberto E. Escobar
 */
public final class UserManager {

   public static String DOUBLE_CLICK_SETTING_KEY = "onDoubleClickOpenUsingArtifactEditor";

   private static final LazyObject<UserAdmin> provider = new LazyObject<UserAdmin>() {

      @Override
      protected FutureTask<UserAdmin> createLoaderTask() {
         Callable<UserAdmin> callable = new Callable<UserAdmin>() {

            @Override
            public UserAdmin call() throws Exception {
               UserAdminImpl userAdmin = new UserAdminImpl();

               CacheAdmin cacheAdmin = ServiceUtil.getCacheAdmin();
               userAdmin.setCacheAdmin(cacheAdmin);

               userAdmin.start();

               return userAdmin;
            }

         };
         return new FutureTask<UserAdmin>(callable);
      }

   };

   private UserManager() {
      // Utility class
   }

   private static UserAdmin getUserAdmin() throws OseeCoreException {
      return provider.get();
   }

   /**
    * Returns the currently authenticated user
    */
   public static User getUser() throws OseeCoreException {
      return getUserAdmin().getCurrentUser();
   }

   public static void releaseUser() throws OseeCoreException {
      getUserAdmin().releaseCurrentUser();
   }

   public static void clearCache() throws OseeCoreException {
      getUserAdmin().reset();
   }

   public static List<User> getUsersByUserId(Collection<String> userIds) throws OseeCoreException {
      List<User> users = new ArrayList<User>();
      for (String userId : userIds) {
         try {
            User user = getUserAdmin().getUserByUserId(userId);
            if (user != null) {
               users.add(user);
            }
         } catch (UserNotInDatabase ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
      return users;
   }

   /**
    * @return shallow copy of ArrayList of all active users in the datastore sorted by user name
    */
   public static List<User> getUsers() throws OseeCoreException {
      return getUserAdmin().getActiveUsers();
   }

   public static List<User> getUsersAll() throws OseeCoreException {
      return getUserAdmin().getUsersAll();
   }

   public static List<User> getUsersSortedByName() throws OseeCoreException {
      return getUserAdmin().getActiveUsersSortedByName();
   }

   public static List<User> getUsersAllSortedByName() throws OseeCoreException {
      return getUserAdmin().getUsersAllSortedByName();
   }

   /**
    * Return sorted list of active User.getName() in database
    */
   public static String[] getUserNames() throws OseeCoreException {
      return getUserAdmin().getUserNames();
   }

   public static String getSafeUserNameById(int userArtifactId) {
      UserAdmin userAdmin = null;
      try {
         userAdmin = getUserAdmin();
      } catch (OseeCoreException ex) {
         // Do nothing;
      }

      String name;
      if (userAdmin != null) {
         name = userAdmin.getSafeUserNameById(userArtifactId);
      } else {
         name = String.format("Unable resolve user by artId[%s] since userAdmin was unavailable", userArtifactId);
      }
      return name;
   }

   public static User getUserByArtId(int userArtifactId) throws OseeCoreException {
      return getUserAdmin().getUserByArtId(userArtifactId);
   }

   /**
    * This is not the preferred way to get a user. Most likely getUserByUserId() or getUserByArtId() should be used
    * 
    * @return the first user found with the given name
    */
   public static User getUserByName(String name) throws OseeCoreException {
      return getUserAdmin().getUserByName(name);
   }

   public static User getUser(IUserToken user) throws OseeCoreException {
      return getUserAdmin().getUser(user);
   }

   public static User getUserByUserId(String userId) throws OseeCoreException {
      return getUserAdmin().getUserByUserId(userId);
   }

   /**
    * @return whether the Authentication manager is in the middle of creating a user
    * @throws OseeCoreException
    */
   public static boolean duringMainUserCreation() throws OseeCoreException {
      return getUserAdmin().isDuringCurrentUserCreation();
   }

   public static User createUser(IUserToken userToken, SkynetTransaction transaction) throws OseeCoreException {
      return getUserAdmin().createUser(userToken, transaction);
   }

   public static String getSetting(String key) throws OseeCoreException {
      return getUser().getSetting(key);
   }

   public static String getSetting(Long key) throws OseeCoreException {
      return getUser().getSetting(String.valueOf(key));
   }

   public static boolean getBooleanSetting(String key) throws OseeCoreException {
      return getUser().getBooleanSetting(key);
   }

   public static void setSetting(String key, String value) throws OseeCoreException {
      getUser().setSetting(key, value);
   }

   public static void setSetting(String key, Long value) throws OseeCoreException {
      getUser().setSetting(key, String.valueOf(value));
   }

}