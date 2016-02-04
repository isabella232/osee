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
import static org.eclipse.osee.framework.core.enums.LoadLevel.ALL;
import static org.eclipse.osee.framework.skynet.core.artifact.LoadType.INCLUDE_CACHE;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IArtifactToken;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.enums.LoadLevel;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.MultipleArtifactsExist;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.model.event.IBasicGuidArtifact;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.framework.jdk.core.type.ResultSets;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoader;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.model.EventBasicGuidArtifact;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.orcs.rest.client.OseeClient;
import org.eclipse.osee.orcs.rest.client.QueryBuilder;
import org.eclipse.osee.orcs.rest.model.search.artifact.RequestType;
import org.eclipse.osee.orcs.rest.model.search.artifact.SearchMatch;
import org.eclipse.osee.orcs.rest.model.search.artifact.SearchParameters;
import org.eclipse.osee.orcs.rest.model.search.artifact.SearchResult;

/**
 * @author Ryan D. Brooks
 */
public class ArtifactQuery {

   private static Map<String, Long> guidToUuid;
   private static Map<Long, String> uuidToGuid;

   public static Artifact getArtifactFromToken(IArtifactToken artifactToken, BranchId branch) throws OseeCoreException {
      return getArtifactFromId(artifactToken.getGuid(), branch);
   }

   public static Artifact getArtifactFromToken(EventBasicGuidArtifact guidArt) throws OseeCoreException {
      return getArtifactFromId(guidArt.getGuid(), BranchManager.getBranch(guidArt.getBranchId()));
   }

   public static Artifact getArtifactFromToken(IBasicGuidArtifact guidArt) throws OseeCoreException {
      return getArtifactFromId(guidArt.getGuid(), BranchManager.getBranch(guidArt.getBranchId()));
   }

   /**
    * search for exactly one artifact by one its id - otherwise throw an exception
    *
    * @param artId the id of the desired artifact
    * @return exactly one artifact by one its id - otherwise throw an exception
    * @throws ArtifactDoesNotExist if no artifacts are found
    */
   public static Artifact getArtifactFromId(int artId, BranchId branch) throws OseeCoreException {
      return getArtifactFromId(artId, branch, EXCLUDE_DELETED);
   }

   public static Artifact getArtifactFromId(long artId, BranchId branch) throws OseeCoreException {
      return getArtifactFromId(new Long(artId).intValue(), branch, EXCLUDE_DELETED);
   }

   /**
    * search for exactly one artifact by one its id - otherwise throw an exception
    *
    * @param artId the id of the desired artifact
    * @param allowDeleted whether to return the artifact even if it has been deleted
    * @return exactly one artifact by one its id - otherwise throw an exception
    * @throws ArtifactDoesNotExist if no artifacts are found
    */
   public static Artifact getArtifactFromId(int artId, BranchId branch, DeletionFlag allowDeleted) throws OseeCoreException {
      return getOrCheckArtifactFromId(artId, branch, allowDeleted, QueryType.GET);
   }

   public static Artifact getArtifactFromId(Long artId, BranchId branch, DeletionFlag allowDeleted) throws OseeCoreException {
      return getOrCheckArtifactFromId(artId.intValue(), branch, allowDeleted, QueryType.GET);
   }

   private static Artifact getOrCheckArtifactFromId(int artId, BranchId branch, DeletionFlag allowDeleted, QueryType queryType) throws OseeCoreException {
      Artifact artifact = ArtifactCache.getActive(artId, branch);
      if (artifact != null) {
         if (artifact.isDeleted() && allowDeleted == EXCLUDE_DELETED) {
            if (queryType == QueryType.CHECK) {
               artifact = null;
            } else {
               throw new ArtifactDoesNotExist("Deleted artifact unexpectedly returned");
            }
         }
      } else {
         artifact = new ArtifactQueryBuilder(artId, branch, allowDeleted, ALL).getOrCheckArtifact(queryType);
      }
      return artifact;
   }

   /**
    * Checks for existence of an artifact by id
    *
    * @param artifactId the id of the desired artifact
    * @param allowDeleted whether to return the artifact even if it has been deleted
    * @return one artifact by one its id if it exists, otherwise null
    */
   public static Artifact checkArtifactFromId(int artifactId, BranchId branch, DeletionFlag allowDeleted) throws OseeCoreException {
      return getOrCheckArtifactFromId(artifactId, branch, allowDeleted, QueryType.CHECK);
   }

