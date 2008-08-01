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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactData;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTransfer;
import org.eclipse.osee.framework.skynet.core.artifact.BranchPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.IATSArtifact;
import org.eclipse.osee.framework.skynet.core.event.LocalTransactionEvent;
import org.eclipse.osee.framework.skynet.core.event.RemoteTransactionEvent;
import org.eclipse.osee.framework.skynet.core.event.SkynetEventManager;
import org.eclipse.osee.framework.skynet.core.event.TransactionEvent;
import org.eclipse.osee.framework.skynet.core.event.TransactionEvent.TransactionChangeType;
import org.eclipse.osee.framework.ui.plugin.event.Event;
import org.eclipse.osee.framework.ui.plugin.event.IEventReceiver;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.artifact.ArtifactPromptChange;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.ArtifactEditor;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.customize.CustomizeData;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.skynet.SkynetXViewerFactory;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.skynet.column.XViewerArtifactNameColumn;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.skynet.column.XViewerGuidColumn;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.skynet.column.XViewerHridColumn;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Donald G. Dunne
 */
public class MassXViewer extends XViewer implements IEventReceiver {

   private String title;
   private Collection<? extends Artifact> artifacts;
   private final IDirtiableEditor editor;
   private final List<String> EXTRA_COLUMNS = Arrays.asList(new String[] {"GUID", "HRID", "Artifact Type"});

   /**
    * @param parent
    * @param style
    */
   public MassXViewer(Composite parent, int style, IDirtiableEditor editor) {
      super(parent, style, new MassXViewerFactory());
      this.editor = editor;
      this.addDoubleClickListener(new IDoubleClickListener() {
         public void doubleClick(org.eclipse.jface.viewers.DoubleClickEvent event) {
            handleDoubleClick();
         };
      });
      SkynetEventManager.getInstance().register(RemoteTransactionEvent.class, this);
      SkynetEventManager.getInstance().register(LocalTransactionEvent.class, this);
   }

