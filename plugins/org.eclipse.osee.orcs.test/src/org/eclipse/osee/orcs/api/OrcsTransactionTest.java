/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.api;

import static org.eclipse.osee.framework.core.enums.CoreArtifactTypes.Component;
import static org.eclipse.osee.framework.core.enums.CoreArtifactTypes.GeneralDocument;
import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import static org.eclipse.osee.framework.core.enums.CoreRelationTypes.Default_Hierarchical__Child;
import static org.eclipse.osee.framework.core.enums.CoreRelationTypes.Default_Hierarchical__Parent;
import static org.eclipse.osee.framework.core.enums.CoreRelationTypes.Dependency__Artifact;
import static org.eclipse.osee.framework.core.enums.CoreRelationTypes.Dependency__Dependency;
import static org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes.LEXICOGRAPHICAL_DESC;
import static org.eclipse.osee.orcs.OrcsIntegrationRule.integrationRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.Callable;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.orcs.ApplicationContext;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.OrcsBranch;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.AttributeReadable;
import org.eclipse.osee.orcs.data.BranchReadable;
import org.eclipse.osee.orcs.data.TransactionReadable;
import org.eclipse.osee.orcs.db.mock.OsgiService;
import org.eclipse.osee.orcs.search.BranchQuery;
import org.eclipse.osee.orcs.search.QueryBuilder;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.eclipse.osee.orcs.search.TransactionQuery;
import org.eclipse.osee.orcs.transaction.TransactionBuilder;
import org.eclipse.osee.orcs.transaction.TransactionFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * @author Roberto E. Escobar
 */
public class OrcsTransactionTest {

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   @Rule
   public TestRule osgi = integrationRule(this);

   @Rule
   public TestName testName = new TestName();

   @OsgiService
   private OrcsApi orcsApi;

   private final ApplicationContext context = null; // TODO use real application context
   private TransactionFactory txFactory;
   private ArtifactReadable userArtifact;
   private OrcsBranch orcsBranch;
   private QueryFactory query;

   @Before
   public void setUp() throws Exception {
      txFactory = orcsApi.getTransactionFactory(context);
      orcsBranch = orcsApi.getBranchOps(context);
      query = orcsApi.getQueryFactory(context);
      userArtifact = getSystemUser();
   }

   @Test
   public void testWritingUriAttribute() throws OseeCoreException {
      final String requirementText = "The engine torque shall be directly controllable through the engine control unit";

      TransactionBuilder tx = createTx();

      ArtifactId torqueRequirement =
         tx.createArtifact(CoreArtifactTypes.SoftwareRequirementPlainText, "Engine Torque Control");
      tx.createAttribute(torqueRequirement, CoreAttributeTypes.PlainTextContent, requirementText);

      tx.commit();

      ResultSet<ArtifactReadable> results =
         query.fromBranch(CoreBranches.COMMON).andIsOfType(CoreArtifactTypes.SoftwareRequirementPlainText).getResults();

      Optional<ArtifactReadable> item = Iterables.tryFind(results, new Predicate<ArtifactReadable>() {
         @Override
         public boolean apply(ArtifactReadable artifact) {
            String data = "";
            try {
               data = artifact.getSoleAttributeAsString(CoreAttributeTypes.PlainTextContent, "");
            } catch (OseeCoreException ex) {
               fail(Lib.exceptionToString(ex));
            }
            return requirementText.equals(data);
         }
      });

      assertTrue(item.isPresent());
      assertEquals(torqueRequirement.getGuid(), item.get().getGuid());
   }

   @Test
   public void testCreateArtifact() throws OseeCoreException {
      String comment = "Test Artifact Write";
      String expectedName = "Create A Folder";
      String expectedAnnotation = "Annotate It";

      BranchQuery branchQuery = query.branchQuery();
      org.eclipse.osee.orcs.data.BranchReadable branchReadable =
         branchQuery.andIds(CoreBranches.COMMON).getResults().getExactlyOne();

      TransactionQuery transactionQuery = query.transactionQuery();
      TransactionReadable previousTx = transactionQuery.andIsHead(branchReadable).getResults().getExactlyOne();

      TransactionBuilder tx = txFactory.createTransaction(branchReadable, userArtifact, comment);

      ArtifactId artifactId = tx.createArtifact(CoreArtifactTypes.Folder, expectedName);

      tx.setAttributesFromStrings(artifactId, CoreAttributeTypes.Annotation, expectedAnnotation);
      assertEquals(expectedName, artifactId.getName());

      TransactionReadable newTx = tx.commit();
      assertFalse(tx.isCommitInProgress());

      TransactionReadable newHeadTx = transactionQuery.andIsHead(branchReadable).getResults().getExactlyOne();

      assertEquals(newTx.getGuid().intValue(), newHeadTx.getLocalId().intValue());

      TransactionReadable newTxReadable = transactionQuery.andTxId(newTx.getGuid()).getResults().getExactlyOne();
      checkTransaction(previousTx, newTxReadable, branchReadable, comment, userArtifact);

      ResultSet<ArtifactReadable> result = query.fromBranch(CoreBranches.COMMON).andIds(artifactId).getResults();

      ArtifactReadable artifact = result.getExactlyOne();

      assertEquals(expectedAnnotation, artifact.getAttributeValues(CoreAttributeTypes.Annotation).iterator().next());
      assertEquals(expectedName, artifact.getName());
      assertEquals(expectedAnnotation, artifact.getAttributeValues(CoreAttributeTypes.Annotation).iterator().next());
      assertEquals(artifactId.getGuid(), artifact.getGuid());
   }

