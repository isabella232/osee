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
package org.eclipse.osee.framework.ui.plugin.xnavigate;

import java.util.List;

/**
 * @author Andrew M. Finkbeiner
 */
public class XNavigateExtensionPointData {
   private final String viewId;
   private final String category;
   private final IXNavigateContainer navigateItem;

   public XNavigateExtensionPointData(String viewId, String category, IXNavigateContainer navigateItem) {
      this.viewId = viewId;
      this.category = category;
      this.navigateItem = navigateItem;
   }

   public String[] getItemPath() {
      if (category != null && category.length() > 0) {
         return category.split("\\.");
      } else {
         return new String[0];
      }
   }

   public List<XNavigateItem> getNavigateItems() {
      return navigateItem.getNavigateItems();
   }

   public String getCategory() {
      return category;
   }

   public String getViewId() {
      return viewId;
   }
}
