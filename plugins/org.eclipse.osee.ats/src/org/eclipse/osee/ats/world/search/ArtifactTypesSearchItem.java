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
package org.eclipse.osee.ats.world.search;

import static org.eclipse.osee.framework.core.enums.DeletionFlag.EXCLUDE_DELETED;
import java.util.Collection;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;

/**
 * @author Donald G. Dunne
 */
public class ArtifactTypesSearchItem extends WorldUISearchItem {

   private final Collection<IArtifactType> artifactTypes;

   public ArtifactTypesSearchItem(String name, Collection<IArtifactType> artifactTypeNames) {
      super(name, FrameworkImage.FLASHLIGHT);
      this.artifactTypes = artifactTypeNames;
   }

   private ArtifactTypesSearchItem(ArtifactTypesSearchItem artifactTypesSearchItem) {
      super(artifactTypesSearchItem, FrameworkImage.FLASHLIGHT);
      this.artifactTypes = artifactTypesSearchItem.artifactTypes;
   }

   @Override
   public Collection<Artifact> performSearch(SearchType searchType) throws OseeCoreException {
      Conditions.checkNotNullOrEmpty(artifactTypes, getName());
      return ArtifactQuery.getArtifactListFromTypes(artifactTypes, AtsUtil.getAtsBranch(), EXCLUDE_DELETED);
   }

   @Override
   public WorldUISearchItem copy() {
      return new ArtifactTypesSearchItem(this);
   }

}
