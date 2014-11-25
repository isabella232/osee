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
package org.eclipse.osee.orcs.db.internal.search.engines;

import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.db.internal.SqlProvider;
import org.eclipse.osee.orcs.db.internal.sql.AbstractSqlWriter;
import org.eclipse.osee.orcs.db.internal.sql.QueryType;
import org.eclipse.osee.orcs.db.internal.sql.SqlContext;
import org.eclipse.osee.orcs.db.internal.sql.SqlHandler;
import org.eclipse.osee.orcs.db.internal.sql.TableEnum;
import org.eclipse.osee.orcs.db.internal.sql.join.SqlJoinFactory;

/**
 * @author Roberto E. Escobar
 */
public class QuerySqlWriter extends AbstractSqlWriter {

   private final TableEnum table;
   private final String idColumn;

   public QuerySqlWriter(Log logger, SqlJoinFactory joinFactory, SqlProvider sqlProvider, SqlContext context, QueryType queryType, TableEnum table, String idColumn) {
      super(logger, joinFactory, sqlProvider, context, queryType);
      this.table = table;
      this.idColumn = idColumn;
   }

   @Override
   public void writeSelect(Iterable<SqlHandler<?>> handlers) throws OseeCoreException {
      String tableAlias = getLastAlias(table);
      if (isCountQueryType()) {
         write("SELECT%s count(%s.%s)", getSqlHint(), tableAlias, idColumn);
      } else {
         write("SELECT%s %s.*", getSqlHint(), tableAlias);
      }
   }

   @Override
   protected void write(Iterable<SqlHandler<?>> handlers) throws OseeCoreException {
      computeTables(handlers);
      computeWithClause(handlers);

      writeWithClause();
      writeSelect(handlers);
      write("\n FROM \n");
      writeTables();
      write("\n WHERE \n");
      writePredicates(handlers);

      if (toString().endsWith("\n WHERE \n")) {
         removeDanglingSeparator("\n WHERE \n");
      }
      writeGroupAndOrder();
   }

   @Override
   public void writeGroupAndOrder() throws OseeCoreException {
      if (!isCountQueryType()) {
         String tableAlias = getLastAlias(table);
         write("\n ORDER BY %s.%s", tableAlias, idColumn);
      }
   }

   @Override
   public String getTxBranchFilter(String txsAlias) {
      return Strings.emptyString();
   }

   @Override
   public String getTxBranchFilter(String txsAlias, boolean allowDeleted) throws OseeCoreException {
      return Strings.emptyString();
   }

   @Override
   public String getWithClauseTxBranchFilter(String txsAlias, boolean deletedPredicate) throws OseeCoreException {
      return Strings.emptyString();
   }

}
