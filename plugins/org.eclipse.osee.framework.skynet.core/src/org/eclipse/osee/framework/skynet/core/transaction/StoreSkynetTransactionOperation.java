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
package org.eclipse.osee.framework.skynet.core.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.database.core.DatabaseTransactions;
import org.eclipse.osee.framework.database.core.IDbTransactionWork;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.model.ArtifactEvent;
import org.eclipse.osee.framework.skynet.core.event.model.EventModifiedBasicGuidArtifact;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.skynet.core.transaction.BaseTransactionData.InsertDataCollector;

/**
 * @author Roberto E. Escobar
 * @author Ryan D. Brooks
 * @author Jeff C. Phillips
 */
public final class StoreSkynetTransactionOperation extends AbstractOperation implements IDbTransactionWork, InsertDataCollector {

   private static final String GET_EXISTING_ATTRIBUTE_IDS =
      "SELECT att1.attr_id FROM osee_attribute att1, osee_artifact art1, osee_txs txs1 WHERE att1.attr_type_id = ? AND att1.art_id = ? AND att1.art_id = art1.art_id AND art1.gamma_id = txs1.gamma_id AND txs1.branch_id <> ?";

   private static final String UPDATE_TXS_NOT_CURRENT =
      "UPDATE osee_txs txs1 SET tx_current = " + TxChange.NOT_CURRENT.getValue() + " WHERE txs1.transaction_id = ? AND txs1.gamma_id = ?";

   private final HashCollection<String, Object[]> dataItemInserts = new HashCollection<String, Object[]>();
   private final Map<Integer, String> dataInsertOrder = new HashMap<Integer, String>();

   private final Branch branch;
   private final TransactionRecord transactionRecord;
   private final Collection<BaseTransactionData> txDatas;
   private final Collection<Artifact> artifactReferences;

   private boolean executedWithException;

   public StoreSkynetTransactionOperation(String name, Branch branch, TransactionRecord transactionRecord, Collection<BaseTransactionData> txDatas, Collection<Artifact> artifactReferences) {
      super(name, Activator.PLUGIN_ID);
      this.branch = branch;
      this.transactionRecord = transactionRecord;
      this.txDatas = txDatas;
      this.artifactReferences = artifactReferences;
   }

   @Override
   public int getTransactionNumber() {
      return transactionRecord.getId();
   }

   @Override
   public int getBranchId() {
      return branch.getId();
   }

   @Override
   public void internalAddInsertToBatch(int insertPriority, String insertSql, Object... data) {
      dataItemInserts.put(insertSql, data);
      dataInsertOrder.put(insertPriority, insertSql);
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      DatabaseTransactions.execute(this);
   }

   @Override
   public void handleTxWork(OseeConnection connection) throws OseeCoreException {
      executedWithException = false;
      TransactionManager.internalPersist(connection, transactionRecord);
      if (!txDatas.isEmpty()) {
         executeTransactionDataItems(connection);
      }
      if (branch.getBranchState() == BranchState.CREATED) {
         branch.setBranchState(BranchState.MODIFIED);
         BranchManager.persist(branch);
      }
   }

   @Override
   public void handleTxException(Exception ex) {
      executedWithException = true;
      for (BaseTransactionData transactionData : txDatas) {
         try {
            transactionData.internalOnRollBack();
         } catch (OseeCoreException ex1) {
            OseeLog.log(Activator.class, Level.SEVERE, ex1);
         }
      }
   }

   @Override
   public void handleTxFinally() throws OseeCoreException {
      if (!executedWithException) {
         updateModifiedCachedObject();
      }
   }

