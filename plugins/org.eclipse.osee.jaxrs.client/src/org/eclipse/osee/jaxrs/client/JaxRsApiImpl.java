/*******************************************************************************
 * Copyright (c) 2020 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.jaxrs.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.util.Collection;
import org.eclipse.osee.framework.core.JaxRsApi;
import org.eclipse.osee.framework.core.OrcsTokenService;
import org.eclipse.osee.framework.core.data.ArtifactTypeId;
import org.eclipse.osee.framework.core.data.ArtifactTypeToken;
import org.eclipse.osee.framework.core.data.AttributeTypeGeneric;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import javax.ws.rs.client.WebTarget;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.spec.ClientImpl.WebTargetImpl;
import org.eclipse.osee.framework.core.data.AttributeTypeToken;
import org.eclipse.osee.framework.core.data.RelationTypeId;
import org.eclipse.osee.framework.core.data.RelationTypeToken;
import org.eclipse.osee.framework.core.util.JsonUtil;
import org.eclipse.osee.framework.jdk.core.type.IdDeserializer;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.jaxrs.client.JaxRsClient.JaxRsClientFactory;
import org.eclipse.osee.jaxrs.client.internal.JaxRsClientRuntime;

/**
 * @author Ryan D. Brooks
 */
public final class JaxRsApiImpl implements JaxRsApi {
   private OrcsTokenService tokenService;
   private ObjectMapper mapper;
   private TypeFactory typeFactory;

   public void setOrcsTokenService(OrcsTokenService tokenService) {
      this.tokenService = tokenService;
   }

   public void start() {
      SimpleModule module = JsonUtil.createModule();

      module.addDeserializer(AttributeTypeGeneric.class,
         new IdDeserializer<AttributeTypeGeneric<?>>(tokenService::getAttributeType));
      module.addDeserializer(AttributeTypeToken.class,
         new IdDeserializer<AttributeTypeToken>(tokenService::getAttributeType));
      module.addDeserializer(AttributeTypeId.class,
         new IdDeserializer<AttributeTypeId>(tokenService::getAttributeType));

      module.addDeserializer(ArtifactTypeToken.class,
         new IdDeserializer<ArtifactTypeToken>(tokenService::getArtifactType));
      module.addDeserializer(ArtifactTypeId.class, new IdDeserializer<ArtifactTypeId>(tokenService::getArtifactType));

      module.addDeserializer(RelationTypeToken.class,
         new IdDeserializer<RelationTypeToken>(tokenService::getRelationType));
      module.addDeserializer(RelationTypeId.class, new IdDeserializer<RelationTypeId>(tokenService::getRelationType));

      mapper = JsonUtil.createStandardDateObjectMapper(module);
      typeFactory = mapper.getTypeFactory();
   }

   @Override
   public JsonNode readTree(String json) {
      return JsonUtil.readTree(mapper, json);
   }

   @Override
   public String toJson(Object object) {
      return JsonUtil.toJson(mapper, object);
   }

   @Override
   public <T> T readValue(String json, Class<T> valueType) {
      return JsonUtil.readValue(mapper, json, valueType);
   }

   @Override
   public <T> T readValue(String json, Class<? extends Collection> collectionClass, Class<?> elementClass) {
      try {
         return mapper.readValue(json, typeFactory.constructCollectionType(collectionClass, elementClass));
      } catch (IOException ex) {
         throw OseeCoreException.wrap(ex);
      }
   }

   @Override
   public WebTarget newTarget(String path) {
      JaxRsClientFactory factory = JaxRsClientRuntime.getClientFactoryInstance(mapper);
      JaxRsClientConfig config = new JaxRsClientConfig();
      config.setCreateThreadSafeProxyClients(true);
      String baseUrl = System.getProperty("osee.application.server", "http://localhost:8089");
      return factory.newWebTarget(config, baseUrl + "/" + path);
   }

   @Override
   public <T> T newProxy(WebTarget target, Class<T> clazz) {
      // This is here to force a webClient to store its configuration
      target.request();

      if (target instanceof WebTargetImpl) {
         return JAXRSClientFactory.fromClient(((WebTargetImpl) target).getWebClient(), clazz);
      }
      throw new OseeStateException("%s is of type %s not WebTargetImpl", target, target.getClass());
   }

   @Override
   public ObjectMapper getObjectMapper() {
      return mapper;
   }
}