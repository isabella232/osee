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
package org.eclipse.osee.ats.client.integration.tests.ats.agile;

import java.util.List;
import javax.ws.rs.core.Response;
import org.eclipse.osee.ats.api.agile.AgileEndpointApi;
import org.eclipse.osee.ats.api.agile.JaxAgileBacklog;
import org.eclipse.osee.ats.api.agile.JaxAgileFeatureGroup;
import org.eclipse.osee.ats.api.agile.JaxAgileSprint;
import org.eclipse.osee.ats.api.agile.JaxAgileTeam;
import org.eclipse.osee.ats.api.agile.JaxNewAgileBacklog;
import org.eclipse.osee.ats.api.agile.JaxNewAgileFeatureGroup;
import org.eclipse.osee.ats.api.agile.JaxNewAgileSprint;
import org.eclipse.osee.ats.api.agile.JaxNewAgileTeam;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test unit for AgileEndpointImpl
 *
 * @author Donald G. Dunne
 */
public class AgileEndpointTest {

   private AgileEndpointApi agile;
   private long teamUuid;

   @Before
   public void setup() {
      agile = AtsClientService.getAgile();
      teamUuid = Lib.generateArtifactIdAsInt();
   }

   @After
   public void cleanup() {
      Artifact agileTeam = AtsClientService.get().getArtifact(teamUuid);
      if (agileTeam != null) {
         agile.deleteTeam(teamUuid);
      }
   }

   @Test
   public void testTeamCRUD() {
      // Test Create
      JaxNewAgileTeam newTeam = newJaxAgileTeam();
      Response response = agile.createTeam(newTeam);
      Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

      // Test Get
      JaxAgileTeam team = agile.getTeam(teamUuid);
      Assert.assertNotNull(team);
      Assert.assertEquals("My Agile Team", team.getName());
      Assert.assertEquals(teamUuid, team.getUuid());
      Assert.assertEquals("", team.getDescription());
      Assert.assertEquals(true, team.isActive());

      // Test Update
      team.setDescription("description");
      team.setActive(false);
      team.setName("New Name");
      agile.updateTeam(team);
      JaxAgileTeam updatedTeam = agile.getTeam(teamUuid);
      Assert.assertNotNull(updatedTeam);
      Assert.assertEquals("New Name", updatedTeam.getName());
      Assert.assertEquals("description", updatedTeam.getDescription());
      Assert.assertEquals(false, updatedTeam.isActive());

      // Test Delete
      agile.deleteTeam(teamUuid);
      Assert.assertNull(AtsClientService.get().getArtifact(teamUuid));
   }

   private JaxNewAgileTeam newJaxAgileTeam() {
      JaxNewAgileTeam newTeam = new JaxNewAgileTeam();
      newTeam.setName("My Agile Team");
      newTeam.setUuid(teamUuid);
      return newTeam;
   }

   @Test
   public void testSprintCRUD() {
      JaxNewAgileTeam newTeam = newJaxAgileTeam();
      Response response = agile.createTeam(newTeam);
      Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

      // Test Create
      JaxNewAgileSprint newSprint = new JaxNewAgileSprint();
      newSprint.setName("My Sprint");
      Long uuid = Lib.generateArtifactIdAsInt();
      newSprint.setUuid(uuid);
      newSprint.setTeamUuid(teamUuid);
      Response response2 = agile.createSprint(teamUuid, newSprint);
      Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response2.getStatus());

      // Test Get
      List<JaxAgileSprint> sprints = agile.getSprints(teamUuid);
      Assert.assertEquals(1, sprints.size());
      JaxAgileSprint sprint = sprints.iterator().next();
      Assert.assertEquals("My Sprint", sprint.getName());
      Assert.assertEquals(teamUuid, sprint.getTeamUuid());
      Assert.assertEquals(uuid.longValue(), sprint.getUuid());

      // Test Delete
      agile.deleteSprint(teamUuid, sprint.getUuid());
      sprints = agile.getSprints(teamUuid);
      Assert.assertNull(AtsClientService.get().getArtifact(sprint.getUuid()));
   }

   @Test
   public void testFeatureGroupCRUD() {
      JaxNewAgileTeam newTeam = newJaxAgileTeam();
      Response response = agile.createTeam(newTeam);
      Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

      // Test Create
      JaxNewAgileFeatureGroup group = new JaxNewAgileFeatureGroup();
      group.setName("Communications");
      group.setTeamUuid(teamUuid);
      Long uuid = Lib.generateArtifactIdAsInt();
      group.setUuid(uuid);

      Response response2 = agile.createFeatureGroup(teamUuid, group);
      Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response2.getStatus());

      // Test Get
      List<JaxAgileFeatureGroup> groups = agile.getFeatureGroups(teamUuid);
      Assert.assertEquals(1, groups.size());
      JaxAgileFeatureGroup newGroup = groups.iterator().next();
      Assert.assertEquals("Communications", newGroup.getName());
      Assert.assertEquals(teamUuid, newGroup.getTeamUuid());
      Assert.assertEquals(uuid.longValue(), newGroup.getUuid());

      // Test Delete
      agile.deleteFeatureGroup(teamUuid, newGroup.getUuid());
      groups = agile.getFeatureGroups(teamUuid);
      Assert.assertTrue(groups.isEmpty());
      Assert.assertNull(AtsClientService.get().getArtifact(newGroup.getUuid()));
   }

   @Test
   public void testBacklogCR() {
      JaxNewAgileTeam newTeam = newJaxAgileTeam();
      Response response = agile.createTeam(newTeam);
      Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

      JaxNewAgileBacklog backlog = new JaxNewAgileBacklog();
      backlog.setName("My Backlog");
      backlog.setTeamUuid(teamUuid);
      Long uuid = Lib.generateArtifactIdAsInt();
      backlog.setUuid(uuid);

      // Test Create
      Response response2 = agile.createBacklog(teamUuid, backlog);
      Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response2.getStatus());

      // Test Get
      JaxAgileBacklog newBacklog = agile.getBacklog(teamUuid);
      Assert.assertEquals("My Backlog", newBacklog.getName());
      Assert.assertEquals(teamUuid, newBacklog.getTeamUuid());
      Assert.assertTrue(newBacklog.isActive());
      Assert.assertEquals(uuid.longValue(), newBacklog.getUuid());
   }

}
