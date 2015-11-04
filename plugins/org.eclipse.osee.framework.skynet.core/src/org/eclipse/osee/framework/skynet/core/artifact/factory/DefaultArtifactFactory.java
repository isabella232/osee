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
package org.eclipse.osee.framework.skynet.core.artifact.factory;

import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactFactory;

/**
 * This artifact factory is used when no other ArtifactFactory has registered itself as being responsible. This is the
 * case when no specific ArtifactFactory has been defined such as when a new Artifact is created dynamically.
 *
 * @author Donald G. Dunne
 */
public final class DefaultArtifactFactory extends ArtifactFactory {

   public DefaultArtifactFactory() {
      super();
   }

   @Override
   public Artifact getArtifactInstance(String guid, IOseeBranch branch, IArtifactType artifactType, boolean inDataStore) throws OseeCoreException {
      return new Artifact(guid, branch, artifactType);
   }

   @Override
   public boolean isUserCreationEnabled(IArtifactType artifactType) {
      if (artifactType.getGuid().equals(CoreArtifactTypes.RootArtifact.getGuid())) {
         return false;
      }
      return true;
   }

}