   /**
    * Checks for existence of an artifact by one its guid - otherwise throw an exception
    *
    * @param guid either the guid of the desired artifact
    * @param allowDeleted whether to return the artifact even if it has been deleted
    * @return one artifact by one its id if it exists, otherwise null
    */
   public static Artifact checkArtifactFromId(String guid, BranchId branch, DeletionFlag allowDeleted) throws OseeCoreException {
      return getOrCheckArtifactFromId(guid, branch, allowDeleted, QueryType.CHECK);
   }

   /**
    * Checks for existence of an artifact by one its guid or human readable id - otherwise throw an exception
    *
    * @param uuid of the desired artifact
    * @return one artifact by its guid if it exists, otherwise null
    */
   public static Artifact checkArtifactFromId(long uuid, BranchId branch) throws OseeCoreException {
      return getOrCheckArtifactFromId(new Long(uuid).intValue(), branch, EXCLUDE_DELETED, QueryType.CHECK);
   }

   /**
    * search for exactly one artifact by one its guid - otherwise throw an exception
    *
    * @param guid of the desired artifact
    * @return exactly one artifact by one its guid - otherwise throw an exception
    * @throws ArtifactDoesNotExist if no artifacts are found
    * @throws MultipleArtifactsExist if more than one artifact is found
    */
   public static Artifact getArtifactFromId(String guid, BranchId branch) throws OseeCoreException {
      return getOrCheckArtifactFromId(guid, branch, EXCLUDE_DELETED, QueryType.GET);
   }

   /**
    * search for exactly one artifact by one its guid or human readable id - otherwise throw an exception
    *
    * @param the guid of the desired artifact
    * @param allowDeleted whether to return the artifact even if it has been deleted
    * @return exactly one artifact by one its guid or human readable id - otherwise throw an exception
    * @throws ArtifactDoesNotExist if no artifacts are found
    * @throws MultipleArtifactsExist if more than one artifact is found
    */
   public static Artifact getArtifactFromId(String guid, BranchId branch, DeletionFlag allowDeleted) throws OseeCoreException {
      return getOrCheckArtifactFromId(guid, branch, allowDeleted, QueryType.GET);
   }

   private static Artifact getOrCheckArtifactFromId(String guid, BranchId branch, DeletionFlag allowDeleted, QueryType queryType) throws OseeCoreException {
      Artifact artifact = ArtifactCache.getActive(guid, branch);
      if (artifact != null) {
         if (artifact.isDeleted() && allowDeleted == EXCLUDE_DELETED) {
            if (queryType == QueryType.CHECK) {
               return null;
            } else {
               throw new ArtifactDoesNotExist("Deleted artifact unexpectedly returned");
            }
         }
         return artifact;
      }
      return new ArtifactQueryBuilder(guid, branch, allowDeleted, ALL).getOrCheckArtifact(queryType);
   }

   /**
    * search for exactly one artifact based on its type and name - otherwise throw an exception
    *
    * @return exactly one artifact based on its type and name - otherwise throw an exception
    * @throws ArtifactDoesNotExist if no artifacts are found
    * @throws MultipleArtifactsExist if more than one artifact is found
    */
   public static Artifact getArtifactFromTypeAndName(IArtifactType artifactType, String artifactName, BranchId branch, QueryOption... options) throws OseeCoreException {
      return queryFromTypeAndAttribute(artifactType, CoreAttributeTypes.Name, artifactName, branch,
         options).getOrCheckArtifact(QueryType.GET);
   }

   /**
    * search for exactly one artifact based on its type and name - otherwise throw an exception
    *
    * @return exactly one artifact based on its type and name, otherwise null
    * @throws MultipleArtifactsExist if more than one artifact is found
    */
   public static Artifact getArtifactFromTypeAndNameNoException(IArtifactType artifactType, String artifactName, BranchId branch, QueryOption... options) throws OseeCoreException {
      try {
         return queryFromTypeAndAttribute(artifactType, CoreAttributeTypes.Name, artifactName, branch,
            options).getOrCheckArtifact(QueryType.GET);
      } catch (ArtifactDoesNotExist ex) {
         // do nothing
      }
      return null;
   }

   public static List<Integer> selectArtifactIdsFromTypeAndName(IArtifactType artifactType, String artifactName, BranchId branch, QueryOption... options) throws OseeCoreException {
      return queryFromTypeAndAttribute(artifactType, CoreAttributeTypes.Name, artifactName, branch,
         options).selectArtifacts(2);
   }

   public static List<Integer> selectArtifactIdsFromTypeAndAttribute(IArtifactType artifactType, IAttributeType attributeType, String attributeValue, BranchId branch, QueryOption... options) throws OseeCoreException {
      return queryFromTypeAndAttribute(artifactType, attributeType, attributeValue, branch, options).selectArtifacts(
         10000);
   }

