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
package org.eclipse.osee.define.traceability.operations;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osee.define.traceability.TraceUnitExtensionManager;
import org.eclipse.osee.define.traceability.TraceUnitExtensionManager.TraceHandler;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;

/**
 * @author Roberto E. Escobar
 */
public class TraceUnitFromResourceOperation {

   public static Set<String> getTraceUnitHandlerIds() throws OseeCoreException {
      return TraceUnitExtensionManager.getInstance().getTraceUnitHandlerIds();
   }

   private static ResourceToTraceUnit getResourceToTestUnit(Iterable<URI> sources, boolean isRecursive, boolean isFileWithMultiplePaths, boolean addGuidToSourceFile, String... testUnitTraceIds) throws OseeCoreException {
      checkSourceArgument(sources);
      checkTraceUnitHandlerIdsArgument(testUnitTraceIds);

      ResourceToTraceUnit operation = new ResourceToTraceUnit(sources, isRecursive, isFileWithMultiplePaths);
      TraceUnitExtensionManager traceManager = TraceUnitExtensionManager.getInstance();
      for (String traceUnitHandlerId : testUnitTraceIds) {

         TraceHandler handler = traceManager.getTraceUnitHandlerById(traceUnitHandlerId);
         if (handler != null) {
            operation.addTraceUnitHandler(handler.getLocator(), handler.getParser());
         }
      }
      return operation;
   }

   public static void printTraceFromTestUnits(IProgressMonitor monitor, Iterable<URI> sources, boolean isRecursive, boolean isFileWithMultiplePaths, boolean addGuidToSourceFile, String... traceUnitHandlerIds) throws OseeCoreException {
      ResourceToTraceUnit operation =
         getResourceToTestUnit(sources, isRecursive, isFileWithMultiplePaths, addGuidToSourceFile, traceUnitHandlerIds);
      if (monitor == null) {
         monitor = new NullProgressMonitor();
      }
      operation.addTraceProcessor(new TraceUnitReportProcessor());
      operation.execute(monitor);
   }

   public static void importTraceFromTestUnits(IProgressMonitor monitor, Iterable<URI> sources, boolean isRecursive, boolean isFileWithMultiplePaths, IOseeBranch importToBranch, boolean addGuidToSourceFile, String... traceUnitHandlerIds) throws OseeCoreException {
      checkBranchArguments(importToBranch);

      ResourceToTraceUnit operation =
         getResourceToTestUnit(sources, isRecursive, isFileWithMultiplePaths, addGuidToSourceFile, traceUnitHandlerIds);
      if (monitor == null) {
         monitor = new NullProgressMonitor();
      }
      operation.addTraceProcessor(new TraceUnitToArtifactProcessor(importToBranch, addGuidToSourceFile));
      operation.execute(monitor);
   }

   private static void checkTraceUnitHandlerIdsArgument(String... traceUnitHandlerIds) throws OseeCoreException {
      if (traceUnitHandlerIds == null) {
         throw new OseeArgumentException("Test unit trace ids was null");
      }
      if (traceUnitHandlerIds.length == 0) {
         throw new OseeArgumentException("Test unit trace ids was empty");
      }

      try {
         Set<String> ids = getTraceUnitHandlerIds();
         List<String> notFound = Collections.setComplement(Arrays.asList(traceUnitHandlerIds), ids);
         if (!notFound.isEmpty()) {
            throw new OseeArgumentException("Invalid test unit trace id(s) [%s]", notFound);
         }
      } catch (Exception ex) {
         OseeCoreException.wrapAndThrow(ex);
      }
   }

   private static void checkSourceArgument(Iterable<URI> sources) throws OseeArgumentException {
      if (sources == null) {
         throw new OseeArgumentException("Source was null");
      }
      try {
         for (URI source : sources) {
            IFileStore fileStore = EFS.getStore(source);
            IFileInfo fileInfo = fileStore.fetchInfo();
            if (!fileInfo.exists()) {
               throw new OseeArgumentException("Unable to access source: [%s]", source);
            }
         }
      } catch (CoreException ex) {
         throw new OseeArgumentException(ex);
      }
   }

   private static void checkBranchArguments(IOseeBranch importToBranch) throws OseeCoreException {
      if (importToBranch == null) {
         throw new OseeArgumentException("Branch to import into was null");
      }
      BranchType branchType = BranchManager.getBranch(importToBranch).getBranchType();
      if (!branchType.isOfType(BranchType.WORKING)) {
         throw new OseeArgumentException("Branch to import into was not a working branch: [%s]", importToBranch);
      }
   }
}
