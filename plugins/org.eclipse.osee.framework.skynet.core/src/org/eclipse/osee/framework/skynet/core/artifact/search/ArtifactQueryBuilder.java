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
package org.eclipse.osee.framework.skynet.core.artifact.search;

import static org.eclipse.osee.framework.core.enums.DeletionFlag.EXCLUDE_DELETED;
import static org.eclipse.osee.framework.core.enums.DeletionFlag.INCLUDE_DELETED;
import static org.eclipse.osee.framework.skynet.core.artifact.LoadType.INCLUDE_CACHE;
import static org.eclipse.osee.framework.skynet.core.artifact.LoadType.RELOAD_CACHE;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.enums.LoadLevel;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.MultipleArtifactsExist;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoader;
import org.eclipse.osee.framework.skynet.core.artifact.ISearchConfirmer;
import org.eclipse.osee.framework.skynet.core.artifact.LoadType;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.orcs.rest.client.OseeClient;
import org.eclipse.osee.orcs.rest.client.QueryBuilder;
import org.eclipse.osee.orcs.rest.model.search.artifact.SearchResponse;
import org.eclipse.osee.orcs.rest.model.search.artifact.SearchResult;

/**
 * @author Ryan D. Brooks
 */
public class ArtifactQueryBuilder {
   private List<String> guids;
   private String guid;
   private final ArtifactSearchCriteria[] criteria;
   private final BranchId branch;
   private int artifactId;
   private Collection<Integer> artifactIds;
   private final Collection<? extends IArtifactType> artifactTypes;
   private final DeletionFlag allowDeleted;
   private final LoadLevel loadLevel;
   private boolean emptyCriteria = false;
   private final TransactionId transactionId;

   /**
    * @param allowDeleted set whether deleted artifacts should be included in the resulting artifact list
    */
   public ArtifactQueryBuilder(int artId, BranchId branch, DeletionFlag allowDeleted, LoadLevel loadLevel) {
      this(null, artId, null, null, null, branch, TransactionId.SENTINEL, allowDeleted, loadLevel);
   }

   /**
    * search for artifacts with the given ids
    *
    * @param artifactIds list of artifact ids
    * @param allowDeleted set whether deleted artifacts should be included in the resulting artifact list
    */
   public ArtifactQueryBuilder(Collection<Integer> artifactIds, BranchId branch, DeletionFlag allowDeleted, LoadLevel loadLevel) {
      this(artifactIds, 0, null, null, null, branch, TransactionId.SENTINEL, allowDeleted, loadLevel);
      emptyCriteria = artifactIds.isEmpty();
   }

   public ArtifactQueryBuilder(List<String> guids, BranchId branch, LoadLevel loadLevel) {
      this(null, 0, guids, null, null, branch, TransactionId.SENTINEL, EXCLUDE_DELETED, loadLevel);
      emptyCriteria = guids.isEmpty();
   }

   public ArtifactQueryBuilder(List<String> guids, BranchId branch, DeletionFlag allowDeleted, LoadLevel loadLevel) {
      this(null, 0, guids, null, null, branch, TransactionId.SENTINEL, allowDeleted, loadLevel);
      emptyCriteria = guids.isEmpty();
   }

   public ArtifactQueryBuilder(List<String> guids, TransactionId transactionId, DeletionFlag allowDeleted, LoadLevel loadLevel) throws OseeCoreException {
      this(null, 0, guids, null, null, transactionId.getBranch(), transactionId, allowDeleted, loadLevel);
      emptyCriteria = guids.isEmpty();
   }

   public ArtifactQueryBuilder(Collection<Integer> artifactIds, TransactionId transactionId, DeletionFlag allowDeleted, LoadLevel loadLevel) throws OseeCoreException {
      this(artifactIds, 0, null, null, null, transactionId.getBranch(), transactionId, allowDeleted, loadLevel);
      emptyCriteria = artifactIds.isEmpty();
   }

   public ArtifactQueryBuilder(int artifactId, TransactionId transactionId, DeletionFlag allowDeleted, LoadLevel loadLevel) throws OseeCoreException {
      this(null, artifactId, null, null, null, transactionId.getBranch(), transactionId, allowDeleted, loadLevel);
   }