   private void executeTransactionDataItems(OseeConnection connection) throws OseeCoreException {
      List<Object[]> txNotCurrentData = new ArrayList<Object[]>();
      for (BaseTransactionData transactionData : txDatas) {
         // Collect inserts for attribute, relation, artifact, and artifact version tables
         transactionData.addInsertToBatch(this);

         // Collect stale tx currents for batch update
         fetchTxNotCurrent(connection, branch, transactionData, txNotCurrentData);
      }

      // Insert into data tables - i.e. attribute, relation and artifact version tables
      List<Integer> keys = new ArrayList<Integer>(dataInsertOrder.keySet());
      Collections.sort(keys);
      for (int priority : keys) {
         String sqlKey = dataInsertOrder.get(priority);
         ConnectionHandler.runBatchUpdate(connection, sqlKey, (List<Object[]>) dataItemInserts.getValues(sqlKey));
      }

      // Set stale tx currents in txs table
      ConnectionHandler.runBatchUpdate(connection, UPDATE_TXS_NOT_CURRENT, txNotCurrentData);
   }

   private static void fetchTxNotCurrent(OseeConnection connection, Branch branch, BaseTransactionData transactionData, List<Object[]> results) throws OseeCoreException {
      IOseeStatement chStmt = ConnectionHandler.getStatement(connection);
      try {
         String query = ClientSessionManager.getSql(transactionData.getSelectTxNotCurrentSql());

         chStmt.runPreparedQuery(query, transactionData.getItemId(), branch.getId());
         while (chStmt.next()) {
            results.add(new Object[] {chStmt.getInt("transaction_id"), chStmt.getLong("gamma_id")});
         }
      } finally {
         chStmt.close();
      }
   }

   private void updateModifiedCachedObject() throws OseeCoreException {
      ArtifactEvent artifactEvent = new ArtifactEvent(transactionRecord.getBranch());
      artifactEvent.setTransactionId(getTransactionNumber());

      // Update all transaction items before collecting events
      for (BaseTransactionData transactionData : txDatas) {
         transactionData.internalUpdate(transactionRecord);
      }

      // Collect events before clearing any dirty flags
      for (BaseTransactionData transactionData : txDatas) {
         transactionData.internalAddToEvents(artifactEvent);
      }

      // Collect attribute events
      for (Artifact artifact : artifactReferences) {
         if (artifact.hasDirtyAttributes()) {
            EventModifiedBasicGuidArtifact guidArt =
               new EventModifiedBasicGuidArtifact(artifact.getBranch().getGuid(), artifact.getArtifactType().getGuid(),
                  artifact.getGuid(), artifact.getDirtyFrameworkAttributeChanges());
            artifactEvent.getArtifacts().add(guidArt);

            // Collection relation reorder records for events
            if (!artifact.getRelationOrderRecords().isEmpty()) {
               artifactEvent.getRelationOrderRecords().addAll(artifact.getRelationOrderRecords());
            }
         }
      }

      // Clear all dirty flags
      for (BaseTransactionData transactionData : txDatas) {
         transactionData.internalClearDirtyState();
      }

      // Clear all relation order records
      for (Artifact artifact : artifactReferences) {
         artifact.getRelationOrderRecords().clear();
      }

      if (!artifactEvent.getArtifacts().isEmpty() || !artifactEvent.getRelations().isEmpty()) {
         OseeEventManager.kickPersistEvent(this, artifactEvent);
      }
   }

   protected static int getNewAttributeId(Artifact artifact, Attribute<?> attribute) throws OseeCoreException {
      IOseeStatement chStmt = ConnectionHandler.getStatement();
      AttributeType attributeType = attribute.getAttributeType();
      int attrId = -1;
      // reuse an existing attribute id when there should only be a max of one and it has already been created on another branch
      if (attributeType.getMaxOccurrences() == 1) {
         try {
            chStmt.runPreparedQuery(GET_EXISTING_ATTRIBUTE_IDS, attributeType.getId(), artifact.getArtId(),
               artifact.getBranch().getId());

            if (chStmt.next()) {
               attrId = chStmt.getInt("attr_id");
            }
         } finally {
            chStmt.close();
         }
      }
      if (attrId < 1) {
         attrId = ConnectionHandler.getSequence().getNextAttributeId();
      }
      return attrId;
   }

}
