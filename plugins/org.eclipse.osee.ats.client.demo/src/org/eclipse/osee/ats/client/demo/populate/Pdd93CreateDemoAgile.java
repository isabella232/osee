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
package org.eclipse.osee.ats.client.demo.populate;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.core.Response;
import org.eclipse.core.runtime.Assert;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.agile.AgileEndpointApi;
import org.eclipse.osee.ats.api.agile.AgileWriterResult;
import org.eclipse.osee.ats.api.agile.IAgileProgram;
import org.eclipse.osee.ats.api.agile.IAgileProgramBacklog;
import org.eclipse.osee.ats.api.agile.IAgileProgramBacklogItem;
import org.eclipse.osee.ats.api.agile.IAgileProgramFeature;
import org.eclipse.osee.ats.api.agile.IAgileSprint;
import org.eclipse.osee.ats.api.agile.IAgileStory;
import org.eclipse.osee.ats.api.agile.JaxAgileItem;
import org.eclipse.osee.ats.api.agile.JaxAgileProgram;
import org.eclipse.osee.ats.api.agile.JaxAgileProgramBacklog;
import org.eclipse.osee.ats.api.agile.JaxAgileProgramBacklogItem;
import org.eclipse.osee.ats.api.agile.JaxAgileProgramFeature;
import org.eclipse.osee.ats.api.agile.JaxAgileStory;
import org.eclipse.osee.ats.api.agile.JaxNewAgileBacklog;
import org.eclipse.osee.ats.api.agile.JaxNewAgileFeatureGroup;
import org.eclipse.osee.ats.api.agile.JaxNewAgileProgramFeature;
import org.eclipse.osee.ats.api.agile.JaxNewAgileSprint;
import org.eclipse.osee.ats.api.agile.JaxNewAgileTeam;
import org.eclipse.osee.ats.api.config.JaxAtsObject;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.WorkItemType;
import org.eclipse.osee.ats.api.workflow.transition.IAtsTransitionManager;
import org.eclipse.osee.ats.api.workflow.transition.TransitionOption;
import org.eclipse.osee.ats.api.workflow.transition.TransitionResults;
import org.eclipse.osee.ats.client.demo.SprintItemData;
import org.eclipse.osee.ats.client.demo.internal.AtsClientService;
import org.eclipse.osee.ats.core.workflow.state.TeamState;
import org.eclipse.osee.ats.core.workflow.transition.TransitionFactory;
import org.eclipse.osee.ats.core.workflow.transition.TransitionHelper;
import org.eclipse.osee.ats.demo.api.DemoArtifactToken;
import org.eclipse.osee.ats.demo.api.DemoArtifactTypes;
import org.eclipse.osee.ats.demo.api.DemoWorkflowTitles;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.DemoUsers;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.enums.RelationSorter;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.SevereLoggingMonitor;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.support.test.util.TestUtil;

/**
 * @author Donald G. Dunne
 */
public class Pdd93CreateDemoAgile {

   private static void validateArtifactCache() {
      final Collection<Artifact> list = ArtifactCache.getDirtyArtifacts();
      if (!list.isEmpty()) {
         for (Artifact artifact : list) {
            System.err.println(String.format("Artifact [%s] is dirty [%s]", artifact.toStringWithId(),
               Artifacts.getDirtyReport(artifact)));
         }
         throw new OseeStateException("[%d] Dirty Artifacts found after populate (see console for details)",
            list.size());
      }

   }

   public void run() throws Exception {
      validateArtifactCache();

      AtsClientService.get().reloadServerAndClientCaches();

      SevereLoggingMonitor monitorLog = TestUtil.severeLoggingStart();

      // create agile program
      IAgileProgram aProgram = createAgileProgram();
      // create two agile teams and add to program
      createSawAgileTeam(aProgram);
      createCisAgileTeam(aProgram);
      createProgramBacklogAndFeaturesAndStories(aProgram);

      createAgileStandAloneTeam();

      TestUtil.severeLoggingEnd(monitorLog);
   }

