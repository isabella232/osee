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

import static org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes.USER_DEFINED;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.ApplicabilityId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.HasBranch;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.RelationSorter;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.skynet.core.relation.order.RelationOrderData;
import org.eclipse.osee.framework.skynet.core.relation.order.RelationOrderFactory;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Jeff C. Phillips
 * @author Ryan D. Brooks
 */
public class RelationLink implements HasBranch {

   public static interface ArtifactLinker {

      public void updateCachedArtifact(int artId, BranchId branch);

      public Artifact getArtifact(int ArtId, BranchId branch) throws OseeCoreException;

      public String getLazyArtifactName(int aArtifactId, BranchId branch);

      public void deleteFromRelationOrder(Artifact aArtifact, Artifact bArtifact, IRelationType relationType) throws OseeCoreException;
   }

   private int relationId;
   private int gammaId;
   private String rationale;
   private final RelationType relationType;
   private boolean dirty;
   private final int aArtifactId;
   private final int bArtifactId;
   private final BranchId branch;
   private ModificationType modificationType;
   private ApplicabilityId applicabilityId;

   private static final boolean SET_DIRTY = true;
   private static final boolean SET_NOT_DIRTY = false;

   private final ArtifactLinker artifactLinker;
   private boolean useBackingData;

   public RelationLink(ArtifactLinker artifactLinker, int aArtifactId, int bArtifactId, BranchId branch, RelationType relationType, int relationId, int gammaId, String rationale, ModificationType modificationType, ApplicabilityId applicabilityId) {
      this.artifactLinker = artifactLinker;
      this.relationType = relationType;
      this.relationId = relationId;
      this.gammaId = gammaId;
      this.rationale = rationale == null ? "" : rationale;
      this.dirty = false;
      this.aArtifactId = aArtifactId;
      this.bArtifactId = bArtifactId;
      this.branch = branch;
      this.applicabilityId = applicabilityId;
      internalSetModType(modificationType, false, false);
   }

   public void internalSetModType(ModificationType modificationType, boolean useBackingData, boolean dirty) {
      this.modificationType = modificationType;
      this.useBackingData = useBackingData;
      this.dirty = dirty;
   }

   public RelationSide getSide(Artifact artifact) {
      if (aArtifactId == artifact.getArtId()) {
         return RelationSide.SIDE_A;
      }
      if (bArtifactId == artifact.getArtId()) {
         return RelationSide.SIDE_B;
      }
      throw new IllegalArgumentException("The artifact " + artifact + " is on neither side of " + this);
   }

   public RelationSide getOppositeSide(Artifact artifact) {
      return getSide(artifact).oppositeSide();
   }

   /**
    * artifact.persist(); artifact.reloadAttributesAndRelations(); Will need to be called afterwards to see replaced
    * data in memory
    */
   public void replaceWithVersion(int gammaId) {
      internalSetPersistenceData(gammaId, ModificationType.REPLACED_WITH_VERSION);
   }

   private void internalSetPersistenceData(int gammaId, ModificationType modType) {
      internalSetModType(modType, true, true);
      internalSetGammaId(gammaId);
   }

   public int getAArtifactId() {
      return aArtifactId;
   }

   public int getBArtifactId() {
      return bArtifactId;
   }

   public int getArtifactId(RelationSide relationSide) {
      return relationSide == RelationSide.SIDE_A ? aArtifactId : bArtifactId;
   }

   public boolean isDeleted() {
      return modificationType.isDeleted();
   }

   public boolean isUnDeleted() {
      return modificationType.isUnDeleted();
   }

   public boolean isDirty() {
      return dirty;
   }

   public ApplicabilityId getApplicabilityId() {
      return applicabilityId;
   }

   public void delete(boolean reorderRelations) {
      delete(reorderRelations, null);
   }

   public void delete(boolean reorderRelations, SkynetTransaction transaction) {
      internalDelete(reorderRelations, true);

      deleteEmptyRelationOrder(transaction);
   }