   @Test
   public void testCreateArtifactWithoutTagger() throws OseeCoreException {
      String comment = "Test Artifact with untagged attribute";
      String expectedName = "Create An Artifact";
      String expectedAnnotation = "Annotate It";
      String expectedQualifaction = "Test";

      BranchQuery branchQuery = query.branchQuery();
      org.eclipse.osee.orcs.data.BranchReadable branchReadable =
         branchQuery.andIds(CoreBranches.COMMON).getResults().getExactlyOne();

      TransactionQuery transactionQuery = query.transactionQuery();
      TransactionReadable previousTx = transactionQuery.andIsHead(branchReadable).getResults().getExactlyOne();

      TransactionBuilder tx = txFactory.createTransaction(branchReadable, userArtifact, comment);

      ArtifactId artifactId = tx.createArtifact(CoreArtifactTypes.SubsystemRequirementHTML, expectedName);

      tx.setAttributesFromStrings(artifactId, CoreAttributeTypes.Annotation, expectedAnnotation);
      tx.setAttributesFromStrings(artifactId, CoreAttributeTypes.QualificationMethod, expectedQualifaction);
      assertEquals(expectedName, artifactId.getName());

      TransactionReadable newTx = tx.commit();

      ArtifactReadable artifactReadable =
         query.fromBranch(CoreBranches.COMMON).andIds(artifactId).getResults().getExactlyOne();
      assertEquals(expectedName, artifactReadable.getName());
      assertEquals(expectedQualifaction,
         artifactReadable.getSoleAttributeAsString(CoreAttributeTypes.QualificationMethod));

   }

   @Test
   public void testCopyArtifact() throws Exception {
      ArtifactReadable user =
         query.fromBranch(CoreBranches.COMMON).andIds(SystemUser.Anonymous).getResults().getExactlyOne();

      // duplicate on same branch
      TransactionBuilder transaction1 = createTx();
      ArtifactId duplicate = transaction1.copyArtifact(user);
      transaction1.commit();
      ArtifactReadable userDup = query.fromBranch(CoreBranches.COMMON).andIds(duplicate).getResults().getExactlyOne();

      assertNotSame(SystemUser.Anonymous.getGuid(), userDup.getGuid());
      assertEquals(SystemUser.Anonymous.getName(), userDup.getName());

      // duplicate on different branch
      IOseeBranch branchToken = TokenFactory.createBranch("DuplicateArtifact tests");
      Callable<BranchReadable> callableBranch = orcsBranch.createTopLevelBranch(branchToken, userArtifact);

      BranchReadable topLevelBranch = callableBranch.call();

      TransactionBuilder transaction2 =
         txFactory.createTransaction(topLevelBranch, userArtifact, testName.getMethodName());
      duplicate = transaction2.copyArtifact(user);
      transaction2.commit();
      userDup = query.fromBranch(topLevelBranch).andIds(duplicate).getResults().getExactlyOne();

      assertNotSame(SystemUser.Anonymous.getGuid(), userDup.getGuid());
      assertEquals(SystemUser.Anonymous.getName(), userDup.getName());
   }

   @Test
   public void testIntroduceArtifact() throws Exception {
      ArtifactReadable user =
         query.fromBranch(CoreBranches.COMMON).andIds(SystemUser.Anonymous).getResults().getExactlyOne();

      IOseeBranch branchToken = TokenFactory.createBranch("IntroduceArtifact tests");
      BranchReadable topLevelBranch = orcsBranch.createTopLevelBranch(branchToken, userArtifact).call();

      TransactionBuilder transaction =
         txFactory.createTransaction(topLevelBranch, userArtifact, testName.getMethodName());
      transaction.introduceArtifact(COMMON, user);
      transaction.commit();

      ArtifactReadable introduced =
         query.fromBranch(topLevelBranch).andIds(SystemUser.Anonymous).getResults().getExactlyOne();
      assertEquals(user.getLocalId(), introduced.getLocalId());
   }

   @Test
   public void testIntroduceOnSameBranch() throws OseeCoreException {
      ArtifactReadable user =
         query.fromBranch(CoreBranches.COMMON).andIds(SystemUser.Anonymous).getResults().getExactlyOne();

      TransactionBuilder tx = createTx();

      thrown.expect(OseeArgumentException.class);
      tx.introduceArtifact(COMMON, user);
   }

   @Test
   public void testReadAfterWrite() throws OseeCoreException {
      QueryBuilder queryBuilder = query.fromBranch(CoreBranches.COMMON).andIds(SystemUser.Anonymous);

      ArtifactReadable originalGuest = queryBuilder.getResults().getExactlyOne();

      TransactionBuilder tx = createTx();
      tx.setSoleAttributeFromString(originalGuest, CoreAttributeTypes.Name, "Test");
      tx.commit();

      ArtifactReadable newGuest = queryBuilder.getResults().getExactlyOne();

      assertEquals(SystemUser.Anonymous.getName(), originalGuest.getName());
      assertEquals("Test", newGuest.getName());
   }

   @Test
   public void testDeleteArtifact() throws OseeCoreException {
      TransactionBuilder tx = createTx();
      ArtifactId artifact = tx.createArtifact(CoreArtifactTypes.AccessControlModel, "deleteMe");
      tx.commit();

      ArtifactReadable toDelete = query.fromBranch(CoreBranches.COMMON).andIds(artifact).getResults().getExactlyOne();

      tx = txFactory.createTransaction(CoreBranches.COMMON, userArtifact, testName.getMethodName());
      tx.deleteArtifact(toDelete);
      tx.commit();

      toDelete =
         query.fromBranch(CoreBranches.COMMON).andIds(artifact).includeDeletedArtifacts().getResults().getOneOrNull();
      assertNotNull(toDelete);
      assertTrue(toDelete.isDeleted());
   }

