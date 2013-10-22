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
package org.eclipse.osee.framework.skynet.core;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * @author Roberto E. Escobar
 */
public class OseeGroup {
   private static final String USERS_GROUP_FOLDER_NAME = "User Groups";

   private final String groupName;
   private Artifact groupArtifact;
   private final Map<String, Boolean> temporaryOverride = new HashMap<String, Boolean>();

   public OseeGroup(String groupName) {
      this.groupName = groupName;
      this.groupArtifact = null;
      this.groupArtifact = null;
   }

   /**
    * @return Returns the group.
    */
   public Artifact getGroupArtifact() throws OseeCoreException {
      checkGroupExists();
      return groupArtifact;
   }

   /**
    * This does not persist the newly created relation that is the callers responsibility.
    */
   public void addMember(User user) throws OseeCoreException {
      getGroupArtifact().addRelation(CoreRelationTypes.Users_User, user);
   }

   /**
    * Determines whether the user is a member of this group
    * 
    * @param user to check
    * @return whether the user is a member of this group
    */
   public boolean isMember(User user) throws OseeCoreException {
      return isTemporaryOverride(user) || getGroupArtifact().isRelated(CoreRelationTypes.Users_User, user);
   }

   /**
    * Determines whether the current user is a member of this group
    * 
    * @return whether the current user is a member of this group
    */
   public boolean isCurrentUserMember() throws OseeCoreException {
      return isMember(UserManager.getUser());
   }

   public boolean isTemporaryOverride(User user) {
      if (temporaryOverride.get(groupName) != null) {
         return temporaryOverride.get(groupName);
      }
      return false;
   }

   /**
    * Allow user to temporarily override admin
    */
   public void setTemporaryOverride(boolean member) {
      temporaryOverride.put(groupName, member);
   }

   public void removeTemporaryOverride() {
      temporaryOverride.remove(groupName);
   }

   private void checkGroupExists() throws OseeCoreException {
      if (groupArtifact == null) {
         groupArtifact = getOrCreateGroupArtifact(groupName);
      }
   }

   private Artifact getOrCreateGroupArtifact(String groupName) throws OseeCoreException {
      Branch branch = BranchManager.getCommonBranch();
      String cacheKey = CoreArtifactTypes.UserGroup + "." + groupName;
      Artifact groupArtifact = ArtifactCache.getByTextId(cacheKey, branch);

      if (groupArtifact == null) {
         groupArtifact = ArtifactQuery.checkArtifactFromTypeAndName(CoreArtifactTypes.UserGroup, groupName, branch);
         if (groupArtifact == null) {
            Artifact userGroupsFolder = getOrCreateUserGroupsFolder(branch);
            groupArtifact = ArtifactTypeManager.addArtifact(CoreArtifactTypes.UserGroup, branch, groupName);
            userGroupsFolder.addChild(groupArtifact);
         }
         ArtifactCache.cacheByTextId(cacheKey, groupArtifact);
      }

      return groupArtifact;
   }

   private Artifact getOrCreateUserGroupsFolder(Branch branch) throws OseeCoreException {
      String cacheKey = CoreArtifactTypes.Folder.getName() + "." + USERS_GROUP_FOLDER_NAME;
      Artifact usersGroupFolder = ArtifactCache.getByTextId(cacheKey, branch);
      if (usersGroupFolder == null) {
         usersGroupFolder =
            ArtifactQuery.checkArtifactFromTypeAndName(CoreArtifactTypes.Folder, USERS_GROUP_FOLDER_NAME, branch);
         if (usersGroupFolder == null) {
            Artifact root = OseeSystemArtifacts.getDefaultHierarchyRootArtifact(branch);
            if (root.hasChild(USERS_GROUP_FOLDER_NAME)) {
               usersGroupFolder = root.getChild(USERS_GROUP_FOLDER_NAME);
            } else {
               usersGroupFolder =
                  ArtifactTypeManager.addArtifact(CoreArtifactTypes.Folder, branch, USERS_GROUP_FOLDER_NAME);
               root.addChild(usersGroupFolder);
            }
         }

         ArtifactCache.cacheByTextId(cacheKey, usersGroupFolder);
      }
      return usersGroupFolder;
   }

   @Override
   public String toString() {
      return "OseeGroup [groupName=" + groupName + "]";
   }
}