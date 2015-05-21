/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.activity.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Ryan D. Brooks
 */
@Path("activity-log")
public interface ActivityLogEndpoint {

   /**
    * Get activity entry data
    *
    * @param entryId activity entry id
    * @return JSON stream containing activity entry data
    */
   @GET
   @Path("/entry/{entry-id}")
   @Produces({MediaType.APPLICATION_JSON})
   ActivityEntry getEntry(@PathParam("entry-id") Long entryId);

   /**
    * Get all activity types
    *
    * @param typeId activity typeId id
    * @return activityType
    */
   @GET
   @Path("/type")
   @Produces({MediaType.APPLICATION_JSON})
   DefaultActivityType[] getActivityTypes();

   /**
    * Get activity type data
    *
    * @param typeId activity typeId id
    * @return activityType
    */
   @GET
   @Path("/type/{type-id}")
   @Produces({MediaType.APPLICATION_JSON})
   DefaultActivityType getActivityType(@PathParam("type-id") Long typeId);

   /**
    * Create a new activity entry
    *
    * @param accountId account id
    * @param clientId client id
    * @param typeId activity type id
    * @param parentId of the parent activity
    * @param status of the activity
    * @param message to log for the activity
    * @return entryId
    */
   @POST
   @Path("/entry")
   @Produces({MediaType.APPLICATION_JSON})
   ActivityEntryId createEntry(@QueryParam("accountId") Long accountId, @QueryParam("clientId") Long clientId, @QueryParam("typeId") Long typeId, @QueryParam("parentId") Long parentId, @QueryParam("status") Integer status, @QueryParam("message") String message);

   /**
    * Create a new activity type
    *
    * @param typeId activity type id to use
    * @param logLevel of activity type
    * @param module this activity comes from
    * @param messageFormat for activity type
    * @return activityType
    */
   @POST
   @Path("/type/{type-id}")
   @Produces({MediaType.APPLICATION_JSON})
   DefaultActivityType createActivityType(@PathParam("type-id") Long typeId, @QueryParam("level") Long logLevel, @QueryParam("module") String module, @QueryParam("messageFormat") String messageFormat);

   /**
    * Create a new activity type (will generate a new type id)
    *
    * @param logLevel of activity type
    * @param module this activity comes from
    * @param messageFormat for activity type
    * @return activityType
    */
   @POST
   @Path("/type")
   @Produces({MediaType.APPLICATION_JSON})
   DefaultActivityType createActivityType(@QueryParam("level") Long logLevel, @QueryParam("module") String module, @QueryParam("messageFormat") String messageFormat);

   /**
    * Modify an entries status
    *
    * @param entryId entry to modify
    * @param statusId to set entry status to
    * @return response whether entry was modified
    * @response.representation.200.doc entry status modified
    * @response.representation.304.doc entry status not modified
    */
   @PUT
   @Path("/entry/{entry-id}/status/{status-id}")
   Response updateEntry(@PathParam("entry-id") Long entryId, @PathParam("status-id") Integer statusId);

}
