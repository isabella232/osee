/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.vcast.model;

/**
 * @author Shawn F. Cook
 */
public class VCastMcdcCoveragePairRow {

   private final int id;
   private final int mcdcId;
   private final int rowValue;
   private final int rowResult;
   private final int hitCount;
   private final int maxHitCount;

   public VCastMcdcCoveragePairRow(int id, int mcdcId, int rowValue, int rowResult, int hitCount, int maxHitCount) {
      super();
      this.id = id;
      this.mcdcId = mcdcId;
      this.rowValue = rowValue;
      this.rowResult = rowResult;
      this.hitCount = hitCount;
      this.maxHitCount = maxHitCount;
   }

   public int getId() {
      return id;
   }

   public int getMcdcId() {
      return mcdcId;
   }

   public int getRowValue() {
      return rowValue;
   }

   public int getRowResult() {
      return rowResult;
   }

   public int getHitCount() {
      return hitCount;
   }

   public int getMaxHitCount() {
      return maxHitCount;
   }

}
