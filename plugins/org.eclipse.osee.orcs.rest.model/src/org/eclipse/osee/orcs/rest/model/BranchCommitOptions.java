/*********************************************************************
 * Copyright (c) 2015 Boeing
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

package org.eclipse.osee.orcs.rest.model;

import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.osee.framework.core.data.ArtifactId;

/**
 * @author Roberto E. Escobar
 */
@XmlRootElement
public class BranchCommitOptions {
   private ArtifactId committer = ArtifactId.SENTINEL;
   private boolean archive;

   public ArtifactId getCommitter() {
      return committer;
   }

   public void setCommitter(ArtifactId committer) {
      this.committer = committer;
   }

   public boolean isArchive() {
      return archive;
   }

   public void setArchive(boolean archive) {
      this.archive = archive;
   }
}