   /**
    * Checks for existence of an artifact based on its type and name
    *
    * @return one artifact based on its type and name if it exists, otherwise null
    */
   public static Artifact checkArtifactFromTypeAndName(IArtifactType artifactTypeToken, String artifactName, BranchId branch, QueryOption... options) throws OseeCoreException {
      return queryFromTypeAndAttribute(artifactTypeToken, CoreAttributeTypes.Name, artifactName, branch,
         options).getOrCheckArtifact(QueryType.CHECK);
   }

   /**
    * search for un-deleted artifacts with any of the given artifact ids
    *
    * @return a collection of the artifacts found or an empty collection if none are found
    */
   public static List<Artifact> getArtifactListFromIds(Collection<Integer> artifactIds, BranchId branch) throws OseeCoreException {
      return ArtifactLoader.loadArtifacts(artifactIds, branch, LoadLevel.ALL, INCLUDE_CACHE, INCLUDE_DELETED);
   }

   /**
    * search for artifacts with any of the given artifact ids
    *
    * @return a collection of the artifacts found or an empty collection if none are found
    */
   public static List<Artifact> getArtifactListFromIds(Collection<Integer> artifactIds, BranchId branch, DeletionFlag allowDeleted) throws OseeCoreException {
      return ArtifactLoader.loadArtifacts(artifactIds, branch, LoadLevel.ALL, INCLUDE_CACHE, allowDeleted);
   }

   /**
    * search for artifacts with any of the given artifact guids
    *
    * @return a collection of the artifacts found or an empty collection if none are found
    */
   public static List<Artifact> getArtifactListFromIds(List<String> guids, BranchId branch) throws OseeCoreException {
      return new ArtifactQueryBuilder(guids, branch, ALL).getArtifacts(30, null);
   }

   public static List<Artifact> getArtifactListFromIds(List<String> guids, BranchId branch, DeletionFlag allowDeleted) throws OseeCoreException {
      return new ArtifactQueryBuilder(guids, branch, allowDeleted, ALL).getArtifacts(30, null);
   }

   public static List<Artifact> getHistoricalArtifactListFromIds(List<String> guids, TransactionRecord transactionId, DeletionFlag allowDeleted) throws OseeCoreException {
      return new ArtifactQueryBuilder(guids, transactionId, allowDeleted, ALL).getArtifacts(30, null);
   }

   public static List<Artifact> getHistoricalArtifactListFromIds(Collection<Integer> artifactIds, TransactionRecord transactionId, DeletionFlag allowDeleted) throws OseeCoreException {
      return new ArtifactQueryBuilder(artifactIds, transactionId, allowDeleted, ALL).getArtifacts(30, null);
   }

   public static Artifact getHistoricalArtifactFromId(int artifactId, TransactionRecord transactionId, DeletionFlag allowDeleted) throws OseeCoreException {
      return new ArtifactQueryBuilder(artifactId, transactionId, allowDeleted, ALL).getOrCheckArtifact(QueryType.GET);
   }

   public static Artifact getHistoricalArtifactFromId(String guid, TransactionRecord transactionId, DeletionFlag allowDeleted) throws OseeCoreException {
      return new ArtifactQueryBuilder(Arrays.asList(guid), transactionId, allowDeleted, ALL).getOrCheckArtifact(
         QueryType.GET);
   }

   public static Artifact checkHistoricalArtifactFromId(int artifactId, TransactionRecord transactionId, DeletionFlag allowDeleted) throws OseeCoreException {
      return new ArtifactQueryBuilder(artifactId, transactionId, allowDeleted, ALL).getOrCheckArtifact(QueryType.CHECK);
   }

   public static Artifact checkHistoricalArtifactFromId(String guid, TransactionRecord transactionId, DeletionFlag allowDeleted) throws OseeCoreException {
      return new ArtifactQueryBuilder(Arrays.asList(guid), transactionId, allowDeleted, ALL).getOrCheckArtifact(
         QueryType.CHECK);
   }

   public static List<Artifact> getArtifactListFromName(String artifactName, BranchId branch, DeletionFlag allowDeleted, QueryOption... options) throws OseeCoreException {
      return new ArtifactQueryBuilder(branch, ALL, allowDeleted,
         new AttributeCriteria(CoreAttributeTypes.Name, artifactName, options)).getArtifacts(30, null);
   }

