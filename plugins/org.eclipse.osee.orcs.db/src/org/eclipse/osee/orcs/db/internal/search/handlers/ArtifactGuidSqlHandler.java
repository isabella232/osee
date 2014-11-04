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

import java.util.Collection;
import org.eclipse.osee.framework.database.core.AbstractJoinQuery;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.core.ds.OptionsUtil;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaArtifactGuids;
import org.eclipse.osee.orcs.db.internal.sql.AbstractSqlWriter;
import org.eclipse.osee.orcs.db.internal.sql.AliasEntry;
import org.eclipse.osee.orcs.db.internal.sql.ObjectType;
import org.eclipse.osee.orcs.db.internal.sql.SqlHandler;
import org.eclipse.osee.orcs.db.internal.sql.SqlUtil;
import org.eclipse.osee.orcs.db.internal.sql.TableEnum;
import org.eclipse.osee.orcs.db.internal.sql.WithClause;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactGuidSqlHandler extends SqlHandler<CriteriaArtifactGuids> {

   private static final AliasEntry ART_GUID_WITH = SqlUtil.newAlias("artifactGuidWith", "artUuid");

   private CriteriaArtifactGuids criteria;

   private String artAlias;
   private String jguidAlias;
   private String txsAlias;
   private AbstractJoinQuery joinQuery;
   private String withClauseName;
   private WithClause withClause;

   @Override
   public void addWithTables(AbstractSqlWriter writer) {
      if (OptionsUtil.isHistorical(writer.getOptions())) {
         StringBuilder sb = new StringBuilder();
         sb.append("SELECT max(txs.transaction_id) as transaction_id, art.art_id as art_id\n");
         Collection<String> ids = criteria.getIds();
         if (ids.size() > 1) {
            sb.append("    FROM osee_txs txs, osee_artifact art, osee_join_char_id id\n");
         } else {
            sb.append("    FROM osee_txs txs, osee_artifact art\n");
         }
         sb.append("    WHERE txs.gamma_id = art.gamma_id\n");
         if (ids.size() > 1) {
            AbstractJoinQuery joinQuery = writer.writeCharJoin(ids);
            sb.append("    AND art.guid = id.id AND id.query_id = ?");

            writer.addParameter(joinQuery.getQueryId());
         } else {
            sb.append("    AND art.guid = ?");
            writer.addParameter(ids.iterator().next());
         }
         sb.append(" AND ");
         sb.append(writer.getAllChangesTxBranchFilter("txs"));
         sb.append("\n    GROUP BY art.art_id");
         String body = sb.toString();

         withClauseName = writer.getNextAlias(ART_GUID_WITH);
         withClause = SqlUtil.newSimpleWithClause(withClauseName, body);
         writer.addWithClause(withClause);
         writer.addTable(withClauseName);
      }
   }

   @Override
   public void setData(CriteriaArtifactGuids criteria) {
      this.criteria = criteria;
   }

   @Override
   public void addTables(AbstractSqlWriter writer) {
      if (criteria.getIds().size() > 1) {
         jguidAlias = writer.addTable(TableEnum.CHAR_JOIN_TABLE);
      }
      artAlias = writer.addTable(TableEnum.ARTIFACT_TABLE);
      txsAlias = writer.addTable(TableEnum.TXS_TABLE, ObjectType.ARTIFACT);
   }

   @Override
   public boolean addPredicates(AbstractSqlWriter writer) throws OseeCoreException {
      Collection<String> ids = criteria.getIds();
      if (ids.size() > 1) {
         joinQuery = writer.writeCharJoin(ids);
         writer.write(artAlias);
         writer.write(".guid = ");
         writer.write(jguidAlias);
         writer.write(".id AND ");
         writer.write(jguidAlias);
         writer.write(".query_id = ?");
         writer.addParameter(joinQuery.getQueryId());
      } else {
         writer.write(artAlias);
         writer.write(".guid = ?");
         writer.addParameter(ids.iterator().next());
      }
      if (withClause != null) {
         writer.writeAndLn();
         writer.write(withClauseName);
         writer.write(".transaction_id = ");
         writer.write(txsAlias);
         writer.write(".transaction_id AND ");
         writer.write(withClauseName);
         writer.write(".art_id = ");
         writer.write(artAlias);
         writer.write(".art_id");
      }
      writer.write(" AND ");
      writer.write(artAlias);
      writer.write(".gamma_id = ");
      writer.write(txsAlias);
      writer.write(".gamma_id AND ");

      boolean includeDeletedArtifacts = OptionsUtil.areDeletedArtifactsIncluded(writer.getOptions());
      writer.write(writer.getTxBranchFilter(txsAlias, includeDeletedArtifacts));
      return true;
   }

   @Override
   public int getPriority() {
      return SqlHandlerPriority.ARTIFACT_GUID.ordinal();
   }

}
