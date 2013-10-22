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

import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ISearchPrimitive;
import org.eclipse.osee.framework.skynet.core.artifact.search.NotSearch;
import org.eclipse.osee.framework.skynet.core.artifact.search.OrphanArtifactSearch;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.search.filter.FilterTableViewer;
import org.eclipse.swt.widgets.Control;

/**
 * @author Jeff C. Phillips
 */
public class OrphanSearchFilter extends SearchFilter {
   private final ListViewer searchTypeList;

   public OrphanSearchFilter(String filterName, Control optionsControl, ListViewer searchTypeList) {
      super(filterName, optionsControl);
      this.searchTypeList = searchTypeList;
   }

   @Override
   public void addFilterTo(FilterTableViewer filterViewer) {
      try {
         for (String typeName : searchTypeList.getList().getSelection()) {

            IArtifactType artifactType = ArtifactTypeManager.getType(typeName);
            ISearchPrimitive primitive = new OrphanArtifactSearch(artifactType);

            if (not) {
               primitive = new NotSearch(primitive);
            }
            filterViewer.addItem(primitive, getFilterName(), typeName, "");
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public boolean isValid() {
      return true;
   }

   @Override
   public void loadFromStorageString(FilterTableViewer filterViewer, String type, String value, String storageString, boolean isNotEnabled) {
      ISearchPrimitive primitive = OrphanArtifactSearch.getPrimitive(storageString);
      if (isNotEnabled) {
         primitive = new NotSearch(primitive);
      }
      filterViewer.addItem(primitive, getFilterName(), type, value);
   }
}
