/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.framework.skynet.core.importing.resolvers;

import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.importing.RoughArtifact;

/**
 * Used during imports that ask for artifact re-use to resolve the Artifact to be used for a particular RoughArtifact
 * 
 * @author Robert A. Fisher
 */
public interface IArtifactImportResolver {

   public Artifact resolve(RoughArtifact roughArtifact, BranchId branch, Artifact realParent, Artifact root);
}
