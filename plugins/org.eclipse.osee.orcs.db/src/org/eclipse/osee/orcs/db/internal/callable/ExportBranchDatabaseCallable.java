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
package org.eclipse.osee.orcs.db.internal.callable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import org.eclipse.osee.database.schema.DatabaseCallable;
import org.eclipse.osee.executor.admin.ExecutionCallbackAdapter;
import org.eclipse.osee.executor.admin.ExecutorAdmin;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.database.core.AbstractJoinQuery;
import org.eclipse.osee.framework.database.core.ExportImportJoinQuery;
import org.eclipse.osee.framework.database.core.JoinUtility;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.ExportOptions;
import org.eclipse.osee.orcs.core.SystemPreferences;
import org.eclipse.osee.orcs.db.internal.exchange.ExchangeUtil;
import org.eclipse.osee.orcs.db.internal.exchange.ExportItemFactory;
import org.eclipse.osee.orcs.db.internal.exchange.export.AbstractExportItem;
import org.eclipse.osee.orcs.db.internal.resource.ResourceConstants;

/**
 * @author Roberto E. Escobar
 */
public class ExportBranchDatabaseCallable extends DatabaseCallable<URI> {

   private static final String BRANCH_EXPORT_EXECUTOR_ID = "branch.export.worker";

   private final ExportItemFactory factory;

   private final SystemPreferences preferences;
   private final ExecutorAdmin executorAdmin;
   private final BranchCache branchCache;
   private final List<IOseeBranch> branches;
   private final PropertyStore options;

   private String exportName;

   public ExportBranchDatabaseCallable(ExportItemFactory factory, SystemPreferences preferences, ExecutorAdmin executorAdmin, BranchCache branchCache, List<IOseeBranch> branches, PropertyStore options, String exportName) {
      super(factory.getLogger(), factory.getDbService());
      this.factory = factory;
      this.preferences = preferences;
      this.executorAdmin = executorAdmin;
      this.branchCache = branchCache;
      this.branches = branches;
      this.options = options;
      this.exportName = exportName;
   }

   private SystemPreferences getSystemPreferences() {
      return preferences;
   }

   private BranchCache getBranchCache() {
      return branchCache;
   }

   private ExecutorAdmin getExecutorAdmin() {
      return executorAdmin;
   }

   private String getExchangeFileName() {
      return this.exportName;
   }

   private void setExchangeFileName(String name) {
      this.exportName = name;
   }

   @Override
   public URI call() throws Exception {
      long startTime = System.currentTimeMillis();
      try {
         Conditions.checkNotNull(factory, "exportItemFactory");
         Conditions.checkNotNull(executorAdmin, "executorAdmin");

         Conditions.checkNotNullOrEmpty(branches, "branches");
         Conditions.checkNotNull(options, "options");
         doWork();
         return factory.getResourceManager().generateResourceLocator(ResourceConstants.EXCHANGE_RESOURCE_PROTOCOL, "",
            getExchangeFileName()).getLocation();
      } finally {
         getLogger().info("Exported [%s] branch%s in [%s]", branches.size(), branches.size() != 1 ? "es" : "",
            Lib.getElapseString(startTime));
      }
   }

   private File createTempFolder() throws OseeCoreException {
      String exchangeBasePath = ResourceConstants.getExchangeDataPath(getSystemPreferences());
      File rootDirectory = ExchangeUtil.createTempFolder(exchangeBasePath);
      if (!Strings.isValid(getExchangeFileName())) {
         setExchangeFileName(rootDirectory.getName());
      }
      return rootDirectory;
   }

   private void doWork() throws Exception {
      ExportImportJoinQuery joinQuery = JoinUtility.createExportImportJoinQuery(getDatabaseService());
      BranchCache branchCache = getBranchCache();
      for (IOseeBranch branch : branches) {
         int branchId = branchCache.getLocalId(branch);
         joinQuery.add((long) branchId, -1L);
      }
      joinQuery.store();

      List<AbstractExportItem> taskList = factory.createTaskList(joinQuery.getQueryId(), options);
      try {
         File tempFolder = createTempFolder();

         for (AbstractExportItem exportItem : taskList) {
            exportItem.setWriteLocation(tempFolder);
         }

         executeTasks(taskList);

         finishExport(tempFolder);
      } finally {
         cleanUp(joinQuery, taskList);
      }
   }

   private void cleanUp(AbstractJoinQuery joinQuery, List<AbstractExportItem> taskList) {
      for (AbstractExportItem exportItem : taskList) {
         exportItem.cleanUp();
      }
      try {
         joinQuery.delete();
      } catch (OseeCoreException ex) {
         getLogger().warn(ex, "Error during export clean-up");
      }
   }

   private void finishExport(File tempFolder) throws IllegalArgumentException, IOException {
      String zipTargetName = getExchangeFileName() + "." + ResourceConstants.ZIP_EXTENSION;

      if (options.getBoolean(ExportOptions.COMPRESS.name())) {
         getLogger().info("Compressing Branch Export Data - [%s]", zipTargetName);
         File zipTarget = new File(tempFolder.getParent(), zipTargetName);
         Lib.compressDirectory(tempFolder, zipTarget.getAbsolutePath(), true);
         getLogger().info("Deleting Branch Export Temp Folder - [%s]", tempFolder);
         Lib.deleteDir(tempFolder);
      } else {
         File target = new File(tempFolder.getParent(), getExchangeFileName());
         if (!target.equals(tempFolder)) {
            if (!tempFolder.renameTo(target)) {
               getLogger().info("Unable to move [%s] to [%s]", tempFolder.getAbsolutePath(), target.getAbsolutePath());
            }
         }
      }
   }

   private void executeTasks(List<AbstractExportItem> taskList) throws Exception {
      final List<Throwable> throwables = new LinkedList<Throwable>();
      final List<Future<?>> futures = new CopyOnWriteArrayList<Future<?>>();

      ExecutorAdmin executor = getExecutorAdmin();
      for (AbstractExportItem exportItem : taskList) {
         Future<?> future =
            executor.schedule(BRANCH_EXPORT_EXECUTOR_ID, exportItem, new ExecutionCallbackAdapter<Boolean>() {

               @Override
               public void onFailure(Throwable throwable) {
                  super.onFailure(throwable);
                  throwables.add(throwable);
                  for (Future<?> future : futures) {
                     if (!future.isDone() && !future.isCancelled()) {
                        future.cancel(true);
                     }
                  }
               }

            });
         futures.add(future);
      }

      for (Future<?> future : futures) {
         future.get();
      }

      if (!throwables.isEmpty()) {
         List<StackTraceElement> trace = new LinkedList<StackTraceElement>();
         for (Throwable th : throwables) {
            for (StackTraceElement element : th.getStackTrace()) {
               trace.add(element);
            }
         }
         OseeCoreException exception = new OseeCoreException("Error detected during branch export");
         exception.setStackTrace(trace.toArray(new StackTraceElement[trace.size()]));
         throw exception;
      }
   }

}
