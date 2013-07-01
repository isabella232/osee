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
package org.eclipse.osee.orcs.core.internal.console;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import org.eclipse.osee.console.admin.Console;
import org.eclipse.osee.console.admin.ConsoleCommand;
import org.eclipse.osee.console.admin.ConsoleParameters;
import org.eclipse.osee.executor.admin.CancellableCallable;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;
import org.eclipse.osee.orcs.ExportOptions;
import org.eclipse.osee.orcs.ImportOptions;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.OrcsBranch;

/**
 * @author Roberto E. Escobar
 */
public final class BranchImportCommand implements ConsoleCommand {

   private OrcsApi orcsApi;

   public void setOrcsApi(OrcsApi orcsApi) {
      this.orcsApi = orcsApi;
   }

   public OrcsApi getOrcsApi() {
      return orcsApi;
   }

   @Override
   public String getName() {
      return "branch_import";
   }

   @Override
   public String getDescription() {
      return "Import a specific set of branches from an exchange zip file";
   }

   @Override
   public String getUsage() {
      return "uri=<EXCHANGE_FILE_LOCATION,...> [branchIds=<BRANCH_IDS,...>] [minTx=<TX_ID>] [maxTx=<TX_ID>] [excludeBaselineTxs=<TRUE|FALSE>] [allAsRootBranches=<TRUE|FALSE>] [clean=<TRUE|FALSE>]";
   }

   @Override
   public Callable<?> createCallable(Console console, ConsoleParameters params) {
      List<String> importFiles = Arrays.asList(params.getArray("uri"));
      List<String> branchIds = Arrays.asList(params.getArray("branchIds"));

      PropertyStore options = new PropertyStore();
      if (params.exists("minTx")) {
         options.put(ExportOptions.MIN_TXS.name(), params.getLong("minTx"));
      }
      if (params.exists("maxTx")) {
         options.put(ExportOptions.MAX_TXS.name(), params.getLong("maxTx"));
      }
      options.put(ImportOptions.EXCLUDE_BASELINE_TXS.name(), params.getBoolean("excludeBaselineTxs"));
      options.put(ImportOptions.ALL_AS_ROOT_BRANCHES.name(), params.getBoolean("allAsRootBranches"));
      options.put(ImportOptions.CLEAN_BEFORE_IMPORT.name(), params.getBoolean("clean"));

      OrcsBranch orcsBranch = getOrcsApi().getBranchOps(null);
      return new ImportBranchDelegateCallable(console, orcsBranch, getOrcsApi().getBranchCache(), options, importFiles,
         branchIds);
   }

   private static final class ImportBranchDelegateCallable extends CancellableCallable<Boolean> {

      private final Console console;
      private final OrcsBranch orcsBranch;
      private final BranchCache branchCache;
      private final PropertyStore options;
      private final List<String> importFiles;
      private final List<String> branchIds;

      public ImportBranchDelegateCallable(Console console, OrcsBranch orcsBranch, BranchCache branchCache, PropertyStore options, List<String> importFiles, List<String> branchIds) {
         super();
         this.console = console;
         this.orcsBranch = orcsBranch;
         this.branchCache = branchCache;
         this.options = options;
         this.importFiles = importFiles;
         this.branchIds = branchIds;
      }

      @Override
      public Boolean call() throws Exception {
         if (importFiles.isEmpty()) {
            throw new OseeArgumentException("Files to import were not specified");
         }

         List<IOseeBranch> branches = new LinkedList<IOseeBranch>();
         for (String branchIdString : branchIds) {
            IOseeBranch branch = branchCache.getById(Integer.parseInt(branchIdString));
            branches.add(branch);
         }

         for (String fileToImport : importFiles) {
            URI uri = new URI("exchange://" + fileToImport);
            console.writeln("Importing from [%s]", uri);
            Callable<URI> callable = orcsBranch.importBranch(uri, branches, options);
            callable.call();
            checkForCancelled();
         }
         return Boolean.TRUE;
      }
   }
}