   private void deleteEmptyRelationOrder(SkynetTransaction transaction) {
      try {
         Artifact aArtifact = ArtifactQuery.getArtifactFromId(aArtifactId, branch, DeletionFlag.INCLUDE_DELETED);

         if (aArtifact.getAttributeCount(CoreAttributeTypes.RelationOrder) == 1) {
            RelationOrderData relationOrderData = new RelationOrderFactory().createRelationOrderData(aArtifact);
            if (!relationOrderData.hasEntries()) {
               aArtifact.getSoleAttribute(CoreAttributeTypes.RelationOrder).delete();
               if (transaction == null) {
                  aArtifact.persist("Delete empty relation order attribute for artifact: " + aArtifact.getGuid());
               } else {
                  aArtifact.persist(transaction);
               }
            }
         }

      } catch (OseeCoreException ex) {
         OseeLog.log(this.getClass(), Level.INFO, ex.toString(), ex);
      }
   }

   public void undelete() {
      internalUnDelete();
   }

   public void internalUnDelete() {
      internalSetModType(ModificationType.UNDELETED, false, true);
   }

   /**
    * This is only for remote events because it does not </br>
    * 1) reorder relations, cause the attribute change came over and was already applied</br>
    * 2) set the relation dirty
    */
   public void internalRemoteEventDelete() {
      internalDelete(false, false);
   }

   private void internalDelete(boolean reorderRelations, boolean setDirty) {
      if (!isDeleted()) {
         if (reorderRelations) {
            try {
               artifactLinker.deleteFromRelationOrder(getArtifactA(), getArtifactB(), getRelationType());
            } catch (OseeCoreException e) {
               OseeLog.log(Activator.class, Level.SEVERE, e.getMessage());
            }
         }

         internalSetModType(ModificationType.DELETED, true, setDirty);
      }
   }

   public void markAsPurged() {
      internalSetModType(ModificationType.DELETED, false, SET_NOT_DIRTY);
   }

   public Artifact getArtifact(RelationSide relationSide) throws OseeCoreException {
      return artifactLinker.getArtifact(getArtifactId(relationSide), getBranch());
   }

   public Artifact getArtifactOnOtherSide(Artifact artifact) throws OseeCoreException {
      return getArtifact(getOppositeSide(artifact));
   }

   public Artifact getArtifactA() throws OseeCoreException {
      return getArtifact(RelationSide.SIDE_A);
   }

   public Artifact getArtifactB() throws OseeCoreException {
      return getArtifact(RelationSide.SIDE_B);
   }

   public String getRationale() {
      return rationale;
   }

   public void setRationale(String rationale) {
      if (rationale == null) {
         rationale = "";
      }
      if (this.rationale.equals(rationale)) {
         return;
      }
      internalSetRationale(rationale);
      internalSetModType(ModificationType.MODIFIED, false, SET_DIRTY);

   }

   public void internalSetRationale(String rationale) {
      if (rationale == null) {
         rationale = "";
      }
      if (this.rationale.equals(rationale)) {
         return;
      }
      this.rationale = rationale;
   }

   public boolean isOfType(IRelationType oseeType) {
      return relationType.equals(oseeType);
   }

   public RelationType getRelationType() {
      return relationType;
   }

   public String getSideNameFor(Artifact artifact) {
      return relationType.getSideName(getSide(artifact));
   }

   public String getSidePhrasingFor(Artifact artifact) throws OseeCoreException {
      return getSidePhrasingFor(artifact, false);
   }

   public String getSidePhrasingForOtherArtifact(Artifact artifact) throws OseeCoreException {
      return getSidePhrasingFor(artifact, true);
   }

   private String getSidePhrasingFor(Artifact artifact, boolean isOtherArtifact) throws OseeCoreException {
      RelationSide side;
      if (artifact.equals(getArtifact(RelationSide.SIDE_A))) {
         side = RelationSide.SIDE_A;
      } else if (artifact.equals(getArtifact(RelationSide.SIDE_B))) {
         side = RelationSide.SIDE_B;
      } else {
         throw new OseeArgumentException("Link does not contain the artifact.");
      }
      if (isOtherArtifact) {
         side = side.oppositeSide();
      }
      return "has (" + getRelationType().getMultiplicity().asLimitLabel(side) + ")";
   }

