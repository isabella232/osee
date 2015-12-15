/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.rest.internal.notify;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.notify.AtsNotificationCollector;
import org.eclipse.osee.ats.api.notify.AtsNotificationEvent;
import org.eclipse.osee.ats.api.notify.AtsNotifyType;
import org.eclipse.osee.ats.api.notify.AtsWorkItemNotificationEvent;
import org.eclipse.osee.ats.api.review.IAtsPeerToPeerReview;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.team.IAtsWorkItemFactory;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.user.IAtsUserService;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.IAttributeResolver;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.state.IAtsStateManager;
import org.eclipse.osee.ats.rest.IAtsServer;
import org.eclipse.osee.ats.rest.internal.notify.WorkItemNotificationProcessor;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsApi;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test unit for {@link WorkItemNotificationProcessor}
 * 
 * @author Donald G. Dunne
 */
public class WorkItemNotificationProcessorTest {

   // @formatter:off
   @Mock IAtsUser joeSmith_CurrentUser, kay_ValidEmail, jason_ValidEmail, alex_NoValidEmail, inactiveSteve; 
   @Mock IAtsTeamWorkflow teamWf;
   @Mock IAtsPeerToPeerReview peerReview;
   @Mock IAtsStateManager stateMgr;
   @Mock Log logger;
   @Mock IAtsServer atsServer;
   @Mock OrcsApi orcsApi;
   @Mock IAtsWorkItemFactory workItemFactory;
   @Mock IAtsUserService userService;
   @Mock IAttributeResolver attrResolver;
   @Mock IAtsStateDefinition stateDef;
   @Mock IAtsTeamDefinition teamDef;
   @Mock IAtsActionableItem ai;

   // @formatter:on

   @Before
   public void setup() {
      MockitoAnnotations.initMocks(this);

      setupUser(joeSmith_CurrentUser, 61106791L, "Joe Smith", "joe@boeing.com", "3333", true,
         false);
      setupUser(kay_ValidEmail, 5896672L, "Kay Jones", "kay@boeing.com", "4444", true, false);
      setupUser(jason_ValidEmail, 277990L, "Jason Michael", "jason@boeing.com", "5555",
         true, false);
      setupUser(alex_NoValidEmail, 8006939L, "Alex Kay", "", "6666", true, false);
      setupUser(inactiveSteve, 5808093L, "Inactive Steve", "insactiveSteve@boeing.com", "7777",
         false, false);

      when(teamWf.getName()).thenReturn(WorkItemNotificationProcessorTest.class.getSimpleName() + "-testNotify");
      List<IAtsUser> assignees = new ArrayList<>();
      assignees.addAll(Arrays.asList(inactiveSteve, alex_NoValidEmail, jason_ValidEmail, kay_ValidEmail,
         joeSmith_CurrentUser));
      String atsId = "ATS003";
      when(teamWf.getAtsId()).thenReturn(atsId);
      when(workItemFactory.getWorkItemByAtsId(atsId)).thenReturn(teamWf);
      when(attrResolver.getSoleAttributeValue(teamWf, AtsAttributeTypes.LegacyPcrId, "")).thenReturn(atsId);
      when(atsServer.getConfigValue(eq("ActionUrl"))).thenReturn("http://ats/action/UUID/");
      when(teamWf.getUuid()).thenReturn(98L);
      when(teamWf.getArtifactTypeName()).thenReturn("Team Workflow");
      when(teamWf.getStateMgr()).thenReturn(stateMgr);
      when(stateMgr.getCurrentStateName()).thenReturn("Analyze");
      when(stateMgr.getAssignees()).thenReturn(assignees);

      when(teamWf.getStateDefinition()).thenReturn(stateDef);
      when(teamWf.getTeamDefinition()).thenReturn(teamDef);
      Set<IAtsActionableItem> ais = new HashSet<>();
      ais.add(ai);
      when(teamWf.getActionableItems()).thenReturn(ais);

   }

