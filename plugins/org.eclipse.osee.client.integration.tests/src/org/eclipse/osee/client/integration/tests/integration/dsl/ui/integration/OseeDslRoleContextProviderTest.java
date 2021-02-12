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

package org.eclipse.osee.client.integration.tests.integration.dsl.ui.integration;

import static org.eclipse.osee.client.demo.DemoChoice.OSEE_CLIENT_DEMO;
import static org.eclipse.osee.framework.core.enums.CoreArtifactTypes.Artifact;
import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.client.test.framework.OseeClientIntegrationRule;
import org.eclipse.osee.client.test.framework.OseeLogMonitorRule;
import org.eclipse.osee.framework.core.data.AccessContextToken;
import org.eclipse.osee.framework.core.dsl.OseeDslResourceUtil;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDsl;
import org.eclipse.osee.framework.core.dsl.ui.integration.operations.OseeDslRoleContextProvider;
import org.eclipse.osee.framework.core.enums.CoreArtifactTokens;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test Case for {@link OseeDslRoleContextProvider}
 *
 * @author John R. Misinco
 */
public class OseeDslRoleContextProviderTest {

   @Rule
   public OseeClientIntegrationRule integration = new OseeClientIntegrationRule(OSEE_CLIENT_DEMO);

   @Rule
   public OseeLogMonitorRule monitorRule = new OseeLogMonitorRule();

   @Test
   public void testGetContextId() throws Exception {
      Long contextId = Lib.generateArtifactIdAsInt();
      Artifact user = ArtifactQuery.getArtifactFromToken(SystemUser.Anonymous);
      String testSheet = getTestSheet1(contextId, user.getId());
      OseeDsl model = OseeDslResourceUtil.loadModel("osee:/text.osee", testSheet).getModel();
      MockDslProvider dslProvider = new MockDslProvider(model);
      OseeDslRoleContextProvider contextProvider = new OseeDslRoleContextProvider(dslProvider);
      Collection<? extends AccessContextToken> contextIds = contextProvider.getContextId(user);

      Assert.assertEquals(1, contextIds.size());
      Assert.assertEquals(contextId, contextIds.iterator().next().getId());
   }

   @Test
   public void testGetContextIdExtended() throws Exception {
      Long contextId1 = Lib.generateArtifactIdAsInt();
      Long contextId2 = Lib.generateArtifactIdAsInt();
      Artifact user = ArtifactQuery.getArtifactFromToken(SystemUser.Anonymous);
      Artifact role2User = ArtifactTypeManager.addArtifact(Artifact, COMMON, "Role 2 user");

      String testSheet = getTestSheet2(contextId1, user.getId(), contextId2, role2User);
      OseeDsl model = OseeDslResourceUtil.loadModel("osee:/text.osee", testSheet).getModel();
      MockDslProvider dslProvider = new MockDslProvider(model);
      OseeDslRoleContextProvider contextProvider = new OseeDslRoleContextProvider(dslProvider);
      Collection<? extends AccessContextToken> contextIds = contextProvider.getContextId(user);

      Assert.assertEquals(1, contextIds.size());
      Assert.assertEquals(contextId1, contextIds.iterator().next().getId());

      role2User.persist("Test User");
      contextIds = contextProvider.getContextId(role2User);

      Assert.assertEquals(2, contextIds.size());
      Iterator<? extends AccessContextToken> iterator = contextIds.iterator();
      List<Long> contextList = new LinkedList<>();
      contextList.add(contextId1);
      contextList.add(contextId2);
      Assert.assertTrue(contextList.remove(iterator.next().getId()));
      Assert.assertTrue(contextList.remove(iterator.next().getId()));

      role2User.deleteAndPersist(getClass().getSimpleName());
   }

   @Test
   public void testDbInitCreationOfAccessModel() throws Exception {
      Artifact model = ArtifactQuery.getArtifactFromToken(CoreArtifactTokens.FrameworkAccessModel);
      String xtext = model.getSoleAttributeValue(CoreAttributeTypes.GeneralStringData);
      Assert.assertTrue(xtext.contains("anonymous.context"));
   }

   private String getTestSheet1(Long contextId, Long role1Id) {
      StringBuilder sb = new StringBuilder();
      sb.append("role \"role1\" {\n");
      sb.append("   id ");
      sb.append(role1Id);
      sb.append(";\n");
      sb.append("   accessContext \"role1.context\";\n");
      sb.append("}\n\n");

      sb.append("accessContext \"role1.context\" {\n");
      sb.append("   id ");
      sb.append(contextId);
      sb.append(";\n");
      sb.append("   DENY edit relationType ALL BOTH;\n");
      sb.append("}\n");
      return sb.toString();
   }

   private String getTestSheet2(Long context1, Long role1Id, Long context2, Artifact role2) {
      StringBuilder sb = new StringBuilder(getTestSheet1(context1, role1Id));
      sb.append("\nrole \"role2\" extends \"role1\" {\n");
      sb.append("   id ");
      sb.append(role2.getIdString());
      sb.append(";\n");
      sb.append("   accessContext \"role2.context\";\n");
      sb.append("}\n\n");

      sb.append("accessContext \"role2.context\" {\n");
      sb.append("   id ");
      sb.append(context2);
      sb.append(";\n");
      sb.append("   DENY edit relationType ALL BOTH;\n");
      sb.append("}\n");
      return sb.toString();
   }
}
