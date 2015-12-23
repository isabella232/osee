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
package org.eclipse.osee.ats.walker;

import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.core.client.review.ReviewManager;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.graphics.Image;

/**
 * @author Donald G. Dunne
 */
public class ReviewWrapper implements IActionWalkerItem {

   private final TeamWorkFlowArtifact teamArt;

   public ReviewWrapper(TeamWorkFlowArtifact teamArt) {
      this.teamArt = teamArt;
   }

   @Override
   public String toString() {
      try {
         return String.format(ReviewManager.getReviews(teamArt).size() + " Reviews");
      } catch (OseeCoreException ex) {
         return "Exception: " + ex.getLocalizedMessage();
      }
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (teamArt == null ? 0 : teamArt.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      ReviewWrapper other = (ReviewWrapper) obj;
      if (teamArt == null) {
         if (other.teamArt != null) {
            return false;
         }
      } else if (!teamArt.equals(other.teamArt)) {
         return false;
      }
      return true;
   }

   @Override
   public Image getImage() {
      return ImageManager.getImage(AtsImage.REVIEW);
   }

   @Override
   public String getName() {
      return toString();
   }

   @Override
   public void handleDoubleClick() {
      try {
         AtsUtil.openInAtsWorldEditor("Reviews",
            Collections.castAll(Artifact.class, ReviewManager.getReviews(teamArt)));
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   public TeamWorkFlowArtifact getTeamArt() {
      return teamArt;
   }

}
