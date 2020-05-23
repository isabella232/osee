/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.orcs.api;

import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import static org.eclipse.osee.framework.core.enums.DemoBranches.SAW_Bld_1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreUserGroups;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.db.mock.OrcsIntegrationByClassRule;
import org.eclipse.osee.orcs.db.mock.OseeClassDatabase;
import org.eclipse.osee.orcs.db.mock.OsgiService;
import org.eclipse.osee.orcs.search.QueryBuilder;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 * @author Jeff C. Phillips
 */
public class OrcsAttributeLoadingTest {

   @Rule
   public TestRule db = OrcsIntegrationByClassRule.integrationRule(this);

   @OsgiService
   private OrcsApi orcsApi;

   private QueryFactory query;

   @Before
   public void setUp() throws Exception {
      query = orcsApi.getQueryFactory();
   }

   @AfterClass
   public static void cleanup() throws Exception {
      OseeClassDatabase.cleanup();
   }

   @Test
   public void testAttributeLoading() throws Exception {
      ArtifactReadable art = query.fromBranch(COMMON).andId(CoreUserGroups.Everyone).getResults().getExactlyOne();
      Assert.assertNotNull(art);
      assertEquals("Everyone", art.getSoleAttributeAsString(CoreAttributeTypes.Name));

      //Test boolean attributes
      assertEquals(art.getSoleAttributeAsString(CoreAttributeTypes.DefaultGroup), "true");
   }

   @Test
   public void testLoadWordTemplateContentAttributes() {
      QueryBuilder builder = query.fromBranch(SAW_Bld_1).andNameEquals("Haptic Constraints");

      ResultSet<ArtifactReadable> resultSet = builder.getResults();

      ArtifactReadable artifact = resultSet.iterator().next();
      assertTrue(artifact.getSoleAttributeAsString(CoreAttributeTypes.WordTemplateContent).length() > 2);

      assertFalse(resultSet.isEmpty());
      assertEquals(resultSet.size(), builder.getCount());
   }

}