   public static List<Artifact> getArtifactListFromTypeAndName(IArtifactType artifactType, String artifactName, BranchId branch, QueryOption... options) throws OseeCoreException {
      return getArtifactListFromTypeAndAttribute(artifactType, CoreAttributeTypes.Name, artifactName, branch, options);
   }

   /**
    * search for exactly one artifact based on its type and an attribute of a given type and value - otherwise throw an
    * exception
    *
    * @return a collection of the artifacts found or an empty collection if none are found
    * @throws ArtifactDoesNotExist if no artifacts are found
    * @throws MultipleArtifactsExist if more than one artifact is found
    */
   public static Artifact getArtifactFromTypeAndAttribute(IArtifactType artifactType, IAttributeType attributeType, String attributeValue, BranchId branch) throws OseeCoreException {
      return queryFromTypeAndAttribute(artifactType, attributeType, attributeValue, branch).getOrCheckArtifact(
         QueryType.GET);
   }

   /**
    * search for exactly one artifact based on its type and an attribute of a given type and value - otherwise throw an
    * exception
    *
    * @return a collection of the artifacts found or an empty collection if none are found
    * @throws ArtifactDoesNotExist if no artifacts are found
    * @throws MultipleArtifactsExist if more than one artifact is found
    */
   public static Artifact getArtifactFromAttribute(IAttributeType attributeType, String attributeValue, BranchId branch) throws OseeCoreException {
      return new ArtifactQueryBuilder(branch, ALL, EXCLUDE_DELETED,
         new AttributeCriteria(attributeType, attributeValue)).getOrCheckArtifact(QueryType.GET);
   }

   /**
    * Does not return any inherited artifacts. Use getArtifactListFromTypeWithInheritence instead.
    *
    * @param artifactTypeToken
    * @param branch
    * @param allowDeleted
    * @return
    * @throws OseeCoreException
    */
   public static List<Artifact> getArtifactListFromType(IArtifactType artifactTypeToken, BranchId branch, DeletionFlag allowDeleted) throws OseeCoreException {
      return new ArtifactQueryBuilder(artifactTypeToken, branch, ALL, allowDeleted).getArtifacts(1000, null);
   }

   public static List<Artifact> getArtifactListFromType(List<IArtifactType> artifactTypeTokens, BranchId branch, DeletionFlag allowDeleted) throws OseeCoreException {
      return new ArtifactQueryBuilder(artifactTypeTokens, branch, ALL, allowDeleted).getArtifacts(1000, null);
   }

   public static List<Artifact> getArtifactListFromType(IArtifactType artifactTypeToken, BranchId branch) throws OseeCoreException {
      return getArtifactListFromType(artifactTypeToken, branch, EXCLUDE_DELETED);
   }

   public static List<Artifact> getArtifactListFromBranch(BranchId branch, DeletionFlag allowDeleted) throws OseeCoreException {
      return new ArtifactQueryBuilder(branch, ALL, allowDeleted).getArtifacts(10000, null);
   }

   public static List<Integer> selectArtifactListFromType(IArtifactType artifactTypeToken, BranchId branch, DeletionFlag allowDeleted) throws OseeCoreException {
      return new ArtifactQueryBuilder(artifactTypeToken, branch, ALL, allowDeleted).selectArtifacts(10000);
   }

   public static List<Integer> selectArtifactListFromBranch(BranchId branch, DeletionFlag allowDeleted) throws OseeCoreException {
      return new ArtifactQueryBuilder(branch, ALL, allowDeleted).selectArtifacts(10000);
   }

   public static List<Artifact> getArtifactListFromBranch(BranchId branch, LoadLevel loadLevel, DeletionFlag allowDeleted) throws OseeCoreException {
      return new ArtifactQueryBuilder(branch, loadLevel, allowDeleted).getArtifacts(10000, null);
   }

   public static List<Artifact> reloadArtifactListFromBranch(BranchId branch, DeletionFlag allowDeleted) throws OseeCoreException {
      return new ArtifactQueryBuilder(branch, ALL, allowDeleted).reloadArtifacts(10000);
   }

   /**
    * do not use this method if searching for a super type and its descendants, instead use getArtifactListFromTypeAnd
    */
   public static List<Artifact> getArtifactListFromTypes(Collection<? extends IArtifactType> artifactTypes, BranchId branch, DeletionFlag allowDeleted) throws OseeCoreException {
      return new ArtifactQueryBuilder(artifactTypes, branch, ALL, allowDeleted).getArtifacts(1000, null);
   }

