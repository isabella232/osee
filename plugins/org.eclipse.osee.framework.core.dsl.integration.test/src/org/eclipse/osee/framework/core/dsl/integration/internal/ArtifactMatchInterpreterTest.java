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
package org.eclipse.osee.framework.core.dsl.integration.internal;

import java.util.Collection;
import java.util.Iterator;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.dsl.integration.ArtifactDataProvider.ArtifactProxy;
import org.eclipse.osee.framework.core.dsl.integration.mocks.DslAsserts;
import org.eclipse.osee.framework.core.dsl.integration.mocks.MockModel;
import org.eclipse.osee.framework.core.dsl.oseeDsl.CompareOp;
import org.eclipse.osee.framework.core.dsl.oseeDsl.CompoundCondition;
import org.eclipse.osee.framework.core.dsl.oseeDsl.Condition;
import org.eclipse.osee.framework.core.dsl.oseeDsl.MatchField;
import org.eclipse.osee.framework.core.dsl.oseeDsl.SimpleCondition;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XArtifactMatcher;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XLogicOperator;
import org.eclipse.osee.framework.core.model.IBasicArtifact;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.jdk.core.type.Identity;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test Case for {@link ArtifactMatchInterpreter}
 * 
 * @author Roberto E. Escobar
 */
public class ArtifactMatchInterpreterTest {

   private final ArtifactMatchInterpreter interpreter = new ArtifactMatchInterpreter();

   @Test
   public void testMatchNoConditions() throws OseeCoreException {
      XArtifactMatcher matcher = MockModel.createXArtifactMatcherRef("TestArtifact");
      boolean actual = interpreter.matches(matcher, (ArtifactProxy) null);
      Assert.assertEquals(false, actual);
   }

   @Test
   public void testArtifactNameEq() throws OseeCoreException {
      XArtifactMatcher matcher =
         MockModel.createMatcher("artifactMatcher \"Test\" where artifactName EQ \"Test Artifact\";");

      DslAsserts.assertEquals(matcher.getConditions().iterator().next(), MatchField.ARTIFACT_NAME, CompareOp.EQ,
         "Test Artifact");

      ArtifactProxy proxy = createProxy(GUID.create(), "test Artifact");
      boolean actual = interpreter.matches(matcher, proxy);
      Assert.assertEquals(false, actual);

      proxy = createProxy(GUID.create(), "Test Artifact");
      actual = interpreter.matches(matcher, proxy);
      Assert.assertEquals(true, actual);
   }

   @Test
   public void testArtifactNameLike() throws OseeCoreException {
      XArtifactMatcher matcher =
         MockModel.createMatcher("artifactMatcher \"Test\" where artifactName LIKE \".*arti.*\";");

      DslAsserts.assertEquals(matcher.getConditions().iterator().next(), MatchField.ARTIFACT_NAME, CompareOp.LIKE,
         ".*arti.*");

      ArtifactProxy proxy = createProxy(GUID.create(), "9999 arti_121341");
      boolean actual = interpreter.matches(matcher, proxy);
      Assert.assertEquals(true, actual);
   }

   @Test
   public void testArtifactGuidEq() throws OseeCoreException {
      String guid = GUID.create();
      XArtifactMatcher matcher =
         MockModel.createMatcher("artifactMatcher \"Test\" where artifactGuid EQ \"" + guid + "\";");

      DslAsserts.assertEquals(matcher.getConditions().iterator().next(), MatchField.ARTIFACT_GUID, CompareOp.EQ, guid);

      ArtifactProxy proxy = createProxy(guid, "");
      boolean actual = interpreter.matches(matcher, proxy);
      Assert.assertEquals(true, actual);
   }

   @Test
   public void testArtifactGuidLike() throws OseeCoreException {
      XArtifactMatcher matcher = MockModel.createMatcher("artifactMatcher \"Test\" where artifactGuid LIKE \"\\w+\";");

      DslAsserts.assertEquals(matcher.getConditions().iterator().next(), MatchField.ARTIFACT_GUID, CompareOp.LIKE,
         "\\w+");

      ArtifactProxy proxy = createProxy("ABCDEFGHIJK123456789", "");
      boolean actual = interpreter.matches(matcher, proxy);
      Assert.assertEquals(true, actual);
   }

   @Test
   public void testArtifactBranchNameEq() throws OseeCoreException {
      XArtifactMatcher matcher = MockModel.createMatcher("artifactMatcher \"Test\" where branchName EQ \"branch1\";");

      DslAsserts.assertEquals(matcher.getConditions().iterator().next(), MatchField.BRANCH_NAME, CompareOp.EQ,
         "branch1");

      ArtifactProxy proxy = createProxy(GUID.create(), "art1", Lib.generateUuid(), "branch2");
      boolean actual = interpreter.matches(matcher, proxy);
      Assert.assertEquals(false, actual);

      proxy = createProxy(GUID.create(), "art1", Lib.generateUuid(), "branch1");
      actual = interpreter.matches(matcher, proxy);
      Assert.assertEquals(true, actual);
   }

