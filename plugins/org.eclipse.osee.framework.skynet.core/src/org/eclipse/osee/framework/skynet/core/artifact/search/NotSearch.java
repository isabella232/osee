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
package org.eclipse.osee.framework.skynet.core.artifact.search;

import java.util.List;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactPersistenceManager;

/**
 * @author Robert A. Fisher
 */
public class NotSearch implements ISearchPrimitive {
   private final ISearchPrimitive search;

   public NotSearch(ISearchPrimitive search) {
      super();
      this.search = search;
   }

   @Override
   public String getCriteriaSql(List<Object> dataList, IOseeBranch branch) throws OseeCoreException {
      return "NOT EXISTS(SELECT 'x' FROM (" + ArtifactPersistenceManager.getSelectArtIdSql(search, dataList, branch) + ") arts" + " WHERE osee_artifact.art_id = arts." + search.getArtIdColName() + ")";
   }

   @Override
   public String getArtIdColName() {
      return "art_id";
   }

   @Override
   public String getTableSql(List<Object> dataList, IOseeBranch branch) {
      return "osee_artifact";
   }

   @Override
   public String getStorageString() {
      return "Not [" + search.getStorageString() + "]";
   }

   @Override
   public String toString() {
      return "Not " + search.toString();
   }
}