   public static List<Artifact> getArtifactListFromTypeWithInheritence(IArtifactType artifactType, BranchId branch, DeletionFlag allowDeleted) throws OseeCoreException {
      ArtifactType artifactTypeFull = ArtifactTypeManager.getType(artifactType);
      Collection<ArtifactType> artifactTypes = artifactTypeFull.getAllDescendantTypes();
      artifactTypes.add(artifactTypeFull);
      return getArtifactListFromTypes(artifactTypes, branch, allowDeleted);
   }

   public static int getArtifactCountFromTypeWithInheritence(IArtifactType artifactType, BranchId branch, DeletionFlag allowDeleted) throws OseeCoreException {
      ArtifactType artifactTypeFull = ArtifactTypeManager.getType(artifactType);
      Collection<ArtifactType> artifactTypes = artifactTypeFull.getAllDescendantTypes();
      artifactTypes.add(artifactTypeFull);
      return getArtifactCountFromTypes(artifactTypes, branch, allowDeleted);
   }

   public static int getArtifactCountFromTypes(Collection<? extends IArtifactType> artifactTypes, BranchId branch, DeletionFlag allowDeleted) throws OseeCoreException {
      return new ArtifactQueryBuilder(artifactTypes, branch, ALL, allowDeleted).countArtifacts();
   }

   /**
    * search for artifacts of the given type on a particular branch that satisfy the given criteria
    *
    * @return a collection of the artifacts found or an empty collection if none are found
    */
   public static List<Artifact> getArtifactListFromTypeAnd(IArtifactType artifactType, BranchId branch, int artifactCountEstimate, List<ArtifactSearchCriteria> criteria) throws OseeCoreException {
      return new ArtifactQueryBuilder(artifactType, branch, ALL, criteria).getArtifacts(artifactCountEstimate, null);
   }

   /**
    * search for artifacts on a particular branch that satisfy the given criteria
    *
    * @return a collection of the artifacts found or an empty collection if none are found
    */
   public static List<Artifact> getArtifactListFromCriteria(BranchId branch, int artifactCountEstimate, List<ArtifactSearchCriteria> criteria) throws OseeCoreException {
      return new ArtifactQueryBuilder(branch, ALL, criteria).getArtifacts(artifactCountEstimate, null);
   }

   /**
    * search for artifacts on a particular branch that satisfy the given criteria
    *
    * @return a collection of the artifacts found or an empty collection if none are found
    */
   public static List<Artifact> getArtifactListFromCriteria(BranchId branch, int artifactCountEstimate, ArtifactSearchCriteria... criteria) throws OseeCoreException {
      return new ArtifactQueryBuilder(branch, ALL, EXCLUDE_DELETED, criteria).getArtifacts(artifactCountEstimate, null);
   }

   /**
    * search for artifacts related
    *
    * @return a collection of the artifacts found or an empty collection if none are found
    */
   public static List<Artifact> getRelatedArtifactList(Artifact artifact, IRelationType relationType, RelationSide relationSide) throws OseeCoreException {
      return new ArtifactQueryBuilder(artifact.getBranch(), ALL, EXCLUDE_DELETED,
         new RelationCriteria(artifact.getArtId(), relationType, relationSide)).getArtifacts(1000, null);
   }

   /**
    * search for artifacts by relation
    *
    * @return a collection of the artifacts found or an empty collection if none are found
    */
   public static List<Artifact> getArtifactListFromRelation(IRelationType relationType, RelationSide relationSide, BranchId branch) throws OseeCoreException {
      return new ArtifactQueryBuilder(branch, ALL, EXCLUDE_DELETED,
         new RelationCriteria(relationType, relationSide)).getArtifacts(1000, null);
   }

   /**
    * search for artifacts of the given type with an attribute of the given type and value
    *
    * @return a collection of the artifacts found or an empty collection if none are found
    */
   public static List<Artifact> getArtifactListFromTypeAndAttribute(IArtifactType artifactType, IAttributeType attributeType, String attributeValue, BranchId branch, QueryOption... options) throws OseeCoreException {
      return new ArtifactQueryBuilder(artifactType, branch, ALL,
         new AttributeCriteria(attributeType, attributeValue, options)).getArtifacts(100, null);
   }

   public static List<Artifact> getArtifactListFromAttribute(IAttributeType attributeType, String attributeValue, BranchId branch, QueryOption... options) throws OseeCoreException {
      return new ArtifactQueryBuilder(branch, ALL, EXCLUDE_DELETED,
         new AttributeCriteria(attributeType, attributeValue, options)).getArtifacts(300, null);
   }

