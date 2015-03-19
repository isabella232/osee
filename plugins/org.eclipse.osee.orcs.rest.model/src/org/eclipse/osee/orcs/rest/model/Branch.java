/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.rest.model;

import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.BranchType;

/**
 * @author Roberto E. Escobar
 */
@XmlRootElement
public class Branch {

   private static final int DEFAULT_INT = -1;

   private long branchUuid;
   private String name;
   private long parentBranchUuid;

   private int associatedArtifactId = DEFAULT_INT;
   private int baseTransaction = DEFAULT_INT;
   private int sourceTransaction = DEFAULT_INT;

   private BranchArchivedState archiveState = BranchArchivedState.UNARCHIVED;
   private BranchState branchState = BranchState.CREATED;
   private BranchType branchType = BranchType.WORKING;
   private boolean inheritAccessControl = false;

   public Branch() {
      super();
   }

   public long getBranchUuid() {
      return branchUuid;
   }

   public void setBranchUuid(long branchUuid) {
      this.branchUuid = branchUuid;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public int getAssociatedArtifactId() {
      return associatedArtifactId;
   }

   public void setAssociatedArtifactId(int artId) {
      associatedArtifactId = artId;
   }

   public int getBaseTransactionId() {
      return baseTransaction;
   }

   public void setBaseTransactionId(int baseTx) {
      baseTransaction = baseTx;
   }

   public int getSourceTransactionId() {
      return sourceTransaction;
   }

   public void setSourceTransactionId(int sourceTx) {
      sourceTransaction = sourceTx;
   }

   public long getParentBranchUuid() {
      return parentBranchUuid;
   }

   public void setParentBranchUuid(long parentBranchUuid) {
      this.parentBranchUuid = parentBranchUuid;
   }

   public boolean hasParentBranchUuid() {
      return getParentBranchUuid() > 0;
   }

   public BranchArchivedState getArchiveState() {
      return archiveState;
   }

   public void setArchiveState(BranchArchivedState state) {
      this.archiveState = state;
   }

   public BranchState getBranchState() {
      return branchState;
   }

   public void setBranchState(BranchState state) {
      this.branchState = state;
   }

   public BranchType getBranchType() {
      return branchType;
   }

   public void setBranchType(BranchType type) {
      branchType = type;
   }

   public boolean isInheritAccessControl() {
      return inheritAccessControl;
   }

   public void setInheritAccessControl(boolean inheritAccessControl) {
      this.inheritAccessControl = inheritAccessControl;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (int) (branchUuid ^ (branchUuid >>> 32));
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      Branch other = (Branch) obj;
      if (branchUuid != other.branchUuid) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return "Branch [branchUuid=" + getBranchUuid() + ", name=" + name + ", parentBranchUuid=" + getParentBranchUuid() + ", associatedArtifactId=" + associatedArtifactId + ", baseTransaction=" + baseTransaction + ", sourceTransaction=" + sourceTransaction + ", archiveState=" + archiveState + ", branchState=" + branchState + ", branchType=" + branchType + ", inheritAccessControl=" + inheritAccessControl + "]";
   }

}
