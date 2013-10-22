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
package org.eclipse.osee.ats.core.client.artifact;

import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public abstract class AbstractAtsArtifact extends Artifact implements IAtsObject {

   public AbstractAtsArtifact(String guid, Branch branch, IArtifactType artifactType) throws OseeCoreException {
      super(guid, branch, artifactType);
   }

   @SuppressWarnings("unused")
   public Artifact getParentAtsArtifact() throws OseeCoreException {
      return null;
   }

}
