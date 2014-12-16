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
package org.eclipse.osee.ats.api.workflow;

import java.util.List;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.jaxrs.mvc.IdentityView;

/**
 * @author Donald G. Dunne
 */
public interface AtsActionEndpointApi {

   @GET
   @Produces(MediaType.TEXT_HTML)
   public abstract String get() throws Exception;

   /**
    * @param ids (guid, atsId) of action to display
    * @return html representation of the action
    */
   @Path("{ids}")
   @IdentityView
   @GET
   @Produces({MediaType.APPLICATION_JSON})
   public abstract List<IAtsWorkItem> getAction(String ids) throws Exception;

   /**
    * @param ids (guid, atsId) of action to display
    * @return html representation of the action
    */
   @Path("{ids}/details")
   @GET
   public abstract List<IAtsWorkItem> getActionDetails(String ids) throws Exception;

   /**
    * @query_string <attr type name>=<value>, <attr type id>=<value>
    * @return json representation of the matching workItem(s)
    */
   @Path("query")
   @GET
   public abstract Set<IAtsWorkItem> query(UriInfo uriInfo) throws Exception;

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
   @POST
   @Consumes("application/x-www-form-urlencoded")
   public abstract Response createAction(MultivaluedMap<String, String> form) throws Exception;

}