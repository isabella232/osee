/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.skynet.core.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import javax.ws.rs.core.Response;
import org.eclipse.osee.framework.core.enums.OseeCacheEnum;
import org.eclipse.osee.framework.core.model.BranchFactory;
import org.eclipse.osee.framework.core.model.TransactionRecordFactory;
import org.eclipse.osee.framework.core.model.cache.ArtifactTypeCache;
import org.eclipse.osee.framework.core.model.cache.AttributeTypeCache;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.cache.IOseeCache;
import org.eclipse.osee.framework.core.model.cache.OseeEnumTypeCache;
import org.eclipse.osee.framework.core.model.cache.RelationTypeCache;
import org.eclipse.osee.framework.core.model.cache.TransactionCache;
import org.eclipse.osee.framework.core.services.IOseeCachingService;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.internal.accessors.DatabaseBranchAccessor;
import org.eclipse.osee.framework.skynet.core.internal.accessors.DatabaseTransactionRecordAccessor;
import org.eclipse.osee.jaxrs.client.JaxRsExceptions;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.jdbc.JdbcService;
import org.eclipse.osee.orcs.rest.client.OseeClient;
import org.eclipse.osee.orcs.rest.model.TypesEndpoint;
import com.google.common.io.InputSupplier;

/**
 * @author Roberto E. Escobar
 */
public class ClientCachingServiceProxy implements IOseeCachingService {

   public static interface TypesLoader {
      void loadTypes(IOseeCachingService service, InputSupplier<? extends InputStream> supplier);
   }

   private JdbcService jdbcService;
   private OseeClient oseeClient;

   private BranchCache branchCache;
   private TransactionCache txCache;

   private OseeEnumTypeCache enumTypeCache;
   private AttributeTypeCache attributeTypeCache;
   private ArtifactTypeCache artifactTypeCache;
   private RelationTypeCache relationTypeCache;

   private List<IOseeCache<?, ?>> caches;

   public void setJdbcService(JdbcService jdbcService) {
      this.jdbcService = jdbcService;
   }

   public void setOseeClient(OseeClient oseeClient) {
      this.oseeClient = oseeClient;
   }

   public void start() {
      JdbcClient jdbcClient = jdbcService.getClient();

      txCache = new TransactionCache();
      branchCache = new BranchCache(new DatabaseBranchAccessor(jdbcClient, txCache, new BranchFactory()), txCache);
      txCache.setAccessor(new DatabaseTransactionRecordAccessor(jdbcClient, branchCache, new TransactionRecordFactory()));

      artifactTypeCache = new ArtifactTypeCache();
      enumTypeCache = new OseeEnumTypeCache();
      attributeTypeCache = new AttributeTypeCache();
      relationTypeCache = new RelationTypeCache();

      caches = new ArrayList<IOseeCache<?, ?>>();
      caches.add(branchCache);
      caches.add(txCache);
      caches.add(artifactTypeCache);
      caches.add(attributeTypeCache);
      caches.add(relationTypeCache);
      caches.add(enumTypeCache);
   }

   public void stop() {
      caches.clear();

      enumTypeCache = null;
      attributeTypeCache = null;
      relationTypeCache = null;
      artifactTypeCache = null;

      branchCache = null;
      txCache = null;
   }

   @Override
   public BranchCache getBranchCache() {
      return branchCache;
   }

   @Override
   public TransactionCache getTransactionCache() {
      return txCache;
   }

   @Override
   public ArtifactTypeCache getArtifactTypeCache() {
      return artifactTypeCache;
   }

   @Override
   public AttributeTypeCache getAttributeTypeCache() {
      return attributeTypeCache;
   }

   @Override
   public RelationTypeCache getRelationTypeCache() {
      return relationTypeCache;
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
   public IOseeCache<?, ?> getCache(OseeCacheEnum cacheId) {
      Conditions.checkNotNull(cacheId, "cache id to find");
      for (IOseeCache<?, ?> cache : caches) {
         if (cache.getCacheId().equals(cacheId)) {
            return cache;
         }
      }
      throw new OseeArgumentException("Unable to find cache for id [%s]", cacheId);
   }

   @Override
   public void reloadTypes() {
      DslToTypeLoader typesLoader = new DslToTypeLoader(branchCache);
      typesLoader.loadTypes(this, new InputSupplier<InputStream>() {
         @Override
         public InputStream getInput() {
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
      getTransactionCache().reloadCache();

      reloadTypes();
   }

   @Override
   public void clearAll() {
      getBranchCache().decacheAll();
      getTransactionCache().decacheAll();
      clearAllTypes();
   }

   private void clearAllTypes() {
      getEnumTypeCache().decacheAll();
      getAttributeTypeCache().decacheAll();
      getRelationTypeCache().decacheAll();
      getArtifactTypeCache().decacheAll();
   }

}
