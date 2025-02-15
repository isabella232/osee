/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.ds.criteria;

import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.HasBranch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.orcs.core.ds.Criteria;
import org.eclipse.osee.orcs.core.ds.Options;

/**
 * @author Roberto E. Escobar
 */
public class CriteriaBranch extends Criteria implements HasBranch, BranchCriteria {

   private final BranchId branchId;

   public CriteriaBranch(BranchId branchId) {
      this.branchId = branchId;
   }

   @Override
   public void checkValid(Options options) throws OseeCoreException {
      Conditions.checkNotNull(branchId, "branch");
   }

   @Override
   public BranchId getBranch() {
      return branchId;
   }

   @Override
   public String toString() {
      return "CriteriaBranch [branch=" + branchId + "]";
   }
}