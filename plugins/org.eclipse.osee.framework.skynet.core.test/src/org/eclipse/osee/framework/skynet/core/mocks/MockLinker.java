/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *public static final CoreAttributeTypes   Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.skynet.core.mocks;

import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.relation.RelationLink.ArtifactLinker;

/**
 * @author Roberto E. Escobar
 */
public class MockLinker implements ArtifactLinker {

   private final String name;

   public MockLinker(String name) {
      super();
      this.name = name;
   }

   public MockLinker() {
      this(null);
   }

   @Override
   public void updateCachedArtifact(int artId, BranchId branch) {
      //
   }

   @Override
   public Artifact getArtifact(int ArtId, BranchId branch) {
      return null;
   }

   @Override
   public String getLazyArtifactName(int aArtifactId, BranchId branch) {
      return name;
   }

   @Override
   public void deleteFromRelationOrder(Artifact aArtifact, Artifact bArtifact, IRelationType relationType) {
      //
   }
}