   @Override
   public void handleColumnMultiEdit(TreeColumn treeColumn, Collection<TreeItem> treeItems) {
      String colName = treeColumn.getText();
      Set<Artifact> useArts = new HashSet<Artifact>();
      for (TreeItem item : treeItems) {
         useArts.add((Artifact) item.getData());
      }
      try {
         if (ArtifactPromptChange.promptChangeAttribute(colName, colName, useArts, false)) {
            refresh();
            editor.onDirtied();
         }
      } catch (SQLException ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, true);
      }
   }

   @Override
   public boolean isColumnMultiEditable(TreeColumn treeColumn, Collection<TreeItem> treeItems) {
      if (EXTRA_COLUMNS.contains(treeColumn.getText())) return false;
      return super.isColumnMultiEditable(treeColumn, treeItems);
   }

   @Override
   public boolean isColumnMultiEditEnabled() {
      return true;
   }

   @Override
   public boolean handleAltLeftClick(TreeColumn treeColumn, TreeItem treeItem) {
      return handleAltLeftClick(treeColumn, treeItem, false);
   }

   public boolean handleAltLeftClick(TreeColumn treeColumn, TreeItem treeItem, boolean persist) {
      try {
         super.handleAltLeftClick(treeColumn, treeItem);
         // System.out.println("Column " + treeColumn.getText() + " item " +
         // treeItem);
         String colName = treeColumn.getText();
         if (EXTRA_COLUMNS.contains(colName)) {
            AWorkbench.popup("ERROR", "Can't change the field " + colName);
         }
         Artifact useArt = ((Artifact) treeItem.getData());
         if (ArtifactPromptChange.promptChangeAttribute(colName, colName, Arrays.asList(useArt), persist)) {
            refresh();
            editor.onDirtied();
            return true;
         }
      } catch (SQLException ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, true);
      }
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewer#createSupportWidgets(org.eclipse.swt.widgets.Composite)
    */
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

         public void dragFinished(DragSourceEvent event) {
            refresh();
         }

         public void dragSetData(DragSourceEvent event) {
            Collection<Artifact> arts = getSelectedArtifacts();
            if (arts.size() > 0) {
               Artifact artifact = arts.iterator().next();
               if (artifact.getBranch() == BranchPersistenceManager.getDefaultBranch()) event.data =
                     new ArtifactData(arts.toArray(new Artifact[arts.size()]), "", MassArtifactEditor.EDITOR_ID);
            }
         }

         public void dragStart(DragSourceEvent event) {
            event.doit = false;
            Collection<Artifact> arts = getSelectedArtifacts();
            if (arts.size() > 0) {
               Artifact artifact = arts.iterator().next();
               if (artifact.getBranch() == BranchPersistenceManager.getDefaultBranch()) event.doit = true;
            }
         }
      });

      // Do not allow drop if default branch is not same as artifacts that reside in this table
      DropTarget target = new DropTarget(getTree(), DND.DROP_COPY);
      target.setTransfer(new Transfer[] {FileTransfer.getInstance(), TextTransfer.getInstance(),
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
         }
      });
   }

   private void performDrop(DropTargetEvent e) {
      try {
         if (e.data instanceof ArtifactData) {
            Artifact[] artsToAdd = ((ArtifactData) e.data).getArtifacts();
            Set<Artifact> arts = new HashSet<Artifact>();
            arts.addAll(artifacts);
            for (Artifact art : artsToAdd)
               arts.add(art);
            set(arts);
         }
         refresh();
      } catch (Exception ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, true);
      }
   }

   public void handleDoubleClick() {
      if (getSelectedArtifacts().size() == 0) return;
      Artifact art = getSelectedArtifacts().iterator().next();
      ArtifactEditor.editArtifact(art);
   }

   public ArrayList<Artifact> getLoadedArtifacts() {
      ArrayList<Artifact> arts = new ArrayList<Artifact>();
      TreeItem items[] = getTree().getItems();
      if (items.length > 0) for (TreeItem item : items)
         arts.add((Artifact) item.getData());
      return arts;
   }

   /**
    * Release resources
    */
   @Override
   public void dispose() {
      SkynetEventManager.getInstance().unRegisterAll(this);
      SkynetEventManager.getInstance().unRegisterAll(this);
      // Tell the label provider to release its ressources
      getLabelProvider().dispose();
   }

   public ArrayList<Artifact> getSelectedArtifacts() {
      ArrayList<Artifact> arts = new ArrayList<Artifact>();
      TreeItem items[] = getTree().getSelection();
      if (items.length > 0) for (TreeItem item : items)
         arts.add((Artifact) item.getData());
      return arts;
   }

   /**
    * @return Returns the title.
    */
   public String getTitle() {
      return title;
   }

   public void add(Collection<Artifact> artifacts) throws SQLException {
      resetColumns(artifacts);
      ((MassContentProvider) getContentProvider()).add(artifacts);
   }

   public void set(Collection<? extends Artifact> artifacts) throws SQLException {
      resetColumns(artifacts);
      this.artifacts = artifacts;
      ((MassContentProvider) getContentProvider()).set(artifacts);
   }

   public void resetColumns(Collection<? extends Artifact> artifacts) throws SQLException {
      CustomizeData custData = new CustomizeData();

      List<XViewerColumn> columns =
            ((MassArtifactEditorInput) ((MassArtifactEditor) editor).getEditorInput()).getColumns();
      if (columns == null) {
         columns = getDefaultArtifactColumns(this, artifacts);
         custData.getSortingData().setSortingNames(Arrays.asList(nameCol.getId()));
      }
      custData.getColumnData().setColumns(columns);
      ((MassXViewerFactory) getXViewerFactory()).setDefaultCustData(custData);
      ((MassXViewerFactory) getXViewerFactory()).setColumns(columns);
      getCustomizeMgr().loadCustomization(custData);
   }

   private static final XViewerArtifactNameColumn nameCol = new XViewerArtifactNameColumn("Name");

   public static List<XViewerColumn> getDefaultArtifactColumns(XViewer xViewer, Collection<? extends Artifact> artifacts) throws SQLException {
      List<XViewerColumn> columns = SkynetXViewerFactory.getAllAttributeColumnsForArtifacts(artifacts);
      columns.add(new XViewerHridColumn("ID"));
      columns.add(new XViewerGuidColumn("GUID"));

      return columns;
   }

   /**
    * @return the artifacts
    */
   public Collection<? extends Artifact> getArtifacts() {
      return artifacts;
   }

   public void onEvent(final Event event) {
      if (getTree() == null || getTree().isDisposed()) {
         dispose();
         return;
      }
      if (event instanceof TransactionEvent) {
         TransactionEvent transEvent = (TransactionEvent) event;
         Set<Integer> artIds = transEvent.getArtIds(TransactionChangeType.Modified);
         Set<Artifact> modArts = new HashSet<Artifact>(20);
         for (int artId : artIds) {
            Artifact art = ArtifactCache.getActive(artId, ((MassArtifactEditor) editor).getBranch());
            if (art != null && (art instanceof IATSArtifact)) {
               modArts.add(art);
               try {
                  if (art instanceof IATSArtifact) {
                     Artifact parentArt = ((IATSArtifact) art).getParentAtsArtifact();
                     if (parentArt != null) {
                        modArts.add(parentArt);
                     }
                  }
               } catch (Exception ex) {
                  // do nothing
               }
            }
         }
         if (modArts.size() > 0) update(modArts.toArray(), null);

         artIds = transEvent.getArtIds(TransactionChangeType.Deleted);
         artIds.addAll(transEvent.getArtIds(TransactionChangeType.Purged));
         modArts.clear();
         for (int artId : artIds) {
            Artifact art = ArtifactCache.getActive(artId, ((MassArtifactEditor) editor).getBranch());
            if (art != null && (art instanceof IATSArtifact)) {
               modArts.add(art);
            }
         }
         if (modArts.size() > 0) remove(modArts.toArray());

         modArts.clear();
         for (int artId : artIds) {
            Artifact art = ArtifactCache.getActive(artId, ((MassArtifactEditor) editor).getBranch());
            if (art != null && (art instanceof IATSArtifact)) {
               modArts.add(art);
               try {
                  if (art instanceof IATSArtifact) {
                     Artifact parentArt = ((IATSArtifact) art).getParentAtsArtifact();
                     if (parentArt != null) {
                        modArts.add(parentArt);
                     }
                  }
               } catch (Exception ex) {
                  // do nothing
               }
            }
         }
         if (modArts.size() > 0) {
            for (Artifact art : modArts) {
               refresh(art);
            }
         }
      } else
         OSEELog.logSevere(SkynetGuiPlugin.class, "Unexpected event => " + event, true);
   }

   public boolean runOnEventInDisplayThread() {
      return true;
   }

}
