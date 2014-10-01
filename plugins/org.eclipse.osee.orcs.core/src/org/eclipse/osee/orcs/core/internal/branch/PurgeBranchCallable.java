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
package org.eclipse.osee.orcs.core.internal.branch;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.ds.BranchDataStore;
import org.eclipse.osee.orcs.data.BranchReadable;
import org.eclipse.osee.orcs.search.BranchQuery;
import org.eclipse.osee.orcs.search.QueryFactory;

/**
 * @author Roberto E. Escobar
 */
public class PurgeBranchCallable extends AbstractBranchCallable<List<IOseeBranch>> {

   private final IOseeBranch branchToken;
   private final boolean isRecursive;
   private final QueryFactory queryFactory;

   public PurgeBranchCallable(Log logger, OrcsSession session, BranchDataStore branchStore, IOseeBranch branchToken, boolean isRecursive, QueryFactory queryFactory) {
      super(logger, session, branchStore);
      this.branchToken = branchToken;
      this.isRecursive = isRecursive;
      this.queryFactory = queryFactory;
   }

   @Override
   protected List<IOseeBranch> innerCall() throws Exception {
      Conditions.checkNotNull(branchToken, "branchToPurge");

      BranchQuery branchQuery = queryFactory.branchQuery();
      branchQuery.andIds(branchToken);
      if (isRecursive) {
         branchQuery.andIsChildOf(branchToken);
      }

      ResultSet<BranchReadable> branches = branchQuery.getResults();

      List<IOseeBranch> purged = new LinkedList<IOseeBranch>();
      List<BranchReadable> orderedBranches = BranchUtil.orderByParentReadable(queryFactory, branches);
      for (BranchReadable aBranch : orderedBranches) {
         checkForCancelled();
         checkForChildBranches(aBranch);
         Callable<Void> callable = getBranchStore().purgeBranch(getSession(), aBranch);
         callAndCheckForCancel(callable);
         purged.add(aBranch);
      }
      return purged;
   }

   private void checkForChildBranches(BranchReadable aBranch) {
      BranchQuery branchQuery = queryFactory.branchQuery();
      branchQuery.andIsChildOf(aBranch);
      if (branchQuery.getCount() > 0) {
         throw new OseeArgumentException("Unable to purge a branch containing children: branchUuid[%s] branchType[%s]",
            aBranch.getUuid(), aBranch.getBranchType());
      }
   }
}