   @org.junit.Test
   public void testNotifyOriginator() throws OseeCoreException {

      WorkItemNotificationProcessor processor =
         new WorkItemNotificationProcessor(logger, atsServer, workItemFactory, userService, attrResolver);
      AtsNotificationCollector notifications = new AtsNotificationCollector();
      AtsWorkItemNotificationEvent event = new AtsWorkItemNotificationEvent();
      event.setFromUserId(joeSmith_CurrentUser.getUserId());
      event.setNotifyType(AtsNotifyType.Originator);
      event.getAtsIds().add(teamWf.getAtsId());
      when(teamWf.getCreatedBy()).thenReturn(kay_ValidEmail);

      processor.run(notifications, event);

      Assert.assertEquals(1, notifications.getNotificationEvents().size());
      AtsNotificationEvent notifyEvent = notifications.getNotificationEvents().get(0);
      Assert.assertEquals(AtsNotifyType.Originator.name(), notifyEvent.getType());
      Assert.assertEquals(kay_ValidEmail.getUserId(), notifyEvent.getUserIds().iterator().next());
      Assert.assertEquals(
         "You have been set as the originator of [Team Workflow] state [Analyze] titled [WorkItemNotificationProcessorTest-testNotify]",
         notifyEvent.getDescription());

      notifications = new AtsNotificationCollector();
      when(teamWf.getCreatedBy()).thenReturn(inactiveSteve);
      processor.run(notifications, event);
      Assert.assertEquals(0, notifications.getNotificationEvents().size());

   }

   @org.junit.Test
   public void testNotifyAssignee() throws OseeCoreException {

      WorkItemNotificationProcessor processor =
         new WorkItemNotificationProcessor(logger, atsServer, workItemFactory, userService, attrResolver);
      AtsNotificationCollector notifications = new AtsNotificationCollector();
      AtsWorkItemNotificationEvent event = new AtsWorkItemNotificationEvent();
      event.setFromUserId(joeSmith_CurrentUser.getUserId());
      event.setNotifyType(AtsNotifyType.Assigned);
      event.getAtsIds().add(teamWf.getAtsId());
      event.getUserIds().add(kay_ValidEmail.getUserId());

      processor.run(notifications, event);
      Assert.assertEquals(1, notifications.getNotificationEvents().size());
      AtsNotificationEvent notifyEvent = notifications.getNotificationEvents().get(0);
      Assert.assertEquals(AtsNotifyType.Assigned.name(), notifyEvent.getType());

      // joe smith should be removed from list cause it's current user
      // alex should be removed cause not valid email
      List<String> expectedUserIds = new ArrayList<>();
      expectedUserIds.add(jason_ValidEmail.getUserId());
      expectedUserIds.add(kay_ValidEmail.getUserId());
      List<IAtsUser> users = new ArrayList<>();
      for (String userId : event.getUserIds()) {
         users.add(userService.getUserById(userId));
      }
      event.getUserIds().clear();

      notifications = new AtsNotificationCollector();
      processor.run(notifications, event);
      Assert.assertEquals(1, notifications.getNotificationEvents().size());
      notifyEvent = notifications.getNotificationEvents().get(0);

      Assert.assertTrue(org.eclipse.osee.framework.jdk.core.util.Collections.isEqual(expectedUserIds,
         notifyEvent.getUserIds()));
      Assert.assertEquals(
         "You have been set as the assignee of [Team Workflow] in state [Analyze] titled [WorkItemNotificationProcessorTest-testNotify]",
         notifyEvent.getDescription());

      notifications = new AtsNotificationCollector();
      event.getUserIds().add(jason_ValidEmail.getUserId());
      processor.run(notifications, event);
      Assert.assertEquals(1, notifications.getNotificationEvents().size());
      notifyEvent = notifications.getNotificationEvents().get(0);

      Assert.assertEquals(AtsNotifyType.Assigned.name(), notifyEvent.getType());
      // only alex should be emailed cause sent in list
      Assert.assertEquals(1, notifyEvent.getUserIds().size());
      Assert.assertEquals(jason_ValidEmail.getUserId(), notifyEvent.getUserIds().iterator().next());
      Assert.assertEquals(
         "You have been set as the assignee of [Team Workflow] in state [Analyze] titled [WorkItemNotificationProcessorTest-testNotify]",
         notifyEvent.getDescription());
   }

