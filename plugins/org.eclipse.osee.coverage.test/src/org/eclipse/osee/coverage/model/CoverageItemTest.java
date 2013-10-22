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
package org.eclipse.osee.coverage.model;

import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Donald G. Dunne
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CoverageItemTest {

   public static CoverageUnit parent = null;
   public static CoverageItem ci1 = null, ci2 = null, ci3 = null, ci4 = null;
   public static SimpleTestUnitProvider simpleTestUnitProvider = new SimpleTestUnitProvider();

   @BeforeClass
   public static void testSetup() throws OseeCoreException {
      parent = CoverageUnitFactory.createCoverageUnit(null, "Top", "C:/UserData/", null);
      ci1 = new CoverageItem(parent, CoverageOptionManager.Deactivated_Code, "1");
      ci1.setName("this is text");
   }

   /**
    * Test method for
    * {@link org.eclipse.osee.coverage.model.CoverageItem#CoverageItem(org.eclipse.osee.coverage.model.CoverageUnit, org.eclipse.osee.coverage.model.CoverageOption, java.lang.String)}
    * .
    */
   @Test
   public void testCoverageItemCoverageUnitCoverageOptionString() {
      Assert.assertNotNull(ci1);
      ci2 = new CoverageItem(parent, CoverageOptionManager.Exception_Handling, "2");
      Assert.assertNotNull(ci2);
      ci3 = new CoverageItem(parent, CoverageOptionManager.Test_Unit, "3");
      Assert.assertNotNull(ci3);
      ci4 = new CoverageItem(parent, CoverageOptionManager.Not_Covered, "4");
      Assert.assertNotNull(ci4);
   }

   /**
    * Test method for
    * {@link org.eclipse.osee.coverage.model.CoverageItem#CoverageItem(org.eclipse.osee.coverage.model.CoverageUnit, java.lang.String)}
    * .
    */
   @Test
   public void testCoverageItemCoverageUnitString() throws OseeCoreException {
      String xml = ci1.toXml();
      CoverageItem ci =
         CoverageItem.createCoverageItem(parent, xml, CoverageOptionManagerDefault.instance(), simpleTestUnitProvider);
      Assert.assertEquals(ci1.getParent(), ci.getParent());
      Assert.assertEquals(ci1.getCoverageMethod(), ci.getCoverageMethod());
      Assert.assertEquals(ci1.getOrderNumber(), ci.getOrderNumber());
   }

   /**
    * Test method for
    * {@link org.eclipse.osee.coverage.model.CoverageItem#addTestUnit(org.eclipse.osee.coverage.model.CoverageTestUnit)}
    * .
    */
   @Test
   public void testAddGetTestUnit() throws OseeCoreException {
      for (int x = 0; x < 10; x++) {
         ci1.addTestUnitName("Test Unit " + x);
      }
      Assert.assertEquals(10, ci1.getTestUnits().size());
   }

   /**
    * Test method for
    * {@link org.eclipse.osee.coverage.model.CoverageItem#setCoverageMethod(org.eclipse.osee.coverage.model.CoverageOption)}
    * .
    */
   @Test
   public void testSetGetCoverageMethod() {
      ci1.setCoverageMethod(CoverageOptionManager.Exception_Handling);
      Assert.assertEquals(CoverageOptionManager.Exception_Handling, ci1.getCoverageMethod());
      ci1.setCoverageMethod(CoverageOptionManager.Deactivated_Code);
      Assert.assertEquals(CoverageOptionManager.Deactivated_Code, ci1.getCoverageMethod());
   }

   @Test
   public void testSetGetWorkProductGuid() {
      String guid = GUID.create();
      ci1.setWorkProductTaskGuid(guid);
      Assert.assertEquals(guid, ci1.getWorkProductTaskGuid());
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#getExecuteNum()}.
    */
   @Test
   public void testGetOrderNum() {
      Assert.assertEquals("1", ci1.getOrderNumber());
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#getNotes()}.
    */
   @Test
   public void testGetNotes() {
      Assert.assertEquals(ci1.getNotes(), null);
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#isEditable()}.
    */
   @Test
   public void testIsEditable() {
      Assert.assertTrue(ci1.isEditable().isFalse());
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#getName()}.
    */
   @Test
   public void testGetName() {
      Assert.assertEquals("this is text", ci1.getName());
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#getName()}.
    */
   @Test
   public void testGetNameFull() {
      parent.setOrderNumber("33");
      Assert.assertEquals("33:1 [this is text]", ci1.getNameFull());
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#isCovered()}.
    */
   @Test
   public void testIsCovered() {
      Assert.assertTrue(ci1.isCovered());
      Assert.assertTrue(ci2.isCovered());
      Assert.assertTrue(ci3.isCovered());
      Assert.assertFalse(ci4.isCovered());
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#equals(java.lang.Object)}.
    */
   @Test
   public void testEqualsObject() throws OseeCoreException {
      CoverageItem ci =
         CoverageItem.createCoverageItem(parent, ci1.toXml(), CoverageOptionManagerDefault.instance(),
            simpleTestUnitProvider);
      Assert.assertEquals(ci, ci1);
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#getGuid()}.
    */
   @Test
   public void testGetGuid() throws OseeCoreException {
      CoverageItem ci =
         CoverageItem.createCoverageItem(parent, ci1.toXml(), CoverageOptionManagerDefault.instance(),
            simpleTestUnitProvider);
      Assert.assertEquals(ci1.getGuid(), ci.getGuid());
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#getParent()}.
    */
   @Test
   public void testGetParent() {
      Assert.assertTrue(ci1.getParent().equals(parent));
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#getRationale()}.
    */
   @Test
   public void testSetGetCoverageRationale() {
      ci1.setRationale("this is rationale");
      Assert.assertEquals("this is rationale", ci1.getRationale());
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#setName(java.lang.String)}.
    */
   @Test
   public void testSetGetText() throws OseeCoreException {
      ci1.setName("this is text2");
      Assert.assertEquals("this is text2", ci1.getFileContents());
      ci1.setName("this is text");
      Assert.assertEquals("this is text", ci1.getFileContents());
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#getLocation()}.
    */
   @Test
   public void testSetGetLocation() {
      Assert.assertEquals("", ci1.getLocation());
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#getNamespace()}.
    */
   @Test
   public void testGetNamespace() {
      Assert.assertEquals("", ci1.getNamespace());
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#toXml()}.
    */
   @Test
   public void testToXml() throws OseeCoreException {
      CoverageItem ci =
         CoverageItem.createCoverageItem(parent, ci1.toXml(), CoverageOptionManagerDefault.instance(),
            simpleTestUnitProvider);
      Assert.assertEquals(ci1.getGuid(), ci.getGuid());
      Assert.assertEquals(ci1.getOrderNumber(), ci.getOrderNumber());
      Assert.assertEquals(ci1.getCoverageMethod(), ci.getCoverageMethod());
      Assert.assertEquals(ci1.getFileContents(), ci.getFileContents());
      Assert.assertEquals(ci1.getRationale(), ci.getRationale());
      Assert.assertEquals(ci1.getWorkProductTaskGuid(), ci.getWorkProductTaskGuid());
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#isAssignable()}.
    */
   @Test
   public void testIsAssignable() {
      Assert.assertFalse(ci1.isAssignable());
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#getAssignees()}.
    */
   @Test
   public void testGetAssignees() {
      Assert.assertEquals("", ci1.getAssignees());
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#getCoveragePercent()}.
    */
   @Test
   public void testGetCoveragePercent() {
      Assert.assertEquals(Double.valueOf(100.0), ci1.getCoveragePercent());
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#getChildren()}.
    */
   @Test
   public void testGetChildrenItems() {
      Assert.assertEquals(0, ci1.getChildren().size());
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#getChildren(boolean)}.
    */
   @Test
   public void testGetCoverageEditorItems() {
      Assert.assertEquals(0, ci1.getChildren(false).size());
      Assert.assertEquals(0, ci1.getChildren(true).size());
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#getCoveragePercentStr()}.
    */
   @Test
   public void testGetCoveragePercentStr() {
      Assert.assertEquals("100", ci1.getCoveragePercentStr());
      Assert.assertEquals("0", ci4.getCoveragePercentStr());
   }

   /**
    * Test method for {@link org.eclipse.osee.coverage.model.CoverageItem#isFolder()}.
    */
   @Test
   public void testIsFolder() {
      Assert.assertEquals(false, ci1.isFolder());
   }

}
