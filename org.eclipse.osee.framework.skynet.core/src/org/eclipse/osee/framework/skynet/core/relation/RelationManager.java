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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.info.SQL3DataType;
import org.eclipse.osee.framework.jdk.core.type.CompositeKeyHashMap;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactType;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.CacheArtifactModifiedEvent;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.event.SkynetEventManager;
import org.eclipse.osee.framework.skynet.core.relation.RelationModifiedEvent.ModType;
import org.eclipse.osee.framework.skynet.core.util.ArtifactDoesNotExist;
import org.eclipse.osee.framework.skynet.core.util.MultipleArtifactsExist;

/**
 * @author Ryan D. Brooks
 */
public class RelationManager {
   // the branch is accounted for because Artifact.equals includes the branch in the comparison
   private static final CompositeKeyHashMap<Artifact, RelationType, List<RelationLink>> relationsByType =
         new CompositeKeyHashMap<Artifact, RelationType, List<RelationLink>>(1024);

   private static final HashMap<Artifact, List<RelationLink>> artifactToRelations =
         new HashMap<Artifact, List<RelationLink>>(1024);

   private static final int LINKED_LIST_KEY = -1;

   private static RelationLink getLoadedRelation(Artifact artifact, int aArtifactId, int bArtifactId, RelationType relationType) {
      List<RelationLink> selectedRelations = relationsByType.get(artifact, relationType);
      if (selectedRelations != null) {
         for (RelationLink relation : selectedRelations) {
            if (!relation.isDeleted() && relation.getAArtifactId() == aArtifactId && relation.getBArtifactId() == bArtifactId) {
               return relation;
            }
         }
      }
      return null;
   }

   /**
    * This method should never be called by application code.
    * 
    * @param relationType
    * @param aArtifactId
    * @param bArtifactId
    * @param aBranch
    * @param bBranch
    * @return
    */
   public static RelationLink getLoadedRelation(RelationType relationType, int aArtifactId, int bArtifactId, Branch aBranch, Branch bBranch) {
      Artifact artifactA = ArtifactCache.getActive(aArtifactId, aBranch);
      Artifact artifactB = ArtifactCache.getActive(bArtifactId, bBranch);

      RelationLink relation = null;
      if (artifactA != null) {
         relation = getLoadedRelation(artifactA, aArtifactId, bArtifactId, relationType);
      }
      if (artifactB != null && relation == null) {
         relation = getLoadedRelation(artifactB, aArtifactId, bArtifactId, relationType);
      }
      return relation;
   }

   /**
    * Store the newly instantiated relation from the perspective of relationSide in its appropriate order
    * 
    * @param relation
    * @param relationSide
    */
   public static void manageRelation(RelationLink relation, RelationSide relationSide) {
      Artifact artifact =
            ArtifactCache.getActive(relation.getArtifactId(relationSide), relation.getBranch(relationSide));

      if (artifact != null && (!artifact.isLinksLoaded() || !relation.isInDb())) {
         List<RelationLink> artifactsRelations = artifactToRelations.get(artifact);
         if (artifactsRelations == null) {
            artifactsRelations = Collections.synchronizedList(new ArrayList<RelationLink>(4));
            artifactToRelations.put(artifact, artifactsRelations);
         }
         if (artifactsRelations.contains(relation)) {
            System.out.printf("%s  Rel: %d, artA: %d, artB: %d, \n", relation.getRelationType().getTypeName(),
                  relation.getRelationId(), relation.getAArtifactId(), relation.getBArtifactId());
            return;
         }

         artifactsRelations.add(relation);

         List<RelationLink> selectedRelations = relationsByType.get(artifact, relation.getRelationType());
         if (selectedRelations == null) {
            selectedRelations = Collections.synchronizedList(new ArrayList<RelationLink>(4));
            relationsByType.put(artifact, relation.getRelationType(), selectedRelations);
         }
         RelationSide sideToSort = relation.getSide(artifact).oppositeSide();
         selectedRelations.add(relation);

         //sort the relations
         int artId = LINKED_LIST_KEY;
         int lastArtId = LINKED_LIST_KEY;
         for (int i = 0; i < selectedRelations.size(); i++) {
            if (selectedRelations.get(i).getSide(artifact).oppositeSide() == sideToSort) {
               for (int j = i; j < selectedRelations.size(); j++) {
                  if (selectedRelations.get(j).getSide(artifact).oppositeSide() == sideToSort) {
                     lastArtId = selectedRelations.get(j).getArtifactId(sideToSort);
                     int newId = selectedRelations.get(j).getOrder(sideToSort);
                     if (newId == artId) {
                        if (i != j) {
                           selectedRelations.add(i, selectedRelations.remove(j));
                        }
                        break;
                     }
                  }
               }
               artId = lastArtId;
            }
         }
      }
   }

