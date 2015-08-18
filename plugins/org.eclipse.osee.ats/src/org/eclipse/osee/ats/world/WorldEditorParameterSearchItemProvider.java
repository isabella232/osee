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
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.nebula.widgets.xviewer.customize.CustomizeData;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.world.search.WorldSearchItem;
import org.eclipse.osee.ats.world.search.WorldSearchItem.SearchType;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.skynet.widgets.util.IDynamicWidgetLayoutListener;
import org.eclipse.osee.framework.ui.skynet.widgets.util.XWidgetRendererItem;
import org.eclipse.osee.framework.ui.swt.CursorManager;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class WorldEditorParameterSearchItemProvider extends WorldEditorProvider implements IWorldEditorParameterProvider {

   private final WorldEditorParameterSearchItem worldParameterSearchItem;
   public final static String ENTER_OPTIONS_AND_SELECT_SEARCH = "Enter options and select \"Search\"";
   private boolean firstTime = true;
   private boolean loading = false;
   private WorldEditor worldEditor;

   public WorldEditorParameterSearchItemProvider(WorldEditorParameterSearchItem worldParameterSearchItem) {
      this(worldParameterSearchItem, null, TableLoadOption.None);
   }

   public WorldEditorParameterSearchItemProvider(WorldEditorParameterSearchItem worldParameterSearchItem, CustomizeData customizeData, TableLoadOption... tableLoadOptions) {
      super(customizeData, tableLoadOptions);
      this.worldParameterSearchItem = worldParameterSearchItem;
   }

   @Override
   public IWorldEditorProvider copyProvider() throws OseeCoreException {
      return new WorldEditorParameterSearchItemProvider(
         (WorldEditorParameterSearchItem) worldParameterSearchItem.copy(), customizeData, tableLoadOptions);
   }

   public WorldSearchItem getWorldSearchItem() {
      return worldParameterSearchItem;
   }

   @Override
   public String getName() throws OseeCoreException {
      return worldParameterSearchItem.getName();
   }

   @Override
   public void run(WorldEditor worldEditor, SearchType searchType, boolean forcePend) throws OseeCoreException {
      this.worldEditor = worldEditor;
      worldEditor.getWorldComposite().getXViewer().setForcePend(forcePend);
      if (firstTime) {
         firstTime = false;
         worldEditor.setTableTitle(ENTER_OPTIONS_AND_SELECT_SEARCH, false);
         return;
      }
      if (worldParameterSearchItem.isCancelled()) {
         return;
      }

      Result result = worldParameterSearchItem.isParameterSelectionValid();
      if (result.isFalse()) {
         AWorkbench.popup(result);
         return;
      }

      if (loading) {
         AWorkbench.popup("Already Loading, Please Wait");
         return;
      }
      LoadTableJob job = null;
      job = new LoadTableJob(worldEditor, worldParameterSearchItem, searchType, tableLoadOptions, forcePend);
      job.setUser(false);
      job.setPriority(Job.LONG);
      job.schedule();
      if (forcePend) {
         try {
            worldEditor.getWorldComposite().getXViewer().setForcePend(true);
            job.join();
         } catch (InterruptedException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
   }
   private class LoadTableJob extends Job {

      private final WorldEditorParameterSearchItem worldParameterSearchItem;
      private boolean cancel = false;
      private final SearchType searchType;
      private final WorldEditor worldEditor;
      private final TableLoadOption[] tableLoadOptions;
      private final boolean forcePend;

      public LoadTableJob(WorldEditor worldEditor, WorldEditorParameterSearchItem worldParameterSearchItem, SearchType searchType, TableLoadOption[] tableLoadOptions, boolean forcePend) throws OseeCoreException {
         super("Loading \"" + worldParameterSearchItem.getSelectedName(searchType) + "\"...");
         this.worldEditor = worldEditor;
         this.worldParameterSearchItem = worldParameterSearchItem;
         this.searchType = searchType;
         this.tableLoadOptions = tableLoadOptions.clone();
         this.forcePend = forcePend;
      }

      @Override
      protected IStatus run(IProgressMonitor monitor) {
         if (loading) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Already Loading, Please Wait", null);
         }
         String selectedName = "";
         try {
            setLoading(true);
            selectedName = worldParameterSearchItem.getSelectedName(searchType);
            worldEditor.setEditorTitle(selectedName != null ? selectedName : worldParameterSearchItem.getName());
            worldEditor.setTableTitle("Loading \"" + (selectedName != null ? selectedName : "") + "\"...", false);
            cancel = false;
            worldParameterSearchItem.setCancelled(cancel);
            worldEditor.getWorldComposite().getXViewer().clear(forcePend);

            Callable<Collection<? extends Artifact>> worker = worldParameterSearchItem.createSearch();
            Collection<? extends Artifact> artifacts = worker.call();

            if (artifacts.isEmpty()) {
               if (worldParameterSearchItem.isCancelled()) {
                  monitor.done();
                  worldEditor.setTableTitle("CANCELLED - " + selectedName, false);
                  return Status.CANCEL_STATUS;
               } else {
                  monitor.done();
                  worldEditor.setTableTitle("No Results Found - " + selectedName, true);
                  return Status.OK_STATUS;
               }
            }
            worldEditor.getWorldComposite().load(selectedName, artifacts, customizeData, tableLoadOptions);
         } catch (final Exception ex) {
            String str = "Exception occurred. Network may be down.";
            if (Strings.isValid(ex.getLocalizedMessage())) {
               str += " => " + ex.getLocalizedMessage();
            }
            worldEditor.getWorldComposite().setTableTitle("Searching Error - " + selectedName, false);
            OseeLog.log(Activator.class, Level.SEVERE, ex);
            monitor.done();
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, str, null);
         } finally {
            setLoading(false);
         }
         monitor.done();
         return Status.OK_STATUS;
      }
   }

   @Override
   public String getSelectedName(SearchType searchType) throws OseeCoreException {
      return Strings.truncate(worldParameterSearchItem.getSelectedName(searchType), WorldEditor.TITLE_MAX_LENGTH, true);
   }

   @Override
   public String getParameterXWidgetXml() throws OseeCoreException {
      return worldParameterSearchItem.getParameterXWidgetXml();
   }

   @Override
   public IDynamicWidgetLayoutListener getDynamicWidgetLayoutListener() {
      return worldParameterSearchItem;
   }

   @Override
   public String[] getWidgetOptions(XWidgetRendererItem widgetData) {
      return null;
   }

   public boolean isLoading() {
      return loading;
   }

   public void setLoading(final boolean loading) {
      this.loading = loading;
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (Widgets.isAccessible(worldEditor.getWorldComposite())) {
               if (loading) {
                  worldEditor.getWorldComposite().setCursor(CursorManager.getCursor(SWT.CURSOR_WAIT));
               } else {
                  worldEditor.getWorldComposite().setCursor(null);
               }
            }
         }
      });

   }

   @Override
   public void handleSaveButtonPressed() {
      if (isSaveButtonAvailable()) {
         worldParameterSearchItem.handleSaveButtonPressed();
      }
   }

   @Override
   public boolean isSaveButtonAvailable() {
      return worldParameterSearchItem.isSaveButtonAvailable();
   }

}
