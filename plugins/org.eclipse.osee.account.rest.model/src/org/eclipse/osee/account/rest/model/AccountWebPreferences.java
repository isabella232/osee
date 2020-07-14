/*********************************************************************
* Copyright (c) 2015 Boeing
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

package org.eclipse.osee.account.rest.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.jaxrs.client.JaxRsApiImpl;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Angel Avila
 */
public class AccountWebPreferences {

   Map<String, Link> linksMap = new HashMap<>();

   public AccountWebPreferences() {

   }

   public AccountWebPreferences(Map<String, String> teamToPreferences) {
      for (String team : teamToPreferences.keySet()) {
         initPreferences(teamToPreferences.get(team), team);
      }
   }

   private void initPreferences(String string, String team) {
      try {
         //   int i = 3 / 0;
         JaxRsApiImpl impl = new JaxRsApiImpl();
         impl.start();
         String json = impl.toJson(string);
         HashMap jsonMap = new HashMap();
         ObjectMapper OM = impl.getObjectMapper();

         JsonNode node = OM.readTree(string);

         JSONObject jObject = new JSONObject(string);
         JSONObject linkJsonObject = jObject.getJSONObject("links");
         @SuppressWarnings("unchecked")
         Iterator<String> keys = linkJsonObject.keys();
         while (keys.hasNext()) {
            String next = keys.next();
            JsonNode linkJObject = OM.readTree(next);
            JSONObject linkJObject2 = linkJsonObject.getJSONObject(next);
            Link link = new Link();
            if (linkJObject.has("name")) {
               link.setName(linkJObject.get("name").toString());

            }
            if (linkJObject.has("url")) {
               link.setUrl(linkJObject.get("url").toString());
            }
            if (linkJObject.has("tags")) {
               List<String> array = linkJObject.findValuesAsText("tags");
               JSONArray array2 = linkJObject2.getJSONArray("tags");
               for (int x = 0; x < array.size(); x++) {
                  link.getTags().add(array.get(x));
               }
            }
            link.setTeam(team);
            link.setId(linkJObject.get("id").toString());
            linksMap.put(next, link);
         }

      } catch (Exception ex) {
         System.out.println("Exception:" + ex);
      }
   }

   public AccountWebPreferences(String jsonString, String team) {
      initPreferences(jsonString, team);
   }

   public Map<String, Link> getLinks() {
      return linksMap;
   }

   public void setLinks(Map<String, Link> links) {
      this.linksMap = links;
   }

}
