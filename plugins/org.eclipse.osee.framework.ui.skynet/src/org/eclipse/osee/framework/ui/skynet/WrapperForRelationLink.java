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
package org.eclipse.osee.framework.ui.skynet;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Andrew M. Finkbeiner
 */
class WrapperForRelationLink implements IAdaptable {

   private final IRelationType relationType;
   private final Artifact artifactA;
   private final Artifact artifactB;
   private final Artifact other;

   public WrapperForRelationLink(IRelationType relationType, Artifact other, Artifact artifactA, Artifact artifactB) {
      this.relationType = relationType;
      this.artifactA = artifactA;
      this.artifactB = artifactB;
      this.other = other;
   }

   IRelationType getRelationType() {
      return relationType;
   }

   Artifact getArtifactA() {
      return artifactA;
   }

   Artifact getArtifactB() {
      return artifactB;
   }

   Artifact getOther() {
      return other;
   }

   RelationSide getRelationSide() {
      if (other.equals(artifactB)) {
         return RelationSide.SIDE_B;
      } else {
         return RelationSide.SIDE_A;
      }
   }

   @SuppressWarnings("unchecked")
   @Override
   public <T> T getAdapter(Class<T> type) {
      if (type != null && type.isAssignableFrom(Artifact.class)) {
         return (T) other;
      }
      return null;
   }
}
