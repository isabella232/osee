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
package org.eclipse.osee.framework.core.message.internal.translation;

import org.eclipse.osee.framework.core.message.BranchCreationResponse;
import org.eclipse.osee.framework.core.translation.ITranslator;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;

/**
 * @author Roberto E. Escobar
 */
public class BranchCreationResponseTranslator implements ITranslator<BranchCreationResponse> {

   private static enum Fields {
      BRANCH_ID;
   }

   @Override
   public BranchCreationResponse convert(PropertyStore store) {
      long branchId = store.getLong(Fields.BRANCH_ID.name());
      return new BranchCreationResponse(branchId);
   }

   @Override
   public PropertyStore convert(BranchCreationResponse object) {
      PropertyStore store = new PropertyStore();
      store.put(Fields.BRANCH_ID.name(), object.getBranchId());
      return store;
   }
}
