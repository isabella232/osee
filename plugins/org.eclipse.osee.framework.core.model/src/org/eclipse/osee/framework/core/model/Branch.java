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

package org.eclipse.osee.framework.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.jdk.core.type.NamedId;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Roberto E. Escobar
 */
public class Branch extends NamedId implements BranchReadable, IAdaptable {

   private final Set<Branch> childBranches = new HashSet<>();
   private BranchType branchType;
   private BranchState branchState;
   private boolean isArchived;
   private boolean inheritsAccessControl;
   private TransactionRecord parentTx;
   private TransactionRecord baselineTx;
   private Branch parent;
   private Integer associatedArtifactId;

   public Branch(Long uuid, String name, BranchType branchType, BranchState branchState, boolean isArchived, boolean inheritsAccessControl) {
      super(uuid, name);
      this.branchType = branchType;
      this.branchState = branchState;
      this.isArchived = isArchived;
      this.inheritsAccessControl = inheritsAccessControl;
   }

   @Override
   public BranchId getParentBranch() throws OseeCoreException {
      return parent;
   }

   @Override
   public BranchType getBranchType() {
      return branchType;
   }

   @Override
   public BranchState getBranchState() {
      return branchState;
   }

   @Override
   public boolean isArchived() {
      return isArchived;
   }

   @Override
   public Integer getAssociatedArtifactId() throws OseeCoreException {
      return associatedArtifactId;
   }

   public void setAssociatedArtifactId(Integer artifactId) throws OseeCoreException {
      this.associatedArtifactId = artifactId;
   }

   @Override
   public TransactionRecord getBaseTransaction() throws OseeCoreException {
      return baselineTx;
   }

   @Override
   public TransactionRecord getSourceTransaction() throws OseeCoreException {
      return parentTx;
   }

   public void setArchived(boolean isArchived) {
      this.isArchived = isArchived;
   }

   public void setBranchState(BranchState branchState) {
      this.branchState = branchState;
   }

   public void setBranchType(BranchType branchType) {
      this.branchType = branchType;
   }

   public void setParentBranch(Branch parentBranch) throws OseeCoreException {
      if (parent != null) {
         parent.childBranches.remove(this);
      }
      parent = parentBranch;
      parentBranch.childBranches.add(this);
   }

   public void setBaseTransaction(TransactionRecord baselineTx) throws OseeCoreException {
      this.baselineTx = baselineTx;
   }

   public void setSourceTransaction(TransactionRecord parentTx) throws OseeCoreException {
      this.parentTx = parentTx;
   }

   public boolean isInheritAccessControl() {
      return inheritsAccessControl;
   }

   public void setInheritAccessControl(boolean inheritsAccessControl) {
      this.inheritsAccessControl = inheritsAccessControl;
   }

   public Set<Branch> getChildren() throws OseeCoreException {
      return childBranches;
   }

   /**
    * @return all child branches. It is equivalent to calling getChildBranches with new BranchFilter() (.i.e no child
    * branches are excluded)
    * @throws OseeCoreException
    */
   @Override
   public Collection<BranchReadable> getAllChildBranches(boolean recurse) throws OseeCoreException {
      Set<BranchReadable> children = new HashSet<>();
      getChildBranches(children, recurse, b -> true);
      return children;
   }

   @Override
   public void getChildBranches(Collection<BranchReadable> children, boolean recurse, Predicate<BranchReadable> filter) {
      for (BranchReadable branch : getChildren()) {
         if (filter.test(branch)) {
            children.add(branch);
            if (recurse) {
               branch.getChildBranches(children, recurse, filter);
            }
         }
      }
   }

   @Override
   public Collection<BranchId> getAncestors() throws OseeCoreException {
      List<BranchId> ancestors = new ArrayList<>();
      Branch branchCursor = this;
      ancestors.add(branchCursor);
      while ((branchCursor = branchCursor.parent) != null) {
         ancestors.add(branchCursor);
      }
      return ancestors;
   }

   @SuppressWarnings("rawtypes")
   @Override
   public Object getAdapter(Class adapter) {
      if (adapter == null) {
         throw new IllegalArgumentException("adapter can not be null");
      }

      if (adapter.isInstance(this)) {
         return this;
      }
      return null;
   }

   @Override
   public boolean isAncestorOf(BranchId branch) throws OseeCoreException {
      return getAllChildBranches(true).contains(branch);
   }

   public boolean hasAncestor(BranchId ancestor) {
      Branch branchCursor = this;
      while ((branchCursor = branchCursor.parent) != null) {
         if (branchCursor.equals(ancestor)) {
            return true;
         }
      }
      return false;
   }

   /*
    * Provide easy way to display/report [guid][name]
    */
   public final String toStringWithId() {
      return String.format("[%s][%s]", getGuid(), getName());
   }

}