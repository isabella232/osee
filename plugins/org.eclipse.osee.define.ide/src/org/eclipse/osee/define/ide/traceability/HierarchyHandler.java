/*********************************************************************
 * Copyright (c) 2012 Boeing
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

package org.eclipse.osee.define.ide.traceability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.framework.core.data.BranchToken;
import org.eclipse.osee.framework.core.enums.CoreArtifactTokens;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.skynet.core.OseeSystemArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Roberto E. Escobar
 */
public final class HierarchyHandler {
   private static final Matcher subsystemMatcher = Pattern.compile("(\\w*)\\.ss").matcher("");
   private final Map<String, Artifact> folderNameToArtifact = new HashMap<>(50);
   private final SkynetTransaction transaction;
   private final BranchToken branch;
   private Artifact root;

   public HierarchyHandler(SkynetTransaction transaction) {
      this.transaction = transaction;
      this.branch = BranchManager.getBranchToken(transaction.getBranch());
   }

   public void addArtifact(Artifact testUnit) {
      Conditions.checkExpressionFailOnTrue(!testUnit.isOnBranch(branch), "Artifact [%s] must be on branch [%s]",
         testUnit.toString(), branch.getId());
      Artifact folder = null;

      if (testUnit.isOfType(CoreArtifactTypes.TestCase)) {
         folder = getOrCreateTestCaseFolder();
      } else if (testUnit.isOfType(CoreArtifactTypes.TestSupport)) {
         folder = getOrCreateTestSupportFolder();
      } else if (testUnit.isOfType(CoreArtifactTypes.CodeUnit)) {
         folder = getOrCreateCodeUnitFolder(testUnit.getName());
      } else {
         folder = getOrCreateUnknownTestUnitFolder();
      }

      addChildIfNotRelated(folder, testUnit);
   }

   private Artifact getOrCreateUnknownTestUnitFolder() {
      return getOrCreateTestUnitsFolder("Unknown Test Unit Type", true);
   }

   private Artifact getOrCreateTestSupportFolder() {
      return getOrCreateTestUnitsFolder(CoreArtifactTokens.TestSupportUnitsFolder.getName(), true);
   }

   private Artifact getOrCreateTestCaseFolder() {
      return getOrCreateTestUnitsFolder("Test Cases", true);
   }

   private Artifact getRoot() {
      if (root == null) {
         root = OseeSystemArtifacts.getDefaultHierarchyRootArtifact(branch);
      }
      return root;
   }

   private Artifact getOrCreateCodeUnitFolder(String codeUnitName) {
      Artifact root = getRoot();
      Artifact toReturn = getOrCreateFolder("Code Units", root);

      String subSystem;
      subsystemMatcher.reset(codeUnitName);
      if (subsystemMatcher.find()) {
         subSystem = subsystemMatcher.group(1);
         subSystem = subSystem.toUpperCase();
         toReturn = getOrCreateFolder(subSystem, toReturn);
      }

      return toReturn;
   }

   private Artifact getOrCreateTestUnitsFolder(String subfolderName, boolean includesSubfolder) {
      Artifact root = getRoot();
      Artifact testFolder = getOrCreateFolder("Test", root);
      Artifact testUnitFolder = getOrCreateFolder("Test Units", testFolder);

      if (subfolderName != null && includesSubfolder) {
         Artifact subFolder = getOrCreateFolder(subfolderName, testUnitFolder);
         return subFolder;
      }
      return testUnitFolder;
   }

   private void persistHelper(Artifact toPersist) {
      if (transaction != null) {
         toPersist.persist(transaction);
      }
   }

   private void addChildIfNotRelated(Artifact parentFolder, Artifact childFolder) {
      boolean related = parentFolder.isRelated(CoreRelationTypes.DefaultHierarchical_Child, childFolder);
      if (!related) {
         parentFolder.addChild(childFolder);
         persistHelper(parentFolder);
      }
   }

   private Artifact getOrCreateFolder(String folderName, Artifact parentFolder) {
      Artifact toReturn = folderNameToArtifact.get(folderName);
      if (toReturn == null) {
         List<Artifact> relatedFolders =
            ArtifactQuery.getArtifactListFromTypeAndName(CoreArtifactTypes.Folder, folderName, branch);
         if (relatedFolders.size() == 1) {
            toReturn = relatedFolders.iterator().next();
         } else if (relatedFolders.size() > 1) {
            for (Artifact folder : relatedFolders) {
               if (parentFolder.isRelated(CoreRelationTypes.DefaultHierarchical_Child, folder)) {
                  toReturn = folder;
                  break;
               }
            }
         }
         if (toReturn == null) {
            toReturn = ArtifactTypeManager.addArtifact(CoreArtifactTypes.Folder, branch, folderName);
            parentFolder.addChild(toReturn);
            toReturn.persist(transaction);
         }
         folderNameToArtifact.put(folderName, toReturn);
      }
      return toReturn;
   }
}