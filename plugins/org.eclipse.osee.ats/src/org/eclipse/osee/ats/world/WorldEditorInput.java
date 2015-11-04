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
package org.eclipse.osee.ats.world;

import java.util.Collection;
import java.util.logging.Level;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.search.AtsSearchWorkflowSearchItem;
import org.eclipse.osee.ats.world.search.WorldSearchItem;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * @author Donald G. Dunne
 */
public class WorldEditorInput implements IEditorInput, IPersistableElement {

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
   @SuppressWarnings({"rawtypes", "unchecked"})
   public Object getAdapter(Class adapter) {
      return null;
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

   public Collection<Integer> getGuids() {
      if (editor.isReloadTabShown() && (iWorldEditorProvider instanceof WorldEditorReloadProvider)) {
         WorldEditorReloadProvider provider = (WorldEditorReloadProvider) iWorldEditorProvider;
         return provider.getValidArtUuids();
      } else {
         return Artifacts.toIds(editor.getLoadedArtifacts());
      }
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

   public long getBranchUuid() {
      long branchUuid = 0;
      if (editor.isReloadTabShown() && (iWorldEditorProvider instanceof WorldEditorReloadProvider)) {
         WorldEditorReloadProvider provider = (WorldEditorReloadProvider) iWorldEditorProvider;
         branchUuid = provider.getBranchUuid();
      } else {
         if (!editor.getLoadedArtifacts().isEmpty()) {
            branchUuid = editor.getLoadedArtifacts().iterator().next().getBranchId();
         }
      }
      return branchUuid;
   }

   public Long getAtsSearchUuid() {
      if (iWorldEditorProvider instanceof WorldEditorParameterSearchItemProvider) {
         WorldSearchItem worldSearchItem =
            ((WorldEditorParameterSearchItemProvider) iWorldEditorProvider).getWorldSearchItem();
         if (worldSearchItem instanceof AtsSearchWorkflowSearchItem) {
            return ((AtsSearchWorkflowSearchItem) worldSearchItem).getSearchUuid();
         }
      }
      return 0L;
   }
}
