/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.coverage.integration.tests.integration;

import static org.eclipse.osee.coverage.demo.CoverageChoice.OSEE_COVERAGE_DEMO;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.eclipse.osee.client.test.framework.OseeClientIntegrationRule;
import org.eclipse.osee.client.test.framework.OseeHousekeepingRule;
import org.eclipse.osee.client.test.framework.OseeLogMonitorRule;
import org.eclipse.osee.coverage.ICoverageImporter;
import org.eclipse.osee.coverage.demo.CoverageExampleFactory;
import org.eclipse.osee.coverage.demo.CoverageExamples;
import org.eclipse.osee.coverage.editor.params.CoverageParameters;
import org.eclipse.osee.coverage.model.CoverageImport;
import org.eclipse.osee.coverage.model.CoverageItem;
import org.eclipse.osee.coverage.model.CoverageOptionManager;
import org.eclipse.osee.coverage.model.CoverageUnit;
import org.eclipse.osee.coverage.model.CoverageUnitFactory;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.coverage.store.OseeCoverageUnitStore;
import org.eclipse.osee.coverage.util.CoverageUtil;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Donald G. Dunne
 */
public class CoverageParametersTest {

   @Rule
   public TemporaryFolder tempFolder = new TemporaryFolder();

   @Rule
   public OseeClientIntegrationRule integration = new OseeClientIntegrationRule(OSEE_COVERAGE_DEMO);

   @Rule
   public OseeLogMonitorRule monitorRule = new OseeLogMonitorRule();

   @Rule
   public OseeHousekeepingRule hk = new OseeHousekeepingRule();

   public static CoverageImport coverageImport = null;

   @Test
   public void testIsAssigneeMatch() throws Exception {
      CoverageParameters coverageParameters = new CoverageParameters(new CoverageImport("test"));
      CoverageUnit unit = CoverageUnitFactory.createCoverageUnit(null, "Quark", tempFolder.getRoot().toString(), null);
      CoverageItem item = new CoverageItem(unit, CoverageOptionManager.Test_Unit, "1");

      // Will match both unit and item cause assignee search not specified
      Assert.assertTrue(coverageParameters.isAssigneeMatch(item));
      Assert.assertTrue(coverageParameters.isAssigneeMatch(unit));

      coverageParameters.setAssignee(UserManager.getUser(SystemUser.Guest));
      // Won't match unit cause assignees don't match
      Assert.assertFalse(coverageParameters.isAssigneeMatch(unit));
      // Will match item cause item doesn't store assignee
      Assert.assertTrue(coverageParameters.isAssigneeMatch(item));

      OseeCoverageUnitStore.setAssignees(unit, UserManager.getUser(SystemUser.Guest));
      // Will match unit cause assignees match
      Assert.assertTrue(coverageParameters.isAssigneeMatch(unit));
   }

   @Test
   public void testIsRationaleMatch() throws Exception {
      CoverageParameters coverageParameters = new CoverageParameters(new CoverageImport("test"));
      CoverageUnit unit = CoverageUnitFactory.createCoverageUnit(null, "Quark", "C:/UserData", null);
      CoverageItem item = new CoverageItem(unit, CoverageOptionManager.Test_Unit, "1");

      // Will match both unit and item cause Rationale search not specified
      Assert.assertTrue(coverageParameters.isRationaleMatch(item));
      Assert.assertTrue(coverageParameters.isRationaleMatch(unit));

      coverageParameters.setRationale("test");
      // Won't match unit cause Rationales don't match
      Assert.assertFalse(coverageParameters.isRationaleMatch(item));
      // Will match unit cause unit doesn't store Rationale
      Assert.assertTrue(coverageParameters.isRationaleMatch(unit));

      item.setRationale("this is a test");
      // Will match item cause Rationales match
      Assert.assertTrue(coverageParameters.isRationaleMatch(item));
   }

   @Test
   public void testIsNotesMatch() throws Exception {
      CoverageParameters coverageParameters = new CoverageParameters(new CoverageImport("test"));
      CoverageUnit unit = CoverageUnitFactory.createCoverageUnit(null, "Quark", "C:/UserData", null);
      CoverageItem item = new CoverageItem(unit, CoverageOptionManager.Test_Unit, "1");

      // Will match both unit and item cause notes search not specified
      Assert.assertTrue(coverageParameters.isNotesMatch(item));
      Assert.assertTrue(coverageParameters.isNotesMatch(unit));

      coverageParameters.setNotes("test");
      // Won't match unit cause unit notes is null
      Assert.assertFalse(coverageParameters.isNotesMatch(unit));
      // Will match item cause item doesn't store Notes
      Assert.assertTrue(coverageParameters.isNotesMatch(item));

      unit.setNotes("this is a test");
      // Will match unit cause unit notes contains word test
      Assert.assertTrue(coverageParameters.isNotesMatch(unit));

      unit.setNotes("alpha beta gamma");
      // Won't match unit cause unit notes does not contain test
      Assert.assertFalse(coverageParameters.isNotesMatch(unit));
   }

