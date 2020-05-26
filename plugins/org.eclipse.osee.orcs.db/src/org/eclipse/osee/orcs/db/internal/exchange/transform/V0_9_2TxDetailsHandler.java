/*********************************************************************
 * Copyright (c) 2009 Boeing
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

package org.eclipse.osee.orcs.db.internal.exchange.transform;

import java.util.Map;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.jdk.core.util.io.xml.AbstractSaxHandler;
import org.xml.sax.Attributes;

/**
 * @author Roberto E. Escobar
 */
public class V0_9_2TxDetailsHandler extends AbstractSaxHandler {
   private final Map<Long, Integer> branchToBaseTx;

   public V0_9_2TxDetailsHandler(Map<Long, Integer> branchToBaseTx) {
      this.branchToBaseTx = branchToBaseTx;
   }

   @Override
   public void endElementFound(String uri, String localName, String qName) {
      //
   }

   @Override
   public void startElementFound(String uri, String localName, String qName, Attributes attributes) {
      if (localName.equals("entry")) {
         int txType = Integer.parseInt(attributes.getValue("tx_type"));
         TransactionDetailsType detailsType = TransactionDetailsType.valueOf(txType);
         if (detailsType.isBaseline()) {
            Long branchUuid = Long.parseLong(attributes.getValue("branch_id"));
            Integer baseTransaction = Integer.parseInt(attributes.getValue("transaction_id"));
            branchToBaseTx.put(branchUuid, baseTransaction);
         }
      }
   }
}