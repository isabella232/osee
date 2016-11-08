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
package org.eclipse.osee.framework.core.data;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.eclipse.osee.framework.jdk.core.type.Id;
import org.eclipse.osee.framework.jdk.core.type.IdSerializer;
import org.eclipse.osee.framework.jdk.core.type.NamedIdBase;

/**
 * @author Ryan D. Brooks
 */
@JsonSerialize(using = IdSerializer.class)
// TODO: Rename to RelationTypeId
public interface IRelationType extends Id {

   public static IRelationType valueOf(long id, String name) {
      final class RelationTypeIdImpl extends NamedIdBase implements IRelationType {

         public RelationTypeIdImpl(Long id, String name) {
            super(id, name);
         }
      }
      return new RelationTypeIdImpl(id, name);
   }
}
