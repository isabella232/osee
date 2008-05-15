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

import org.eclipse.osee.framework.skynet.core.artifact.ArtifactFactory;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.SearchArtifact;
import org.eclipse.osee.framework.skynet.core.attribute.ArtifactSubtypeDescriptor;

public class SearchArtifactFactory extends ArtifactFactory {
   private static SearchArtifactFactory factory = null;

   private SearchArtifactFactory(int factoryId) {
      super(factoryId);
   }

   public static SearchArtifactFactory getInstance(int factoryId) {
      if (factory == null) {
         factory = new SearchArtifactFactory(factoryId);
      }
      return factory;
   }

   public static SearchArtifactFactory getInstance() {
      return factory;
   }

   public @Override
   SearchArtifact getArtifactInstance(String guid, String humandReadableId, String factoryKey, Branch branch, ArtifactSubtypeDescriptor artifactType) {
      return new SearchArtifact(this, guid, humandReadableId, branch, artifactType);
   }
}