   @Override
   public String toString() {
      String artAName = artifactLinker.getLazyArtifactName(getAArtifactId(), getBranch());
      String artBName = artifactLinker.getLazyArtifactName(getBArtifactId(), getBranch());
      return String.format("type[%s] id[%d] modType[%s] [%s]: aName[%s] aId[%d] <--> bName[%s] bId[%s]",
         relationType.getName(), relationId, getModificationType(), isDirty() ? "dirty" : "not dirty", artAName,
         aArtifactId, artBName, bArtifactId);
   }

   public void setNotDirty() {
      setDirtyFlag(false);
   }

   public void setDirty() {
      setDirtyFlag(true);
   }

   private void setDirtyFlag(boolean dirty) {
      this.dirty = dirty;
      artifactLinker.updateCachedArtifact(aArtifactId, getBranch());
      artifactLinker.updateCachedArtifact(bArtifactId, getBranch());
   }

   public void internalSetRelationId(int relationId) {
      this.relationId = relationId;
   }

   public int getId() {
      return relationId;
   }

   public int getGammaId() {
      return gammaId;
   }

   public boolean isInDb() {
      return getId() > 0;
   }

   void internalSetGammaId(int gammaId) {
      this.gammaId = gammaId;
   }

   @Override
   public BranchId getBranch() {
      return branch;
   }

   public ModificationType getModificationType() {
      return modificationType;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof RelationLink) {
         RelationLink other = (RelationLink) obj;
         //@formatter:off
         boolean result = aArtifactId == other.aArtifactId &&
         branch.equals(other.branch) &&
         bArtifactId == other.bArtifactId &&
         other.modificationType == modificationType &&
         relationType.equals(other.relationType);
         //@formatter:on

         // This should eventually be removed once DB cleanup occurs
         return result && relationId == other.relationId;
      }
      return false;
   }

   /**
    * Same as equals except don't check relationIds. This is what equals should become once database is cleaned
    * (permanently) of duplicate "conceptual" relations. ex same artA, artB and relationType
    */
   public boolean equalsConceptually(Object obj) {
      if (obj instanceof RelationLink) {
         RelationLink other = (RelationLink) obj;
         //@formatter:off
         return aArtifactId == other.aArtifactId &&
         branch.equals(other.branch) &&
         bArtifactId == other.bArtifactId &&
         other.modificationType == modificationType &&
         relationType.equals(other.relationType);
         //@formatter:on
      }
      return false;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + branch.hashCode();
      result = prime * result + aArtifactId;
      result = prime * result + bArtifactId;
      result = prime * result + relationType.hashCode();
      result = prime * result + modificationType.hashCode();
      return result;
   }

   public boolean isUserDefined() throws OseeCoreException {
      RelationOrderFactory factory = new RelationOrderFactory();
      Artifact aArtifact = ArtifactQuery.getArtifactFromId(getAArtifactId(), branch);
      Artifact bArtifact = ArtifactQuery.getArtifactFromId(getBArtifactId(), branch);

      RelationOrderData leftData = factory.createRelationOrderData(aArtifact);
      RelationOrderData rightData = factory.createRelationOrderData(bArtifact);

      RelationSorter leftSorter = leftData.getCurrentSorterGuid(getRelationType(), getSide(aArtifact));
      RelationSorter rightSorter = rightData.getCurrentSorterGuid(getRelationType(), getSide(bArtifact));

      return rightSorter.equals(USER_DEFINED) && leftSorter.equals(USER_DEFINED);
   }

   public void introduce(int sourceGamma, ModificationType sourceModificationType) {
      internalSetPersistenceData(sourceGamma, sourceModificationType);
   }

   public boolean isUseBackingData() {
      return useBackingData;
   }

}