   @org.junit.Test
   public void testNotifySubscribe() throws OseeCoreException {

      when(userService.getSubscribed(teamWf)).thenReturn(Arrays.asList(kay_ValidEmail));
      AtsWorkItemNotificationEvent event = new AtsWorkItemNotificationEvent();
      event.setFromUserId(joeSmith_CurrentUser.getUserId());
      event.setNotifyType(AtsNotifyType.Subscribed);
      event.getAtsIds().add(teamWf.getAtsId());

      WorkItemNotificationProcessor processor =
         new WorkItemNotificationProcessor(logger, atsServer, workItemFactory, userService, attrResolver);
      AtsNotificationCollector notifications = new AtsNotificationCollector();
      processor.run(notifications, event);

      Assert.assertEquals(1, notifications.getNotificationEvents().size());
      AtsNotificationEvent notifyEvent = notifications.getNotificationEvents().get(0);

      Assert.assertEquals(AtsNotifyType.Subscribed.name(), notifyEvent.getType());
      // only alex should be emailed cause sent in list
      Assert.assertEquals(1, notifyEvent.getUserIds().size());
      Assert.assertEquals(kay_ValidEmail.getUserId(), notifyEvent.getUserIds().iterator().next());
      Assert.assertEquals(
         "[Team Workflow] titled [WorkItemNotificationProcessorTest-testNotify] transitioned to [Analyze] and you subscribed for notification.",
         notifyEvent.getDescription());

   }

   @org.junit.Test
   public void testNotifyCompleted() throws OseeCoreException {

      AtsWorkItemNotificationEvent event = new AtsWorkItemNotificationEvent();
      event.setFromUserId(joeSmith_CurrentUser.getUserId());
      event.setNotifyType(AtsNotifyType.Completed);
      event.getAtsIds().add(teamWf.getAtsId());
      when(teamWf.isTask()).thenReturn(false);
      when(stateDef.getStateType()).thenReturn(StateType.Completed);
      when(teamWf.getCreatedBy()).thenReturn(inactiveSteve);
      when(stateMgr.getCurrentStateName()).thenReturn("Completed");

      WorkItemNotificationProcessor processor =
         new WorkItemNotificationProcessor(logger, atsServer, workItemFactory, userService, attrResolver);
      AtsNotificationCollector notifications = new AtsNotificationCollector();

      processor.run(notifications, event);
      Assert.assertEquals(0, notifications.getNotificationEvents().size());

      notifications = new AtsNotificationCollector();
      when(teamWf.getCreatedBy()).thenReturn(kay_ValidEmail);
      processor.run(notifications, event);
      Assert.assertEquals(1, notifications.getNotificationEvents().size());
      AtsNotificationEvent notifyEvent = notifications.getNotificationEvents().get(0);
      Assert.assertEquals(AtsNotifyType.Completed.name(), notifyEvent.getType());
      Assert.assertEquals(kay_ValidEmail.getUserId(), notifyEvent.getUserIds().iterator().next());
      Assert.assertEquals("[Team Workflow] titled [WorkItemNotificationProcessorTest-testNotify] is [Completed]",
         notifyEvent.getDescription());

      notifications = new AtsNotificationCollector();
      when(teamWf.getCreatedBy()).thenReturn(inactiveSteve);
      Assert.assertEquals(0, notifications.getNotificationEvents().size());

   }

