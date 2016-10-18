/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.data;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.eclipse.osee.framework.jdk.core.type.BaseId;
import org.eclipse.osee.framework.jdk.core.type.Id;
import org.eclipse.osee.framework.jdk.core.type.IdSerializer;

/**
 * @author Ryan D. Brooks
 */
@JsonSerialize(using = IdSerializer.class)
public interface TupleFamilyId extends Id {

   @JsonCreator
   public static TupleFamilyId valueOf(long tupleFamilyTypeId) {
      final class TupleFailyTypeImpl extends BaseId implements TupleFamilyId {
         public TupleFailyTypeImpl(Long tupleFamilyTypeId) {
            super(tupleFamilyTypeId);
         }
      }
      return new TupleFailyTypeImpl(tupleFamilyTypeId);
   }
}