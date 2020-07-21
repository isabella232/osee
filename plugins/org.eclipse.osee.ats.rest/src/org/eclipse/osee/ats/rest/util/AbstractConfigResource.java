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

package org.eclipse.osee.ats.rest.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.IAtsConfigObject;
import org.eclipse.osee.ats.core.util.AtsObjects;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.data.ArtifactTypeToken;
import org.eclipse.osee.framework.core.enums.CoreArtifactTokens;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.search.QueryBuilder;

/**
 * @author Donald G. Dunne
 */
public abstract class AbstractConfigResource {

   protected final AtsApi atsApi;
   private final ArtifactTypeToken artifactType;
   private final QueryBuilder query;
   private final OrcsApi orcsApi;

   public AbstractConfigResource(ArtifactTypeToken artifactType, AtsApi atsApi, OrcsApi orcsApi) {
      this.artifactType = artifactType;
      this.atsApi = atsApi;
      this.orcsApi = orcsApi;
      query = orcsApi.getQueryFactory().fromBranch(CoreBranches.COMMON);
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<ArtifactToken> get() {

      Map<ArtifactId, ArtifactReadable> artifactMap = orcsApi.getQueryFactory().fromBranch(CoreBranches.COMMON).andIds(
         CoreArtifactTokens.OseeConfiguration).asArtifactMap();

      return query.andIsOfType(artifactType).asArtifactTokens();
   }

   @GET
   @Path("details")
   @Produces(MediaType.APPLICATION_JSON)
   public List<IAtsConfigObject> getObjectsJson() {
      List<IAtsConfigObject> configs = new ArrayList<>();
      for (ArtifactToken art : query.andTypeEquals(artifactType).getResults()) {
         configs.add(AtsObjects.getConfigObject(art, atsApi));
      }
      return configs;
   }

   @GET
   @Path("{id}")
   @Produces(MediaType.APPLICATION_JSON)
   public ArtifactToken getObjectJson(@PathParam("id") ArtifactId artifactId) {
      return query.andId(artifactId).asArtifactToken();
   }

   @GET
   @Path("{id}/details")
   @Produces(MediaType.APPLICATION_JSON)
   public IAtsConfigObject getObjectDetails(@PathParam("id") ArtifactId artifactId) {
      return AtsObjects.getConfigObject(atsApi.getQueryService().getArtifact(artifactId), atsApi);
   }
}