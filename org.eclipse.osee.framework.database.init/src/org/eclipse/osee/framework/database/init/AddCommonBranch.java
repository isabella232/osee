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

package org.eclipse.osee.framework.database.init;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.OseeSystemArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.UniversalGroup;
import org.eclipse.osee.framework.skynet.core.importing.OseeTypesImport;

/**
 * This class creates the common branch and imports the appropriate skynet types. Class should be extended for plugins
 * that require extra skynet types to be added to common.
 * 
 * @author Donald G. Dunne
 */
public abstract class AddCommonBranch implements IDbInitializationTask {
   private final boolean initializeRootArtifacts;

   public AddCommonBranch() {
      this(true);
   }

   public AddCommonBranch(boolean initializeRootArtifacts) {
      this.initializeRootArtifacts = initializeRootArtifacts;
   }

   public void run() throws OseeCoreException {
      Branch systemBranch = BranchManager.createSystemRootBranch();
      Set<String> registeredTypes = new HashSet<String>();
      registerFrameworkOseeTypes(registeredTypes);
      registerRequiredOseeTypes(registeredTypes);
      OseeTypesImport.execute(registeredTypes);

      if (initializeRootArtifacts) {
         ArtifactTypeManager.addArtifact(OseeSystemArtifacts.ROOT_ARTIFACT_TYPE_NAME, systemBranch,
               OseeSystemArtifacts.DEFAULT_HIERARCHY_ROOT_NAME).persistAttributesAndRelations();
         ArtifactTypeManager.addArtifact(UniversalGroup.ARTIFACT_TYPE_NAME, systemBranch,
               OseeSystemArtifacts.ROOT_ARTIFACT_TYPE_NAME).persistAttributesAndRelations();
      }

      BranchManager.createTopLevelBranch(Branch.COMMON_BRANCH_CONFIG_ID, Branch.COMMON_BRANCH_CONFIG_ID, null);
   }

   /**
    * Clients should register custom OSEE Types by implementing this method
    * 
    * @param registeredTypes
    */
   protected abstract void registerRequiredOseeTypes(Collection<String> registeredTypes);

   private void registerFrameworkOseeTypes(Collection<String> registeredTypes) {
      //registeredTypes.add("org.eclipse.osee.framework.skynet.core.OseeTypes_Framework");
      //registeredTypes.add("org.eclipse.osee.ats.OseeTypes_ATS");
   }

   public boolean canRun() {
      return true;
   }
}
