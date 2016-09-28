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
package org.eclipse.osee.ats.core.client.artifact;

import static org.eclipse.osee.framework.core.enums.RelationSorter.USER_DEFINED;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.framework.core.data.ArtifactTypeId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public abstract class CollectorArtifact extends AbstractWorkflowArtifact implements HasMembers {

   private final IRelationTypeSide membersRelationType;

   public CollectorArtifact(String guid, BranchId branch, ArtifactTypeId artifactType, IRelationTypeSide membersRelationType) {
      super(guid, branch, artifactType);
      this.membersRelationType = membersRelationType;
   }

   @Override
   public void addMember(Artifact artifact) throws OseeCoreException {
      if (!getMembers().contains(artifact)) {
         addRelation(USER_DEFINED, membersRelationType, artifact);
      }
   }

}