   public ArtifactQueryBuilder(String guid, BranchId branch, DeletionFlag allowDeleted, LoadLevel loadLevel) throws OseeCoreException {
      this(null, 0, null, ensureValid(guid), null, branch, TransactionId.SENTINEL, allowDeleted, loadLevel);
   }

   public ArtifactQueryBuilder(IArtifactType artifactType, BranchId branch, LoadLevel loadLevel, DeletionFlag allowDeleted) {
      this(null, 0, null, null, Arrays.asList(artifactType), branch, TransactionId.SENTINEL, allowDeleted, loadLevel);
   }

   public ArtifactQueryBuilder(Collection<? extends IArtifactType> artifactTypes, BranchId branch, LoadLevel loadLevel, DeletionFlag allowDeleted) {
      this(null, 0, null, null, artifactTypes, branch, TransactionId.SENTINEL, allowDeleted, loadLevel);
      emptyCriteria = artifactTypes.isEmpty();
   }

   public ArtifactQueryBuilder(BranchId branch, LoadLevel loadLevel, DeletionFlag allowDeleted) {
      this(null, 0, null, null, null, branch, TransactionId.SENTINEL, allowDeleted, loadLevel);
   }

   public ArtifactQueryBuilder(BranchId branch, LoadLevel loadLevel, DeletionFlag allowDeleted, ArtifactSearchCriteria... criteria) {
      this(null, 0, null, null, null, branch, TransactionId.SENTINEL, allowDeleted, loadLevel, criteria);
      emptyCriteria = criteria.length == 0;
   }

   public ArtifactQueryBuilder(BranchId branch, LoadLevel loadLevel, List<ArtifactSearchCriteria> criteria) {
      this(null, 0, null, null, null, branch, TransactionId.SENTINEL, EXCLUDE_DELETED, loadLevel, toArray(criteria));
      emptyCriteria = criteria.isEmpty();
   }

   public ArtifactQueryBuilder(IArtifactType artifactType, BranchId branch, LoadLevel loadLevel, ArtifactSearchCriteria... criteria) {
      this(null, 0, null, null, Arrays.asList(artifactType), branch, TransactionId.SENTINEL, EXCLUDE_DELETED, loadLevel,
         criteria);
      emptyCriteria = criteria.length == 0;
   }

   public ArtifactQueryBuilder(IArtifactType artifactType, BranchId branch, LoadLevel loadLevel, List<ArtifactSearchCriteria> criteria) {
      this(null, 0, null, null, Arrays.asList(artifactType), branch, TransactionId.SENTINEL, EXCLUDE_DELETED, loadLevel,
         toArray(criteria));
      emptyCriteria = criteria.isEmpty();
   }

   private ArtifactQueryBuilder(Collection<Integer> artifactIds, int artifactId, List<String> guids, String guid, Collection<? extends IArtifactType> artifactTypes, BranchId branch, TransactionId transactionId, DeletionFlag allowDeleted, LoadLevel loadLevel, ArtifactSearchCriteria... criteria) {
      this.artifactTypes = artifactTypes;
      this.branch = branch;
      this.criteria = criteria;
      this.loadLevel = loadLevel;
      this.allowDeleted = allowDeleted;
      this.guid = guid;
      this.artifactId = artifactId;
      this.transactionId = transactionId;
      if (artifactIds != null && !artifactIds.isEmpty()) {
         if (artifactIds.size() == 1) {
            this.artifactId = artifactIds.iterator().next();
         } else {
            this.artifactIds = artifactIds;
         }
      }

      if (Conditions.hasValues(guids)) {
         if (guids.size() == 1) {
            this.guid = guids.get(0);
         } else {
            this.guids = new ArrayList<>();
            for (String id : guids) {
               if (GUID.isValid(id)) {
                  this.guids.add(id);
               }
            }
         }
      }

   }

   private static ArtifactSearchCriteria[] toArray(List<ArtifactSearchCriteria> criteria) {
      return criteria.toArray(new ArtifactSearchCriteria[criteria.size()]);
   }

   private static String ensureValid(String id) throws OseeCoreException {
      boolean guidCheck = GUID.isValid(id);
      if (!guidCheck) {
         throw new OseeArgumentException("Invalid guid detected [%s]", id);
      }
      return id;
   }

