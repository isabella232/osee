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
package org.eclipse.osee.ats.impl.internal.convert;

import java.util.List;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.impl.IAtsServer;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.BranchReadable;
import org.eclipse.osee.orcs.transaction.TransactionBuilder;

/**
 * See description below
 * 
 * @author Donald G Dunne
 */
public class ConvertBaselineGuidToBaselineUuid extends AbstractConvertGuidToUuid {

   // Leave this attribute definition and conversion for other OSEE sites to convert
   private static final IAttributeType BaselineBranchGuid = TokenFactory.createAttributeType(0x10000000000000A9L,
      "ats.Baseline Branch Guid");

   public ConvertBaselineGuidToBaselineUuid(Log logger, IOseeDatabaseService dbService, OrcsApi orcsApi, IAtsServer atsServer) {
      super(logger, dbService, orcsApi, atsServer);
   }

   @Override
   public void run(XResultData data, boolean reportOnly) {
      if (reportOnly) {
         data.log("REPORT ONLY - Changes not persisted\n");
      }
      if (!getOrcsApi().getOrcsTypes(null).getAttributeTypes().exists(AtsAttributeTypes.BaselineBranchUuid)) {
         data.logError("ats.BaselineBranchUuid is not configured for this database");
         return;
      }
      TransactionBuilder tx = createTransactionBuilder();
      int numChanges = 0;
      for (ArtifactReadable art : getOrcsApi().getQueryFactory(null).fromBranch(AtsUtilCore.getAtsBranch()).andTypeEquals(
         AtsArtifactTypes.Version, AtsArtifactTypes.TeamDefinition).andExists(BaselineBranchGuid).getResults()) {
         List<String> attributeValues = art.getAttributeValues(BaselineBranchGuid);
         for (String guid : attributeValues) {
            if (!guid.isEmpty()) {
               BranchReadable branch = null;
               try {
                  branch = getBranch(guid);
               } catch (Exception ex) {
                  // do nothing
               }
               if (branch == null) {
                  data.logErrorWithFormat("Branch with guid %s can't be found", guid);
               } else {
                  long branchUuid = branch.getUuid();
                  String uuid = art.getSoleAttributeAsString(AtsAttributeTypes.BaselineBranchUuid, null);
                  if (!Strings.isValid(uuid) || isUuidDifferent(uuid, branchUuid)) {
                     if (!Strings.isValid(uuid)) {
                        data.logWithFormat(
                           "Adding uuid attribute of value %d to artifact type [%s] name [%s] id [%s]\n", branchUuid,
                           art.getArtifactType(), art.getName(), art.getGuid());
                     } else if (isUuidDifferent(uuid, branchUuid)) {
                        data.logWithFormat(
                           "Updating uuid attribute of value %d to artifact type [%s] name [%s] id [%s]\n", branchUuid,
                           art.getArtifactType(), art.getName(), art.getGuid());
                     }
                     numChanges++;
                     if (!reportOnly) {
                        tx.setSoleAttributeValue(art, AtsAttributeTypes.BaselineBranchUuid, String.valueOf(branchUuid));
                     }
                  }
               }
            }
         }
      }
      if (!reportOnly) {
         data.log("\n" + numChanges + " Changes Persisted");
         tx.commit();
      } else {
         data.log("\n" + numChanges + " Need to be Changed");
      }
   }

   @Override
   public String getDescription() {
      StringBuffer data = new StringBuffer();
      data.append("ConvertBaselineGuidToBaselineUuid (required conversion)\n\n");
      data.append("Necessary for upgrading from OSEE 0.16.2 to 0.17.0");
      data.append("- Verify that ats.BaselineBranchUuid is a valid attribute type\n");
      data.append("- Verify Add uuid attribute for every ats.BaselineBranchGuid attribute on Version artifacts\n");
      data.append("- Verify Add uuid attribute for every ats.BaselineBranchGuid attribute on Team Definition artifacts\n\n");
      data.append("NOTE: This operation can be run multiple times\n");
      data.append("Manual Cleanup (optional): Use Purge Attribute Type BLAM to remove the ats.BaselineBranchGuid attributes.");
      return data.toString();
   }

   @Override
   public String getName() {
      return "ConvertBaselineGuidToBaselineUuid";
   }

   private boolean isUuidDifferent(String uuid, long branchUuid) {
      return Strings.isValid(uuid) && !Long.valueOf(uuid).equals(branchUuid);
   }
}
