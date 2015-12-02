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
package org.eclipse.osee.client.integration.tests.integration.ui.skynet;

import static org.eclipse.osee.client.demo.DemoChoice.OSEE_CLIENT_DEMO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.client.test.framework.OseeClientIntegrationRule;
import org.eclipse.osee.client.test.framework.OseeLogMonitorRule;
import org.eclipse.osee.client.test.framework.TestInfo;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.OseeSystemArtifacts;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.linking.LinkType;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.ui.skynet.preferences.MsWordPreferencePage;
import org.eclipse.osee.framework.ui.skynet.render.IRenderer;
import org.eclipse.osee.framework.ui.skynet.render.ITemplateRenderer;
import org.eclipse.osee.framework.ui.skynet.render.WordTemplateRenderer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Mark Joy
 * @link: WordTemplateRenderer
 */

public class WordTemplateRendererTest {

   @Rule
   public OseeClientIntegrationRule integration = new OseeClientIntegrationRule(OSEE_CLIENT_DEMO);

   @Rule
   public OseeLogMonitorRule monitorRule = new OseeLogMonitorRule();

   @Rule
   public TestInfo method = new TestInfo();

   private static final String beginWordString = "<w:p><w:r><w:t>";
   private static final String endWordString = "</w:t></w:r></w:p>";
   private static final String beginLinkInsert = "</w:t></w:r>OSEE_LINK(";
   private static final String endLinkInsert = ")<w:r><w:t>";
   private static final String tabString = "wx:wTabBefore=\"540\" wx:wTabAfter=\"90\"";
   private static final Pattern findSetRsidR = Pattern.compile("wsp:rsidR=\".*?\"", Pattern.DOTALL | Pattern.MULTILINE);
   private static final Pattern findSetRsidRDefault = Pattern.compile("wsp:rsidRDefault=\".*?\"",
      Pattern.DOTALL | Pattern.MULTILINE);
   private static final Pattern findBlankPage = Pattern.compile("This page is intentionally left blank");
   private static final Pattern findHlinks = Pattern.compile("<w:hlink w:dest=\".*?\"",
      Pattern.DOTALL | Pattern.MULTILINE);

   private static String RECURSE_TEMPLATE_STRING;
   private static String SINGLE_TEMPLATE_STRING;
   private static String SINGLE_TEMPLATE_WITH_ATTRIBUTES_STRING;

   private static String MASTER_TEMPLATE_STRING;
   private static String MASTER_TEMPLATE_STRING_IDONLY;
   private static String MASTER_TEMPLATE_STRING_IDANDNAME;
   private static String SLAVE_TEMPLATE_STRING;

   private IOseeBranch rootBranch;
   private IOseeBranch updateBranch;
   private Artifact docFolder;
   private Artifact swReqFolder;
   private Artifact templateFolder;
   private Artifact recurseTemplate;
   private Artifact singleTemplate;
   private Artifact singleTemplateAttrib;
   private Artifact masterTemplate;
   private Artifact masterTemplate_idOnly;
   private Artifact masterTemplate_idAndName;
   private Artifact slaveTemplate;

   private final WordTemplateRenderer renderer = new WordTemplateRenderer();
   private Object[] options;

   @BeforeClass
   public static void loadTemplateInfo() throws Exception {
      RECURSE_TEMPLATE_STRING = getResourceData("wordrenderer_recurse.xml");
      SINGLE_TEMPLATE_STRING = getResourceData("wordrenderer_single.xml");
      SINGLE_TEMPLATE_WITH_ATTRIBUTES_STRING = getResourceData("wordrenderer_single_attrib.xml");
      MASTER_TEMPLATE_STRING = getResourceData("wordrenderer_master.xml");
      MASTER_TEMPLATE_STRING_IDONLY = getResourceData("wordrenderer_master-idonly.xml");
      MASTER_TEMPLATE_STRING_IDANDNAME = getResourceData("wordrenderer_master-idandname.xml");
      SLAVE_TEMPLATE_STRING = getResourceData("wordrenderer_slave.xml");
   }

   @Before
   public void setUp() throws OseeCoreException {
      // Establish default option settings
      options =
         new Object[] {
            "Branch",
            null,
            "compareBranch",
            null,
            "Publish As Diff",
            true,
            "linkType",
            LinkType.INTERNAL_DOC_REFERENCE_USE_NAME,
            WordTemplateRenderer.UPDATE_PARAGRAPH_NUMBER_OPTION,
            false,
            ITemplateRenderer.TRANSACTION_OPTION,
            null,
            IRenderer.SKIP_ERRORS,
            true,
            "Exclude Folders",
            true,
         "EXCLUDE ARTIFACT TYPES",
         new ArrayList<IArtifactType>(),
            "Recurse On Load",
            true,
            "Maintain Order",
            true,
            ITemplateRenderer.USE_TEMPLATE_ONCE,
            true,
            WordTemplateRenderer.FIRST_TIME,
            true,
            IRenderer.NO_DISPLAY,
            true};

      String branchName = method.getQualifiedTestName();
      rootBranch = BranchManager.createTopLevelBranch(branchName);
      Artifact programRoot = OseeSystemArtifacts.getDefaultHierarchyRootArtifact(rootBranch);

      templateFolder = ArtifactTypeManager.addArtifact(CoreArtifactTypes.Folder, rootBranch, "Templates");
      swReqFolder = ArtifactTypeManager.addArtifact(CoreArtifactTypes.Folder, rootBranch, "Software Requirements");
      docFolder = ArtifactTypeManager.addArtifact(CoreArtifactTypes.Folder, rootBranch, "Document Folder");

      programRoot.addChild(docFolder);
      programRoot.addChild(templateFolder);
      programRoot.addChild(swReqFolder);

      setupTemplates(templateFolder, rootBranch);
      templateFolder.persist("TEMPLATE FOLDER SETUP");

      setUpDocFolder(docFolder, rootBranch);
      docFolder.persist("DOCUMENT FOLDER SETUP");

      setUpSWReq(swReqFolder, rootBranch);
      swReqFolder.persist("SOFTWARE REQUIREMENTS SETUP");

      String workingBranchName = String.format("%s.child_branch", method.getQualifiedTestName());
      updateBranch = BranchManager.createWorkingBranch(rootBranch, workingBranchName);
      setUpDocChanges(docFolder);
   }

