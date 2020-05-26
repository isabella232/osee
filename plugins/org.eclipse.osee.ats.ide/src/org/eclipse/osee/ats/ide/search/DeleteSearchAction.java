/*********************************************************************
 * Copyright (c) 2015 Boeing
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

package org.eclipse.osee.ats.ide.search;

import java.util.Collections;
import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.ats.api.query.AtsSearchData;
import org.eclipse.osee.ats.api.util.AtsTopicEvent;
import org.eclipse.osee.ats.ide.internal.AtsClientService;
import org.eclipse.osee.framework.core.event.EventType;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.model.TopicEvent;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.ArrayTreeContentProvider;
import org.eclipse.osee.framework.ui.plugin.util.StringLabelProvider;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.FilteredTreeDialog;
import org.eclipse.osee.framework.ui.swt.ImageManager;

/**
 * @author Donald G. Dunne
 */
public final class DeleteSearchAction extends Action {

   private final AtsSearchWorkflowSearchItem searchItem;

   public DeleteSearchAction(AtsSearchWorkflowSearchItem searchItem) {
      this.searchItem = searchItem;
   }

   @Override
   public String getText() {
      return "Delete Saved Search";
   }

   @Override
   public void run() {
      List<AtsSearchData> searchDatas = AtsClientService.get().getQueryService().getSavedSearches(
         AtsClientService.get().getUserService().getCurrentUser(), searchItem.getNamespace());
      Collections.sort(searchDatas, new QuickSearchDataComparator());
      FilteredTreeDialog dialog = new FilteredTreeDialog("Delete Saved Search", "Select Search to Delete",
         new ArrayTreeContentProvider(), new StringLabelProvider());
      dialog.setInput(searchDatas);

      if (dialog.open() == 0) {
         AtsSearchData selected = (AtsSearchData) dialog.getSelectedFirst();
         AtsClientService.get().getQueryService().removeSearch(AtsClientService.get().getUserService().getCurrentUser(),
            selected);

         TopicEvent event = new TopicEvent(AtsTopicEvent.SAVED_SEARCHES_MODIFIED, "", "", EventType.LocalOnly);
         OseeEventManager.kickTopicEvent(DeleteSearchAction.class, event);

         AWorkbench.popup("Search Deleted");
      }
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(FrameworkImage.DELETE);
   }
};
