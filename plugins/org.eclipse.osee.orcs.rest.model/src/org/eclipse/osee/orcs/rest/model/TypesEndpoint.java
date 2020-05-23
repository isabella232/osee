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

package org.eclipse.osee.orcs.rest.model;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.osee.framework.core.data.OrcsTypesVersion;

/**
 * @author Roberto E. Escobar
 */
@Path("types")
public interface TypesEndpoint {

   @GET
   @Produces({OrcsMediaType.APPLICATION_ORCS_TYPES, MediaType.TEXT_PLAIN})
   Response getTypes();

   @GET
   @Path("config")
   @Produces({MediaType.APPLICATION_JSON})
   Response getConfig();

   @GET
   @Path("config/sheet")
   @Produces({MediaType.APPLICATION_JSON})
   Response getConfigSheets();

   @POST
   @Path("config/sheet")
   @Consumes({MediaType.APPLICATION_JSON})
   @Produces({MediaType.APPLICATION_JSON})
   Response setConfigSheets(OrcsTypesVersion version);

   @GET
   @Path("attribute/enum")
   @Produces({MediaType.APPLICATION_JSON})
   Response getEnums();

   @GET
   @Path("attribute/enum/{uuid}")
   @Produces({MediaType.APPLICATION_JSON})
   Response getEnums(@PathParam("uuid") Long uuid);

   @GET
   @Path("attribute/enum/{uuid}/entry")
   @Produces({MediaType.APPLICATION_JSON})
   Response getEnumEntries(@PathParam("uuid") Long uuid);

   @POST
   @Path("invalidate-caches")
   Response invalidateCaches();
}