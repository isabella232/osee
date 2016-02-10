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
package org.eclipse.osee.ats.core.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.IAtsConfigObject;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.config.WorkType;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.program.IAtsProgram;
import org.eclipse.osee.ats.api.query.IAtsConfigQuery;
import org.eclipse.osee.ats.api.query.IAtsQueryFilter;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.framework.jdk.core.type.ResultSets;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Donald G. Dunne
 */
public abstract class AbstractAtsConfigQueryImpl implements IAtsConfigQuery {

   protected final List<AtsAttributeQuery> andAttr;
   protected IArtifactType artifactType;
   protected Collection<Long> uuids;
   protected final IAtsServices services;
   protected Collection<Long> aiUuids;
   protected List<Integer> onlyIds = null;
   protected final List<IAtsQueryFilter> queryFilters;

   public AbstractAtsConfigQueryImpl(IAtsServices services) {
      this.services = services;
      andAttr = new ArrayList<>();
      aiUuids = new ArrayList<>();
      uuids = new ArrayList<>();
      queryFilters = new ArrayList<>();
   }

   @Override
   public Collection<Integer> getItemIds() throws OseeCoreException {
      onlyIds = new LinkedList<>();
      getItems();
      return onlyIds;
   }

   public abstract void createQueryBuilder();

   @Override
   public <T extends IAtsConfigObject> Collection<T> getItems() {
      createQueryBuilder();

      if (artifactType != null) {
         queryAndIsOfType(artifactType);
      }

      if (uuids != null && uuids.size() > 0) {
         addUuidCriteria(uuids);
      }

      addAttributeCriteria();

      Set<T> allResults = new HashSet<>();
      collectResults(allResults, artifactType);

      return allResults;
   }

   public abstract Collection<ArtifactId> runQuery();

   @SuppressWarnings("unchecked")
   private <T> Collection<T> collectResults(Set<T> allResults, IArtifactType artifactType) {
      Set<T> results = new HashSet<>();
      if (isOnlyIds()) {
         onlyIds.addAll(queryGetIds());
      }
      // filter on original artifact types
      else {
         Collection<ArtifactId> artifacts = runQuery();
         for (ArtifactId artifact : artifacts) {
            if (artifactType != null || isArtifactTypeMatch(artifact, artifactType)) {
               results.add((T) createFromFactory(artifact));
            }
         }
      }
      addtoResultsWithNullCheck(allResults, results);

      return results;
   }

   @SuppressWarnings("unchecked")
   private <T> T createFromFactory(ArtifactId artifact) {
      return (T) services.getConfigItemFactory().getConfigObject(artifact);
   }

   private <T> void addtoResultsWithNullCheck(Set<T> allResults, Collection<? extends T> configObjects) {
      if (configObjects.contains(null)) {
         OseeLog.log(AbstractAtsConfigQueryImpl.class, Level.SEVERE, "Null found in results");
      } else {
         allResults.addAll(configObjects);
      }
   }

   private boolean isArtifactTypeMatch(ArtifactId artifact, IArtifactType artType) {
      if (artType == null) {
         return true;
      }
      if (services.getArtifactResolver().isOfType(artifact, artType)) {
         return true;
      }
      return false;
   }

   public abstract void queryAndNotExists(IRelationTypeSide relationTypeSide);

   public abstract void queryAndExists(IRelationTypeSide relationTypeSide);

   public abstract void queryAndIsOfType(IArtifactType artifactType);

   public boolean isOnlyIds() {
      return onlyIds != null;
   }

   public abstract List<Integer> queryGetIds();

   @Override
   public IAtsConfigQuery isOfType(IArtifactType artifactType) {
      if (this.artifactType != null) {
         throw new OseeArgumentException("Can only specify one artifact type");
      }
      this.artifactType = artifactType;
      return this;
   }

   @Override
   public IAtsConfigQuery andAttr(IAttributeType attributeType, Collection<String> values, QueryOption... queryOptions) throws OseeCoreException {
      andAttr.add(new AtsAttributeQuery(attributeType, values, queryOptions));
      return this;
   }

   @Override
   public IAtsConfigQuery andUuids(Long... uuids) {
      this.uuids = org.eclipse.osee.framework.jdk.core.util.Collections.getAggregate(uuids);
      return this;
   }

   @Override
   public IAtsConfigQuery andAttr(IAttributeType attributeType, String value, QueryOption... queryOption) {
      return andAttr(attributeType, Collections.singleton(value), queryOption);
   }

   @Override
   public <T extends IAtsConfigObject> ResultSet<T> getResults() {
      return ResultSets.newResultSet(getItems());
   }

   @SuppressWarnings("unchecked")
   @Override
   public <T extends ArtifactId> ResultSet<T> getResultArtifacts() {
      List<T> items = new ArrayList<>();
      for (IAtsConfigObject configObject : getResults()) {
         if (configObject == null) {
            OseeLog.log(AbstractAtsConfigQueryImpl.class, Level.SEVERE, "Null found in results");
         } else {
            items.add((T) configObject.getStoreObject());
         }
      }
      // filter on original artifact types
      List<T> artifacts = new LinkedList<>();
      for (ArtifactId artifact : items) {
         boolean artifactTypeMatch = isArtifactTypeMatch(artifact, artifactType);
         if (artifactTypeMatch) {
            artifacts.add((T) artifact);
         }
      }
      return ResultSets.newResultSet(artifacts);
   }

   public abstract void queryAndIsOfType(List<IArtifactType> artTypes);

   public abstract void queryAnd(IAttributeType attrType, String value);

   public abstract void queryAndRelatedToLocalIds(IRelationTypeSide relationTypeSide, int artId);

   private void addAttributeCriteria() {
      if (!andAttr.isEmpty()) {
         for (AtsAttributeQuery attrQuery : andAttr) {
            queryAnd(attrQuery.getAttrType(), attrQuery.getValues(), attrQuery.getQueryOption());
         }
      }
   }

   public abstract void queryAnd(IAttributeType attrType, Collection<String> values, QueryOption[] queryOption);

   public abstract void queryAnd(IAttributeType attrType, String value, QueryOption[] queryOption);

   private void addUuidCriteria(Collection<Long> uuids) {
      if (uuids != null) {
         List<Integer> artIds = new LinkedList<>();
         for (Long uuid : uuids) {
            artIds.add(uuid.intValue());
         }
         queryAndLocalIds(artIds);
      }
   }

   public abstract void queryAndLocalIds(List<Integer> artIds);

   public abstract void queryAnd(IAttributeType attrType, Collection<String> values);

   public IArtifactType getArtifactType() {
      return artifactType;
   }

   public void setArtifactType(IArtifactType artifactType) {
      this.artifactType = artifactType;
   }

   @Override
   public IAtsConfigQuery andProgram(IAtsProgram program) {
      return andProgram(program.getUuid());
   }

   @Override
   public IAtsConfigQuery andProgram(Long uuid) {
      return andAttr(AtsAttributeTypes.ProgramUuid, Collections.singleton(String.valueOf(uuid)));
   }

   @Override
   public IAtsConfigQuery andWorkType(WorkType workType, WorkType... workTypes) {
      List<String> workTypeStrs = new LinkedList<>();
      workTypeStrs.add(workType.name());
      for (WorkType workType2 : workTypes) {
         workTypeStrs.add(workType2.name());
      }
      return andAttr(AtsAttributeTypes.WorkType, workTypeStrs);
   }

   @Override
   public IAtsConfigQuery andCsci(Collection<String> cscis) {
      return andAttr(AtsAttributeTypes.CSCI, cscis);
   }

}
