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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.nebula.widgets.xviewer.customize.CustomizeData;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.world.search.WorldSearchItem.SearchType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.swt.CursorManager;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public abstract class WorldEditorProvider implements IWorldEditorProvider {

   protected TableLoadOption[] tableLoadOptions;
   protected CustomizeData customizeData;
   private WorldEditor worldEditor;
   private boolean loading;

   public WorldEditorProvider(CustomizeData customizeData, TableLoadOption[] tableLoadOptions) {
      this.customizeData = customizeData;
      this.tableLoadOptions = tableLoadOptions.clone();
   }

   @Override
   public String getSelectedName(SearchType searchType) throws OseeCoreException {
      return getName();
   }

   @Override
   public IAtsVersion getTargetedVersionArtifact() throws OseeCoreException {
      return null;
   }

   @Override
   public TableLoadOption[] getTableLoadOptions() {
      return tableLoadOptions;
   }

   public CustomizeData getCustomizeData() {
      return customizeData;
   }

   @Override
   public void setTableLoadOptions(TableLoadOption... tableLoadOptions) {
      this.tableLoadOptions = tableLoadOptions;
   }

   @Override
   public void setCustomizeData(CustomizeData customizeData) {
      this.customizeData = customizeData;
   }

   @Override
   public void run(WorldEditor worldEditor, SearchType searchType, boolean forcePend) throws OseeCoreException {
      this.worldEditor = worldEditor;

      boolean pend = Collections.getAggregate(tableLoadOptions).contains(TableLoadOption.ForcePend) || forcePend;
      worldEditor.getWorldComposite().getXViewer().setForcePend(pend);

      if (loading) {
         AWorkbench.popup("Already Loading, Please Wait");
         return;
      }

      LoadTableJob job = null;
      job = new LoadTableJob(worldEditor, this, searchType, tableLoadOptions, pend);
      job.setUser(false);
      job.setPriority(Job.LONG);
      job.schedule();
      if (pend) {
         try {
            worldEditor.getWorldComposite().getXViewer().setForcePend(true);
            job.join();
         } catch (InterruptedException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
   }

   private class LoadTableJob extends Job {

      private final boolean cancel = false;
      private final SearchType searchType;
      private final WorldEditor worldEditor;
      private final TableLoadOption[] tableLoadOptions;
      private final boolean forcePend;
      private final WorldEditorProvider worldEditorProvider;

      public LoadTableJob(WorldEditor worldEditor, WorldEditorProvider worldEditorProvider, SearchType searchType, TableLoadOption[] tableLoadOptions, boolean forcePend) throws OseeCoreException {
         super("Loading \"" + worldEditorProvider.getSelectedName(searchType) + "\"...");
         this.worldEditor = worldEditor;
         this.worldEditorProvider = worldEditorProvider;
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
            selectedName = worldEditorProvider.getSelectedName(searchType);
            worldEditor.setEditorTitle(selectedName != null ? selectedName : worldEditorProvider.getName());
            worldEditor.setTableTitle("Loading \"" + (selectedName != null ? selectedName : "") + "\"...", false);
            worldEditor.getWorldComposite().getXViewer().clear(forcePend);

            Collection<Artifact> artifacts = performSearch(searchType);

            if (cancel) {
               monitor.done();
               worldEditor.setTableTitle("CANCELLED - " + selectedName, false);
               return Status.CANCEL_STATUS;
            }
            if (artifacts.isEmpty()) {
               monitor.done();
               worldEditor.setTableTitle("No Results Found - " + selectedName, true);
               return Status.OK_STATUS;
            }

            if (searchType == SearchType.ReSearch) {
               deCacheAndReload(worldEditor, selectedName, forcePend, artifacts);
            } else {
               worldEditor.getWorldComposite().load(selectedName != null ? selectedName : "", artifacts, customizeData,
                  tableLoadOptions);
            }
         } catch (final Exception ex) {
            String str = "Exception occurred.";
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

   public void deCacheAndReload(WorldEditor worldEditor, String name, boolean forcePend, Collection<Artifact> artifacts) {

      if (forcePend) {
         deCacheAndReload(worldEditor, name, artifacts);
      } else {
         Job job = new Job("Re-Loading " + name) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
               deCacheAndReload(worldEditor, name, artifacts);
               return Status.OK_STATUS;
            }

         };
         job.setSystem(false);
         job.schedule();
      }
   }

   private void deCacheAndReload(WorldEditor worldEditor, String name, Collection<Artifact> artifacts) {
      for (Artifact art : artifacts) {
         ArtifactCache.deCache(art);
      }
      worldEditor.getWorldComposite().load(name != null ? name : "", artifacts, customizeData);

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

}
