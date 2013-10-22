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
package org.eclipse.osee.framework.skynet.core.internal.accessors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.enums.CoreTranslatorId;
import org.eclipse.osee.framework.core.message.RelationTypeCacheUpdateResponse;
import org.eclipse.osee.framework.core.message.RelationTypeCacheUpdateResponse.RelationTypeRow;
import org.eclipse.osee.framework.core.model.cache.AbstractOseeCache;
import org.eclipse.osee.framework.core.model.cache.IOseeCache;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.core.model.type.RelationTypeFactory;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Roberto E. Escobar
 */
public class ClientRelationTypeAccessor extends AbstractClientDataAccessor<Long, RelationType> {

   private final AbstractOseeCache<Long, ArtifactType> artCache;
   private final RelationTypeFactory relationTypeFactory;

   public ClientRelationTypeAccessor(RelationTypeFactory relationTypeFactory, AbstractOseeCache<Long, ArtifactType> artCache) {
      super();
      this.relationTypeFactory = relationTypeFactory;
      this.artCache = artCache;
   }

   private RelationTypeFactory getFactory() {
      return relationTypeFactory;
   }

   @Override
   public void load(IOseeCache<Long, RelationType> cache) throws OseeCoreException {
      artCache.ensurePopulated();
      super.load(cache);
   }

   @Override
   protected Collection<RelationType> updateCache(IOseeCache<Long, RelationType> cache) throws OseeCoreException {
      List<RelationType> updatedItems = new ArrayList<RelationType>();

      RelationTypeCacheUpdateResponse response =
         requestUpdateMessage(cache, CoreTranslatorId.RELATION_TYPE_CACHE_UPDATE_RESPONSE);

      RelationTypeFactory factory = getFactory();
      for (RelationTypeRow row : response.getRelationTypeRows()) {
         IArtifactType aSideType = artCache.getById(row.getArtifactTypeSideA());
         IArtifactType bSideType = artCache.getById(row.getArtifactTypeSideB());

         RelationType type =
            factory.createOrUpdate(cache, row.getId(), row.getStorageState(), row.getGuid(), row.getName(),
               row.getSideAName(), row.getSideBName(), aSideType, bSideType, row.getMultiplicity(),
               row.getDefaultOrderTypeGuid());
         updatedItems.add(type);
      }
      return updatedItems;
   }
}
