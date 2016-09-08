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
package org.eclipse.osee.ats.core.client.internal.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.query.IAtsQuery;
import org.eclipse.osee.ats.core.query.AbstractAtsQueryImpl;
import org.eclipse.osee.ats.core.query.AtsAttributeQuery;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.framework.jdk.core.util.ElapsedTime;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.artifact.search.QueryBuilderArtifact;

/**
 * @author Donald G. Dunne
 */
public class AtsQueryImpl extends AbstractAtsQueryImpl {

   private QueryBuilderArtifact query;

   public AtsQueryImpl(IAtsServices services) {
      super(services);
   }

   @Override
   public Collection<ArtifactId> runQuery() {
      List<ArtifactId> results = new ArrayList<ArtifactId>();
      Iterator<Artifact> iterator = query.getResults().iterator();
      while (iterator.hasNext()) {
         results.add(iterator.next());
      }
      return results;
   }

   @Override
   public void createQueryBuilder() {
      query = ArtifactQuery.createQueryBuilder(AtsUtilCore.getAtsBranch());
   }

   @Override
   public void queryAnd(IAttributeType attrType, Collection<String> values) {
      query.and(attrType, values);
   }

   @Override
   public void queryAndIsOfType(IArtifactType artifactType) {
      query.andIsOfType(artifactType);
   }

   @Override
   public List<Integer> queryGetIds() {
      return query.getIds();
   }

   @Override
   public void queryAndIsOfType(List<IArtifactType> artTypes) {
      query.andIsOfType(artTypes);
   }

   @Override
   public void queryAnd(IAttributeType attrType, String value) {
      query.and(attrType, value);
   }

   @Override
   public void queryAndRelatedToLocalIds(IRelationTypeSide relationTypeSide, int artId) {
      query.andRelatedToLocalIds(relationTypeSide, artId);
   }

   @Override
   public void queryAnd(IAttributeType attrType, Collection<String> values, QueryOption[] queryOption) {
      query.and(attrType, values, queryOption);
   }

   @Override
   public void queryAndRelatedToLocalIds(IRelationTypeSide relationTypeSide, List<Integer> artIds) {
      query.andRelatedToLocalIds(relationTypeSide, artIds);
   }

   @Override
   public void queryAnd(IAttributeType attrType, String value, QueryOption[] queryOption) {
      query.and(attrType, value, queryOption);
   }

   @Override
   public void queryAndLocalIds(List<Integer> artIds) {
      query.andLocalIds(artIds);
   }

   @Override
   public void queryAndNotExists(IRelationTypeSide relationTypeSide) {
      query.andNotExists(relationTypeSide);
   }

   @Override
   public void queryAndExists(IRelationTypeSide relationTypeSide) {
      query.andExists(relationTypeSide);
   }

   @Override
   public List<String> getWorkPackagesForColorTeam(String colorTeam) {
      List<String> workPackageGuids = new LinkedList<>();
      for (Artifact workPackageArt : ArtifactQuery.getArtifactListFromTypeAndAttribute(AtsArtifactTypes.WorkPackage,
         AtsAttributeTypes.ColorTeam, colorTeam, AtsUtilCore.getAtsBranch())) {
         workPackageGuids.add(workPackageArt.getGuid());
      }
      return workPackageGuids;
   }

   @Override
   public List<Integer> getRelatedTeamWorkflowUuidsBasedOnTeamDefsAisAndVersions(List<AtsAttributeQuery> teamWorkflowAttr) {
      AtsQueryImpl search = new AtsQueryImpl(services);
      search.isOfType(AtsArtifactTypes.TeamWorkflow);
      if (teamDefUuids != null && !teamDefUuids.isEmpty()) {
         search.andTeam(new ArrayList<Long>(teamDefUuids));
      }
      if (teamWorkflowAttr != null && !teamWorkflowAttr.isEmpty()) {
         for (AtsAttributeQuery attrQuery : teamWorkflowAttr) {
            search.andAttr.add(attrQuery);
         }
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
   public IAtsQuery andNotExists(IAttributeType attributeType) {
      query.andNotExists(attributeType);
      return this;
   }

}
