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
package org.eclipse.osee.orcs.core.internal.types.impl;

import java.util.Collection;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.core.internal.types.OrcsTypesIndexProvider;
import org.eclipse.osee.orcs.data.EnumType;
import org.eclipse.osee.orcs.data.EnumTypes;

/**
 * @author John Misinco
 */
public class EnumTypesImpl implements EnumTypes {

   private final OrcsTypesIndexProvider indexProvider;

   public EnumTypesImpl(OrcsTypesIndexProvider indexProvider) {
      this.indexProvider = indexProvider;
   }

   @Override
   public Collection<? extends EnumType> getAll() throws OseeCoreException {
      return getIndex().getAllTokens();
   }

   @Override
   public EnumType getByUuid(Long typeId) throws OseeCoreException {
      return getIndex().getTokenByUuid(typeId);
   }

   @Override
   public boolean exists(EnumType item) throws OseeCoreException {
      return getIndex().existsByUuid(item.getGuid());
   }

   @Override
   public boolean isEmpty() throws OseeCoreException {
      return getAll().isEmpty();
   }

   @Override
   public int size() throws OseeCoreException {
      return getAll().size();
   }

   private EnumTypeIndex getIndex() throws OseeCoreException {
      return indexProvider.getEnumTypeIndex();
   }

}
