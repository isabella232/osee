/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.ats.ide.util.widgets.dialog;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.ats.ide.internal.AtsApiService;
import org.eclipse.osee.ats.ide.util.AtsObjectLabelProvider;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.util.ArtifactNameSorter;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.FilteredCheckboxTreeDialog;

/**
 * @author Donald G. Dunne
 */
public class ActionableItemListDialog extends FilteredCheckboxTreeDialog<IAtsActionableItem> {

   public ActionableItemListDialog(Active active, String message) {
      super("Select Actionable Item(s)", "Select Actionable Item(s)", new AITreeContentProvider(active),
         new AtsObjectLabelProvider(), new ArtifactNameSorter());
      try {
         setInput(AtsApiService.get().getActionableItemService().getTopLevelActionableItems(active));
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   public Set<IAtsActionableItem> getSelected() {
      Set<IAtsActionableItem> selectedactionItems = new HashSet<>();
      for (Object obj : getResult()) {
         selectedactionItems.add((IAtsActionableItem) obj);
      }
      return selectedactionItems;
   }

   @Override
   public void setInput(Object input) {
      super.setInput(input);
      if (input instanceof Collection<?>) {
         Collection<?> coll = (Collection<?>) input;
         if (coll.size() == 1) {
            getTreeViewer().getViewer().expandToLevel(coll.iterator().next(), 1);
         }
      } else if (input instanceof IAtsActionableItem) {
         getTreeViewer().getViewer().expandToLevel(input, 1);
      }
   }

}
