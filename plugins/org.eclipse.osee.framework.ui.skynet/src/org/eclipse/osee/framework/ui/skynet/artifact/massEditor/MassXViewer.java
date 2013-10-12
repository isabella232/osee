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
package org.eclipse.osee.framework.ui.skynet.artifact.massEditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactData;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.artifact.ArtifactPromptChange;
import org.eclipse.osee.framework.ui.skynet.artifact.ArtifactTransfer;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Donald G. Dunne
 */
public class MassXViewer extends XViewer implements IMassViewerEventHandler {

   private String title;
   private final Set<Artifact> artifacts = new HashSet<Artifact>(50);
   private final IDirtiableEditor editor;
   private final List<String> EXTRA_COLUMNS = Arrays.asList(new String[] {"GUID", "Artifact Type"});
   private final Composite parent;

   public MassXViewer(Composite parent, int style, MassArtifactEditor editor) {
      super(parent, style, ((MassArtifactEditorInput) editor.getEditorInput()).getXViewerFactory());
      this.parent = parent;
      this.editor = editor;
      MassXViewerEventManager.add(this);
      final MassXViewer fMassXViewer = this;
      parent.addDisposeListener(new DisposeListener() {

         @Override
         public void widgetDisposed(DisposeEvent e) {
            MassXViewerEventManager.remove(fMassXViewer);
         }
      });
   }

   @Override
   public void handleColumnMultiEdit(TreeColumn treeColumn, Collection<TreeItem> treeItems) {
      String colName = treeColumn.getText();
      Set<Artifact> artifacts = new HashSet<Artifact>();
      for (TreeItem item : treeItems) {
         artifacts.add((Artifact) item.getData());
      }
      try {
         IAttributeType attributeType = AttributeTypeManager.getType(colName);
         if (ArtifactPromptChange.promptChangeAttribute(attributeType, artifacts, false)) {
            refresh();
            editor.onDirtied();
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public boolean isColumnMultiEditable(TreeColumn treeColumn, Collection<TreeItem> treeItems) {
      if (EXTRA_COLUMNS.contains(treeColumn.getText())) {
         return false;
      }
      return super.isColumnMultiEditable(treeColumn, treeItems);
   }

   @Override
   public boolean isColumnMultiEditEnabled() {
      return true;
   }

   @Override
   public boolean handleAltLeftClick(TreeColumn treeColumn, TreeItem treeItem) {
      super.handleAltLeftClick(treeColumn, treeItem);
      String colName = treeColumn.getText();
      if (EXTRA_COLUMNS.contains(colName)) {
         AWorkbench.popup("ERROR", "Can't change the field " + colName);
      }
      try {
         IAttributeType attributeType = AttributeTypeManager.getType(colName);
         Artifact useArt = (Artifact) treeItem.getData();
         boolean persist = false;
         if (ArtifactPromptChange.promptChangeAttribute(attributeType, Arrays.asList(useArt), persist)) {
            refresh();
            editor.onDirtied();
            return true;
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return false;
   }

   @Override
   protected void createSupportWidgets(Composite parent) {
      super.createSupportWidgets(parent);
      setupDragAndDropSupport();
   }

   private void setupDragAndDropSupport() {

      // Do not allow drag if artifacts in this table are not on same branch as default branch
      DragSource source = new DragSource(getTree(), DND.DROP_COPY);
      source.setTransfer(new Transfer[] {ArtifactTransfer.getInstance()});
      source.addDragListener(new DragSourceListener() {
         @Override
         public void dragFinished(DragSourceEvent event) {
            refresh();
         }

         @Override
         public void dragSetData(DragSourceEvent event) {
            Collection<Artifact> arts = getSelectedArtifacts();
            if (arts.size() > 0) {
               event.data = new ArtifactData(arts.toArray(new Artifact[arts.size()]), "", MassArtifactEditor.EDITOR_ID);

            }
         }

         @Override
         public void dragStart(DragSourceEvent event) {
            event.doit = false;
            Collection<Artifact> arts = getSelectedArtifacts();
            if (arts.size() > 0) {
               event.doit = true;
            }
         }
      });

      // Do not allow drop if default branch is not same as artifacts that reside in this table
      DropTarget target = new DropTarget(getTree(), DND.DROP_COPY);
      target.setTransfer(new Transfer[] {
         FileTransfer.getInstance(),
         TextTransfer.getInstance(),
         ArtifactTransfer.getInstance()});
      target.addDropListener(new DropTargetAdapter() {

         @Override
         public void drop(DropTargetEvent event) {
            performDrop(event);
         }

         @Override
         public void dragOver(DropTargetEvent event) {
            // if ((event.data instanceof ArtifactData) && ((ArtifactData)
            // event.data).getArtifacts().length > 0)
            event.detail = DND.DROP_COPY;
         }

         @Override
         public void dropAccept(DropTargetEvent event) {
            // do nothing
         }
      });
   }

   private void performDrop(DropTargetEvent e) {
      try {
         if (e.data instanceof ArtifactData) {
            Artifact[] artsToAdd = ((ArtifactData) e.data).getArtifacts();
            add(Arrays.asList(artsToAdd));
         }
         refresh();
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public void handleDoubleClick() {
      if (getSelectedArtifacts().isEmpty()) {
         return;
      }
      RendererManager.openInJob(getSelectedArtifacts(), PresentationType.DEFAULT_OPEN);
   }

   public ArrayList<Artifact> getLoadedArtifacts() {
      ArrayList<Artifact> arts = new ArrayList<Artifact>();
      TreeItem items[] = getTree().getItems();
      if (items.length > 0) {
         for (TreeItem item : items) {
            arts.add((Artifact) item.getData());
         }
      }
      return arts;
   }

   @Override
   public void dispose() {
      MassXViewerEventManager.remove(this);
      // Tell the label provider to release its resources
      getLabelProvider().dispose();
   }

   public ArrayList<Artifact> getSelectedArtifacts() {
      ArrayList<Artifact> arts = new ArrayList<Artifact>();
      TreeItem items[] = getTree().getSelection();
      if (items.length > 0) {
         for (TreeItem item : items) {
            arts.add((Artifact) item.getData());
         }
      }
      return arts;
   }

   public String getTitle() {
      return title;
   }

   public void add(Collection<? extends Artifact> artifacts) {
      if (xViewerFactory instanceof MassXViewerFactory) {
         ((MassXViewerFactory) xViewerFactory).registerAllAttributeColumnsForArtifacts(artifacts, true, true);
      }
      for (Artifact art : artifacts) {
         this.artifacts.add(art);
      }
      ((MassContentProvider) getContentProvider()).add(artifacts);
   }

   public void set(Collection<? extends Artifact> artifacts) {
      if (xViewerFactory instanceof MassXViewerFactory) {
         ((MassXViewerFactory) xViewerFactory).registerAllAttributeColumnsForArtifacts(artifacts, true, true);
      }
      this.artifacts.clear();
      for (Artifact art : artifacts) {
         this.artifacts.add(art);
      }
      ((MassContentProvider) getContentProvider()).set(artifacts);
   }

   public Collection<? extends Artifact> getArtifacts() {
      return artifacts;
   }

   @Override
   public MassXViewer getMassXViewer() {
      return this;
   }

   @Override
   public boolean isDisposed() {
      return parent == null || parent.isDisposed();
   }

}