   private void createProgramBacklogAndFeaturesAndStories(IAgileProgram aProgram) {

      JaxAgileProgramBacklog jaxProgramBacklog =
         JaxAgileProgramBacklog.construct(aProgram, DemoArtifactToken.RD_Program_Backlog);
      IAgileProgramBacklog programBacklog =
         AtsClientService.get().getAgileService().createAgileProgramBacklog(aProgram, jaxProgramBacklog);

      JaxAgileProgramBacklogItem backlogItem1 =
         JaxAgileProgramBacklogItem.construct(programBacklog, DemoArtifactToken.RD_Program_Backlog_Item_1);
      IAgileProgramBacklogItem item =
         AtsClientService.get().getAgileService().createAgileProgramBacklogItem(programBacklog, backlogItem1);

      JaxAgileProgramBacklogItem item2 =
         JaxAgileProgramBacklogItem.construct(programBacklog, DemoArtifactToken.RD_Program_Backlog_Item_2);
      AtsClientService.get().getAgileService().createAgileProgramBacklogItem(programBacklog, item2);
      JaxAgileProgramBacklogItem item3 =
         JaxAgileProgramBacklogItem.construct(programBacklog, DemoArtifactToken.RD_Program_Backlog_Item_3);
      AtsClientService.get().getAgileService().createAgileProgramBacklogItem(programBacklog, item3);

      JaxAgileProgramFeature jaxFeature =
         JaxAgileProgramFeature.construct(backlogItem1, DemoArtifactToken.RD_Program_Feature_Robot_Nav);
      IAgileProgramFeature feature =
         AtsClientService.get().getAgileService().createAgileProgramFeature(item, jaxFeature);

      JaxAgileStory jaxStory1 = JaxAgileStory.construct(feature, DemoArtifactToken.RD_Robot_Nav_Story_1);
      IAgileStory story1 = AtsClientService.get().getAgileService().createAgileStory(feature, jaxStory1);

      JaxAgileStory jaxStory2 = JaxAgileStory.construct(feature, DemoArtifactToken.RD_Robot_Nav_Story_2);
      IAgileStory story2 = AtsClientService.get().getAgileService().createAgileStory(feature, jaxStory2);

      IAtsChangeSet changes = AtsClientService.get().createChangeSet("Add Agile Items to Stories");
      IAtsTeamWorkflow codeWf = AtsClientService.get().getTeamWf(DemoArtifactToken.SAW_Commited_Code_TeamWf);
      AtsClientService.get().getAgileService().setAgileStory(codeWf, story1, changes);
      IAtsTeamWorkflow testWf = AtsClientService.get().getTeamWf(DemoArtifactToken.SAW_Commited_Test_TeamWf);
      AtsClientService.get().getAgileService().setAgileStory(testWf, story1, changes);
      IAtsTeamWorkflow reqWf = AtsClientService.get().getTeamWf(DemoArtifactToken.SAW_Commited_Req_TeamWf);
      AtsClientService.get().getAgileService().setAgileStory(reqWf, story1, changes);

      IAtsTeamWorkflow codeWf2 = AtsClientService.get().getTeamWf(DemoArtifactToken.SAW_UnCommited_Code_TeamWf);
      AtsClientService.get().getAgileService().setAgileStory(codeWf2, story2, changes);
      IAtsTeamWorkflow testWf2 = AtsClientService.get().getTeamWf(DemoArtifactToken.SAW_UnCommited_Test_TeamWf);
      AtsClientService.get().getAgileService().setAgileStory(testWf2, story2, changes);
      IAtsTeamWorkflow reqWf2 = AtsClientService.get().getTeamWf(DemoArtifactToken.SAW_UnCommited_Req_TeamWf);
      AtsClientService.get().getAgileService().setAgileStory(reqWf2, story2, changes);
      changes.execute();

      Artifact progArt = AtsClientService.get().getArtifact(aProgram.getId());
      RelationManager.setRelationOrder(progArt, CoreRelationTypes.Default_Hierarchical__Child, RelationSide.SIDE_B,
         RelationSorter.UNORDERED, progArt.getChildren());

      jaxFeature = JaxAgileProgramFeature.construct(backlogItem1, DemoArtifactToken.RD_Program_Feature_Robot_Voice);
      AtsClientService.get().getAgileService().createAgileProgramFeature(item, jaxFeature);
   }

