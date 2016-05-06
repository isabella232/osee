/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.internal.branch.provider;

import java.util.Collection;
import java.util.Collections;
import org.eclipse.osee.framework.core.model.Branch;

/**
 * @author Ryan D. Brooks
 */
public class SingleBranchProvider implements BranchProvider {
   private final Branch branch;

   public SingleBranchProvider(Branch branch) {
      this.branch = branch;
   }

   @Override
   public Collection<Branch> getBranches() {
      return Collections.singleton(branch);
   }
}