   /**
    * Return all artifacts that have one or more attributes of given type regardless of the value
    */
   public static List<Artifact> getArtifactListFromAttributeType(IAttributeType attributeType, BranchId branch) throws OseeCoreException {
      return new ArtifactQueryBuilder(branch, ALL, EXCLUDE_DELETED, new AttributeCriteria(attributeType)).getArtifacts(
         300, null);
   }

   private static ArtifactQueryBuilder queryFromTypeAndAttribute(IArtifactType artifactType, IAttributeType attributeType, String attributeValue, BranchId branch, QueryOption... options) {
      return new ArtifactQueryBuilder(artifactType, branch, ALL,
         new AttributeCriteria(attributeType, attributeValue, options));
   }

   public static List<Artifact> getArtifactListFromTypeAndAttribute(IArtifactType artifactType, IAttributeType attributeType, Collection<String> attributeValues, BranchId branch, int artifactCountEstimate) throws OseeCoreException {
      return new ArtifactQueryBuilder(artifactType, branch, ALL,
         new AttributeCriteria(attributeType, attributeValues)).getArtifacts(artifactCountEstimate, null);
   }

   public static List<Artifact> getArtifactListFromAttributeValues(IAttributeType attributeType, Collection<String> attributeValues, BranchId branch, int artifactCountEstimate) throws OseeCoreException {
      return new ArtifactQueryBuilder(branch, ALL, EXCLUDE_DELETED,
         new AttributeCriteria(attributeType, attributeValues)).getArtifacts(artifactCountEstimate, null);
   }

   /**
    * Searches for artifacts having attributes which contain matching keywords entered in the query string.
    * <p>
    * Special characters such as (<b><code>' '</code>, <code>!</code>, <code>"</code>, <code>#</code>, <code>$</code>,
    * <code>%</code>, <code>(</code>, <code>)</code>, <code>*</code>, <code>+</code>, <code>,</code>, <code>-</code>,
    * <code>.</code>, <code>/</code>, <code>:</code>, <code>;</code>, <code>&lt;</code>, <code>&gt;</code>,
    * <code>?</code>, <code>@</code>, <code>[</code>, <code>\</code>, <code>]</code>, <code>^</code>, <code>{</code>,
    * <code>|</code>, <code>}</code>, <code>~</code>, <code>_</code></b>) are assumed to be word separators.
    * </p>
    * <p>
    * For example:
    * <ul>
    * <li><b>'<code>hello.world</code>'</b> will be translated to <b>'<code>hello</code>'</b> and <b>'<code>world</code>
    * '</b>. The search will match attributes with <b>'<code>hello</code>'</b> and <b>'<code>world</code>'</b> keywords.
    * </li>
    * </ul>
    * </p>
    *
    * @param queryString keywords to match
    * @param matchWordOrder <b>true</b> ensures the query string words exist in order; <b>false</b> matches words in any
    * order
    * @param nameOnly <b>true</b> searches in name attributes only; <b>false</b> includes all tagged attribute types
    * @param allowDeleted <b>true</b> includes deleted artifacts in results; <b>false</b> omits deleted artifacts
    * @return a collection of the artifacts found or an empty collection if none are found
    */
   public static List<Artifact> getArtifactListFromAttributeKeywords(BranchId branch, String queryString, boolean isMatchWordOrder, DeletionFlag deletionFlag, boolean isCaseSensitive, IAttributeType... attributeTypes) throws OseeCoreException {
      QueryBuilderArtifact queryBuilder = createQueryBuilder(branch);
      queryBuilder.includeDeleted(deletionFlag.areDeletedAllowed());
      QueryOption matchCase = QueryOption.getCaseType(isCaseSensitive);
      QueryOption matchWordOrder = QueryOption.getTokenOrderType(isMatchWordOrder);
      Collection<IAttributeType> typesToSearch = attributeTypes.length == 0 ? Collections.singleton(
         QueryBuilder.ANY_ATTRIBUTE_TYPE) : Arrays.asList(attributeTypes);
      queryBuilder.and(typesToSearch, queryString, matchCase, matchWordOrder);
      List<Artifact> toReturn = new LinkedList<>();
      for (Artifact art : queryBuilder.getResults()) {
         toReturn.add(art);
      }
      return toReturn;
   }

