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
package org.eclipse.osee.ats.core.program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.config.WorkType;
import org.eclipse.osee.ats.api.country.IAtsCountry;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.ev.IAtsWorkPackage;
import org.eclipse.osee.ats.api.insertion.IAtsInsertion;
import org.eclipse.osee.ats.api.insertion.IAtsInsertionActivity;
import org.eclipse.osee.ats.api.program.IAtsProgram;
import org.eclipse.osee.ats.api.program.IAtsProgramService;
import org.eclipse.osee.ats.api.program.ProjectType;
import org.eclipse.osee.ats.api.query.IAtsConfigQuery;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.api.workflow.IAtsAction;
import org.eclipse.osee.ats.api.workflow.IAtsTask;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.WorkItemType;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Donald G. Dunne
 */
public class AtsProgramService implements IAtsProgramService {

   private final IAtsServices services;

   public AtsProgramService(IAtsServices services) {
      this.services = services;
   }

   @Override
   public Collection<IAtsInsertionActivity> getInsertionActivities(IAtsInsertion insertion) {
      List<IAtsInsertionActivity> insertionActivitys = new ArrayList<>();
      for (ArtifactId artifact : services.getRelationResolver().getRelated(services.getArtifact(insertion.getUuid()),
         AtsRelationTypes.InsertionToInsertionActivity_InsertionActivity)) {
         insertionActivitys.add(services.getConfigItemFactory().getInsertionActivity(artifact));
      }
      return insertionActivitys;
   }

   @Override
   public IAtsInsertionActivity getInsertionActivity(Long insertionActivityUuid) {
      return services.getConfigItemFactory().getInsertionActivity(services.getArtifact(insertionActivityUuid));
   }

   @Override
   public IAtsWorkPackage getWorkPackage(Long workPackageUuid) {
      return services.getConfigItemFactory().getWorkPackage(services.getArtifact(workPackageUuid));
   }

   @Override
   public IAtsInsertionActivity getInsertionActivity(IAtsWorkPackage workPackage) {
      ArtifactId wpArt = services.getArtifact(workPackage.getUuid());
      Collection<ArtifactId> related = services.getRelationResolver().getRelated(wpArt,
         AtsRelationTypes.InsertionActivityToWorkPackage_InsertionActivity);
      if (related.size() > 0) {
         return services.getConfigItemFactory().getInsertionActivity(related.iterator().next());
      }
      return null;
   }

   @Override
   public IAtsInsertion getInsertion(IAtsInsertionActivity activity) {
      Collection<ArtifactId> related = services.getRelationResolver().getRelated(activity.getStoreObject(),
         AtsRelationTypes.InsertionToInsertionActivity_Insertion);
      if (related.size() > 0) {
         return services.getConfigItemFactory().getInsertion(related.iterator().next());
      }
      return null;
   }

   @Override
   public IAtsProgram getProgram(IAtsInsertion insertion) {
      Collection<ArtifactId> related = services.getRelationResolver().getRelated(insertion.getStoreObject(),
         AtsRelationTypes.ProgramToInsertion_Program);
      if (related.size() > 0) {
         return services.getConfigItemFactory().getProgram(related.iterator().next());
      }
      return null;
   }

   @Override
   public void setWorkPackage(IAtsWorkPackage workPackage, List<IAtsWorkItem> workItems) {
      IAtsChangeSet changes = services.getStoreService().createAtsChangeSet("Set Work Package");
      for (IAtsWorkItem workItem : workItems) {
         if (workPackage == null) {
            changes.deleteSoleAttribute(workItem, AtsAttributeTypes.WorkPackageGuid);
         } else {
            changes.setSoleAttributeValue(workItem, AtsAttributeTypes.WorkPackageGuid, workPackage.getGuid());
         }
      }
      if (!changes.isEmpty()) {
         changes.execute();
      }
   }

   @Override
   public IAtsInsertion getInsertion(Long insertionUuid) {
      return services.getConfigItemFactory().getInsertion(services.getArtifact(insertionUuid));
   }

   @Override
   public Collection<IAtsInsertion> getInsertions(IAtsProgram program) {
      List<IAtsInsertion> insertions = new ArrayList<>();
      for (ArtifactId artifact : services.getRelationResolver().getRelated(services.getArtifact(program.getUuid()),
         AtsRelationTypes.ProgramToInsertion_Insertion)) {
         insertions.add(services.getConfigItemFactory().getInsertion(artifact));
      }
      return insertions;
   }

   @Override
   public IAtsProgram getProgramByGuid(String guid) {
      ArtifactId prgArt = services.getArtifactById(guid);
      return services.getConfigItemFactory().getProgram(prgArt);
   }

   @Override
   public Collection<IAtsProgram> getPrograms() {
      List<IAtsProgram> programs = new ArrayList<>();
      for (ArtifactId artifact : services.getQueryService().createQuery(
         AtsArtifactTypes.Program).getResultArtifacts()) {
         programs.add(services.getConfigItemFactory().getProgram(artifact));
      }
      return programs;
   }

   @Override
   public IAtsProgram getProgram(Long programUuid) {
      return services.getConfigItemFactory().getProgram(services.getArtifact(programUuid));
   }

