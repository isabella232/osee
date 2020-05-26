/*********************************************************************
 * Copyright (c) 2014 Boeing
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

package org.eclipse.osee.orcs.core.ds.criteria;

import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.orcs.core.ds.Criteria;
import org.eclipse.osee.orcs.core.ds.Options;

/**
 * @author Roberto E. Escobar
 */
public class CriteriaTxGetPrior extends Criteria implements TxCriteria {

   private final TransactionToken txId;

   public CriteriaTxGetPrior(TransactionToken txId) {
      this.txId = txId;
   }

   public TransactionToken getTxId() {
      return txId;
   }

   @Override
   public void checkValid(Options options) {
      super.checkValid(options);
      Conditions.checkExpressionFailOnTrue(txId.isInvalid(), "TxId [%s] is invalid. Must be >= 0", txId);
   }

   @Override
   public String toString() {
      return "CriteriaTxGetPrior [txId=" + txId + "]";
   }
}