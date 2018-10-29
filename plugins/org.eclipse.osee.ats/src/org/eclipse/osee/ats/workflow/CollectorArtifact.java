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
package org.eclipse.osee.ats.workflow;

import static org.eclipse.osee.framework.core.enums.RelationSorter.USER_DEFINED;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.workflow.goal.HasMembers;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.RelationTypeSide;

/**
 * @author Donald G. Dunne
 */
public abstract class CollectorArtifact extends AbstractWorkflowArtifact implements HasMembers {

   private final RelationTypeSide membersRelationType;

   public CollectorArtifact(Long id, String guid, BranchId branch, IArtifactType artifactType, RelationTypeSide membersRelationType) {
      super(id, guid, branch, artifactType);
      this.membersRelationType = membersRelationType;
   }

   @Override
   public void addMember(ArtifactId artifact) {
      if (!getMembers().contains(artifact)) {
         addRelation(USER_DEFINED, membersRelationType,
            AtsClientService.get().getQueryServiceClient().getArtifact(artifact));
      }
   }

}