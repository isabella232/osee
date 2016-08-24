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
public interface GammaId extends Id {
   GammaId SENTINEL = valueOf(Id.SENTINEL);

   public static GammaId valueOf(String id) {
      return valueOf(Long.valueOf(id));
   }

   @JsonCreator
   public static GammaId valueOf(long id) {
      final class GammaIdImpl extends BaseId implements GammaId {
         public GammaIdImpl(Long id) {
            super(id);
         }
      }
      return new GammaIdImpl(id);
   }
}