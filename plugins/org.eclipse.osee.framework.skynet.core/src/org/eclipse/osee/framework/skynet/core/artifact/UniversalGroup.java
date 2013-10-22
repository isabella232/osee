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
package org.eclipse.osee.framework.skynet.core.artifact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.OseeSystemArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Donald G. Dunne
 */
public class UniversalGroup {

   public static Collection<Artifact> getGroups(IOseeBranch branch) {
      Collection<Artifact> artifacts = null;
      try {
         artifacts = ArtifactQuery.getArtifactListFromType(CoreArtifactTypes.UniversalGroup, branch);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         artifacts = new LinkedList<Artifact>();
      }
      return artifacts;
   }

   public static Collection<Artifact> getGroupsNotRoot(IOseeBranch branch) {
      Collection<Artifact> groups = new HashSet<Artifact>();
      for (Artifact group : UniversalGroup.getGroups(branch)) {
         if (!group.getName().equals("Root Artifact")) {
            groups.add(group);
         }
      }
      return groups;
   }

   public static Collection<Artifact> getGroups(String groupName, IOseeBranch branch) {
      try {
         return ArtifactQuery.getArtifactListFromTypeAndName(CoreArtifactTypes.UniversalGroup, groupName, branch);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return new ArrayList<Artifact>();
   }

   public static Artifact addGroup(String name, IOseeBranch branch, SkynetTransaction transaction) throws OseeCoreException {
      if (!getGroups(name, branch).isEmpty()) {
         throw new OseeArgumentException("Group Already Exists");
      }

      Artifact groupArt = ArtifactTypeManager.addArtifact(CoreArtifactTypes.UniversalGroup, branch, name);
      groupArt.persist(transaction);

      Artifact groupRoot = getTopUniversalGroupArtifact(branch);
      groupRoot.addRelation(CoreRelationTypes.Universal_Grouping__Members, groupArt);
      groupRoot.persist(transaction);

      return groupArt;
   }

   public static Artifact getTopUniversalGroupArtifact(IOseeBranch branch) throws OseeCoreException {
      return ArtifactQuery.getArtifactFromTypeAndName(CoreArtifactTypes.UniversalGroup,
         OseeSystemArtifacts.ROOT_ARTIFACT_TYPE_NAME, branch);
   }
}