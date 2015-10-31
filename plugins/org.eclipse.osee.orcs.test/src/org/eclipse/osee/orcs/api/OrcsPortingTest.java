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

import static org.eclipse.osee.orcs.OrcsIntegrationRule.integrationRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.OrcsBranch;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.TransactionReadable;
import org.eclipse.osee.orcs.db.mock.OsgiService;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.eclipse.osee.orcs.transaction.TransactionBuilder;
import org.eclipse.osee.orcs.transaction.TransactionFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;

/**
 * @author David W. Miller
 */
public class OrcsPortingTest {

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   @Rule
   public TestRule osgi = integrationRule(this);

   @OsgiService
   private OrcsApi orcsApi;

   private OrcsBranch branchApi;
   private QueryFactory query;
   private TransactionFactory txFactory;
   private final String branchString = "CopiedTxBranch";

   private ArtifactReadable author;

   @Before
   public void setUp() throws Exception {
      branchApi = orcsApi.getBranchOps();
      query = orcsApi.getQueryFactory();
      txFactory = orcsApi.getTransactionFactory();

      author = query.fromBranch(CoreBranches.COMMON).andIds(SystemUser.OseeSystem).getResults().getExactlyOne();
   }

   @Test
   public void testCreateBranch() throws Exception {
      String artifactGuid = GUID.create();

      String assocArtifactGuid = GUID.create();
      setupAssociatedArtifact(assocArtifactGuid);

      TransactionReadable mainBranchTx = createBaselineBranchAndArtifacts(artifactGuid);
      IOseeBranch branch = TokenFactory.createBranch(mainBranchTx.getBranchId());
      TransactionReadable transactionToCopy = createWorkingBranchChanges(branch, artifactGuid);

      IOseeBranch copyTxBranch = createCopyFromTransactionBranch(transactionToCopy, assocArtifactGuid);
      TransactionReadable finalTx = commitToDestinationBranch(copyTxBranch);

      // now check to make sure everything is as expected
      // we should have a SoftwareRequirement named "SecondRequirement" with an attribute named "test changed" (changed on child branch to this)
      // the attribute for the SecondRequirement should not be named "test changed again" (on the branch after the copy from)
      // we should have a folder named "childBranch folder", but no folder named "folder after transaction"
      ResultSet<ArtifactReadable> artifacts =
         query.fromBranch(finalTx.getBranchId()).andTypeEquals(CoreArtifactTypes.Artifact).getResults();
      for (ArtifactReadable art : artifacts) {
         if (art.isOfType(CoreArtifactTypes.SoftwareRequirement)) {
            assertEquals(2, art.getAttributes().size());

            assertEquals(artifactGuid, art.getGuid());
            assertEquals("SecondRequirement", art.getName());

            String actual = art.getSoleAttributeAsString(CoreAttributeTypes.Subsystem);
            assertEquals("test changed", actual);
            // if there is a requirement with an attribute other than "test changed" then the above should fail
         } else if (art.isOfType(CoreArtifactTypes.Folder)) {
            assertEquals("childBranch folder", art.getName());
            // if there is any other folder like "folder after transaction" then the above should fail
         } else {
            fail("incorrect artifact found on branch");
         }
      }

   }

   @Test
   public void testForMultiplePortBranches() throws Exception {
      String artifactGuid = GUID.create();
      String differentGuid = GUID.create();

      String assocArtifactGuid = GUID.create();
      setupAssociatedArtifact(assocArtifactGuid);

      TransactionReadable mainBranchTx = createBaselineBranchAndArtifacts(artifactGuid);
      TransactionReadable differentBranchTx = createBaselineBranchAndArtifacts(differentGuid);

      IOseeBranch copyTxBranch = createCopyFromTransactionBranch(mainBranchTx, assocArtifactGuid);
      assertNotNull(copyTxBranch);

      // There should only be one Port Branch per associated artifact. Expecting an exception
      thrown.expect(OseeCoreException.class);
      thrown.expectMessage(String.format("Existing port branch creation detected for [%s]", branchString));
      createCopyFromTransactionBranch(differentBranchTx, assocArtifactGuid);
      fail(); // should never get here due to thrown exception
   }

