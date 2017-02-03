/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.rest.internal.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.query.IAtsQuery;
import org.eclipse.osee.ats.core.query.AbstractAtsQueryImpl;
import org.eclipse.osee.ats.core.query.AtsAttributeQuery;
import org.eclipse.osee.ats.rest.IAtsServer;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.core.data.RelationTypeSide;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.search.QueryBuilder;

/**
 * @author Donald G. Dunne
 */
public class AtsQueryImpl extends AbstractAtsQueryImpl {

   private final IAtsServer atsServer;
   private QueryBuilder query;

   public AtsQueryImpl(IAtsServer atsServer) {
      super(atsServer.getServices());
      this.atsServer = atsServer;
   }

   @Override
   public Collection<ArtifactId> runQuery() {
      List<ArtifactId> results = new ArrayList<ArtifactId>();
      Iterator<ArtifactReadable> iterator = query.getResults().iterator();
      while (iterator.hasNext()) {
         results.add(iterator.next());
      }
      return results;
   }

   @Override
   public void createQueryBuilder() {
      query = atsServer.getOrcsApi().getQueryFactory().fromBranch(atsServer.getAtsBranch());
   }

   @Override
   public void queryAnd(AttributeTypeId attrType, Collection<String> values) {
      query.and(attrType, values);
   }

   @Override
   public void queryAndIsOfType(IArtifactType artifactType) {
      query.andIsOfType(artifactType);
   }

   @Override
   public List<ArtifactId> queryGetIds() {
      List<ArtifactId> results = new LinkedList<>();
      Iterator<? extends ArtifactId> iterator = query.getResultsAsLocalIds().iterator();
      while (iterator.hasNext()) {
         results.add(iterator.next());
      }
      return results;
   }

   @Override
   public void queryAndIsOfType(Collection<IArtifactType> artTypes) {
      query.andIsOfType(artTypes);
   }

   @Override
   public void queryAnd(AttributeTypeId attrType, String value) {
      query.and(attrType, value);
   }

   @Override
   public void queryAndRelatedToLocalIds(RelationTypeSide relationTypeSide, ArtifactId artId) {
      query.andRelatedTo(relationTypeSide, artId);
   }

   @Override
   public void queryAndRelatedTo(RelationTypeSide relationTypeSide, List<ArtifactId> artifacts) {
      query.andRelatedTo(relationTypeSide, artifacts);
   }

   @Override
   public void queryAnd(AttributeTypeId attrType, Collection<String> values, QueryOption[] queryOption) {
      query.and(attrType, values, queryOption);
   }

   @Override
   public void queryAnd(AttributeTypeId attrType, String value, QueryOption[] queryOption) {
      query.and(attrType, value, queryOption);
   }

   @Override
   public void queryAndLocalIds(List<Integer> artIds) {
      List<Long> results = new LinkedList<>();
      for (Integer artId : artIds) {
         results.add(new Long(artId));
      }
      query.andUuids(results);
   }

   @Override
   public void queryAndNotExists(RelationTypeSide relationTypeSide) {
      query.andNotExists(relationTypeSide);
   }

   @Override
   public void queryAndExists(RelationTypeSide relationTypeSide) {
      query.andExists(relationTypeSide);
   }

   @Override
   public List<String> getWorkPackagesForColorTeam(String colorTeam) {
      List<String> workPackageGuids = new LinkedList<>();
      for (ArtifactReadable workPackageArt : atsServer.getOrcsApi().getQueryFactory().fromBranch(
         atsServer.getAtsBranch()).andIsOfType(AtsArtifactTypes.WorkPackage).and(AtsAttributeTypes.ColorTeam,
            colorTeam).getResults()) {
         workPackageGuids.add(workPackageArt.getGuid());
      }
      return workPackageGuids;
   }

   @Override
   public List<ArtifactId> getRelatedTeamWorkflowUuidsBasedOnTeamDefsAisAndVersions(List<AtsAttributeQuery> teamWorkflowAttr) {
      AtsQueryImpl search = new AtsQueryImpl(atsServer);
      search.isOfType(AtsArtifactTypes.TeamWorkflow);
      if (teamDefUuids != null && !teamDefUuids.isEmpty()) {
         search.andTeam(new ArrayList<Long>(teamDefUuids));
      }
      if (aiUuids != null && !aiUuids.isEmpty()) {
         search.andActionableItem(new ArrayList<Long>(aiUuids));
      }
      if (versionUuid != null && versionUuid > 0) {
         search.andVersion(versionUuid);
      }
      return new ArrayList<>(search.getItemIds());
   }

   @Override
   public IAtsQuery andNotExists(AttributeTypeId attributeType) {
      query.andNotExists(attributeType);
      return this;
   }
}