   @After
   public void tearDown() throws Exception {
      if (BranchManager.branchExists(updateBranch)) {
         BranchManager.purgeBranch(updateBranch);
      }
      if (BranchManager.branchExists(rootBranch)) {
         BranchManager.purgeBranch(rootBranch);
      }
   }

   @Test
   public void testBlankWordTemplateContent() {
      IOseeBranch rootBr = BranchManager.createTopLevelBranch("Root Branch");
      SkynetTransaction tx =
         TransactionManager.createTransaction(rootBr, String.format("%s", method.getQualifiedTestName()));
      Artifact vol4 = ArtifactTypeManager.addArtifact(CoreArtifactTypes.HeadingMSWord, rootBr, "Volume 4");
      vol4.setSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, "1");
      vol4.persist(tx);
      Artifact introArt = ArtifactTypeManager.addArtifact(CoreArtifactTypes.HeadingMSWord, rootBr, "Intro");
      introArt.setSoleAttributeFromString(CoreAttributeTypes.WordTemplateContent, "blah");

      vol4.addChild(introArt);
      introArt.persist(tx);
      tx.execute();

      Branch middleBr = BranchManager.createWorkingBranch(rootBr, "Middle Branch");
      Artifact middleVol4 = ArtifactQuery.getArtifactFromId(vol4.getGuid(), middleBr);
      middleVol4.setSoleAttributeFromString(CoreAttributeTypes.WordTemplateContent, " ");
      middleVol4.persist("added blank content");

      Branch childBr = BranchManager.createWorkingBranch(middleBr, "Child Branch");
      vol4 = ArtifactQuery.getArtifactFromId(vol4.getGuid(), childBr);

      modifyOption("Branch", childBr);
      modifyOption("Publish As Diff", true);
      modifyOption("compareBranch", rootBr);

      renderer.publish(singleTemplate, null, Collections.singletonList(vol4), options);

