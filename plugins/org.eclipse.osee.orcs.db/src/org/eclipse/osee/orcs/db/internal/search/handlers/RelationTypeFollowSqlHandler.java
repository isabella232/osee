/*******************************************************************************
 * Copyright (c) 2014 Boeing.
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
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.core.ds.OptionsUtil;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaRelationTypeFollow;
import org.eclipse.osee.orcs.db.internal.sql.AbstractSqlWriter;
import org.eclipse.osee.orcs.db.internal.sql.SqlHandler;
import org.eclipse.osee.orcs.db.internal.sql.TableEnum;

/**
 * @author Roberto E. Escobar
 */
public class RelationTypeFollowSqlHandler extends SqlHandler<CriteriaRelationTypeFollow> {

   private CriteriaRelationTypeFollow criteria;

   private String artAlias0;
   private String txsAlias0;

   private String relAlias1;
   private String txsAlias1;

   private String artAlias2;
   private String txsAlias2;

   @Override
   public void setData(CriteriaRelationTypeFollow criteria) {
      this.criteria = criteria;
   }

   @Override
   public void addTables(AbstractSqlWriter writer) throws OseeCoreException {
      List<String> artAliases = writer.getAliases(TableEnum.ARTIFACT_TABLE);
      if (artAliases.isEmpty()) {
         artAlias0 = writer.addTable(TableEnum.ARTIFACT_TABLE);
         txsAlias0 = writer.addTable(TableEnum.TXS_TABLE);
      } else {
         artAlias0 = artAliases.iterator().next();
      }
      relAlias1 = writer.addTable(TableEnum.RELATION_TABLE);
      txsAlias1 = writer.addTable(TableEnum.TXS_TABLE);

      // Set to next Level
      writer.nextAliasLevel();

      artAlias2 = writer.addTable(TableEnum.ARTIFACT_TABLE);
      txsAlias2 = writer.addTable(TableEnum.TXS_TABLE);
   }

   @Override
   public boolean addPredicates(AbstractSqlWriter writer) throws OseeCoreException {
      boolean includeDeletedRelations = OptionsUtil.areDeletedRelationsIncluded(writer.getOptions());
      IRelationTypeSide typeSide = criteria.getType();

      if (txsAlias0 != null && artAlias0 != null) {
         writer.write(artAlias0);
         writer.write(".gamma_id = ");
         writer.write(txsAlias0);
         writer.write(".gamma_id");
         writer.write(" AND ");
         writer.write(writer.getTxBranchFilter(txsAlias0, includeDeletedRelations));
         writer.write("\n AND \n");
      }

      writer.write(relAlias1);
      writer.write(".rel_link_type_id = ?");
      writer.addParameter(typeSide.getGuid());

      ////////////////////// Source - Side   /////////////
      String aOrbArtId = typeSide.getSide().isSideA() ? ".b_art_id" : ".a_art_id";
      writer.write(" AND ");
      writer.write(relAlias1);
      writer.write(aOrbArtId);
      writer.write(" = ");
      writer.write(artAlias0);
      writer.write(".art_id");

      writer.write(" AND ");
      writer.write(relAlias1);
      writer.write(".gamma_id = ");
      writer.write(txsAlias1);
      writer.write(".gamma_id");
      writer.write(" AND ");
      writer.write(writer.getTxBranchFilter(txsAlias1, includeDeletedRelations));

      ////////////////////// Destination - Side   /////////////
      String oppositeAOrBartId = typeSide.getSide().isSideA() ? ".a_art_id" : ".b_art_id";
      writer.write("\n AND \n");
      writer.write(relAlias1);
      writer.write(oppositeAOrBartId);
      writer.write(" = ");
      writer.write(artAlias2);
      writer.write(".art_id");

      writer.write(" AND ");
      writer.write(artAlias2);
      writer.write(".gamma_id = ");
      writer.write(txsAlias2);
      writer.write(".gamma_id");
      writer.write(" AND ");
      writer.write(writer.getTxBranchFilter(txsAlias2, includeDeletedRelations));
      return true;
   }

   @Override
   public int getPriority() {
      return SqlHandlerPriority.FOLLOW_RELATION_TYPES.ordinal();
   }

}
