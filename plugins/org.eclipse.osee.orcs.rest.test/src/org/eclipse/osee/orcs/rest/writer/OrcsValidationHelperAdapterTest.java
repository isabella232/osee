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

package org.eclipse.osee.orcs.rest.writer;

import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.db.mock.OrcsIntegrationByClassRule;
import org.eclipse.osee.orcs.db.mock.OseeClassDatabase;
import org.eclipse.osee.orcs.db.mock.OsgiService;
import org.eclipse.osee.orcs.rest.internal.writer.OrcsValidationHelperAdapter;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

/**
 * Test case for {@link OrcsValidationHelperAdapter}
 *
 * @author Donald G. Dunne
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OrcsValidationHelperAdapterTest {

   @Rule
   public TestRule db = OrcsIntegrationByClassRule.integrationRule(this);

   @OsgiService
   private OrcsApi orcsApi;

   private OrcsValidationHelperAdapter helper;

   @Before
   public void setUp() throws Exception {
      helper = new OrcsValidationHelperAdapter(orcsApi);
   }

   @AfterClass
   public static void cleanup() throws Exception {
      OseeClassDatabase.cleanup();
   }

   @Test
   public void testIsBranchExists() {
      Assert.assertTrue(helper.isBranchExists(COMMON));

      Assert.assertFalse(helper.isBranchExists(BranchId.valueOf(34598L)));
   }

   @Test
   public void testIsUserExists() {
      Assert.assertTrue(helper.isUserExists(SystemUser.OseeSystem.getUserId()));

      Assert.assertFalse(helper.isUserExists("notUserId"));
   }

   @Test
   public void testIsArtifactExists() {
      ArtifactReadable artifact = orcsApi.getQueryFactory().fromBranch(COMMON).andTypeEquals(
         CoreArtifactTypes.User).getResults().iterator().next();
      Assert.assertTrue(helper.isArtifactExists(COMMON, artifact.getUuid()));

      Assert.assertFalse(helper.isArtifactExists(COMMON, 999999L));
   }

   @Test
   public void testIsArtifactTypeExist() {
      System.out.println("testIsArtifactTypeExist ");

      Assert.assertTrue(helper.isArtifactTypeExist(CoreArtifactTypes.User.getId()));

      Assert.assertFalse(helper.isArtifactTypeExist(999999L));
   }

   @Test
   public void testIsRelationTypeExist() {
      System.out.println("testIsRelationTypeExist ");
      Assert.assertTrue(helper.isRelationTypeExist(CoreRelationTypes.DefaultHierarchical_Child.getGuid()));

      Assert.assertFalse(helper.isRelationTypeExist(999999L));
   }

   @Test
   public void testIsAttributeTypeExists() {
      System.out.println("testIsAttributeTypeExists ");
      Assert.assertTrue(helper.isAttributeTypeExists(CoreAttributeTypes.StaticId.getId()));

      Assert.assertFalse(helper.isAttributeTypeExists(999999L));
   }

}
