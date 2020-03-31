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
package org.eclipse.osee.ats.ide.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.util.AtsUtil;
import org.eclipse.osee.ats.api.workdef.ITransitionResult;
import org.eclipse.osee.ats.api.workflow.transition.TransitionResults;
import org.eclipse.osee.ats.api.workflow.transition.TransitionWorkItemResult;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.ats.ide.internal.AtsClientService;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.event.filter.ArtifactTypeEventFilter;
import org.eclipse.osee.framework.skynet.core.event.filter.BranchIdEventFilter;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.utility.DbUtil;

/**
 * @author Donald G. Dunne
 */
public class AtsUtilClient {
   private static ArtifactTypeEventFilter atsObjectArtifactTypesFilter =
      new ArtifactTypeEventFilter(AtsArtifactTypes.TeamWorkflow, AtsArtifactTypes.Action, AtsArtifactTypes.Task,
         AtsArtifactTypes.Goal, AtsArtifactTypes.AgileSprint, AtsArtifactTypes.PeerToPeerReview,
         AtsArtifactTypes.DecisionReview, AtsArtifactTypes.Version);
   private static ArtifactTypeEventFilter reviewArtifactTypesFilter =
      new ArtifactTypeEventFilter(AtsArtifactTypes.PeerToPeerReview, AtsArtifactTypes.DecisionReview);
   private static ArtifactTypeEventFilter teamWorkflowArtifactTypesFilter =
      new ArtifactTypeEventFilter(AtsArtifactTypes.TeamWorkflow);
   private static List<IEventFilter> atsObjectEventFilter = new ArrayList<>(2);
   private static boolean emailEnabled = true;
   private static BranchIdEventFilter commonBranchIdEventFilter =
      new BranchIdEventFilter(AtsClientService.get().getAtsBranch());

   public static boolean isEmailEnabled() {
      return emailEnabled;
   }

   public static void setEmailEnabled(boolean enabled) {
      if (!DbUtil.isDbInit() && !AtsUtil.isInTest()) {
         OseeLog.log(Activator.class, Level.INFO, "Email " + (enabled ? "Enabled" : "Disabled"));
      }
      emailEnabled = enabled;
   }

   public static BranchIdEventFilter getAtsBranchFilter() {
      return commonBranchIdEventFilter;
   }

   public synchronized static List<IEventFilter> getAtsObjectEventFilters() {
      try {
         if (atsObjectEventFilter.isEmpty()) {
            atsObjectEventFilter.add(AtsUtilClient.getAtsBranchFilter());
            atsObjectEventFilter.add(getAtsObjectArtifactTypeEventFilter());
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return atsObjectEventFilter;
   }

   public static ArtifactTypeEventFilter getAtsObjectArtifactTypeEventFilter() {
      return atsObjectArtifactTypesFilter;
   }

   public static ArtifactTypeEventFilter getTeamWorkflowArtifactTypeEventFilter() {
      return teamWorkflowArtifactTypesFilter;
   }

   public static ArtifactTypeEventFilter getReviewArtifactTypeEventFilter() {
      return reviewArtifactTypesFilter;
   }

   /**
    * Log exceptions to OseeLog. Don't always want to do this due to testing.
    */
   public static void logExceptions(TransitionResults transResult) {
      for (ITransitionResult result : transResult.getResults()) {
         String ex = result.getException();
         if (Strings.isValid(ex)) {
            OseeLog.log(TransitionResults.class, Level.SEVERE, result.getDetails() + "\n\n" + ex);
         }
      }
      for (TransitionWorkItemResult transitionWorkItemResult : transResult.getTransitionWorkItems()) {
         for (ITransitionResult result : transitionWorkItemResult.getResults()) {
            String ex = result.getException();
            if (Strings.isValid(ex)) {
               String message = transitionWorkItemResult.getWorkItem().toStringWithId() + " - " + result.getDetails();
               OseeLog.log(TransitionResults.class, Level.SEVERE, message + "\n\n" + ex);
            }
         }
      }
   }
}
