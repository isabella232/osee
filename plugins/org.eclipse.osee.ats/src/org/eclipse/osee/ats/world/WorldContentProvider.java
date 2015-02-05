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

package org.eclipse.osee.ats.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.artifact.GoalArtifact;
import org.eclipse.osee.ats.core.client.artifact.SprintArtifact;
import org.eclipse.osee.ats.core.client.config.AtsBulkLoad;
import org.eclipse.osee.ats.core.client.review.ReviewManager;
import org.eclipse.osee.ats.core.client.task.AbstractTaskableArtifact;
import org.eclipse.osee.ats.core.client.task.TaskManager;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowManager;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.Widgets;

/**
 * @author Donald G. Dunne
 */
public class WorldContentProvider implements ITreeContentProvider {

   // Store off relatedArts as they are discovered so they're not garbage collected
   protected Set<Artifact> relatedArts = new HashSet<Artifact>();
   private final WorldXViewer xViewer;

   public WorldContentProvider(WorldXViewer WorldXViewer) {
      super();
      this.xViewer = WorldXViewer;
   }

   @Override
   public String toString() {
      return "WorldContentProvider";
   }

   public void clear(boolean forcePend) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (Widgets.isAccessible(xViewer.getControl())) {
               xViewer.setInput(Collections.emptyList());
               xViewer.refresh();
            }
         };
      }, forcePend);
   }

   @Override
   public Object[] getChildren(Object parentElement) {
      if (parentElement instanceof Collection<?>) {
         return ((Collection<?>) parentElement).toArray();
      }
      if (parentElement instanceof Artifact) {
         try {
            Artifact artifact = (Artifact) parentElement;
            if (artifact.isDeleted()) {
               return new Object[] {};
            }
            if (artifact.isOfType(AtsArtifactTypes.Action)) {
               relatedArts.addAll(ActionManager.getTeams(artifact));
               return ActionManager.getTeams((artifact)).toArray();
            }
            if (artifact.isOfType(AtsArtifactTypes.Goal)) {
               List<Artifact> arts = AtsClientService.get().getGoalMembersCache().getMembers((GoalArtifact) artifact);
               relatedArts.addAll(arts);
               AtsBulkLoad.bulkLoadArtifacts(relatedArts);
               return arts.toArray(new Artifact[arts.size()]);
            }
            if (artifact.isOfType(AtsArtifactTypes.AgileSprint)) {
               List<Artifact> arts =
                  AtsClientService.get().getSprintMembersCache().getMembers((SprintArtifact) artifact);
               relatedArts.addAll(arts);
               AtsBulkLoad.bulkLoadArtifacts(relatedArts);
               return arts.toArray(new Artifact[arts.size()]);
            }
            if (artifact.isOfType(AtsArtifactTypes.TeamWorkflow)) {
               TeamWorkFlowArtifact teamArt = TeamWorkFlowManager.cast(artifact);
               List<Artifact> arts = new ArrayList<Artifact>();
               // Convert artifacts to WorldArtifactItems
               arts.addAll(ReviewManager.getReviews(teamArt));
               arts.addAll(teamArt.getTaskArtifactsSorted());
               relatedArts.addAll(arts);
               return arts.toArray();
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
      return org.eclipse.osee.framework.jdk.core.util.Collections.EMPTY_ARRAY;
   }

   @Override
   public Object getParent(Object element) {
      if (element instanceof Artifact) {
         try {
            Artifact artifact = (Artifact) element;
            if (artifact.isDeleted()) {
               return null;
            }
            if (artifact.isOfType(AtsArtifactTypes.TeamWorkflow)) {
               return TeamWorkFlowManager.cast(artifact).getParentActionArtifact();
            }
            if (artifact.isOfType(AtsArtifactTypes.Task)) {
               return TaskManager.cast(artifact).getParentAWA();
            }
            if (artifact.isOfType(AtsArtifactTypes.ReviewArtifact)) {
               return ReviewManager.cast(artifact).getParentAWA();
            }
            if (artifact.isOfType(AtsArtifactTypes.Goal)) {
               return ((GoalArtifact) artifact).getParentAWA();
            }
            if (artifact.isOfType(AtsArtifactTypes.AgileSprint)) {
               return ((SprintArtifact) artifact).getParentAWA();
            }
         } catch (Exception ex) {
            // do nothing
         }
      }
      return null;
   }

   @Override
   public boolean hasChildren(Object element) {
      if (element instanceof Collection<?>) {
         return true;
      }
      if (element instanceof String) {
         return false;
      }
      if (((Artifact) element).isDeleted()) {
         return false;
      }
      if (Artifacts.isOfType(element, AtsArtifactTypes.Action)) {
         return true;
      }
      if (element instanceof AbstractWorkflowArtifact) {
         try {
            return hasAtsWorldChildren((AbstractWorkflowArtifact) element);
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex.toString());
         }
      }
      return true;
   }

   private boolean hasAtsWorldChildren(AbstractWorkflowArtifact workflow) throws OseeCoreException {
      if (workflow.isOfType(AtsArtifactTypes.Task)) {
         return false;
      }
      if (workflow instanceof AbstractTaskableArtifact && workflow.getRelatedArtifactsCount(AtsRelationTypes.TeamWfToTask_Task) > 0) {
         return true;
      }
      if (workflow instanceof TeamWorkFlowArtifact && workflow.getRelatedArtifactsCount(AtsRelationTypes.TeamWorkflowToReview_Review) > 0) {
         return true;
      }
      if (workflow instanceof GoalArtifact && workflow.getRelatedArtifactsCount(AtsRelationTypes.Goal_Member) > 0) {
         return true;
      }
      if (workflow instanceof SprintArtifact && workflow.getRelatedArtifactsCount(AtsRelationTypes.AgileSprint_Item) > 0) {
         return true;
      }
      return false;
   }

   @Override
   public Object[] getElements(Object inputElement) {
      if (inputElement instanceof String) {
         return new Object[] {inputElement};
      }
      return getChildren(inputElement);
   }

   @Override
   public void dispose() {
      // do nothing
   }

   @Override
   public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      // do nothing
   }

}
