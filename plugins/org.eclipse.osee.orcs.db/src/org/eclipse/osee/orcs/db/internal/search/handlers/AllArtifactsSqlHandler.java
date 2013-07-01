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
package org.eclipse.osee.orcs.db.internal.search.handlers;

import java.util.List;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.core.ds.QueryOptions;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaAllArtifacts;
import org.eclipse.osee.orcs.db.internal.sql.AbstractSqlWriter;
import org.eclipse.osee.orcs.db.internal.sql.SqlHandler;
import org.eclipse.osee.orcs.db.internal.sql.TableEnum;

/**
 * @author Roberto E. Escobar
 */
public class AllArtifactsSqlHandler extends SqlHandler<CriteriaAllArtifacts, QueryOptions> {

   private String artAlias;
   private String txsAlias;

   @Override
   public void setData(CriteriaAllArtifacts criteria) {
      // Criteria not used
   }

   @Override
   public void addTables(AbstractSqlWriter<QueryOptions> writer) {
      List<String> artAliases = writer.getAliases(TableEnum.ARTIFACT_TABLE);
      List<String> txsAliases = writer.getAliases(TableEnum.TXS_TABLE);

      if (artAliases.isEmpty()) {
         artAlias = writer.addTable(TableEnum.ARTIFACT_TABLE);
      }
      if (txsAliases.isEmpty()) {
         txsAlias = writer.addTable(TableEnum.TXS_TABLE);
      }
   }

   @Override
   public boolean addPredicates(AbstractSqlWriter<QueryOptions> writer) throws OseeCoreException {
      boolean modified = false;
      if (artAlias != null && txsAlias != null) {
         writer.write(artAlias);
         writer.write(".gamma_id = ");
         writer.write(txsAlias);
         writer.write(".gamma_id AND ");
         writer.write(writer.getTxBranchFilter(txsAlias));
         modified = true;
      }
      return modified;
   }

   @Override
   public int getPriority() {
      return SqlHandlerPriority.ALL_ARTIFACTS.ordinal();
   }
}
