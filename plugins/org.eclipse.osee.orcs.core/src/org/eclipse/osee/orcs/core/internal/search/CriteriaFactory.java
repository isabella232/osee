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
package org.eclipse.osee.orcs.core.internal.search;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.core.ds.Criteria;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaAllArtifacts;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaArtifactGuids;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaArtifactIds;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaArtifactType;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaAttributeKeywords;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaAttributeTypeExists;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaAttributeTypeNotExists;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaRelatedTo;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaRelationTypeExists;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaRelationTypeFollow;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaRelationTypeNotExists;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaRelationTypeSideExists;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaRelationTypeSideNotExists;
import org.eclipse.osee.orcs.data.ArtifactTypes;
import org.eclipse.osee.orcs.data.AttributeTypes;
import org.eclipse.osee.orcs.search.ArtifactQueryBuilder;

/**
 * @author Roberto E. Escobar
 */
public class CriteriaFactory {

   private final ArtifactTypes artifactTypeCache;
   private final AttributeTypes attributeTypeCache;

   public CriteriaFactory(ArtifactTypes artifactTypeCache, AttributeTypes attributeTypeCache) {
      this.artifactTypeCache = artifactTypeCache;
      this.attributeTypeCache = attributeTypeCache;
   }

   private Collection<? extends IAttributeType> checkForAnyType(Collection<? extends IAttributeType> attributeTypes) throws OseeCoreException {
      Collection<? extends IAttributeType> toReturn;
      if (attributeTypes.contains(ArtifactQueryBuilder.ANY_ATTRIBUTE_TYPE)) {
         Collection<IAttributeType> temp = new LinkedList<IAttributeType>();
         temp.addAll(attributeTypeCache.getAll());
         toReturn = temp;
      } else {
         toReturn = attributeTypes;
      }
      return toReturn;
   }

   public Criteria createExistsCriteria(Collection<? extends IAttributeType> attributeTypes) throws OseeCoreException {
      return new CriteriaAttributeTypeExists(attributeTypes);
   }

   public Criteria createNotExistsCriteria(IAttributeType attributeType) throws OseeCoreException {
      return new CriteriaAttributeTypeNotExists(attributeType);
   }

   public Criteria createExistsCriteria(IRelationType relationType) throws OseeCoreException {
      return new CriteriaRelationTypeExists(relationType);
   }

   public Criteria createExistsCriteria(IRelationTypeSide relationTypeSide) throws OseeCoreException {
      return new CriteriaRelationTypeSideExists(relationTypeSide);
   }

   public Criteria createNotExistsCriteria(IRelationType relationType) {
      return new CriteriaRelationTypeNotExists(relationType);
   }

   public Criteria createNotExistsCriteria(IRelationTypeSide relationTypeSide) {
      return new CriteriaRelationTypeSideNotExists(relationTypeSide);
   }

   public Criteria createAttributeCriteria(Collection<IAttributeType> attributeTypes, Collection<String> values, QueryOption... options) throws OseeCoreException {
      Collection<? extends IAttributeType> types = checkForAnyType(attributeTypes);
      boolean isIncludeAllTypes = attributeTypes.contains(ArtifactQueryBuilder.ANY_ATTRIBUTE_TYPE);
      return new CriteriaAttributeKeywords(isIncludeAllTypes, types, attributeTypeCache, values, options);
   }

   public Criteria createArtifactTypeCriteria(Collection<? extends IArtifactType> artifactTypes) throws OseeCoreException {
      return new CriteriaArtifactType(artifactTypeCache, artifactTypes, false);
   }

   public Criteria createArtifactTypeCriteriaWithInheritance(Collection<? extends IArtifactType> artifactTypes) throws OseeCoreException {
      return new CriteriaArtifactType(artifactTypeCache, artifactTypes, true);
   }

   public Criteria createArtifactIdCriteria(Collection<Integer> artifactIds) throws OseeCoreException {
      return new CriteriaArtifactIds(artifactIds);
   }

   public Criteria createArtifactGuidCriteria(Set<String> guids) throws OseeCoreException {
      return new CriteriaArtifactGuids(guids);
   }

   public Criteria createRelatedToCriteria(IRelationTypeSide relationType, Collection<Integer> artifactIds) throws OseeCoreException {
      return new CriteriaRelatedTo(relationType, artifactIds);
   }

   public Criteria createAllArtifactsCriteria() {
      return new CriteriaAllArtifacts();
   }

   public Criteria createFollowRelationType(IRelationTypeSide relationTypeSide) {
      return new CriteriaRelationTypeFollow(relationTypeSide);
   }
}
