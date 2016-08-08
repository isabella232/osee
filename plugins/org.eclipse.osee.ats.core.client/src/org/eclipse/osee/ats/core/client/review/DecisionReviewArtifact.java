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
package org.eclipse.osee.ats.core.client.review;

import org.eclipse.osee.ats.api.review.IAtsDecisionReview;
import org.eclipse.osee.framework.core.data.ArtifactTypeId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.skynet.core.artifact.IATSStateMachineArtifact;

/**
 * @author Donald G. Dunne
 */
public class DecisionReviewArtifact extends AbstractReviewArtifact implements IATSStateMachineArtifact, IAtsDecisionReview {

   public XDecisionOptions decisionOptions;

   public DecisionReviewArtifact(String guid, BranchId branch, ArtifactTypeId artifactType) {
      super(guid, branch, artifactType);
      decisionOptions = new XDecisionOptions(this);
   }
}