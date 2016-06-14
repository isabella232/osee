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
package org.eclipse.osee.orcs.core.internal.branch;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.BranchReadable;
import org.eclipse.osee.orcs.data.CreateBranchData;
import org.eclipse.osee.orcs.search.BranchQuery;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.eclipse.osee.orcs.search.TransactionQuery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**  */
public class BranchDataFactoryTest {

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   // @formatter:off
   @Mock private BranchQuery branchQuery;
   @Mock private TransactionQuery txQuery;
   @Mock private QueryFactory queryFactory;
   @Mock private ArtifactReadable author;
   @Mock private ArtifactReadable associatedArtifact;
   @Mock private ResultSet<TransactionToken> results;
   @Mock private ResultSet<BranchReadable> branchResults;
   @Mock private BranchReadable parentBranch;
   // @formatter:on

   private BranchDataFactory factory;
   private final TransactionToken txRecord = TransactionToken.valueOf(99, parentBranch);

   @Before
   public void init() {
      MockitoAnnotations.initMocks(this);

      when(author.getLocalId()).thenReturn(55);
      when(associatedArtifact.getLocalId()).thenReturn(66);
      when(queryFactory.transactionQuery()).thenReturn(txQuery);
      when(queryFactory.branchQuery()).thenReturn(branchQuery);
      factory = new BranchDataFactory(queryFactory);

      when(branchQuery.andIds(txRecord.getBranch())).thenReturn(branchQuery);
      when(branchQuery.getResults()).thenReturn(branchResults);
      when(branchResults.getExactlyOne()).thenReturn(parentBranch);

      when(txQuery.andIsHead(any(BranchId.class))).thenReturn(txQuery);
      when(txQuery.getTokens()).thenReturn(results);
      when(results.getExactlyOne()).thenReturn(txRecord);

      when(txQuery.andTxId(txRecord)).thenReturn(txQuery);

      when(parentBranch.getName()).thenReturn("testParentBranchName");
      when(parentBranch.getId()).thenReturn(44L);
   }

   @Test
   public void testDataForTopLevelBranch() throws OseeCoreException {
      IOseeBranch branch = TokenFactory.createBranch("testDataForTopLevelBranch");
      CreateBranchData result = factory.createTopLevelBranchData(branch, author);

      verify(txQuery).andIsHead(CoreBranches.SYSTEM_ROOT);

      String comment = String.format("New Branch from %s (%s)", CoreBranches.SYSTEM_ROOT.getName(), txRecord);
      assertData(result, branch.getName(), branch.getId(), BranchType.BASELINE, comment, txRecord, author, null, false);
   }

   @Test
   public void testDataForBaselineBranch() throws OseeCoreException {
      IOseeBranch branch = TokenFactory.createBranch("testDataForBaselineBranch");
      CreateBranchData result = factory.createBaselineBranchData(branch, author, parentBranch, associatedArtifact);

      verify(txQuery).andIsHead(parentBranch);

      String comment = String.format("New Branch from %s (%s)", parentBranch.getName(), txRecord.getId());
      assertData(result, branch.getName(), branch.getId(), BranchType.BASELINE, comment, txRecord, author,
         associatedArtifact, false);
   }

   @Test
   public void testDataForWorkingBranch() throws OseeCoreException {
      IOseeBranch branch = TokenFactory.createBranch("testDataForWorkingBranch");

      CreateBranchData result = factory.createWorkingBranchData(branch, author, parentBranch, associatedArtifact);
      verify(txQuery).andIsHead(parentBranch);

      String comment = String.format("New Branch from %s (%s)", parentBranch.getName(), txRecord.getId());
      assertData(result, branch.getName(), branch.getId(), BranchType.WORKING, comment, txRecord, author,
         associatedArtifact, false);
   }

   @Test
   public void testDataForCopyTxBranch() throws OseeCoreException {
      IOseeBranch branch = TokenFactory.createBranch("testDataForCopyTxBranch");

      CreateBranchData result = factory.createCopyTxBranchData(branch, author, txRecord, null);

      verify(txQuery).andTxId(txRecord);
      verify(branchQuery).andIds(txRecord.getBranch());

      String comment = String.format("Transaction %d copied from %s to create Branch %s", txRecord.getId(),
         parentBranch.getName(), branch.getName());
      assertData(result, branch.getName(), branch.getId(), BranchType.WORKING, comment, txRecord, author, null, true);
   }

   @Test
   public void testDataForPortBranch() throws OseeCoreException {
      IOseeBranch branch = TokenFactory.createBranch("testDataForPortBranch");

      CreateBranchData result = factory.createPortBranchData(branch, author, txRecord, null);

      verify(txQuery).andTxId(txRecord);
      verify(branchQuery).andIds(txRecord.getBranch());

      String comment = String.format("Transaction %d ported from %s to create Branch %s", txRecord.getId(),
         parentBranch.getName(), branch.getName());
      assertData(result, branch.getName(), branch.getId(), BranchType.PORT, comment, txRecord, author, null, true);
   }

   private static void assertData(CreateBranchData actual, String branchName, Long branchUuid, BranchType type, String comment, TransactionId fromTx, ArtifactReadable author, ArtifactReadable associatedArtifact, boolean isCopyFromTx) {
      assertEquals(branchName, actual.getName());
      assertEquals(branchUuid, actual.getGuid());

      assertEquals(type, actual.getBranchType());
      assertEquals(comment, actual.getCreationComment());
      assertEquals(fromTx, actual.getFromTransaction());

      assertEquals(-1, actual.getMergeAddressingQueryId());
      assertEquals(-1L, actual.getMergeDestinationBranchId());

      assertEquals(author, actual.getUserArtifact());
      assertEquals(author.getLocalId(), actual.getUserArtifactId());

      assertEquals(associatedArtifact, actual.getAssociatedArtifact());

      int assocArtId = associatedArtifact == null ? -1 : associatedArtifact.getLocalId();
      assertEquals(assocArtId, actual.getAssociatedArtifactId());

      assertEquals(isCopyFromTx, actual.isTxCopyBranchType());
   }
}
