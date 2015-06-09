/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.account.admin.internal;

import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsAdmin;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.search.QueryBuilder;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.eclipse.osee.orcs.transaction.TransactionBuilder;
import org.eclipse.osee.orcs.transaction.TransactionFactory;

/**
 * @author Roberto E. Escobar
 */
public abstract class AbstractOrcsStorage {

   private Log logger;
   private OrcsApi orcsApi;

   private IOseeBranch storageBranch;
   private AccountFactory factory;

   public void setLogger(Log logger) {
      this.logger = logger;
   }

   public void setOrcsApi(OrcsApi orcsApi) {
      this.orcsApi = orcsApi;
   }

   public void setAccountFactory(AccountFactory factory) {
      this.factory = factory;
   }

   public void start() {
      logger.trace("Starting [%s]...", getClass().getSimpleName());
      storageBranch = CoreBranches.COMMON;
   }

   public void stop() {
      logger.trace("Stopping [%s]...", getClass().getSimpleName());
      storageBranch = null;
   }

   private IOseeBranch getBranch() {
      return storageBranch;
   }

   protected Log getLogger() {
      return logger;
   }

   protected AccountFactory getFactory() {
      return factory;
   }

   protected QueryBuilder newQuery() {
      QueryFactory queryFactory = orcsApi.getQueryFactory();
      return queryFactory.fromBranch(getBranch());
   }

   @SuppressWarnings("unchecked")
   protected ArtifactReadable getSystemUser() {
      return newQuery().andIds(SystemUser.OseeSystem).getResults().getExactlyOne();
   }

   protected TransactionBuilder newTransaction(String comment) {
      TransactionFactory transactionFactory = orcsApi.getTransactionFactory();
      return transactionFactory.createTransaction(getBranch(), getSystemUser(), comment);
   }

   protected boolean isInitialized() {
      OrcsAdmin adminOps = orcsApi.getAdminOps();
      return adminOps.isDataStoreInitialized();
   }

}
