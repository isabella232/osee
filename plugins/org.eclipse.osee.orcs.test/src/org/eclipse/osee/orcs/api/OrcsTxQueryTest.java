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
package org.eclipse.osee.orcs.api;

import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import static org.eclipse.osee.framework.core.enums.CoreBranches.SYSTEM_ROOT;
import static org.eclipse.osee.framework.core.enums.DemoBranches.CIS_Bld_1;
import static org.eclipse.osee.framework.core.enums.DemoBranches.SAW_Bld_1;
import static org.eclipse.osee.framework.core.enums.DemoBranches.SAW_Bld_2;
import static org.eclipse.osee.framework.core.enums.SystemUser.OseeSystem;
import static org.eclipse.osee.framework.core.enums.TransactionDetailsType.Baselined;
import static org.eclipse.osee.framework.core.enums.TransactionDetailsType.NonBaselined;
import static org.eclipse.osee.orcs.OrcsIntegrationRule.integrationRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.TransactionReadable;
import org.eclipse.osee.orcs.db.mock.OsgiService;
import org.eclipse.osee.orcs.search.Operator;
import org.eclipse.osee.orcs.search.QueryBuilder;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.eclipse.osee.orcs.search.TransactionQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;

/**
 * @author Roberto E. Escobar
 */
public class OrcsTxQueryTest {

   @Rule
   public TestRule osgi = integrationRule(this);

   @Rule
   public TestName testName = new TestName();

   @OsgiService
   private OrcsApi orcsApi;

   private final static DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
   private QueryFactory factory;
   private final ArtifactId JoeSmith = ArtifactId.valueOf(61106791);
   private final Long CommonBranchHeadTransaction = 117L;
   private final String CommonBranchTransComment = "Create new Agile Feature Group";
   private final int NumberCommonTransactions = 77;

   @Before
   public void setup() {
      factory = orcsApi.getQueryFactory();
   }

   @Test
   public void testGetAll() throws Exception {
      TransactionQuery query = factory.transactionQuery();

      ResultSet<TransactionReadable> results = query.getResults();
      assertEquals(CommonBranchHeadTransaction, Long.valueOf(results.size()));

      List<TransactionReadable> transactions = results.getList();
      assertTx(transactions, Long.valueOf(4), Baselined, "Branch Creation for Common", COMMON, OseeSystem);
      assertTx(transactions, Long.valueOf(5), NonBaselined, "Add Types to Common Branch", COMMON, OseeSystem);
   }

   @Test
   public void testGetByTxId() throws OseeCoreException {
      TransactionQuery query = factory.transactionQuery();
      query.andTxId(3, 23);

      ResultSet<TransactionReadable> results = query.getResults();
      assertEquals(2, results.size());

      List<TransactionReadable> transactions = results.getList();
      assertTxExists(transactions, Long.valueOf(3));
      assertTxExists(transactions, Long.valueOf(23));

      Iterator<Long> ids = query.getResultsAsIds().iterator();
      assertEquals(Long.valueOf(3), ids.next());
      assertEquals(Long.valueOf(23), ids.next());
   }

   @Test
   public void testGetByCommentEquals() throws OseeCoreException {
      TransactionQuery query = factory.transactionQuery();
      query.andCommentEquals("Branch Creation for SAW_Bld_1");

      TransactionReadable actual = query.getResults().getExactlyOne();

      assertEquals(1, query.getCount());
      assertTx(Arrays.asList(actual), Long.valueOf(26), Baselined, "Branch Creation for SAW_Bld_1", SAW_Bld_1,
         OseeSystem);
      assertEquals(Long.valueOf(26), query.getResultsAsIds().getExactlyOne());
   }

   @Test
   public void testGetByCommentPatternEquals() throws OseeCoreException {
      TransactionQuery query = factory.transactionQuery();
      query.andCommentPattern("Branch Creation for.*");

      ResultSet<TransactionReadable> results = query.getResults();
      assertEquals(4, results.size());
      assertEquals(4, query.getCount());

   }

