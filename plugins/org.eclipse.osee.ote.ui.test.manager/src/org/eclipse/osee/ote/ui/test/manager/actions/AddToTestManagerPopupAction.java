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
package org.eclipse.osee.ote.ui.test.manager.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.ws.AWorkspace;
import org.eclipse.osee.ote.ui.test.manager.operations.AddIFileToTestManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class AddToTestManagerPopupAction implements IWorkbenchWindowActionDelegate {

   public static String[] getSelection() {
      StructuredSelection sel = AWorkspace.getSelection();
      Iterator<?> i = sel.iterator();
      List<String> selection = new ArrayList<>();

      while (i.hasNext()) {
         Object obj = i.next();
         if (obj instanceof IResource) {
            IResource resource = (IResource) obj;
            selection.add(resource.getLocation().toOSString());
         } else if (obj instanceof ICompilationUnit) {
            ICompilationUnit resource = (ICompilationUnit) obj;
            selection.add(resource.getResource().getLocation().toOSString());
         }
      }
      return selection.toArray(new String[0]);
   }

   IWorkbenchWindow activeWindow = null;

   // IWorkbenchWindowActionDelegate method
   @Override
   public void dispose() {
      // nothing to do
   }

   // IWorkbenchWindowActionDelegate method
   @Override
   public void init(IWorkbenchWindow window) {
      activeWindow = window;
   }

   @Override
   public void run(IAction proxyAction) {
      String[] files = getSelection();
      if (files.length == 0) {
         AWorkbench.popup("ERROR", "Can't retrieve file");
         return;
      }
      AddIFileToTestManager.getOperation().addIFileToScriptsPage(files);
   }

   // IActionDelegate method
   @Override
   public void selectionChanged(IAction proxyAction, ISelection selection) {

   }
}