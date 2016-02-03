/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.config.copy;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.core.config.ActionableItems;
import org.eclipse.osee.ats.core.config.TeamDefinitions;
import org.eclipse.osee.ats.health.ValidateAtsDatabase;
import org.eclipse.osee.ats.health.ValidateResults;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public class CopyAtsValidation {

   private final ConfigData configData;
   protected final XResultData resultData;

   public CopyAtsValidation(ConfigData configData, XResultData resultData) {
      this.configData = configData;
      this.resultData = resultData;
   }

   public void validate() throws OseeCoreException {
      configData.validateData(resultData);
      if (resultData.isErrors()) {
         return;
      }

      performValidateAtsDatabaseChecks();
      if (resultData.isErrors()) {
         return;
      }

      validateTeamDefinition(configData.getTeamDef());
      validateActionableItem(configData.getActionableItem());
   }

   private void performValidateAtsDatabaseChecks() throws OseeCoreException {
      ValidateResults results = new ValidateResults();

      // Validate AIs to TeamDefs
      Set<Artifact> aias = new HashSet<>();
      aias.addAll(AtsClientService.get().getConfigArtifacts(
         ActionableItems.getActionableItemsFromItemAndChildren(configData.getActionableItem())));
      ValidateAtsDatabase.testActionableItemToTeamDefinition(aias, results);

      // Validate TeamDefs have Workflow Definitions
      Set<IAtsTeamDefinition> teamDefs = new HashSet<>();
      teamDefs.addAll(TeamDefinitions.getTeamsFromItemAndChildren(configData.getTeamDef()));

      results.addResultsMapToResultData(resultData);
   }

   private void validateTeamDefinition(IAtsTeamDefinition teamDef) throws OseeCoreException {
      String newName = CopyAtsUtil.getConvertedName(configData, teamDef.getName());
      if (newName.equals(teamDef.getName())) {
         resultData.errorf("Could not get new name from name conversion for Team Definition [%s]", teamDef.getName());
      }
      for (IAtsTeamDefinition childTeamDef : TeamDefinitions.getTeamsFromItemAndChildren(teamDef)) {
         if (!teamDef.getUuid().equals(childTeamDef.getUuid())) {
            validateTeamDefinition(childTeamDef);
         }
      }
   }

   private void validateActionableItem(IAtsActionableItem aiArt) throws OseeCoreException {
      String newName = CopyAtsUtil.getConvertedName(configData, aiArt.getName());
      if (newName.equals(aiArt.getName())) {
         resultData.errorf("Could not get new name from name conversion for ActionableItem [%s]", aiArt.getName());
      }
      for (IAtsActionableItem childAiArt : ActionableItems.getActionableItemsFromItemAndChildren(aiArt)) {
         if (!aiArt.equals(childAiArt)) {
            validateActionableItem(childAiArt);
         }
      }
   }
}