   @Test
   public void testGetByType() throws OseeCoreException {
      TransactionQuery query = factory.transactionQuery();

      query.andIs(Baselined);

      ResultSet<TransactionReadable> results = query.getResults();
      assertEquals(8, results.size());
      assertEquals(8, query.getCount());

      List<TransactionReadable> transactions = results.getList();
      assertTx(transactions, Long.valueOf(1), Baselined, "System Root Branch Creation", SYSTEM_ROOT, OseeSystem);
      assertTx(transactions, Long.valueOf(4), Baselined, "Branch Creation for Common", COMMON, OseeSystem);
      assertTx(transactions, Long.valueOf(26), Baselined, "Branch Creation for SAW_Bld_1", SAW_Bld_1, OseeSystem);
      assertTx(transactions, Long.valueOf(28), Baselined, "Branch Creation for CIS_Bld_1", CIS_Bld_1, OseeSystem);
      assertTx(transactions, Long.valueOf(66), Baselined, "Branch Creation for SAW_Bld_2", SAW_Bld_2, JoeSmith);
   }

   @Test
   public void testGetByBranchIds() throws OseeCoreException {
      TransactionQuery query = factory.transactionQuery();
      query.andBranchIds(5, 4);

      ResultSet<TransactionReadable> results = query.getResults();
      assertEquals(4, results.size());
      assertEquals(4, query.getCount());

      List<TransactionReadable> transactions = results.getList();
      assertTx(transactions, Long.valueOf(28), Baselined, "Branch Creation for CIS_Bld_1", CIS_Bld_1, OseeSystem);
      assertTx(transactions, Long.valueOf(66), Baselined, "Branch Creation for SAW_Bld_2", SAW_Bld_2, JoeSmith);

   }

   @Test
   public void testGetByBranchUuids() throws OseeCoreException {
      TransactionQuery query = factory.transactionQuery();
      query.andBranch(SAW_Bld_1, CIS_Bld_1);

      ResultSet<TransactionReadable> results = query.getResults();
      assertEquals(8, results.size());
      assertEquals(8, query.getCount());

      List<TransactionReadable> transactions = results.getList();
      //@formatter:off
      assertTx(transactions, Long.valueOf(26), Baselined,    "Branch Creation for SAW_Bld_1", SAW_Bld_1, OseeSystem);
      assertTx(transactions, Long.valueOf(27), NonBaselined, "DemoDatabaseConfig", SAW_Bld_1, OseeSystem);
      assertTx(transactions, Long.valueOf(28), Baselined,    "Branch Creation for CIS_Bld_1", CIS_Bld_1, OseeSystem);
      assertTx(transactions, Long.valueOf(29), NonBaselined, "DemoDatabaseConfig", CIS_Bld_1,OseeSystem);
      assertTx(transactions, Long.valueOf(62), NonBaselined, "ArtifactImportOperationFactory: Artifact Import Wizard transaction", SAW_Bld_1, JoeSmith);
      assertTx(transactions, Long.valueOf(63), NonBaselined, "ArtifactImportOperationFactory: Artifact Import Wizard transaction", SAW_Bld_1, JoeSmith);
      assertTx(transactions, Long.valueOf(64), NonBaselined, "ArtifactImportOperationFactory: Artifact Import Wizard transaction", SAW_Bld_1, JoeSmith);
      assertTx(transactions, Long.valueOf(65), NonBaselined, "Populate Demo DB - Create Traceability", SAW_Bld_1, JoeSmith);
      //@formatter:on

   }

