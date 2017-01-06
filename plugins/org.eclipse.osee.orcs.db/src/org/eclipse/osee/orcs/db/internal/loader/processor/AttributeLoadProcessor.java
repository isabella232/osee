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
package org.eclipse.osee.orcs.db.internal.loader.processor;

import org.eclipse.osee.framework.core.data.ApplicabilityId;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.jdbc.JdbcStatement;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.AttributeData;
import org.eclipse.osee.orcs.core.ds.Options;
import org.eclipse.osee.orcs.core.ds.OptionsUtil;
import org.eclipse.osee.orcs.core.ds.VersionData;
import org.eclipse.osee.orcs.db.internal.loader.data.AttributeObjectFactory;

/**
 * @author Roberto E. Escobar
 */
public class AttributeLoadProcessor extends LoadProcessor<AttributeData, AttributeObjectFactory> {

   private final Log logger;

   public AttributeLoadProcessor(Log logger, AttributeObjectFactory factory) {
      super(factory);
      this.logger = logger;
   }

   @Override
   protected AttributeData createData(Object conditions, AttributeObjectFactory factory, JdbcStatement chStmt, Options options) throws OseeCoreException {
      AttributeData toReturn = null;

      BranchId branch = BranchId.valueOf(chStmt.getLong("branch_id"));
      int artId = chStmt.getInt("art_id");
      int attrId = chStmt.getInt("attr_id");
      long gammaId = chStmt.getInt("gamma_id");
      ModificationType modType = ModificationType.getMod(chStmt.getInt("mod_type"));
      ApplicabilityId applicId = ApplicabilityId.valueOf(chStmt.getLong("app_id"));

      boolean historical = OptionsUtil.isHistorical(options);

      CreateConditions condition = asConditions(conditions);
      if (!condition.isSame(branch, artId, attrId)) {
         condition.saveConditions(branch, artId, attrId, gammaId, modType);

         TransactionId txId = TransactionId.valueOf(chStmt.getLong("transaction_id"));

         VersionData version = factory.createVersion(branch, txId, gammaId, historical);
         if (historical) {
            version.setStripeId(TransactionId.valueOf(chStmt.getLong("stripe_transaction_id")));
         }

         AttributeTypeId attributeType = AttributeTypeId.valueOf(chStmt.getLong("attr_type_id"));

         String value = chStmt.getString("value");
         String uri = chStmt.getString("uri");

         toReturn = factory.createAttributeData(version, attrId, attributeType, modType, artId, value, uri, applicId);

      } else {
         if (!historical) {
            logger.warn(
               "multiple attribute versions for attribute id [%d] artifact id[%d] branch[%d] previousGammaId[%s] currentGammaId[%s] previousModType[%s] currentModType[%s]",
               attrId, artId, branch, condition.previousGammaId, gammaId, condition.previousModType, modType);
         }
      }
      return toReturn;
   }

   @Override
   protected Object createPreConditions(Options options) {
      return new CreateConditions();
   }

   private CreateConditions asConditions(Object conditions) {
      return (CreateConditions) conditions;
   }

   private static final class CreateConditions {
      int previousArtId = -1;
      BranchId previousBranchId = BranchId.SENTINEL;
      int previousAttrId = -1;
      long previousGammaId = -1;
      ModificationType previousModType = null;

      boolean isSame(BranchId branch, int artifactId, int attrId) {
         return previousBranchId.equals(branch) && previousArtId == artifactId && previousAttrId == attrId;
      }

      void saveConditions(BranchId branch, int artifactId, int attrId, long gammaId, ModificationType modType) {
         previousBranchId = branch;
         previousArtId = artifactId;
         previousAttrId = attrId;
         previousGammaId = gammaId;
         previousModType = modType;
      }

   }
}