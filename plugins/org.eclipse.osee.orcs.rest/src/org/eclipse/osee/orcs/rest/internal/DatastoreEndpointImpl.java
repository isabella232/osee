/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.rest.internal;

import static org.eclipse.osee.orcs.rest.internal.OrcsRestUtil.executeCallable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.eclipse.osee.orcs.ApplicationContext;
import org.eclipse.osee.orcs.OrcsAdmin;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.OrcsMetaData;
import org.eclipse.osee.orcs.rest.model.DatastoreEndpoint;
import org.eclipse.osee.orcs.rest.model.DatastoreInfo;
import org.eclipse.osee.orcs.rest.model.DatastoreInitOptions;

/**
 * @author Roberto E. Escobar
 */
public class DatastoreEndpointImpl implements DatastoreEndpoint {

   private final OrcsApi orcsApi;

   @Context
   private UriInfo uriInfo;

   public DatastoreEndpointImpl(OrcsApi orcsApi) {
      this.orcsApi = orcsApi;
   }

   protected void setUriInfo(UriInfo uriInfo) {
      this.uriInfo = uriInfo;
   }

   protected UriInfo getUriInfo() {
      return uriInfo;
   }

   private ApplicationContext newContext() {
      return new ApplicationContext() {

         @Override
         public String getSessionId() {
            return null;
         }
      };
   }

   private OrcsAdmin getOrcsAdmin() {
      return orcsApi.getAdminOps(newContext());
   }

   @Override
   public DatastoreInfo getInfo() {
      OrcsAdmin adminOps = getOrcsAdmin();
      Callable<OrcsMetaData> callable = adminOps.createFetchOrcsMetaData();
      OrcsMetaData metaData = executeCallable(callable);
      return asDatastoreInfo(metaData);
   }

   @Override
   public Response initialize(DatastoreInitOptions options) {
      OrcsAdmin adminOps = getOrcsAdmin();

      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("schema.table.data.namespace", options.getTableDataSpace());
      parameters.put("schema.index.data.namespace", options.getIndexDataSpace());
      parameters.put("schema.user.file.specified.schema.names", Boolean.toString(options.isUseFileSpecifiedSchemas()));

      Callable<OrcsMetaData> callable = adminOps.createDatastore(parameters);
      OrcsMetaData metaData = executeCallable(callable);

      UriInfo uriInfo = getUriInfo();
      URI location = getDatastoreLocation(uriInfo);
      return Response.created(location).entity(asDatastoreInfo(metaData)).build();
   }

   private URI getDatastoreLocation(UriInfo uriInfo) {
      return uriInfo.getRequestUriBuilder().path("../").path("info").build();
   }

   private DatastoreInfo asDatastoreInfo(OrcsMetaData metaData) {
      DatastoreInfo info = new DatastoreInfo();
      info.setProperties(metaData.getProperties());
      return info;
   }

}