   @Test
   public void testDeleteAttribute() throws OseeCoreException {
      TransactionBuilder tx = createTx();
      ArtifactId artifact = tx.createArtifact(CoreArtifactTypes.AccessControlModel, "deleteThis");
      tx.createAttribute(artifact, CoreAttributeTypes.GeneralStringData, "deleted Name");
      tx.createAttribute(artifact, CoreAttributeTypes.PublishInline, true);
      tx.commit();

      tx = createTx();
      tx.deleteAttributes(artifact, CoreAttributeTypes.GeneralStringData);
      tx.commit();

      QueryBuilder builder = query.fromBranch(CoreBranches.COMMON);
      builder.andExists(CoreAttributeTypes.GeneralStringData);
      builder.andIds(artifact);
      ResultSet<ArtifactReadable> artifacts = builder.getResults();
      assertEquals(0, artifacts.size());
      builder = query.fromBranch(CoreBranches.COMMON);
      builder.andExists(CoreAttributeTypes.GeneralStringData);
      builder.andIds(artifact);
      builder.includeDeletedAttributes();
      artifacts = builder.getResults();
      assertEquals(1, artifacts.size());
      builder = query.fromBranch(CoreBranches.COMMON);
      builder.andExists(CoreAttributeTypes.Annotation);
      builder.andIds(artifact);
      builder.includeDeletedAttributes();
      artifacts = builder.getResults();
      assertEquals(0, artifacts.size());

      ArtifactReadable toDelete = query.fromBranch(CoreBranches.COMMON).andIds(artifact).getResults().getExactlyOne();

      tx = createTx();
      tx.deleteArtifact(toDelete);
      tx.commit();

      toDelete =
         query.fromBranch(CoreBranches.COMMON).andIds(artifact).includeDeletedArtifacts().getResults().getOneOrNull();
      assertNotNull(toDelete);
      assertTrue(toDelete.isDeleted());

   }

   private int[] setupHistory(ArtifactId[] artifacts) {
      TransactionBuilder tx = createTx();
      ArtifactId artifact1 = tx.createArtifact(CoreArtifactTypes.AccessControlModel, "deleteThis");
      tx.createAttribute(artifact1, CoreAttributeTypes.GeneralStringData, "deleted Name");
      tx.createAttribute(artifact1, CoreAttributeTypes.PublishInline, true);
      ArtifactId artifact2 = tx.createArtifact(CoreArtifactTypes.Folder, "deleteThisFolder");
      tx.createAttribute(artifact2, CoreAttributeTypes.Annotation, "annotation");
      tx.relate(artifact2, Default_Hierarchical__Parent, artifact1);
      TransactionReadable tx1 = tx.commit();
      artifacts[0] = artifact1;
      artifacts[1] = artifact2;

      tx = createTx();
      tx.deleteAttributes(artifact1, CoreAttributeTypes.GeneralStringData);
      TransactionReadable tx2 = tx.commit();

      ArtifactReadable toDelete = query.fromBranch(CoreBranches.COMMON).andIds(artifact1).getResults().getExactlyOne();

      tx = createTx();
      tx.deleteArtifact(toDelete);
      tx.deleteAttributes(artifact2, CoreAttributeTypes.Annotation);
      tx.unrelate(artifact2, Default_Hierarchical__Parent, artifact1);
      TransactionReadable tx3 = tx.commit();

      toDelete = query.fromBranch(CoreBranches.COMMON).andIds(artifact2).getResults().getExactlyOne();

      tx = createTx();
      tx.deleteArtifact(toDelete);
      TransactionReadable tx4 = tx.commit();

      toDelete =
         query.fromBranch(CoreBranches.COMMON).andIds(artifact1).includeDeletedArtifacts().getResults().getOneOrNull();
      assertNotNull(toDelete);
      assertTrue(toDelete.isDeleted());
      int[] toReturn = {tx1.getGuid(), tx2.getGuid(), tx3.getGuid(), tx4.getGuid()};
      return toReturn;
   }

   @Test
   public void testHistoricalArtifactsCreated() throws OseeCoreException {

      ArtifactId[] theArtifacts = {null, null};
      int[] transactions = setupHistory(theArtifacts);
      ArtifactId artifact1 = theArtifacts[0];
      ArtifactId artifact2 = theArtifacts[1];
      QueryBuilder builder = query.fromBranch(CoreBranches.COMMON);
      builder.fromTransaction(transactions[0]);
      builder.andExists(CoreAttributeTypes.GeneralStringData, CoreAttributeTypes.Annotation);
      builder.andIds(artifact1, artifact2);
      ResultSet<ArtifactReadable> artifacts = builder.getResults();
      verifyHistoricalArtifacts(artifacts, artifact1, artifact2);
   }

   @Test
   public void testHistoricalOneArtifactDeleted() throws OseeCoreException {

      ArtifactId[] theArtifacts = {null, null};
      int[] transactions = setupHistory(theArtifacts);
      ArtifactId artifact1 = theArtifacts[0];
      ArtifactId artifact2 = theArtifacts[1];
      QueryBuilder builder = query.fromBranch(CoreBranches.COMMON);
      builder.fromTransaction(transactions[2]);
      builder.andIds(artifact1, artifact2);
      ResultSet<ArtifactReadable> artifacts = builder.getResults();
      verifyHistoricalArtifacts(artifacts, null, artifact2);
   }

   @Test
   public void testHistoricalDeletedAttribute() throws OseeCoreException {

      ArtifactId[] theArtifacts = {null, null};
      int[] transactions = setupHistory(theArtifacts);
      ArtifactId artifact1 = theArtifacts[0];
      ArtifactId artifact2 = theArtifacts[1];
      QueryBuilder builder = query.fromBranch(CoreBranches.COMMON);
      builder.fromTransaction(transactions[1]);
      builder.andExists(CoreAttributeTypes.GeneralStringData, CoreAttributeTypes.Annotation);
      builder.andIds(artifact1, artifact2);
      // test the historical count query
      int count = builder.getCount();
      assertEquals(1, count);
      ResultSet<ArtifactReadable> artifacts = builder.getResults();
      verifyHistoricalArtifacts(artifacts, null, artifact2);
   }

   @Test
   public void testHistoricalOneArtifactDeletedAttribute() throws OseeCoreException {

      ArtifactId[] theArtifacts = {null, null};
      int[] transactions = setupHistory(theArtifacts);
      ArtifactId artifact1 = theArtifacts[0];
      ArtifactId artifact2 = theArtifacts[1];
      QueryBuilder builder = query.fromBranch(CoreBranches.COMMON);
      builder.fromTransaction(transactions[1]);
      builder.andExists(CoreAttributeTypes.GeneralStringData, CoreAttributeTypes.Annotation);
      builder.andIds(artifact1);
      ResultSet<ArtifactReadable> artifacts = builder.getResults();
      verifyHistoricalArtifacts(artifacts, null, null);
   }

