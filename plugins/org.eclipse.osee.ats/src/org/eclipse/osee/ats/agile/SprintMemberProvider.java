/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.agile;

import java.util.List;
import org.eclipse.nebula.widgets.xviewer.IXViewerFactory;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.api.agile.IAgileSprint;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.core.client.artifact.SprintArtifact;
import org.eclipse.osee.ats.goal.AbstractMemberProvider;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.framework.core.data.RelationTypeSide;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.swt.KeyedImage;

/**
 * @author Donald G. Dunne
 */
public class SprintMemberProvider extends AbstractMemberProvider {

   private final IAgileSprint sprint;

   public SprintMemberProvider(IAgileSprint sprint) {
      this.sprint = sprint;
   }

   @Override
   public String getCollectorName() {
      return "Sprint";
   }

   @Override
   public String getMembersName() {
      return "Items";
   }

   @Override
   public KeyedImage getImageKey() {
      return AtsImage.AGILE_SPRINT;
   }

   @Override
   public List<Artifact> getMembers() {
      return getArtifact().getMembers();
   }

   @Override
   public SprintArtifact getArtifact() {
      return (SprintArtifact) sprint.getStoreObject();
   }

   @Override
   public String getGuid() {
      return getArtifact().getGuid();
   }

   @Override
   public void addMember(Artifact artifact) {
      getArtifact().addMember(artifact);
   }

   @Override
   public IXViewerFactory getXViewerFactory(Artifact awa) {
      return new SprintXViewerFactory((SprintArtifact) awa, this);
   }

   @Override
   public String getColumnName() {
      return "ats.column.sprintOrder";
   }

   @Override
   public RelationTypeSide getMemberRelationTypeSide() {
      return AtsRelationTypes.AgileSprintToItem_AtsItem;
   }

   @Override
   public void promptChangeOrder(Artifact sprintArt, List<Artifact> selectedAtsArtifacts) {
      new SprintManager().promptChangeMemberOrder((SprintArtifact) sprintArt, selectedAtsArtifacts);
   }

   @Override
   public Result isAddValid(List<Artifact> artifacts) {
      StringBuilder builder = new StringBuilder();
      for (Artifact art : artifacts) {
         List<Artifact> relatedSprints = art.getRelatedArtifacts(AtsRelationTypes.AgileSprintToItem_Sprint);
         if (relatedSprints.size() > 1 || relatedSprints.size() == 1 && !relatedSprints.iterator().next().equals(
            getArtifact())) {
            builder.append(art.getArtifactTypeName());
            builder.append(" ");
            builder.append(art.toStringWithId());
            builder.append(" already belongs to ");
            builder.append(art.getRelatedArtifactsCount(AtsRelationTypes.AgileSprintToItem_Sprint));
            builder.append(" Sprint(s)\n");
         }
      }
      if (builder.toString().isEmpty()) {
         return Result.TrueResult;
      } else {
         builder.append("\nItems can only belong to 1 Sprint.  Move items to this Sprint?");
      }
      return new Result(false, builder.toString());
   }

   @Override
   public void deCacheArtifact() {
      AtsClientService.get().getSprintItemsCache().decache(getArtifact());
   }

   @Override
   public boolean isBacklog() {
      return false;
   }

   @Override
   public boolean isSprint() {
      return true;
   }

}
