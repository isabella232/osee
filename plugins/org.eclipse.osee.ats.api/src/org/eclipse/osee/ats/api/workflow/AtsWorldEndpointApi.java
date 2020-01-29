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
package org.eclipse.osee.ats.api.workflow;

import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.nebula.widgets.xviewer.core.model.CustomizeData;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.query.AtsSearchData;
import org.eclipse.osee.framework.jdk.core.result.ResultRows;

/**
 * @author Donald G. Dunne
 */
@Path("world")
public interface AtsWorldEndpointApi {

   @GET
   @Path("cust/global")
   @Produces(MediaType.APPLICATION_JSON)
   Collection<CustomizeData> getCustomizationsGlobal() throws Exception;

   @GET
   @Path("cust")
   @Produces(MediaType.APPLICATION_JSON)
   Collection<CustomizeData> getCustomizations() throws Exception;

   @GET
   @Path("my/{id}")
   @Produces(MediaType.APPLICATION_JSON)
   Collection<IAtsWorkItem> getMyWorld(int id) throws Exception;

   @GET
   @Path("my/{id}/ui")
   @Produces(MediaType.TEXT_HTML)
   String getMyWorldUI(int id) throws Exception;

   @GET
   @Path("my/{id}/ui/{customize_guid}")
   @Produces(MediaType.TEXT_HTML)
   String getMyWorldUICustomized(int id, String customize_guid) throws Exception;

   @GET
   @Path("coll/{id}")
   @Produces(MediaType.APPLICATION_JSON)
   Collection<IAtsWorkItem> getCollection(long id) throws Exception;

   @GET
   @Path("coll/{id}/ui")
   @Produces(MediaType.TEXT_HTML)
   String getCollectionUI(long id) throws Exception;

   @GET
   @Path("coll/{id}/ui/{customize_guid}")
   @Produces(MediaType.TEXT_HTML)
   String getCollectionUICustomized(long id, String customize_guid) throws Exception;

   @GET
   @Path("search")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   ResultRows search(AtsSearchData atsSearchData);

}