   @Test
   public void testHistoricalIncludeDeletedAttribute() throws OseeCoreException {

      ArtifactId[] theArtifacts = {null, null};
      int[] transactions = setupHistory(theArtifacts);
      ArtifactId artifact1 = theArtifacts[0];
      ArtifactId artifact2 = theArtifacts[1];
      QueryBuilder builder = query.fromBranch(CoreBranches.COMMON);
      builder.fromTransaction(transactions[1]);
      builder.andExists(CoreAttributeTypes.GeneralStringData, CoreAttributeTypes.Annotation);
      builder.andIds(artifact1);
      builder.includeDeletedAttributes();
      ResultSet<ArtifactReadable> artifacts = builder.getResults();
      verifyHistoricalArtifacts(artifacts, artifact1, null);
   }

   @Test
   public void testHistoricalTwoArtifactsIncludeDeletedAttribute() throws OseeCoreException {

      ArtifactId[] theArtifacts = {null, null};
      int[] transactions = setupHistory(theArtifacts);
      ArtifactId artifact1 = theArtifacts[0];
      ArtifactId artifact2 = theArtifacts[1];
      QueryBuilder builder = query.fromBranch(CoreBranches.COMMON);
      builder.fromTransaction(transactions[1]);
      builder.andExists(CoreAttributeTypes.GeneralStringData, CoreAttributeTypes.Annotation);
      builder.andIds(artifact2, artifact1);
      builder.includeDeletedAttributes();
      ResultSet<ArtifactReadable> artifacts = builder.getResults();
      verifyHistoricalArtifacts(artifacts, artifact1, artifact2);
   }

   @Test
   public void testHistoricalDeletedArtifacts() throws OseeCoreException {

      ArtifactId[] theArtifacts = {null, null};
      int[] transactions = setupHistory(theArtifacts);
      ArtifactId artifact1 = theArtifacts[0];
      ArtifactId artifact2 = theArtifacts[1];
      QueryBuilder builder = query.fromBranch(CoreBranches.COMMON);
      builder.fromTransaction(transactions[2]);
      builder.andIds(artifact1, artifact2);
      ResultSet<ArtifactReadable> artifacts = builder.getResults();
      verifyHistoricalArtifacts(artifacts, null, artifact2);
   }

   @Test
   public void testHistoricalAllowDeletedArtifactsDeletedAttributes() throws OseeCoreException {

      ArtifactId[] theArtifacts = {null, null};
      int[] transactions = setupHistory(theArtifacts);
      ArtifactId artifact1 = theArtifacts[0];
      ArtifactId artifact2 = theArtifacts[1];
      QueryBuilder builder = query.fromBranch(CoreBranches.COMMON);
      builder.fromTransaction(transactions[2]);
      builder.andExists(CoreAttributeTypes.GeneralStringData, CoreAttributeTypes.Annotation);
      builder.andIds(artifact1, artifact2);
      builder.includeDeletedArtifacts();
      ResultSet<ArtifactReadable> artifacts = builder.getResults();
      verifyHistoricalArtifacts(artifacts, null, null);
   }

   @Test
   public void testHistoricalAllowDeletedArtifactsNondeletedAttribute() throws OseeCoreException {

      ArtifactId[] theArtifacts = {null, null};
      int[] transactions = setupHistory(theArtifacts);
      ArtifactId artifact1 = theArtifacts[0];
      ArtifactId artifact2 = theArtifacts[1];
      QueryBuilder builder = query.fromBranch(CoreBranches.COMMON);
      builder.fromTransaction(transactions[2]);
      builder.andExists(CoreAttributeTypes.PublishInline, CoreAttributeTypes.Annotation);
      builder.andIds(artifact1, artifact2);
      builder.includeDeletedArtifacts();
      ResultSet<ArtifactReadable> artifacts = builder.getResults();
      verifyHistoricalArtifacts(artifacts, artifact1, null);
   }

   @Test
   public void testHistoricalRelation() throws OseeCoreException {

      ArtifactId[] theArtifacts = {null, null};
      int[] transactions = setupHistory(theArtifacts);
      ArtifactId artifact1 = theArtifacts[0];
      ArtifactId artifact2 = theArtifacts[1];
      QueryBuilder builder = query.fromBranch(CoreBranches.COMMON);
      builder.fromTransaction(transactions[1]);
      builder.andExists(CoreAttributeTypes.GeneralStringData, CoreAttributeTypes.Annotation);
      builder.andIds(artifact2, artifact1);
      builder.includeDeletedAttributes();
      ResultSet<ArtifactReadable> artifacts = builder.getResults();
      verifyHistoricalArtifacts(artifacts, artifact1, artifact2);
      Iterator<ArtifactReadable> iter = artifacts.iterator();
      ArtifactReadable artifactActual = iter.next();
      ArtifactReadable artifact1Actual = iter.next();
      builder = query.fromBranch(CoreBranches.COMMON);
      builder.fromTransaction(transactions[1]);
      builder.andExists(CoreAttributeTypes.GeneralStringData, CoreAttributeTypes.Annotation);
      builder.andIds(artifact1, artifact2);
      builder.includeDeletedArtifacts();
      builder.includeDeletedAttributes();
      builder.andRelatedTo(Default_Hierarchical__Parent, artifact1Actual);
      artifacts = builder.getResults();
      verifyHistoricalArtifacts(artifacts, artifact1, null);
   }

