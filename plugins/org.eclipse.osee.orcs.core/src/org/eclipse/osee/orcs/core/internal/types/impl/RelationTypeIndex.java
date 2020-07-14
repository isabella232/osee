/*********************************************************************
 * Copyright (c) 2013 Boeing
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

package org.eclipse.osee.orcs.core.internal.types.impl;

import org.eclipse.osee.framework.core.data.ArtifactTypeId;
import org.eclipse.osee.framework.core.data.ArtifactTypeToken;
import org.eclipse.osee.framework.core.data.RelationTypeToken;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XArtifactType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XRelationType;
import org.eclipse.osee.framework.core.enums.RelationSide;

/**
 * @author Roberto E. Escobar
 */
public class RelationTypeIndex extends TokenTypeIndex<RelationTypeToken, XRelationType> {

   private final ArtifactTypeIndex artifactTypeIndex;

   public RelationTypeIndex(ArtifactTypeIndex artifactTypeIndex) {
      super(RelationTypeToken.SENTINEL);
      this.artifactTypeIndex = artifactTypeIndex;
   }

   public ArtifactTypeId getArtifactType(RelationTypeToken relation, RelationSide relationSide) {
      XRelationType type = getDslTypeByToken(relation);
      XArtifactType artifactType =
         relationSide == RelationSide.SIDE_A ? type.getSideAArtifactType() : type.getSideBArtifactType();
      return artifactTypeIndex.getTokenByDslType(artifactType);
   }

   public boolean isArtifactTypeAllowed(RelationTypeToken relation, RelationSide relationSide, ArtifactTypeToken artifactType) {
      ArtifactTypeId allowedType = getArtifactType(relation, relationSide);
      return artifactType.inheritsFrom(allowedType);
   }
}