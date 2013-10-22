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
package org.eclipse.osee.orcs.core.internal.proxy;

import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.internal.artifact.Artifact;
import org.eclipse.osee.orcs.core.internal.attribute.Attribute;
import org.eclipse.osee.orcs.core.internal.relation.RelationNode;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.AttributeReadable;

/**
 * @author Megumi Telles
 */
public interface ExternalArtifactManager {

   ResultSet<ArtifactReadable> asExternalArtifacts(OrcsSession session, Iterable<? extends Artifact> artifacts) throws OseeCoreException;

   ResultSet<? extends RelationNode> asInternalArtifacts(Iterable<? extends ArtifactReadable> externals) throws OseeCoreException;

   Artifact asInternalArtifact(ArtifactReadable external) throws OseeCoreException;

   ArtifactReadable asExternalArtifact(OrcsSession session, Artifact artifact) throws OseeCoreException;

   <T> AttributeReadable<T> asExternalAttribute(OrcsSession session, Attribute<T> attribute) throws OseeCoreException;

   <T> ResultSet<AttributeReadable<T>> asExternalAttributes(OrcsSession session, Iterable<? extends Attribute<T>> attributes) throws OseeCoreException;

}