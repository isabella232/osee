/*********************************************************************
 * Copyright (c) 2014 Boeing
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

package org.eclipse.osee.framework.core.model.datarights;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.util.PageOrientation;

/**
 * @author Angel Avila
 */
public class DataRightInput implements Iterable<DataRightEntry> {
   private Set<DataRightEntry> data;

   public Set<DataRightEntry> getData() {
      if (data == null) {
         data = new HashSet<>();
      }
      return data;
   }

   public void setData(Set<DataRightEntry> data) {
      this.data = data;
   }

   public boolean isEmpty() {
      return data == null || data.isEmpty();
   }

   public void clear() {
      data = null;
   }

   public void addData(ArtifactId id, String classification, PageOrientation orientation, int index) {
      DataRightEntry toAdd = new DataRightEntry();
      toAdd.setClassification(classification);
      toAdd.setId(id);
      toAdd.setOrientation(orientation);
      toAdd.setIndex(index);

      getData().add(toAdd);
   }

   @Override
   public Iterator<DataRightEntry> iterator() {
      return data.iterator();
   }

}