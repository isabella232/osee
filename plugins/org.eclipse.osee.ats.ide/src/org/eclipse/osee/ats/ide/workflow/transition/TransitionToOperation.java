/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.ide.workflow.transition;

import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.workflow.transition.ITransitionHelper;
import org.eclipse.osee.ats.api.workflow.transition.TransitionResult;
import org.eclipse.osee.ats.api.workflow.transition.TransitionResults;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.ats.ide.internal.AtsClientService;
import org.eclipse.osee.ats.ide.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Donald G. Dunne
 */
public class TransitionToOperation extends AbstractOperation {

   private final ITransitionHelper helper;
   private TransitionResults results;

   public TransitionToOperation(ITransitionHelper helper) {
      super(helper.getName(), Activator.PLUGIN_ID);
      this.helper = helper;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      try {
         IAtsChangeSet changes = AtsClientService.get().createChangeSet(helper.getName() + ".preSave");
         for (IAtsWorkItem workItem : helper.getWorkItems()) {
            AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) workItem;
            if (awa.isDirty()) {
               changes.add(awa);
            }
         }
         changes.executeIfNeeded();

         results = AtsClientService.get().getWorkItemService().transition(helper);
         if (!results.isEmpty()) {
            TransitionResultsUi.report("Transition", results);
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         results.addResult(
            new TransitionResult(String.format("Exception [%s] transitioning to [%s].  See error log for details.",
               ex.getLocalizedMessage(), helper.getToStateName())));
      }
   }

   public TransitionResults getResults() {
      return results;
   }

}
