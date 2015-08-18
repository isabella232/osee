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
import org.eclipse.osee.ats.world.search.NextVersionSearchItem;
import org.eclipse.osee.ats.world.search.VersionTargetedForTeamSearchItem;
import org.eclipse.osee.ats.world.search.WorldSearchItem;
import org.eclipse.osee.ats.world.search.WorldSearchItem.SearchType;
import org.eclipse.osee.ats.world.search.WorldUISearchItem;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;

/**
 * @author Donald G. Dunne
 */
public class WorldEditorUISearchItemProvider extends WorldEditorProvider {

   private final WorldUISearchItem worldUISearchItem;

   public WorldEditorUISearchItemProvider(WorldUISearchItem worldUISearchItem) {
      this(worldUISearchItem, null, TableLoadOption.None);
   }

   public WorldEditorUISearchItemProvider(WorldUISearchItem worldUISearchItem, CustomizeData customizeData, TableLoadOption... tableLoadOptions) {
      super(customizeData, tableLoadOptions);
      this.worldUISearchItem = worldUISearchItem;
   }

   @Override
   public IWorldEditorProvider copyProvider() throws OseeCoreException {
      return new WorldEditorUISearchItemProvider((WorldUISearchItem) worldUISearchItem.copy(), customizeData,
         tableLoadOptions);
   }

   /**
    * @return the worldSearchItem
    */
   public WorldSearchItem getWorldSearchItem() {
      return worldUISearchItem;
   }

   @Override
   public String getName() throws OseeCoreException {
      return worldUISearchItem.getName();
   }

   @Override
   public String getSelectedName(SearchType searchType) throws OseeCoreException {
      return Strings.truncate(worldUISearchItem.getSelectedName(searchType), WorldEditor.TITLE_MAX_LENGTH, true);
   }

   @Override
   public void run(WorldEditor worldEditor, SearchType searchType, boolean forcePend) throws OseeCoreException {

      Collection<TableLoadOption> options = Collections.getAggregate(tableLoadOptions);
      if (!options.contains(TableLoadOption.NoUI) && searchType == SearchType.Search) {
         worldUISearchItem.performUI(searchType);
      }
      if (worldUISearchItem.isCancelled()) {
         worldEditor.close(false);
         return;
      }

      LoadTableJob job = null;
      job = new LoadTableJob(worldEditor, worldUISearchItem, searchType, forcePend);
      job.setUser(false);
      job.setPriority(Job.LONG);
      job.schedule();
      if (options.contains(TableLoadOption.ForcePend)) {
         try {
            worldEditor.getWorldComposite().getXViewer().setForcePend(true);
            job.join();
         } catch (InterruptedException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
   }

   private class LoadTableJob extends Job {

      private final WorldUISearchItem worldUISearchItem;
      private boolean cancel = false;
      private final SearchType searchType;
      private final WorldEditor worldEditor;
      private final boolean forcePend;

      public LoadTableJob(WorldEditor worldEditor, WorldUISearchItem worldUISearchItem, SearchType searchType, boolean forcePend) throws OseeCoreException {
         super("Loading \"" + worldUISearchItem.getSelectedName(searchType) + "\"...");
         this.worldEditor = worldEditor;
         this.worldUISearchItem = worldUISearchItem;
         this.searchType = searchType;
         this.forcePend = forcePend;

      }

      @Override
      protected IStatus run(IProgressMonitor monitor) {
         String selectedName = "";
         try {
            selectedName = worldUISearchItem.getSelectedName(searchType);
            worldEditor.setEditorTitle(selectedName != null ? selectedName : worldUISearchItem.getName());
            worldEditor.setTableTitle("Loading \"" + (selectedName != null ? selectedName : "") + "\"...", false);
            cancel = false;
            worldUISearchItem.setCancelled(cancel);
            final Collection<Artifact> artifacts;
            worldEditor.getWorldComposite().getXViewer().clear(forcePend);
            artifacts = worldUISearchItem.performSearchGetResults(false, searchType);
            if (artifacts.isEmpty()) {
               if (worldUISearchItem.isCancelled()) {
                  worldEditor.setTableTitle("CANCELLED - " + selectedName, false);
                  return Status.CANCEL_STATUS;
               } else {
                  worldEditor.setTableTitle("No Results Found - " + selectedName, true);
                  return Status.OK_STATUS;
               }
            }
            worldEditor.getWorldComposite().load((selectedName != null ? selectedName : ""), artifacts, customizeData);
         } catch (final Exception ex) {
            worldEditor.getWorldComposite().setTableTitle("Searching Error - " + selectedName, false);
            OseeLog.log(Activator.class, Level.SEVERE, ex);
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, ex.getLocalizedMessage(), ex);
         } finally {
            monitor.done();
         }

         return Status.OK_STATUS;
      }
   }

   @Override
   public IAtsVersion getTargetedVersionArtifact() {
      if (worldUISearchItem instanceof VersionTargetedForTeamSearchItem) {
         return ((VersionTargetedForTeamSearchItem) worldUISearchItem).getSearchVersionArtifact();
      } else if (worldUISearchItem instanceof NextVersionSearchItem) {
         return ((NextVersionSearchItem) worldUISearchItem).getSelectedVersionArt();
      }
      return null;
   }

}
