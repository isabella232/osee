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

import org.eclipse.osee.orcs.core.ds.Criteria;

/**
 * @author Roberto E. Escobar
 */
public class CriteriaTxGetHead extends Criteria {

   private final long branchId;

   public CriteriaTxGetHead(long branchId) {
      super();
      this.branchId = branchId;
   }

   public long getBranchid() {
      return branchId;
   }

   @Override
   public String toString() {
      return "CriteriaTxGetHead [branchId=" + branchId + "]";
   }

}
