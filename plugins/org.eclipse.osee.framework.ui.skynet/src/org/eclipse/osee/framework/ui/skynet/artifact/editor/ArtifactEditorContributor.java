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

package org.eclipse.osee.framework.ui.skynet.artifact.editor;

import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.SelectionCountChangeListener;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageManager;
import org.eclipse.osee.framework.ui.skynet.OseeStatusContributionItemFactory;
import org.eclipse.osee.framework.ui.skynet.RelationsComposite;
import org.eclipse.osee.framework.ui.skynet.action.RevealInExplorerAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.StatusLineContributionItem;

public class ArtifactEditorContributor extends MultiPageEditorActionBarContributor {

   private StatusLineContributionItem typeStatusItem;
   private RevealInExplorerAction showInExplorerAction;
   private Artifact artifact;

   public ArtifactEditorContributor() {
      super();
      createActions();
   }

   @Override
   public void setActiveEditor(IEditorPart part) {
      super.setActiveEditor(part);
      artifact = part.getAdapter(Artifact.class);
      if (artifact != null) {
         typeStatusItem.setText(artifact.getArtifactType().getName());
         typeStatusItem.setImage(ArtifactImageManager.getImage(artifact));

         RelationsComposite composite = part.getAdapter(RelationsComposite.class);
         if (composite != null) {
            composite.getTreeViewer().addSelectionChangedListener(
               new SelectionCountChangeListener(this.getActionBars().getStatusLineManager()));
         }
      }
   }

   @Override
   public void setActivePage(IEditorPart part) {
      // do nothing
   }

   private void createActions() {
      typeStatusItem = new StatusLineContributionItem("skynet.artifactType", true, 25);
      typeStatusItem.setToolTipText("The type of the artifact being edited.");

      showInExplorerAction = new RevealInExplorerAction(artifact);
   }

   @Override
   public void contributeToStatusLine(IStatusLineManager statusLineManager) {
      statusLineManager.add(typeStatusItem);
      OseeStatusContributionItemFactory.addTo(statusLineManager);
   }

   @Override
   public void contributeToCoolBar(ICoolBarManager coolBarManager) {
      coolBarManager.add(showInExplorerAction);
   }

}
