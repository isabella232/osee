/*********************************************************************
 * Copyright (c) 2015 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.ats.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.country.IAtsCountry;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.ev.IAtsEarnedValueService;
import org.eclipse.osee.ats.api.insertion.IAtsInsertion;
import org.eclipse.osee.ats.api.insertion.IAtsInsertionActivity;
import org.eclipse.osee.ats.api.program.IAtsProgram;
import org.eclipse.osee.ats.api.program.IAtsProgramService;
import org.eclipse.osee.ats.api.query.IAtsQueryService;
import org.eclipse.osee.ats.api.workdef.IAttributeResolver;
import org.eclipse.osee.ats.api.workdef.IRelationResolver;
import org.eclipse.osee.ats.api.workflow.IAtsGoal;
import org.eclipse.osee.ats.api.workflow.IAtsTask;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test Case for {@link WorkPackageUtility}
 *
 * @author Donald G. Dunne
 */
public class WorkPackageUtilityTest {

   public static final ArtifactToken workPackageArt = ArtifactToken.valueOf(5, "Work Package", CoreBranches.COMMON);

   // @formatter:off
   @Mock private IAtsGoal goal;
   @Mock private IAtsTeamWorkflow teamWf;
   @Mock private IAtsTask task;

   @Mock private AtsApi atsApi;
   @Mock private IAttributeResolver attributeResolver;
   @Mock private IRelationResolver relationResolver;

   @Mock private ArtifactToken activityArt;
   @Mock private IAtsInsertionActivity activity;

   @Mock private IAtsInsertion insertion;
   @Mock private IAtsProgram program;
   @Mock private IAtsCountry country;

   @Mock private IAtsEarnedValueService evService;
   @Mock private IAtsProgramService programService;
   @Mock private IAtsQueryService queryService;

   // @formatter:on

   private WorkPackageUtility util;

   @Before
   public void setup() {
      MockitoAnnotations.initMocks(this);
      when(teamWf.isTeamWorkflow()).thenReturn(true);

      when(task.getParentTeamWorkflow()).thenReturn(teamWf);
      when(task.isTeamWorkflow()).thenReturn(false);

      when(atsApi.getAttributeResolver()).thenReturn(attributeResolver);
      when(atsApi.getRelationResolver()).thenReturn(relationResolver);
      when(atsApi.getEarnedValueService()).thenReturn(evService);
      when(atsApi.getQueryService()).thenReturn(queryService);
      when(atsApi.getProgramService()).thenReturn(programService);

      util = new WorkPackageUtility();
   }

   @org.junit.Test
   public void testGetWorkPackageArtifact() throws Exception {
      when(attributeResolver.getSoleAttributeValue(teamWf, AtsAttributeTypes.WorkPackageReference,
         ArtifactId.SENTINEL)).thenReturn(ArtifactId.SENTINEL);
      Pair<ArtifactId, Boolean> result = util.getWorkPackageArtifact(atsApi, teamWf);
      assertResult(result, null, false);

      result = util.getWorkPackageArtifact(atsApi, teamWf);
      assertResult(result, null, false);

      when(attributeResolver.getSoleAttributeValue(teamWf, AtsAttributeTypes.WorkPackageReference,
         ArtifactId.SENTINEL)).thenReturn(workPackageArt);
      result = util.getWorkPackageArtifact(atsApi, teamWf);
      assertResult(result, workPackageArt, false);

      when(attributeResolver.getSoleAttributeValue(task, AtsAttributeTypes.WorkPackageReference,
         ArtifactId.SENTINEL)).thenReturn(ArtifactId.SENTINEL);
      result = util.getWorkPackageArtifact(atsApi, task);
      assertResult(result, workPackageArt, true);
   }

   @org.junit.Test
   public void testGetInsertionActivity() throws Exception {
      when(attributeResolver.getSoleAttributeValue(teamWf, AtsAttributeTypes.WorkPackageReference,
         ArtifactId.SENTINEL)).thenReturn(ArtifactId.SENTINEL);
      Pair<IAtsInsertionActivity, Boolean> result = util.getInsertionActivity(atsApi, teamWf);
      assertResult(result, null, false);

      when(attributeResolver.getSoleAttributeValue(teamWf, AtsAttributeTypes.WorkPackageReference,
         ArtifactId.SENTINEL)).thenReturn(workPackageArt);

      when(relationResolver.getRelatedOrNull(workPackageArt,
         AtsRelationTypes.InsertionActivityToWorkPackage_InsertionActivity)).thenReturn(null);
      result = util.getInsertionActivity(atsApi, teamWf);
      assertResult(result, null, false);

      when(relationResolver.getRelatedOrNull(workPackageArt,
         AtsRelationTypes.InsertionActivityToWorkPackage_InsertionActivity)).thenReturn(activityArt);
      when(programService.getInsertionActivityById(activityArt)).thenReturn(activity);
      result = util.getInsertionActivity(atsApi, teamWf);
      assertResult(result, activity, false);

      when(attributeResolver.getSoleAttributeValue(task, AtsAttributeTypes.WorkPackageReference,
         ArtifactId.SENTINEL)).thenReturn(ArtifactId.SENTINEL);
      result = util.getInsertionActivity(atsApi, task);
      assertResult(result, activity, true);
   }