   @Test
   public void testHistoricalDeletedRelation() throws OseeCoreException {

      ArtifactId[] theArtifacts = {null, null};
      int[] transactions = setupHistory(theArtifacts);
      ArtifactId artifact1 = theArtifacts[0];
      ArtifactId artifact2 = theArtifacts[1];
      QueryBuilder builder = query.fromBranch(CoreBranches.COMMON);
      builder.fromTransaction(transactions[1]);
      builder.andExists(CoreAttributeTypes.GeneralStringData, CoreAttributeTypes.Annotation);
      builder.andIds(artifact2, artifact1);
      builder.includeDeletedAttributes();
      ResultSet<ArtifactReadable> artifacts = builder.getResults();
      verifyHistoricalArtifacts(artifacts, artifact1, artifact2);
      Iterator<ArtifactReadable> iter = artifacts.iterator();
      ArtifactReadable artifactActual = iter.next();
      ArtifactReadable artifact1Actual = iter.next();
      builder = query.fromBranch(CoreBranches.COMMON);
      builder.fromTransaction(transactions[2]);
      builder.andExists(CoreAttributeTypes.GeneralStringData, CoreAttributeTypes.Annotation);
      builder.andIds(artifact1, artifact2);
      builder.includeDeletedArtifacts();
      builder.includeDeletedAttributes();
      builder.andRelatedTo(Default_Hierarchical__Parent, artifact1Actual);
      artifacts = builder.getResults();
      verifyHistoricalArtifacts(artifacts, null, null);
   }

   @Test
   public void testHistoricalAllowDeletedRelation() throws OseeCoreException {

      ArtifactId[] theArtifacts = {null, null};
      int[] transactions = setupHistory(theArtifacts);
      ArtifactId artifact1 = theArtifacts[0];
      ArtifactId artifact2 = theArtifacts[1];
      QueryBuilder builder = query.fromBranch(CoreBranches.COMMON);
      builder.fromTransaction(transactions[1]);
      builder.andExists(CoreAttributeTypes.GeneralStringData, CoreAttributeTypes.Annotation);
      builder.andIds(artifact2, artifact1);
      builder.includeDeletedAttributes();
      ResultSet<ArtifactReadable> artifacts = builder.getResults();
      verifyHistoricalArtifacts(artifacts, artifact1, artifact2);
      Iterator<ArtifactReadable> iter = artifacts.iterator();
      ArtifactReadable artifactActual = iter.next();
      ArtifactReadable artifact1Actual = iter.next();
      builder = query.fromBranch(CoreBranches.COMMON);
      builder.fromTransaction(transactions[2]);
      builder.andExists(CoreAttributeTypes.GeneralStringData, CoreAttributeTypes.Annotation);
      builder.andIds(artifact1, artifact2);
      builder.includeDeletedArtifacts();
      builder.includeDeletedAttributes();
      builder.includeDeletedRelations();
      builder.andRelatedTo(Default_Hierarchical__Parent, artifact1Actual);
      artifacts = builder.getResults();
      verifyHistoricalArtifacts(artifacts, artifact1, null);
   }

   public void verifyHistoricalArtifacts(ResultSet<ArtifactReadable> artifacts, ArtifactId artifact, ArtifactId artifact1) throws OseeCoreException {
      int size = artifacts.size();
      int expectedSize = 0;
      if (artifact != null) {
         expectedSize++;
      }
      if (artifact1 != null) {
         expectedSize++;
      }
      assertEquals(expectedSize, size);
      if (size > 0) {
         for (ArtifactReadable art : artifacts) {
            if ((artifact != null) && art.matches(artifact)) {
               assertEquals(1,
                  art.getAttributeCount(CoreAttributeTypes.GeneralStringData, DeletionFlag.INCLUDE_DELETED));
               assertEquals(1, art.getAttributeCount(CoreAttributeTypes.PublishInline, DeletionFlag.INCLUDE_DELETED));
            } else if ((artifact1 != null) && art.matches(artifact1)) {
               assertEquals(1, art.getAttributeCount(CoreAttributeTypes.Annotation, DeletionFlag.INCLUDE_DELETED));
            } else {
               assertTrue("Unexpected artifact", false);
            }
         }
      }
   }

   @Test
   public void testArtifactGetTransaction() throws OseeCoreException {
      TransactionBuilder tx = createTx();

      String guid = tx.createArtifact(CoreArtifactTypes.Component, "A component").getGuid();
      int startingTx = tx.commit().getGuid();

      ArtifactReadable artifact = query.fromBranch(CoreBranches.COMMON).andGuid(guid).getResults().getExactlyOne();
      assertEquals(startingTx, artifact.getTransaction());

      TransactionBuilder tx2 = createTx();
      tx2.setName(artifact, "Modified - component");
      int lastTx = tx2.commit().getGuid();

      assertTrue(startingTx != lastTx);

      ArtifactReadable currentArtifact =
         query.fromBranch(CoreBranches.COMMON).andGuid(guid).getResults().getExactlyOne();
      assertEquals(lastTx, currentArtifact.getTransaction());
   }

   @Test
   public void testRelate() throws OseeCoreException {
      TransactionBuilder tx1 = createTx();
      ArtifactId art1 = tx1.createArtifact(CoreArtifactTypes.Component, "A component");
      ArtifactId art2 = tx1.createArtifact(CoreArtifactTypes.User, "User Artifact");
      tx1.relate(art1, CoreRelationTypes.Users_User, art2);
      tx1.commit();

      ArtifactReadable artifact = query.fromBranch(CoreBranches.COMMON).andIds(art1).getResults().getExactlyOne();
      assertEquals("A component", artifact.getName());

      ResultSet<ArtifactReadable> related = artifact.getRelated(CoreRelationTypes.Users_User);
      assertEquals(1, related.size());
      assertEquals("User Artifact", related.getExactlyOne().getName());
   }

   @Test
   public void testRelateTypeCheckException() throws OseeCoreException {
      TransactionBuilder tx1 = createTx();
      ArtifactId art1 = tx1.createArtifact(CoreArtifactTypes.Component, "A component");
      ArtifactId art2 = tx1.createArtifact(CoreArtifactTypes.User, "User Artifact");

      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("Relation validity error for [artifact type[Component]");
      thrown.expectMessage("only items of type [User] are allowed");
      tx1.relate(art2, CoreRelationTypes.Users_User, art1);
   }

