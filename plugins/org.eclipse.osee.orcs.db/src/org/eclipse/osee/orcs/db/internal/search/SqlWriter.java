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
package org.eclipse.osee.orcs.db.internal.search;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.AbstractJoinQuery;
import org.eclipse.osee.framework.database.core.CharJoinQuery;
import org.eclipse.osee.framework.database.core.IdJoinQuery;
import org.eclipse.osee.framework.database.core.JoinUtility;
import org.eclipse.osee.orcs.core.ds.QueryOptions;
import org.eclipse.osee.orcs.core.ds.QueryPostProcessor;
import org.eclipse.osee.orcs.db.internal.search.SqlBuilder.QueryType;
import org.eclipse.osee.orcs.db.internal.search.SqlConstants.TableEnum;

/**
 * @author Roberto E. Escobar
 */
public class SqlWriter {

   private final SqlAliasManager aliasManager = new SqlAliasManager();
   private final IOseeDatabaseService dbService;
   private final int branchId;
   private final SqlContext context;
   private final Appendable output;

   private boolean isFirstTable = true;

   public SqlWriter(IOseeDatabaseService dbService, int branchId, SqlContext context, Appendable output) {
      this.dbService = dbService;
      this.branchId = branchId;
      this.context = context;
      this.output = output;
   }

   public void writeCountSelect() throws OseeCoreException {
      if (context.getOptions().isHistorical()) {
         write("SELECT count(xTable.art_id) FROM (\n ");
         writeSelect();
      } else {
         write("SELECT%s count(%s.art_id)");
      }
   }

   public void writeSelect() throws OseeCoreException {
      if (context.getOptions().isHistorical()) {
         write("SELECT%s max(%s.transaction_id), %s.art_id, %s.branch_id");
      } else {
         write("SELECT%s %s.art_id, %s.branch_id");
      }
   }

   public void writeTables(List<SqlHandler> handlers) throws OseeCoreException {
      for (SqlHandler handler : handlers) {
         handler.addTables(this);
      }
   }

   public void writePredicates(List<SqlHandler> handlers) throws OseeCoreException {
      int size = handlers.size();
      for (int index = 0; index < size; index++) {
         SqlHandler handler = handlers.get(index);
         handler.addPredicates(this);
         if (index + 1 < size) {
            write("\n AND \n");
         }
      }
      List<String> aliases = aliasManager.getAliases(TableEnum.TXS_TABLE);
      if (aliases.size() > 1) {
         writeTxsJoin(aliases);
      }
   }

   private void writeTxsJoin(List<String> aliases) {
      // Do Nothing
      //      if (false) {
      //         write("\n AND \n");
      //         int size = aliases.size();
      //         if (size > 1) {
      //            for (int index = 1; index < size; index++) {
      //               String alias1 = aliases.get(index - 1);
      //               String alias2 = aliases.get(index);
      //               write(alias1);
      //               write(".gamma_id = ");
      //               write(alias2);
      //               write(".gamma_id AND ");
      //               write(alias1);
      //               write(".transaction_id = ");
      //               write(alias2);
      //               write(".transaction_id AND ");
      //               write(alias1);
      //               write(".branch_id = ");
      //               write(alias2);
      //               write(".branch_id");
      //               if (index + 1 < size) {
      //                  write("\n AND \n");
      //               }
      //            }
      //         }
      //      }
   }

   public List<String> getAliases(TableEnum table) {
      return aliasManager.getAliases(table);
   }

   public String writeTable(TableEnum table) throws OseeCoreException {
      String alias = null;
      if (isFirstTable) {
         isFirstTable = false;
      } else {
         write(", ");
      }
      write(table.getName());
      write(" ");

      alias = aliasManager.getNextAlias(table);
      write(alias);
      return alias;
   }

   public void writeGroupAndOrder(QueryType type) throws OseeCoreException {
      if (context.getOptions().isHistorical()) {
         String artAlias = aliasManager.getAliases(TableEnum.ARTIFACT_TABLE).get(0);
         String txAlias = aliasManager.getAliases(TableEnum.TXS_TABLE).get(0);

         write(String.format("\n GROUP BY %s.art_id, %s.branch_id", artAlias, txAlias));
      }
      if (type != QueryType.COUNT_ARTIFACTS) {
         String artAlias = aliasManager.getAliases(TableEnum.ARTIFACT_TABLE).get(0);
         String txAlias = aliasManager.getAliases(TableEnum.TXS_TABLE).get(0);

         write(String.format("\n ORDER BY %s.art_id, %s.branch_id", artAlias, txAlias));
      } else {
         if (context.getOptions().isHistorical()) {
            write("\n) xTable");
         }
      }
   }

   public void writeTxBranchFilter(String txsAlias) throws OseeCoreException {
      writeTxFilter(txsAlias);
      if (branchId > 0) {
         write(" AND ");
         write(txsAlias);
         write(".branch_id = ?");
         addParameter(branchId);
      }
   }

   public void write(String data) throws OseeCoreException {
      try {
         output.append(data);
      } catch (IOException ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
   }

   public void writeTxFilter(String txsAlias) throws OseeCoreException {
      if (context.getOptions().isHistorical()) {
         write(txsAlias);
         write(".transaction_id <= ?");
         addParameter(context.getOptions().getFromTransaction());
         if (!context.getOptions().areDeletedIncluded()) {
            write("\n AND \n");
            write(txsAlias);
            write(".tx_current");
            write(" IN (");
            write(String.valueOf(TxChange.CURRENT.getValue()));
            write(", ");
            write(String.valueOf(TxChange.NOT_CURRENT.getValue()));
            write(")");
         }
      } else {
         write(txsAlias);
         write(".tx_current");
         if (context.getOptions().areDeletedIncluded()) {
            write(" IN (");
            write(String.valueOf(TxChange.CURRENT.getValue()));
            write(", ");
            write(String.valueOf(TxChange.DELETED.getValue()));
            write(", ");
            write(String.valueOf(TxChange.ARTIFACT_DELETED.getValue()));
            write(")");
         } else {
            write(" = ");
            write(String.valueOf(TxChange.CURRENT.getValue()));
         }
      }
   }

   public void addParameter(Object data) {
      context.getParameters().add(data);
   }

   private void addJoin(AbstractJoinQuery join) {
      context.getJoins().add(join);
   }

   public CharJoinQuery writeCharJoin(Collection<String> ids) {
      CharJoinQuery joinQuery = JoinUtility.createCharJoinQuery(dbService, context.getSessionId());
      for (String id : ids) {
         joinQuery.add(id);
      }
      addJoin(joinQuery);
      return joinQuery;
   }

   public IdJoinQuery writeIdJoin(Collection<Integer> ids) {
      IdJoinQuery joinQuery = JoinUtility.createIdJoinQuery(dbService);
      for (Integer id : ids) {
         joinQuery.add(id);
      }
      addJoin(joinQuery);
      return joinQuery;
   }

   public void addPostProcessor(QueryPostProcessor processor) {
      context.getPostProcessors().add(processor);
   }

   public QueryOptions getOptions() {
      return context.getOptions();
   }
}
