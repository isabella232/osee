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
package org.eclipse.osee.ats.client.integration.tests.ats.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.Response;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.task.AtsTaskEndpointApi;
import org.eclipse.osee.ats.api.task.JaxAtsTask;
import org.eclipse.osee.ats.api.task.JaxAttribute;
import org.eclipse.osee.ats.api.task.NewTaskData;
import org.eclipse.osee.ats.client.demo.DemoUsers;
import org.eclipse.osee.ats.client.demo.DemoUtil;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.ats.core.users.AtsCoreUsers;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test unit for AtsTaskEndpointImpl
 *
 * @author Donald G. Dunne
 */
public class AtsTaskEndpointImplTest {

   private AtsTaskEndpointApi taskEp;
   private long taskUuid1, taskUuid2, taskUuid3, codeTeamWfUuid;

   @Before
   public void setup() {
      taskEp = AtsClientService.getTaskEp();
      taskUuid1 = Lib.generateArtifactIdAsInt();
      taskUuid2 = Lib.generateArtifactIdAsInt();
      taskUuid3 = Lib.generateArtifactIdAsInt();
      codeTeamWfUuid = DemoUtil.getSawCodeUnCommittedWf().getUuid();
   }

   @After
   public void cleanup() {
      taskEp.delete(taskUuid1);
      taskEp.delete(taskUuid2);
      taskEp.delete(taskUuid3);
   }

   @Test
   public void testTaskCRD() {
      // Test Create
      NewTaskData data = new NewTaskData();
      String createdByUserId = DemoUsers.Joe_Smith.getUserId();
      data.setAsUserId(createdByUserId);
      Date createdDate = new Date();
      data.setCommitComment("Create Tasks via - " + getClass().getSimpleName());
      data.setTeamWfUuid(codeTeamWfUuid);

      JaxAtsTask task = createJaxAtsTask(taskUuid1, "Task 1", "description", createdByUserId, createdDate, null);
      task.setTaskWorkDef("WorkDef_Task_Default");
      data.getNewTasks().add(task);

      JaxAtsTask task2 = createJaxAtsTask(taskUuid2, "Task 2", "description", createdByUserId, createdDate, null);
      data.getNewTasks().add(task2);

      JaxAtsTask task3 = createJaxAtsTask(taskUuid3, "Task 3", null, createdByUserId, createdDate,
         Arrays.asList(DemoUsers.Alex_Kay.getUserId()));
      task3.addAttribute(CoreAttributeTypes.StaticId.getName(), "my static id");
      data.getNewTasks().add(task3);

      Response response = taskEp.create(data);
      Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

      // Test Get
      JaxAtsTask task1R = taskEp.get(taskUuid1).readEntity(JaxAtsTask.class);
      Assert.assertNotNull(task1R);
      Assert.assertEquals("Task 1", task1R.getName());
      Assert.assertEquals(taskUuid1, task1R.getUuid());
      Assert.assertEquals(createdByUserId, task1R.getCreatedByUserId());
      Assert.assertEquals("description", task1R.getDescription());
      Assert.assertEquals(true, task1R.isActive());
      String createdDateStr = DateUtil.getDateNow(createdDate, DateUtil.MMDDYYHHMM);
      String taskDateStr = DateUtil.getDateNow(task1R.getCreatedDate(), DateUtil.MMDDYYHHMM);
      Assert.assertEquals(createdDateStr, taskDateStr);
      Assert.assertEquals(1, task1R.getAssigneeUserIds().size());
      Assert.assertEquals(AtsCoreUsers.UNASSIGNED_USER.getUserId(), task1R.getAssigneeUserIds().iterator().next());
      List<JaxAttribute> attributes = task1R.getAttributes();
      // Work Definition should be set
      boolean found = false;
      for (JaxAttribute attr : attributes) {
         if (attr.getAttrTypeName().equals(AtsAttributeTypes.WorkflowDefinition.getName())) {
            found = true;
            Assert.assertEquals("Expected Attribute WorkDefintiion WorkDef_Task_Default", "WorkDef_Task_Default",
               attr.getValues().iterator().next());
         }
      }
      if (!found) {
         Assert.fail("Attribute WorkDefintiion wasn't found");
      }

      JaxAtsTask task2R = taskEp.get(taskUuid2).readEntity(JaxAtsTask.class);
      Assert.assertNotNull(task2R);
      Assert.assertEquals("Task 2", task2R.getName());
      Assert.assertEquals(taskUuid2, task2R.getUuid());
      Assert.assertEquals(createdByUserId, task2R.getCreatedByUserId());
      Assert.assertEquals("description", task2R.getDescription());
      Assert.assertEquals(true, task2R.isActive());
      Assert.assertEquals(1, task2R.getAssigneeUserIds().size());
      Assert.assertEquals(AtsCoreUsers.UNASSIGNED_USER.getUserId(), task2R.getAssigneeUserIds().iterator().next());
      // Work Definition attribute should NOT be set
      attributes = task2R.getAttributes();
      found = false;
      for (JaxAttribute attr : attributes) {
         if (attr.getAttrTypeName().equals(AtsAttributeTypes.WorkflowDefinition.getName())) {
            Assert.fail(
               String.format("WorkDefintiion should not be set but is [%s]", attr.getValues().iterator().next()));
         }
      }

      JaxAtsTask task3R = taskEp.get(taskUuid3).readEntity(JaxAtsTask.class);
      Assert.assertNotNull(task3R);
      Assert.assertEquals("Task 3", task3R.getName());
      Assert.assertEquals(taskUuid3, task3R.getUuid());
      Assert.assertEquals(createdByUserId, task2R.getCreatedByUserId());
      Assert.assertEquals("", task3R.getDescription());
      Assert.assertEquals(true, task3R.isActive());
      Assert.assertEquals(1, task3R.getAssigneeUserIds().size());
      Assert.assertEquals(9, task3R.getAttributes().size());
      found = false;
      for (JaxAttribute attribute : task3R.getAttributes()) {
         if (attribute.getAttrTypeName().equals(CoreAttributeTypes.StaticId.getName())) {
            Assert.assertEquals("my static id", attribute.getValues().iterator().next());
            found = true;
            break;
         }
      }
      Assert.assertTrue("Static Id attribute not found", found);
      Assert.assertEquals(DemoUsers.Alex_Kay.getUserId(), task3R.getAssigneeUserIds().iterator().next());

      // Test Delete
      taskEp.delete(taskUuid1);
      Assert.assertNull(AtsClientService.get().getArtifact(taskUuid1));
      taskEp.delete(taskUuid2);
      Assert.assertNull(AtsClientService.get().getArtifact(taskUuid2));
      taskEp.delete(taskUuid3);
      Assert.assertNull(AtsClientService.get().getArtifact(taskUuid3));
   }

   private JaxAtsTask createJaxAtsTask(long taskUuid, String title, String description, String createdByUserId, Date createdDate, List<String> assigneeUserIds) {
      JaxAtsTask task = new JaxAtsTask();
      task.setUuid(taskUuid);
      task.setName(title);
      task.setDescription(description);
      task.setCreatedByUserId(createdByUserId);
      task.setCreatedDate(createdDate);
      if (assigneeUserIds == null || assigneeUserIds.isEmpty()) {
         assigneeUserIds = new ArrayList<>();
         assigneeUserIds.add(AtsCoreUsers.UNASSIGNED_USER.getUserId());
      }
      task.setAssigneeUserIds(assigneeUserIds);
      return task;
   }

}
