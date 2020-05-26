/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.framework.ui.skynet.dbHealth;

import java.io.IOException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.results.MultiPageResultsProvider;

/**
 * @author Roberto E. Escobar
 * @author Jeff C. Phillips
 */
public abstract class DatabaseHealthOperation extends AbstractOperation {

   private boolean isFixOperationEnabled;
   private int itemToFixCount;

   private Appendable appendableBuffer;
   private final StringBuilder detailedReport;
   private final MultiPageResultsProvider resultsProvider = new MultiPageResultsProvider(this);

   protected DatabaseHealthOperation(String operationName) {
      super(operationName, Activator.PLUGIN_ID);
      this.isFixOperationEnabled = false;
      this.appendableBuffer = new StringBuilder();
      this.detailedReport = new StringBuilder();
      this.itemToFixCount = 0;
   }

   @Override
   public String getName() {
      return isFixOperationEnabled() ? getFixTaskName() : getVerifyTaskName();
   }

   public String getVerifyTaskName() {
      return String.format("Check for %s", super.getName());
   }

   public String getFixTaskName() {
      return String.format("Fix %s", super.getName());
   }

   public void setFixOperationEnabled(boolean isFixOperationEnabled) {
      this.isFixOperationEnabled = isFixOperationEnabled;
   }

   public boolean isFixOperationEnabled() {
      return isFixOperationEnabled;
   }

   public void setSummary(Appendable appendableBuffer) {
      this.appendableBuffer = appendableBuffer;
   }

   public Appendable getSummary() {
      return appendableBuffer;
   }

   public void appendToDetails(String value) throws IOException {
      getDetailedReport().append(value);
   }

   public Appendable getDetailedReport() {
      return detailedReport;
   }

   public MultiPageResultsProvider getResultsProvider() {
      return resultsProvider;
   }

   protected void setItemsToFix(int value) {
      this.itemToFixCount = value;
   }

   public boolean hadItemsToFix() {
      return getItemsToFixCount() > 0;
   }

   public int getItemsToFixCount() {
      return itemToFixCount;
   }

   @Override
   protected final void doWork(IProgressMonitor monitor) throws Exception {
      detailedReport.delete(0, detailedReport.length());
      setItemsToFix(0);
      doHealthCheck(monitor);
   }

   /**
    * @return Returns description of check and any other useful information i.e. time of execution
    */
   public abstract String getCheckDescription();

   /**
    * @return Returns description of what will happen and any consequences to clients
    */
   public abstract String getFixDescription();

   protected abstract void doHealthCheck(IProgressMonitor monitor) throws Exception;
}
