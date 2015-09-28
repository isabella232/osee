/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.config;

import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.country.IAtsCountry;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.insertion.IAtsInsertion;
import org.eclipse.osee.ats.api.insertion.IAtsInsertionActivity;
import org.eclipse.osee.ats.api.program.IAtsProgram;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Donald G. Dunne
 */
public class WorkPackageUtility {

   public Pair<IAtsCountry, Boolean> getCountry(IAtsServices services, IAtsWorkItem workItem) {
      Pair<IAtsCountry, Boolean> result = new Pair<>(null, false);
      Pair<IAtsProgram, Boolean> programResult = getProgram(services, workItem);
      if (programResult.getFirst() != null) {
         IAtsCountry country = services.getRelationResolver().getRelatedOrNull(programResult.getFirst(),
            AtsRelationTypes.CountryToProgram_Country, IAtsCountry.class);
         if (country != null) {
            result.setFirst(country);
            result.setSecond(programResult.getSecond());
         }
      }
      return result;
   }

   public Pair<IAtsProgram, Boolean> getProgram(IAtsServices services, IAtsWorkItem workItem) {
      Pair<IAtsProgram, Boolean> result = new Pair<>(null, false);
      Pair<IAtsInsertion, Boolean> insertionResult = getInsertion(services, workItem);
      if (insertionResult.getFirst() != null) {
         IAtsProgram program = services.getRelationResolver().getRelatedOrNull(insertionResult.getFirst(),
            AtsRelationTypes.ProgramToInsertion_Program, IAtsProgram.class);
         if (program != null) {
            result.setFirst(program);
            result.setSecond(insertionResult.getSecond());
         }
      }
      return result;
   }

   /**
    * @return IAtsInsertionActivity and true if value is inherited from parent team wf, false otherwise
    */
   public Pair<IAtsInsertion, Boolean> getInsertion(IAtsServices services, IAtsWorkItem workItem) {
      Pair<IAtsInsertion, Boolean> result = new Pair<>(null, false);
      Pair<IAtsInsertionActivity, Boolean> insertionActivityResult = getInsertionActivity(services, workItem);
      if (insertionActivityResult.getFirst() != null) {
         IAtsInsertion insertion = services.getRelationResolver().getRelatedOrNull(insertionActivityResult.getFirst(),
            AtsRelationTypes.InsertionToInsertionActivity_Insertion, IAtsInsertion.class);
         if (insertion != null) {
            result.setFirst(insertion);
            result.setSecond(insertionActivityResult.getSecond());
         }
      }
      return result;
   }

   /**
    * @return IAtsInsertionActivity and true if value is inherited from parent team wf, false otherwise
    */
   public Pair<IAtsInsertionActivity, Boolean> getInsertionActivity(IAtsServices services, IAtsWorkItem workItem) {
      Pair<IAtsInsertionActivity, Boolean> result = new Pair<>(null, false);
      Pair<ArtifactId, Boolean> workPackageResult = getWorkPackageArtifact(services, workItem);
      if (workPackageResult.getFirst() != null) {
         ArtifactId activityArt = services.getRelationResolver().getRelatedOrNull(workPackageResult.getFirst(),
            AtsRelationTypes.InsertionActivityToWorkPackage_InsertionActivity);
         if (activityArt != null) {
            IAtsInsertionActivity activity = services.getConfigItemFactory().getInsertionActivity(activityArt);
            result.setFirst(activity);
            result.setSecond(workPackageResult.getSecond());
         }
      }
      return result;
   }

   /**
    * @return Work Package artifact and true if value is inherited from parent team wf, false otherwise
    */
   public Pair<ArtifactId, Boolean> getWorkPackageArtifact(IAtsServices services, IAtsWorkItem workItem) {
      Pair<ArtifactId, Boolean> result = new Pair<>(null, false);
      String workPackageGuid =
         services.getAttributeResolver().getSoleAttributeValue(workItem, AtsAttributeTypes.WorkPackageGuid, null);
      if (Strings.isValid(workPackageGuid)) {
         ArtifactId workPackageArt = services.getArtifactById(workPackageGuid);
         if (workPackageArt != null) {
            result.setFirst(workPackageArt);
         }
      }
      if (result.getFirst() == null && !workItem.isTeamWorkflow()) {
         IAtsTeamWorkflow teamWf = workItem.getParentTeamWorkflow();
         Pair<ArtifactId, Boolean> teamResult = getWorkPackageArtifact(services, teamWf);
         if (teamResult.getFirst() != null) {
            result.setSecond(true);
            result.setFirst(teamResult.getFirst());
         }
      }
      return result;
   }

}
