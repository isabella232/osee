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
package org.eclipse.osee.ats.core.client.internal;

import java.util.concurrent.TimeUnit;
import org.eclipse.osee.ats.api.config.AtsConfigurations;
import org.eclipse.osee.ats.api.config.IAtsConfigurationProvider;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.user.IUserArtLoader;
import org.eclipse.osee.ats.api.user.JaxAtsUser;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * @author Donald G. Dunne
 */
public class AtsConfigurationsService implements IAtsConfigurationProvider {

   @Override
   public AtsConfigurations getConfigurations() {
      return configurationsCache.get();
   }

   @Override
   public void clearConfigurationsCaches() {
      configurationsCache = Suppliers.memoizeWithExpiration(getConfigurationsSupplier(), 5, TimeUnit.MINUTES);
   }

   private Supplier<AtsConfigurations> configurationsCache =
      Suppliers.memoizeWithExpiration(getConfigurationsSupplier(), 5, TimeUnit.MINUTES);

   private Supplier<AtsConfigurations> getConfigurationsSupplier() {
      return new Supplier<AtsConfigurations>() {
         @Override
         public AtsConfigurations get() {
            return loadConfigurations();
         }
      };
   }

   private AtsConfigurations loadConfigurations() {
      AtsConfigurations configs = AtsClientService.getConfigEndpoint().get();
      for (IAtsUser user : configs.getUsers()) {
         JaxAtsUser jUser = (JaxAtsUser) user;
         jUser.setUserArtLoader(userLoader);
      }
      return configs;
   }

   /**
    * Lazy Loader for user artifact
    */
   private final UserArtLoader userLoader = new UserArtLoader();
   private class UserArtLoader implements IUserArtLoader {

      @Override
      public ArtifactId loadUser(IAtsUser user) {
         ArtifactId userArt = null;
         try {
            userArt = UserManager.getUserByArtId(user.getId().intValue());
            if (userArt == null) {
               userArt = ArtifactQuery.getArtifactFromId(user.getId(), AtsUtilCore.getAtsBranch());
            }
         } catch (ArtifactDoesNotExist ex) {
            // do nothing
         }
         user.setStoreObject(userArt);
         return userArt;
      }
   }

}