   /**
    * Searches for keywords in attributes and returning match location information such as artifact where match was
    * found, attribute containing the match and match location in attribute data.
    *
    * @see #getArtifactsFromAttributeWithKeywords
    * @param findAllMatchLocations when set to <b>true</b> returns all match locations instead of just returning the
    * first one. When returning all match locations, search performance may be slow.
    */
   public static Iterable<ArtifactMatch> getArtifactMatchesFromAttributeKeywords(SearchRequest searchRequest) throws OseeCoreException {
      QueryBuilderArtifact queryBuilder = createQueryBuilder(searchRequest.getBranch());
      SearchOptions options = searchRequest.getOptions();
      queryBuilder.includeDeleted(options.getDeletionFlag().areDeletedAllowed());
      QueryOption matchCase = QueryOption.getCaseType(options.isCaseSensitive());
      QueryOption matchWordOrder = QueryOption.getTokenOrderType(options.isMatchWordOrder());
      QueryOption matchExact = QueryOption.TOKEN_DELIMITER__ANY;
      if (options.isExactMatch()) {
         matchCase = QueryOption.CASE__MATCH;
         matchWordOrder = QueryOption.TOKEN_MATCH_ORDER__MATCH;
         matchExact = QueryOption.TOKEN_DELIMITER__EXACT;
      }

      Collection<IAttributeType> typesToSearch = Conditions.hasValues(
         options.getAttributeTypeFilter()) ? options.getAttributeTypeFilter() : Collections.singleton(
            QueryBuilder.ANY_ATTRIBUTE_TYPE);
      queryBuilder.and(typesToSearch, searchRequest.getRawSearch(), matchCase, matchWordOrder, matchExact);

      if (Conditions.hasValues(options.getArtifactTypeFilter())) {
         queryBuilder.andIsOfType(options.getArtifactTypeFilter());
      }

      return queryBuilder.getMatches();
   }

   public static Artifact reloadArtifactFromId(int artId, BranchId branch) throws OseeCoreException {
      ArtifactQueryBuilder query = new ArtifactQueryBuilder(artId, branch, INCLUDE_DELETED, ALL);
      Artifact artifact = query.reloadArtifact();
      OseeEventManager.kickLocalArtifactReloadEvent(query, Collections.singleton(artifact));
      return artifact;
   }

   public static Collection<? extends Artifact> reloadArtifacts(Collection<? extends Artifact> artifacts) throws OseeCoreException {
      Collection<Artifact> reloadedArts = new ArrayList<Artifact>(artifacts.size());
      HashCollection<BranchId, Artifact> branchMap = new HashCollection<BranchId, Artifact>();
      if (artifacts.isEmpty()) {
         return artifacts;
      }
      for (Artifact artifact : artifacts) {
         // separate/group artifacts by branch since ArtifactQueryBuilder only supports a single branch
         branchMap.put(artifact.getBranch(), artifact);
      }
      Set<Integer> artIds = new HashSet<>();
      for (Entry<BranchId, Collection<Artifact>> entrySet : branchMap.entrySet()) {

         for (Artifact artifact : entrySet.getValue()) {
            artIds.add(artifact.getArtId());
         }

         ArtifactQueryBuilder query = new ArtifactQueryBuilder(artIds, entrySet.getKey(), INCLUDE_DELETED, ALL);

         reloadedArts.addAll(query.reloadArtifacts(artIds.size()));
         OseeEventManager.kickLocalArtifactReloadEvent(query, reloadedArts);
         artIds.clear();
      }
      return reloadedArts;
   }

   public static Artifact getOrCreate(String guid, IArtifactType type, BranchId branch) throws OseeCoreException {
      Artifact artifact = null;
      try {
         artifact = ArtifactQuery.getArtifactFromId(guid, branch);
      } catch (ArtifactDoesNotExist ex) {
         //do nothing since this is expected if the artifact does not exist
      }
      if (artifact == null) {
         Branch fullBranch = BranchManager.getBranch(branch);
         artifact = ArtifactTypeManager.addArtifact(type, fullBranch, null, guid);
      }
      if (artifact == null) {
         throw new ArtifactDoesNotExist("Artifact of type [%s] does not exist on branch [%s]", type, branch);
      }
      return artifact;
   }

   public static QueryBuilderArtifact createQueryBuilder(BranchId branch) throws OseeCoreException {
      OseeClient client = ServiceUtil.getOseeClient();
      QueryBuilder builder = client.createQueryBuilder(branch);

      QueryBuilderProxy handler = new QueryBuilderProxy(builder);

      Class<?>[] types = new Class<?>[] {QueryBuilderArtifact.class};
      QueryBuilderArtifact query =
         (QueryBuilderArtifact) Proxy.newProxyInstance(QueryBuilderArtifact.class.getClassLoader(), types, handler);

      return query;
   }

   private static final class QueryBuilderProxy implements InvocationHandler {

      private final QueryBuilder proxied;