      String resultPath = renderer.getStringOption(IRenderer.RESULT_PATH_RETURN);
      Assert.assertNotEquals(String.format("%s Published Doc not found", method.getQualifiedTestName()), resultPath,
         null);
      try {
         String document = getFileAsString(resultPath);
         String testName = method.getQualifiedTestName();
         String altString = "  \"";
         String period = ".";

         Assert.assertTrue(
            String.format("%s, Expected 1. Volume 4", testName),
            document.contains("<wx:t wx:val=\"1" + period + altString + "/><wx:font wx:val=\"Times New Roman\"/></w:listPr></w:pPr><w:r><w:t>Volume 4</w:t></w:r>"));

         Assert.assertTrue(
            String.format("%s, Expected 2.", testName),
            document.contains("<wx:t wx:val=\"2" + period + altString + "/><wx:font wx:val=\"Times New Roman\"/></w:listPr></w:pPr><w:r>"));

         // This is a separate check due to the wordMl.resetListValue(); function injecting extra wordML that resets the list numbering
         Assert.assertTrue(String.format("%s, Expected Intro", testName), document.contains("<w:t>Intro</w:t></w:r>"));
      } catch (IOException ex) {
         // Do nothing - test failed
      }
   }

   @Test
   public void testPublishWithoutDiff() throws OseeCoreException {
      modifyOption("Branch", updateBranch);
      modifyOption("Publish As Diff", false);
      List<Artifact> artifacts = new ArrayList<>();
      artifacts.add(docFolder);
      renderer.publish(singleTemplate, null, artifacts, options);

      String resultPath = renderer.getStringOption(IRenderer.RESULT_PATH_RETURN);
      Assert.assertNotEquals(String.format("%s Published Doc not found", method.getQualifiedTestName()), resultPath,
         null);
      try {
         String contents = getFileAsString(resultPath);
         basicDocumentCheck(contents, tabString, false, false);
      } catch (IOException ex) {
         // Do nothing - test failed
      }
   }

   @Test
   public void testPublishWithDiff() throws OseeCoreException {
      modifyOption("Branch", updateBranch);
      modifyOption("Publish As Diff", true);
      List<Artifact> artifacts = new ArrayList<>();
      Artifact updateDoc = ArtifactQuery.getArtifactFromId(docFolder.getArtId(), updateBranch);
      artifacts.add(updateDoc);
      renderer.publish(singleTemplate, null, artifacts, options);

      String resultPath = renderer.getStringOption(IRenderer.RESULT_PATH_RETURN);
      Assert.assertNotEquals(String.format("%s Published Doc not found", method.getQualifiedTestName()), resultPath,
         null);
      try {
         String contents = getFileAsString(resultPath);
         basicDocumentCheck(contents, "", false, false);
      } catch (IOException ex) {
         // Do nothing - test failed
      }
   }

   @Test
   public void testPublishWithoutDiffRecurseTemplate() throws OseeCoreException {
      modifyOption("Branch", updateBranch);
      modifyOption("Publish As Diff", false);
      List<Artifact> artifacts = new ArrayList<>();
      artifacts.add(docFolder);
      renderer.publish(recurseTemplate, null, artifacts, options);

      String resultPath = renderer.getStringOption(IRenderer.RESULT_PATH_RETURN);
      Assert.assertNotEquals(String.format("%s Published Doc not found", method.getQualifiedTestName()), resultPath,
         null);
      try {
         String contents = getFileAsString(resultPath);
         basicDocumentCheck(contents, tabString, false, false);
      } catch (IOException ex) {
         // Do nothing - test failed
      }
   }

   @Test
   public void testPublishWithDiffRecurseTemplate() throws OseeCoreException {
      modifyOption("Branch", updateBranch);
      modifyOption("Publish As Diff", true);
      List<Artifact> artifacts = new ArrayList<>();
      Artifact updateDoc = ArtifactQuery.getArtifactFromId(docFolder.getArtId(), updateBranch);
      artifacts.add(updateDoc);
      renderer.publish(recurseTemplate, null, artifacts, options);

      String resultPath = renderer.getStringOption(IRenderer.RESULT_PATH_RETURN);
      Assert.assertNotEquals(String.format("%s Published Doc not found", method.getQualifiedTestName()), resultPath,
         null);
      try {
         String contents = getFileAsString(resultPath);
         basicDocumentCheck(contents, "", false, false);
      } catch (IOException ex) {
         // Do nothing - test failed
      }
   }

   @Test
   public void testPublishWithDiffMerge() throws OseeCoreException {
      modifyOption("Branch", updateBranch);
      modifyOption("Publish As Diff", true);
      modifyOption("compareBranch", rootBranch);
      modifyOption("linkType", LinkType.INTERNAL_DOC_REFERENCE_USE_PARAGRAPH_NUMBER);
      List<Artifact> artifacts = new ArrayList<>();
      Artifact updateDoc = ArtifactQuery.getArtifactFromId(docFolder.getArtId(), updateBranch);
      artifacts.add(updateDoc);
      renderer.publish(singleTemplate, null, artifacts, options);

      String resultPath = renderer.getStringOption(IRenderer.RESULT_PATH_RETURN);
      Assert.assertNotEquals(String.format("%s Published Doc not found", method.getQualifiedTestName()), resultPath,
         null);
      String contents;
      try {
         contents = getFileAsString(resultPath);

         // either one of these strings could be correct depending on word preferences
         String mergeContent1 =
            "<aml:content><w:r><w:t>paragraph describes</w:t></w:r><w:r><w:t>is</w:t></w:r></aml:content>";
         String mergeContent2 =
            "<aml:content><w:r><w:t>paragraph </w:t></w:r><w:proofErr w:type=\"spellStart\"/><w:r><w:t>describes</w:t></w:r><w:r><w:t>is</w:t></w:r></aml:content>";
         Assert.assertTrue("Merge content not found",
            contents.contains(mergeContent1) || contents.contains(mergeContent2));

         Assert.assertTrue("Paragraph Number only Link not found",
            contents.contains("<w:r><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>2.1</w:t></w:r>"));
         basicDocumentCheck(contents, "", true, false);
      } catch (IOException ex) {
         // Do nothing - test failed
      }
   }

   @Test
   public void testPublishWithDiffLinks() throws OseeCoreException {
      modifyOption("Branch", updateBranch);
      modifyOption("Publish As Diff", true);
      modifyOption("compareBranch", null);
      modifyOption("linkType", LinkType.INTERNAL_DOC_REFERENCE_USE_PARAGRAPH_NUMBER_AND_NAME);
      List<Artifact> artifacts = new ArrayList<>();
      Artifact updateDoc = ArtifactQuery.getArtifactFromId(docFolder.getArtId(), updateBranch);
      artifacts.add(updateDoc);
      renderer.publish(singleTemplateAttrib, null, artifacts, options);

      String resultPath = renderer.getStringOption(IRenderer.RESULT_PATH_RETURN);
      Assert.assertNotEquals(String.format("%s Published Doc not found", method.getQualifiedTestName()), resultPath,
         null);
      String contents;
      try {
         contents = getFileAsString(resultPath);
         Assert.assertTrue("Paragraph Number & Name Link not found",
            contents.contains("<w:r><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>2.1 Hardware</w:t></w:r>"));
         basicDocumentCheck(contents, "", false, false);
         // Need to replace word created ids with something consistent for testing
         Matcher m = findSetRsidR.matcher(contents);
         while (m.find()) {
            String rev = m.group();
            contents = contents.replace(rev, "wsp:rsidR=\"TESTING\"");
         }
         m = findSetRsidRDefault.matcher(contents);
         while (m.find()) {
            String rev = m.group();
            contents = contents.replace(rev, "wsp:rsidRDefault=\"TESTING\"");
         }
         Assert.assertTrue(
            "Original Paragram Numbering for Notes is incorrect",
            contents.contains("<w:r><w:t>Notes</w:t></w:r></w:p><w:p wsp:rsidR=\"TESTING\" wsp:rsidRDefault=\"TESTING\"><w:r><w:t> Paragraph Number: 3</w:t></w:r>"));
         Assert.assertTrue(
            "Original Paragram Numbering for More Notes is incorrect",
            contents.contains("<w:r><w:t>More Notes</w:t></w:r></w:p><w:p wsp:rsidR=\"TESTING\" wsp:rsidRDefault=\"TESTING\"><w:r><w:t> Paragraph Number: 3.1</w:t></w:r>"));
         m = findBlankPage.matcher(contents);
         int counter = 0;
         while (m.find()) {
            counter++;
         }
         Assert.assertEquals("Number of blank pages should only be 1", 1, counter);
      } catch (IOException ex) {
         // Do nothing - test failed
      }
   }

   @Test
   public void testPublishWithoutDiffUpdateParagraphNumbers() throws OseeCoreException {
      SkynetTransaction transaction =
         TransactionManager.createTransaction(updateBranch, String.format("%s", method.getQualifiedTestName()));
      modifyOption("Branch", updateBranch);
      modifyOption(ITemplateRenderer.TRANSACTION_OPTION, transaction);
      modifyOption("Publish As Diff", false);
      modifyOption("linkType", LinkType.INTERNAL_DOC_REFERENCE_USE_PARAGRAPH_NUMBER_AND_NAME);
      modifyOption(WordTemplateRenderer.UPDATE_PARAGRAPH_NUMBER_OPTION, true);
      List<Artifact> artifacts = new ArrayList<>();
      Artifact updateDoc = ArtifactQuery.getArtifactFromId(docFolder.getArtId(), updateBranch);
      artifacts.add(updateDoc);
      renderer.publish(singleTemplateAttrib, null, artifacts, options);

      String resultPath = renderer.getStringOption(IRenderer.RESULT_PATH_RETURN);
      Assert.assertNotEquals(String.format("%s Published Doc not found", method.getQualifiedTestName()), resultPath,
         null);
      String contents;
      try {
         contents = getFileAsString(resultPath);
         Assert.assertTrue("Paragraph Number & Name Link not found",
            contents.contains("<w:r><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>2.1 Hardware</w:t></w:r>"));
         basicDocumentCheck(contents, tabString, false, false);
         Assert.assertTrue(
            "Paragraph Number 2 is not updated",
            contents.contains("<w:r><w:t>Notes</w:t></w:r></w:p><w:p><w:r><w:t> Paragraph Number: </w:t></w:r><w:r><w:t>2</w:t></w:r>"));
         Assert.assertTrue(
            "Paragraph Number 2.1 is not updated",
            contents.contains("<w:r><w:t>More Notes</w:t></w:r></w:p><w:p><w:r><w:t> Paragraph Number: </w:t></w:r><w:r><w:t>2.1</w:t></w:r>"));
         Matcher m = findBlankPage.matcher(contents);
         int counter = 0;
         while (m.find()) {
            counter++;
         }
         Assert.assertEquals("Number of blank pages should only be 1", 1, counter);
      } catch (IOException ex) {
         // Do nothing - test failed
      }
   }

   @Test
   public void testPublishWithDiffDontUseTemplateOnce() throws OseeCoreException {
      modifyOption("Branch", updateBranch);
      modifyOption("Publish As Diff", true);
      modifyOption("linkType", LinkType.INTERNAL_DOC_REFERENCE_USE_PARAGRAPH_NUMBER_AND_NAME);
      modifyOption(ITemplateRenderer.USE_TEMPLATE_ONCE, false);
      List<Artifact> artifacts = new ArrayList<>();
      Artifact updateDoc = ArtifactQuery.getArtifactFromId(docFolder.getArtId(), updateBranch);
      artifacts.add(updateDoc);
      renderer.publish(singleTemplateAttrib, null, artifacts, options);

      String resultPath = renderer.getStringOption(IRenderer.RESULT_PATH_RETURN);
      Assert.assertNotEquals(String.format("%s Published Doc not found", method.getQualifiedTestName()), resultPath,
         null);
      String contents;
      try {
         contents = getFileAsString(resultPath);
         basicDocumentCheck(contents, "", false, false);
         Matcher m = findBlankPage.matcher(contents);
         int counter = 0;
         while (m.find()) {
            counter++;
         }
         Assert.assertTrue("Number of blank pages not found", (counter >= 10));

      } catch (IOException ex) {
         // Do nothing - test failed
      }
   }

   @Test
   public void testPublishSoftwareRequirements() throws OseeCoreException {
      modifyOption("Branch", updateBranch);
      modifyOption("Publish As Diff", false);
      List<Artifact> artifacts = new ArrayList<>();
      artifacts.add(swReqFolder);
      renderer.publish(masterTemplate, slaveTemplate, artifacts, options);

      String resultPath = renderer.getStringOption(IRenderer.RESULT_PATH_RETURN);
      Assert.assertNotEquals(String.format("%s Published Doc not found", method.getQualifiedTestName()), resultPath,
         null);
      String contents;
      try {
         contents = getFileAsString(resultPath);
         Matcher m = findHlinks.matcher(contents);
         int counter = 0;
         int indx = resultPath.lastIndexOf(File.separator);
         String justPath = resultPath.substring(0, indx + 1);
         while (m.find()) {
            String hfile = m.group();
            hfile = hfile.substring(17, (hfile.length() - 1));
            File testFile = new File(justPath + hfile);
            Assert.assertTrue(String.format("File does not exist %s", testFile), testFile.exists());
            counter++;
         }
         Assert.assertTrue("Did not find links to 3 files.", (counter == 3));

      } catch (IOException ex) {
         // Do nothing - test failed
      }
   }

   @Test
   public void testPublishUsingIds() throws OseeCoreException {
      modifyOption("Branch", updateBranch);
      modifyOption("Publish As Diff", false);
      List<Artifact> artifacts = new ArrayList<>();
      artifacts.add(swReqFolder);
      renderer.publish(masterTemplate_idOnly, slaveTemplate, artifacts, options);

      String resultPath = renderer.getStringOption(IRenderer.RESULT_PATH_RETURN);
      Assert.assertNotEquals(String.format("%s Published Doc not found", method.getQualifiedTestName()), resultPath,
         null);
      String contents;
      try {
         contents = getFileAsString(resultPath);
         Matcher m = findHlinks.matcher(contents);
         int counter = 0;
         int indx = resultPath.lastIndexOf(File.separator);
         String justPath = resultPath.substring(0, indx + 1);
         while (m.find()) {
            String hfile = m.group();
            hfile = hfile.substring(17, (hfile.length() - 1));
            File testFile = new File(justPath + hfile);
            Assert.assertTrue(String.format("File does not exist %s", testFile), testFile.exists());
            counter++;
         }
         Assert.assertTrue("Did not find links to 3 files.", (counter == 3));

      } catch (IOException ex) {
         // Do nothing - test failed
      }
   }

   @Test
   public void testPublishUsingIdAndName() throws OseeCoreException {
      modifyOption("Branch", updateBranch);
      modifyOption("Publish As Diff", false);
      List<Artifact> artifacts = new ArrayList<>();
      artifacts.add(swReqFolder);
      renderer.publish(masterTemplate_idAndName, slaveTemplate, artifacts, options);

      String resultPath = renderer.getStringOption(IRenderer.RESULT_PATH_RETURN);
      Assert.assertNotEquals(String.format("%s Published Doc not found", method.getQualifiedTestName()), resultPath,
         null);
      String contents;
      try {
         contents = getFileAsString(resultPath);
         Matcher m = findHlinks.matcher(contents);
         int counter = 0;
         int indx = resultPath.lastIndexOf(File.separator);
         String justPath = resultPath.substring(0, indx + 1);
         while (m.find()) {
            String hfile = m.group();
            hfile = hfile.substring(17, (hfile.length() - 1));
            File testFile = new File(justPath + hfile);
            Assert.assertTrue(String.format("File does not exist %s", testFile), testFile.exists());
            counter++;
         }
         Assert.assertTrue("Did not find links to 3 files.", (counter == 3));

      } catch (IOException ex) {
         // Do nothing - test failed
      }
   }

   @Test
   public void testPublishDiffWithFieldCodes() throws OseeCoreException {
      modifyOption("Branch", updateBranch);
      modifyOption("Publish As Diff", true);
      List<Artifact> artifacts = new ArrayList<>();
      setupFieldCodeChange();
      Artifact updateDoc = ArtifactQuery.getArtifactFromId(docFolder.getArtId(), updateBranch);
      artifacts.add(updateDoc);
      renderer.publish(singleTemplate, null, artifacts, options);

      String resultPath = renderer.getStringOption(IRenderer.RESULT_PATH_RETURN);
      Assert.assertNotEquals(String.format("%s Published Doc not found", method.getQualifiedTestName()), resultPath,
         null);
      try {
         String contents = getFileAsString(resultPath);
         basicDocumentCheck(contents, "", false, true);
         Assert.assertTrue(
            "Field Code Diff not as expected",
            contents.contains("<w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>Hardware</w:t></w:r><aml:annotation aml:id=\"8\" w:type=\"Word.Insertion\""));
         Assert.assertTrue(
            "Field Code Diff not as expected",
            contents.contains("<aml:content><w:r><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t> Functions</w:t></w:r></aml:content></aml:annotation>"));
      } catch (IOException ex) {
         // Do nothing - test failed
      }
   }

   @Test
   public void testPublishDiffWithOutFieldCodes() throws OseeCoreException {
      modifyOption("Branch", updateBranch);
      modifyOption("Publish As Diff", true);
      List<Artifact> artifacts = new ArrayList<>();
      setupFieldCodeChange();
      UserManager.setSetting(MsWordPreferencePage.IGNORE_FIELD_CODE_CHANGES, "true");
      Artifact updateDoc = ArtifactQuery.getArtifactFromId(docFolder.getArtId(), updateBranch);
      artifacts.add(updateDoc);
      renderer.publish(singleTemplate, null, artifacts, options);

      String resultPath = renderer.getStringOption(IRenderer.RESULT_PATH_RETURN);
      Assert.assertNotEquals(String.format("%s Published Doc not found", method.getQualifiedTestName()), resultPath,
         null);
      try {
         String contents = getFileAsString(resultPath);
         basicDocumentCheck(contents, "", false, true);
         Assert.assertTrue(
            "Appears to have Field Code Diff",
            contents.contains("<w:r><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>Hardware Functions</w:t></w:r></w:hlink>"));
      } catch (IOException ex) {
         // Do nothing - test failed
      } finally {
         UserManager.setSetting(MsWordPreferencePage.IGNORE_FIELD_CODE_CHANGES, "false");
      }
   }

   // Create the folder to store the templates
   private void setupTemplates(Artifact folder, IOseeBranch branch) throws OseeCoreException {
      recurseTemplate = ArtifactTypeManager.addArtifact(CoreArtifactTypes.RendererTemplate, branch, "Recurse Template");
      recurseTemplate.setSoleAttributeValue(CoreAttributeTypes.WholeWordContent, RECURSE_TEMPLATE_STRING);
      recurseTemplate.addAttributeFromString(CoreAttributeTypes.TemplateMatchCriteria,
         "org.eclipse.osee.framework.ui.skynet.word PREVIEW PREVIEW_WITH_RECURSE_NO_ATTRIBUTES");
      recurseTemplate.addAttributeFromString(CoreAttributeTypes.TemplateMatchCriteria,
         "org.eclipse.osee.framework.ui.skynet.render.WordTemplateRenderer PREVIEW PREVIEW_WITH_RECURSE_NO_ATTRIBUTES");
      singleTemplate = ArtifactTypeManager.addArtifact(CoreArtifactTypes.RendererTemplate, branch, "Single Template");
      singleTemplate.setSoleAttributeValue(CoreAttributeTypes.WholeWordContent, SINGLE_TEMPLATE_STRING);
      singleTemplateAttrib =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.RendererTemplate, branch, "Single With Attributes");
      singleTemplateAttrib.setSoleAttributeValue(CoreAttributeTypes.WholeWordContent,
         SINGLE_TEMPLATE_WITH_ATTRIBUTES_STRING);
      masterTemplate =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.RendererTemplate, branch, "srsMaster Template");
      masterTemplate.setSoleAttributeFromString(CoreAttributeTypes.WholeWordContent, MASTER_TEMPLATE_STRING);
      masterTemplate_idOnly =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.RendererTemplate, branch, "srsMaster Template ID only");
      masterTemplate_idOnly.setSoleAttributeFromString(CoreAttributeTypes.WholeWordContent,
         MASTER_TEMPLATE_STRING_IDONLY);
      masterTemplate_idAndName =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.RendererTemplate, branch, "srsMaster Template ID and name");
      masterTemplate_idAndName.setSoleAttributeFromString(CoreAttributeTypes.WholeWordContent,
         MASTER_TEMPLATE_STRING_IDANDNAME);

      slaveTemplate = ArtifactTypeManager.addArtifact(CoreArtifactTypes.RendererTemplate, branch, "srsSlave Template");
      slaveTemplate.setSoleAttributeFromString(CoreAttributeTypes.WholeWordContent, SLAVE_TEMPLATE_STRING);

      folder.addChild(recurseTemplate);
      folder.addChild(singleTemplate);
      folder.addChild(singleTemplateAttrib);
      folder.addChild(masterTemplate);
      folder.addChild(masterTemplate_idOnly);
      folder.addChild(masterTemplate_idAndName);
      folder.addChild(slaveTemplate);
   }

   // Create the SW Requirement test artifact structure
   //@formatter:off
   /*
    Software Requirements
    |
    |----Crew Station Requirements
         |---- Communication Subsystem Crew Interface
         |---- Navigation Subsystem Crew Interface
         |---- Aircraft Systems Management Subsystem Crew Interface
         |     |---- Aircraft Drawing
         |     |---- Ventilation
    */
   //@formatter:on
   private void setUpSWReq(Artifact swReqFolder, IOseeBranch branch) throws OseeCoreException {
      Artifact crewReq =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.HeadingMSWord, branch, "Crew Station Requirements");
      Artifact commReq =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, branch,
            "Communication Subsystem Crew Interface");
      Artifact navReq =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, branch,
            "Navigation Subsystem Crew Interface");
      Artifact airReq =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.HeadingMSWord, branch,
            "Aircraft Systems Management Subsystem Crew Interface");
      Artifact airDrawReq =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.HeadingMSWord, branch, "Aircraft Drawing");
      Artifact ventReq = ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, branch, "Ventilation");

      swReqFolder.addChild(crewReq);
      crewReq.addChild(RelationOrderBaseTypes.USER_DEFINED, commReq);
      crewReq.addChild(RelationOrderBaseTypes.USER_DEFINED, navReq);
      crewReq.addChild(RelationOrderBaseTypes.USER_DEFINED, airReq);
      airReq.addChild(RelationOrderBaseTypes.USER_DEFINED, airDrawReq);
      airReq.addChild(RelationOrderBaseTypes.USER_DEFINED, ventReq);

      commReq.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent,
         beginWordString + "This is the list of Communication crew station requirements." + endWordString);
      navReq.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent,
         beginWordString + "This is the list of Navigation crew station requirements." + endWordString);
      airReq.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent,
         beginWordString + "This is the list of Aircraft Management crew station requirements." + endWordString);
      ventReq.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent,
         beginWordString + "This is the Ventilation crew station requirements." + endWordString);
   }

   // Create the generic test document artifact structure
   //@formatter:off
   /*
    Document Folder
    |
    |----Introduction
         |---- Background
         |---- Scope
         |  
         Subsystem
         |---- Hardware
         |     |---- Hardware Functions
         |---- Software
         |     |---- Software Functions
         |
         Notes
         |---- More Notes
    */
   //@formatter:on
   private void setUpDocFolder(Artifact docFolder, IOseeBranch branch) throws OseeCoreException {
      Artifact intro = ArtifactTypeManager.addArtifact(CoreArtifactTypes.HeadingMSWord, branch, "Introduction");
      Artifact background = ArtifactTypeManager.addArtifact(CoreArtifactTypes.SubsystemDesign, branch, "Background");
      Artifact scope = ArtifactTypeManager.addArtifact(CoreArtifactTypes.SubsystemDesign, branch, "Scope");
      Artifact subSystem = ArtifactTypeManager.addArtifact(CoreArtifactTypes.SubsystemDesign, branch, "Subsystem");
      Artifact hardware = ArtifactTypeManager.addArtifact(CoreArtifactTypes.HeadingMSWord, branch, "Hardware");
      Artifact hardwareFunc =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.HardwareRequirement, branch, "Hardware Functions");
      Artifact software = ArtifactTypeManager.addArtifact(CoreArtifactTypes.HeadingMSWord, branch, "Software");
      Artifact softwareFunc =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareDesign, branch, "Software Functions");
      Artifact notes = ArtifactTypeManager.addArtifact(CoreArtifactTypes.HeadingMSWord, branch, "Notes");
      Artifact morenotes = ArtifactTypeManager.addArtifact(CoreArtifactTypes.HeadingMSWord, branch, "More Notes");

      docFolder.addChild(intro);
      intro.addChild(background);
      intro.addChild(scope);
      docFolder.addChild(subSystem);
      subSystem.addChild(hardware);
      hardware.addChild(hardwareFunc);
      subSystem.addChild(software);
      software.addChild(softwareFunc);
      docFolder.addChild(notes);
      notes.addChild(morenotes);

      intro.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent,
         beginWordString + "Introduction section of the document." + endWordString);
      intro.setSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, "1");
      background.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent,
         beginWordString + "This is the background of the doc" + endWordString);
      background.setSoleAttributeValue(CoreAttributeTypes.SafetyCriticality, "III");
      background.setSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, "1.1");
      scope.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent,
         beginWordString + "The scope is the entire test" + endWordString);
      scope.setSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, "1.2");
      subSystem.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent,
         beginWordString + "The following are SubSystems of the test document" + endWordString);
      subSystem.setSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, "2");
      hardware.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent,
         beginWordString + "Hardware is an important Sub System" + endWordString);
      hardware.setSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, "2.1");
      hardwareFunc.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent,
         beginWordString + "The first hardware function is power on switch" + endWordString);
      hardwareFunc.setSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, "2.1.1");
      software.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent,
         beginWordString + "Software is crucial to be running correctly" + endWordString);
      software.setSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, "2.2");
      softwareFunc.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent,
         beginWordString + "Hello World, is basic software." + endWordString);
      softwareFunc.setSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, "2.2.1");
      notes.setSoleAttributeValue(
         CoreAttributeTypes.WordTemplateContent,
         beginWordString + "Notes are great for small topics, and the link" + beginLinkInsert + hardware.getGuid() + endLinkInsert + " too." + endWordString);
      notes.setSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, "3");
      morenotes.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent,
         beginWordString + "More notes to read!" + endWordString);
      morenotes.setSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, "3.1");
   }

   // Add changes to the Document
   // 1. Change just the original branch
   // 2. Change to both the original branch and working branch
   // 3. Change to just the working branch
   private void setUpDocChanges(Artifact folder) throws OseeCoreException {
      // 1.
      SkynetTransaction onRootTx = TransactionManager.createTransaction(rootBranch, "ORIG UPDATE");

      Artifact intro = folder.getDescendant("Introduction");
      Assert.assertNotNull("Cant find Introduction on branch", intro);
      intro.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent,
         beginWordString + "Introduction section of the test document." + endWordString);
      intro.persist(onRootTx);

      // 2.
      Artifact bckgrd = folder.getDescendant("Introduction").getDescendant("Background");
      Assert.assertNotNull("Cant find Background on branch", bckgrd);
      bckgrd.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent,
         beginWordString + "This is the background of the document" + endWordString);
      bckgrd.persist(onRootTx);
      onRootTx.execute();

      SkynetTransaction onChildTx = TransactionManager.createTransaction(updateBranch, "WORKING UPDATE");

      Artifact background = ArtifactQuery.getArtifactFromId(bckgrd.getGuid(), updateBranch);
      Assert.assertNotNull("Cant find Background on update branch", background);
      background.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent,
         beginWordString + "This paragraph describes the background of the doc" + endWordString);
      background.persist(onChildTx);

      // 3.
      String guid =
         folder.getDescendant("Subsystem").getDescendant("Hardware").getDescendant("Hardware Functions").getGuid();
      Artifact hdwrFunc = ArtifactQuery.getArtifactFromId(guid, updateBranch);
      Assert.assertNotNull("Cant find Hardware Functions on update branch", hdwrFunc);
      hdwrFunc.setSoleAttributeValue(CoreAttributeTypes.WordTemplateContent,
         beginWordString + "The first hardware function of importance is the power on switch." + endWordString);
      hdwrFunc.persist(onChildTx);

      onChildTx.execute();
   }

   // Add a change to use a different hyperlink for field code diff testing
   private void setupFieldCodeChange() {
      SkynetTransaction onChildTx = TransactionManager.createTransaction(updateBranch, "WORKING UPDATE");
      String hdwrGuid =
         docFolder.getDescendant("Subsystem").getDescendant("Hardware").getDescendant("Hardware Functions").getGuid();
      String notesGuid = docFolder.getDescendant("Notes").getGuid();
      Artifact notes = ArtifactQuery.getArtifactFromId(notesGuid, updateBranch);
      notes.setSoleAttributeValue(
         CoreAttributeTypes.WordTemplateContent,
         beginWordString + "Notes are great for small topics, and the link" + beginLinkInsert + hdwrGuid + endLinkInsert + " too." + endWordString);

      notes.persist(onChildTx);
      onChildTx.execute();
   }

   private static String getResourceData(String relativePath) throws IOException {
      String value = Lib.fileToString(WordTemplateProcessorTest.class, "support/" + relativePath);
      Assert.assertTrue(Strings.isValid(value));
      return value;
   }

   private String getFileAsString(String filePath) throws IOException {
      String retStr;
      Assert.assertNotNull("File is Null", filePath);
      File doc = new File(filePath);
      retStr = Lib.fileToString(doc);
      return retStr;
   }

   private void modifyOption(String optName, Object optValue) {
      for (int i = 0; i < options.length; i += 2) {
         if (optName.equals(options[i])) {
            options[i + 1] = optValue;
            break;
         }
      }
   }

   private void basicDocumentCheck(String document, String pubString, boolean merge, boolean fieldcode) {
      String testName = method.getQualifiedTestName();
      String altString = "\" ";
      String period = "";
      if (pubString.isEmpty()) {
         altString = "  \"";
         period = ".";
      }
      Assert.assertTrue(
         String.format("%s, Expected 1. Introduction", testName),
         document.contains("<wx:t wx:val=\"1" + period + altString + pubString + "/><wx:font wx:val=\"Times New Roman\"/></w:listPr></w:pPr><w:r><w:t>Introduction</w:t></w:r>"));
      if (!merge) {
         Assert.assertTrue(
            String.format("%s, Expected 1.1 Background", testName),
            document.contains("<wx:t wx:val=\"1.1" + altString + pubString + "/><wx:font wx:val=\"Times New Roman\"/></w:listPr></w:pPr><w:r><w:t>Background</w:t></w:r>"));
      }
      Assert.assertTrue(
         String.format("%s, Expected 1.2 Scope", testName),
         document.contains("<wx:t wx:val=\"1.2" + altString + pubString + "/><wx:font wx:val=\"Times New Roman\"/></w:listPr></w:pPr><w:r><w:t>Scope</w:t></w:r>"));
      Assert.assertTrue(
         String.format("%s, Expected 2. Notes", testName),
         document.contains("<wx:t wx:val=\"2" + period + altString + pubString + "/><wx:font wx:val=\"Times New Roman\"/></w:listPr></w:pPr><w:r><w:t>Notes</w:t></w:r>"));
      Assert.assertTrue(
         String.format("%s, Expected 2.1 More Notes", testName),
         document.contains("<wx:t wx:val=\"2.1" + altString + pubString + "/><wx:font wx:val=\"Times New Roman\"/></w:listPr></w:pPr><w:r><w:t>More Notes</w:t></w:r>"));
      Assert.assertTrue(
         String.format("%s, Expected 3. Subsystem", testName),
         document.contains("<wx:t wx:val=\"3" + period + altString + pubString + "/><wx:font wx:val=\"Times New Roman\"/></w:listPr></w:pPr><w:r><w:t>Subsystem</w:t></w:r>"));
      if (!fieldcode) {
         Assert.assertTrue(
            String.format("%s, Expected 3.1 Hardware", testName),
            document.contains("<wx:t wx:val=\"3.1" + altString + pubString + "/><wx:font wx:val=\"Times New Roman\"/></w:listPr></w:pPr><w:r><w:t>Hardware</w:t></w:r>"));
      }
      Assert.assertTrue(
         String.format("%s, Expected 3.1.1 Hardware Functions", testName),
         document.contains("<wx:t wx:val=\"3.1.1" + altString + pubString + "/><wx:font wx:val=\"Times New Roman\"/></w:listPr></w:pPr><w:r><w:t>Hardware Functions</w:t></w:r>"));
      Assert.assertTrue(
         String.format("%s, Expected 3.2 Software", testName),
         document.contains("<wx:t wx:val=\"3.2" + altString + pubString + "/><wx:font wx:val=\"Times New Roman\"/></w:listPr></w:pPr><w:r><w:t>Software</w:t></w:r>"));
      Assert.assertTrue(
         String.format("%s, Expected 3.2.1 Software Functions", testName),
         document.contains("<wx:t wx:val=\"3.2.1" + altString + pubString + "/><wx:font wx:val=\"Times New Roman\"/></w:listPr></w:pPr><w:r><w:t>Software Functions</w:t></w:r>"));
   }
}