   @Test
   public void testRelateWithSortType() throws OseeCoreException {
      TransactionBuilder tx1 = createTx();
      ArtifactId art1 = tx1.createArtifact(CoreArtifactTypes.Component, "A component");
      ArtifactId art2 = tx1.createArtifact(CoreArtifactTypes.Component, "B component");
      ArtifactId art3 = tx1.createArtifact(CoreArtifactTypes.Component, "C component");
      tx1.addChildren(art1, art2, art3);
      int tx1Id = tx1.commit().getGuid();

      QueryBuilder art1Query = query.fromBranch(CoreBranches.COMMON).andIds(art1);

      ArtifactReadable artifact = art1Query.getResults().getExactlyOne();
      assertEquals("A component", artifact.getName());
      assertEquals(tx1Id, artifact.getTransaction());

      ResultSet<ArtifactReadable> children = artifact.getChildren();
      assertEquals(2, children.size());

      Iterator<ArtifactReadable> iterator = children.iterator();
      assertEquals("B component", iterator.next().getName());
      assertEquals("C component", iterator.next().getName());

      TransactionBuilder tx2 = createTx();
      ArtifactId art4 = tx2.createArtifact(Component, "D component");
      tx2.relate(art1, Default_Hierarchical__Child, art4, LEXICOGRAPHICAL_DESC);
      int tx2Id = tx2.commit().getGuid();

      ArtifactReadable artifact21 = art1Query.getResults().getExactlyOne();
      assertEquals("A component", artifact21.getName());
      assertEquals(tx2Id, artifact21.getTransaction());

      ResultSet<ArtifactReadable> children2 = artifact21.getChildren();
      assertEquals(3, children2.size());

      Iterator<ArtifactReadable> iterator2 = children2.iterator();
      assertEquals("D component", iterator2.next().getName());
      assertEquals("C component", iterator2.next().getName());
      assertEquals("B component", iterator2.next().getName());
   }

   @Test
   public void testSetRelations() throws OseeCoreException {
      TransactionBuilder tx1 = createTx();
      ArtifactId art1 = tx1.createArtifact(Component, "A component");
      ArtifactId art2 = tx1.createArtifact(Component, "B component");
      ArtifactId art3 = tx1.createArtifact(Component, "C component");
      ArtifactId art4 = tx1.createArtifact(Component, "D component");
      tx1.addChildren(art1, art2);
      tx1.commit();

      ArtifactReadable artifact1 = query.fromBranch(CoreBranches.COMMON).andIds(art1).getResults().getExactlyOne();
      assertEquals("A component", artifact1.getName());

      ResultSet<ArtifactReadable> children = artifact1.getChildren();
      assertEquals(1, children.size());

      Iterator<ArtifactReadable> iterator = children.iterator();
      ArtifactReadable artifact2 = children.iterator().next();

      assertEquals("B component", artifact2.getName());
      assertEquals(artifact1, artifact2.getParent());
      assertEquals(art2, artifact2);

      TransactionBuilder tx2 = createTx();
      tx2.setRelations(art1, Default_Hierarchical__Child, Arrays.asList(art3, art4));
      tx2.commit();

      artifact1 = query.fromBranch(CoreBranches.COMMON).andIds(art1).getResults().getExactlyOne();
      assertEquals("A component", artifact1.getName());

      children = artifact1.getChildren();
      assertEquals(2, children.size());

      iterator = children.iterator();
      ArtifactReadable artifact3 = iterator.next();
      ArtifactReadable artifact4 = iterator.next();

      assertEquals("C component", artifact3.getName());
      assertEquals("D component", artifact4.getName());

      assertEquals(artifact1, artifact3.getParent());
      assertEquals(artifact1, artifact4.getParent());

      assertEquals(art3, artifact3);
      assertEquals(art4, artifact4);

      artifact2 = query.fromBranch(CoreBranches.COMMON).andIds(art2).getResults().getExactlyOne();
      assertEquals("B component", artifact2.getName());
      assertNull(artifact2.getParent());
      assertEquals(art2, artifact2);
   }

   @Test
   public void testAddChildren() throws OseeCoreException {
      TransactionBuilder tx1 = createTx();
      ArtifactId art1 = tx1.createArtifact(Component, "A component");
      ArtifactId art2 = tx1.createArtifact(Component, "C component");
      ArtifactId art3 = tx1.createArtifact(Component, "B component");
      tx1.commit();

      TransactionBuilder tx2 = createTx();
      tx2.addChildren(art1, art2, art3);
      tx2.commit();

      ArtifactReadable artifact1 = query.fromBranch(CoreBranches.COMMON).andIds(art1).getResults().getExactlyOne();
      assertEquals("A component", artifact1.getName());

      ResultSet<ArtifactReadable> children = artifact1.getChildren();
      assertEquals(2, children.size());

      Iterator<ArtifactReadable> iterator = children.iterator();
      ArtifactReadable artifact3 = iterator.next();
      ArtifactReadable artifact2 = iterator.next();

      assertEquals(art3, artifact3);
      assertEquals(art2, artifact2);

      assertEquals("B component", artifact3.getName());
      assertEquals("C component", artifact2.getName());

      assertEquals(artifact1, artifact2.getParent());
      assertEquals(artifact1, artifact3.getParent());
   }

   @Test
   public void testSetRationale() throws OseeCoreException {
      String rationale = "This is my rationale";

      TransactionBuilder tx1 = createTx();
      ArtifactId art1 = tx1.createArtifact(Component, "A component");
      ArtifactId art2 = tx1.createArtifact(Component, "B component");

      tx1.relate(art1, Default_Hierarchical__Child, art2);
      tx1.setRationale(art1, Default_Hierarchical__Child, art2, rationale);

      tx1.commit();

      ArtifactReadable artifact = query.fromBranch(CoreBranches.COMMON).andIds(art1).getResults().getExactlyOne();
      assertEquals("A component", artifact.getName());

      ResultSet<ArtifactReadable> children = artifact.getChildren();
      assertEquals(1, children.size());

      ArtifactReadable otherArtifact = children.getExactlyOne();
      assertEquals("B component", otherArtifact.getName());

      String actual1 = artifact.getRationale(Default_Hierarchical__Child, otherArtifact);
      assertEquals(rationale, actual1);

      String actual2 = otherArtifact.getRationale(Default_Hierarchical__Parent, artifact);
      assertEquals(rationale, actual2);
   }

