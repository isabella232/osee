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
package org.eclipse.osee.ats.client.integration.tests.ats.resource;

import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.osee.framework.core.client.OseeClientProperties;
import org.eclipse.osee.jaxrs.client.JaxRsClient;
import org.junit.Assert;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Donald G. Dunne
 */
public abstract class AbstractRestTest {

   protected JsonObject getObjectNamed(String name, JsonArray array) throws Exception {
      for (int x = 0; x < array.size(); x++) {
         JsonObject obj = (JsonObject) array.get(x);
         JsonElement jsonElement = obj.get("Name");
         if (jsonElement == null) {
            jsonElement = obj.get("name");
         }
         if (jsonElement.getAsString().equals(name)) {
            return obj;
         }
      }
      return null;
   }

   protected String getAndCheckStr(String url) {
      return getAndCheck(url, MediaType.TEXT_HTML_TYPE);
   }

   protected String getAndCheck(String url, MediaType mediaType) {
      String appServer = getAppServerAddr();
      URI uri = UriBuilder.fromUri(appServer).path(url).build();
      return getAndCheck(uri, mediaType);
   }

   protected String getAppServerAddr() {
      return OseeClientProperties.getOseeApplicationServer();
   }

   protected String getAndCheck(URI uri, MediaType mediaType) {
      Response response = JaxRsClient.newClient().target(uri).request(mediaType).get();
      Assert.assertEquals("Unexpected error code: " + response.readEntity(String.class),
         javax.ws.rs.core.Response.Status.OK.getStatusCode(), response.getStatus());
      return response.readEntity(String.class);
   }

   protected JsonArray getAndCheckArray(URI uri) throws Exception {
      String json = getAndCheck(uri, MediaType.APPLICATION_JSON_TYPE);
      return parseJsonArray(json);
   }

   protected JsonArray getAndCheckArray(String url) throws Exception {
      String json = getAndCheck(url, MediaType.APPLICATION_JSON_TYPE);
      return parseJsonArray(json);
   }

   protected JsonArray parseJsonArray(String json) {
      JsonParser jp = new JsonParser();
      JsonElement je = jp.parse(json);
      return (JsonArray) je;
   }
}