   @Test
   public void testGetLTIdOperators() throws OseeCoreException {
      TransactionQuery query = factory.transactionQuery();
      query.andTxId(Operator.LESS_THAN_EQ, 2);
      ResultSet<TransactionReadable> results = query.getResults();
      assertEquals(2, results.size());
      assertEquals(2, query.getCount());

      List<TransactionReadable> transactions = results.getList();
      assertTxExists(transactions, Long.valueOf(1));
      assertTxExists(transactions, Long.valueOf(2));

      query = factory.transactionQuery();
      query.andTxId(Operator.LESS_THAN, 2);
      results = query.getResults();
      assertEquals(1, results.size());
      assertEquals(1, query.getCount());

      transactions = results.getList();
      assertTxExists(transactions, Long.valueOf(1));
   }

   @Test
   public void testGetGTIdOperators() throws OseeCoreException {
      TransactionQuery query = factory.transactionQuery();
      query.andTxId(Operator.GREATER_THAN_EQ, 21);
      ResultSet<TransactionReadable> results = query.getResults();
      assertEquals(CommonBranchHeadTransaction - 21 + 1, results.size());

      List<TransactionReadable> transactions = results.getList();
      for (Long x = Long.valueOf(21); x <= CommonBranchHeadTransaction; x++) {
         assertNotNull(getTransaction(transactions, x));
      }

      query = factory.transactionQuery();
      query.andTxId(Operator.GREATER_THAN, 21);
      results = query.getResults();
      assertEquals(CommonBranchHeadTransaction - 21, results.size());

      transactions = results.getList();
      Assert.assertNull(getTransaction(transactions, Long.valueOf(21)));
      for (Long x = Long.valueOf(22); x <= CommonBranchHeadTransaction; x++) {
         assertNotNull(getTransaction(transactions, x));
      }

   }

   @Test
   public void testGetEQIdOperators() throws OseeCoreException {
      TransactionQuery query = factory.transactionQuery();
      query.andTxId(Operator.EQUAL, 21);
      query.andBranch(COMMON);
      ResultSet<TransactionReadable> results = query.getResults();
      assertEquals(1, results.size());

      List<TransactionReadable> transactions = results.getList();
      assertTxExists(transactions, Long.valueOf(21));

      query = factory.transactionQuery();
      query.andTxId(Operator.NOT_EQUAL, 10);
      query.andBranch(COMMON);
      results = query.getResults();

      transactions = results.getList();
      assertEquals(NumberCommonTransactions - 1, results.size());
   }

   @Test
   public void testGetTwoIdOperators() throws OseeCoreException {
      TransactionQuery query = factory.transactionQuery();
      query.andTxId(Operator.GREATER_THAN, 21, Operator.LESS_THAN, 23);
      ResultSet<TransactionReadable> results = query.getResults();
      assertEquals(1, results.size());
      assertEquals(1, query.getCount());

      List<TransactionReadable> transactions = results.getList();
      assertTxExists(transactions, Long.valueOf(22));

      query = factory.transactionQuery();
      query.andTxId(Operator.GREATER_THAN_EQ, 21, Operator.LESS_THAN_EQ, 23);
      results = query.getResults();
      assertEquals(3, results.size());
      assertEquals(3, query.getCount());

      transactions = results.getList();
      assertTxExists(transactions, Long.valueOf(21));
      assertTxExists(transactions, Long.valueOf(22));
      assertTxExists(transactions, Long.valueOf(23));

   }

   @Test
   public void testGetDateOperator() throws Exception {
      TransactionQuery query = factory.transactionQuery();
      query.andTxId(65);
      Timestamp date = new Timestamp(query.getResults().getAtMostOneOrNull().getDate().getTime());

      query = factory.transactionQuery();
      query.andDate(Operator.GREATER_THAN, date);
      ResultSet<TransactionReadable> results = query.getResults();
      assertEquals(CommonBranchHeadTransaction - 65, results.size());

      query = factory.transactionQuery();
      query.andDate(Operator.LESS_THAN, date);
      results = query.getResults();
      assertEquals(64, results.size());

   }

