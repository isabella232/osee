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
package org.eclipse.osee.orcs.db.internal.loader.processor;

import java.util.Date;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.core.ds.Options;
import org.eclipse.osee.orcs.core.ds.TxOrcsData;
import org.eclipse.osee.orcs.db.internal.loader.data.TransactionObjectFactory;

/**
 * @author Roberto E. Escobar
 */
public class TransactionLoadProcessor extends LoadProcessor<TxOrcsData, TransactionObjectFactory> {

   public TransactionLoadProcessor(TransactionObjectFactory factory) {
      super(factory);
   }

   @Override
   protected TxOrcsData createData(Object conditions, TransactionObjectFactory factory, IOseeStatement chStmt, Options options) throws OseeCoreException {
      long branchUuid = chStmt.getLong("branch_id");
      int localId = chStmt.getInt("transaction_id");
      TransactionDetailsType type = TransactionDetailsType.toEnum(chStmt.getInt("tx_type"));
      String comment = chStmt.getString("osee_comment");
      Date date = chStmt.getTimestamp("time");
      int authorId = chStmt.getInt("author");
      int commitId = chStmt.getInt("commit_art_id");
      return factory.createTxData(localId, type, date, comment, branchUuid, authorId, commitId);
   }
}