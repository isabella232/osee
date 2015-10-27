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
package org.eclipse.osee.ats.core.client.artifact;

import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.UuidIdentity;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public abstract class AbstractAtsArtifact extends Artifact implements IAtsObject {

   public AbstractAtsArtifact(String guid, IOseeBranch branch, IArtifactType artifactType) throws OseeCoreException {
      super(guid, branch, artifactType);
   }

   public Artifact getParentAtsArtifact() throws OseeCoreException {
      return null;
   }

   @Override
   public ArtifactId getStoreObject() {
      return this;
   }

   @Override
   public void setStoreObject(ArtifactId artifact) {
      // do nothing
   }

   @Override
   public long getUuid() {
      return getArtId();
   }

   @Override
   public boolean matches(UuidIdentity... identities) {
      for (UuidIdentity identity : identities) {
         if (equals(identity)) {
            return true;
         }
      }
      return false;
   }

}
