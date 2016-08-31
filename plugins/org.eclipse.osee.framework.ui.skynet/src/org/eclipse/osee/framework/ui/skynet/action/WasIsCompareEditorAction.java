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
package org.eclipse.osee.framework.ui.skynet.action;

import static org.eclipse.osee.framework.core.data.TransactionId.SENTINEL;
import java.net.URI;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.core.client.OseeClientProperties;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.change.AttributeChange;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.skynet.core.utility.ConnectionHandler;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.commandHandlers.Handlers;
import org.eclipse.osee.framework.ui.skynet.compare.CompareHandler;
import org.eclipse.osee.framework.ui.skynet.compare.CompareItem;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.osee.jaxrs.client.JaxRsClient;
import org.eclipse.osee.jaxrs.client.JaxRsExceptions;
import org.eclipse.osee.jdbc.JdbcStatement;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;

/**
 * @author Donald G. Dunne
 */
public class WasIsCompareEditorAction extends Action {

   private static String ATTRIBUTE_TRANSACTIONS_QUERY_DESC =
      "SELECT txs.transaction_id, txs.gamma_id FROM osee_attribute atr, osee_txs txs WHERE atr.attr_id = ? AND atr.gamma_id = txs.gamma_id AND txs.branch_id = ? order by gamma_id desc";

   public WasIsCompareEditorAction() {
      super("View Was/Is Comparison");
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(FrameworkImage.COMPARE_DOCUMENTS);
   }

   @Override
   public void run() {
      try {
         ISelection selection =
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
         if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;

            List<Change> localChanges = Handlers.getArtifactChangesFromStructuredSelection(structuredSelection);
            if (localChanges.isEmpty() || localChanges.size() > 1) {
               AWorkbench.popup("Can only show Was/Is for single selection");
               return;
            }
            Change change = localChanges.iterator().next();
            List<TransactionRecord> transactionsFromStructuredSelection =
               Handlers.getTransactionsFromStructuredSelection(structuredSelection);
            TransactionId transactionId = transactionsFromStructuredSelection.iterator().next();
            List<Artifact> artifactsFromStructuredSelection =
               Handlers.getArtifactsFromStructuredSelection(structuredSelection);
            Artifact artifact = artifactsFromStructuredSelection.iterator().next();

            String was = change.getWasValue();
            int attrId = ((AttributeChange) change).getAttrId();
            TransactionId previousTransaction = getPreviousTransaction(artifact.getBranchId(), attrId, transactionId);
            if (!Strings.isValid(was) && change instanceof AttributeChange) {
               if (previousTransaction.isValid()) {
                  was = loadAttributeValue(attrId, previousTransaction, artifact);
               }
            }

            String is = change.getIsValue();
            if (!Strings.isValid(is) && change instanceof AttributeChange) {
               is = loadAttributeValue(attrId, transactionId, artifact);
            }
            CompareHandler compareHandler = new CompareHandler(String.format("Compare [%s]", change),
               new CompareItem(String.format("Was [Transaction: %s]", previousTransaction), was,
                  System.currentTimeMillis()),
               new CompareItem(String.format("Is [Transaction: %s]", transactionId), is, System.currentTimeMillis()),
               null);
            compareHandler.compare();
         }
      } catch (Exception ex) {
         OseeLog.log(getClass(), OseeLevel.SEVERE_POPUP, ex);
      }
   }

   private TransactionId getPreviousTransaction(long branchUuid, int attrId, TransactionId transactionId) {
      TransactionId previousTransaction = TransactionId.SENTINEL;
      boolean found = false;
      JdbcStatement chStmt = ConnectionHandler.getStatement();
      try {
         chStmt.runPreparedQuery(ATTRIBUTE_TRANSACTIONS_QUERY_DESC, attrId, branchUuid);
         while (chStmt.next()) {
            TransactionId transaction = TransactionId.valueOf(chStmt.getLong("transaction_id"));
            if (found) {
               return transaction;
            }
            if (transactionId.equals(transaction)) {
               found = true;
            }
         }
      } finally {
         chStmt.close();
      }
      return previousTransaction;
   }

   private String loadAttributeValue(int attrId, TransactionId transactionId, Artifact artifact) {
      String appServer = OseeClientProperties.getOseeApplicationServer();
      URI uri =
         UriBuilder.fromUri(appServer).path("orcs").path("branch").path(String.valueOf(artifact.getBranchId())).path(
            "artifact").path(artifact.getGuid()).path("attribute").path(String.valueOf(attrId)).path("version").path(
               String.valueOf(transactionId)).build();
      try {
         return JaxRsClient.newClient().target(uri).request(MediaType.TEXT_PLAIN).get(String.class);
      } catch (Exception ex) {
         throw JaxRsExceptions.asOseeException(ex);
      }

   }

   private static ISelectionProvider getSelectionProvider() {
      ISelectionProvider selectionProvider = null;
      IWorkbench workbench = PlatformUI.getWorkbench();
      if (!workbench.isStarting() && !workbench.isClosing()) {
         IWorkbenchPage page = AWorkbench.getActivePage();
         if (page != null) {
            IWorkbenchPart part = page.getActivePart();
            if (part != null) {
               IWorkbenchSite site = part.getSite();
               if (site != null) {
                  selectionProvider = site.getSelectionProvider();
               }
            }
         }
      }
      return selectionProvider;
   }

   public static boolean isEnabledStatic() {
      if (PlatformUI.getWorkbench().isClosing()) {
         return false;
      }
      boolean isEnabled = false;

      ISelectionProvider selectionProvider = getSelectionProvider();
      if (selectionProvider != null) {
         ISelection selection = selectionProvider.getSelection();
         if (selection instanceof IStructuredSelection) {
            isEnabled = ((IStructuredSelection) selection).size() == 1;
         }
      }
      return isEnabled;
   }
}