   @Test
   public void testGetDateRange() throws Exception {
      TransactionQuery query = factory.transactionQuery();
      query.andTxId(1);
      Timestamp date1 = new Timestamp(query.getResults().getAtMostOneOrNull().getDate().getTime());

      query = factory.transactionQuery();
      query.andTxId(65);
      Timestamp date2 = new Timestamp(query.getResults().getAtMostOneOrNull().getDate().getTime());

      query = factory.transactionQuery();
      query.andDate(date1, date2);
      ResultSet<TransactionReadable> results = query.getResults();
      assertEquals(65, results.size());
   }

   @Test
   public void testGetAuthorId() throws Exception {
      TransactionQuery query = factory.transactionQuery();
      query.andAuthorLocalIds(OseeSystem);
      ResultSet<TransactionReadable> results = query.getResults();
      assertEquals(72, results.size());
      assertEquals(72, query.getCount());

      List<TransactionReadable> transactions = results.getList();
      assertTx(transactions, Long.valueOf(28), Baselined, "Branch Creation for CIS_Bld_1", CIS_Bld_1, OseeSystem);

      query = factory.transactionQuery();
      query.andAuthorLocalIds(OseeSystem, JoeSmith);
      results = query.getResults();
      assertEquals(117, results.size());
      assertEquals(117, query.getCount());

      transactions = results.getList();
      //@formatter:off
      assertTx(transactions, Long.valueOf(28), Baselined, "Branch Creation for CIS_Bld_1", CIS_Bld_1, OseeSystem);
      assertTx(transactions, Long.valueOf(62), NonBaselined, "ArtifactImportOperationFactory: Artifact Import Wizard transaction", SAW_Bld_1, JoeSmith);
      assertTx(transactions, CommonBranchHeadTransaction, NonBaselined, CommonBranchTransComment, COMMON, OseeSystem);
      //@formatter:on

      query = factory.transactionQuery();
      ArrayList<ArtifactId> list = new ArrayList<>(2);
      list.add(OseeSystem);
      list.add(JoeSmith);
      query.andAuthorLocalIds(list);
      results = query.getResults();
      assertEquals(117, results.size());
      assertEquals(117, query.getCount());

      transactions = results.getList();
      //@formatter:off
      assertTx(transactions, Long.valueOf(28), Baselined, "Branch Creation for CIS_Bld_1", CIS_Bld_1, OseeSystem);
      assertTx(transactions, Long.valueOf(62), NonBaselined, "ArtifactImportOperationFactory: Artifact Import Wizard transaction", SAW_Bld_1, JoeSmith);
      assertTx(transactions, CommonBranchHeadTransaction, NonBaselined, CommonBranchTransComment, COMMON, OseeSystem);
      //@formatter:on

   }

   @Test(expected = OseeArgumentException.class)
   public void testGetCommitId() throws Exception {
      TransactionQuery query = factory.transactionQuery();
      query.andNullCommitId();
      ResultSet<TransactionReadable> results = query.getResults();
      assertEquals(116, query.getCount());

      List<TransactionReadable> transactions = results.getList();
      assertTxExists(transactions, Long.valueOf(1));
      assertTxExists(transactions, Long.valueOf(2));
      assertTxExists(transactions, Long.valueOf(3));
      assertTxExists(transactions, Long.valueOf(4));
      assertTxExists(transactions, Long.valueOf(5));

      query = factory.transactionQuery();
      query.andCommitIds(OseeSystem.getId().intValue(), 1);
      results = query.getResults();
      assertEquals(0, results.size());
      assertEquals(0, query.getCount());

      query = factory.transactionQuery();
      ArrayList<Integer> list = new ArrayList<>(2);
      list.add(1);
      list.add(2);
      query.andAuthorIds(list);
      results = query.getResults();
      assertEquals(0, results.size());
      assertEquals(0, query.getCount());

      /************************************************************
       * null not allowed in multiple argument case -- throws OseeArgumentException
       */
      query = factory.transactionQuery();
      query.andCommitIds(null, OseeSystem.getId().intValue(), 1);
      results = query.getResults();

   }

