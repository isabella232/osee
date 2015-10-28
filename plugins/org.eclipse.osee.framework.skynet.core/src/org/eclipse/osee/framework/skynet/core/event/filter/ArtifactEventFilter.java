/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.skynet.core.event.filter;

import java.util.Arrays;
import java.util.List;
import org.eclipse.osee.framework.core.model.event.IBasicGuidArtifact;
import org.eclipse.osee.framework.core.model.event.IBasicGuidRelation;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author John R. Misinco
 */
public class ArtifactEventFilter implements IEventFilter {

   private final String filterArtifactGuid;
   private final Long filterBranchUuid;

   public ArtifactEventFilter(Artifact artifact) {
      filterArtifactGuid = artifact.getGuid();
      filterBranchUuid = artifact.getBranchId();
   }

   @Override
   public boolean isMatch(Long branchUuid) {
      return branchUuid.equals(filterBranchUuid);
   }

   @Override
   public boolean isMatchArtifacts(List<? extends IBasicGuidArtifact> guidArts) {
      for (IBasicGuidArtifact art : guidArts) {
         if (art.getGuid().equals(filterArtifactGuid) && art.isOnBranch(filterBranchUuid)) {
            return true;
         }
      }
      return false;
   }

   @Override
   public boolean isMatchRelationArtifacts(List<? extends IBasicGuidRelation> relations) {
      for (IBasicGuidRelation relation : relations) {
         if (isMatchArtifacts(Arrays.asList(relation.getArtA(), relation.getArtB()))) {
            return true;
         }
      }
      return false;
   }

}
