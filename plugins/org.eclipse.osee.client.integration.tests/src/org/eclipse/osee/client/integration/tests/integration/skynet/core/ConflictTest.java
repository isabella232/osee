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
package org.eclipse.osee.client.integration.tests.integration.skynet.core;

import static org.eclipse.osee.client.demo.DemoChoice.OSEE_CLIENT_DEMO;
import static org.eclipse.osee.framework.core.enums.DeletionFlag.INCLUDE_DELETED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osee.client.demo.DemoBranches;
import org.eclipse.osee.client.integration.tests.integration.skynet.core.utils.ConflictTestManager;
import org.eclipse.osee.client.test.framework.OseeClientIntegrationRule;
import org.eclipse.osee.client.test.framework.OseeHousekeepingRule;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.ConflictStatus;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.logging.SevereLoggingMonitor;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.PurgeArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.conflict.AttributeConflict;
import org.eclipse.osee.framework.skynet.core.conflict.Conflict;
import org.eclipse.osee.framework.skynet.core.conflict.ConflictManagerExternal;
import org.eclipse.osee.framework.skynet.core.conflict.RelationConflict;
import org.eclipse.osee.framework.skynet.core.revision.ConflictManagerInternal;
import org.eclipse.osee.framework.skynet.core.utility.ConnectionHandler;
import org.eclipse.osee.jdbc.JdbcStatement;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runners.MethodSorters;

