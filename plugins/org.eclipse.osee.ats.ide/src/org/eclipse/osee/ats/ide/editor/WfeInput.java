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

package org.eclipse.osee.ats.ide.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.ats.api.data.AtsArtifactImages;
import org.eclipse.osee.ats.ide.AtsArtifactImageProvider;
import org.eclipse.osee.ats.ide.AtsImage;
import org.eclipse.osee.ats.ide.internal.AtsApiService;
import org.eclipse.osee.ats.ide.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.ArtifactEditorInput;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * @author Donald G. Dunne
 */
public class WfeInput extends ArtifactEditorInput {

   public WfeInput(Artifact artifact) {
      super(artifact);
   }

   public WfeInput(BranchId branch, ArtifactId artId, String title) {
      super(branch, artId, title);
   }

   @Override
   public boolean isReload() {
      return getArtifact() == null;
   }

   @Override
   public IPersistableElement getPersistable() {
      return this;
   }

   @Override
   public void saveState(IMemento memento) {
      WfeInputFactory.saveState(memento, this);
   }

   @Override
   public String getFactoryId() {
      return WfeInputFactory.ID;
   }

   @Override
   public String getName() {
      String name = getSavedTitle();
      if (getArtifact() != null && !getArtifact().isDeleted()) {
         if (isBacklog()) {
            name = "Backlog: " + getArtifact().getName();
         } else {
            name = ((AbstractWorkflowArtifact) getArtifact()).getEditorTitle();
         }
      }
      return name;
   }

   boolean isBacklog() {
      return AtsApiService.get().getAgileService().isBacklog(getArtifact());
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      if (AtsApiService.get().getAgileService().isBacklog(getArtifact())) {
         return ImageManager.getImageDescriptor(
            AtsArtifactImageProvider.getKeyedImage(AtsArtifactImages.AGILE_BACKLOG));
      }
      return ImageManager.getImageDescriptor(AtsImage.TEAM_WORKFLOW);
   }

   /**
    * Need to override so this editor is not confused with Artifact Editor
    */
   @Override
   public boolean equals(Object obj) {
      if (obj instanceof WfeInput) {
         WfeInput other = (WfeInput) obj;
         if (getArtId().notEqual(other.getArtId())) {
            return false;
         }
         return getBranchId().equals(other.getBranchId());
      }
      return false;
   }

}