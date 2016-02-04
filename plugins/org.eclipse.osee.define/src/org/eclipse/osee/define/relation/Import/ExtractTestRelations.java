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
package org.eclipse.osee.define.relation.Import;

import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.osee.define.internal.Activator;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.MultipleArtifactsExist;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.text.FindResults;
import org.eclipse.osee.framework.jdk.core.text.tool.Find;
import org.eclipse.osee.framework.jdk.core.text.tool.FindResultsIterator;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.ws.AWorkspace;

public class ExtractTestRelations {
   private final String scriptsDir;
   private final String fileNamePattern;
   private final Branch branch;
   private static final Matcher testScriptMatcher = Pattern.compile("doTestCase").matcher("");
   private static final Matcher traceabilityMatcher = Pattern.compile("RequirementId\\(\\\"([^\\\"]+)\\\"").matcher("");

   public ExtractTestRelations(String scriptsDir, String fileNamePattern, Branch branch) {
      super();
      this.scriptsDir = scriptsDir;
      this.fileNamePattern = fileNamePattern;
      this.branch = branch;
   }

   public void run() {
      ArrayList<String> patterns = new ArrayList<>();
      patterns.add(scriptsDir);
      Find app = new Find("RequirementId\\(\\\"([^\\\"]+)\\\"", new File(scriptsDir), fileNamePattern);
      app.find(999999, true);
      FindResults results = app.getResults();

      for (FindResultsIterator i = results.iterator(); i.hasNext();) {
         try {
            addRelationToDatabaseIfNotAlreadyThere(AWorkspace.fileToIFile(i.currentFile), i.currentRegion);
         } catch (Exception ex) {
            ex.printStackTrace();
         }
      }

   }

   public static void traceabilityReport() throws IOException {
      Pattern javaFilePattern = Pattern.compile(".*\\.java");
      for (IProject project : AWorkspace.getProjects()) {
         File projectLocation = project.getLocation().toFile();
         for (File javaFile : Lib.recursivelyListFiles(projectLocation, javaFilePattern)) {
            CharBuffer buf = Lib.fileToCharBuffer(javaFile);
            testScriptMatcher.reset(buf);
            if (testScriptMatcher.find()) {
               traceabilityMatcher.reset(buf);
               while (traceabilityMatcher.find()) {
                  System.out.println(javaFile.getName() + ", " + traceabilityMatcher.group(1));
               }
            } else {
               System.out.println(javaFile + ": no traceability");
            }
         }
      }
   }

   private void addRelationToDatabaseIfNotAlreadyThere(IFile testArtifactFile, String reqArtifactName) throws OseeCoreException {

      // Make sure that the runtime relation type is available
      Artifact reqArtifact =
         ArtifactQuery.getArtifactFromTypeAndName(CoreArtifactTypes.SoftwareRequirement, reqArtifactName, branch);

      // Make sure we have the reqArtifact
      if (reqArtifact == null) {
         System.out.println("Unable to locate the requirement named:\"" + reqArtifactName + "\"");
         return;
      }

      // TODO replace the null here with the Test_TestSide relationSide enumeration
      reqArtifact.addRelation(null, getTestArtifact(testArtifactFile, reqArtifact.getBranch()));
      // Get a new test relation with the same tag as the requirement artifact
      //      UserRelation testRelation = relationManager.getUserRelationDescriptor("Test").makeNewUserRelation(reqArtifact.getTag());
      //      RelationLink link = relationManager.getRelationLinkDescriptor("Test").makeNewLink();

      // Add the items to the relation
      //      link.setArtifact("Requirement", reqArtifact);
      //      link.setArtifact("Test", getTestArtifact(testArtifactName, reqArtifact.getTag()));
      //      reqArtifact.getLinkManager().addLink(link);
      //
      //      link.setArtifactA(reqArtifact);
      //      link.setArtifactB(getTestArtifact(testArtifactName, reqArtifact.getTag()));
      //      link.persist(getClass().getSimpleName());

      //      testRelation.addArtifact("test", getTestArtifact(testArtifactName, reqArtifact.getTag()));
      //      testRelation.addArtifact("requirement", reqArtifact);

      // Save the relation
      //      link.persist(getClass().getSimpleName());
   }

   private Artifact getTestArtifact(IFile testArtifactFile, BranchId branch) throws OseeCoreException {
      try {
         return ArtifactQuery.getArtifactFromTypeAndName(CoreArtifactTypes.TestCase, testArtifactFile.getName(),
            branch);
      } catch (MultipleArtifactsExist ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return null;
      } catch (ArtifactDoesNotExist ex) {
         Artifact testArtifact =
            ArtifactTypeManager.addArtifact(CoreArtifactTypes.TestCase, branch, testArtifactFile.getName());
         testArtifact.setSoleAttributeValue(CoreAttributeTypes.ContentUrl, testArtifactFile.getFullPath().toString());
         testArtifact.persist(getClass().getSimpleName());
         return testArtifact;
      }
   }
}
