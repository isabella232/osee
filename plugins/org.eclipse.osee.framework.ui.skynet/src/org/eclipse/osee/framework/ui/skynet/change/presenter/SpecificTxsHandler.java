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
package org.eclipse.osee.framework.ui.skynet.change.presenter;

import java.text.NumberFormat;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.model.TransactionDelta;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.AXml;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.change.ChangeUiData;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.swt.KeyedImage;

public final class SpecificTxsHandler implements IChangeReportUiHandler {

   private static final int BRANCH_NAME_LEN = 60;

   @Override
   public String getActionName() {
      return "Compare two transactions on a branch";
   }

   @Override
   public String getActionDescription() {
      return "Compares two specific transactions on a branch.";
   }

   @Override
   public String getName(TransactionDelta txDelta) {
      String branchName;
      try {
         branchName = BranchManager.getBranch(txDelta.getEndTx().getBranch()).getShortName(BRANCH_NAME_LEN);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex.toString(), ex);
         branchName = "Unknown";
      }
      String toReturn;
      if (txDelta.getEndTx().getComment() != null) {
         toReturn = String.format("%s", txDelta.getEndTx().getComment());
      } else {
         toReturn = String.format("%s - Transactions", branchName);
      }
      return toReturn;
   }

   @Override
   public KeyedImage getActionImage() {
      return FrameworkImage.BRANCH_CHANGE;
   }

   @Override
   public KeyedImage getScenarioImage(ChangeUiData changeUiData) {
      return FrameworkImage.DELTAS_TXS_SAME_BRANCH;
   }

   @Override
   public String getScenarioDescriptionHtml(ChangeUiData changeUiData) throws OseeCoreException {
      TransactionDelta txDelta = changeUiData.getTxDelta();
      NumberFormat formatter = NumberFormat.getInstance();
      return String.format("Shows changes made to [<b>%s</b>] between transactions [<b>%s</b>] and [<b>%s</b>].",
         AXml.textToXml(BranchManager.getBranchName(txDelta.getStartTx())),
         AXml.textToXml(formatter.format(txDelta.getStartTx().getId())),
         AXml.textToXml(formatter.format(txDelta.getEndTx().getId())));
   }

   @Override
   public void appendTransactionInfoHtml(StringBuilder sb, ChangeUiData changeUiData) {
      TransactionDelta txDelta = changeUiData.getTxDelta();
      //      sb.append("<b>Branch 1 Last Modified</b>:<br/>");
      //      ChangeReportInfoPresenter.addTransactionInfo(sb, txDelta.getStartTx());
      //      sb.append("<br/><br/>");
      sb.append("<b>Committed: </b><br/>");
      ChangeReportInfoPresenter.addTransactionInfo(sb, txDelta.getEndTx());
   }
}