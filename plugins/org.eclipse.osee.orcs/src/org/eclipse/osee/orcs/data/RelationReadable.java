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
package org.eclipse.osee.orcs.data;

import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.RelationId;
import org.eclipse.osee.framework.core.data.RelationTypeId;
import org.eclipse.osee.framework.core.enums.ModificationType;

/**
 * @author Donald G. Dunne
 */
public interface RelationReadable extends RelationId, OrcsReadable {

   long getGammaId();

   @Override
   ModificationType getModificationType();

   RelationTypeId getRelationType();

   boolean isOfType(IRelationType relationType);

   @Override
   String toString();

   int getArtIdA();

   int getArtIdB();

}