   private void createAgileStandAloneTeam() {
      long teamId = 999L;

      // Create Facilities Team
      JaxNewAgileTeam newTeam = new JaxNewAgileTeam();
      newTeam.setName("Facilities Team");
      newTeam.setId(teamId);
      Response response = AtsClientService.getAgile().createTeam(newTeam);
      Assert.isTrue(Response.Status.CREATED.getStatusCode() == response.getStatus());

      // Create Backlog
      JaxNewAgileBacklog backlog = new JaxNewAgileBacklog();
      backlog.setName("Facilities Backlog");
      backlog.setId(9991L);
      backlog.setTeamId(newTeam.getId());
      response = AtsClientService.getAgile().createBacklog(teamId, backlog);
      Assert.isTrue(Response.Status.CREATED.getStatusCode() == response.getStatus());
   }

   private void createCisAgileTeam(IAgileProgram aProgram) {
      // Create CIS Team
      JaxNewAgileTeam newTeam = new JaxNewAgileTeam();
      newTeam.setName(DemoArtifactToken.CIS_Agile_Team.getName());
      newTeam.setId(DemoArtifactToken.CIS_Agile_Team.getId());
      newTeam.setProgramId(aProgram.getId().toString());
      Response response = AtsClientService.getAgile().createTeam(newTeam);
      Assert.isTrue(Response.Status.CREATED.getStatusCode() == response.getStatus());

      IAtsChangeSet changes = AtsClientService.get().createChangeSet("Config Agile Team with points attr type");
      Artifact sawAgileTeam = AtsClientService.get().getArtifact(DemoArtifactToken.CIS_Agile_Team.getId());
      changes.setSoleAttributeValue(sawAgileTeam, AtsAttributeTypes.PointsAttributeType,
         AtsAttributeTypes.Points.getName());
      changes.execute();

      // Create Backlog
      JaxNewAgileBacklog backlog = new JaxNewAgileBacklog();
      backlog.setName(DemoArtifactToken.CIS_Backlog.getName());
      backlog.setId(DemoArtifactToken.CIS_Backlog.getId());
      backlog.setTeamId(newTeam.getId());
      response = AtsClientService.getAgile().createBacklog(DemoArtifactToken.CIS_Agile_Team.getId(), backlog);
      Assert.isTrue(Response.Status.CREATED.getStatusCode() == response.getStatus());
   }

   private IAgileProgram createAgileProgram() {
      JaxAgileProgram jProgram = new JaxAgileProgram();
      jProgram.setName(DemoArtifactToken.RD_Agile_Program.getName());
      jProgram.setId(DemoArtifactToken.RD_Agile_Program.getId());
      IAgileProgram aProgram = AtsClientService.get().getAgileService().createAgileProgram(jProgram);
      return aProgram;
   }