   private boolean useServerSearch() {
      return Conditions.hasValues(artifactTypes) || guid != null || Conditions.hasValues(
         guids) || criteria.length > 0 || artifactId == 0 && !Conditions.hasValues(artifactIds);
   }

   private QueryBuilder getQueryBuilder() throws OseeCoreException {
      QueryBuilder toReturn;
      if (useServerSearch()) {
         OseeClient client = ServiceUtil.getOseeClient();
         toReturn = client.createQueryBuilder(branch);
      } else {
         LocalIdQueryBuilder builder = new LocalIdQueryBuilder(branch);

         Class<?>[] types = new Class<?>[] {QueryBuilder.class};
         toReturn = (QueryBuilder) Proxy.newProxyInstance(QueryBuilder.class.getClassLoader(), types, builder);
      }
      return toReturn;
   }

   private QueryBuilder createOrcsQuery() throws OseeCoreException {

      QueryBuilder builder = getQueryBuilder();

      if (allowDeleted == INCLUDE_DELETED) {
         builder.includeDeleted();
      }

      if (artifactId != 0 || Conditions.hasValues(artifactIds)) {
         if (Conditions.hasValues(artifactIds)) {
            builder.andLocalIds(artifactIds);
         } else if (artifactId != 0) {
            builder.andLocalId(artifactId);
         }
      }

      if (Conditions.hasValues(artifactTypes)) {
         builder.andTypeEquals(artifactTypes);
      }

      if (guid != null) {
         builder.andGuids(guid);
      }

      if (Conditions.hasValues(guids)) {
         builder.andGuids(guids);
      }

      if (criteria.length > 0) {
         for (ArtifactSearchCriteria idx : criteria) {
            idx.addToQueryBuilder(builder);
         }
      }

      if (transactionId.isValid()) {
         builder.fromTransaction(transactionId.getId());
      }

      return builder;
   }

   public List<Artifact> getArtifacts(int artifactCountEstimate, ISearchConfirmer confirmer) throws OseeCoreException {
      return internalGetArtifacts(artifactCountEstimate, confirmer, INCLUDE_CACHE);
   }

   public List<Artifact> reloadArtifacts(int artifactCountEstimate) throws OseeCoreException {
      return internalGetArtifacts(artifactCountEstimate, null, RELOAD_CACHE);
   }

   public Artifact reloadArtifact() throws OseeCoreException {
      if (emptyCriteria) {
         throw new ArtifactDoesNotExist("received an empty list in the criteria for this search");
      }
      Collection<Artifact> artifacts = internalGetArtifacts(1, null, RELOAD_CACHE);

      if (artifacts.isEmpty()) {
         throw new ArtifactDoesNotExist(getSoleExceptionMessage(artifacts.size()));
      }
      if (artifacts.size() > 1) {
         throw new MultipleArtifactsExist(getSoleExceptionMessage(artifacts.size()));
      }
      return artifacts.iterator().next();
   }

   private List<Artifact> loadArtifactsFromServerIds(LoadType reload) throws OseeCoreException {
      List<Integer> ids = createOrcsQuery().getIds();
      List<Artifact> artifacts;
      if (ids != null && !ids.isEmpty()) {
         artifacts = ArtifactLoader.loadArtifacts(ids, branch, loadLevel, reload, allowDeleted, transactionId);
      } else {
         artifacts = Collections.emptyList();
      }
      return artifacts;
   }

   private List<Artifact> internalGetArtifacts(int artifactCountEstimate, ISearchConfirmer confirmer, LoadType reload) throws OseeCoreException {
      if (emptyCriteria) {
         return java.util.Collections.emptyList();
      }

      List<Artifact> artifactsFromServerIds = loadArtifactsFromServerIds(reload);
      return artifactsFromServerIds;
   }

   public List<Integer> selectArtifacts(int artifactCountEstimate) throws OseeCoreException {
      return createOrcsQuery().getIds();
   }

   public int countArtifacts() throws OseeCoreException {
      if (emptyCriteria) {
         return 0;
      } else {
         return createOrcsQuery().getCount();
      }
   }

