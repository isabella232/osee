/*********************************************************************
 * Copyright (c) 2013 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.orcs.db.internal.search.handlers;

import org.eclipse.osee.framework.core.enums.SqlTable;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaTxGetHead;
import org.eclipse.osee.orcs.db.internal.sql.AbstractSqlWriter;
import org.eclipse.osee.orcs.db.internal.sql.SqlHandler;

/**
 * @author Roberto E. Escobar
 */
public class TxGetHeadSqlHandler extends SqlHandler<CriteriaTxGetHead> {
   private CriteriaTxGetHead criteria;
   private String txdAlias;

   @Override
   public void setData(CriteriaTxGetHead criteria) {
      this.criteria = criteria;
   }

   @Override
   public void addTables(AbstractSqlWriter writer) {
      txdAlias = writer.getMainTableAlias(SqlTable.TX_DETAILS_TABLE);
   }

   @Override
   public void addPredicates(AbstractSqlWriter writer) {
      writer.write(txdAlias);
      writer.write(".transaction_id = ");
      writer.write("(SELECT max(transaction_id) FROM ");
      writer.write(SqlTable.TX_DETAILS_TABLE.getName());
      writer.write(" WHERE ");
      writer.writeEqualsParameter("branch_id", criteria.getBranch());
      writer.write(")");
   }

   @Override
   public int getPriority() {
      return SqlHandlerPriority.TX_ID.ordinal();
   }
}