   private void createSawAgileTeam(IAgileProgram aProgram) {
      AgileEndpointApi agile = AtsClientService.getAgile();

      // Create Team
      JaxNewAgileTeam newTeam = getJaxAgileTeam();
      newTeam.setProgramId(aProgram.getId().toString());
      Response response = agile.createTeam(newTeam);
      Assert.isTrue(Response.Status.CREATED.getStatusCode() == response.getStatus());

      IAtsChangeSet changes = AtsClientService.get().createChangeSet("Config Agile Team with points attr type");
      Artifact sawAgileTeam = AtsClientService.get().getArtifact(DemoArtifactToken.SAW_Agile_Team);
      changes.setSoleAttributeValue(sawAgileTeam, AtsAttributeTypes.PointsAttributeType,
         AtsAttributeTypes.Points.getName());
      changes.execute();

      // Assigne ATS Team to Agile Team
      Artifact sawCodeArt = AtsClientService.get().getArtifact(DemoArtifactToken.SAW_Code);
      Conditions.assertNotNull(sawCodeArt, "sawCodeArt");
      Artifact agileTeam = AtsClientService.get().getArtifact(newTeam.getId());
      agileTeam.addRelation(AtsRelationTypes.AgileTeamToAtsTeam_AtsTeam, sawCodeArt);
      agileTeam.persist("Assigne ATS Team to Agile Team");

      // Add team members to agile team
      Artifact joeUser = AtsClientService.get().getArtifact(DemoUsers.Joe_Smith);
      agileTeam.addRelation(CoreRelationTypes.Users_User, joeUser);
      Artifact kayUser = AtsClientService.get().getArtifact(DemoUsers.Kay_Jones);
      agileTeam.addRelation(CoreRelationTypes.Users_User, kayUser);
      agileTeam.persist("Add Team Members to Agile Team");

      // Create Backlog
      JaxNewAgileBacklog backlog = getBacklog();
      response = agile.createBacklog(DemoArtifactToken.SAW_Agile_Team.getId(), backlog);
      Assert.isTrue(Response.Status.CREATED.getStatusCode() == response.getStatus());

      // Add items to backlog
      Collection<IAtsWorkItem> items =
         AtsClientService.get().getQueryService().createQuery(WorkItemType.TeamWorkflow).isOfType(
            DemoArtifactTypes.DemoCodeTeamWorkflow, DemoArtifactTypes.DemoReqTeamWorkflow,
            DemoArtifactTypes.DemoTestTeamWorkflow).getItems();
      Assert.isTrue(items.size() > 0);

      JaxAgileItem item = new JaxAgileItem();
      item.setBacklogId(backlog.getId());
      item.setSetBacklog(true);
      for (IAtsWorkItem workItem : items) {
         item.getIds().add(workItem.getId());
      }
      AgileWriterResult result = agile.updateItems(item);
      Conditions.assertFalse(result.getResults().isErrors(), result.getResults().toString());

      // Set backlog as user_defined member order
      Artifact backlogArt = AtsClientService.get().getArtifact(backlog.getId());
      RelationManager.setRelationOrder(backlogArt, AtsRelationTypes.Goal_Member, RelationSide.SIDE_B,
         RelationSorter.USER_DEFINED, backlogArt.getRelatedArtifacts(AtsRelationTypes.Goal_Member));

      // Create Sprints
      JaxNewAgileSprint sprint1 = newSprint(DemoArtifactToken.SAW_Sprint_1);
      response = agile.createSprint(sprint1.getTeamId(), sprint1);
      Assert.isTrue(Response.Status.CREATED.getStatusCode() == response.getStatus());

      JaxNewAgileSprint sprint2 = newSprint(DemoArtifactToken.SAW_Sprint_2);
      response = agile.createSprint(sprint2.getTeamId(), sprint2);
      Assert.isTrue(Response.Status.CREATED.getStatusCode() == response.getStatus());

      // Add items to Sprint
      JaxAgileItem completedItems = new JaxAgileItem();
      completedItems.setSprintId(DemoArtifactToken.SAW_Sprint_1.getId());
      completedItems.setSetSprint(true);

      JaxAgileItem inworkItems = new JaxAgileItem();
      inworkItems.setSprintId(DemoArtifactToken.SAW_Sprint_2.getId());
      inworkItems.setSetSprint(true);

      for (IAtsWorkItem workItem : items) {
         if (workItem.getStateMgr().getStateType().isCompleted()) {
            completedItems.getIds().add(workItem.getId());
         } else {
            inworkItems.getIds().add(workItem.getId());
         }
      }
      result = agile.updateItems(inworkItems);
      Conditions.assertFalse(result.getResults().isErrors(), result.getResults().toString());
      result = agile.updateItems(completedItems);
      Conditions.assertFalse(result.getResults().isErrors(), result.getResults().toString());

      Artifact sprint1Art = AtsClientService.get().getArtifact(sprint1.getId());
      RelationManager.setRelationOrder(sprint1Art, AtsRelationTypes.AgileSprintToItem_AtsItem, RelationSide.SIDE_B,
         RelationSorter.USER_DEFINED, sprint1Art.getRelatedArtifacts(AtsRelationTypes.AgileSprintToItem_AtsItem));
      sprint1Art.persist("Set sort order for Sprint 1");

      Artifact sprint2Art = AtsClientService.get().getArtifact(sprint2.getId());
      RelationManager.setRelationOrder(sprint2Art, AtsRelationTypes.AgileSprintToItem_AtsItem, RelationSide.SIDE_B,
         RelationSorter.USER_DEFINED, sprint2Art.getRelatedArtifacts(AtsRelationTypes.AgileSprintToItem_AtsItem));
      sprint2Art.persist("Set sort order for Sprint 2");

      // Transition First Sprint to completed
      IAtsWorkItem sprint = AtsClientService.get().getQueryService().createQuery(WorkItemType.WorkItem).andIds(
         DemoArtifactToken.SAW_Sprint_1.getId()).getItems().iterator().next();
      changes.reset("Transition Agile Sprint");
      TransitionHelper helper =
         new TransitionHelper("Transition Agile Stprint", Arrays.asList(sprint), TeamState.Completed.getName(), null,
            null, changes, AtsClientService.get().getServices(), TransitionOption.OverrideAssigneeCheck);
      IAtsTransitionManager transitionMgr = TransitionFactory.getTransitionManager(helper);
      TransitionResults results = transitionMgr.handleAll();

      /**
       * Setup Agile Team Story Names (this maps an assignee name to a story name for Agile Teams using stories instead
       * of assignees in kanban
       */
      changes.addAttribute(agileTeam, AtsAttributeTypes.KanbanStoryName,
         DemoUsers.Jason_Michael.getName() + ":Jason Rockstar Michael");

      if (results.isEmpty()) {
         changes.execute();
      } else {
         throw new OseeStateException("Can't transition sprint to completed [%s]", results.toString());
      }

      // Create Feature Groups
      for (String name : Arrays.asList("Communications", "UI", "Documentation", "Framework")) {
         JaxNewAgileFeatureGroup featureGroup = newFeatureGroup(name);
         response = agile.createFeatureGroup(DemoArtifactToken.SAW_Program.getId(), featureGroup);
         Assert.isTrue(Response.Status.CREATED.getStatusCode() == response.getStatus());
      }

      setupSprint2ForBurndown(DemoArtifactToken.SAW_Sprint_2.getId());
   }

