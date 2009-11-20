/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.data;

import java.util.Collection;
import org.eclipse.osee.framework.core.enums.OseeCacheEnum;

/**
 * @author Roberto E. Escobar
 */
public class CacheUpdateResponse<T> {
   private final Collection<T> items;
   private final OseeCacheEnum cacheId;

   public CacheUpdateResponse(OseeCacheEnum cacheId, Collection<T> items) {
      this.cacheId = cacheId;
      this.items = items;
   }

   public Collection<T> getItems() {
      return items;
   }

   public OseeCacheEnum getCacheId() {
      return cacheId;
   }
}
