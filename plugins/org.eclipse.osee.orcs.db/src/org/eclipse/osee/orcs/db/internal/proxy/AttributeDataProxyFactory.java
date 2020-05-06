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
package org.eclipse.osee.orcs.db.internal.proxy;

import org.eclipse.osee.framework.core.OrcsTokenService;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.resource.management.IResourceManager;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.DataProxy;
import org.eclipse.osee.orcs.data.AttributeTypes;

/**
 * @author Roberto E. Escobar
 */
public class AttributeDataProxyFactory {
   private final AttributeTypes attributeTypeCache;
   private final OrcsTokenService tokenService;
   private final IResourceManager resourceManager;
   private final Log logger;

   public AttributeDataProxyFactory(AttributeTypes attributeTypes, OrcsTokenService tokenService, IResourceManager resourceManager, Log logger) {
      this.attributeTypeCache = attributeTypes;
      this.tokenService = tokenService;
      this.resourceManager = resourceManager;
      this.logger = logger;
   }

   public DataProxy createProxy(AttributeTypeId attributeType, Object value, String uri) {
      String attributeProviderId = attributeTypeCache.getAttributeProviderId(attributeType);

      Object checkedValue = intern(attributeType, value);
      AbstractDataProxy dataProxy;
      if (attributeProviderId.equals("UriAttributeDataProvider") || attributeProviderId.equals(
         "MappedAttributeDataProvider")) {
         dataProxy = new UriDataProxy();
      } else {
         dataProxy = new VarCharDataProxy();
      }
      dataProxy.setLogger(logger);
      dataProxy.setStorage(new Storage(resourceManager, dataProxy));
      dataProxy.setData(checkedValue, uri);
      return dataProxy;
   }

   private Object intern(AttributeTypeId attributeType, Object original) {
      Object value = original;
      if (tokenService.getAttributeType(attributeType.getId()).isEnumerated() && value instanceof String) {
         value = Strings.intern((String) value);
      }
      return value;
   }
}