      public QueryBuilderProxy(QueryBuilder proxied) {
         super();
         this.proxied = proxied;
      }

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
         Object toReturn = null;
         Method localMethod = getMethodFor(this.getClass(), method);
         try {
            if (localMethod != null) {
               toReturn = localMethod.invoke(this, args);
            } else {
               toReturn = invokeOnDelegate(proxied, method, args);
            }
         } catch (InvocationTargetException e) {
            throw e.getCause();
         }
         return toReturn;
      }

      protected Object invokeOnDelegate(Object target, Method method, Object[] args) throws Throwable {
         return method.invoke(target, args);
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

      // this method is called from invoke in the localMethod case
      @SuppressWarnings("unused")
      public ResultSet<Artifact> getResults() throws OseeCoreException {
         SearchResult result = proxied.getSearchResult(RequestType.IDS);
         SearchParameters searchParameters = result.getSearchParameters();

         BranchId branch = TokenFactory.createBranch(searchParameters.getBranchUuid());

         TransactionRecord tx = null;
         if (searchParameters.getFromTx() > 0) {
            tx = TransactionManager.getTransactionId(searchParameters.getFromTx());
         }
         DeletionFlag deletionFlag =
            searchParameters.isIncludeDeleted() ? DeletionFlag.INCLUDE_DELETED : DeletionFlag.EXCLUDE_DELETED;

         List<Integer> ids = result.getIds();
         ResultSet<Artifact> toReturn;
         if (ids != null && !ids.isEmpty()) {
            List<Artifact> loadedArtifacts =
               ArtifactLoader.loadArtifacts(ids, branch, LoadLevel.ALL, INCLUDE_CACHE, deletionFlag, tx);
            toReturn = ResultSets.newResultSet(loadedArtifacts);
         } else {
            toReturn = ResultSets.emptyResultSet();
         }
         return toReturn;
      }

      // this method is called from invoke in the localMethod case
      @SuppressWarnings("unused")
      public ResultSet<ArtifactMatch> getMatches() throws OseeCoreException {
         SearchResult result = proxied.getSearchResult(RequestType.MATCHES);
         SearchParameters searchParameters = result.getSearchParameters();

         BranchId branch = TokenFactory.createBranch(searchParameters.getBranchUuid());

         TransactionRecord tx = null;
         if (searchParameters.getFromTx() > 0) {
            tx = TransactionManager.getTransactionId(searchParameters.getFromTx());
         }
         DeletionFlag deletionFlag =
            searchParameters.isIncludeDeleted() ? DeletionFlag.INCLUDE_DELETED : DeletionFlag.EXCLUDE_DELETED;

         Map<Integer, Artifact> artIdToArtifact = new HashMap<>();

         List<Artifact> loadedArtifacts =
            ArtifactLoader.loadArtifacts(result.getIds(), branch, LoadLevel.ALL, INCLUDE_CACHE, deletionFlag, tx);

         for (Artifact art : loadedArtifacts) {
            artIdToArtifact.put(art.getArtId(), art);
         }

         Map<Artifact, ArtifactMatch> matches = new HashMap<>();
         for (SearchMatch match : result.getSearchMatches()) {
            int artId = match.getArtId();
            int attrId = match.getAttrId();
            Artifact art = artIdToArtifact.get(artId);
            if (art != null) {
               ArtifactMatch toAddTo = matches.get(art);
               if (toAddTo == null) {
                  toAddTo = new ArtifactMatch(art);
                  matches.put(art, toAddTo);
               }
               toAddTo.addMatchData(attrId, match.getLocations());
            }
         }

         return ResultSets.newResultSet(matches.values());
      }
   }

   public static Long getUuidFromGuid(String guid, BranchId branch) {
      if (guidToUuid == null) {
         guidToUuid = new HashMap<>(200);
      }
      Long result = null;
      if (guidToUuid.containsKey(guid)) {
         result = guidToUuid.get(guid);
      } else {
         Artifact art = getArtifactFromId(guid, branch);
         if (art != null) {
            result = art.getUuid();
            guidToUuid.put(guid, result);
         }
      }
      return result;
   }

   public static String getGuidFromUuid(long uuid, BranchId branch) {
      if (uuidToGuid == null) {
         uuidToGuid = new HashMap<>(200);
      }
      String result = null;
      if (uuidToGuid.containsKey(uuid)) {
         result = uuidToGuid.get(uuid);
      } else {
         Artifact art = getArtifactFromId(uuid, branch);
         if (art != null) {
            result = art.getGuid();
            uuidToGuid.put(uuid, result);
         }
      }
      return result;
   }

}