   @Override
   public List<IAtsProgram> getPrograms(IAtsCountry atsCountry) {
      List<IAtsProgram> programs = new LinkedList<>();
      ArtifactId artifact = atsCountry.getStoreObject();
      if (artifact != null) {
         for (ArtifactId related : services.getRelationResolver().getRelated(artifact,
            AtsRelationTypes.CountryToProgram_Program)) {
            programs.add(services.getConfigItemFactory().getProgram(related));
         }
      }
      return programs;
   }

   @Override
   public IAtsCountry getCountry(IAtsProgram atsProgram) {
      IAtsCountry country = null;
      ArtifactId artifact = atsProgram.getStoreObject();
      if (artifact != null) {
         ArtifactId countryArt =
            services.getRelationResolver().getRelatedOrNull(artifact, AtsRelationTypes.CountryToProgram_Country);
         if (countryArt != null) {
            country = services.getConfigItemFactory().getCountry(countryArt);
         }
      }
      return country;
   }

   @Override
   public IAtsProgram getProgram(IAtsWorkItem workItem) {
      IAtsTeamDefinition teamDef = workItem.getParentTeamWorkflow().getTeamDefinition();
      return getProgram(teamDef);
   }

   @Override
   public IAtsProgram getProgram(IAtsTeamDefinition teamDef) {
      IAtsProgram program = null;
      Long programUuid =
         services.getAttributeResolver().getSoleAttributeValue(teamDef, AtsAttributeTypes.ProgramUuid, null);
      if (programUuid != null) {
         program = services.getProgramService().getProgram(programUuid);
      }
      if (program == null) {
         IAtsTeamDefinition topTeamDef = teamDef.getTeamDefinitionHoldingVersions();
         if (!teamDef.equals(topTeamDef)) {
            program = getProgram(teamDef);
         }
      }
      if (program == null) {
         program = (IAtsProgram) services.getQueryService().createQuery(AtsArtifactTypes.Program).andAttr(
            AtsAttributeTypes.TeamDefinition, services.getStoreService().getGuid(teamDef)).getResults().getOneOrNull();
      }
      return program;
   }

   @Override
   public String getDescription(IAtsProgram program) {
      return services.getAttributeResolver().getSoleAttributeValue(program, AtsAttributeTypes.Description, "");
   }

   @Override
   public IAtsTeamDefinition getTeamDefHoldingVersions(IAtsProgram program) throws OseeCoreException {
      return getTeamDefinition(program);
   }

   @Override
   public IAtsTeamDefinition getTeamDefinition(IAtsProgram program) {
      IAtsTeamDefinition teamDefinition = null;
      String guid =
         services.getAttributeResolver().getSoleAttributeValue(program, AtsAttributeTypes.TeamDefinition, "");
      if (Strings.isValid(guid)) {
         Long uuid = services.getStoreService().getUuidFromGuid(guid);
         teamDefinition = services.getSoleByUuid(uuid, IAtsTeamDefinition.class);
      }
      return teamDefinition;
   }

   @Override
   public Collection<IAtsActionableItem> getAis(IAtsProgram program) {
      return Collections.castAll(services.getQueryService() //
      .createQuery(AtsArtifactTypes.ActionableItem) //
      .andAttr(AtsAttributeTypes.ProgramUuid, String.valueOf(program.getUuid())) //
      .getResults().getList());
   }

   @Override
   public Collection<IAtsTeamDefinition> getTeamDefs(IAtsProgram program) {
      return Collections.castAll(services.getQueryService() //
      .createQuery(AtsArtifactTypes.TeamDefinition) //
      .andAttr(AtsAttributeTypes.ProgramUuid, String.valueOf(program.getUuid())) //
      .getResults().getList());
   }

   @Override
   public Collection<IAtsProgram> getPrograms(IArtifactType artifactType) {
      return Collections.castAll(services.getQueryService() //
      .createQuery(artifactType).getResults().getList());
   }

   @Override
   public Collection<String> getCscis(IAtsProgram program) {
      return services.getAttributeResolver().getAttributesToStringList(program, AtsAttributeTypes.CSCI);
   }

   @Override
   public Collection<IAtsActionableItem> getAis(IAtsProgram program, WorkType workType) throws OseeCoreException {
      return getAis(program, java.util.Collections.singleton(workType));
   }

   @Override
   public Collection<IAtsActionableItem> getAis(IAtsProgram program, Collection<WorkType> workTypes) throws OseeCoreException {
      IAtsConfigQuery query = services.getQueryService() //
      .createQuery(AtsArtifactTypes.ActionableItem) //
      .andAttr(AtsAttributeTypes.ProgramUuid, String.valueOf(program.getUuid()));
      List<String> types = new LinkedList<>();
      for (WorkType type : workTypes) {
         types.add(type.name());
      }
      query.andAttr(AtsAttributeTypes.WorkType, types, QueryOption.EXACT_MATCH_OPTIONS);
      return Collections.castAll(query.getResults().getList());
   }

