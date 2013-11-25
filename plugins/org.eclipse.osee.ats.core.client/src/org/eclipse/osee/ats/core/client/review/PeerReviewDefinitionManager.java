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
package org.eclipse.osee.ats.core.client.review;

import java.util.Collection;
import java.util.Date;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.workdef.IAtsPeerReviewDefinition;
import org.eclipse.osee.ats.api.workdef.IStateToken;
import org.eclipse.osee.ats.api.workdef.ReviewBlockType;
import org.eclipse.osee.ats.api.workdef.StateEventType;
import org.eclipse.osee.ats.api.workflow.log.LogType;
import org.eclipse.osee.ats.api.workflow.transition.TransitionAdapter;
import org.eclipse.osee.ats.core.AtsCore;
import org.eclipse.osee.ats.core.client.internal.AtsClientService;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.users.AtsCoreUsers;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;

/**
 * Create PeerToPeer Review from transition if defined by StateDefinition.
 * 
 * @author Donald G. Dunne
 */
public class PeerReviewDefinitionManager extends TransitionAdapter {

   /**
    * Creates PeerToPeer review if one of same name doesn't already exist
    */
   public static PeerToPeerReviewArtifact createNewPeerToPeerReview(IAtsPeerReviewDefinition peerRevDef, IAtsChangeSet changes, TeamWorkFlowArtifact teamArt, Date createdDate, IAtsUser createdBy) throws OseeCoreException {
      String title = peerRevDef.getReviewTitle();
      if (!Strings.isValid(title)) {
         title = String.format("Review [%s]", teamArt.getName());
      }
      if (Artifacts.getNames(ReviewManager.getReviews(teamArt)).contains(title)) {
         // Already created this review
         return null;
      }
      PeerToPeerReviewArtifact peerArt =
         PeerToPeerReviewManager.createNewPeerToPeerReview(teamArt, title, peerRevDef.getRelatedToState(), createdDate,
            createdBy, changes);
      if (Strings.isValid(peerRevDef.getDescription())) {
         peerArt.setSoleAttributeFromString(AtsAttributeTypes.Description, peerRevDef.getDescription());
      }
      ReviewBlockType reviewBlockType = peerRevDef.getBlockingType();
      if (reviewBlockType != null) {
         peerArt.setSoleAttributeFromString(AtsAttributeTypes.ReviewBlocks, reviewBlockType.name());
      }
      if (Strings.isValid(peerRevDef.getLocation())) {
         peerArt.setSoleAttributeFromString(AtsAttributeTypes.Location, peerRevDef.getLocation());
      }
      Collection<IAtsUser> assignees =
         AtsClientService.get().getUserAdmin().getUsersByUserIds(peerRevDef.getAssignees());
      if (assignees.size() > 0) {
         peerArt.getStateMgr().setAssignees(assignees);
      }
      peerArt.getLog().addLog(LogType.Note, null, String.format("Review [%s] auto-generated", peerRevDef.getName()),
         AtsCore.getUserService().getCurrentUser().getUserId());
      for (IReviewProvider provider : ReviewProviders.getAtsReviewProviders()) {
         provider.reviewCreated(peerArt);
      }
      AtsCore.getLogFactory().writeToStore(peerArt);
      return peerArt;
   }

   @Override
   public void transitioned(IAtsWorkItem workItem, IStateToken fromState, IStateToken toState, Collection<? extends IAtsUser> toAssignees, IAtsChangeSet changes) throws OseeCoreException {
      AbstractWorkflowArtifact sma = (AbstractWorkflowArtifact) workItem;
      // Create any decision or peerToPeer reviews for transitionTo and transitionFrom
      if (!sma.isTeamWorkflow()) {
         return;
      }
      Date createdDate = new Date();
      IAtsUser createdBy = AtsCoreUsers.SYSTEM_USER;
      TeamWorkFlowArtifact teamArt = (TeamWorkFlowArtifact) sma;

      for (IAtsPeerReviewDefinition peerRevDef : teamArt.getStateDefinition().getPeerReviews()) {
         if (peerRevDef.getStateEventType() != null && peerRevDef.getStateEventType().equals(
            StateEventType.TransitionTo)) {
            PeerToPeerReviewArtifact decArt =
               createNewPeerToPeerReview(peerRevDef, changes, teamArt, createdDate, createdBy);
            if (decArt != null) {
               changes.add(decArt);
            }
         }
      }
   }

}
