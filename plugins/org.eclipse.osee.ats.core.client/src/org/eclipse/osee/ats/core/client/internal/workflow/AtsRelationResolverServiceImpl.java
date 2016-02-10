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
package org.eclipse.osee.ats.core.client.internal.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.workdef.IRelationResolver;
import org.eclipse.osee.ats.core.client.IAtsClient;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public class AtsRelationResolverServiceImpl implements IRelationResolver {

   private final IAtsClient atsClient;

   public AtsRelationResolverServiceImpl(IAtsClient atsClient) {
      this.atsClient = atsClient;
   }

   @Override
   public Collection<ArtifactId> getRelated(ArtifactId artifact, IRelationTypeSide relationType) {
      List<ArtifactId> results = new ArrayList<>();
      Artifact useArt = getArtifact(artifact);
      if (useArt != null) {
         for (Artifact art : useArt.getRelatedArtifacts(relationType)) {
            results.add(art);
         }
      }
      return results;
   }

   @SuppressWarnings("unchecked")
   @Override
   public <T extends IAtsObject> Collection<T> getRelated(IAtsObject atsObject, IRelationTypeSide relationType, Class<T> clazz) {
      List<T> results = new ArrayList<>();
      Artifact useArt = getArtifact(atsObject);
      if (useArt != null) {
         for (Artifact art : useArt.getRelatedArtifacts(relationType)) {
            IAtsObject object = getAtsObject(art);
            if (object != null) {
               results.add((T) object);
            }
         }
      }
      return results;
   }

   private IAtsObject getAtsObject(Artifact artifact) {
      IAtsObject result = null;
      if (artifact instanceof IAtsWorkItem) {
         result = atsClient.getWorkItemFactory().getWorkItem(artifact);
      } else if (atsClient.getConfigItemFactory().isAtsConfigArtifact(artifact)) {
         result = atsClient.getConfigItemFactory().getConfigObject(artifact);
      } else if (artifact.isOfType(AtsArtifactTypes.Action)) {
         result = atsClient.getWorkItemFactory().getAction(artifact);
      }
      return result;
   }

   private Artifact getArtifact(Object object) {
      Artifact useArt = null;
      if (object instanceof Artifact) {
         useArt = (Artifact) object;
      } else if (object instanceof IAtsObject) {
         IAtsObject atsObject = (IAtsObject) object;
         if (atsObject.getStoreObject() instanceof Artifact) {
            useArt = (Artifact) atsObject.getStoreObject();
         }
      }
      return useArt;
   }

   @Override
   public boolean areRelated(ArtifactId artifact1, IRelationTypeSide relationType, ArtifactId artifact2) {
      boolean related = false;
      Artifact useArt1 = getArtifact(artifact1);
      Artifact useArt2 = getArtifact(artifact2);
      if (useArt1 != null && useArt2 != null) {
         related = useArt1.isRelated(relationType, useArt2);
      }
      return related;
   }

   @Override
   public boolean areRelated(IAtsObject atsObject1, IRelationTypeSide relationType, IAtsObject atsObject2) {
      boolean related = false;
      Artifact useArt1 = getArtifact(atsObject1);
      Artifact useArt2 = getArtifact(atsObject2);
      if (useArt1 != null && useArt2 != null) {
         related = useArt1.isRelated(relationType, useArt2);
      }
      return related;
   }

   @Override
   public ArtifactId getRelatedOrNull(ArtifactId artifact, IRelationTypeSide relationType) {
      ArtifactId related = null;
      Artifact art = getArtifact(artifact);
      if (art != null) {
         try {
            related = art.getRelatedArtifact(relationType);
         } catch (ArtifactDoesNotExist ex) {
            // do nothing
         }
      }
      return related;
   }

   @SuppressWarnings("unchecked")
   @Override
   public <T> T getRelatedOrNull(IAtsObject atsObject, IRelationTypeSide relationType, Class<T> clazz) {
      T related = null;
      Artifact art = getArtifact(atsObject);
      if (art != null) {
         try {
            Artifact artifact = art.getRelatedArtifact(relationType);
            if (artifact != null) {
               IAtsObject object = getAtsObject(artifact);
               if (object != null) {
                  related = (T) object;
               }
            }
         } catch (ArtifactDoesNotExist ex) {
            // do nothing
         }
      }
      return related;
   }

   @Override
   public int getRelatedCount(IAtsWorkItem workItem, IRelationTypeSide relationType) {
      Artifact artifact = getArtifact(workItem);
      int count = 0;
      if (artifact != null) {
         count = artifact.getRelatedArtifactsCount(relationType);
      }
      return count;
   }

   @Override
   public ArtifactId getRelatedOrNull(IAtsObject atsObject, IRelationTypeSide relationSide) {
      Artifact art = getArtifact(atsObject);
      if (art != null) {
         return art.getRelatedArtifactOrNull(relationSide);
      }
      return null;
   }

}
