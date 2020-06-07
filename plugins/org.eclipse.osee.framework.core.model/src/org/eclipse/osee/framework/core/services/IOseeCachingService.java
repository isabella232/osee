/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.framework.core.services;

import java.util.Collection;
import org.eclipse.osee.framework.core.OrcsTokenService;
import org.eclipse.osee.framework.core.enums.OseeCacheEnum;
import org.eclipse.osee.framework.core.model.cache.ArtifactTypeCache;
import org.eclipse.osee.framework.core.model.cache.AttributeTypeCache;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.cache.IOseeCache;
import org.eclipse.osee.framework.core.model.cache.OseeEnumTypeCache;

/**
 * @author Roberto E. Escobar
 */
public interface IOseeCachingService {

   ArtifactTypeCache getArtifactTypeCache();

   AttributeTypeCache getAttributeTypeCache();

   OseeEnumTypeCache getEnumTypeCache();

   BranchCache getBranchCache();

   Collection<?> getCaches();

   IOseeCache<?> getCache(OseeCacheEnum cacheId);

   void reloadTypes();

   void reloadAll();

   void clearAll();

   OrcsTokenService getTokenService();
}
