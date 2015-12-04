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
package org.eclipse.osee.framework.ui.skynet.search;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.skynet.core.artifact.search.ISearchPrimitive;
import org.eclipse.osee.framework.skynet.core.artifact.search.InRelationSearch;
import org.eclipse.osee.framework.ui.skynet.search.filter.FilterTableViewer;
import org.eclipse.swt.widgets.Control;

/**
 * @author Ryan D. Brooks
 */
public class InRelationFilter extends SearchFilter {
   private final ComboViewer relationTypeList;
   private final ComboViewer relationSideList;

   public InRelationFilter(Control optionsControl, ComboViewer relationTypeList, ComboViewer relationSideList) {
      super("Artifact in Relation", optionsControl);
      this.relationTypeList = relationTypeList;
      this.relationSideList = relationSideList;
   }

   @Override
   public void addFilterTo(FilterTableViewer filterViewer) {
      String typeName = relationTypeList.getCombo().getText();
      String sideName = relationSideList.getCombo().getText();

      RelationType relationType = (RelationType) relationTypeList.getData(typeName);
      Boolean sideAName = null;
      try {
         sideAName = relationType.isSideAName(sideName);
      } catch (OseeArgumentException ex) {
         // do nothing, user wants either
      }
      ISearchPrimitive primitive = new InRelationSearch(relationType, sideAName);

      filterViewer.addItem(primitive, getFilterName(), typeName, sideName);
   }

   @Override
   public boolean isValid() {
      return true;
   }

   @Override
   public void loadFromStorageString(FilterTableViewer filterViewer, String type, String value, String storageString, boolean isNotEnabled) {
      ISearchPrimitive primitive = InRelationSearch.getPrimitive(storageString);
      filterViewer.addItem(primitive, getFilterName(), type, value);
   }

   @Override
   public String getSearchDescription() {
      return "This search will return all artifacts in the selected relation";
   }
}