   @Test
   public void testArtifactBranchNameLike() throws OseeCoreException {
      XArtifactMatcher matcher =
         MockModel.createMatcher("artifactMatcher \"Test\" where branchName LIKE \".*hello.*\";");

      DslAsserts.assertEquals(matcher.getConditions().iterator().next(), MatchField.BRANCH_NAME, CompareOp.LIKE,
         ".*hello.*");

      ArtifactProxy proxy = createProxy(GUID.create(), "art1", Lib.generateUuid(), "this is the hello branch");
      boolean actual = interpreter.matches(matcher, proxy);
      Assert.assertEquals(true, actual);
   }

   @Test
   public void testArtifactBranchUuidEq() throws OseeCoreException {
      long uuid = Lib.generateUuid();
      XArtifactMatcher matcher =
         MockModel.createMatcher("artifactMatcher \"Test\" where branchUuid EQ \"" + uuid + "\";");

      DslAsserts.assertEquals(matcher.getConditions().iterator().next(), MatchField.BRANCH_UUID, CompareOp.EQ,
         String.valueOf(uuid));

      ArtifactProxy proxy = createProxy(GUID.create(), "art1", uuid, "");
      boolean actual = interpreter.matches(matcher, proxy);
      Assert.assertEquals(true, actual);
   }

   @Test
   public void testArtifactBranchUuidLike() throws OseeCoreException {
      XArtifactMatcher matcher = MockModel.createMatcher("artifactMatcher \"Test\" where branchUuid LIKE \"\\w+\";");

      DslAsserts.assertEquals(matcher.getConditions().iterator().next(), MatchField.BRANCH_UUID, CompareOp.LIKE, "\\w+");

      ArtifactProxy proxy = createProxy(GUID.create(), "art1", Lib.generateUuid(), "");
      boolean actual = interpreter.matches(matcher, proxy);
      Assert.assertEquals(true, actual);
   }

   @Test
   public void testCompoundCondition1() throws OseeCoreException {
      XArtifactMatcher andMatcher =
         MockModel.createMatcher("artifactMatcher \"Test\" where artifactGuid EQ \"ABCDEFGHIJK123456789\" AND artifactName EQ \"myArtifact\";");

      Iterator<Condition> iterator = andMatcher.getConditions().iterator();
      DslAsserts.assertEquals(iterator.next(), MatchField.ARTIFACT_GUID, CompareOp.EQ, "ABCDEFGHIJK123456789");
      DslAsserts.assertEquals(iterator.next(), MatchField.ARTIFACT_NAME, CompareOp.EQ, "myArtifact");

      Assert.assertEquals(1, andMatcher.getOperators().size());
      Assert.assertEquals(XLogicOperator.AND, andMatcher.getOperators().iterator().next());

      XArtifactMatcher orMatcher =
         MockModel.createMatcher("artifactMatcher \"Test\" where artifactGuid EQ \"ABCDEFGHIJK123456789\" OR artifactName EQ \"myArtifact\";");

      Iterator<Condition> iterator2 = orMatcher.getConditions().iterator();
      DslAsserts.assertEquals(iterator2.next(), MatchField.ARTIFACT_GUID, CompareOp.EQ, "ABCDEFGHIJK123456789");
      DslAsserts.assertEquals(iterator2.next(), MatchField.ARTIFACT_NAME, CompareOp.EQ, "myArtifact");

      Assert.assertEquals(1, orMatcher.getOperators().size());
      Assert.assertEquals(XLogicOperator.OR, orMatcher.getOperators().iterator().next());

      ArtifactProxy proxy1 = createProxy("1BCDEFGHIJK123456789", "xArtifact");
      ArtifactProxy proxy2 = createProxy("1BCDEFGHIJK123456789", "myArtifact");
      ArtifactProxy proxy3 = createProxy("ABCDEFGHIJK123456789", "xArtifact");
      ArtifactProxy proxy4 = createProxy("ABCDEFGHIJK123456789", "myArtifact");

      Assert.assertEquals(false, interpreter.matches(andMatcher, proxy1));
      Assert.assertEquals(false, interpreter.matches(andMatcher, proxy2));
      Assert.assertEquals(false, interpreter.matches(andMatcher, proxy3));
      Assert.assertEquals(true, interpreter.matches(andMatcher, proxy4));

      Assert.assertEquals(false, interpreter.matches(orMatcher, proxy1));
      Assert.assertEquals(true, interpreter.matches(orMatcher, proxy2));
      Assert.assertEquals(true, interpreter.matches(orMatcher, proxy3));
      Assert.assertEquals(true, interpreter.matches(orMatcher, proxy4));
   }

