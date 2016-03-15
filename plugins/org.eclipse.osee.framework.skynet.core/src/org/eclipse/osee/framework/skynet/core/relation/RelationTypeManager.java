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
package org.eclipse.osee.framework.skynet.core.relation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.exception.OseeTypeDoesNotExist;
import org.eclipse.osee.framework.core.model.cache.AbstractOseeCache;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.core.services.IOseeCachingService;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;

/**
 * @author Ryan D. Brooks
 * @author Andrew M. Finkbeiner
 */
public class RelationTypeManager {

   private static IOseeCachingService getCacheService() throws OseeCoreException {
      return ServiceUtil.getOseeCacheService();
   }

   public static AbstractOseeCache<RelationType> getCache() throws OseeCoreException {
      return getCacheService().getRelationTypeCache();
   }

   public static AbstractOseeCache<ArtifactType> getArtifactTypeCache() throws OseeCoreException {
      return getCacheService().getArtifactTypeCache();
   }

   public static List<RelationType> getValidTypes(IArtifactType artifactType, IOseeBranch branch) throws OseeCoreException {
      Collection<RelationType> relationTypes = getAllTypes();
      List<RelationType> validRelationTypes = new ArrayList<>();
      for (RelationType relationType : relationTypes) {
         int sideAMax = getRelationSideMax(relationType, artifactType, RelationSide.SIDE_A);
         int sideBMax = getRelationSideMax(relationType, artifactType, RelationSide.SIDE_B);
         boolean onSideA = sideBMax > 0;
         boolean onSideB = sideAMax > 0;
         if (onSideA || onSideB) {
            validRelationTypes.add(relationType);
         }
      }
      return validRelationTypes;
   }

   public static int getRelationSideMax(RelationType relationType, IArtifactType artifactType, RelationSide relationSide) throws OseeCoreException {
      int toReturn = 0;
      ArtifactType type = getArtifactTypeCache().get(artifactType);
      if (relationType.isArtifactTypeAllowed(relationSide, type)) {
         toReturn = relationType.getMultiplicity().getLimit(relationSide);
      }
      return toReturn;
   }

   /**
    * @return all the relation types that are valid for the given branch
    */
   public static Collection<RelationType> getValidTypes(IOseeBranch branch) throws OseeCoreException {
      return getAllTypes();
   }

   /**
    * @return all Relation types
    */
   public static Collection<RelationType> getAllTypes() throws OseeCoreException {
      return getCache().getAll();
   }

   public static RelationType getTypeByGuid(Long guid) throws OseeCoreException {
      RelationType relationType = getCache().getByGuid(guid);
      if (relationType == null) {
         getCacheService().reloadTypes();
         relationType = getCache().getByGuid(guid);
         if (relationType == null) {
            throw new OseeTypeDoesNotExist("The relation with type guid [%s] does not exist", guid);
         }
      }
      return relationType;
   }

   public static RelationType getType(IRelationType relationType) throws OseeCoreException {
      return getCache().get(relationType);
   }

   public static RelationType getType(String typeName) throws OseeCoreException {
      RelationType relationType = getCache().getUniqueByName(typeName);
      if (relationType == null) {
         throw new OseeTypeDoesNotExist("The relation type [%s] does not exist", typeName);
      }
      return relationType;
   }

   public static boolean typeExists(String name) throws OseeCoreException {
      return !getCache().getByName(name).isEmpty();
   }

}
