/*********************************************************************
 * Copyright (c) 2016 Boeing
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

package org.eclipse.osee.framework.core.data;

import static org.eclipse.osee.framework.core.enums.CoreTupleFamilyTypes.DefaultFamily;

/**
 * @author Ryan D. Brooks
 */
public interface Tuple2Type<E1, E2> extends TupleTypeId {

   public static <E1, E2> Tuple2Type<E1, E2> valueOf(TupleFamilyId family, Long tupleTypeId) {
      final class Tuple2TypeImpl extends TupleTypeImpl implements Tuple2Type<E1, E2> {
         public Tuple2TypeImpl(TupleFamilyId family, Long tupleTypeId) {
            super(family, tupleTypeId);
         }

         public Tuple2TypeImpl(Long tupleTypeId) {
            super(tupleTypeId);
         }

         @Override
         public Long getId() {
            return tupleTypeId;
         }
      }
      return new Tuple2TypeImpl(family, tupleTypeId);
   }

   public static <E1, E2> Tuple2Type<E1, E2> valueOf(Long tupleType) {
      return valueOf(DefaultFamily, tupleType);
   }

   public static <E1, E2> Tuple2Type<E1, E2> valueOf(String tupleType) {
      return valueOf(DefaultFamily, Long.parseLong(tupleType));
   }
}