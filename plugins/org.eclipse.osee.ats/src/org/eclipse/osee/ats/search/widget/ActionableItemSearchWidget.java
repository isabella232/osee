/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.search.widget;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.query.AtsSearchData;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.widgets.XHyperlabelActionableItemSelection;
import org.eclipse.osee.ats.world.WorldEditorParameterSearchItem;

/**
 * @author Donald G. Dunne
 */
public class ActionableItemSearchWidget {

   private final WorldEditorParameterSearchItem searchItem;

   public ActionableItemSearchWidget(WorldEditorParameterSearchItem searchItem) {
      this.searchItem = searchItem;
   }

   public void addWidget() {
      addWidget(0);
   }

   public void addWidget(int beginComposite) {
      searchItem.addWidgetXml(String.format(
         "<XWidget displayName=\"Actionable Item(s)\" xwidgetType=\"XHyperlabelActionableItemSelection\" horizontalLabel=\"true\" %s />",
         searchItem.getBeginComposite(beginComposite)));
   }

   public Collection<Long> getUuids() {
      List<Long> uuids = new LinkedList<>();
      for (IAtsActionableItem ai : get()) {
         uuids.add(ai.getUuid());
      }
      return uuids;
   }

   public Collection<IAtsActionableItem> get() {
      XHyperlabelActionableItemSelection widget = getWidget();
      if (widget != null) {
         return widget.getSelectedActionableItems();
      }
      return null;
   }

   public XHyperlabelActionableItemSelection getWidget() {
      return (XHyperlabelActionableItemSelection) searchItem.getxWidgets().get("Actionable Item(s)");
   }

   public void set(Collection<IAtsActionableItem> ais) {
      getWidget().setSelectedAIs(ais);
   }

   public void set(AtsSearchData data) {
      List<IAtsActionableItem> ais = new LinkedList<>();
      for (Long uuid : data.getAiUuids()) {
         IAtsActionableItem ai = AtsClientService.get().getConfig().getSoleByUuid(uuid, IAtsActionableItem.class);
         if (ai != null) {
            ais.add(ai);
         }
      }
      set(ais);
   }

}
