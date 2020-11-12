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

package org.eclipse.osee.framework.ui.skynet.commandHandlers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.skynet.core.conflict.Conflict;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.change.view.ChangeReportEditor;
import org.eclipse.osee.framework.ui.skynet.explorer.ArtifactExplorer;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * This is a utility class for OSEE handlers
 *
 * @author Jeff C. Phillips
 */
public class Handlers {

   /**
    * Populates a list of ArtifactChange from a IStructuredSelection. Returns an empty list if no ArtifactChange were
    * found.
    */
   public static List<Change> getArtifactChangesFromStructuredSelection(IStructuredSelection structuredSelection) {
      return processSelectionObjects(Change.class, structuredSelection);
   }

   /**
    * Populates a list of TransactionIds from a IStructuredSelection. Returns an empty list if no TransactionIds were
    * found.
    */
   public static List<TransactionToken> getTransactionsFromStructuredSelection(IStructuredSelection structuredSelection) {
      return processSelectionObjects(TransactionToken.class, structuredSelection);
   }

   /**
    * Populates a list of branches from a IStructuredSelection. Returns an empty list if no branches were found.
    */
   public static List<IOseeBranch> getBranchesFromStructuredSelection(IStructuredSelection structuredSelection) {
      return processSelectionObjects(IOseeBranch.class, structuredSelection);
   }

   public static List<Branch> getBranchesFromStructuredSelection2(IStructuredSelection structuredSelection) {
      return processSelectionObjects(Branch.class, structuredSelection);
   }

   /**
    * Populates a list of artifacts from a IStructuredSelection. Returns an empty list if no artifacts were found.
    */
   public static List<Artifact> getArtifactsFromStructuredSelection(IStructuredSelection structuredSelection) {
      return processSelectionObjects(Artifact.class, structuredSelection);
   }

   /**
    * Populates a list of artifacts from a IStructuredSelection. Returns an empty list if no artifacts were found.
    */
   public static List<Conflict> getConflictsFromStructuredSelection(IStructuredSelection structuredSelection) {
      return processSelectionObjects(Conflict.class, structuredSelection);
   }

   /**
    * @return Returns a list of objects from the sturctruedSelection that are an instance of the Class
    */
   public static <E> List<E> processSelectionObjects(Class<E> clazz, IStructuredSelection structuredSelection) {
      List<E> objects = new LinkedList<>();
      Iterator<?> iterator = structuredSelection.iterator();

      while (iterator.hasNext()) {
         Object object = iterator.next();
         if (clazz.isAssignableFrom(object.getClass())) {
            objects.add(clazz.cast(object));
         } else {
            Object targetObject = null;

            if (object instanceof IAdaptable) {
               targetObject = ((IAdaptable) object).getAdapter(clazz);
            } else if (object instanceof Match) {
               targetObject = ((Match) object).getElement();
            }

            if (clazz.isInstance(targetObject)) {
               objects.add(clazz.cast(targetObject));
            }
         }
      }
      return objects;
   }

   public static ArtifactId getViewId() {
      IWorkbench workbench = PlatformUI.getWorkbench();
      if (!workbench.isStarting() && !workbench.isClosing()) {
         IWorkbenchPage page = AWorkbench.getActivePage();
         if (page != null) {
            IWorkbenchPart activePart = page.getActivePart();
            if (activePart instanceof ArtifactExplorer) {
               ArtifactId viewId = ((ArtifactExplorer) activePart).getViewId();
               return viewId == null ? ArtifactId.SENTINEL : viewId;
            }
            if (activePart instanceof ChangeReportEditor) {
               return ((ChangeReportEditor) activePart).getViewId();
            }
            IEditorPart activeEditor = page.getActiveEditor();
            if (activeEditor instanceof ChangeReportEditor) {
               return ((ChangeReportEditor) activeEditor).getViewId();
            }
         }
      }
      return ArtifactId.SENTINEL;
   }
}