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
package org.eclipse.osee.framework.branch.management.creation;

import org.eclipse.osee.framework.branch.management.Branch;
import org.eclipse.osee.framework.branch.management.IBranchCreation;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.LogProgressMonitor;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.database.IOseeDatabaseServiceProvider;

/**
 * @author Andrew M. Finkbeiner
 */
public class BranchCreation implements IBranchCreation {
   private final IOseeDatabaseServiceProvider provider;

   public BranchCreation(IOseeDatabaseServiceProvider provider) {
      this.provider = provider;
   }

   public int createBranch(Branch branch, int authorId, String creationComment, int populateBaseTxFromAddressingQueryId, int destinationBranchId) throws Exception {
      IOperation operation =
            new CreateBranchOperation(provider, branch, authorId, creationComment, populateBaseTxFromAddressingQueryId,
                  destinationBranchId);
      Operations.executeWorkAndCheckStatus(operation, new LogProgressMonitor(), -1);
      return branch.getId();
   }
}
