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
package org.eclipse.osee.orcs.core.ds;

import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Roberto E. Escobar
 */
public interface ArtifactDataFactory {

   ArtifactData create(IOseeBranch branch, IArtifactType artifactType, String guid) throws OseeCoreException;

   ArtifactData create(IOseeBranch branch, IArtifactType artifactType, String guid, String hrid) throws OseeCoreException;

   ArtifactData copy(IOseeBranch destination, ArtifactData source) throws OseeCoreException;

   ArtifactData clone(ArtifactData source) throws OseeCoreException;

   ArtifactData introduce(IOseeBranch destination, ArtifactData source) throws OseeCoreException;

}
