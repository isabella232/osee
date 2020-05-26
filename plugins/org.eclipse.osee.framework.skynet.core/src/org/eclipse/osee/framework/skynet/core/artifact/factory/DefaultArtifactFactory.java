/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.framework.skynet.core.artifact.factory;

import org.eclipse.osee.framework.core.data.ArtifactTypeToken;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactFactory;

/**
 * This artifact factory is used when no other ArtifactFactory has registered itself as being responsible. This is the
 * case when no specific ArtifactFactory has been defined such as when a new Artifact is created dynamically.
 *
 * @author Donald G. Dunne
 */
public final class DefaultArtifactFactory extends ArtifactFactory {

   @Override
   public Artifact getArtifactInstance(Long id, String guid, BranchId branch, ArtifactTypeToken artifactType, boolean inDataStore) {
      return new Artifact(id, guid, branch, artifactType);
   }

   @Override
   public boolean isUserCreationEnabled(ArtifactTypeToken artifactType) {
      return artifactType.notEqual(CoreArtifactTypes.RootArtifact);
   }
}