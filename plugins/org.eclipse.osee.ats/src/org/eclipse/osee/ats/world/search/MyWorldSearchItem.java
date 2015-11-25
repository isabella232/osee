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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public class MyWorldSearchItem extends UserSearchItem {

   public MyWorldSearchItem(String name) {
      this(name, null);
   }

   public MyWorldSearchItem() {
      this("My World", null);
   }

   public MyWorldSearchItem(String name, IAtsUser user) {
      super(name, user, AtsImage.GLOBE);
   }

   public MyWorldSearchItem(MyWorldSearchItem myWorldSearchItem) {
      super(myWorldSearchItem);
   }

   @Override
   public Collection<Artifact> searchIt(IAtsUser user) throws OseeCoreException {
      Set<Artifact> assigned = AtsUtil.getAssigned(user);

      Set<Artifact> results = new HashSet<>(assigned.size());
      for (Artifact artifact : assigned) {
         if (artifact instanceof AbstractWorkflowArtifact) {
            if (artifact.isOfType(AtsArtifactTypes.Task)) {
               results.add(((TaskArtifact) artifact).getParentTeamWorkflow());
            } else {
               results.add(artifact);
            }
         }
      }
      return results;
   }

   @Override
   public WorldUISearchItem copy() {
      return new MyWorldSearchItem(this);
   }

}