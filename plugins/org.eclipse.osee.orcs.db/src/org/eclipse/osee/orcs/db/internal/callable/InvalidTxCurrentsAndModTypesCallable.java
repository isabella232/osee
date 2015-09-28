/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.internal.callable;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.jdbc.JdbcConnection;
import org.eclipse.osee.jdbc.JdbcConstants;
import org.eclipse.osee.jdbc.JdbcStatement;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.db.internal.util.Address;

/**
 * @author Ryan D. Brooks
 */
public class InvalidTxCurrentsAndModTypesCallable extends AbstractDatastoreTxCallable<Void> {
   private static final String SELECT_ADDRESSES =
      "select %s, txs.branch_id, txs.transaction_id, txs.gamma_id, txs.mod_type, txs.tx_current, txd.tx_type from %s t1, osee_txs%s txs, osee_tx_details txd where t1.gamma_id = txs.gamma_id and txd.transaction_id = txs.transaction_id and txs.branch_id = txd.branch_id order by txs.branch_id, %s, txs.transaction_id desc, txs.gamma_id desc";

   private static final String DELETE_ADDRESS =
      "delete from osee_txs%s where transaction_id = ? and gamma_id = ? and branch_id = ?";
   private static final String UPDATE_ADDRESS =
      "update osee_txs%s set tx_current = ? where transaction_id = ? and gamma_id = ? and branch_id = ?";

   private final List<Address> addresses = new ArrayList<>();

   private final List<Object[]> purgeData = new ArrayList<>();
   private final List<Object[]> currentData = new ArrayList<>();
   private final String tableName;
   private final String columnName;
   private final boolean isFixOperationEnabled;
   private final String txsTableName;

   public InvalidTxCurrentsAndModTypesCallable(Log logger, OrcsSession session, JdbcClient jdbcClient, String operationName, String tableName, String columnName, boolean isFixOperationEnabled, boolean archived) {
      super(logger, session, jdbcClient);
      this.tableName = tableName;
      this.columnName = columnName;
      this.isFixOperationEnabled = isFixOperationEnabled;
      txsTableName = archived ? "_archived" : "";
   }

   private void fixIssues() throws OseeCoreException {
      if (isFixOperationEnabled) {
         checkForCancelled();
         getJdbcClient().runBatchUpdate(String.format(DELETE_ADDRESS, txsTableName), purgeData);
         getJdbcClient().runBatchUpdate(String.format(UPDATE_ADDRESS, txsTableName), currentData);
      }
   }

   private void consolidateAddressing() {
      checkForMultipleVersionsInOneTransaction();
      checkForIdenticalAddressingInDifferentTransactions();
      checkForMultipleCurrents();
      checkForInvalidMergedModType();

      if (issueDetected()) {
         for (Address address : addresses) {
            if (address.isPurge()) {
               logIssue("purged", address);

               purgeData.add(new Object[] {address.getTransactionId(), address.getGammaId(), address.getBranchId()});
            } else if (address.getCorrectedTxCurrent() != null) {
               logIssue("corrected txCurrent: " + address.getCorrectedTxCurrent(), address);

               currentData.add(new Object[] {
                  address.getCorrectedTxCurrent().getValue(),
                  address.getTransactionId(),
                  address.getGammaId(),
                  address.getBranchId()});
            } else {
               logIssue("would have fixed merge here");
            }
         }
      }
   }

   private void checkForInvalidMergedModType() {
      int index = addresses.size() - 1;
      Address lastAddress = addresses.get(index);
      if (!lastAddress.isBaselineTx()) {
         for (; index > -1; index--) {
            if (!addresses.get(index).isPurge()) {
               if (addresses.get(index).getModType() == ModificationType.MERGED) {
                  //                  logIssue("found merged mod type for item not in baseline: ", addresses.get(index));
               }
               return;
            }
         }
      }
   }

   private void checkForIdenticalAddressingInDifferentTransactions() {
      Address previousAddress = null;

      for (Address address : addresses) {
         if (address.hasSameGamma(previousAddress) && address.hasSameModType(previousAddress)) {
            previousAddress.setPurge(true);
         }
         previousAddress = address;
      }
   }

   private boolean issueDetected() {
      for (Address address : addresses) {
         if (address.hasIssue()) {
            return true;
         }
      }
      return false;
   }

   private void checkForMultipleVersionsInOneTransaction() {
      Address previousAddress = null;

      for (Address address : addresses) {
         if (address.isSameTransaction(previousAddress)) {
            if (address.hasSameModType(previousAddress) || !address.getModType().isDeleted() && previousAddress.getModType().isEdited()) {
               address.setPurge(true);
            } else {
               logIssue("multiple versions in one transaction - unknown case", address);
            }
         }
         previousAddress = address;
      }
   }

   private void checkForMultipleCurrents() {
      boolean mostRecentTx = true;
      for (Address address : addresses) {
         if (!address.isPurge()) {
            if (mostRecentTx) {
               address.ensureCorrectCurrent();
               mostRecentTx = false;
            } else {
               address.ensureNotCurrent();
            }
         }
      }
   }

   @Override
   protected Void handleTxWork(JdbcConnection connection) {
      checkForCancelled();

      JdbcStatement chStmt = getJdbcClient().getStatement();
      String sql = String.format(SELECT_ADDRESSES, columnName, tableName, txsTableName, columnName);
      try {
         chStmt.runPreparedQuery(JdbcConstants.JDBC__MAX_FETCH_SIZE, sql);

         Address previousAddress = null;
         while (chStmt.next()) {
            checkForCancelled();
            ModificationType modType = ModificationType.getMod(chStmt.getInt("mod_type"));
            TxChange txCurrent = TxChange.getChangeType(chStmt.getInt("tx_current"));
            TransactionDetailsType type = TransactionDetailsType.toEnum(chStmt.getInt("tx_type"));
            Address address =
               new Address(type.isBaseline(), chStmt.getLong("branch_id"), chStmt.getInt(columnName),
                  chStmt.getInt("transaction_id"), chStmt.getLong("gamma_id"), modType, txCurrent);

            if (!address.isSimilar(previousAddress)) {
               if (!addresses.isEmpty()) {
                  consolidateAddressing();
               }
               addresses.clear();
            }

            addresses.add(address);
            previousAddress = address;
         }
      } finally {
         chStmt.close();
      }
      fixIssues();
      return null;
   }

   private void logIssue(String message, Address address) {
      getLogger().info("msg[%s] - branchId[%s] itemId[%s] txId[%s] gammaId[%s] modType[%s] txCurrent[%s]",//
         message, //
         address.getBranchId(), // 
         address.getItemId(),//
         address.getTransactionId(),// 
         address.getGammaId(),//
         address.getModType(), //
         address.getTxCurrent());
   }

   private void logIssue(String message) {
      getLogger().info(message);
   }
}