   protected Artifact getOrCheckArtifact(QueryType queryType) throws OseeCoreException {
      if (emptyCriteria) {
         throw new ArtifactDoesNotExist("received an empty list in the criteria for this search");
      }
      Collection<Artifact> artifacts = getArtifacts(1, null);

      if (artifacts.isEmpty()) {
         if (queryType.equals(QueryType.CHECK)) {
            return null;
         }
         throw new ArtifactDoesNotExist(getSoleExceptionMessage(artifacts.size()));
      }
      if (artifacts.size() > 1) {
         throw new MultipleArtifactsExist(getSoleExceptionMessage(artifacts.size()));
      }
      return artifacts.iterator().next();
   }

   private String getSoleExceptionMessage(int artifactCount) {
      StringBuilder message = new StringBuilder(250);
      if (artifactCount == 0) {
         message.append("ArtifactQueryBuilder: No artifact found");
      } else {
         message.append(artifactCount);
         message.append(" artifacts found");
      }
      if (artifactTypes != null) {
         message.append(" with type(s): ");
         message.append(artifactTypes);
      }
      if (artifactId != 0) {
         message.append(" with id \"");
         message.append(artifactId);
         message.append("\"");
      }
      if (guid != null) {
         message.append(" with id \"");
         message.append(guid);
         message.append("\"");
      }
      if (criteria.length > 0) {
         message.append(" with criteria \"");
         message.append(Arrays.deepToString(criteria));
         message.append("\"");
      }
      message.append(" on branch \"");
      message.append(branch.getUuid());
      message.append("\"");
      return message.toString();
   }

   private static final class LocalIdQueryBuilder implements InvocationHandler {

      private final Set<Integer> localIds = new LinkedHashSet<>();
      private DeletionFlag allowDeleted = EXCLUDE_DELETED;
      private int txId = -1;
      private final BranchId branch;

      public LocalIdQueryBuilder(BranchId branch) {
         super();
         this.branch = branch;
      }

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
         Object toReturn = null;

         Method localMethod = getMethodFor(getClass(), method);
         if (localMethod != null) {
            try {
               toReturn = localMethod.invoke(this, args);
            } catch (InvocationTargetException e) {
               throw e.getCause();
            }
            if (toReturn == null) {
               toReturn = proxy;
            }
         } else {
            throw new UnsupportedOperationException();
         }
         return toReturn;
      }

      private Method getMethodFor(Class<?> clazz, Method method) {
         Method toReturn = null;
         try {
            toReturn = clazz.getMethod(method.getName(), method.getParameterTypes());
         } catch (Exception ex) {
            // Do Nothing;
         }
         return toReturn;
      }

      @SuppressWarnings("unused")
      public void fromTransaction(int transactionId) {
         txId = transactionId;
      }

      @SuppressWarnings("unused")
      public void includeDeleted() {
         includeDeleted(true);
      }

      public void includeDeleted(boolean enabled) {
         allowDeleted = enabled ? INCLUDE_DELETED : EXCLUDE_DELETED;
      }

      @SuppressWarnings("unused")
      public SearchResult getSearchResult() throws OseeCoreException {
         SearchResponse response = new SearchResponse();
         List<Integer> ids = new LinkedList<>(localIds);
         response.setIds(ids);
         return response;
      }

      @SuppressWarnings("unused")
      public int getCount() throws OseeCoreException {
         TransactionId tx;
         if (txId == -1) {
            tx = TransactionManager.getHeadTransaction(branch);
         } else {
            tx = TransactionManager.getTransactionId(txId);
         }
         List<Artifact> results = ArtifactLoader.loadArtifacts(localIds, branch, LoadLevel.ARTIFACT_DATA,
            LoadType.INCLUDE_CACHE, allowDeleted, tx);
         return results.size();
      }

      @SuppressWarnings("unused")
      public void andLocalId(int... artifactId) {
         for (int id : artifactId) {
            localIds.add(id);
         }
      }

      @SuppressWarnings("unused")
      public void andLocalIds(Collection<Integer> artifactIds) {
         localIds.addAll(artifactIds);
      }

      @SuppressWarnings("unused")
      public List<Integer> getIds() {
         return new LinkedList<Integer>(localIds);
      }

   }

}