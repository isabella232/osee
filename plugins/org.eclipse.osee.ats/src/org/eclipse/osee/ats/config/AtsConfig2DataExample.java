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
package org.eclipse.osee.ats.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.core.workdef.WorkDefinitionSheet;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.workdef.AtsWorkDefinitionSheetProviders;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * Example configuration of ATS using the AtsConfig2 data and operation classes. This can be run from demo database from
 * ATS Navigator. It will create 3 actionable items, 2 teams (software and requirements), 1 team workdefinition and 1
 * task workdefinition that will be configured for the Software team.
 * 
 * @author Donald G. Dunne
 */
public class AtsConfig2DataExample extends AbstractAtsConfig2Data {

   public static ArtifactToken Software_Team = TokenFactory.createArtifactToken(4696084, "AGZs2EW0tWRkIv3OqfAA",
      "AtsConfig2 Software", AtsArtifactTypes.TeamDefinition);
   public static ArtifactToken Requirements_Team = TokenFactory.createArtifactToken(4696085, "AGZs2EU1d3db9M57WJQA",
      "AtsConfig2 Requirements", AtsArtifactTypes.TeamDefinition);

   public AtsConfig2DataExample() {
      super("AtsConfig2 Example Configuration");
   }

   @Override
   public void performPostConfig(IAtsChangeSet changes, AbstractAtsConfig2Data data) {
      Artifact dtsSoftware = ArtifactQuery.getArtifactFromToken(Software_Team, AtsUtilCore.getAtsBranch());
      dtsSoftware.addAttribute(AtsAttributeTypes.RelatedTaskWorkDefinition, "WorkDef_Task_AtsConfig2Example");
      changes.add(dtsSoftware);
   }

   @Override
   public Collection<WorkDefinitionSheet> getTeamsAiSheets() {
      List<WorkDefinitionSheet> sheets = new ArrayList<>();
      sheets.add(new WorkDefinitionSheet("WorkDef_Team_AtsConfig2_AIs_And_Teams",
         AtsWorkDefinitionSheetProviders.getSupportFile(Activator.PLUGIN_ID,
            "support/WorkDef_Team_AtsConfig2Example_AIs_And_Teams.ats")));
      return sheets;
   }

   @Override
   public Collection<WorkDefinitionSheet> getWorkDefSheets() {
      List<WorkDefinitionSheet> sheets = new ArrayList<>();
      sheets.add(
         new WorkDefinitionSheet("WorkDef_Team_AtsConfig2Example", AtsWorkDefinitionSheetProviders.getSupportFile(
            Activator.PLUGIN_ID, "support/WorkDef_Team_AtsConfig2Example.ats")));
      sheets.add(
         new WorkDefinitionSheet("WorkDef_Task_AtsConfig2Example", AtsWorkDefinitionSheetProviders.getSupportFile(
            Activator.PLUGIN_ID, "support/WorkDef_Task_AtsConfig2Example.ats")));
      return sheets;
   }

}
