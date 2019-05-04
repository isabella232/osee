/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.ide.search;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.ats.api.query.AtsSearchData;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.ide.internal.AtsClientService;
import org.eclipse.osee.ats.ide.navigate.NavigateView;
import org.eclipse.osee.ats.ide.navigate.NavigateViewItems;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.EntryDialog;
import org.eclipse.osee.framework.ui.swt.ImageManager;

/**
 * @author Donald G. Dunne
 */
public final class SaveAsSearchAction extends Action {

   private final AtsSearchWorkflowSearchItem searchItem;

   public SaveAsSearchAction(AtsSearchWorkflowSearchItem searchItem) {
      this.searchItem = searchItem;
   }

   @Override
   public String getText() {
      return "Save As Search";
   }

   @Override
   public String getToolTipText() {
      return "Enter search criteria and select to save as";
   }

   @Override
   public void run() {
      IAtsUser asUser = AtsClientService.get().getUserService().getCurrentUser();
      EntryDialog dialog = new EntryDialog("Save Search As", "Save Search?\n\nSearch Name");
      if (dialog.open() == 0) {
         if (!Strings.isValid(dialog.getEntry())) {
            AWorkbench.popup("Invalid Search Name");
            return;
         }
         searchItem.setSearchName(dialog.getEntry());
         searchItem.setSearchId(Lib.generateId());

         String namespace = searchItem.getNamespace();
         AtsSearchData data =
            AtsClientService.get().getQueryService().createSearchData(namespace, searchItem.getSearchName());
         searchItem.loadSearchData(data);
         if (data.getId() <= 0) {
            data.setId(Lib.generateArtifactIdAsInt());
         }
         Conditions.checkExpressionFailOnTrue(data.getId() <= 0, "searchId must be > 0, not %d", data.getId());
         Conditions.checkNotNullOrEmpty(data.getSearchName(), "New Search Name");
         AtsClientService.get().getQueryService().saveSearch(asUser, data);
         AtsClientService.get().getQueryServiceClient().getArtifact(asUser).reloadAttributesAndRelations();
         if (NavigateView.getNavigateView() != null) {
            NavigateViewItems.refreshTopAtsSearchItem();
         }
         AWorkbench.popupf("Search Saved as [%s]", data.getSearchName());
      }
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(FrameworkImage.SAVE_AS);
   }
};