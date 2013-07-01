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
import java.util.HashSet;
import java.util.List;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.database.core.AbstractJoinQuery;
import org.eclipse.osee.orcs.core.ds.QueryOptions;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaArtifactType;
import org.eclipse.osee.orcs.db.internal.sql.AbstractSqlWriter;
import org.eclipse.osee.orcs.db.internal.sql.SqlHandler;
import org.eclipse.osee.orcs.db.internal.sql.TableEnum;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactTypeSqlHandler extends SqlHandler<CriteriaArtifactType, QueryOptions> {

   private CriteriaArtifactType criteria;

   private String jIdAlias;
   private String artAlias;
   private String txsAlias;
   private AbstractJoinQuery joinQuery;
   private Collection<Integer> typeIds;

   private List<String> artAliases;
   private List<String> txsAliases;

   @Override
   public void setData(CriteriaArtifactType criteria) {
      this.criteria = criteria;
   }

   @Override
   public void addTables(AbstractSqlWriter<QueryOptions> writer) throws OseeCoreException {
      typeIds = getLocalTypeIds(writer.getOptions());
      if (typeIds.size() > 1) {
         jIdAlias = writer.addTable(TableEnum.ID_JOIN_TABLE);
      }

      artAliases = writer.getAliases(TableEnum.ARTIFACT_TABLE);
      txsAliases = writer.getAliases(TableEnum.TXS_TABLE);

      if (artAliases.isEmpty()) {
         artAlias = writer.addTable(TableEnum.ARTIFACT_TABLE);
      }
      if (txsAliases.isEmpty()) {
         txsAlias = writer.addTable(TableEnum.TXS_TABLE);
      }
      artAliases = writer.getAliases(TableEnum.ARTIFACT_TABLE);
      txsAliases = writer.getAliases(TableEnum.TXS_TABLE);
   }

   private Collection<Integer> getLocalTypeIds(QueryOptions options) throws OseeCoreException {
      Collection<? extends IArtifactType> types = criteria.getTypes(options);
      Collection<Integer> toReturn = new HashSet<Integer>();
      for (IArtifactType type : types) {
         int localId = getIdentityService().getLocalId(type);
         toReturn.add(localId);
      }
      return toReturn;
   }

   @Override
   public boolean addPredicates(AbstractSqlWriter<QueryOptions> writer) throws OseeCoreException {
      boolean modified = false;

      if (typeIds.size() > 1) {
         modified = true;
         joinQuery = writer.writeIdJoin(typeIds);
         writer.write(jIdAlias);
         writer.write(".query_id = ?");
         writer.addParameter(joinQuery.getQueryId());

         if (!artAliases.isEmpty()) {
            writer.write(" AND ");
            int aSize = artAliases.size();
            for (int index = 0; index < aSize; index++) {
               String artAlias = artAliases.get(index);
               writer.write(artAlias);
               writer.write(".art_type_id = ");
               writer.write(jIdAlias);
               writer.write(".id");
               if (index + 1 < aSize) {
                  writer.write(" AND ");
               }
            }
         }
      } else {
         modified = true;
         int localId = typeIds.iterator().next();

         int aSize = artAliases.size();
         for (int index = 0; index < aSize; index++) {
            String artAlias = artAliases.get(index);
            writer.write(artAlias);
            writer.write(".art_type_id = ?");
            writer.addParameter(localId);
            if (index + 1 < aSize) {
               writer.write(" AND ");
            }
         }
      }

      if (artAlias != null && txsAlias != null) {
         writer.write(" AND ");
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
      return SqlHandlerPriority.ARTIFACT_TYPE.ordinal();
   }
}
