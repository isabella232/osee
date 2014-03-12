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

import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.message.ChangeBranchTypeRequest;
import org.eclipse.osee.framework.core.translation.ITranslator;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;

/**
 * @author Megumi Telles
 */
public final class BranchChangeTypeRequestTranslator implements ITranslator<ChangeBranchTypeRequest> {

   private static enum Entry {
      BRANCH_ID,
      BRANCH_TYPE,
   }

   @Override
   public ChangeBranchTypeRequest convert(PropertyStore propertyStore) {
      long branchId = propertyStore.getLong(Entry.BRANCH_ID.name());
      BranchType type = BranchType.valueOf(propertyStore.get(Entry.BRANCH_TYPE.name()));
      ChangeBranchTypeRequest data = new ChangeBranchTypeRequest(branchId, type);
      return data;
   }

   @Override
   public PropertyStore convert(ChangeBranchTypeRequest data) {
      PropertyStore store = new PropertyStore();
      store.put(Entry.BRANCH_ID.name(), data.getBranchId());
      store.put(Entry.BRANCH_TYPE.name(), data.getType().name());
      return store;
   }

}
