/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.skynet.core.artifact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.MultipleArtifactsExist;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.OseeSystemArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.skynet.core.relation.RelationLink;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;

/**
 * @author Ryan D. Brooks
 * @author David W. Miller
 */
public class IntroduceArtifactOperation {
   private final Artifact fosterParent;
   private final IOseeBranch destinationBranch;
   private Collection<Artifact> sourceArtifacts;
   private List<Artifact> destinationArtifacts;

   public IntroduceArtifactOperation(IOseeBranch destinationBranch) throws OseeCoreException {
      this(OseeSystemArtifacts.getDefaultHierarchyRootArtifact(destinationBranch));
   }

   public IntroduceArtifactOperation(Artifact fosterParent) {
      this.fosterParent = fosterParent;
      this.destinationBranch = fosterParent.getBranch();
   }

   /**
    * @return the introduced artifact on the destination branch
    * @throws OseeCoreException
    */
   public Artifact introduce(Artifact sourceArtifact) throws OseeCoreException {
      introduce(Arrays.asList(sourceArtifact));
      return destinationArtifacts.get(0);
   }

   public List<Artifact> introduce(Collection<Artifact> sourceArtifacts) throws OseeCoreException {
      this.sourceArtifacts = sourceArtifacts;
      destinationArtifacts = new ArrayList<>(sourceArtifacts.size());

      for (Artifact sourceArtifact : sourceArtifacts) {
         introduceArtifact(sourceArtifact);
      }
      return destinationArtifacts;
   }

   private void introduceArtifact(Artifact sourceArtifact) throws OseeCoreException {
      Artifact destinationArtifact =
         ArtifactQuery.checkArtifactFromId(sourceArtifact.getArtId(), destinationBranch, DeletionFlag.INCLUDE_DELETED);

      if (destinationArtifact == null) {
         destinationArtifact = sourceArtifact.introduceShallowArtifact(destinationBranch);
         processArtifact(sourceArtifact, destinationArtifact);
      } else {
         destinationArtifact.introduce(sourceArtifact);
         processArtifact(sourceArtifact, destinationArtifact);
      }

      destinationArtifacts.add(destinationArtifact);
   }

   private void processArtifact(Artifact sourceArtifact, Artifact destinationArtifact) throws OseeCoreException {
      introduceAttributes(sourceArtifact, destinationArtifact);

      if (!sourceArtifact.isHistorical()) {
         introduceRelations(sourceArtifact, destinationArtifact);
         try {
            if (sourceArtifact.hasParent() && !destinationArtifact.hasParent() && !sourceArtifacts.contains(sourceArtifact.getParent())) {
               fosterParent.addChild(destinationArtifact);
            }
         } catch (MultipleArtifactsExist ex) {
            fosterParent.addChild(destinationArtifact);
         }
      } else {
         OseeLog.logf(Activator.class, Level.INFO,
            "Historical relations are only supported on the server. Artifact [%s] is historical", sourceArtifact);
      }
   }

   private void introduceAttributes(Artifact sourceArtifact, Artifact destinationArtifact) throws OseeDataStoreException, OseeCoreException {
      List<Attribute<?>> sourceAttributes = sourceArtifact.getAttributes(true);

      removeNewAttributesFromDestination(sourceArtifact, destinationArtifact);

      // introduce the existing attributes
      for (Attribute<?> sourceAttribute : sourceAttributes) {
         // must be valid for the destination branch
         if (destinationArtifact.isAttributeTypeValid(sourceAttribute.getAttributeType())) {
            introduceAttribute(sourceAttribute, destinationArtifact);
         }
      }
   }

   private void introduceAttribute(Attribute<?> sourceAttribute, Artifact destinationArtifact) throws OseeDataStoreException, OseeCoreException {
      if (sourceAttribute.isDirty()) {
         throw new OseeArgumentException(
            "The un-persisted attribute [%s] can not be introduced until it is persisted.", sourceAttribute);

      } else if (sourceAttribute.isInDb()) {
         Attribute<?> destinationAttribute = destinationArtifact.getAttributeById(sourceAttribute.getId(), true);

         if (destinationAttribute == null) {
            destinationArtifact.internalInitializeAttribute(sourceAttribute.getAttributeType(),
               sourceAttribute.getId(), sourceAttribute.getGammaId(), sourceAttribute.getModificationType(), true,
               sourceAttribute.getAttributeDataProvider().getData()).internalSetModType(
               sourceAttribute.getModificationType(), true, true);
         } else {
            destinationAttribute.introduce(sourceAttribute);
         }
      }
   }

   private void introduceRelations(Artifact sourceArtifact, Artifact destinationArtifact) throws OseeCoreException {
      List<RelationLink> sourceRelations = sourceArtifact.getRelationsAll(DeletionFlag.INCLUDE_DELETED);

      for (RelationLink sourceRelation : sourceRelations) {
         // must be valid for the destination branch
         if (destinationArtifact.isRelationTypeValid(sourceRelation.getRelationType())) {
            introduceRelation(sourceRelation, destinationArtifact);
         }
      }
   }

   private void introduceRelation(RelationLink sourceRelation, Artifact destinationArtifact) throws OseeDataStoreException, OseeCoreException {
      if (sourceRelation.isDirty()) {
         throw new OseeArgumentException("The un-persisted relation [%s] can not be introduced until it is persisted.",
            sourceRelation);
      } else if (sourceRelation.isInDb()) {
         RelationLink destinationRelation =
            RelationManager.getLoadedRelationById(sourceRelation.getId(), sourceRelation.getAArtifactId(),
               sourceRelation.getBArtifactId(), destinationBranch);

         if (destinationRelation == null) {
            int aArtifactId = sourceRelation.getAArtifactId();
            int bArtifactId = sourceRelation.getBArtifactId();
            if (doesRelatedArtifactExist(destinationArtifact, aArtifactId, bArtifactId)) {
               ModificationType modType = sourceRelation.getModificationType();
               destinationRelation =
                  RelationManager.getOrCreate(aArtifactId, bArtifactId, destinationBranch,
                     sourceRelation.getRelationType(), sourceRelation.getId(), sourceRelation.getGammaId(),
                     sourceRelation.getRationale(), modType);
               destinationRelation.internalSetModType(modType, true, true);
            }
         } else {
            destinationRelation.introduce(sourceRelation.getGammaId(), sourceRelation.getModificationType());
         }
      }
   }

   private void removeNewAttributesFromDestination(Artifact sourceArtifact, Artifact destinationArtifact) throws OseeCoreException {
      List<Attribute<?>> destAttributes = destinationArtifact.getAttributes(true);

      // since introduce is 'replacing' the destination artifact with the source artifact, 
      // any new attributes from the destination artifact should be removed/deleted.
      for (Attribute<?> destAttribute : destAttributes) {
         Attribute<?> attribute = sourceArtifact.getAttributeById(destAttribute.getId(), true);
         if (attribute == null) {
            destAttribute.delete();
         }
      }
   }

   private boolean doesRelatedArtifactExist(Artifact destinationArtifact, int aArtifactId, int bArtifactId) throws OseeCoreException {
      int checkArtId = destinationArtifact.getArtId() == aArtifactId ? bArtifactId : aArtifactId;

      Artifact otherArtifact =
         ArtifactQuery.checkArtifactFromId(checkArtId, destinationBranch, DeletionFlag.EXCLUDE_DELETED);

      boolean found = otherArtifact != null;

      if (!found) {
         for (Artifact sourceArtifact : sourceArtifacts) {
            if (sourceArtifact.getArtId() == checkArtId) {
               found = true;
               break;
            }
         }
      }
      return found;
   }
}