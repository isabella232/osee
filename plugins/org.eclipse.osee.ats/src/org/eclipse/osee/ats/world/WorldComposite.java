/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/eplv10.html
 *
 * Contributors:
 *     Boeing  initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.nebula.widgets.xviewer.IXViewerFactory;
import org.eclipse.nebula.widgets.xviewer.customize.CustomizeData;
import org.eclipse.osee.ats.actions.OpenNewAtsWorldEditorAction.IOpenNewAtsWorldEditorHandler;
import org.eclipse.osee.ats.actions.OpenNewAtsWorldEditorSelectedAction.IOpenNewAtsWorldEditorSelectedHandler;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.actions.ISelectedAtsArtifacts;
import org.eclipse.osee.ats.core.client.config.AtsBulkLoad;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.world.search.WorldSearchItem;
import org.eclipse.osee.ats.world.search.WorldSearchItem.SearchType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.skynet.action.RefreshAction.IRefreshActionHandler;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.skynet.util.DbConnectionExceptionComposite;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Donald G. Dunne
 */
public class WorldComposite extends ScrolledComposite implements ISelectedAtsArtifacts, IWorldViewerEventHandler, IOpenNewAtsWorldEditorHandler, IOpenNewAtsWorldEditorSelectedHandler, IRefreshActionHandler {

   private final WorldXViewer worldXViewer;
   private final Set<Artifact> worldArts = new HashSet<Artifact>(200);
   private final Set<Artifact> otherArts = new HashSet<Artifact>(200);
   protected IWorldEditor iWorldEditor;
   private final String id;

   public WorldComposite(String id, IWorldEditor worldEditor, Composite parent, int style) {
      this(id, worldEditor, null, parent, style, true);
   }

   public WorldComposite(String id, final IWorldEditor worldEditor, IXViewerFactory xViewerFactory, Composite parent, int style, boolean createDragAndDrop) {
      super(parent, style);
      this.id = id;
      this.iWorldEditor = worldEditor;

      setLayout(new GridLayout(1, true));
      setLayoutData(new GridData(GridData.FILL_BOTH));

      Composite mainComp = new Composite(this, SWT.NONE);
      mainComp.setLayout(ALayout.getZeroMarginLayout());
      mainComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      if (!DbConnectionExceptionComposite.dbConnectionIsOk(this)) {
         worldXViewer = null;
         return;
      }

      worldXViewer =
         new WorldXViewer(mainComp, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION,
            xViewerFactory != null ? xViewerFactory : new WorldXViewerFactory(), null);
      worldXViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

      worldXViewer.setContentProvider(new WorldContentProvider(worldXViewer));
      worldXViewer.setLabelProvider(new WorldLabelProvider(worldXViewer));

      if (createDragAndDrop) {
         new WorldViewDragAndDrop(this, WorldEditor.EDITOR_ID);
      }

      setContent(mainComp);
      setExpandHorizontal(true);
      setExpandVertical(true);
      layout();

      WorldXViewerEventManager.add(this);
   }

   public double getManHoursPerDayPreference() throws OseeCoreException {
      if (worldArts.size() > 0) {
         Artifact artifact = worldArts.iterator().next();
         if (artifact.isOfType(AtsArtifactTypes.Action)) {
            artifact = ActionManager.getFirstTeam(artifact);
         }
         return ((AbstractWorkflowArtifact) artifact).getManHrsPerDayPreference();
      }
      return AtsUtilCore.DEFAULT_HOURS_PER_WORK_DAY;
   }

   public void setCustomizeData(CustomizeData customizeData) {
      worldXViewer.getCustomizeMgr().loadCustomization(customizeData);
   }

   public Control getControl() {
      return worldXViewer.getControl();
   }

   public void load(final String name, final Collection<? extends Artifact> arts, TableLoadOption... tableLoadOption) {
      load(name, arts, null, tableLoadOption);
   }