   private void setupSprint2ForBurndown(long secondSprintId) {

      // Transition First Sprint to completed
      IAtsWorkItem sprint = AtsClientService.get().getQueryService().createQuery(WorkItemType.WorkItem).andIds(
         secondSprintId).getItems().iterator().next();
      IAtsChangeSet changes = AtsClientService.get().createChangeSet("Setup Sprint 2 for Burndown");

      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DAY_OF_YEAR, -5);
      Date holiday1 = null, holiday2 = null;
      // backup start date till hit weekday
      while (!DateUtil.isWeekDay(cal)) {
         cal.add(Calendar.DAY_OF_YEAR, -1);
      }
      changes.setSoleAttributeValue(sprint, AtsAttributeTypes.StartDate, cal.getTime());
      int x = 1;
      int holidayDayNum = 5;
      // count up 20 weekdays and set 2 weekday holidays
      for (x = 1; x <= 21; x++) {
         cal.add(Calendar.DAY_OF_YEAR, 1);
         while (!DateUtil.isWeekDay(cal)) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
         }
         if (x == holidayDayNum) {
            holiday1 = cal.getTime();
         } else if (x == holidayDayNum + 1) {
            holiday2 = cal.getTime();
         }
      }
      changes.setSoleAttributeValue(sprint, AtsAttributeTypes.EndDate, cal.getTime());
      changes.setSoleAttributeValue(sprint, AtsAttributeTypes.UnPlannedPoints, 45);
      changes.setSoleAttributeValue(sprint, AtsAttributeTypes.PlannedPoints, 200);
      changes.addAttribute(sprint, AtsAttributeTypes.Holiday, holiday1);
      changes.addAttribute(sprint, AtsAttributeTypes.Holiday, holiday2);
      // set sprint data on sprint items
      Artifact agileTeamArt =
         ((Artifact) sprint.getStoreObject()).getRelatedArtifact(AtsRelationTypes.AgileTeamToSprint_AgileTeam);
      changes.execute();

      setSprintItemData(agileTeamArt.getId(), (IAgileSprint) sprint);
   }

   private void setSprintItemData(Long teamId, IAgileSprint sprint) {
      List<SprintItemData> datas = new LinkedList<>();
      datas.add(
         new SprintItemData("Sprint Order", "Title", "Points", "Unplanned Work", "Feature Group", "CreatedDate"));
      datas.add(
         new SprintItemData("1", "Button W doesn't work on Situation Page", "8", " ", "Communications", "10/03/2016"));
      datas.add(new SprintItemData("2", "Can't load Diagram Tree", "4", "Unplanned Work", "Framework", "10/03/2016"));
      datas.add(new SprintItemData("3", "Can't see the Graph View", "8", "Unplanned Work", "Framework", "10/03/2016"));
      datas.add(new SprintItemData("4", "Problem in Diagram Tree", "40", " ", "Framework", "10/03/2016"));
      datas.add(new SprintItemData("5", "Problem with the Graph View", "8", " ", "Communications", "10/03/2016"));
      datas.add(new SprintItemData("6", DemoWorkflowTitles.SAW_COMMITTED_REQT_CHANGES_FOR_DIAGRAM_VIEW, "2",
         "Unplanned Work", "Framework", "10/03/2016"));
      datas.add(new SprintItemData("7", DemoWorkflowTitles.SAW_COMMITTED_REQT_CHANGES_FOR_DIAGRAM_VIEW, "8", " ",
         "Framework", "10/03/2016"));
      datas.add(new SprintItemData("8", DemoWorkflowTitles.SAW_COMMITTED_REQT_CHANGES_FOR_DIAGRAM_VIEW, "16", " ", "UI",
         "10/03/2016"));
      datas.add(new SprintItemData("9", DemoWorkflowTitles.SAW_NO_BRANCH_REQT_CHANGES_FOR_DIAGRAM_VIEW, "32", " ",
         "Communications", "10/03/2016"));
      datas.add(new SprintItemData("10", DemoWorkflowTitles.SAW_NO_BRANCH_REQT_CHANGES_FOR_DIAGRAM_VIEW, "40", " ",
         "Documentation", "10/03/2016"));
      datas.add(new SprintItemData("11", DemoWorkflowTitles.SAW_NO_BRANCH_REQT_CHANGES_FOR_DIAGRAM_VIEW, "8", " ",
         "Documentation", "10/03/2016"));
      datas.add(new SprintItemData("12", DemoWorkflowTitles.SAW_UNCOMMITTED_REQT_CHANGES_FOR_DIAGRAM_VIEW, "1", " ",
         "Communications", "10/03/2016"));
      datas.add(new SprintItemData("13", DemoWorkflowTitles.SAW_UNCOMMITTED_REQT_CHANGES_FOR_DIAGRAM_VIEW, "6", " ",
         "Documentation", "10/03/2016"));
      datas.add(new SprintItemData("14", DemoWorkflowTitles.SAW_UNCOMMITTED_REQT_CHANGES_FOR_DIAGRAM_VIEW, "32", " ",
         "Communications", "10/03/2016"));
      datas.add(new SprintItemData("15", DemoArtifactToken.SAW_UnCommitedConflicted_Req_TeamWf.getName(), "1", " ",
         "Communications", "10/03/2016"));
      datas.add(new SprintItemData("16", "Workaround for Graph View for SAW_Bld_2", "1", "Unplanned Work",
         "Communications", "10/03/2016"));
      datas.add(new SprintItemData("17", "Workaround for Graph View for SAW_Bld_3", "2", "Unplanned Work",
         "Communications", "10/03/2016"));

      int x = 1;
      for (JaxAtsObject jaxWorkItem : AtsClientService.getAgile().getSprintItemsAsJax(teamId,
         sprint.getId()).getAtsObjects()) {
         SprintItemData data = getSprintData(datas, x++, jaxWorkItem);
         String featureGroupName = data.getFeature();
         if (Strings.isValid(featureGroupName)) {
            AtsClientService.getAgile().addFeatureGroup(jaxWorkItem.getId(), featureGroupName);
         }
         String unPlannedStr = data.getUnPlanned();
         boolean unPlanned = false;
         if (Strings.isValid(unPlannedStr)) {
            if (unPlannedStr.toLowerCase().contains("un")) {
               unPlanned = true;
            }
         }
         AtsClientService.getAgile().setUnPlanned(jaxWorkItem.getId(), unPlanned);
         String points = data.getPoints();
         if (Strings.isValid(points)) {
            AtsClientService.getAgile().setPoints(jaxWorkItem.getId(), points);
         }
      }
   }

   private SprintItemData getSprintData(List<SprintItemData> datas, int i, JaxAtsObject workItem) {
      for (SprintItemData data : datas) {
         if (data.getOrder().equals(String.valueOf(i)) && data.getTitle().equals(workItem.getName())) {
            return data;
         }
      }
      return null;
   }

   private JaxNewAgileBacklog getBacklog() {
      JaxNewAgileBacklog backlog = new JaxNewAgileBacklog();
      backlog.setName(DemoArtifactToken.SAW_Backlog.getName());
      backlog.setId(DemoArtifactToken.SAW_Backlog.getId());
      backlog.setTeamId(DemoArtifactToken.SAW_Agile_Team.getId());
      return backlog;
   }

   private JaxNewAgileProgramFeature newProgramFeature(String name) {
      JaxNewAgileProgramFeature programFeature = new JaxNewAgileProgramFeature();
      programFeature.setName(name);
      programFeature.setProgramBacklogItemId(DemoArtifactToken.SAW_Agile_Team.getId());
      programFeature.setId(Lib.generateArtifactIdAsInt());
      return programFeature;
   }

   private JaxNewAgileFeatureGroup newFeatureGroup(String name) {
      JaxNewAgileFeatureGroup group = new JaxNewAgileFeatureGroup();
      group.setName(name);
      group.setTeamId(DemoArtifactToken.SAW_Agile_Team.getId());
      group.setId(Lib.generateArtifactIdAsInt());
      return group;
   }

   private JaxNewAgileSprint newSprint(ArtifactToken token) {
      JaxNewAgileSprint newSprint = new JaxNewAgileSprint();
      newSprint.setName(token.getName());
      newSprint.setId(token.getId());
      newSprint.setTeamId(DemoArtifactToken.SAW_Agile_Team.getId());
      return newSprint;
   }

   private JaxNewAgileTeam getJaxAgileTeam() {
      JaxNewAgileTeam newTeam = new JaxNewAgileTeam();
      newTeam.setName(DemoArtifactToken.SAW_Agile_Team.getName());
      newTeam.setId(DemoArtifactToken.SAW_Agile_Team.getId());
      return newTeam;
   }

}