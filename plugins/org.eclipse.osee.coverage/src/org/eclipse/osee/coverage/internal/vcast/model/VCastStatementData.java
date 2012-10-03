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
public class VCastStatementData {

   private final int id;
   private final int statementId;
   private final int resultId;
   private final int resultLine;
   private final Boolean hit;

   public VCastStatementData(int id, int statementId, int resultId, int resultLine, Boolean hit) {
      super();
      this.id = id;
      this.statementId = statementId;
      this.resultId = resultId;
      this.resultLine = resultLine;
      this.hit = hit;
   }

   public int getId() {
      return id;
   }

   public int getStatementId() {
      return statementId;
   }

   public int getResultId() {
      return resultId;
   }

   public int getResultLine() {
      return resultLine;
   }

   public Boolean getHit() {
      return hit;
   }

}