   private static List<Artifact> getRelatedArtifacts(Artifact artifact, RelationType relationType, RelationSide relationSide) throws ArtifactDoesNotExist, SQLException {
      List<RelationLink> selectedRelations = null;
      if (relationType == null) {
         selectedRelations = artifactToRelations.get(artifact);
      } else {
         selectedRelations = relationsByType.get(artifact, relationType);
      }
      if (selectedRelations == null) {
         return Collections.emptyList();
      }
      ArrayList<Artifact> artifacts = new ArrayList<Artifact>(selectedRelations.size());

      if (needsBulkLoad(selectedRelations, artifact, relationSide)) {
         if (relationSide == null) {
            ArtifactQuery.getRelatedArtifacts(artifact, relationType, RelationSide.SIDE_A);
            ArtifactQuery.getRelatedArtifacts(artifact, relationType, RelationSide.SIDE_B);
         } else {
            ArtifactQuery.getRelatedArtifacts(artifact, relationType, relationSide);
         }
      }

      for (RelationLink relation : selectedRelations) {
         if (!relation.isDeleted()) {
            if (relationSide == null) {
               artifacts.add(relation.getArtifactOnOtherSide(artifact));
            } else {
               // only select relations where the related artifact is on relationSide
               // (and thus on the side opposite of "artifact")
               if (relation.getSide(artifact) != relationSide) {
                  artifacts.add(relation.getArtifact(relationSide));
               }
            }
         }
      }
      return artifacts;
   }

   private static boolean needsBulkLoad(List<RelationLink> selectedRelations, Artifact artifact, RelationSide relationSide) throws ArtifactDoesNotExist, SQLException {
      for (RelationLink relation : selectedRelations) {
         if (!relation.isDeleted()) {
            if (relationSide == null) {
               Artifact temp = relation.getArtifactOnOtherSideIfLoaded(artifact);
               if (temp == null) {
                  return true;
               }
            } else {
               if (relation.getSide(artifact) != relationSide) {
                  Artifact temp = relation.getArtifactIfLoaded(relationSide);
                  if (temp == null) {
                     return true;
                  }
               }
            }
         }
      }
      return false;
   }

   public static List<Artifact> getRelatedArtifactsAll(Artifact artifact) throws ArtifactDoesNotExist, SQLException {
      return getRelatedArtifacts(artifact, null, null);
   }

   public static List<Artifact> getRelatedArtifacts(Artifact artifact, RelationType relationType) throws ArtifactDoesNotExist, SQLException {
      return getRelatedArtifacts(artifact, relationType, null);
   }

   public static List<Artifact> getRelatedArtifacts(Artifact artifact, IRelationEnumeration relationEnum) throws ArtifactDoesNotExist, SQLException {
      return getRelatedArtifacts(artifact, relationEnum.getRelationType(), relationEnum.getSide());
   }

   private static Artifact getRelatedArtifact(Artifact artifact, RelationType relationType, RelationSide relationSide) throws ArtifactDoesNotExist, SQLException, MultipleArtifactsExist {
      List<Artifact> artifacts = getRelatedArtifacts(artifact, relationType, relationSide);

      if (artifacts.size() == 0) {
         throw new ArtifactDoesNotExist(String.format("There is no artifact related to %s by a relation of type %s",
               artifact, relationType));
      }

      if (artifacts.size() > 1) {
         throw new MultipleArtifactsExist(
               String.format(
                     "There are %s artifacts related to \"%s\" by a relation of type \"%s\" on side %s instead of the expected 1.",
                     artifacts.size(), artifact, relationType, relationSide.toString()));
      }
      return artifacts.get(0);
   }

   public static Artifact getRelatedArtifact(Artifact artifact, IRelationEnumeration relationEnum) throws ArtifactDoesNotExist, SQLException, MultipleArtifactsExist {
      return getRelatedArtifact(artifact, relationEnum.getRelationType(), relationEnum.getSide());
   }

   public static int getRelatedArtifactsCount(Artifact artifact, RelationType relationType, RelationSide relationSide) {
      List<RelationLink> selectedRelations = relationsByType.get(artifact, relationType);

      int artifactCount = 0;
      if (selectedRelations != null) {
         for (RelationLink relation : selectedRelations) {
            if (!relation.isDeleted()) {
               if (relationSide == null) {
                  artifactCount++;
               } else {
                  // only select relations where the related artifact is on the side specified by relationEnum
                  // (and thus on the side opposite of "artifact")
                  if (relation.getSide(artifact) != relationSide) {
                     artifactCount++;
                  }
               }
            }
         }
      }

      return artifactCount;
   }

   /**
    * @param artifact
    * @deprecated
    */
   public static void revertRelationsFor(Artifact artifact) {
      //TODO This is inappropriate to use as references held to links by other applications will be invalid.
      artifactToRelations.remove(artifact);
      relationsByType.remove(artifact);
   }

