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
package org.eclipse.osee.ats.core.client.actions;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public class SelectedAtsArtifactsAdapter implements ISelectedAtsArtifacts {

   public SelectedAtsArtifactsAdapter() {
   }

   @SuppressWarnings("unused")
   @Override
   public Set<? extends Artifact> getSelectedSMAArtifacts() throws OseeCoreException {
      return Collections.emptySet();
   }

   @SuppressWarnings("unused")
   @Override
   public List<Artifact> getSelectedAtsArtifacts() throws OseeCoreException {
      return Collections.emptyList();
   }

   @SuppressWarnings("unused")
   @Override
   public List<TaskArtifact> getSelectedTaskArtifacts() throws OseeCoreException {
      return Collections.emptyList();
   }

}
