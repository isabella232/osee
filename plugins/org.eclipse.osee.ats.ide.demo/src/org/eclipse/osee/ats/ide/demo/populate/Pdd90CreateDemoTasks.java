/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.ats.ide.demo.populate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.eclipse.osee.ats.api.task.JaxAtsTask;
import org.eclipse.osee.ats.api.task.JaxAtsTaskFactory;
import org.eclipse.osee.ats.api.task.NewTaskData;
import org.eclipse.osee.ats.api.task.NewTaskDataFactory;
import org.eclipse.osee.ats.api.task.NewTaskDatas;
import org.eclipse.osee.ats.api.user.AtsUser;
import org.eclipse.osee.ats.ide.demo.DemoUtil;
import org.eclipse.osee.ats.ide.demo.internal.AtsApiService;
import org.eclipse.osee.ats.ide.workflow.teamwf.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.core.enums.DemoUsers;

/**
 * @author Donald G. Dunne
 */
public class Pdd90CreateDemoTasks {

   public void run() throws Exception {
      Date createdDate = new Date();
      AtsUser createdBy = AtsApiService.get().getUserService().getCurrentUser();
      boolean firstTaskWorkflow = true;
      NewTaskDatas newTaskDatas = new NewTaskDatas();
      for (TeamWorkFlowArtifact codeArt : Arrays.asList(DemoUtil.getSawCodeCommittedWf(),
         DemoUtil.getSawCodeUnCommittedWf())) {
         NewTaskData newTaskData = NewTaskDataFactory.get("Populate Demo DB - Create Tasks", createdBy, codeArt);
         List<String> assigneeUserIds = new ArrayList<>();
         if (firstTaskWorkflow) {
            assigneeUserIds.add(DemoUsers.Joe_Smith.getUserId());
            assigneeUserIds.add(DemoUsers.Kay_Jones.getUserId());
         } else {
            assigneeUserIds.add(DemoUsers.Joe_Smith.getUserId());
         }
         for (String title : firstTaskWorkflow ? DemoUtil.Saw_Code_Committed_Task_Titles : DemoUtil.Saw_Code_UnCommitted_Task_Titles) {
            JaxAtsTask task = JaxAtsTaskFactory.get(newTaskData, title, createdBy, createdDate);
            task.setRelatedToState(codeArt.getCurrentStateName());
            task.setAssigneeUserIds(assigneeUserIds);
         }
         firstTaskWorkflow = false;
         newTaskDatas.add(newTaskData);
      }
      AtsApiService.get().getTaskService().createTasks(newTaskDatas);
   }

}
