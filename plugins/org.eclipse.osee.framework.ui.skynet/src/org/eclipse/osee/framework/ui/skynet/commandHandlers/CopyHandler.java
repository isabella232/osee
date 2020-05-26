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
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.internal.ServiceUtil;
import org.eclipse.osee.framework.ui.skynet.util.ArtifactClipboard;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Jeff C. Phillips
 */
public class CopyHandler extends AbstractHandler {
   @Override
   public Object execute(ExecutionEvent event) throws ExecutionException {
      if (HandlerUtil.getActivePartChecked(event) instanceof ViewPart) {
         ViewPart view = (ViewPart) HandlerUtil.getActivePartChecked(event);
         IWorkbenchPartSite myIWorkbenchPartSite = view.getSite();
         ISelectionProvider selectionProvider = myIWorkbenchPartSite.getSelectionProvider();

         if (selectionProvider != null && selectionProvider.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) selectionProvider.getSelection();

            List<String> names = new LinkedList<>();
            List<Artifact> artifacts = new LinkedList<>();
            ArtifactClipboard clipboard = new ArtifactClipboard(view.getSite().getId());
            Iterator<?> iterator = selection.iterator();
            Object selectionObject = null;
            try {
               while (iterator.hasNext()) {
                  Object object = iterator.next();

                  if (object instanceof IAdaptable) {
                     selectionObject = ((IAdaptable) object).getAdapter(IOseeBranch.class);

                     if (selectionObject == null) {
                        selectionObject = ((IAdaptable) object).getAdapter(Artifact.class);
                     }
                  } else if (object instanceof Match) {
                     selectionObject = ((Match) object).getElement();
                  }

                  if (selectionObject instanceof IOseeBranch) {
                     names.add(((IOseeBranch) selectionObject).getName());
                  } else if (selectionObject instanceof Artifact) {
                     Artifact artifact = (Artifact) selectionObject;
                     names.add(artifact.getName());
                     artifacts.add(artifact);
                  }
               }

               if (!names.isEmpty() && artifacts.isEmpty()) {
                  clipboard.setTextToClipboard(names);
               } else if (!names.isEmpty() && !artifacts.isEmpty()) {
                  try {
                     clipboard.setArtifactsToClipboard(ServiceUtil.getAccessPolicy(), artifacts);
                  } catch (OseeCoreException ex) {
                     clipboard.dispose();
                     throw new ExecutionException(ex.getLocalizedMessage());
                  }
               }
            } finally {
               clipboard.dispose();
            }
         }
      }
      return null;
   }

   @Override
   public boolean isHandled() {
      return true;
   }

   @Override
   public boolean isEnabled() {
      return true;
   }
}
