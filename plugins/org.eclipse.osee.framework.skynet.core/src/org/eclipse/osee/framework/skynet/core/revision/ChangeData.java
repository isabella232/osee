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
package org.eclipse.osee.framework.skynet.core.revision;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.exception.OseeTypeDoesNotExist;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.change.ArtifactChange;
import org.eclipse.osee.framework.skynet.core.change.AttributeChange;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.skynet.core.change.RelationChange;

/**
 * Collection of changes from working branch or transactionId from committed branch.
 * 
 * @author Donald G. Dunne
 */
public class ChangeData {

   public static enum KindType {
      Artifact,
      Relation,
      ArtifactOrRelation,
      RelationOnly
   };

   private final Collection<Change> changes;

   public ChangeData(Collection<Change> changes) {
      this.changes = changes;
   }

   public boolean isEmpty() {
      return getChanges() == null || getChanges().isEmpty();
   }

   public Collection<Change> getChanges() {
      return changes;
   }

   public Collection<Change> getArtifactChangesByName(String name) throws OseeCoreException {
      Collection<Change> foundChanges = new HashSet<Change>();
      try {
         for (Change change : changes) {
            if (change instanceof ArtifactChange) {
               if (change.getArtifactName().equals(name)) {
                  foundChanges.add(change);
               }
            }
         }
         return foundChanges;
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
         return null; // unreachable since wrapAndThrow() always throws an exception
      }
   }

   /**
    * Return artifacts of kind and modType.
    */
   public Collection<Artifact> getArtifacts(KindType kindType, ModificationType... modificationType) throws OseeCoreException {
      return getArtifacts(kindType, null, modificationType);
   }

   /**
    * Return artifacts of kind and modType.
    */
   public Collection<Artifact> getArtifacts(KindType kindType, List<IAttributeType> artifactTypesToIgnore, ModificationType... modificationType) throws OseeCoreException {
      if (kindType == KindType.RelationOnly) {
         return getArtifactsRelationOnly(modificationType);
      }

      Collection<ModificationType> modTypes = Collections.getAggregate(modificationType);
      Conditions.checkExpressionFailOnTrue(modTypes.isEmpty(), "ModificationType must be specified");

      Set<Artifact> artifacts = new HashSet<Artifact>();
      if (kindType == KindType.Artifact || kindType == KindType.ArtifactOrRelation || kindType == KindType.Relation) {
         if (!isEmpty()) {
            HashMap<Integer, Boolean> excludeArtifact = new HashMap<Integer, Boolean>();
            for (Change change : changes) {
               Artifact artifact = change.getChangeArtifact();

               ModificationType modType = change.getModificationType();
               if ((artifactTypesToIgnore != null) && (!artifactTypesToIgnore.isEmpty())) {
                  try {
                     if (change instanceof AttributeChange) {
                        Integer id = artifact.getArtId();
                        long typeId = change.getItemTypeId();
                        AttributeType attributeType = AttributeTypeManager.getType(typeId);
                        if (excludeArtifact.containsKey(id)) {
                           if (!artifactTypesToIgnore.contains(attributeType)) {
                              if (excludeArtifact.get(id)) {
                                 excludeArtifact.put(id, false);
                              }
                           }
                        } else {
                           if (artifactTypesToIgnore.contains(attributeType)) {
                              excludeArtifact.put(id, true);
                           } else {
                              excludeArtifact.put(id, false);
                           }
                        }
                     }
                  } catch (OseeTypeDoesNotExist ex) {
                     /*******************
                      * This is pseudo type that just states the artifact was changed. It does not effect the paragraph
                      * number computation
                      */
                  }
               }
               /**
                * Only way to determine if artifact is of type Merged is to check it's attributes cause the Artifact is
                * of type Modified while attribute is of type merged. Only check attribute change for this case.
                */

               if ((kindType == KindType.Artifact || kindType == KindType.ArtifactOrRelation) && isAttributeChangeMergeType(change)) {
                  if (modTypes.contains(modType)) {
                     artifacts.add(artifact);
                  }
               } else if ((kindType == KindType.Artifact || kindType == KindType.ArtifactOrRelation) && change instanceof ArtifactChange) {
                  if (modTypes.contains(modType)) {
                     artifacts.add(artifact);
                  }
               } else if ((kindType == KindType.Relation || kindType == KindType.ArtifactOrRelation) && change instanceof RelationChange) {
                  if (modTypes.contains(modType)) {
                     artifacts.add(artifact);
                     RelationChange relChange = (RelationChange) change;
                     artifacts.add(relChange.getEndTxBArtifact());
                  }
               }
            }
            if ((artifactTypesToIgnore != null) && (!artifactTypesToIgnore.isEmpty())) {
               Set<Artifact> excludeList = new HashSet<Artifact>();
               for (Artifact artifactToCheck : artifacts) {
                  Integer id = artifactToCheck.getArtId();
                  Boolean remove = excludeArtifact.get(id);
                  if (remove != null) {
                     if (remove) {
                        excludeList.add(artifactToCheck);
                     }
                  }
               }
               artifacts.removeAll(excludeList);
            }
         }
      }
      return artifacts;
   }

   private boolean isAttributeChangeMergeType(Change change) {
      if (change instanceof AttributeChange && change.getModificationType() == ModificationType.MERGED) {
         return true;
      }
      return false;
   }

   private Collection<Artifact> getArtifactsRelationOnly(ModificationType... modificationType) throws OseeCoreException {
      Collection<Artifact> artMod = getArtifacts(KindType.Artifact, modificationType);
      Collection<Artifact> relMod = getArtifacts(KindType.Relation, modificationType);
      return Collections.setComplement(relMod, artMod);
   }

   @Override
   public String toString() {
      try {
         StringBuilder sb = new StringBuilder();
         for (KindType kindType : KindType.values()) {
            for (ModificationType modificationType : ModificationType.values()) {
               Collection<Artifact> artifacts = getArtifacts(kindType, modificationType);
               sb.append(String.format("Kind: %s ModType: %s Num: %s\n", kindType, modificationType.getDisplayName(),
                  artifacts.size()));
            }
         }
         return sb.toString();
      } catch (OseeCoreException ex) {
         return ex.toString();
      }
   }
}