   public static boolean hasDirtyLinks(Artifact artifact) {
      List<RelationLink> selectedRelations = artifactToRelations.get(artifact);
      if (selectedRelations == null) {
         return false;
      }
      for (RelationLink relation : selectedRelations) {
         if (relation.isDirty() && !relation.isDeleted()) {
            return true;
         }
      }
      return false;
   }

   public static void persistRelationsFor(Artifact artifact) throws SQLException {
      List<RelationLink> selectedRelations = artifactToRelations.get(artifact);
      if (selectedRelations != null) {
         for (RelationLink relation : selectedRelations) {
            if (relation.isDirty()) {
               RelationPersistenceManager.makePersistent(relation);
            }
         }
      }
   }

   public static List<RelationLink> getRelationsAll(Artifact artifact) {
      List<RelationLink> selectedRelations = artifactToRelations.get(artifact);

      if (selectedRelations == null) {
         return Collections.emptyList();
      }

      List<RelationLink> relations = new ArrayList<RelationLink>(selectedRelations.size());
      for (RelationLink relation : selectedRelations) {
         if (!relation.isDeleted()) {
            relations.add(relation);
         }
      }
      return relations;
   }

   public static List<RelationLink> getRelations(Artifact artifact, RelationType relationType, RelationSide relationSide) {
      List<RelationLink> selectedRelations = relationsByType.get(artifact, relationType);
      if (selectedRelations == null) {
         return Collections.emptyList();
      }

      List<RelationLink> relations = new ArrayList<RelationLink>(selectedRelations.size());

      for (RelationLink relation : selectedRelations) {
         if (!relation.isDeleted()) {
            if (relationSide == null) {
               relations.add(relation);
            } else {
               // only select relations where the related artifact is on the side specified by relationEnum
               // (and thus on the side opposite of "artifact")
               if (relation.getSide(artifact) != relationSide) {
                  relations.add(relation);
               }
            }
         }
      }
      return relations;
   }

   /**
    * @param relationType
    * @param artifactA
    * @param artifactB
    * @param rationale
    * @throws SQLException
    */
   public static void addRelation(RelationType relationType, Artifact artifactA, Artifact artifactB, String rationale) throws SQLException {
      ensureRelationCanBeAdded(relationType, artifactA, artifactB);

      RelationLink relation = getLoadedRelation(artifactA, artifactA.getArtId(), artifactB.getArtId(), relationType);

      if (relation == null) {
         relation = new RelationLink(artifactA, artifactB, relationType, rationale);

         relation.setDirty();
         RelationManager.manageRelation(relation, RelationSide.SIDE_A);
         RelationManager.manageRelation(relation, RelationSide.SIDE_B);
      }
      SkynetEventManager.getInstance().kick(
            new CacheRelationModifiedEvent(relation, relation.getABranch(), relation.getRelationType().getTypeName(),
                  relation.getASideName(), ModType.Added, RelationManager.class));
   }

   public static void ensureRelationCanBeAdded(RelationType relationType, Artifact artifactA, Artifact artifactB) throws SQLException {
      ensureSideWillSupport(artifactA, relationType, RelationSide.SIDE_A, artifactA.getArtifactType(), 1);
      ensureSideWillSupport(artifactB, relationType, RelationSide.SIDE_B, artifactB.getArtifactType(), 1);
   }

   /**
    * Check whether artifactCount number of additional artifacts of type artifactType can be related to the artifact on
    * side relationSide for relations of type relationType
    * 
    * @param relationType
    * @param relationSide
    * @param artifact
    * @param artifactCount
    * @throws SQLException
    */
   public static void ensureSideWillSupport(Artifact artifact, RelationType relationType, RelationSide relationSide, ArtifactType artifactType, int artifactCount) throws SQLException {
      int maxCount = RelationTypeManager.getRelationSideMax(relationType, artifactType, relationSide);
      int usedCount = getRelatedArtifactsCount(artifact, relationType, relationSide.oppositeSide());

      if (maxCount == 0) {
         throw new IllegalArgumentException(String.format(
               "Artifact \"%s\" of type \"%s\" does not belong on side \"%s\" of relation \"%s\"",
               artifact.getDescriptiveName(), artifact.getArtifactTypeName(), relationType.getSideName(relationSide),
               relationType.getTypeName()));
      } else if (maxCount == 1 && usedCount + artifactCount > maxCount) {
         throw new IllegalArgumentException(
               String.format(
                     "Artifact \"%s\" of type \"%s\" can not be added to \"%s\" of relation \"%s\" because doing so would exceed the side maximum of %d for this artifact type",
                     artifact.getDescriptiveName(), artifact.getArtifactTypeName(), relationSide.toString(),
                     relationType.getTypeName(), maxCount));
      }
   }

