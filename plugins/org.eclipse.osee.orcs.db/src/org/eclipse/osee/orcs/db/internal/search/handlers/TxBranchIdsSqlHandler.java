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

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaTxBranchIds;
import org.eclipse.osee.orcs.db.internal.sql.AbstractSqlWriter;
import org.eclipse.osee.orcs.db.internal.sql.SqlHandler;
import org.eclipse.osee.orcs.db.internal.sql.TableEnum;
import org.eclipse.osee.orcs.db.internal.sql.join.AbstractJoinQuery;

/**
 * @author Roberto E. Escobar
 */
public class TxBranchIdsSqlHandler extends SqlHandler<CriteriaTxBranchIds> {

   private CriteriaTxBranchIds criteria;

   private String txAlias;
   private String jIdAlias;

   @Override
   public void setData(CriteriaTxBranchIds criteria) {
      this.criteria = criteria;
   }

   @Override
   public void addTables(AbstractSqlWriter writer) {
      if (criteria.getIds().size() > 1) {
         jIdAlias = writer.addTable(TableEnum.ID_JOIN_TABLE);
      }
      List<String> branchAliases = writer.getAliases(TableEnum.TX_DETAILS_TABLE);
      if (branchAliases.isEmpty()) {
         txAlias = writer.addTable(TableEnum.TX_DETAILS_TABLE);
      } else {
         txAlias = branchAliases.iterator().next();
      }
   }

   @Override
   public boolean addPredicates(AbstractSqlWriter writer) throws OseeCoreException {
      Collection<Long> ids = criteria.getIds();
      if (ids.size() > 1) {
         AbstractJoinQuery joinQuery = writer.writeIdJoin(ids);
         writer.write(txAlias);
         writer.write(".branch_id = ");
         writer.write(jIdAlias);
         writer.write(".id AND ");
         writer.write(jIdAlias);
         writer.write(".query_id = ?");
         writer.addParameter(joinQuery.getQueryId());
      } else {
         writer.write(txAlias);
         writer.write(".branch_id = ?");
         writer.addParameter(ids.iterator().next());
      }
      return true;
   }

   @Override
   public int getPriority() {
      return SqlHandlerPriority.TX_BRANCH_ID.ordinal();
   }
}
