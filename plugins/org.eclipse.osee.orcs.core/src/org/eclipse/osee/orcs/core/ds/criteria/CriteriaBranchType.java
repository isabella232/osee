/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.ds.criteria;

import java.util.Collection;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.core.ds.Criteria;
import org.eclipse.osee.orcs.core.ds.Options;

/**
 * @author Roberto E. Escobar
 */
public class CriteriaBranchType extends Criteria {

   private final Collection<BranchType> types;

   public CriteriaBranchType(Collection<BranchType> types) {
      super();
      this.types = types;
   }

   public Collection<BranchType> getTypes() {
      return types;
   }

   @Override
   public void checkValid(Options options) throws OseeCoreException {
      super.checkValid(options);
      Conditions.checkExpressionFailOnTrue(getTypes().isEmpty(), "Branch Types cannot be empty");
   }

   @Override
   public String toString() {
      return "CriteriaBranchType [types=" + types + "]";
   }

}
