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
package org.eclipse.osee.ats.navigate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.core.util.VisitedItemCache;
import org.eclipse.osee.ats.world.WorldEditor;
import org.eclipse.osee.ats.world.WorldEditorSimpleProvider;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;

/**
 * @author Donald G. Dunne
 */
public class VisitedItems extends XNavigateItemAction {

   private static VisitedItemCache visitedItems;

   public VisitedItems(XNavigateItem parent) {
      super(parent, "My Recently Visited", AtsImage.GLOBE);
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) throws OseeCoreException {
      List<Integer> artIds = new ArrayList<>();
      for (IAtsWorkItem workItem : getCache().getReverseVisited()) {
         artIds.add(new Long(workItem.getUuid()).intValue());
      }
      Collection<Artifact> artifacts = ArtifactQuery.getArtifactListFromIds(artIds, AtsUtilCore.getAtsBranch());
      WorldEditor.open(new WorldEditorSimpleProvider(getName(), artifacts, null, tableLoadOptions));
   }

   public static void clearVisited() {
      getCache().clearVisited();
   }

   public static void addVisited(IAtsWorkItem workItem) {
      getCache().addVisited(workItem);
   }

   public static VisitedItemCache getCache() {
      if (visitedItems == null) {
         visitedItems = new VisitedItemCache();
      }
      return visitedItems;
   }
}