   @Test
   public void testUnrelate() throws OseeCoreException {
      TransactionBuilder tx1 = createTx();
      ArtifactId art1 = tx1.createArtifact(Component, "A component");
      ArtifactId art2 = tx1.createArtifact(Component, "C component");
      ArtifactId art3 = tx1.createArtifact(Component, "B component");
      tx1.addChildren(art1, art2, art3);
      ArtifactId art4 = tx1.createArtifact(GeneralDocument, "Document");
      tx1.relate(art1, Dependency__Dependency, art4);
      tx1.commit();

      ArtifactReadable artifact4 = query.fromBranch(CoreBranches.COMMON).andIds(art4).getResults().getExactlyOne();
      assertEquals(art4, artifact4);

      ArtifactReadable artifact1 = artifact4.getRelated(CoreRelationTypes.Dependency__Artifact).getExactlyOne();
      assertEquals(art1, artifact1);

      Iterator<ArtifactReadable> iterator = artifact1.getChildren().iterator();
      assertEquals(art3, iterator.next());
      assertEquals(art2, iterator.next());

      // Un-relate a child
      TransactionBuilder tx2 = createTx();
      tx2.unrelate(art1, Default_Hierarchical__Child, art2);
      tx2.commit();

      artifact4 = query.fromBranch(CoreBranches.COMMON).andIds(art4).getResults().getExactlyOne();
      assertEquals(art4, artifact4);

      artifact1 = artifact4.getRelated(CoreRelationTypes.Dependency__Artifact).getExactlyOne();
      assertEquals(art1, artifact1);

      assertEquals(art3, artifact1.getChildren().getExactlyOne());
   }

   @Test
   public void testUnrelateFromAllByType() throws OseeCoreException {
      TransactionBuilder tx1 = createTx();
      ArtifactId art1 = tx1.createArtifact(Component, "A component");
      ArtifactId art2 = tx1.createArtifact(Component, "C component");
      ArtifactId art3 = tx1.createArtifact(Component, "B component");
      tx1.addChildren(art1, art2, art3);

      ArtifactId art4 = tx1.createArtifact(GeneralDocument, "Document");
      tx1.relate(art1, Dependency__Dependency, art4);
      tx1.commit();

      ArtifactReadable artifact4 = query.fromBranch(COMMON).andIds(art4).getResults().getExactlyOne();
      assertEquals(art4, artifact4);

      ArtifactReadable artifact1 = artifact4.getRelated(Dependency__Artifact).getExactlyOne();
      assertEquals(art1, artifact1);

      Iterator<ArtifactReadable> iterator = artifact1.getChildren().iterator();
      assertEquals(art3, iterator.next());
      assertEquals(art2, iterator.next());

      // Unrelate All children
      TransactionBuilder tx2 = createTx();
      tx2.unrelateFromAll(Default_Hierarchical__Parent, art1);
      tx2.commit();

      artifact4 = query.fromBranch(COMMON).andIds(art4).getResults().getExactlyOne();
      assertEquals(art4, artifact4);

      artifact1 = artifact4.getRelated(Dependency__Artifact).getExactlyOne();
      assertEquals(art1, artifact1);

      assertEquals(true, artifact1.getChildren().isEmpty());
   }

   @Test
   public void testUnrelateFromAll() throws OseeCoreException {
      ArtifactReadable artifact1;
      ArtifactReadable artifact2;
      ArtifactReadable artifact3;
      ArtifactReadable artifact4;

      TransactionBuilder tx1 = createTx();
      ArtifactId art1 = tx1.createArtifact(Component, "A component");
      ArtifactId art2 = tx1.createArtifact(Component, "C component");
      ArtifactId art3 = tx1.createArtifact(Component, "B component");
      tx1.addChildren(art1, art2, art3);

      ArtifactId art4 = tx1.createArtifact(GeneralDocument, "Document");
      tx1.relate(art1, Dependency__Dependency, art4);
      tx1.commit();

      artifact4 = query.fromBranch(COMMON).andIds(art4).getResults().getExactlyOne();
      assertEquals(art4, artifact4);

      artifact1 = artifact4.getRelated(Dependency__Artifact).getExactlyOne();
      assertEquals(art1, artifact1);

      Iterator<ArtifactReadable> iterator = artifact1.getChildren().iterator();
      assertEquals(art3, iterator.next());
      assertEquals(art2, iterator.next());

      TransactionBuilder tx2 = createTx();
      tx2.unrelateFromAll(art1);
      tx2.commit();

      ResultSet<ArtifactReadable> arts =
         query.fromBranch(COMMON).andIds(art1, art2, art3, art4).includeDeletedArtifacts().getResults();
      Iterator<ArtifactReadable> iterator2 = arts.iterator();
      artifact1 = iterator2.next();
      artifact2 = iterator2.next();
      artifact3 = iterator2.next();
      artifact4 = iterator2.next();

      assertEquals(art1, artifact1);
      assertEquals(art2, artifact2);
      assertEquals(art3, artifact3);
      assertEquals(art4, artifact4);

      assertEquals(true, artifact1.getChildren().isEmpty());
      assertEquals(true, artifact1.getRelated(Dependency__Dependency).isEmpty());

      assertNull(artifact2.getParent());
      assertNull(artifact3.getParent());

      assertEquals(true, artifact4.getRelated(Dependency__Artifact).isEmpty());
   }