   @org.junit.Test
   public void testNotifyCancelled() throws OseeCoreException {

      AtsWorkItemNotificationEvent event = new AtsWorkItemNotificationEvent();
      event.setFromUserId(joeSmith_CurrentUser.getUserId());
      event.setNotifyType(AtsNotifyType.Cancelled);
      event.getAtsIds().add(teamWf.getAtsId());
      when(teamWf.isTask()).thenReturn(false);
      when(stateDef.getStateType()).thenReturn(StateType.Cancelled);
      when(teamWf.getCreatedBy()).thenReturn(inactiveSteve);
      when(teamWf.getCancelledReason()).thenReturn("this is the reason");
      when(teamWf.getCancelledFromState()).thenReturn("Analyze");
      when(stateMgr.getCurrentStateName()).thenReturn("Cancelled");

      WorkItemNotificationProcessor processor =
         new WorkItemNotificationProcessor(logger, atsServer, workItemFactory, userService, attrResolver);
      AtsNotificationCollector notifications = new AtsNotificationCollector();

      processor.run(notifications, event);
      Assert.assertEquals(0, notifications.getNotificationEvents().size());

      notifications = new AtsNotificationCollector();
      when(teamWf.getCreatedBy()).thenReturn(kay_ValidEmail);
      processor.run(notifications, event);
      Assert.assertEquals(1, notifications.getNotificationEvents().size());
      AtsNotificationEvent notifyEvent = notifications.getNotificationEvents().get(0);
      Assert.assertEquals(AtsNotifyType.Cancelled.name(), notifyEvent.getType());
      Assert.assertEquals(kay_ValidEmail.getUserId(), notifyEvent.getUserIds().iterator().next());
      Assert.assertTrue(notifyEvent.getDescription().startsWith(
         "[Team Workflow] titled [WorkItemNotificationProcessorTest-testNotify] was [Cancelled] from the [Analyze] state on"));
      Assert.assertTrue(notifyEvent.getDescription().endsWith(".<br>Reason: [this is the reason]"));

      notifications = new AtsNotificationCollector();
      when(teamWf.getCreatedBy()).thenReturn(inactiveSteve);
      Assert.assertEquals(0, notifications.getNotificationEvents().size());

   }

   @org.junit.Test
   public void testNotifySubscribedTeamOrAi() throws OseeCoreException {

      AtsWorkItemNotificationEvent event = new AtsWorkItemNotificationEvent();
      event.setFromUserId(joeSmith_CurrentUser.getUserId());
      event.setNotifyType(AtsNotifyType.SubscribedTeamOrAi);
      event.getAtsIds().add(teamWf.getAtsId());
      when(teamWf.isTeamWorkflow()).thenReturn(true);
      when(stateDef.getStateType()).thenReturn(StateType.Working);
      when(stateMgr.getCurrentStateName()).thenReturn(StateType.Working.name());

      WorkItemNotificationProcessor processor =
         new WorkItemNotificationProcessor(logger, atsServer, workItemFactory, userService, attrResolver);

      AtsNotificationCollector notifications = new AtsNotificationCollector();
      processor.run(notifications, event);
      when(teamDef.getSubscribed()).thenReturn(new ArrayList<IAtsUser>());
      when(ai.getSubscribed()).thenReturn(new ArrayList<IAtsUser>());
      Assert.assertEquals(0, notifications.getNotificationEvents().size());

      notifications = new AtsNotificationCollector();
      when(teamDef.getSubscribed()).thenReturn(Arrays.asList(kay_ValidEmail));
      when(ai.getSubscribed()).thenReturn(new ArrayList<IAtsUser>());
      processor.run(notifications, event);
      Assert.assertEquals(1, notifications.getNotificationEvents().size());

      notifications = new AtsNotificationCollector();
      when(teamDef.getSubscribed()).thenReturn(new ArrayList<IAtsUser>());
      when(ai.getSubscribed()).thenReturn(Arrays.asList(kay_ValidEmail));
      processor.run(notifications, event);
      Assert.assertEquals(1, notifications.getNotificationEvents().size());

      notifications = new AtsNotificationCollector();
      when(teamDef.getSubscribed()).thenReturn(Arrays.asList(jason_ValidEmail));
      when(ai.getSubscribed()).thenReturn(Arrays.asList(kay_ValidEmail));
      processor.run(notifications, event);
      Assert.assertEquals(2, notifications.getNotificationEvents().size());

   }

   private void setupUser(IAtsUser user, long uuid, String name, String email, String userId, boolean active, boolean admin) {
      when(user.getUuid()).thenReturn(uuid);
      when(user.getName()).thenReturn(name);
      when(user.getEmail()).thenReturn(email);
      when(user.isActive()).thenReturn(active);
      when(user.getUserId()).thenReturn(userId);

      when(userService.getUserById(userId)).thenReturn(user);
   }

}
