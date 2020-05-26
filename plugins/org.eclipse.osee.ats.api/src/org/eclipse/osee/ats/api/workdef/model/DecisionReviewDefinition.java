/*********************************************************************
 * Copyright (c) 2011 Boeing
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

package org.eclipse.osee.ats.api.workdef.model;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.ats.api.user.AtsUser;
import org.eclipse.osee.ats.api.workdef.IAtsDecisionReviewDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsDecisionReviewOption;
import org.eclipse.osee.ats.api.workdef.StateEventType;

/**
 * @author Donald G. Dunne
 */
public class DecisionReviewDefinition implements IAtsDecisionReviewDefinition {

   public String name;
   public String reviewTitle;
   public String description = "";
   public String relatedToState;
   public ReviewBlockType blockingType;
   public StateEventType stateEventType;
   public List<String> assignees = new ArrayList<>();
   public boolean autoTransitionToDecision = false;
   public List<IAtsDecisionReviewOption> options = new ArrayList<>();

   public DecisionReviewDefinition(String name) {
      this.name = name;
   }

   public DecisionReviewDefinition() {
      this("");
   }

   @Override
   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   @Override
   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   @Override
   public ReviewBlockType getBlockingType() {
      return blockingType;
   }

   public void setBlockingType(ReviewBlockType blockingType) {
      this.blockingType = blockingType;
   }

   @Override
   public StateEventType getStateEventType() {
      return stateEventType;
   }

   public void setStateEventType(StateEventType stateEventType) {
      this.stateEventType = stateEventType;
   }

   @Override
   public boolean isAutoTransitionToDecision() {
      return autoTransitionToDecision;
   }

   public void setAutoTransitionToDecision(boolean autoTransitionToDecision) {
      this.autoTransitionToDecision = autoTransitionToDecision;
   }

   @Override
   public List<String> getAssignees() {
      return assignees;
   }

   @Override
   public List<IAtsDecisionReviewOption> getOptions() {
      return options;
   }

   @Override
   public String toString() {
      return name;
   }

   @Override
   public String getReviewTitle() {
      return reviewTitle;
   }

   public void setReviewTitle(String reviewTitle) {
      this.reviewTitle = reviewTitle;
   }

   @Override
   public String getRelatedToState() {
      return relatedToState;
   }

   public void setRelatedToState(String relatedToState) {
      this.relatedToState = relatedToState;
   }

   public void addAssignee(AtsUser user) {
      this.assignees.add(user.getUserId());
   }

   public void addAssignee(String userid) {
      this.assignees.add(userid);
   }

}
