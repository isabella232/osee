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

import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.util.PageOrientation;

/**
 * @author Angel Avila
 */
public class DataRightEntry {
   private ArtifactId id;
   private String classification;
   private PageOrientation orientation;
   private int index;

   public ArtifactId getId() {
      return id;
   }

   public void setId(ArtifactId id) {
      this.id = id;
   }

   public String getClassification() {
      return classification;
   }

   public void setClassification(String classification) {
      this.classification = classification;
   }

   public PageOrientation getOrientation() {
      return orientation;
   }

   public void setOrientation(PageOrientation orientation) {
      this.orientation = orientation;
   }

   public int getIndex() {
      return index;
   }

   public void setIndex(int index) {
      this.index = index;
   }
}