   @Test
   public void testGetHead() throws Exception {
      TransactionQuery query = factory.transactionQuery();
      query.andIsHead(1);
      ResultSet<TransactionReadable> results = query.getResults();
      assertEquals(1, query.getCount());

      List<TransactionReadable> transactions = results.getList();
      assertTxExists(transactions, Long.valueOf(3));
   }

   @Test
   public void testGetHead2() throws Exception {
      TransactionQuery query = factory.transactionQuery();
      query.andIsHead(COMMON);

      ResultSet<TransactionReadable> results = query.getResults();
      assertEquals(1, query.getCount());
      assertEquals(1, results.size());

      List<TransactionReadable> transactions = results.getList();
      assertTx(transactions, CommonBranchHeadTransaction, NonBaselined, CommonBranchTransComment, COMMON, OseeSystem);
   }

   @Test
   public void testAuthorLocal() throws Exception {
      QueryFactory branchFactory = orcsApi.getQueryFactory();
      QueryBuilder branchQuery = branchFactory.fromBranch(COMMON);
      /*********************************************************
       * Only valid user is Joe Smith
       */
      branchQuery.andUuid(JoeSmith.getId());
      ResultSet<ArtifactReadable> userIds = branchQuery.getResults();
      Iterator<ArtifactReadable> itUsers = userIds.iterator();
      ArtifactId joeArt = itUsers.next();

      TransactionQuery query = factory.transactionQuery();
      query.andAuthorLocalIds(joeArt);
      assertEquals(45, query.getCount());

      /**********************************************************
       * Only valid user is 17. Verify the multiple entry by entering the same user twice
       */
      query = factory.transactionQuery();
      query.andAuthorLocalIds(joeArt, joeArt);
      assertEquals(45, query.getCount());

   }

   @Test
   public void testMultifield() throws Exception {
      TransactionQuery query = factory.transactionQuery();
      query.andAuthorLocalIds(OseeSystem);
      query.andIs(Baselined);
      query.andCommentPattern("Branch Creation for.*");
      ResultSet<TransactionReadable> results = query.getResults();
      assertEquals(3, results.size());
      assertEquals(3, query.getCount());

      for (TransactionReadable trans : query.getResults()) {
         assertTrue(trans.getComment().startsWith("Branch Creation for"));
      }
   }

   @Test
   public void testGetPriorTx() throws OseeCoreException {
      TransactionQuery query = factory.transactionQuery();
      query.andIsPriorTx(TransactionId.valueOf(CommonBranchHeadTransaction));

      ResultSet<TransactionReadable> results = query.getResults();
      assertEquals(1, results.size());
      assertEquals(1, query.getCount());

      List<TransactionReadable> transactions = results.getList();
      assertTxExists(transactions, Long.valueOf(CommonBranchHeadTransaction - 1));

      Iterator<Long> ids = query.getResultsAsIds().iterator();
      assertEquals(Long.valueOf(CommonBranchHeadTransaction - 1), ids.next());
   }

   private static void assertTx(List<TransactionReadable> transactions, Long localId, TransactionDetailsType type, String comment, BranchId branch, ArtifactId author) {
      TransactionReadable transaction = getTransaction(transactions, localId);
      assertEquals(localId, transaction.getId());
      assertEquals(type, transaction.getTxType());
      assertNotNull(DATE_FORMATTER.format(transaction.getDate()));
      assertEquals(comment, transaction.getComment());

      assertEquals(author, transaction.getAuthor());
      assertEquals(branch, transaction.getBranchId());
      assertTrue(transaction.getCommitArt().isInvalid());
   }

   private void assertTxExists(List<TransactionReadable> transactions, Long localId) {
      assertTrue(getTransaction(transactions, localId) != null);
   }

   private static TransactionReadable getTransaction(List<TransactionReadable> transactions, Long localId) {
      TransactionReadable transaction = null;
      for (TransactionReadable tx : transactions) {
         if (tx.getId().equals(localId)) {
            transaction = tx;
            break;
         }
      }
      return transaction;
   }
}
