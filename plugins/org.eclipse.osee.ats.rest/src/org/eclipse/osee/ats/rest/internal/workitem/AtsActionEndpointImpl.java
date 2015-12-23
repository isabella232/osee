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
package org.eclipse.osee.ats.rest.internal.workitem;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.team.ChangeType;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.workflow.AtsActionEndpointApi;
import org.eclipse.osee.ats.api.workflow.IAtsAction;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.rest.IAtsServer;
import org.eclipse.osee.ats.rest.internal.util.RestUtil;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.jaxrs.mvc.IdentityView;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Donald G. Dunne
 */
@Path("action")
public final class AtsActionEndpointImpl implements AtsActionEndpointApi {

   private final IAtsServer atsServer;
   private final OrcsApi orcsApi;
   private static final String ATS_UI_ACTION_PREFIX = "/ui/action/UUID";

   public AtsActionEndpointImpl(IAtsServer atsServer, OrcsApi orcsApi) {
      this.atsServer = atsServer;
      this.orcsApi = orcsApi;
   }

   @Override
   @GET
   @Produces(MediaType.TEXT_HTML)
   public String get() throws Exception {
      return RestUtil.simplePageHtml("Action Resource");
   }

   /**
    * @param ids (guid, atsId) of action to display
    * @return html representation of the action
    */
   @Override
   @Path("{ids}")
   @IdentityView
   @GET
   public List<IAtsWorkItem> getAction(@PathParam("ids") String ids) throws Exception {
      List<IAtsWorkItem> workItems = atsServer.getWorkItemListByIds(ids);
      return workItems;
   }

   /**
    * @param ids (guid, atsId) of action to display
    * @return html representation of the action
    */
   @Override
   @Path("{ids}/details")
   @GET
   @Produces({MediaType.APPLICATION_JSON})
   public List<IAtsWorkItem> getActionDetails(@PathParam("ids") String ids) throws Exception {
      List<IAtsWorkItem> workItems = atsServer.getWorkItemListByIds(ids);
      return workItems;
   }

   /**
    * @query_string <attr type name>=<value>, <attr type id>=<value>
    * @return json representation of the matching workItem(s)
    */
   @Override
   @Path("query")
   @GET
   @Produces({MediaType.APPLICATION_JSON})
   public Set<IAtsWorkItem> query(@Context UriInfo uriInfo) throws Exception {
      MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters(true);
      Set<IAtsWorkItem> workItems = new HashSet<>();
      for (String key : queryParameters.keySet()) {
         IAttributeType attrType = null;
         Long attrTypeId = Strings.isNumeric(key) ? Long.valueOf(key) : null;
         for (IAttributeType type : atsServer.getOrcsApi().getOrcsTypes().getAttributeTypes().getAll()) {
            if (attrTypeId != null && type.getGuid().equals(attrTypeId)) {
               attrType = type;
               break;
            } else if (type.getName().equals(key)) {
               attrType = type;
               break;
            }
         }
         if (attrType != null) {
            for (String value : queryParameters.get(key)) {
               Iterator<ArtifactReadable> iterator =
                  atsServer.getOrcsApi().getQueryFactory().fromBranch(AtsUtilCore.getAtsBranch()).and(attrType,
                     value).getResults().iterator();
               while (iterator.hasNext()) {
                  ArtifactReadable artifactReadable = iterator.next();
                  if (artifactReadable.isOfType(AtsArtifactTypes.AbstractWorkflowArtifact)) {
                     IAtsWorkItem workItem = atsServer.getWorkItemFactory().getWorkItem(artifactReadable);
                     if (workItem != null) {
                        workItems.add(workItem);
                     }
                  }
               }
            }
         }

      }
      return workItems;
   }

   /**
    * @param form containing information to create a new action
    * @param form.ats_title - (required) title of new action
    * @param form.desc - description of the action
    * @param form.actionableItems - (required) actionable item name
    * @param form.changeType - (required) Improvement, Refinement, Problem, Support
    * @param form.priority - (required) 1-5
    * @param form.userId - (required)
    * @return html representation of action created
    */
   @Override
   @POST
   @Consumes("application/x-www-form-urlencoded")
   public Response createAction(MultivaluedMap<String, String> form) throws Exception {
      // validate title
      String title = form.getFirst("ats_title");
      if (!Strings.isValid(title)) {
         return RestUtil.returnBadRequest("title is not valid");
      }

      // description is optional
      String description = form.getFirst("desc");

      // validate actionableItemName
      String actionableItems = form.getFirst("actionableItems");
      if (!Strings.isValid(actionableItems)) {
         return RestUtil.returnBadRequest("actionableItems is not valid");
      }
      List<IAtsActionableItem> aias = new ArrayList<>();
      ArtifactReadable aiArt = atsServer.getQuery().andTypeEquals(AtsArtifactTypes.ActionableItem).andNameEquals(
         actionableItems).getResults().getOneOrNull();
      if (aiArt == null) {
         return RestUtil.returnBadRequest(String.format("actionableItems [%s] is not valid", actionableItems));
      }
      IAtsActionableItem aia = (IAtsActionableItem) atsServer.getConfig().getSoleByUuid(aiArt.getUuid());
      aias.add(aia);

      // validate userId
      String userId = form.getFirst("userId");
      if (!Strings.isValid(userId)) {
         return RestUtil.returnBadRequest("userId is not valid");
      }
      IAtsUser atsUser = atsServer.getUserService().getUserById(userId);
      if (atsUser == null) {
         return RestUtil.returnBadRequest(String.format("userId [%s] is not valid", userId));
      }

      // validate changeType
      String changeTypeStr = form.getFirst("changeType");
      if (!Strings.isValid(changeTypeStr)) {
         return RestUtil.returnBadRequest("changeType is not valid");
      }
      IAtsChangeSet changes = atsServer.getStoreService().createAtsChangeSet("Create Action - Server", atsUser);
      orcsApi.getTransactionFactory().createTransaction(AtsUtilCore.getAtsBranch(),
         (ArtifactReadable) atsUser.getStoreObject(), "Create Action - Server");

      ChangeType changeType = null;
      try {
         changeType = ChangeType.valueOf(changeTypeStr);
      } catch (Exception ex) {
         return RestUtil.returnBadRequest(String.format("changeType [%s] is not valid", changeTypeStr));
      }

      // validate priority
      String priority = form.getFirst("priority");
      if (!Strings.isValid(priority)) {
         return RestUtil.returnBadRequest("priority is not valid");
      } else if (!priority.matches("[0-5]{1}")) {
         return RestUtil.returnBadRequest(String.format("priority [%s] is not valid", priority));
      }

      // create action
      IAtsAction action = atsServer.getActionFactory().createAction(atsUser, title, description, changeType, priority,
         false, null, aias, new Date(), atsUser, null, changes).getFirst();
      changes.execute();

      // Redirect to action ui
      return RestUtil.redirect(action.getTeamWorkflows(), ATS_UI_ACTION_PREFIX, atsServer);
   }

}