/**
 * @author Jeff C. Phillips
 * @author Theron Virgin
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConflictTest {

   @Rule
   public OseeClientIntegrationRule integration = new OseeClientIntegrationRule(OSEE_CLIENT_DEMO);

   @Rule
   public MethodRule oseeHousekeepingRule = new OseeHousekeepingRule();

   @BeforeClass
   public static void setUp() throws Exception {
      ConflictTestManager.initializeConflictTest();
   }

   @AfterClass
   public static void tearDown() throws Exception {
      ConflictTestManager.cleanUpConflictTest();
   }

   /**
    * Test method for
    * {@link org.eclipse.osee.framework.skynet.core.artifact.BranchManager#getMergeBranch(Branch, Branch)} .
    */
   @org.junit.Test
   public void test01GetMergeBranchNotCreated() throws Exception {
      SevereLoggingMonitor monitorLog = new SevereLoggingMonitor();
      OseeLog.registerLoggerListener(monitorLog);
      try {
         Branch mergeBranch =
            BranchManager.getMergeBranch(ConflictTestManager.getSourceBranch(), ConflictTestManager.getDestBranch());

         assertTrue("The merge branch should be null as it hasn't been created yet", mergeBranch == null);
      } catch (Exception ex) {
         fail(ex.getMessage());
      }
      assertTrue(String.format("%d SevereLogs during test.", monitorLog.getSevereLogs().size()),
         monitorLog.getSevereLogs().isEmpty());
   }

   /**
    * Test method for
    * {@link org.eclipse.osee.framework.skynet.core.revision.ConflictManagerInternal#getConflictsPerBranch(org.eclipse.osee.framework.core.model.Branch, org.eclipse.osee.framework.core.model.Branch, org.eclipse.osee.framework.skynet.core.transaction.TransactionId)}
    * .
    */
   @org.junit.Test
   public void test02GetConflictsPerBranch() {
      SevereLoggingMonitor monitorLog = new SevereLoggingMonitor();
      OseeLog.registerLoggerListener(monitorLog);
      Collection<Conflict> conflicts = null;
      try {
         conflicts = ConflictManagerInternal.getConflictsPerBranch(ConflictTestManager.getSourceBranch(),
            ConflictTestManager.getDestBranch(), ConflictTestManager.getSourceBranch().getBaseTransaction(),
            new NullProgressMonitor());
      } catch (Exception ex) {
         fail(Lib.exceptionToString(ex));
      }
      int expectedNumber = ConflictTestManager.numberOfConflicts();
      int actualNumber = conflicts.size();
      assertTrue(
         "(Intermittent failures - needs re-write) - Number of conflicts found is not equal to the number of conflicts expected",
         (expectedNumber <= actualNumber) && (actualNumber <= (expectedNumber + 1)));
      assertTrue(String.format("%d SevereLogs during test.", monitorLog.getSevereLogs().size()),
         monitorLog.getSevereLogs().isEmpty());
   }

   /**
    * Test method for
    * {@link org.eclipse.osee.framework.skynet.core.artifact.BranchManager#getMergeBranch(Branch, Branch)} .
    */
   @org.junit.Test
   public void test03GetMergeBranchCreated() throws Exception {
      SevereLoggingMonitor monitorLog = new SevereLoggingMonitor();
      OseeLog.registerLoggerListener(monitorLog);
      try {
         Branch mergeBranch =
            BranchManager.getMergeBranch(ConflictTestManager.getSourceBranch(), ConflictTestManager.getDestBranch());
         assertFalse(mergeBranch == null);
         Collection<Artifact> artifacts = ArtifactQuery.getArtifactListFromBranch(mergeBranch, INCLUDE_DELETED);

         int expectedNumber = ConflictTestManager.numberOfArtifactsOnMergeBranch();
         int actualNumber = artifacts.size();
         assertTrue(
            "(Intermittent failures - needs re-write) - The merge Branch does not contain the expected number of artifacts: ",
            (expectedNumber <= actualNumber) && (actualNumber <= (expectedNumber + 1)));
      } catch (Exception ex) {
         fail(ex.getMessage());
      }
      assertTrue(String.format("%d SevereLogs during test.", monitorLog.getAllLogs().size()),
         monitorLog.getAllLogs().isEmpty());
   }

   @org.junit.Test
   public void test04ResolveConflicts() {
      SevereLoggingMonitor monitorLog = new SevereLoggingMonitor();
      OseeLog.registerLoggerListener(monitorLog);
      try {
         Collection<Conflict> conflicts = ConflictManagerInternal.getConflictsPerBranch(
            ConflictTestManager.getSourceBranch(), ConflictTestManager.getDestBranch(),
            ConflictTestManager.getSourceBranch().getBaseTransaction(), new NullProgressMonitor());

         for (Conflict conflict : conflicts) {
            if (conflict instanceof AttributeConflict) {
               ConflictTestManager.resolveAttributeConflict((AttributeConflict) conflict);
               conflict.setStatus(ConflictStatus.RESOLVED);
            } else if (conflict instanceof RelationConflict) {
               fail("Relation Conflicts are not supported yet");
            }
         }

         conflicts = ConflictManagerInternal.getConflictsPerBranch(ConflictTestManager.getSourceBranch(),
            ConflictTestManager.getDestBranch(), ConflictTestManager.getSourceBranch().getBaseTransaction(),
            new NullProgressMonitor());

         for (Conflict conflict : conflicts) {
            ConflictStatus status = conflict.getStatus();
            assertTrue(
               "This conflict was not found to be resolved ArtId = " + conflict.getArtId() + " " + conflict.getSourceDisplayData(),
               status.isResolved() || status.isInformational());

         }
      } catch (Exception ex) {
         fail(Lib.exceptionToString(ex));
      }
      assertTrue(String.format("%d SevereLogs during test.", monitorLog.getAllLogs().size()),
         monitorLog.getAllLogs().isEmpty());
   }

   @Ignore
   public void test05CommitWithoutResolutionErrors() {
      SevereLoggingMonitor monitorLog = new SevereLoggingMonitor();
      OseeLog.registerLoggerListener(monitorLog);
      try {
         ConflictManagerExternal conflictManager =
            new ConflictManagerExternal(ConflictTestManager.getDestBranch(), ConflictTestManager.getSourceBranch());
         BranchManager.commitBranch(null, conflictManager, false, false);
         assertTrue("Commit did not complete as expected", ConflictTestManager.validateCommit());

         assertEquals("Source Branch state incorrect", BranchState.COMMITTED,
            ConflictTestManager.getSourceBranch().getBranchState());

      } catch (Exception ex) {
         fail("No Exceptions should have been thrown. Not even the " + ex.getLocalizedMessage() + "Exception");
      }

      assertTrue(String.format("%d SevereLogs during test.", monitorLog.getSevereLogs().size()),
         monitorLog.getSevereLogs().isEmpty());
   }

   @Test
   public void testMultiplicityCommit() {
      Branch parent = BranchManager.getBranch(DemoBranches.SAW_Bld_1);
      Artifact testArt =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, parent, "Multiplicity Test");
      testArt.persist("Save testArt on parent");
      Branch child1 = BranchManager.createWorkingBranch(parent, "Child1");
      Branch child2 = BranchManager.createWorkingBranch(parent, "Child2");

      Artifact onChild1 = ArtifactQuery.getArtifactFromId(testArt.getArtId(), child1);
      onChild1.setSoleAttributeFromString(CoreAttributeTypes.ParagraphNumber, "1");
      onChild1.persist("Save paragraph number on child1");

      List<Attribute<Object>> attributes = onChild1.getAttributes(CoreAttributeTypes.ParagraphNumber);
      Assert.assertTrue(attributes.size() == 1);
      Attribute<Object> attr = attributes.iterator().next();
      int child1AttrId = attr.getId();

      ConflictManagerExternal mgr = new ConflictManagerExternal(parent, child1);
      BranchManager.commitBranch(new NullProgressMonitor(), mgr, true, false);
      Assert.assertFalse(mgr.originalConflictsExist());

      Artifact onChild2 = ArtifactQuery.getArtifactFromId(testArt.getArtId(), child2);
      onChild2.setSoleAttributeFromString(CoreAttributeTypes.ParagraphNumber, "2");
      onChild2.persist("Save paragraph number on child2");

      attributes = onChild2.getAttributes(CoreAttributeTypes.ParagraphNumber);
      Assert.assertTrue(attributes.size() == 1);
      attr = attributes.iterator().next();
      int child2AttrId = attr.getId();

      Assert.assertNotEquals(child1AttrId, child2AttrId);

      mgr = new ConflictManagerExternal(parent, child2);
      Assert.assertTrue(mgr.originalConflictsExist());
      List<Conflict> conflicts = mgr.getOriginalConflicts();
      Assert.assertTrue(conflicts.size() == 1);
      Conflict conflict = conflicts.iterator().next();
      int conflictObjId = conflict.getObjectId();
      Assert.assertEquals(child1AttrId, conflictObjId);

      BranchManager.purgeBranch(BranchManager.getMergeBranch(child2, parent));
      BranchManager.purgeBranch(child2);
      PurgeArtifacts p = new PurgeArtifacts(Arrays.asList(testArt));
      Operations.executeWorkAndCheckStatus(p);
   }

   @Ignore
   @org.junit.Test
   public void test06CommitFiltering() throws OseeCoreException {
      checkNoTxCurrent("art_id", "osee_artifact");
      checkNoTxCurrent("attr_id", "osee_attribute");
      checkNoTxCurrent("rel_link_id", "osee_relation_link");

      checkMultipleTxCurrent("art_id", "osee_artifact");
      checkMultipleTxCurrent("rel_link_id", "osee_relation_link");

      //TODO: Causes intermittent failures
      //      checkMultipleTxCurrent("attr_id", "osee_attribute");
   }

   //@formatter:off
   private static final String NO_TX_CURRENT_SET =
      "SELECT DISTINCT t1.%s, txs1.branch_id FROM osee_txs txs1, %s t1 " +
      "WHERE txs1.gamma_id = t1.gamma_id AND txs1.tx_current = 0 %s " +
      "SELECT DISTINCT t2.%s, txs2.branch_id FROM osee_txs txs2, %s t2 " +
      "WHERE txs2.gamma_id = t2.gamma_id AND txs2.tx_current != 0";

   private static final String MULTIPLE_TX_CURRENT_SET =
         "SELECT resulttable.branch_id, resulttable.%s, COUNT(resulttable.branch_id) AS numoccurrences FROM " +
         "(SELECT txs1.branch_id, t1.%s FROM osee_txs txs1, %s t1 WHERE txs1.gamma_id = t1.gamma_id AND txs1.tx_current != 0) resulttable " +
         "GROUP BY resulttable.branch_id, resulttable.%s HAVING(COUNT(resulttable.branch_id) > 1) order by branch_id";
   //@formatter:on

   private static void checkNoTxCurrent(String dataId, String dataTable) throws OseeCoreException {
      JdbcStatement chStmt = ConnectionHandler.getStatement();
      try {
         String query =
            String.format(NO_TX_CURRENT_SET, dataId, dataTable, chStmt.getComplementSql(), dataId, dataTable);
         chStmt.runPreparedQuery(query);
         if (chStmt.next()) {
            fail(String.format("No TX Current Set Failed for dataId = %s and dataTable = %s", dataId, dataTable));
         }
      } finally {
         chStmt.close();
      }
   }

   private static void checkMultipleTxCurrent(String dataId, String dataTable) throws OseeCoreException {
      JdbcStatement chStmt = ConnectionHandler.getStatement();
      try {
         String query = String.format(MULTIPLE_TX_CURRENT_SET, dataId, dataId, dataTable, dataId);
         chStmt.runPreparedQuery(query);
         if (chStmt.next()) {
            fail(String.format("Multiple TX Current Set Failed for dataId = %s and dataTable = %s", dataId, dataTable));
         }
      } finally {
         chStmt.close();
      }
   }

}
