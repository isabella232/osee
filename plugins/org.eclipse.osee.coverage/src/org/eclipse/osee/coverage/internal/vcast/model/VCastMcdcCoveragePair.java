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
package org.eclipse.osee.coverage.internal.vcast.model;

/**
 * @author Shawn F. Cook
 */
public class VCastMcdcCoveragePair {

   private final int id;
   private final int mcdcCondId;
   private final int pairRow1;
   private final int pairRow2;

   public VCastMcdcCoveragePair(int id, int mcdcCondId, int pairRow1, int pairRow2) {
      super();
      this.id = id;
      this.mcdcCondId = mcdcCondId;
      this.pairRow1 = pairRow1;
      this.pairRow2 = pairRow2;
   }

   public int getId() {
      return id;
   }

   public int getMcdcCondId() {
      return mcdcCondId;
   }

   public int getPairRow1() {
      return pairRow1;
   }

   public int getPairRow2() {
      return pairRow2;
   }

}
