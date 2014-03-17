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
package org.eclipse.osee.ats.world.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactSearchCriteria;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;
import org.eclipse.osee.framework.ui.swt.KeyedImage;

/**
 * @author Donald G. Dunne
 */
public class ShowOpenWorkflowsByArtifactType extends WorldUISearchItem {

   private final IArtifactType artifactType;
   private final boolean showFinished;
   private final boolean showWorkflow;

   public ShowOpenWorkflowsByArtifactType(String displayName, IArtifactType artifactType, boolean showFinished, boolean showWorkflow, KeyedImage oseeImage) {
      super(displayName, oseeImage);
      this.artifactType = artifactType;
      this.showFinished = showFinished;
      this.showWorkflow = showWorkflow;
   }

   public ShowOpenWorkflowsByArtifactType(ShowOpenWorkflowsByArtifactType showOpenWorkflowsByArtifactType) {
      super(showOpenWorkflowsByArtifactType);
      this.artifactType = showOpenWorkflowsByArtifactType.artifactType;
      this.showFinished = showOpenWorkflowsByArtifactType.showFinished;
      this.showWorkflow = showOpenWorkflowsByArtifactType.showWorkflow;
   }

   @Override
   public Collection<Artifact> performSearch(SearchType searchType) throws OseeCoreException {

      List<Artifact> artifacts = null;
      if (!showFinished) {
         List<ArtifactSearchCriteria> criteria = new ArrayList<ArtifactSearchCriteria>();
         TeamWorldSearchItem.addIncludeCompletedCancelledCriteria(criteria, showFinished, showFinished);
         artifacts = ArtifactQuery.getArtifactListFromTypeAnd(artifactType, AtsUtilCore.getAtsBranch(), 500, criteria);
      } else {
         artifacts = ArtifactQuery.getArtifactListFromType(artifactType, AtsUtilCore.getAtsBranch());
      }

      if (showWorkflow) {
         return RelationManager.getRelatedArtifacts(artifacts, 1, AtsRelationTypes.TeamWorkflowToReview_Team);
      } else {
         return artifacts;
      }
   }

   @Override
   public WorldUISearchItem copy() {
      return new ShowOpenWorkflowsByArtifactType(this);
   }

}
