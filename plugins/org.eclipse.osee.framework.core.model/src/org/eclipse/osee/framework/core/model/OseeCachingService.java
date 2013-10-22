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
package org.eclipse.osee.framework.core.model;

import java.util.Collection;
import java.util.HashSet;
import org.eclipse.osee.framework.core.enums.OseeCacheEnum;
import org.eclipse.osee.framework.core.model.cache.ArtifactTypeCache;
import org.eclipse.osee.framework.core.model.cache.AttributeTypeCache;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.cache.IOseeCache;
import org.eclipse.osee.framework.core.model.cache.OseeEnumTypeCache;
import org.eclipse.osee.framework.core.model.cache.RelationTypeCache;
import org.eclipse.osee.framework.core.model.cache.TransactionCache;
import org.eclipse.osee.framework.core.services.IOseeCachingService;
import org.eclipse.osee.framework.core.services.IdentityService;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Roberto E. Escobar
 */
public class OseeCachingService implements IOseeCachingService {

   private final BranchCache branchCache;
   private final TransactionCache transactionCache;

   private final ArtifactTypeCache artifactTypeCache;
   private final AttributeTypeCache attributeTypeCache;
   private final RelationTypeCache relationTypeCache;
   private final OseeEnumTypeCache oseeEnumTypeCache;
   public final IdentityService identityService;
   private final Collection<IOseeCache<?, ?>> caches;

   public OseeCachingService(BranchCache branchCache, TransactionCache transactionCache, ArtifactTypeCache artifactTypeCache, AttributeTypeCache attributeTypeCache, RelationTypeCache relationTypeCache, OseeEnumTypeCache oseeEnumTypeCache, IdentityService identityService) {
      this.branchCache = branchCache;
      this.transactionCache = transactionCache;
      this.artifactTypeCache = artifactTypeCache;
      this.attributeTypeCache = attributeTypeCache;
      this.relationTypeCache = relationTypeCache;
      this.oseeEnumTypeCache = oseeEnumTypeCache;
      this.identityService = identityService;
      caches = new HashSet<IOseeCache<?, ?>>();
      caches.add(branchCache);
      caches.add(transactionCache);
      caches.add(artifactTypeCache);
      caches.add(attributeTypeCache);
      caches.add(relationTypeCache);
      caches.add(oseeEnumTypeCache);
   }

   @Override
   public BranchCache getBranchCache() {
      return branchCache;
   }

   @Override
   public TransactionCache getTransactionCache() {
      return transactionCache;
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
   public OseeEnumTypeCache getEnumTypeCache() {
      return oseeEnumTypeCache;
   }

   @Override
   public RelationTypeCache getRelationTypeCache() {
      return relationTypeCache;
   }

   @Override
   public Collection<IOseeCache<?, ?>> getCaches() {
      return caches;
   }

   @Override
   public IOseeCache<?, ?> getCache(OseeCacheEnum cacheId) throws OseeCoreException {
      Conditions.checkNotNull(cacheId, "cache id to find");
      for (IOseeCache<?, ?> cache : getCaches()) {
         if (cache.getCacheId().equals(cacheId)) {
            return cache;
         }
      }
      throw new OseeArgumentException("Unable to find cache for id [%s]", cacheId);
   }

   @Override
   public synchronized void reloadAll() throws OseeCoreException {
      getBranchCache().reloadCache();
      getTransactionCache().reloadCache();
      getIdentityService().clear();
      getArtifactTypeCache().reloadCache();

      //reloading the artifactTypeCache will reload these:
      //      getEnumTypeCache().reloadCache();
      //      getAttributeTypeCache().reloadCache();
      //      getRelationTypeCache().reloadCache();
   }

   @Override
   public synchronized void clearAll() {
      getBranchCache().decacheAll();
      getTransactionCache().decacheAll();
      getEnumTypeCache().decacheAll();
      getAttributeTypeCache().decacheAll();
      getArtifactTypeCache().decacheAll();
      getRelationTypeCache().decacheAll();
   }

   @Override
   public IdentityService getIdentityService() {
      return identityService;
   }
}
