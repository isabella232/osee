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
package org.eclipse.osee.ats.core.client.internal.config;

import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.core.config.IActionableItemFactory;

/**
 * @author Donald G. Dunne
 */
public class ActionableItemFactory implements IActionableItemFactory {

   @Override
   public IAtsActionableItem createActionableItem(String guid, String name) {
      if (guid == null) {
         throw new IllegalArgumentException("guid can not be null");
      }
      return new ActionableItem(name, guid);
   }

}
