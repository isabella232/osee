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
package org.eclipse.osee.framework.ui.skynet.blam;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.transaction.AbstractSkynetTxTemplate;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.blam.operation.BlamOperation;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;

/**
 * @author Ryan D. Brooks
 */
public class BlamJob extends Job {
   private final BlamWorkflow workflow;
   private final BlamVariableMap variableMap;
   private final WorkflowEditor editor;
   private final Collection<IBlamEventListener> listeners;

   public BlamJob(WorkflowEditor editor) {
      super(editor.getWorkflow().getDescriptiveName());
      this.editor = editor;
      this.variableMap = editor.getBlamVariableMap();
      this.workflow = editor.getWorkflow();
      this.listeners = new LinkedList<IBlamEventListener>();
   }

   @Override
   protected IStatus run(IProgressMonitor monitor) {
      IStatus toReturn = Status.CANCEL_STATUS;
      long startTime = System.currentTimeMillis();
      notifyListeners(new BlamStartedEvent());
      try {
         List<BlamOperation> operations = workflow.getOperations();
         if (operations.size() == 0) {
            throw new IllegalStateException("No operations were found for this workflow");
         }
         monitor.beginTask(workflow.getDescriptiveName(), operations.size());

         for (BlamOperation operation : operations) {
            IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);

            operation.setWorkflowEditor(editor);
            Branch branch = operation.wrapOperationForBranch(variableMap);
            if (branch == null) {
               operation.runOperation(variableMap, subMonitor);
            } else {
               new BlamOperationTx(branch, operation, subMonitor).execute();
            }

            monitor.worked(1);
         }

         toReturn = Status.OK_STATUS;
      } catch (Exception ex) {
         OSEELog.logException(getClass(), ex, false);
         toReturn = new Status(Status.ERROR, SkynetGuiPlugin.PLUGIN_ID, -1, ex.getLocalizedMessage(), ex);
      } finally {
         monitor.done();
         notifyListeners(new BlamFinishedEvent(System.currentTimeMillis() - startTime));
      }
      return toReturn;
   }

   public void addListener(IBlamEventListener listener) {
      if (listener == null) {
         throw new IllegalArgumentException("listener can not be null");
      }

      listeners.add(listener);
   }

   public boolean removeListener(IBlamEventListener listener) {
      return listeners.remove(listener);
   }

   private void notifyListeners(IBlamEvent event) {
      for (IBlamEventListener listener : listeners) {
         listener.onEvent(event);
      }
   }

   final private class BlamOperationTx extends AbstractSkynetTxTemplate {
      private IProgressMonitor monitor;
      BlamOperation operation;

      public BlamOperationTx(Branch branch, BlamOperation operation, IProgressMonitor monitor) {
         super(branch);
         this.monitor = monitor;
         this.operation = operation;
      }

      /* (non-Javadoc)
       * @see org.eclipse.osee.framework.skynet.core.transaction.AbstractTxTemplate#handleTxWork()
       */
      @Override
      protected void handleTxWork() throws OseeCoreException {
         try {
            operation.runOperation(variableMap, monitor);
         } catch (Exception ex) {
            throw new OseeCoreException(ex);
         }
      }
   }
}