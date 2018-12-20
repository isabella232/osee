/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.internal.attribute.primitives;

import org.eclipse.osee.framework.jdk.core.type.Id;

public abstract class IdentityReferenceAttribute extends CharacterBackedAttribute<Id> {

   public IdentityReferenceAttribute(Long id) {
      super(id);
   }

   @Override
   public String convertToStorageString(Id rawValue) {
      return rawValue.getIdString();
   }
}