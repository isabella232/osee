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

import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactFactory;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactType;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;

/**
 * @author Ryan D. Brooks
 */
@Deprecated
public class SkynetArtifactFactory extends ArtifactFactory {
   private static SkynetArtifactFactory factory = null;

   private SkynetArtifactFactory(int factoryId) {
      super(factoryId);
   }

   public static SkynetArtifactFactory getInstance(int factoryId) {
      if (factory == null) {
         factory = new SkynetArtifactFactory(factoryId);
      }
      return factory;
   }

   public static SkynetArtifactFactory getInstance() {
      return factory;
   }

   public @Override
   Artifact getArtifactInstance(String guid, String humandReadableId, String factoryKey, Branch branch, ArtifactType artifactType) throws OseeCoreException {
      if (factoryKey.equals(User.ARTIFACT_NAME)) {
         return new User(this, guid, humandReadableId, branch, artifactType);
      }
      return new Artifact(this, guid, humandReadableId, branch, artifactType);
   }
}