   private TransactionReadable createBaselineBranchAndArtifacts(String artifactGuid) throws Exception {
      // set up the main branch
      IOseeBranch branch = TokenFactory.createBranch("MainFromBranch");
      branchApi.createTopLevelBranch(branch, author).call();

      // baseline branch - set up artifacts on the main branch, and on the child branch
      // first, add some transaction on the main branch, then create the child branch
      TransactionBuilder tx = txFactory.createTransaction(branch, author, "add base requirement");
      ArtifactId baseReq = tx.createArtifact(CoreArtifactTypes.SoftwareRequirement, "BaseRequirement");
      tx.setSoleAttributeFromString(baseReq, CoreAttributeTypes.Subsystem, "Test");

      TransactionBuilder tx2 = txFactory.createTransaction(branch, author, "add another requirement");
      ArtifactId nextReq = tx2.createArtifact(CoreArtifactTypes.SoftwareRequirement, "SecondRequirement", artifactGuid);
      tx2.setSoleAttributeFromString(nextReq, CoreAttributeTypes.Subsystem, "Test2");

      return tx2.commit();
   }

   private void setupAssociatedArtifact(String associatedArtifactGuid) throws Exception {
      TransactionBuilder tx = txFactory.createTransaction(CoreBranches.COMMON, author, "setup associated artifact");
      // normally the associated artifact would be a work flow, 
      // but since that is an ATS construct, we are just using a requirement
      tx.createArtifact(CoreArtifactTypes.Requirement, "AssociatedArtifact", associatedArtifactGuid);
      tx.commit();
   }

   private TransactionReadable createWorkingBranchChanges(IOseeBranch parentBranch, String artifactToModifyGuid) throws Exception {
      // set up the child branch to copy to

      IOseeBranch childBranch = TokenFactory.createBranch("childBranch");
      branchApi.createWorkingBranch(childBranch, author, parentBranch, null).call();

      TransactionBuilder tx3 = txFactory.createTransaction(childBranch, author, "update second requirement");
      ArtifactReadable readableReq2 =
         query.fromBranch(childBranch).andGuid(artifactToModifyGuid).getResults().getExactlyOne();

      // modifying this artifact should cause it to get introduced
      tx3.setSoleAttributeFromString(readableReq2, CoreAttributeTypes.Subsystem, "test changed");

      // new artifacts should come across as new
      tx3.createArtifact(CoreArtifactTypes.Folder, "childBranch folder");

      // set this aside to use in the copy from transaction for the branch
      TransactionReadable transactionToCopy = tx3.commit();

      // make an additional transaction to make sure it doesn't get copied also
      TransactionBuilder tx4 = txFactory.createTransaction(childBranch, author, "after second requirement");
      ArtifactReadable readableReq2verA =
         query.fromBranch(childBranch).andGuid(artifactToModifyGuid).getResults().getExactlyOne();

      // modifying this artifact should cause it to get introduced
      tx4.setSoleAttributeFromString(readableReq2verA, CoreAttributeTypes.Subsystem, "test changed again");

      // additional artifacts should not come across
      tx4.createArtifact(CoreArtifactTypes.Folder, "folder after transaction");
      tx4.commit();

      return transactionToCopy;
   }

   private IOseeBranch createCopyFromTransactionBranch(TransactionReadable transactionToCopy, String assocArtifactGuid) throws Exception {
      // create the branch with the copied transaction
      IOseeBranch branch = TokenFactory.createBranch(branchString);

      // get the setup associated artifact - this is for a later test to make sure the branch is not duplicated
      // there should only be one port branch per associated artifact
      ArtifactReadable readableReq =
         query.fromBranch(CoreBranches.COMMON).andGuid(assocArtifactGuid).getResults().getExactlyOne();

      assertNotNull(readableReq);
      // the new branch will contain two transactions -
      return branchApi.createPortBranch(branch, author, transactionToCopy, readableReq).call();
   }

   private TransactionReadable commitToDestinationBranch(IOseeBranch copyTxBranch) throws Exception {
      IOseeBranch destinationBranch = TokenFactory.createBranch("IndepToBranch");
      branchApi.createTopLevelBranch(destinationBranch, author).call();
      return branchApi.commitBranch(author, copyTxBranch, destinationBranch).call();
   }
}
