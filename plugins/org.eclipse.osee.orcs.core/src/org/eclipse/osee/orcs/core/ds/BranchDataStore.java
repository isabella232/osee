/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.ds;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.ITransaction;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.model.change.ChangeItem;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.CreateBranchData;

/**
 * @author Roberto E. Escobar
 */
public interface BranchDataStore {

   Callable<Branch> createBranch(String sessionId, CreateBranchData branchData);

   Callable<Branch> createBranchCopyTx(String sessionId, CreateBranchData branchData);

   Callable<Branch> purgeBranch(String sessionId, Branch branch);

   Callable<TransactionRecord> commitBranch(String sessionId, ArtifactReadable committer, Branch source, Branch destination);

   Callable<List<ChangeItem>> compareBranch(String sessionId, TransactionRecord sourceTx, TransactionRecord destinationTx);

   Callable<URI> exportBranch(String sessionId, List<IOseeBranch> branches, PropertyStore options, String exportName);

   Callable<URI> importBranch(String sessionId, URI fileToImport, List<IOseeBranch> branches, PropertyStore options);

   Callable<URI> checkBranchExchangeIntegrity(String sessionId, URI fileToCheck);

   Callable<TransactionResult> commitTransaction(String sessionId, TransactionData transaction);

   Callable<?> purgeTransactions(String sessionId, Collection<? extends ITransaction> transactions);

   // Temp
   Callable<String> createUnsubscribeTx(ArtifactReadable userArtifact, ArtifactReadable groupArtifact);
}
