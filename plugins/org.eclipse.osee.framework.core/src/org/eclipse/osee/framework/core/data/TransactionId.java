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
public interface TransactionId extends Id {
   TransactionId SENTINEL = valueOf(Id.SENTINEL);

   default boolean isOlderThan(TransactionId other) {
      return getId() < other.getId();
   }

   public static TransactionId valueOf(String id) {
      return valueOf(Long.valueOf(id));
   }

   @JsonCreator
   public static TransactionId valueOf(long id) {
      final class TransactionToken extends BaseId implements TransactionId {
         public TransactionToken(Long txId) {
            super(txId);
         }
      }
      return new TransactionToken(id);
   }
}