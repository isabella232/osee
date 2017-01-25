/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.artifact.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * @author Donald G. Dunne
 */
public class ArtifactEditorInput implements IEditorInput, IPersistableElement {
   private Artifact artifact;
   private BranchId savedBranchId;
   private Long savedArtUuid;
   private String savedTitle;
   private boolean attemptedReload = false;

   public ArtifactEditorInput(Artifact artifact) {
      this.artifact = artifact;
   }

   public ArtifactEditorInput(BranchId branchId, Long artUuid, String title) {
      this.savedBranchId = branchId;
      this.savedArtUuid = artUuid;
      this.savedTitle = title;
   }

   @Override
   public boolean exists() {
      return true;
   }

   public boolean isReload() {
      return artifact == null && savedArtUuid != null;
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(FrameworkImage.ARTIFACT_EDITOR);
   }

   public Image getImage() {
      return ImageManager.getImage(FrameworkImage.ARTIFACT_EDITOR);
   }

   @Override
   public String getName() {
      if (artifact == null) {
         if (Strings.isValid(savedTitle)) {
            return savedTitle;
         }
         return "No Artifact Input Provided";
      }
      return artifact.getVersionedName();
   }

   @Override
   public IPersistableElement getPersistable() {
      return this;
   }

   @Override
   public String getToolTipText() {
      return getName();
   }

   @SuppressWarnings("unchecked")
   @Override
   public <T> T getAdapter(Class<T> type) {
      if (type != null) {
         if (type.isAssignableFrom(getClass())) {
            return (T) getArtifact();
         } else if (type.isAssignableFrom(Artifact.class)) {
            return (T) getArtifact();
         }
      }
      Object obj = null;
      T object = (T) obj;
      return object;
   }

   public Artifact getArtifact() {
      return loadArtifact();
   }

   public boolean isReadOnly() {
      return getArtifact() == null || getArtifact().isReadOnly();
   }

   public void setArtifact(Artifact art) {
      this.artifact = art;
   }

   @Override
   public void saveState(IMemento memento) {
      ArtifactEditorInputFactory.saveState(memento, this);
   }

   @Override
   public String getFactoryId() {
      return ArtifactEditorInputFactory.ID;
   }

   public Artifact loadArtifact() {
      if (artifact == null && !attemptedReload) {
         if (savedArtUuid != null && savedBranchId.isValid()) {
            try {
               artifact = ArtifactQuery.getArtifactFromId(savedArtUuid, savedBranchId);
            } catch (Exception ex) {
               // do nothing
            }
         }
         attemptedReload = true;
      }
      return artifact;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((getArtUuid() == null) ? 0 : getArtUuid().hashCode());
      result = prime * result + getBranchId().hashCode();
      return result;
   }

   private BranchId getBranchId() {
      BranchId id = BranchId.SENTINEL;
      if (artifact != null) {
         id = artifact.getBranch();
      } else if (savedBranchId.isValid()) {
         id = savedBranchId;
      }
      return id;
   }

   private Long getArtUuid() {
      Long uuid = 0L;
      if (artifact != null) {
         uuid = artifact.getUuid();
      } else if (savedArtUuid != null) {
         uuid = savedArtUuid;
      }
      return uuid;
   }

   @Override
   public boolean equals(Object obj) {

      if (obj instanceof ArtifactEditorInput) {
         ArtifactEditorInput other = (ArtifactEditorInput) obj;
         if (!getArtUuid().equals(other.getArtUuid())) {
            return false;
         }
         return getBranchId().equals(other.getBranchId());
      }
      return false;
   }

   public BranchId getSavedBranchId() {
      return savedBranchId;
   }

   public Long getSavedArtUuid() {
      return savedArtUuid;
   }

   public String getSavedTitle() {
      return savedTitle;
   }
}