   @Override
   public Collection<IAtsTeamDefinition> getTeamDefs(IAtsProgram program, Collection<WorkType> workTypes) throws OseeCoreException {
      IAtsConfigQuery query = services.getQueryService() //
      .createQuery(AtsArtifactTypes.TeamDefinition) //
      .andAttr(AtsAttributeTypes.ProgramUuid, String.valueOf(program.getUuid()));
      List<String> types = new LinkedList<>();
      for (WorkType type : workTypes) {
         types.add(type.name());
      }
      query.andAttr(AtsAttributeTypes.WorkType, types, QueryOption.EXACT_MATCH_OPTIONS);
      return Collections.castAll(query.getResults().getList());
   }

   @Override
   public WorkType getWorkType(IAtsTeamWorkflow teamWf) {
      WorkType workType = WorkType.None;
      try {
         IAtsTeamDefinition teamDef = teamWf.getTeamDefinition();
         String typeStr =
            services.getAttributeResolver().getSoleAttributeValueAsString(teamDef, AtsAttributeTypes.WorkType, "");
         if (Strings.isValid(typeStr)) {
            workType = WorkType.valueOf(typeStr);
         }
      } catch (Exception ex) {
         workType = WorkType.Custom;
      }
      return workType;
   }

   @Override
   public ProjectType getProjectType(IAtsProgram program) {
      if (services.getAttributeResolver().getAttributeCount(program, AtsAttributeTypes.CSCI) > 1) {
         return ProjectType.MultiProcessor;
      } else {
         return ProjectType.SingleProcessor;
      }
   }

   @Override
   public String getNamespace(IAtsProgram program) {
      return services.getAttributeResolver().getSoleAttributeValue(program, AtsAttributeTypes.Namespace, "");
   }

   @Override
   public long getCountryUuid(IAtsProgram program) {
      long countryUuid = 0L;
      ArtifactId countryArt =
         services.getRelationResolver().getRelatedOrNull(program, AtsRelationTypes.CountryToProgram_Country);
      if (countryArt != null) {
         countryUuid = countryArt.getUuid();
      }
      return countryUuid;
   }

   @Override
   public Collection<IAtsTeamWorkflow> getWorkflows(IAtsProgram program, WorkType workType, IAtsWorkItem workItem) throws OseeCoreException {
      return getWorkflows(program, java.util.Collections.singleton(workType), workItem);
   }

   @Override
   public Collection<IAtsTeamWorkflow> getWorkflows(IAtsProgram program, Collection<WorkType> workTypes, IAtsWorkItem workItem) throws OseeCoreException {
      if (workTypes.contains(WorkType.All)) {
         throw new OseeArgumentException("Invalid option ALL for getWorkflow");
      }
      Set<IAtsTeamWorkflow> teamArts = new HashSet<>();
      Collection<IAtsTeamDefinition> workTypeTeamDefs = getTeamDefs(program, workTypes);
      if (workTypeTeamDefs.size() > 0) {

         IAtsObject useWorkItem = workItem;
         if (useWorkItem instanceof IAtsTask) {
            useWorkItem = ((IAtsTask) useWorkItem).getParentAction();
         }
         if (useWorkItem instanceof IAtsTeamWorkflow) {
            useWorkItem = ((IAtsTeamWorkflow) useWorkItem).getParentAction();
         }
         if (useWorkItem != null && useWorkItem instanceof IAtsAction) {
            for (IAtsTeamWorkflow team : services.getWorkItemService().getTeams((IAtsAction) useWorkItem)) {
               if (workTypeTeamDefs.contains(team.getTeamDefinition())) {
                  teamArts.add(team);
               }
            }
         }
      }
      return teamArts;
   }

   @Override
   public boolean isActive(IAtsProgram program) {
      return services.getAttributeResolver().getSoleAttributeValue(program, AtsAttributeTypes.Active, true);
   }

   @Override
   public Collection<IAtsTeamDefinition> getTeamDefs(IAtsProgram program, WorkType workType) {
      return getTeamDefs(program, java.util.Collections.singleton(workType));
   }

   @Override
   public Collection<IAtsTeamWorkflow> getWorkflows(IAtsProgram program, Collection<WorkType> workTypes) {
      Collection<IAtsTeamDefinition> teamDefs =
         services.getQueryService().createQuery(AtsArtifactTypes.TeamDefinition).andProgram(program).andWorkType(
            workTypes).getItems();
      return Collections.castAll(
         services.getQueryService().createQuery(WorkItemType.TeamWorkflow).andTeam(teamDefs).getResults().getList());
   }

   @Override
   public Collection<IAtsVersion> getVersions(IAtsProgram program) {
      return services.getProgramService().getTeamDefHoldingVersions(program).getVersions();
   }

   @Override
   public Collection<IAtsTeamWorkflow> getWorkflows(IAtsProgram program, WorkType workType) {
      return getWorkflows(program, java.util.Collections.singleton(workType));
   }

   @Override
   public IAtsVersion getVersion(IAtsProgram program, String versionName) {
      for (IAtsVersion version : getVersions(program)) {
         if (version.getName().equals(versionName)) {
            return version;
         }
      }
      return null;
   }

}
