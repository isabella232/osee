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
package org.eclipse.osee.orcs.db.internal.search.handlers;

import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.core.ds.OptionsUtil;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaArtifactIds;
import org.eclipse.osee.orcs.db.internal.sql.AbstractSqlWriter;
import org.eclipse.osee.orcs.db.internal.sql.AliasEntry;
import org.eclipse.osee.orcs.db.internal.sql.ObjectType;
import org.eclipse.osee.orcs.db.internal.sql.SqlHandler;
import org.eclipse.osee.orcs.db.internal.sql.SqlUtil;
import org.eclipse.osee.orcs.db.internal.sql.TableEnum;
import org.eclipse.osee.orcs.db.internal.sql.WithClause;
import org.eclipse.osee.orcs.db.internal.sql.join.AbstractJoinQuery;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactIdsSqlHandler extends SqlHandler<CriteriaArtifactIds> {
   private static final AliasEntry ART_ID_WITH = SqlUtil.newAlias("historicalArtifactIdWith", "artIdHist");

   private CriteriaArtifactIds criteria;

   private String artAlias;
   private String jIdAlias;
   private String txsAlias;
   private String withClauseName;
   private WithClause withClause;

   @Override
   public void addWithTables(AbstractSqlWriter writer) {
      if (OptionsUtil.isHistorical(writer.getOptions())) {
         StringBuilder sb = new StringBuilder();
         sb.append(
            "SELECT max(txs.transaction_id) as transaction_id, art.art_id\n    FROM osee_txs txs, osee_artifact art");
         if (criteria.hasMultipleIds()) {
            sb.append(", osee_join_id id");
         }
         sb.append("\n    WHERE txs.gamma_id = art.gamma_id\n");
         if (criteria.hasMultipleIds()) {
            AbstractJoinQuery joinQuery = writer.writeJoin(criteria.getIds());
            sb.append("    AND art.art_id = id.id AND id.query_id = ?");
            writer.addParameter(joinQuery.getQueryId());
         } else {
            sb.append("    AND art.art_id = ?");
            writer.addParameter(criteria.getId());
         }
         sb.append(" AND ");
         sb.append(writer.getWithClauseTxBranchFilter("txs", false));
         sb.append("\n    GROUP BY art.art_id");
         String body = sb.toString();

         withClauseName = writer.getNextAlias(ART_ID_WITH);
         withClause = SqlUtil.newSimpleWithClause(withClauseName, body);
         writer.addWithClause(withClause);
         writer.addTable(withClauseName);
      }
   }

   @Override
   public void setData(CriteriaArtifactIds criteria) {
      this.criteria = criteria;
   }

   @Override
   public void addTables(AbstractSqlWriter writer) {
      if (criteria.hasMultipleIds() && !OptionsUtil.isHistorical(writer.getOptions())) {
         jIdAlias = writer.addTable(TableEnum.ID_JOIN_TABLE);
      }
      artAlias = writer.addTable(TableEnum.ARTIFACT_TABLE);
      txsAlias = writer.addTable(TableEnum.TXS_TABLE, ObjectType.ARTIFACT);
   }

   @Override
   public boolean addPredicates(AbstractSqlWriter writer) throws OseeCoreException {

      if (OptionsUtil.isHistorical(writer.getOptions())) {
         writer.write(withClauseName);
         writer.write(".transaction_id = ");
         writer.write(txsAlias);
         writer.write(".transaction_id AND ");
         writer.write(withClauseName);
         writer.write(".art_id = ");
         writer.write(artAlias);
         writer.write(".art_id");
      } else {
         writer.write(artAlias);
         if (criteria.hasMultipleIds()) {
            AbstractJoinQuery joinQuery = writer.writeJoin(criteria.getIds());
            writer.write(".art_id = ");
            writer.write(jIdAlias);
            writer.write(".id AND ");
            writer.write(jIdAlias);
            writer.write(".query_id = ?");
            writer.addParameter(joinQuery.getQueryId());
         } else {
            writer.write(".art_id = ?");
            writer.addParameter(criteria.getId());
         }
      }

      writer.write(" AND ");
      writer.write(artAlias);
      writer.write(".gamma_id = ");
      writer.write(txsAlias);
      writer.write(".gamma_id AND ");
      writer.write(writer.getTxBranchFilter(txsAlias));
      return true;
   }

   @Override
   public int getPriority() {
      return SqlHandlerPriority.ARTIFACT_ID.ordinal();
   }
}