   @Test
   public void testIsCoverageMethodMatch() throws Exception {
      CoverageParameters coverageParameters = new CoverageParameters(new CoverageImport("test"));
      CoverageUnit unit = CoverageUnitFactory.createCoverageUnit(null, "Quark", "C:/UserData", null);
      CoverageItem item = new CoverageItem(unit, CoverageOptionManager.Test_Unit, "1");

      // Will match both unit and item cause coverageMethod search not specified
      Assert.assertTrue(coverageParameters.isCoverageMethodMatch(item));
      Assert.assertTrue(coverageParameters.isCoverageMethodMatch(unit));

      coverageParameters.setCoverageMethods(Collections.singleton(CoverageOptionManager.Exception_Handling));
      // Won't match item cause item coverageMethod is null
      Assert.assertFalse(coverageParameters.isCoverageMethodMatch(item));
      // Will match unit cause unit doesn't store CoverageMethods
      Assert.assertTrue(coverageParameters.isCoverageMethodMatch(unit));

      item.setCoverageMethod(CoverageOptionManager.Exception_Handling);
      // Will match unit cause unit coverageMethod contains word test
      Assert.assertTrue(coverageParameters.isCoverageMethodMatch(item));

      item.setCoverageMethod(CoverageOptionManager.Test_Unit);
      // Won't match unit cause unit coverageMethod does not contain test
      Assert.assertFalse(coverageParameters.isCoverageMethodMatch(item));
   }

   @Test
   public void testIsNameMatch() throws Exception {
      CoverageParameters coverageParameters = new CoverageParameters(new CoverageImport("test"));
      CoverageUnit unit = CoverageUnitFactory.createCoverageUnit(null, "Quark", "C:/UserData", null);
      CoverageItem item = new CoverageItem(unit, CoverageOptionManager.Test_Unit, "1");

      // Will match both unit and item cause name search not specified
      Assert.assertTrue(coverageParameters.isNameMatch(item));
      Assert.assertTrue(coverageParameters.isNameMatch(unit));

      coverageParameters.setName("test");
      // Won't match unit cause unit name is null
      Assert.assertFalse(coverageParameters.isNameMatch(unit));
      // Won't match unit cause item name is null
      Assert.assertFalse(coverageParameters.isNameMatch(item));

      unit.setName("this is a test");
      // Will match unit cause unit name contains word test
      Assert.assertTrue(coverageParameters.isNameMatch(unit));
      // Will match item cause item's getName is made up of text value
      item.setName("this is a test");
      Assert.assertTrue(coverageParameters.isNameMatch(item));

      unit.setName("alpha beta gamma");
      // Won't match unit cause unit name does not contain test
      Assert.assertFalse(coverageParameters.isNameMatch(unit));
      // Won't match item cause item's getName is made up of text value
      item.setName("alpha beta gamma");
      Assert.assertFalse(coverageParameters.isNameMatch(item));
   }

   @Test
   public void testIsNamespaceMatch() throws Exception {
      CoverageParameters coverageParameters = new CoverageParameters(new CoverageImport("test"));
      CoverageUnit unit = CoverageUnitFactory.createCoverageUnit(null, "Quark", "C:/UserData", null);
      CoverageItem item = new CoverageItem(unit, CoverageOptionManager.Test_Unit, "1");

      // Will match both unit and item cause Namespace search not specified
      Assert.assertTrue(coverageParameters.isNamespaceMatch(item));
      Assert.assertTrue(coverageParameters.isNamespaceMatch(unit));

      coverageParameters.setNamespace("test");
      // Won't match unit cause unit Namespace is null
      Assert.assertFalse(coverageParameters.isNamespaceMatch(unit));
      // Won't match unit cause item Namespace is null
      Assert.assertFalse(coverageParameters.isNamespaceMatch(item));

      unit.setNamespace("this is a test");
      // Will match unit cause unit Namespace contains word test
      Assert.assertTrue(coverageParameters.isNamespaceMatch(unit));
      // Will match item cause item's getNamespace is made up of text value
      item.setName("this is a test");
      Assert.assertTrue(coverageParameters.isNamespaceMatch(item));

      unit.setNamespace("alpha beta gamma");
      // Won't match unit cause unit Namespace does not contain test
      Assert.assertFalse(coverageParameters.isNamespaceMatch(unit));
      // Won't match item cause item's getNamespace is made up of text value
      item.setName("alpha beta gamma");
      Assert.assertFalse(coverageParameters.isNamespaceMatch(item));
   }

