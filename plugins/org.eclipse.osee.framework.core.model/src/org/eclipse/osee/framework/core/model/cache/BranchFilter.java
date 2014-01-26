/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.model.cache;

import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.model.IBasicArtifact;
import org.eclipse.osee.framework.core.model.BranchReadable;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Ryan D. Brooks
 */
public class BranchFilter {
   private final BranchArchivedState archivedState;
   private final BranchType[] branchTypes;
   private IBasicArtifact<?> associatedArtifact;

   private BranchState[] branchStates;
   private BranchState[] negatedBranchStates;
   private BranchType[] negatedBranchTypes;

   /**
    * @param archivedState filter will only match branches that are of one of the specified states
    * @param branchTypes filter will only match branches that are of one of the specified types
    */
   public BranchFilter(BranchArchivedState archivedState, BranchType... branchTypes) {
      this.archivedState = archivedState;
      this.branchTypes = branchTypes;
   }

   /**
    * @param branchTypes filter will only match branches that are of one of the specified types
    */
   public BranchFilter(BranchType... branchTypes) {
      this(BranchArchivedState.ALL, branchTypes);
   }

   public boolean matches(BranchReadable branch) throws OseeCoreException {
      if (associatedArtifact != null && !branch.getAssociatedArtifactId().equals(associatedArtifact.getArtId())) {
         return false;
      }
      if (!branch.getArchiveState().matches(archivedState)) {
         return false;
      }
      if (branchTypes.length > 0 && !branch.getBranchType().isOfType(branchTypes)) {
         return false;
      }
      if (branchStates != null && !branch.getBranchState().matches(branchStates)) {
         return false;
      }
      if (negatedBranchStates != null && branch.getBranchState().matches(negatedBranchStates)) {
         return false;
      }
      if (negatedBranchTypes != null && branch.getBranchType().isOfType(negatedBranchTypes)) {
         return false;
      }
      return true;
   }

   public void setAssociatedArtifact(IBasicArtifact<?> associatedArtifact) {
      this.associatedArtifact = associatedArtifact;
   }

   public void setBranchStates(BranchState... branchStates) {
      this.branchStates = branchStates;
   }

   public void setNegatedBranchStates(BranchState... negatedBranchStates) {
      this.negatedBranchStates = negatedBranchStates;
   }

   public void setNegatedBranchTypes(BranchType... negatedBranchTypes) {
      this.negatedBranchTypes = negatedBranchTypes;
   }
}