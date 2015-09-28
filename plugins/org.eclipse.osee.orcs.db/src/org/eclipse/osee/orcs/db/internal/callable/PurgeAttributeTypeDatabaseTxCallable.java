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
package org.eclipse.osee.orcs.db.internal.callable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.jdbc.JdbcConnection;
import org.eclipse.osee.jdbc.JdbcStatement;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.db.internal.sql.join.IdJoinQuery;
import org.eclipse.osee.orcs.db.internal.sql.join.SqlJoinFactory;

/**
 * @author Angel Avila
 */
public final class PurgeAttributeTypeDatabaseTxCallable extends AbstractDatastoreTxCallable<Void> {
   private static final String RETRIEVE_GAMMAS_OF_ATTR_TYPE =
      "select gamma_id from osee_attribute where attr_type_id = ?";

   private static final String RETRIEVE_GAMMAS_OF_ATTR_MULT_TYPES =
      "select gamma_id from osee_attribute attr, osee_join_id jid where attr.attr_type_id = jid.id and jid.query_id = ?";

   private static final String RETRIEVE_GAMMAS_WITH_BRANCH_IDS =
      "select txs.branch_id, txs.gamma_id from osee_attribute attr, osee_txs txs where attr.attr_type_id = ? and txs.gamma_id = attr.gamma_id";

   private static final String RETRIEVE_GAMMAS_WITH_BRANCH_IDS_MULT_TYPES =
      "select txs.branch_id, txs.gamma_id from osee_attribute attr, osee_txs txs, osee_join_id jid where attr.attr_type_id = jid.id and jid.query_id = ? and txs.gamma_id = attr.gamma_id";

   private static final String DELETE_BY_GAMMAS_AND_BRANCH = "DELETE FROM %s WHERE gamma_id = ? AND branch_id = ?";
   private static final String DELETE_BY_GAMMAS = "DELETE FROM osee_attribute WHERE gamma_id = ?";
   private static final String DELETE_FROM_CONFLICT_TABLE_SOURCE_SIDE =
      "DELETE FROM osee_conflict WHERE source_gamma_id = ?";
   private static final String DELETE_FROM_CONFLICT_TABLE_DEST_SIDE =
      "DELETE FROM osee_conflict WHERE dest_gamma_id = ?";

   private final Collection<? extends IAttributeType> typesToPurge;
   private final SqlJoinFactory joinFactory;

   public PurgeAttributeTypeDatabaseTxCallable(Log logger, OrcsSession session, JdbcClient jdbcClient, SqlJoinFactory joinFactory, Collection<? extends IAttributeType> typesToPurge) {
      super(logger, session, jdbcClient);
      this.joinFactory = joinFactory;
      this.typesToPurge = typesToPurge;
   }

   @Override
   protected Void handleTxWork(JdbcConnection connection) throws OseeCoreException {
      List<Object[]> gammasAndBranchIds = retrieveBranchAndGammaIds(connection, typesToPurge);
      List<Object[]> gammas = retrieveGammaIds(connection, typesToPurge);
      processDeletes(connection, gammasAndBranchIds, gammas);
      return null;
   }

   private List<Object[]> retrieveGammaIds(JdbcConnection connection, Collection<? extends IAttributeType> types) throws OseeCoreException {
      List<Object[]> gammas = new LinkedList<>();
      JdbcStatement chStmt = getJdbcClient().getStatement(connection);
      IdJoinQuery joinQuery = joinFactory.createIdJoinQuery();
      try {
         if (types.size() == 1) {
            chStmt.runPreparedQuery(RETRIEVE_GAMMAS_OF_ATTR_TYPE, types.iterator().next().getGuid());
         } else {
            for (IAttributeType type : types) {
               joinQuery.add(type.getGuid());
            }
            joinQuery.store(connection);
            chStmt.runPreparedQuery(RETRIEVE_GAMMAS_OF_ATTR_MULT_TYPES, joinQuery.getQueryId());
         }
         while (chStmt.next()) {
            gammas.add(new Integer[] {chStmt.getInt("gamma_id")});
         }
      } finally {
         joinQuery.delete(connection);
         chStmt.close();
      }
      return gammas;
   }

   private List<Object[]> retrieveBranchAndGammaIds(JdbcConnection connection, Collection<? extends IAttributeType> types) throws OseeCoreException {
      List<Object[]> gammasAndBranchIds = new LinkedList<>();
      JdbcStatement chStmt = getJdbcClient().getStatement(connection);
      IdJoinQuery joinQuery = joinFactory.createIdJoinQuery();
      try {
         if (types.size() == 1) {
            chStmt.runPreparedQuery(RETRIEVE_GAMMAS_WITH_BRANCH_IDS, types.iterator().next().getGuid());
         } else {
            for (IAttributeType type : types) {
               joinQuery.add(type.getGuid());
            }
            chStmt.runPreparedQuery(RETRIEVE_GAMMAS_WITH_BRANCH_IDS_MULT_TYPES, joinQuery.getQueryId());
         }
         while (chStmt.next()) {
            gammasAndBranchIds.add(new Long[] {chStmt.getLong("gamma_id"), chStmt.getLong("branch_id")});
         }
      } finally {
         chStmt.close();
      }

      return gammasAndBranchIds;
   }

   private void processDeletes(JdbcConnection connection, List<Object[]> gammasAndBranchIds, List<Object[]> gammas) throws OseeCoreException {
      getJdbcClient().runBatchUpdate(connection, String.format(DELETE_BY_GAMMAS_AND_BRANCH, "osee_txs"),
         gammasAndBranchIds);
      getJdbcClient().runBatchUpdate(connection, String.format(DELETE_BY_GAMMAS_AND_BRANCH, "osee_txs_archived"),
         gammasAndBranchIds);

      getJdbcClient().runBatchUpdate(connection, DELETE_BY_GAMMAS, gammas);
      getJdbcClient().runBatchUpdate(connection, DELETE_FROM_CONFLICT_TABLE_SOURCE_SIDE, gammas);
      getJdbcClient().runBatchUpdate(connection, DELETE_FROM_CONFLICT_TABLE_DEST_SIDE, gammas);
   }
}