   @Test
   public void testCoverageParameters() throws Exception {
      ICoverageImporter importer = CoverageExampleFactory.createExample(CoverageExamples.COVERAGE_IMPORT_01);
      coverageImport = importer.run(null);
      Assert.assertNotNull(coverageImport);

      // Check import results
      Assert.assertEquals(60, coverageImport.getCoverageItemsCovered().size());
      Assert.assertEquals(122, coverageImport.getCoverageItems().size());
      Assert.assertEquals(49, coverageImport.getCoveragePercent().intValue());
      Assert.assertEquals(0, coverageImport.getCoverageItemsCount(CoverageOptionManager.Deactivated_Code));
      Assert.assertEquals(0, coverageImport.getCoverageItemsCount(CoverageOptionManager.Exception_Handling));
      Assert.assertEquals(60, coverageImport.getCoverageItemsCount(CoverageOptionManager.Test_Unit));
      Assert.assertEquals(62, coverageImport.getCoverageItemsCount(CoverageOptionManager.Not_Covered));

      CoverageParameters coverageParameters = new CoverageParameters(coverageImport);
      Result result = coverageParameters.isParameterSelectionValid();
      Assert.assertTrue(result.isTrue());

      // Test Show All
      coverageParameters.clearAll();
      Pair<Set<ICoverage>, Set<ICoverage>> itemsAndParents = coverageParameters.performSearchGetResults();
      Assert.assertEquals(122, itemsAndParents.getFirst().size());
      Assert.assertEquals(4, itemsAndParents.getSecond().size());

      // Exception_Handling
      coverageParameters.setCoverageMethods(Collections.singleton(CoverageOptionManager.Exception_Handling));
      itemsAndParents = coverageParameters.performSearchGetResults();
      Assert.assertEquals(0, itemsAndParents.getFirst().size());
      Assert.assertEquals(0, itemsAndParents.getSecond().size());

      // Test_Unit
      coverageParameters.setCoverageMethods(Collections.singleton(CoverageOptionManager.Test_Unit));
      itemsAndParents = coverageParameters.performSearchGetResults();
      Assert.assertEquals(60, itemsAndParents.getFirst().size());
      Assert.assertEquals(4, itemsAndParents.getSecond().size());
      Assert.assertEquals(12, CoverageUtil.getFirstNonFolderCoverageUnits(itemsAndParents.getFirst()).size());

      // Not_Covered
      coverageParameters.setCoverageMethods(Collections.singleton(CoverageOptionManager.Not_Covered));
      itemsAndParents = coverageParameters.performSearchGetResults();
      Assert.assertEquals(62, itemsAndParents.getFirst().size());
      Assert.assertEquals(4, itemsAndParents.getSecond().size());
      Assert.assertEquals(12, CoverageUtil.getFirstNonFolderCoverageUnits(itemsAndParents.getFirst()).size());

      // Test_Unit and Not_Covered
      coverageParameters.setCoverageMethods(Arrays.asList(CoverageOptionManager.Not_Covered,
         CoverageOptionManager.Test_Unit));
      itemsAndParents = coverageParameters.performSearchGetResults();
      Assert.assertEquals(122, itemsAndParents.getFirst().size());
      Assert.assertEquals(4, itemsAndParents.getSecond().size());
      Assert.assertEquals(12, CoverageUtil.getFirstNonFolderCoverageUnits(itemsAndParents.getFirst()).size());

      // Name = Power
      coverageParameters.clearAll();
      coverageParameters.setName("Power");
      itemsAndParents = coverageParameters.performSearchGetResults();
      Assert.assertEquals(55, itemsAndParents.getFirst().size());
      Assert.assertEquals(2, itemsAndParents.getSecond().size());
      Assert.assertEquals(4, CoverageUtil.getFirstNonFolderCoverageUnits(itemsAndParents.getFirst()).size());

      // Name = Power; Namespace = apu
      coverageParameters.clearAll();
      coverageParameters.setName("Power");
      coverageParameters.setNamespace("apu");
      itemsAndParents = coverageParameters.performSearchGetResults();
      Assert.assertEquals(23, itemsAndParents.getFirst().size());
      Assert.assertEquals(1, itemsAndParents.getSecond().size());
      Assert.assertEquals(2, CoverageUtil.getFirstNonFolderCoverageUnits(itemsAndParents.getFirst()).size());

      // Assignee
      coverageParameters.clearAll();
      coverageParameters.setName("ScreenBButton");
      itemsAndParents = coverageParameters.performSearchGetResults();
      CoverageUnit button1 = null;
      CoverageUnit button2 = null;
      CoverageUnit button3 = null;
      for (ICoverage coverage : itemsAndParents.getFirst()) {
         if (coverage.getName().startsWith("ScreenBButton1")) {
            button1 = (CoverageUnit) coverage;
            continue;
         }
         if (coverage.getName().startsWith("ScreenBButton2")) {
            button2 = (CoverageUnit) coverage;
            continue;
         }
         if (coverage.getName().startsWith("ScreenBButton3")) {
            button3 = (CoverageUnit) coverage;
            continue;
         }
      }
      Assert.assertNotNull(button1);
      Assert.assertNotNull(button2);
      Assert.assertNotNull(button3);

      CoverageUnit power1 = null;
      CoverageUnit power2 = null;
      coverageParameters.setName("PowerUnit");
      itemsAndParents = coverageParameters.performSearchGetResults();
      for (ICoverage coverage : itemsAndParents.getFirst()) {
         if (coverage.getName().startsWith("PowerUnit1")) {
            power1 = (CoverageUnit) coverage;
            continue;
         }
         if (coverage.getName().startsWith("PowerUnit2")) {
            power2 = (CoverageUnit) coverage;
            continue;
         }
      }
      Assert.assertNotNull(power1);
      Assert.assertNotNull(power2);

      OseeCoverageUnitStore.setAssignees(button1, UserManager.getUser(SystemUser.Guest));
      OseeCoverageUnitStore.setAssignees(button2, UserManager.getUser());
      OseeCoverageUnitStore.setAssignees(button3, UserManager.getUser(SystemUser.Guest));
      OseeCoverageUnitStore.setAssignees(power1, UserManager.getUser());
      OseeCoverageUnitStore.setAssignees(power2, UserManager.getUser(SystemUser.Guest));

      // Test Assignee search
      coverageParameters.clearAll();
      coverageParameters.setAssignee(UserManager.getUser());
      itemsAndParents = coverageParameters.performSearchGetResults();
      Assert.assertEquals(28, itemsAndParents.getFirst().size());
      Assert.assertEquals(2, itemsAndParents.getSecond().size());
      Assert.assertEquals(2, CoverageUtil.getFirstNonFolderCoverageUnits(itemsAndParents.getFirst()).size());

      // Add Power name to Assignee search
      coverageParameters.setName("Power");
      coverageParameters.setAssignee(UserManager.getUser());
      itemsAndParents = coverageParameters.performSearchGetResults();
      Assert.assertEquals(18, itemsAndParents.getFirst().size());
      Assert.assertEquals(1, itemsAndParents.getSecond().size());
      Assert.assertEquals(1, CoverageUtil.getFirstNonFolderCoverageUnits(itemsAndParents.getFirst()).size());

      // Test Notes search
      button2.setNotes("now is the time");
      power1.setNotes("now is the time");
      coverageParameters.clearAll();
      coverageParameters.setNotes("time");
      itemsAndParents = coverageParameters.performSearchGetResults();
      Assert.assertEquals(28, itemsAndParents.getFirst().size());
      Assert.assertEquals(2, itemsAndParents.getSecond().size());
      Assert.assertEquals(2, CoverageUtil.getFirstNonFolderCoverageUnits(itemsAndParents.getFirst()).size());

      // Add Power name to Assignee search
      coverageParameters.setName("Power");
      itemsAndParents = coverageParameters.performSearchGetResults();
      Assert.assertEquals(18, itemsAndParents.getFirst().size());
      Assert.assertEquals(1, itemsAndParents.getSecond().size());
      Assert.assertEquals(1, CoverageUtil.getFirstNonFolderCoverageUnits(itemsAndParents.getFirst()).size());

   }
}
