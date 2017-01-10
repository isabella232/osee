/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.rest.model;

import java.util.HashMap;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.osee.framework.core.data.ApplicabilityId;
import org.eclipse.osee.framework.core.data.ApplicabilityToken;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.FeatureDefinitionData;

/**
 * @author Donald G. Dunne
 */
@Path("applic")
public interface ApplicabilityEndpoint {

   @GET
   @Produces({MediaType.APPLICATION_JSON})
   List<ApplicabilityToken> getApplicabilityTokens();

   @GET
   @Path("feature/data")
   @Produces({MediaType.APPLICATION_JSON})
   List<FeatureDefinitionData> getFeatureDefinitionData();

   @GET
   @Path("view/{viewId}")
   @Produces({MediaType.APPLICATION_JSON})
   List<ApplicabilityToken> getViewApplicabilityTokens(@PathParam("viewId") ArtifactId view);

   /**
    * Set the applicabilities referenced by the provided artifacts. This is stored in the tuple table which means it
    * does not impact applicability in a branch view.
    */
   @PUT
   @Path("artifact/reference")
   @Consumes({MediaType.APPLICATION_JSON})
   @Produces({MediaType.APPLICATION_JSON})
   Response setApplicabilityReference(HashMap<ArtifactId, List<ApplicabilityId>> artifacts);

   @GET
   @Path("artifact/reference/{artId}")
   @Consumes({MediaType.APPLICATION_JSON})
   @Produces({MediaType.APPLICATION_JSON})
   List<ApplicabilityId> getApplicabilitiesReferenced(@PathParam("artId") ArtifactId artifact);

   @GET
   @Path("artifact/reference/token/{artId}")
   @Consumes({MediaType.APPLICATION_JSON})
   @Produces({MediaType.APPLICATION_JSON})
   List<ApplicabilityToken> getApplicabilityReferenceTokens(@PathParam("artId") ArtifactId artifact);

   /**
    * Set the applicability in osee_txs for the given artifacts. This affects whether the artifact is included in a
    * branch view.
    */
   @PUT
   @Path("{applicId}")
   @Consumes({MediaType.APPLICATION_JSON})
   @Produces({MediaType.APPLICATION_JSON})
   Response setApplicability(@PathParam("applicId") ApplicabilityId applicId, List<? extends ArtifactId> artifacts);

   @POST
   void createDemoApplicability();

   @GET
   @Path("artifact/{artId}")
   @Consumes({MediaType.APPLICATION_JSON})
   @Produces({MediaType.APPLICATION_JSON})
   ApplicabilityToken getApplicabilityToken(@PathParam("artId") ArtifactId artId);

   @PUT
   @Consumes({MediaType.APPLICATION_JSON})
   @Produces(MediaType.APPLICATION_JSON)
   List<ApplicabilityToken> getApplicabilityTokensForArts(List<ArtifactId> artIds);

}