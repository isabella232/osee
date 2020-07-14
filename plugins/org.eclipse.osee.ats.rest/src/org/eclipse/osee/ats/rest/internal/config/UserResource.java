/*********************************************************************
 * Copyright (c) 2014 Boeing
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

package org.eclipse.osee.ats.rest.internal.config;

import java.util.HashMap;
import java.util.LinkedList;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.eclipse.osee.ats.api.user.AtsUser;
import org.eclipse.osee.ats.api.user.IAtsUserService;
import org.eclipse.osee.framework.core.JaxRsApi;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.jaxrs.client.JaxRsApiImpl;

/**
 * @author Donald G. Dunne
 */
@Path("user")
public final class UserResource {

   public static void Main(String[] args) {
      String s = jaxRsApi.toJson("");
      System.out.println("s: " + s);
   }

   private final IAtsUserService userService;
   private static JaxRsApi jaxRsApi;

   public UserResource(IAtsUserService userService) {
      this.userService = userService;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public String get(@QueryParam("active") String activeStr) throws Exception {
      Active active = Active.Both;
      if (Strings.isValid(activeStr)) {
         active = Active.valueOf(activeStr);
      }

      String array = "[";
      //String s = jaxRsApi.toJson("");
      //   JSONArray arr = new JSONArray();
      JaxRsApiImpl impl = new JaxRsApiImpl();
      impl.start();
      String implJson = "";
      HashMap jsonArray = new HashMap();
      //   Hashtable ha[] = new Hashtable[23];
      //List<Hashtable> jsonList = new LinkedList<Hashtable>();
      LinkedList jsonList = new LinkedList();
      System.out.println("GET MAPPER: " + impl.getObjectMapper());
      //  System.out.println("READ TREE: " + impl.readTree("\"id\" : 897").toString());
      int i = 0;
      //  jsonArray.clear();
      //jsonList.clear();
      for (AtsUser user : userService.getUsers(active)) {
         // JSONObject obj = new JSONObject();
         System.out.println("beginning of for: " + jsonList);
         System.out.println("beginning of for jsonarray: " + jsonArray);

         //   obj.put("id", user.getUserId());
         //   obj.put("name", user.getName());
         //   obj.put("email", user.getEmail());
         //   obj.put("active", user.isActive());
         //   obj.put("accountId", user.getStoreObject().getId());
         jsonArray.put("id", user.getUserId());
         jsonArray.put("name", user.getName());
         System.out.println("middle of for: " + jsonList);
         System.out.println("middle of for jsonarray: " + jsonArray);

         jsonArray.put("email", user.getEmail());
         jsonArray.put("active", user.isActive());
         jsonArray.put("accountId", user.getStoreObject().getId());
         String jsonString = impl.toJson(jsonArray);
         System.out.println("JSOOOOOOOOONNNNNNNN: " + jsonString);
         //  array += "{";
         //   array += "id:" + user.getUserId() + ",";
         //  array += "name:" + user.getName() + ",";
         //   array += "email:" + user.getEmail() + ",";
         //   array += "active:" + user.isActive() + ",";
         //  array += "accountId:" + user.getStoreObject().getId();
         // array += "}";
         //  implJson += impl.toJson("id:" + user.getUserId() + ",");
         //   implJson += impl.toJson("name:" + user.getName() + ",");
         //   implJson += impl.toJson("email:" + user.getEmail() + ",");
         //  implJson += impl.toJson("active:" + user.isActive() + ",");
         //  implJson += impl.toJson("accountId:" + user.getStoreObject().getId());
         //  arr.put(obj);
         //   impl.toJson
         System.out.println("user: " + jsonArray);
         //  jsonList.add(i, jsonArray);

         System.out.println("jsonlist before: " + jsonList);
         // ha[i] = jsonArray;
         // jsonList.add(0, jsonArray);
         jsonList.add(jsonString);
         System.out.println("jsonlist after: " + jsonList);
         i++;
      }
      array += "]";
      // json = json.replaceAll("name", "\"name\"");
      //  json = json.replaceAll("id", "\"id\"");

      System.out.println("json json" + jsonArray.toString());
      System.out.println("OBBBBBBBBJJJJ to string " + jsonList.toString());
      implJson = impl.toJson(jsonList);
      //  System.out.println("OBBBBBBBBJJJJ to string 2 " + ha[2]);

      System.out.println("OBBBBBBBBJJJJ " + jsonList.toString());

      return jsonList.toString();
   }
}