   @Test
   public void testCompoundCondition2() throws OseeCoreException {
      XArtifactMatcher matcher =
         MockModel.createMatcher("artifactMatcher \"Test\" where artifactGuid EQ \"ABCDEFGHIJK123456789\" AND (branchName EQ \"myArtifact\" OR branchUuid EQ \"3456789101112131415\");");

      Assert.assertEquals(2, matcher.getConditions().size());
      Iterator<Condition> iterator = matcher.getConditions().iterator();
      DslAsserts.assertEquals(iterator.next(), MatchField.ARTIFACT_GUID, CompareOp.EQ, "ABCDEFGHIJK123456789");

      Assert.assertEquals(1, matcher.getOperators().size());
      Assert.assertEquals(XLogicOperator.AND, matcher.getOperators().iterator().next());

      Condition condition = iterator.next();

      Assert.assertTrue(condition instanceof CompoundCondition);
      CompoundCondition compoundCondition = (CompoundCondition) condition;

      Assert.assertEquals(2, compoundCondition.getConditions().size());

      Iterator<SimpleCondition> iterator2 = compoundCondition.getConditions().iterator();
      DslAsserts.assertEquals(iterator2.next(), MatchField.BRANCH_NAME, CompareOp.EQ, "myArtifact");
      DslAsserts.assertEquals(iterator2.next(), MatchField.BRANCH_UUID, CompareOp.EQ,
         String.valueOf(3456789101112131415L));

      Assert.assertEquals(1, compoundCondition.getOperators().size());
      Assert.assertEquals(XLogicOperator.OR, compoundCondition.getOperators().iterator().next());

      String badArtGuid = "1BCDEFGHIJK123456789";
      long badBranchGuid = 333333333123456789L;
      String badBranchName = "xArtifact";

      String goodArtGuid = "ABCDEFGHIJK123456789";
      long goodBranchGuid = 3456789101112131415L;
      String goodBranchName = "myArtifact";

      ArtifactProxy proxy1 = createProxy(badArtGuid, "", badBranchGuid, badBranchName);
      ArtifactProxy proxy2 = createProxy(badArtGuid, "", badBranchGuid, goodBranchName);
      ArtifactProxy proxy3 = createProxy(badArtGuid, "", goodBranchGuid, badBranchName);
      ArtifactProxy proxy4 = createProxy(badArtGuid, "", goodBranchGuid, goodBranchName);
      ArtifactProxy proxy5 = createProxy(goodArtGuid, "", badBranchGuid, badBranchName);
      ArtifactProxy proxy6 = createProxy(goodArtGuid, "", badBranchGuid, goodBranchName);
      ArtifactProxy proxy7 = createProxy(goodArtGuid, "", goodBranchGuid, badBranchName);
      ArtifactProxy proxy8 = createProxy(goodArtGuid, "", goodBranchGuid, goodBranchName);

      Assert.assertEquals(false, interpreter.matches(matcher, proxy1));
      Assert.assertEquals(false, interpreter.matches(matcher, proxy2));
      Assert.assertEquals(false, interpreter.matches(matcher, proxy3));
      Assert.assertEquals(false, interpreter.matches(matcher, proxy4));

      Assert.assertEquals(false, interpreter.matches(matcher, proxy5));
      Assert.assertEquals(true, interpreter.matches(matcher, proxy6));
      Assert.assertEquals(true, interpreter.matches(matcher, proxy7));
      Assert.assertEquals(true, interpreter.matches(matcher, proxy8));
   }

   private static ArtifactProxy createProxy(String artGuid, String artifactName) {
      return createProxy(artGuid, artifactName, Lib.generateUuid(), "dummy");
   }

   private static ArtifactProxy createProxy(final String artGuid, final String artifactName, final long branchGuid, final String branchName) {
      return new ArtifactProxy() {

         @Override
         public IOseeBranch getBranch() {
            return TokenFactory.createBranch(branchGuid, branchName);
         }

         @Override
         public String getName() {
            return artifactName;
         }

         @Override
         public String getGuid() {
            return artGuid;
         }

         @Override
         public ArtifactType getArtifactType() {
            return null;
         }

         @Override
         public boolean isAttributeTypeValid(IAttributeType attributeType) {
            return false;
         }

         @Override
         public Collection<RelationType> getValidRelationTypes() {
            return null;
         }

         @Override
         public Collection<ArtifactProxy> getHierarchy() {
            return null;
         }

         @Override
         public IBasicArtifact<?> getObject() {
            return null;
         }

         @Override
         public boolean matches(Identity<?>... identities) {
            return false;
         }

      };
   }
}
