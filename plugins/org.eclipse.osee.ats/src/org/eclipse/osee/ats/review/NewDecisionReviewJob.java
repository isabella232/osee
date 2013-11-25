/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/

package org.eclipse.osee.ats.review;

import java.util.Date;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.ats.AtsOpenOption;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workdef.IAtsDecisionReviewOption;
import org.eclipse.osee.ats.api.workdef.ReviewBlockType;
import org.eclipse.osee.ats.core.client.review.DecisionReviewArtifact;
import org.eclipse.osee.ats.core.client.review.DecisionReviewManager;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.AtsChangeSet;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.AtsUtil;

/**
 * @author Donald G. Dunne
 */
public class NewDecisionReviewJob extends Job {
   private final TeamWorkFlowArtifact teamParent;
   private final ReviewBlockType reviewBlockType;
   private final String reviewTitle;
   private final String againstState;
   private final List<IAtsDecisionReviewOption> options;
   private final List<? extends IAtsUser> assignees;
   private final String description;
   private final Date createdDate;
   private final IAtsUser createdBy;

   public NewDecisionReviewJob(TeamWorkFlowArtifact teamParent, ReviewBlockType reviewBlockType, String reviewTitle, String againstState, String description, List<IAtsDecisionReviewOption> options, List<? extends IAtsUser> assignees, Date createdDate, IAtsUser createdBy) {
      super("Creating New Decision Review");
      this.teamParent = teamParent;
      this.reviewTitle = reviewTitle;
      this.againstState = againstState;
      this.reviewBlockType = reviewBlockType;
      this.description = description;
      this.options = options;
      this.assignees = assignees;
      this.createdDate = createdDate;
      this.createdBy = createdBy;
   }

   @Override
   public IStatus run(final IProgressMonitor monitor) {
      try {
         AtsChangeSet changes = new AtsChangeSet(getClass().getSimpleName());
         DecisionReviewArtifact decArt =
            DecisionReviewManager.createNewDecisionReview(teamParent, reviewBlockType, reviewTitle, againstState,
               description, options, assignees, createdDate, createdBy, changes);
         changes.execute();
         AtsUtil.openATSAction(decArt, AtsOpenOption.OpenOneOrPopupSelect);
      } catch (Exception ex) {
         monitor.done();
         return new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Error creating Decision Review", ex);
      } finally {
         monitor.done();
      }
      return Status.OK_STATUS;
   }

}
