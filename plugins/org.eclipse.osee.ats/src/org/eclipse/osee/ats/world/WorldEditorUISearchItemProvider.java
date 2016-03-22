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
import org.eclipse.nebula.widgets.xviewer.core.model.CustomizeData;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.world.search.NextVersionSearchItem;
import org.eclipse.osee.ats.world.search.VersionTargetedForTeamSearchItem;
import org.eclipse.osee.ats.world.search.WorldSearchItem;
import org.eclipse.osee.ats.world.search.WorldSearchItem.SearchType;
import org.eclipse.osee.ats.world.search.WorldUISearchItem;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;
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

      boolean pend = options.contains(TableLoadOption.ForcePend) || forcePend;
      super.run(worldEditor, searchType, pend);
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

   @Override
   public Collection<Artifact> performSearch(SearchType searchType) {
      return worldUISearchItem.performSearchGetResults(false, searchType);
   }

}
