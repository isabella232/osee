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
package org.eclipse.osee.orcs.rest.model;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.eclipse.osee.orcs.rest.model.writer.config.OrcsWriterInputConfig;
import org.eclipse.osee.orcs.rest.model.writer.reader.OwCollector;

/**
 * @author Donald G. dunne
 */
public interface OrcsWriterEndpoint {

   /**
    * Return Orcs Writer import excel sheet with all available types in reference sheets
    */
   @GET
   @Path("writer/sheet")
   @Produces({MediaType.APPLICATION_XML})
   Response getOrcsWriterInputDefault();

   @GET
   @Path("writer/sheet")
   @Produces({MediaType.APPLICATION_JSON})
   Response getOrcsWriterInputDefaultJson() throws Exception;

   /**
    * Return OrcsWriter import sheet with only specified types shown in reference tabs.<br/>
    * <br/>
    * { "includeArtifactTypes": [ 24, 11 ], "includeAttributeTypes": [ 1152921504606847088, 1152921504606847111 ],
    * "includeRelationTypes": [ { "relationTypeUuid": 2305843009213694292, "sideA": false } ], "includeTokens": [ {
    * "name": "Root Artifact", "uuid": 60807 } ] }
    */
   @POST
   @Path("writer/sheet")
   @Consumes({MediaType.APPLICATION_JSON})
   @Produces({MediaType.APPLICATION_JSON})
   Response getOrcsWriterInputFromConfig(OrcsWriterInputConfig config);

   @POST
   @Path("writer/validate")
   @Consumes({MediaType.APPLICATION_JSON})
   @Produces({MediaType.APPLICATION_JSON})
   Response getOrcsWriterValidate(OwCollector collector);

   @POST
   @Path("writer")
   @Consumes({MediaType.APPLICATION_JSON})
   @Produces({MediaType.APPLICATION_JSON})
   Response getOrcsWriterPersist(OwCollector collector);

   @POST
   @Path("writer/validate/excel")
   Response validateExcelInput(Attachment attachment);

   @POST
   @Path("writer/excel")
   Response persistExcelInput(Attachment attachment);

}