   public void load(final String name, final Collection<? extends Artifact> arts, final CustomizeData customizeData, TableLoadOption... tableLoadOption) {
      Displays.pendInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (Widgets.isAccessible(worldXViewer.getTree())) {
               worldArts.clear();
               otherArts.clear();
               for (Artifact art : arts) {
                  if (AtsUtil.isAtsArtifact(art)) {
                     worldArts.add(art);
                  } else {
                     otherArts.add(art);
                  }
               }
               if (customizeData != null && !worldXViewer.getCustomizeMgr().generateCustDataFromTable().equals(
                  customizeData)) {
                  setCustomizeData(customizeData);
               }
               if (arts.isEmpty()) {
                  setTableTitle("No Results Found - " + name, true);
               } else {
                  setTableTitle(name, false);
               }
               try {
                  AtsBulkLoad.bulkLoadArtifacts(worldArts);
               } catch (OseeCoreException ex) {
                  OseeLog.log(Activator.class, Level.SEVERE, ex);
               }
               worldXViewer.setInput(worldArts);
               worldXViewer.updateStatusLabel();
               if (otherArts.size() > 0 && MessageDialog.openConfirm(Displays.getActiveShell(),
                  "Open in Artifact Editor?",
                  otherArts.size() + " Non-WorldView Artifacts were returned from request.\n\nOpen in Artifact Editor?")) {
                  RendererManager.openInJob(otherArts, PresentationType.GENERALIZED_EDIT);
               }
               worldXViewer.getTree().setFocus();
            }
         }
      });
      // Need to reflow the managed page based on the results.  Don't put this in the above thread.
      iWorldEditor.reflow();
   }

   public static class FilterLabelProvider implements ILabelProvider {

      @Override
      public Image getImage(Object arg0) {
         return null;
      }

      @Override
      public String getText(Object arg0) {
         try {
            return ((WorldSearchItem) arg0).getSelectedName(SearchType.Search);
         } catch (OseeCoreException ex) {
            return ex.getLocalizedMessage();
         }
      }

      @Override
      public void addListener(ILabelProviderListener arg0) {
         // do nothing
      }

      @Override
      public void dispose() {
         // do nothing
      }

      @Override
      public boolean isLabelProperty(Object arg0, String arg1) {
         return false;
      }

      @Override
      public void removeListener(ILabelProviderListener arg0) {
         // do nothing
      }
   }

   public static class FilterContentProvider implements IStructuredContentProvider {
      @Override
      public Object[] getElements(Object arg0) {
         return ((ArrayList<?>) arg0).toArray();
      }

      @Override
      public void dispose() {
         // do nothing
      }

      @Override
      public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
         // do nothing
      }
   }

   public void setTableTitle(final String title, final boolean warning) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            iWorldEditor.setTableTitle(title, warning);
            worldXViewer.setReportingTitle(title + " - " + DateUtil.getDateNow());
         };
      });
   }

   public List<Artifact> getLoadedArtifacts() {
      return getXViewer().getLoadedArtifacts();
   }

   public void insert(Artifact toInsert, int position) {
      worldArts.add(toInsert);
      getXViewer().insert(toInsert, position);
   }

   public void disposeComposite() {
      if (worldXViewer != null && !worldXViewer.getTree().isDisposed()) {
         worldXViewer.dispose();
      }
      WorldXViewerEventManager.remove(this);
   }

   public WorldXViewer getXViewer() {
      return worldXViewer;
   }

   @Override
   public void refreshActionHandler() {
      try {
         iWorldEditor.reSearch();
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public CustomizeData getCustomizeDataCopy() {
      return worldXViewer.getCustomizeMgr().generateCustDataFromTable();
   }

   @Override
   public IWorldEditorProvider getWorldEditorProviderCopy() throws OseeCoreException {
      return iWorldEditor.getWorldEditorProvider().copyProvider();
   }

   @Override
   public List<Artifact> getSelectedArtifacts() {
      return worldXViewer.getSelectedArtifacts();
   }

   @Override
   public void removeItems(Collection<? extends Object> objects) {
      // remove from model
      worldArts.removeAll(objects);
   }

   @Override
   public WorldXViewer getWorldXViewer() {
      return worldXViewer;
   }

   @Override
   public void relationsModifed(Collection<Artifact> relModifiedArts) {
      // provided for subclass implementation
   }

   @Override
   public String toString() {
      return String.format("WorldComposite [%s][%s]", id, iWorldEditor.getCurrentTitleLabel());
   }

   @Override
   public Set<Artifact> getSelectedSMAArtifacts() {
      Set<Artifact> artifacts = new HashSet<Artifact>();
      for (Artifact art : getSelectedArtifacts()) {
         if (art instanceof AbstractWorkflowArtifact) {
            artifacts.add(art);
         }
      }
      return artifacts;
   }

   @Override
   public List<Artifact> getSelectedAtsArtifacts() {
      List<Artifact> artifacts = new ArrayList<Artifact>();
      for (Artifact art : getSelectedArtifacts()) {
         if (art.isOfType(AtsArtifactTypes.AtsArtifact)) {
            artifacts.add(art);
         }
      }
      return artifacts;
   }

   @Override
   public List<TaskArtifact> getSelectedTaskArtifacts() {
      List<TaskArtifact> tasks = new ArrayList<TaskArtifact>();
      for (Artifact art : getSelectedArtifacts()) {
         if (art instanceof TaskArtifact) {
            tasks.add((TaskArtifact) art);
         }
      }
      return tasks;
   }

}
