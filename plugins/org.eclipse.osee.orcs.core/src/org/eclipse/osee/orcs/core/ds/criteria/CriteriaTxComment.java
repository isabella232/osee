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

import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.orcs.core.ds.Criteria;
import org.eclipse.osee.orcs.core.ds.Options;

/**
 * @author Roberto E. Escobar
 */
public class CriteriaTxComment extends Criteria {

   private final boolean isPattern;
   private final String value;

   public CriteriaTxComment(String value, boolean isPattern) {
      super();
      this.value = value;
      this.isPattern = isPattern;
   }

   public boolean isPattern() {
      return isPattern;
   }

   public String getValue() {
      return value;
   }

   @Override
   public void checkValid(Options options) throws OseeCoreException {
      super.checkValid(options);
      Conditions.checkNotNullOrEmpty(getValue(), "comment value");
   }

   @Override
   public String toString() {
      return "CriteriaTxComment [isPattern=" + isPattern + ", value=" + value + "]";
   }

}
