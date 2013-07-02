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
package org.eclipse.osee.orcs.data;

import java.util.Collection;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Roberto E. Escobar
 */
public interface ArtifactTypes extends IdentityCollection<Long, IArtifactType> {

   boolean hasSuperArtifactTypes(IArtifactType artType) throws OseeCoreException;

   Collection<? extends IArtifactType> getSuperArtifactTypes(IArtifactType artType) throws OseeCoreException;

   Collection<? extends IArtifactType> getDescendantTypes(IArtifactType artType, int depth) throws OseeCoreException;

   Collection<? extends IArtifactType> getAllDescendantTypes(IArtifactType artType) throws OseeCoreException;

   boolean isValidAttributeType(IArtifactType artType, IOseeBranch branch, IAttributeType attributeType) throws OseeCoreException;

   Collection<IAttributeType> getAttributeTypes(IArtifactType artType, IOseeBranch branch) throws OseeCoreException;

   boolean isAbstract(IArtifactType artType) throws OseeCoreException;

   boolean inheritsFrom(IArtifactType artType, IArtifactType... otherTypes) throws OseeCoreException;

}