/*********************************************************************
 * Copyright (c) 2009 Boeing
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

package org.eclipse.osee.framework.skynet.core.internal;

import com.google.common.io.ByteSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import javax.ws.rs.core.Response;
import org.eclipse.osee.framework.core.OrcsTokenService;
import org.eclipse.osee.framework.core.enums.OseeCacheEnum;
import org.eclipse.osee.framework.core.model.cache.ArtifactTypeCache;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.cache.IOseeCache;
import org.eclipse.osee.framework.core.model.cache.OseeEnumTypeCache;
import org.eclipse.osee.framework.core.services.IOseeCachingService;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.internal.accessors.DatabaseBranchAccessor;
import org.eclipse.osee.jaxrs.client.JaxRsExceptions;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.jdbc.JdbcService;
import org.eclipse.osee.orcs.rest.client.OseeClient;
import org.eclipse.osee.orcs.rest.model.TypesEndpoint;

/**
 * @author Roberto E. Escobar
 */
public class ClientCachingServiceProxy implements IOseeCachingService {

   public static interface TypesLoader {
      void loadTypes(IOseeCachingService service, ByteSource supplier);
   }

   private JdbcService jdbcService;
   private OseeClient oseeClient;
   private OrcsTokenService tokenService;
   private BranchCache branchCache;

   private OseeEnumTypeCache enumTypeCache;
   private ArtifactTypeCache artifactTypeCache;

   private List<IOseeCache<?>> caches;

   public void setJdbcService(JdbcService jdbcService) {
      this.jdbcService = jdbcService;
   }

   public void setOseeClient(OseeClient oseeClient) {
      this.oseeClient = oseeClient;
   }

   public void setOrcsTokenService(OrcsTokenService tokenService) {
      this.tokenService = tokenService;
   }

   public void start() {
      JdbcClient jdbcClient = jdbcService.getClient();

      branchCache = new BranchCache(new DatabaseBranchAccessor(jdbcClient));

      artifactTypeCache = new ArtifactTypeCache();
      enumTypeCache = new OseeEnumTypeCache();

      caches = new ArrayList<>();
      caches.add(branchCache);
      caches.add(artifactTypeCache);
      caches.add(enumTypeCache);
   }

   public void stop() {
      caches.clear();
      enumTypeCache = null;
      artifactTypeCache = null;
      branchCache = null;
   }

   @Override
   public BranchCache getBranchCache() {
      return branchCache;
   }

   @Override
   public ArtifactTypeCache getArtifactTypeCache() {
      return artifactTypeCache;
   }

   @Override
   public OseeEnumTypeCache getEnumTypeCache() {
      return enumTypeCache;
   }

   @Override
   public Collection<?> getCaches() {
      return caches;
   }

   @Override
   public OrcsTokenService getTokenService() {
      return tokenService;
   }

   @Override
   public IOseeCache<?> getCache(OseeCacheEnum cacheId) {
      Conditions.checkNotNull(cacheId, "cache id to find");
      for (IOseeCache<?> cache : caches) {
         if (cache.getCacheId().equals(cacheId)) {
            return cache;
         }
      }
      throw new OseeArgumentException("Unable to find cache for id [%s]", cacheId);
   }

   @Override
   public void reloadTypes() {
      DslToTypeLoader typesLoader = new DslToTypeLoader(branchCache, tokenService);
      typesLoader.loadTypes(this, new ByteSource() {
         @Override
         public InputStream openStream() {
            OseeLog.log(Activator.class, Level.INFO, "Loading All type caches <<<<<<<<<<<<<<<<<<<<<<");
            TypesEndpoint typesEndpoint = oseeClient.getTypesEndpoint();
            try {
               Response response = typesEndpoint.getTypes();
               return response.hasEntity() ? response.readEntity(InputStream.class) : new ByteArrayInputStream(
                  new byte[0]);
            } catch (Exception ex) {
               throw JaxRsExceptions.asOseeException(ex);
            }
         }
      });
   }

   @Override
   public void reloadAll() {
      getBranchCache().reloadCache();
      reloadTypes();
   }

   @Override
   public void clearAll() {
      getBranchCache().decacheAll();
      clearAllTypes();
   }

   private void clearAllTypes() {
      getEnumTypeCache().decacheAll();
      getArtifactTypeCache().decacheAll();
   }
}