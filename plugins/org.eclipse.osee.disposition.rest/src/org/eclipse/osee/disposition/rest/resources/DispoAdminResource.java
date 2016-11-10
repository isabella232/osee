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
package org.eclipse.osee.disposition.rest.resources;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import org.eclipse.osee.disposition.model.CopySetParams;
import org.eclipse.osee.disposition.model.DispoProgram;
import org.eclipse.osee.disposition.model.DispoProgramImpl;
import org.eclipse.osee.disposition.model.DispoSet;
import org.eclipse.osee.disposition.rest.DispoApi;
import org.eclipse.osee.disposition.rest.DispoRoles;
import org.eclipse.osee.disposition.rest.internal.report.ExportSet;
import org.eclipse.osee.disposition.rest.internal.report.STRSReport;

/**
 * @author Angel Avila
 */

public class DispoAdminResource {
   private final DispoApi dispoApi;
   private final DispoProgram program;

   public DispoAdminResource(DispoApi dispoApi, DispoProgram program) {
      this.dispoApi = dispoApi;
      this.program = program;
   }

   @Path("/report")
   @GET
   @RolesAllowed(DispoRoles.ROLES_ADMINISTRATOR)
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   public Response getDispoSetReport(@Encoded @QueryParam("primarySet") String primarySet, @Encoded @QueryParam("secondarySet") String secondarySet) {
      final DispoSet dispoSet = dispoApi.getDispoSetById(program, primarySet);
      final DispoSet dispoSet2 = dispoApi.getDispoSetById(program, secondarySet);
      final STRSReport writer = new STRSReport(dispoApi);

      final String fileName = String.format("STRS_Report_%s", System.currentTimeMillis());

      StreamingOutput streamingOutput = new StreamingOutput() {

         @Override
         public void write(OutputStream outputStream) throws WebApplicationException, IOException {
            writer.runReport(program, dispoSet, dispoSet2, outputStream);
            outputStream.flush();
         }
      };
      String contentDisposition =
         String.format("attachment; filename=\"%s.xml\"; creation-date=\"%s\"", fileName, new Date());
      return Response.ok(streamingOutput).header("Content-Disposition", contentDisposition).type(
         "application/xml").build();
   }

   @Path("/export")
   @GET
   @RolesAllowed(DispoRoles.ROLES_ADMINISTRATOR)
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   public Response postDispoSetExport(@Encoded @QueryParam("primarySet") String primarySet, @QueryParam("option") String option) {
      final DispoSet dispoSet = dispoApi.getDispoSetById(program, primarySet);
      final ExportSet writer = new ExportSet(dispoApi);
      final String options = option;
      Date date = new Date();
      String newstring = new SimpleDateFormat("yyyy-MM-dd").format(date);
      final String fileName = String.format("Coverage_%s", newstring);

      StreamingOutput streamingOutput = new StreamingOutput() {

         @Override
         public void write(OutputStream outputStream) throws WebApplicationException, IOException {
            String dispoType = dispoSet.getDispoType();
            if (dispoType.equals("testScript")) {
               writer.runReport(program, dispoSet, options, outputStream);
            } else {
               writer.runCoverageReport(program, dispoSet, options, outputStream);
            }
            outputStream.flush();
         }
      };
      String contentDisposition =
         String.format("attachment; filename=\"%s.xml\"; creation-date=\"%s\"", fileName, new Date());
      return Response.ok(streamingOutput).header("Content-Disposition", contentDisposition).type(
         "application/xml").build();
   }

   @Path("/copyCoverage")
   @POST
   @RolesAllowed(DispoRoles.ROLES_ADMINISTRATOR)
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public Response getDispoSetCopyCoverage(@QueryParam("destinationSet") String destinationSet, @QueryParam("sourceBranch") Long sourceBranch, @QueryParam("sourcePackage") Long sourcePackage, CopySetParams params) {
      Response.Status status;
      final DispoSet destination = dispoApi.getDispoSetById(program, destinationSet);
      dispoApi.copyDispoSetCoverage(sourceBranch, sourcePackage, program, destination, params);
      status = Status.OK;
      return Response.status(status).build();
   }

   @Path("/copy")
   @POST
   @RolesAllowed(DispoRoles.ROLES_ADMINISTRATOR)
   @Produces(MediaType.APPLICATION_JSON)
   public Response getDispoSetCopy(@QueryParam("destinationSet") String destinationSet, @QueryParam("sourceProgram") String sourceProgram, @QueryParam("sourceSet") String sourceSet, CopySetParams params) {
      Response.Status status;
      final DispoSet destination = dispoApi.getDispoSetById(program, destinationSet);
      DispoProgramImpl sourceDispoProgram = new DispoProgramImpl("", Long.valueOf(sourceProgram));
      final DispoSet source = dispoApi.getDispoSetById(sourceDispoProgram, sourceSet);
      dispoApi.copyDispoSet(program, destination, sourceDispoProgram, source, params);
      status = Status.OK;
      return Response.status(status).build();
   }
}
