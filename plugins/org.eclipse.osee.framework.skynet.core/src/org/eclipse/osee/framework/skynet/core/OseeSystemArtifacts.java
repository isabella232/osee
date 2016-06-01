/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.skynet.core;

import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IArtifactToken;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.enums.CoreArtifactTokens;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * Simple creation and access point for artifact instances required by the framework
 *
 * @author Ryan D. Brooks
 */
public final class OseeSystemArtifacts {

   public static Artifact getGlobalPreferenceArtifact() throws OseeCoreException {
      return getCachedArtifact(CoreArtifactTypes.GlobalPreferences, CoreArtifactTypes.GlobalPreferences.getName(),
         COMMON);
   }

   public static Artifact getDefaultHierarchyRootArtifact(BranchId branch) throws OseeCoreException {
      return ArtifactQuery.getArtifactFromToken(CoreArtifactTokens.DefaultHierarchyRoot, branch);
   }

   public static Artifact createGlobalPreferenceArtifact() throws OseeCoreException {
      return ArtifactTypeManager.addArtifact(CoreArtifactTypes.GlobalPreferences, COMMON,
         CoreArtifactTypes.GlobalPreferences.getName());
   }

   /**
    * @return the artifact specified by type, name, and branch from the cache if available otherwise the datastore is
    * accessed, and finally a new artifact is created if it can not be found
    */
   public static Artifact getOrCreateArtifact(IArtifactType artifactType, String artifactName, BranchId branch) throws OseeCoreException {
      return getOrCreateCachedArtifact(artifactType, artifactName, branch, null, true);
   }

   public static Artifact getOrCreateArtifact(IArtifactToken artifactToken, BranchId branch) throws OseeCoreException {
      return getOrCreateCachedArtifact(artifactToken.getArtifactType(), artifactToken.getName(), branch,
         artifactToken.getGuid(), artifactToken.getUuid(), true);
   }

   public static Artifact getCachedArtifact(IArtifactType artifactType, String artifactName, BranchId branch) throws OseeCoreException {
      return getOrCreateCachedArtifact(artifactType, artifactName, branch, null, false);
   }

   private static Artifact getOrCreateCachedArtifact(IArtifactType artifactType, String artifactName, BranchId branch, String guid, boolean create) throws OseeCoreException {
      return getOrCreateCachedArtifact(artifactType, artifactName, branch, guid, null, create);
   }

   private static Artifact getOrCreateCachedArtifact(IArtifactType artifactType, String artifactName, BranchId branch, String guid, Long uuid, boolean create) throws OseeCoreException {
      Artifact artifact = ArtifactQuery.checkArtifactFromTypeAndName(artifactType, artifactName, branch);
      if (artifact == null && create) {
         if (Strings.isValid(guid)) {
            if (uuid != null && uuid > 0) {
               artifact = ArtifactTypeManager.addArtifact(artifactType, branch, artifactName, guid, uuid);
            } else {
               artifact = ArtifactTypeManager.addArtifact(artifactType, branch, artifactName, guid);
            }
            artifact.setName(artifactName);
         } else {
            artifact = ArtifactTypeManager.addArtifact(artifactType, branch, artifactName);
         }
      }
      if (artifact == null) {
         throw new ArtifactDoesNotExist("Artifact of type [%s] with name [%s] does not exist on branch [%s]",
            artifactType, artifactName, branch);
      }
      return artifact;
   }
}