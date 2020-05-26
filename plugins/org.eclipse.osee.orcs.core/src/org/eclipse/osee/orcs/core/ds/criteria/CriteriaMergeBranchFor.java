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

package org.eclipse.osee.orcs.core.ds.criteria;

import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.orcs.core.ds.Criteria;
import org.eclipse.osee.orcs.core.ds.Options;

/**
 * @author John Misinco
 */
public class CriteriaMergeBranchFor extends Criteria implements BranchCriteria {

   private final BranchId source;
   private final BranchId destination;

   public CriteriaMergeBranchFor(BranchId source, BranchId destination) {
      super();
      this.source = source;
      this.destination = destination;
   }

   public BranchId getSource() {
      return source;
   }

   public BranchId getDestination() {
      return destination;
   }

   @Override
   public void checkValid(Options options) {
      Conditions.checkExpressionFailOnTrue(source == null, "Source Uuid cannot be null");
      Conditions.checkExpressionFailOnTrue(destination == null, "Destination Uuid cannot be null");
   }

   @Override
   public String toString() {
      return "CriteriaMergeBranchFor [source=" + source + ", destination=" + destination + "]";
   }
}
