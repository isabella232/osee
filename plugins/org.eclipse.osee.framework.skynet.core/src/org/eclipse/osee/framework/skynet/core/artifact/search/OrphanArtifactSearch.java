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
package org.eclipse.osee.framework.skynet.core.artifact.search;

import java.util.List;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.services.IdentityService;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;

/**
 * @author Jeff C. Phillips
 */
public class OrphanArtifactSearch implements ISearchPrimitive {
   private static final String LABEL = "Orphan Search: ";
   private static final String tables = "osee_artifact";
   private static final String sql =
      "art_type_id =? AND art_id NOT in (SELECT t2.art_id FROM osee_relation_link t1, osee_artifact t2, osee_txs t4, (SELECT Max(t1.gamma_id) AS gamma_id, t1.rel_link_id, t2.branch_id FROM osee_relation_link t1, osee_txs t2 WHERE t1.gamma_id = t2.gamma_id AND t2.branch_id = ? GROUP BY t1.rel_link_id, t2.branch_id) t6 WHERE t1.rel_link_type_id =? AND t1.b_art_id = t2.art_id AND t1.gamma_id = t4.gamma_id AND t1.rel_link_id = t6.rel_link_id AND t4.branch_id = t6.branch_id AND t1.gamma_id = t6.gamma_id AND t4.mod_type <> " + ModificationType.DELETED.getValue() + " GROUP BY t2.art_id)";
   private final IArtifactType aritfactType;
   private final long relationTypeId;

   public OrphanArtifactSearch(IArtifactType aritfactType) throws OseeCoreException {
      this.aritfactType = aritfactType;
      this.relationTypeId = ServiceUtil.getIdentityService().getLocalId(CoreRelationTypes.Default_Hierarchical__Child);
   }

   @Override
   public String getCriteriaSql(List<Object> dataList, IOseeBranch branch) throws OseeCoreException {
      IdentityService identityService = ServiceUtil.getIdentityService();
      dataList.add(identityService.getLocalId(aritfactType));
      dataList.add(BranchManager.getBranchId(branch));
      dataList.add(relationTypeId);

      return sql;
   }

   @Override
   public String getArtIdColName() {
      return "art_id";
   }

   @Override
   public String getTableSql(List<Object> dataList, IOseeBranch branch) {
      return tables;
   }

   @Override
   public String toString() {
      return LABEL + aritfactType.getName();
   }

   public static OrphanArtifactSearch getPrimitive(String storageString) {
      storageString = storageString.replace(LABEL, "");
      if (storageString.endsWith(";")) {
         storageString = storageString.substring(0, storageString.length() - 1);
      }
      OrphanArtifactSearch search = null;
      try {
         ArtifactType artifactType = ArtifactTypeManager.getType(storageString);
         search = new OrphanArtifactSearch(artifactType);
      } catch (OseeCoreException ex) {
         throw new IllegalStateException("Value for " + OrphanArtifactSearch.class.getSimpleName() + " not parsable");
      }
      return search;
   }

   @Override
   public String getStorageString() {
      return LABEL + aritfactType.getName() + ";";
   }
}
