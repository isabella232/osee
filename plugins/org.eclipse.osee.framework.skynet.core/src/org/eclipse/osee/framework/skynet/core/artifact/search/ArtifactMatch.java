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
package org.eclipse.osee.framework.skynet.core.artifact.search;

import java.util.Collection;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.jdk.core.type.MatchLocation;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactMatch {
   private final Artifact artifact;
   private final HashCollection<Integer, MatchLocation> matchData = new HashCollection<>();

   public ArtifactMatch(Artifact artifact) {
      this.artifact = artifact;
   }

   public boolean hasMatchData() {
      return !matchData.isEmpty();
   }

   public Artifact getArtifact() {
      return artifact;
   }

   public HashCollection<Integer, MatchLocation> getMatchData() throws OseeCoreException {
      return matchData;
   }

   public void addMatchData(Integer attr, Collection<MatchLocation> locations) {
      matchData.put(attr, locations);
   }
}