   @Test
   public void testMultiAttriVersionsWriteAndLoading() {
      TransactionBuilder tx = createTx();
      ArtifactId art1 = tx.createArtifact(Component, "A component");
      tx.setSoleAttributeFromString(art1, CoreAttributeTypes.Annotation, "write1");
      tx.commit();

      tx = createTx();
      tx.setSoleAttributeFromString(art1, CoreAttributeTypes.Annotation, "write2");
      tx.commit();

      tx = createTx();
      tx.setSoleAttributeFromString(art1, CoreAttributeTypes.Annotation, "write3");
      tx.commit();

      tx = createTx();
      tx.setSoleAttributeFromString(art1, CoreAttributeTypes.Annotation, "write4");
      tx.commit();

      tx = createTx();
      tx.setSoleAttributeFromString(art1, CoreAttributeTypes.Annotation, "write5");
      TransactionReadable lastTx = tx.commit();

      ArtifactReadable art = query.fromBranch(COMMON).andIds(art1).getResults().getExactlyOne();
      ResultSet<? extends AttributeReadable<Object>> attributes = art.getAttributes(CoreAttributeTypes.Annotation);

      assertEquals(1, attributes.size());
      assertEquals("write5", attributes.getExactlyOne().getValue());

      QueryBuilder builder = query.fromBranch(COMMON).fromTransaction(lastTx.getGuid()).andIds(art1);
      ResultSet<ArtifactReadable> results = builder.getResults();
      art = results.getExactlyOne();
      attributes = art.getAttributes(CoreAttributeTypes.Annotation);
      assertEquals(1, attributes.size());
      assertEquals("write5", attributes.getExactlyOne().getValue());
   }

   @Test
   public void testMultiRelationVersionsWriteAndLoading() {
      TransactionBuilder tx = createTx();
      ArtifactId art1 = tx.createArtifact(CoreArtifactTypes.Component, "A component");
      ArtifactId art2 = tx.createArtifact(CoreArtifactTypes.User, "User Artifact");
      tx.relate(art1, CoreRelationTypes.Users_User, art2, "rationale1");
      tx.commit();

      tx = createTx();
      tx.setRationale(art1, CoreRelationTypes.Users_User, art2, "rationale2");
      tx.commit();

      tx = createTx();
      tx.setRationale(art1, CoreRelationTypes.Users_User, art2, "rationale3");
      tx.commit();

      tx = createTx();
      tx.setRationale(art1, CoreRelationTypes.Users_User, art2, "rationale4");
      tx.commit();

      tx = createTx();
      tx.setRationale(art1, CoreRelationTypes.Users_User, art2, "rationale5");
      TransactionReadable lastTx = tx.commit();

      ArtifactReadable art = query.fromBranch(COMMON).andIds(art1).getResults().getExactlyOne();
      ResultSet<ArtifactReadable> related = art.getRelated(CoreRelationTypes.Users_User);
      assertEquals(1, related.size());
      ArtifactReadable other = related.getExactlyOne();
      String rationale = art.getRationale(CoreRelationTypes.Users_User, other);

      assertEquals("rationale5", rationale);

      art = query.fromBranch(COMMON).fromTransaction(lastTx.getGuid()).andIds(art1).getResults().getExactlyOne();
      related = art.getRelated(CoreRelationTypes.Users_User);
      assertEquals(1, related.size());
      other = related.getExactlyOne();
      rationale = art.getRationale(CoreRelationTypes.Users_User, other);

      assertEquals("rationale5", rationale);
   }

   @Test
   public void testRelateUnrelateMultipleTimes() {
      TransactionBuilder tx = createTx();
      ArtifactId art1 = tx.createArtifact(CoreArtifactTypes.Component, "A component");
      ArtifactId art2 = tx.createArtifact(CoreArtifactTypes.User, "User Artifact");
      tx.relate(art1, CoreRelationTypes.Users_User, art2, "rationale1");
      tx.commit();

      ArtifactReadable art = query.fromBranch(COMMON).andIds(art1).getResults().getExactlyOne();
      ArtifactReadable otherSide = art.getRelated(CoreRelationTypes.Users_User).getExactlyOne();
      assertEquals(true, art.areRelated(CoreRelationTypes.Users_User, otherSide));

      tx = createTx();
      tx.unrelate(art1, CoreRelationTypes.Users_User, art2);
      tx.commit();

      art = query.fromBranch(COMMON).andIds(art1).getResults().getExactlyOne();
      ResultSet<ArtifactReadable> otherSideResults = art.getRelated(CoreRelationTypes.Users_User);
      assertEquals(true, otherSideResults.isEmpty());

      tx = createTx();
      tx.relate(art1, CoreRelationTypes.Users_User, art2);
      tx.commit();

      art = query.fromBranch(COMMON).andIds(art1).getResults().getExactlyOne();
      otherSide = art.getRelated(CoreRelationTypes.Users_User).getExactlyOne();
      assertEquals(true, art.areRelated(CoreRelationTypes.Users_User, otherSide));
   }

   @Test
   public void testSetTransactionComment() throws Exception {
      TransactionBuilder tx = createTx();
      ArtifactId art1 = tx.createArtifact(CoreArtifactTypes.Component, "A component");
      ArtifactId art2 = tx.createArtifact(CoreArtifactTypes.User, "User Artifact");
      tx.relate(art1, CoreRelationTypes.Users_User, art2, "rationale1");
      TransactionReadable transaction = tx.commit();

      assertEquals(testName.getMethodName(), transaction.getComment());

      String expectedComment = "My new Comment";
      txFactory.setTransactionComment(transaction, expectedComment).call();

      TransactionReadable actual = query.transactionQuery().andTxId(transaction.getGuid()).getResults().getExactlyOne();
      assertEquals(transaction.getGuid(), actual.getGuid());
      assertEquals(expectedComment, actual.getComment());
   }

   private TransactionBuilder createTx() throws OseeCoreException {
      return txFactory.createTransaction(COMMON, userArtifact, testName.getMethodName());
   }

   private ArtifactReadable getSystemUser() throws OseeCoreException {
      return query.fromBranch(CoreBranches.COMMON).andIds(SystemUser.OseeSystem).getResults().getExactlyOne();
   }

   private void checkTransaction(TransactionReadable previousTx, TransactionReadable newTx, org.eclipse.osee.orcs.data.BranchReadable branch, String comment, ArtifactReadable user) throws OseeCoreException {
      assertTrue(previousTx.getLocalId() < newTx.getLocalId());
      assertEquals(comment, newTx.getComment());
      assertEquals(branch.getUuid(), newTx.getBranchId());
      assertEquals(TransactionDetailsType.NonBaselined, newTx.getTxType());
      assertEquals(user.getLocalId().intValue(), newTx.getAuthorId());
      assertEquals(0, newTx.getCommit());
      assertTrue(previousTx.getDate().before(newTx.getDate()));
   }

}