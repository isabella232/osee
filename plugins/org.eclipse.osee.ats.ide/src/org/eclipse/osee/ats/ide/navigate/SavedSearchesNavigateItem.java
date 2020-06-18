/*********************************************************************
 * Copyright (c) 2020 Boeing
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

package org.eclipse.osee.ats.ide.navigate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.query.AtsSearchData;
import org.eclipse.osee.ats.api.user.AtsUser;
import org.eclipse.osee.ats.api.util.AtsTopicEvent;
import org.eclipse.osee.ats.api.util.AtsUtil;
import org.eclipse.osee.ats.ide.AtsImage;
import org.eclipse.osee.ats.ide.internal.AtsClientService;
import org.eclipse.osee.ats.ide.search.AtsSearchWorkflowSearchItem;
import org.eclipse.osee.ats.ide.world.AtsWorldEditorItems;
import org.eclipse.osee.ats.ide.world.IAtsWorldEditorItem;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.skynet.util.FrameworkEvents;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Create Saved Searches navigate item.</br>
 * Load/Refresh on events:</br>
 * - FrameworkEvents.NAVIGATE_VIEW_LOADED</br>
 * - AtsTopicEvents.SAVED_SEARCHES_MODIFIED
 *
 * @author Donald G. Dunne
 */
public class SavedSearchesNavigateItem extends XNavigateItem implements EventHandler {

   private static long SAVED_SEARCH_ID = 824378923L;
   private static final AtomicBoolean firstTime = new AtomicBoolean(true);

   public SavedSearchesNavigateItem() {
      // for jax-rs
      super(null, null, null);
   }

   public SavedSearchesNavigateItem(XNavigateItem parent) {
      super(parent, "Saved Action Searches", AtsImage.SEARCH);
      setId(SAVED_SEARCH_ID);
   }

   protected void populateSavedSearchesItem(final SavedSearchesNavigateItem topSearchItem, final AtsUser currentUser, final AtsApi atsApi) {

      Job populateSavedSearchesJob = new Job("Populate Saved Searches") {

         @Override
         protected IStatus run(IProgressMonitor monitor) {

            try {
               // If current user and not first load, reload user to get latest
               AtsUser currUser = currentUser;
               if (!firstTime.getAndSet(
                  false) && (atsApi.getUserService().getCurrentUser().equals(currentUser) || AtsUtil.isInTest())) {
                  currUser = atsApi.getUserService().getCurrentUserNoCache();
               }

               if (topSearchItem.getChildren() != null) {
                  topSearchItem.getChildren().clear();
               }
               Set<Long> ids = new HashSet<Long>();
               for (IAtsWorldEditorItem worldEditorItem : AtsWorldEditorItems.getItems()) {
                  for (AtsSearchWorkflowSearchItem item : worldEditorItem.getSearchWorkflowSearchItems()) {
                     ArrayList<AtsSearchData> savedSearches =
                        atsApi.getQueryService().getSavedSearches(currUser, item.getNamespace());
                     for (AtsSearchData data : savedSearches) {
                        if (!ids.contains(data.getId())) {
                           AtsSearchWorkflowSearchItem searchItem = item.copy();
                           searchItem.setSavedData(data);
                           SearchNavigateItem navItem = new SearchNavigateItem(topSearchItem, searchItem);
                           navItem.setName(item.getShortNamePrefix() + ": " + data.getSearchName());
                           ids.add(data.getId());
                        }
                     }
                  }
               }
               Displays.ensureInDisplayThread(new Runnable() {

                  @Override
                  public void run() {
                     NavigateView.getNavigateView().refresh(topSearchItem);
                  }
               });
            } catch (Exception ex) {
               OseeLog.log(NavigateViewLinksTopicEventHandler.class, Level.WARNING, "Error populating searches", ex);
            }
            return Status.OK_STATUS;
         }

      };
      Jobs.startJob(populateSavedSearchesJob, false);
   }

   public void refresh() {
      Displays.ensureInDisplayThread(new Runnable() {

         @Override
         public void run() {
            SavedSearchesNavigateItem topSearchItem =
               (SavedSearchesNavigateItem) NavigateView.getNavigateView().getItem(SAVED_SEARCH_ID, true);
            if (topSearchItem != null) {
               populateSavedSearchesItem(topSearchItem, AtsClientService.get().getUserService().getCurrentUser(),
                  AtsClientService.get());
            }
         }
      });
   }

   @Override
   public void handleEvent(Event event) {
      try {
         refresh();
      } catch (Exception ex) {
         OseeLog.log(NavigateViewLinksTopicEventHandler.class, Level.WARNING, ex);
      }
   }

   @Override
   public String toString() {
      return String.format("%s for %s and %s ", getClass().getSimpleName(), FrameworkEvents.NAVIGATE_VIEW_LOADED,
         AtsTopicEvent.SAVED_SEARCHES_MODIFIED);
   }

}