   @Test
   public void testGetInsertion() throws Exception {
      when(attributeResolver.getSoleAttributeValue(teamWf, AtsAttributeTypes.WorkPackageReference,
         ArtifactId.SENTINEL)).thenReturn(workPackageArt);
      when(relationResolver.getRelatedOrNull(workPackageArt,
         AtsRelationTypes.InsertionActivityToWorkPackage_InsertionActivity)).thenReturn(activityArt);
      when(programService.getInsertionActivityById(activityArt)).thenReturn(null);

      Pair<IAtsInsertion, Boolean> result = util.getInsertion(atsApi, teamWf);
      assertResult(result, null, false);

      when(programService.getInsertionActivityById(activityArt)).thenReturn(activity);
      when(relationResolver.getRelatedOrNull(activity, AtsRelationTypes.InsertionToInsertionActivity_Insertion,
         IAtsInsertion.class)).thenReturn(insertion);
      result = util.getInsertion(atsApi, teamWf);
      assertResult(result, insertion, false);

      when(attributeResolver.getSoleAttributeValue(task, AtsAttributeTypes.WorkPackageReference,
         ArtifactId.SENTINEL)).thenReturn(ArtifactId.SENTINEL);
      result = util.getInsertion(atsApi, task);
      assertResult(result, insertion, true);
   }

   @Test
   public void testGetProgram() throws Exception {
      when(attributeResolver.getSoleAttributeValue(teamWf, AtsAttributeTypes.WorkPackageReference,
         ArtifactId.SENTINEL)).thenReturn(workPackageArt);
      when(relationResolver.getRelatedOrNull(workPackageArt,
         AtsRelationTypes.InsertionActivityToWorkPackage_InsertionActivity)).thenReturn(activityArt);
      when(programService.getInsertionActivityById(activityArt)).thenReturn(activity);
      when(relationResolver.getRelatedOrNull(activity, AtsRelationTypes.InsertionToInsertionActivity_Insertion,
         IAtsInsertion.class)).thenReturn(insertion);

      Pair<IAtsProgram, Boolean> result = util.getProgram(atsApi, teamWf);
      assertResult(result, null, false);

      when(relationResolver.getRelatedOrNull(insertion, AtsRelationTypes.ProgramToInsertion_Program,
         IAtsProgram.class)).thenReturn(program);
      result = util.getProgram(atsApi, teamWf);
      assertResult(result, program, false);

      when(attributeResolver.getSoleAttributeValue(task, AtsAttributeTypes.WorkPackageReference,
         ArtifactId.SENTINEL)).thenReturn(ArtifactId.SENTINEL);
      result = util.getProgram(atsApi, task);
      assertResult(result, program, true);
   }

   @Test
   public void testGetCountry() throws Exception {
      when(attributeResolver.getSoleAttributeValue(teamWf, AtsAttributeTypes.WorkPackageReference,
         ArtifactId.SENTINEL)).thenReturn(workPackageArt);
      when(relationResolver.getRelatedOrNull(workPackageArt,
         AtsRelationTypes.InsertionActivityToWorkPackage_InsertionActivity)).thenReturn(activityArt);
      when(atsApi.getQueryService().getArtifact(37L)).thenReturn(workPackageArt);
      when(programService.getInsertionActivityById(activityArt)).thenReturn(null);
      when(programService.getInsertionActivityById(activityArt)).thenReturn(activity);
      when(relationResolver.getRelatedOrNull(activity, AtsRelationTypes.InsertionToInsertionActivity_Insertion,
         IAtsInsertion.class)).thenReturn(insertion);
      when(relationResolver.getRelatedOrNull(insertion, AtsRelationTypes.ProgramToInsertion_Program,
         IAtsProgram.class)).thenReturn(program);

      Pair<IAtsCountry, Boolean> result = util.getCountry(atsApi, teamWf);
      assertResult(result, null, false);

      when(relationResolver.getRelatedOrNull(program, AtsRelationTypes.CountryToProgram_Country,
         IAtsCountry.class)).thenReturn(country);
      result = util.getCountry(atsApi, teamWf);
      assertResult(result, country, false);

      when(attributeResolver.getSoleAttributeValue(task, AtsAttributeTypes.WorkPackageReference,
         ArtifactId.SENTINEL)).thenReturn(ArtifactId.SENTINEL);
      result = util.getCountry(atsApi, task);
      assertResult(result, country, true);
   }

   private void assertResult(Pair<? extends Object, Boolean> result, Object expected, boolean inherited) {
      assertNotNull(result);
      assertEquals(expected, result.getFirst());
      assertEquals(inherited, result.getSecond());
   }

}
