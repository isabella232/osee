/*******************************************************************************
 * Copyright (c) 2019 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.data;

/**
 * @author Ryan D. Brooks
 */
public final class AttributeTypeBranchId extends AttributeTypeGeneric<BranchId> {
   public AttributeTypeBranchId(Long id, NamespaceToken namespace, String name, String mediaType, String description, TaggerTypeToken taggerType) {
      super(id, namespace, name, mediaType, description, taggerType);
   }

   @Override
   public String storageStringFromValue(BranchId branch) {
      return branch.getIdString();
   }

   @Override
   public BranchId valueFromStorageString(String storedValue) {
      return BranchId.valueOf(storedValue);
   }
}