   public static void deleteRelation(RelationType relationType, Artifact artifactA, Artifact artifactB) {
      getLoadedRelation(artifactA, artifactA.getArtId(), artifactB.getArtId(), relationType).delete();
   }

   public static void deleteRelationsAll(Artifact artifact) {
      List<RelationLink> selectedRelations = artifactToRelations.get(artifact);
      if (selectedRelations != null) {
         for (RelationLink relation : selectedRelations) {
            relation.delete();
         }
      }
   }

   public static void deleteRelations(Artifact artifact, RelationType relationType, RelationSide relationSide) {
      List<RelationLink> selectedRelations = relationsByType.get(artifact, relationType);
      if (selectedRelations != null) {
         for (RelationLink relation : selectedRelations) {
            if (relationSide == null) {
               relation.delete();
            } else {
               if (relation.getSide(artifact) != relationSide) {
                  relation.delete();
               }
            }
         }
      }
   }

   /**
    * Remove all relations stored in the list awaiting to be deleted.
    * 
    * @throws SQLException
    */
   public static void purgeRelationsFor(Artifact artifact) throws SQLException {
      Collection<RelationLink> links = artifactToRelations.get(artifact);
      if (!links.isEmpty()) {
         List<Object[]> batchArgs = new ArrayList<Object[]>(links.size());
         String PURGE_RELATION = "Delete from osee_define_rel_link WHERE rel_link_id = ?";
         for (RelationLink link : links) {
            batchArgs.add(new Object[] {SQL3DataType.INTEGER, link.getRelationId()});
            link.markAsPurged();
         }
         ConnectionHandler.runPreparedUpdateBatch(PURGE_RELATION, batchArgs);
      }
   }

   /**
    * @param targetLink
    * @param dropLink
    * @throws SQLException
    */
   private static void addRelationAndModifyOrder(Artifact sourceArtifact, Artifact movedArtifact, RelationLink targetLink, boolean infront) throws SQLException {

      RelationSide side = targetLink.getSide(sourceArtifact);
      Artifact artA = null;
      Artifact artB = null;
      if (RelationSide.SIDE_A == side) {
         artA = sourceArtifact;
         artB = movedArtifact;
      } else {
         artA = movedArtifact;
         artB = sourceArtifact;
      }

      RelationLink relationToModify =
            getLoadedRelation(targetLink.getRelationType(), artA.getArtId(), artB.getArtId(), artA.getBranch(),
                  artB.getBranch());
      if (relationToModify == null) {
         RelationManager.addRelation(targetLink.getRelationType(), artA, artB, "");
         relationToModify =
               getLoadedRelation(targetLink.getRelationType(), artA.getArtId(), artB.getArtId(), artA.getBranch(),
                     artB.getBranch());
      }
      if (relationToModify == targetLink) {
         return;
      }
      List<RelationLink> selectedRelations = relationsByType.get(sourceArtifact, targetLink.getRelationType());
      selectedRelations.remove(relationToModify);
      selectedRelations.add(
            infront ? selectedRelations.indexOf(targetLink) : selectedRelations.indexOf(targetLink) + 1,
            relationToModify);

      int lastArtId = LINKED_LIST_KEY;
      for (RelationLink link : selectedRelations) {
         if (!link.isDeleted() && link.getSide(sourceArtifact) == side) {
            if (link.getOrder(side.oppositeSide()) != lastArtId) {
               link.setOrder(side.oppositeSide(), lastArtId);
            }
            lastArtId = link.getArtifactId(side.oppositeSide());
         }
      }
      SkynetEventManager.getInstance().kick(
            new CacheArtifactModifiedEvent(sourceArtifact,
                  org.eclipse.osee.framework.skynet.core.artifact.ArtifactModifiedEvent.ModType.Changed, null));
   }

   /**
    * @param targetLink
    * @param dropLink
    * @throws SQLException
    */
   public static void addRelationAndModifyOrder(Artifact parentArtifact, Artifact targetArtifact, Artifact[] movedArts, RelationType type, boolean infront) throws SQLException {
      RelationLink targetRelation =
            getLoadedRelation(parentArtifact, parentArtifact.getArtId(), targetArtifact.getArtId(), type);
      if (targetRelation == null) {
         targetRelation = getLoadedRelation(parentArtifact, targetArtifact.getArtId(), parentArtifact.getArtId(), type);
         if (targetRelation == null) {
            throw new IllegalArgumentException(
                  String.format(
                        "Unable to locate a valid relation using that has [%s] on one side and [%s] on the other of type [%s]",
                        parentArtifact.toString(), targetArtifact.toString(), type.toString()));
         }
      }

      for (int i = movedArts.length - 1; i >= 0; i--) {
         addRelationAndModifyOrder(parentArtifact, movedArts[i], targetRelation, infront);
      }
   }
}