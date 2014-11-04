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
package org.eclipse.osee.orcs.db.internal.search.handlers;

import java.util.List;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaAllBranches;
import org.eclipse.osee.orcs.db.internal.sql.AbstractSqlWriter;
import org.eclipse.osee.orcs.db.internal.sql.SqlHandler;
import org.eclipse.osee.orcs.db.internal.sql.TableEnum;

/**
 * @author Roberto E. Escobar
 */
public class AllBranchesSqlHandler extends SqlHandler<CriteriaAllBranches> {

   @Override
   public void setData(CriteriaAllBranches criteria) {
      // Criteria not used
   }

   @Override
   public void addTables(AbstractSqlWriter writer) {
      List<String> branchAliases = writer.getAliases(TableEnum.BRANCH_TABLE);
      if (branchAliases.isEmpty()) {
         writer.addTable(TableEnum.BRANCH_TABLE);
      }
   }

   @Override
   public boolean addPredicates(AbstractSqlWriter writer) {
      return false;
   }

   @Override
   public int getPriority() {
      return SqlHandlerPriority.ALL_BRANCHES.ordinal();
   }
}
