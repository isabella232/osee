/*********************************************************************
 * Copyright (c) 2015 Boeing
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

package org.eclipse.osee.orcs.rest.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.orcs.data.TransactionReadable;
import org.eclipse.osee.orcs.rest.model.Transaction;

/**
 * @author Roberto E. Escobar
 */
public final class OrcsRestUtil {

   private OrcsRestUtil() {
      // Utility class
   }

   public static <T> T executeCallable(Callable<T> callable) {
      try {
         return callable.call();
      } catch (Exception ex) {
         throw OseeCoreException.wrap(ex);
      }
   }

   public static Response asResponse(boolean modified) {
      ResponseBuilder builder;
      if (modified) {
         builder = Response.ok();
      } else {
         builder = Response.notModified();
      }
      return builder.build();
   }

   public static List<Transaction> asTransactions(ResultSet<? extends TransactionReadable> results) {
      List<Transaction> toReturn = new ArrayList<>(results.size());
      for (TransactionReadable data : results) {
         toReturn.add(asTransaction(data));
      }
      return toReturn;
   }

   public static Transaction asTransaction(TransactionReadable tx) {
      Transaction data = new Transaction();
      data.setTxId(tx);
      data.setAuthor(tx.getAuthor());
      data.setBranchUuid(tx.getBranch().getId());
      data.setComment(tx.getComment());
      data.setCommitArt(tx.getCommitArt());
      data.setTimeStamp(tx.getDate());
      data.setTxType(tx.getTxType());
      return data;
   }
}