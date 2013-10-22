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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ISearchPrimitive;
import org.eclipse.osee.framework.ui.skynet.search.filter.FilterModel;
import org.eclipse.osee.framework.ui.skynet.search.filter.FilterModelList;

/**
 * @author Ryan D. Brooks
 */
public class FilterArtifactSearchQuery extends AbstractLegacyArtifactSearchQuery {
   private final FilterModelList filterList;
   private final IOseeBranch branch;
   private String criteriaLabel = "";

   public FilterArtifactSearchQuery(FilterModelList filterList, IOseeBranch branch) {
      this.filterList = filterList;
      this.branch = branch;
   }

   @Override
   public Collection<Artifact> getArtifacts() throws OseeCoreException {
      boolean firstTime = true;
      List<ISearchPrimitive> criteria = new LinkedList<ISearchPrimitive>();

      for (FilterModel model : filterList.getFilters()) {
         criteria.add(model.getSearchPrimitive());

         if (!firstTime) {
            if (filterList.isAllSelected()) {
               criteriaLabel += " and ";
            } else {
               criteriaLabel += " or ";
            }
         }

         criteriaLabel += model;
         firstTime = false;
      }

      MaxMatchCountConfirmer confirmer = new MaxMatchCountConfirmer();
      Collection<Artifact> artifacts =
         ArtifactPersistenceManager.getArtifacts(criteria, filterList.isAllSelected(), branch, confirmer);
      if (confirmer.isConfirmed()) {
         return artifacts;
      }
      return Collections.emptyList();
   }

   @Override
   public String getCriteriaLabel() {
      return criteriaLabel;
   }
}