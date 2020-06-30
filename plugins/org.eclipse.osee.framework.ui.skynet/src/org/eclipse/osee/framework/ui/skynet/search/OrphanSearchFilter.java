/*********************************************************************
 * Copyright (c) 2018 Boeing
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

package org.eclipse.osee.framework.ui.skynet.search;

import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.skynet.core.artifact.search.ISearchPrimitive;
import org.eclipse.osee.framework.skynet.core.artifact.search.NotInRelationSearch;
import org.eclipse.osee.framework.ui.skynet.search.filter.FilterTableViewer;
import org.eclipse.swt.widgets.Control;

/**
 * @author Donald G. Dunne
 */
public class OrphanSearchFilter extends NotInRelationFilter {

   public OrphanSearchFilter(Control optionsControl) {
      super(optionsControl, null, null, null);
   }

   @Override
   protected String getFilterName() {
      return "Orphan Search";
   }

   @Override
   public void addFilterTo(FilterTableViewer filterViewer) {
      ISearchPrimitive primitive = new NotInRelationSearch(CoreRelationTypes.DefaultHierarchical_Child, false);
      filterViewer.addItem(primitive, getFilterName(), CoreRelationTypes.DefaultHierarchical_Child.getName(), "Child");
   }

   @Override
   public void loadFromStorageString(FilterTableViewer filterViewer, String type, String value, String storageString, boolean isNotEnabled) {
      // do nothing
   }

   @Override
   public String getSearchDescription() {
      return "Find all artifacts that have no parent default hierarchy relation.";
   }
}