/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.jaxrs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.jaxrs.JsonMappingExceptionMapper;
import org.codehaus.jackson.jaxrs.JsonParseExceptionMapper;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

/**
 * @author Roberto E. Escobar
 */
@Provider
public class JacksonFeature implements Feature {

   private final static String JSON_FEATURE = JacksonFeature.class.getName();

   public static final String JAXRS_CONFIG_PREFIX = "jaxrs";
   public static final String JAXRS_CONFIG_PREFIX_AND_DOT = JAXRS_CONFIG_PREFIX + ".";
   public static final String JSON_FEATURE_KEY = JAXRS_CONFIG_PREFIX_AND_DOT + "jsonFeature";

   @Override
   public boolean configure(FeatureContext context) {
      boolean configured = false;
      Configuration config = context.getConfiguration();

      String propertyKey = getPropertyNameForRuntime(JSON_FEATURE_KEY, config.getRuntimeType());

      String jsonFeature = getJsonFeature(config, propertyKey, JSON_FEATURE);
      if (JSON_FEATURE.equalsIgnoreCase(jsonFeature)) {
         context.property(propertyKey, JSON_FEATURE);

         if (!config.isRegistered(JacksonJaxbJsonProvider.class)) {
            for (Object object : getProviders()) {
               context.register(object);
            }
         }
         configured = true;
      }
      return configured;
   }

   private String getJsonFeature(Configuration config, String propertyKey, String defaultValue) {
      String toReturn = defaultValue;
      Map<String, Object> props = config.getProperties();
      Object value = props != null ? props.get(propertyKey) : null;
      if (value != null) {
         toReturn = String.valueOf(value);
      }
      return toReturn;
   }

   public static String getPropertyNameForRuntime(String key, RuntimeType runtimeType) {
      if (runtimeType != null && key.startsWith(JAXRS_CONFIG_PREFIX)) {
         RuntimeType[] types = RuntimeType.values();
         for (RuntimeType type : types) {
            if (key.startsWith(JAXRS_CONFIG_PREFIX_AND_DOT + type.name().toLowerCase())) {
               return key;
            }
         }
         return key.replace(JAXRS_CONFIG_PREFIX, JAXRS_CONFIG_PREFIX_AND_DOT + runtimeType.name().toLowerCase());
      }
      return key;
   }

   public static List<? extends Object> getProviders() {
      List<Object> providers = new ArrayList<Object>();
      providers.add(JacksonFeature.newJacksonJsonProvider());
      providers.add(JsonParseExceptionMapper.class);
      providers.add(JsonMappingExceptionMapper.class);
      return providers;
   }

   private static JacksonJsonProvider newJacksonJsonProvider() {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(SerializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS, true);
      objectMapper.configure(SerializationConfig.Feature.AUTO_DETECT_FIELDS, true);
      objectMapper.configure(SerializationConfig.Feature.AUTO_DETECT_GETTERS, true);
      objectMapper.configure(SerializationConfig.Feature.AUTO_DETECT_IS_GETTERS, true);
      objectMapper.configure(SerializationConfig.Feature.USE_ANNOTATIONS, true);
      objectMapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, false);
      objectMapper.configure(SerializationConfig.Feature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS, true);

      objectMapper.configure(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS, true);
      objectMapper.configure(DeserializationConfig.Feature.AUTO_DETECT_FIELDS, true);
      objectMapper.configure(DeserializationConfig.Feature.AUTO_DETECT_SETTERS, true);
      objectMapper.configure(DeserializationConfig.Feature.AUTO_DETECT_CREATORS, true);
      objectMapper.configure(DeserializationConfig.Feature.USE_ANNOTATIONS, true);
      objectMapper.configure(DeserializationConfig.Feature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
      objectMapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, false);
      objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      return new JacksonJaxbJsonProvider(objectMapper, JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS);
   }

}