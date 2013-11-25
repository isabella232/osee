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
package org.eclipse.osee.ats.client.integration.tests.ats.core.client.workflow.transition;

import java.util.Arrays;
import java.util.Collection;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workdef.IStateToken;
import org.eclipse.osee.ats.api.workflow.transition.ITransitionListener;
import org.eclipse.osee.ats.api.workflow.transition.TransitionAdapter;
import org.eclipse.osee.ats.api.workflow.transition.TransitionOption;
import org.eclipse.osee.ats.api.workflow.transition.TransitionResult;
import org.eclipse.osee.ats.api.workflow.transition.TransitionResults;
import org.eclipse.osee.ats.client.integration.tests.ats.core.client.AtsTestUtil;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.AtsChangeSet;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionListeners;
import org.eclipse.osee.ats.core.workflow.transition.TransitionManager;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.junit.Assert;

/**
 * @author Donald G. Dunne
 */
public class TransitionListenersTest {

   @org.junit.Test
   public void testHandleTransitionValidation__ExtensionPointCheck() throws OseeCoreException {

      AtsTestUtil.cleanupAndReset("TransitionManagerTest-7");
      TeamWorkFlowArtifact teamArt = AtsTestUtil.getTeamWf();
      MockTransitionHelper helper =
         new MockTransitionHelper(
            getClass().getSimpleName(),
            Arrays.asList(teamArt),
            AtsTestUtil.getImplementStateDef().getName(),
            Arrays.asList(org.eclipse.osee.ats.client.integration.tests.AtsClientService.get().getUserAdmin().getCurrentUser()),
            null, new AtsChangeSet(getClass().getSimpleName()), TransitionOption.None);
      TransitionManager transMgr = new TransitionManager(helper);
      TransitionResults results = new TransitionResults();

      // validate that can transition
      transMgr.handleTransitionValidation(results);
      Assert.assertTrue(results.isEmpty());

      // add transition listeners and verify can't transition
      final String reason1 = "Don't want you to transition";
      final String reason2 = "Don't transition yet";
      final String exceptionStr = "This is the exception message";
      ITransitionListener listener1 = new TransitionAdapter() {

         @Override
         public void transitioning(TransitionResults results, IAtsWorkItem workItem, IStateToken fromState, IStateToken toState, Collection<? extends IAtsUser> toAssignees) {
            results.addResult(new TransitionResult(reason1));
         }

      };
      ITransitionListener listener2 = new TransitionAdapter() {

         @Override
         public void transitioning(TransitionResults results, IAtsWorkItem workItem, IStateToken fromState, IStateToken toState, Collection<? extends IAtsUser> toAssignees) {
            results.addResult(workItem, new TransitionResult(reason2));
         }

      };
      ITransitionListener listener3 = new TransitionAdapter() {

         @Override
         public void transitioning(TransitionResults results, IAtsWorkItem workItem, IStateToken fromState, IStateToken toState, Collection<? extends IAtsUser> toAssignees) {
            // do nothing
         }

      };
      ITransitionListener listener4 = new TransitionAdapter() {

         @Override
         public void transitioning(TransitionResults results, IAtsWorkItem workItem, IStateToken fromState, IStateToken toState, Collection<? extends IAtsUser> toAssignees) throws OseeCoreException {
            throw new OseeCoreException(exceptionStr);
         }

      };
      try {
         TransitionListeners.addListener(listener1);
         TransitionListeners.addListener(listener2);
         TransitionListeners.addListener(listener3);
         TransitionListeners.addListener(listener4);

         transMgr.handleTransitionValidation(results);
         Assert.assertTrue(results.contains(reason1));
         Assert.assertTrue(results.contains(reason2));
         Assert.assertTrue(results.contains(exceptionStr));
      } finally {
         TransitionListeners.removeListener(listener1);
         TransitionListeners.removeListener(listener2);
         TransitionListeners.removeListener(listener3);
         TransitionListeners.removeListener(listener4);
      }
   }

}
