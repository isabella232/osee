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

package org.eclipse.osee.ats.ide.world;

import java.util.Collection;
import java.util.logging.Level;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.ats.ide.AtsImage;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.ats.ide.search.AtsSearchWorkflowSearchItem;
import org.eclipse.osee.ats.ide.world.search.WorldSearchItem;
import org.eclipse.osee.framework.core.data.Adaptable;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * @author Donald G. Dunne
 */
public class WorldEditorInput implements IEditorInput, IPersistableElement, Adaptable {

   IWorldEditorProvider iWorldEditorProvider;
   WorldEditor editor;

   @Override
   public int hashCode() {
      return iWorldEditorProvider.hashCode();
   }

   public IWorldEditorProvider getIWorldEditorProvider() {
      return iWorldEditorProvider;
   }

   public WorldEditorInput(IWorldEditorProvider iWorldEditorProvider) {
      this.iWorldEditorProvider = iWorldEditorProvider;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof WorldEditorInput)) {
         return false;
      }
      WorldEditorInput castObj = (WorldEditorInput) obj;
      return castObj.iWorldEditorProvider.equals(this.iWorldEditorProvider);
   }

   @Override
   public boolean exists() {
      return false;
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(AtsImage.GLOBE);
   }

   @Override
   public IPersistableElement getPersistable() {
      return this;
   }

   @Override
   public void saveState(IMemento memento) {
      WorldEditorInputFactory.saveState(memento, this);
   }

   @Override
   public String getFactoryId() {
      return WorldEditorInputFactory.ID;
   }

   @Override
   public String getToolTipText() {
      try {
         return iWorldEditorProvider.getName();
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return "Exception getting name: " + ex.getLocalizedMessage();
      }
   }

   @Override
   public String getName() {
      try {
         return iWorldEditorProvider.getName();
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return "Exception getting name: " + ex.getLocalizedMessage();
      }
   }

   public String getIdString() {
      Collection<? extends ArtifactId> artifactIds;
      if (editor.isReloadTabShown() && iWorldEditorProvider instanceof WorldEditorReloadProvider) {
         WorldEditorReloadProvider provider = (WorldEditorReloadProvider) iWorldEditorProvider;
         artifactIds = provider.getValidArtIds();
      } else {
         artifactIds = editor.getLoadedArtifacts();
      }
      return Collections.toString(artifactIds, ",", ArtifactId::getIdString);
   }

   public WorldEditor getEditor() {
      return editor;
   }

   public void setEditor(WorldEditor editor) {
      this.editor = editor;
   }

   public boolean isReload() {
      boolean reload = false;
      if (iWorldEditorProvider instanceof WorldEditorReloadProvider) {
         WorldEditorReloadProvider worldEditorReloadProvider = (WorldEditorReloadProvider) iWorldEditorProvider;
         reload = worldEditorReloadProvider.isReload();
      }
      return reload;
   }

   public BranchId getBranch() {
      BranchId branch = BranchId.SENTINEL;
      if (editor.isReloadTabShown() && iWorldEditorProvider instanceof WorldEditorReloadProvider) {
         WorldEditorReloadProvider provider = (WorldEditorReloadProvider) iWorldEditorProvider;
         branch = provider.getBranch();
      } else {
         if (!editor.getLoadedArtifacts().isEmpty()) {
            branch = editor.getLoadedArtifacts().iterator().next().getBranch();
         }
      }
      return branch;
   }

   public Long getAtsSearchId() {
      if (iWorldEditorProvider instanceof WorldEditorParameterSearchItemProvider) {
         WorldSearchItem worldSearchItem =
            ((WorldEditorParameterSearchItemProvider) iWorldEditorProvider).getWorldSearchItem();
         if (worldSearchItem instanceof AtsSearchWorkflowSearchItem) {
            return ((AtsSearchWorkflowSearchItem) worldSearchItem).getSearchId();
         }
      }
      return 0L;
   }

   public String getAtsSearchNamespace() {
      if (iWorldEditorProvider instanceof WorldEditorParameterSearchItemProvider) {
         WorldSearchItem worldSearchItem =
            ((WorldEditorParameterSearchItemProvider) iWorldEditorProvider).getWorldSearchItem();
         if (worldSearchItem instanceof AtsSearchWorkflowSearchItem) {
            return ((AtsSearchWorkflowSearchItem) worldSearchItem).getNamespace();
         }
      }

      return null;
   }
}
