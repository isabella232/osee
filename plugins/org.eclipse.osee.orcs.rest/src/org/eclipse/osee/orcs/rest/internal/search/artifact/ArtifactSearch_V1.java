/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.rest.internal.search.artifact;

import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import org.eclipse.osee.framework.core.data.HasLocalId;
import org.eclipse.osee.framework.jdk.core.type.MatchLocation;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.AttributeReadable;
import org.eclipse.osee.orcs.rest.internal.search.artifact.dsl.DslFactory;
import org.eclipse.osee.orcs.rest.internal.search.artifact.dsl.SearchQueryBuilder;
import org.eclipse.osee.orcs.rest.model.search.artifact.RequestType;
import org.eclipse.osee.orcs.rest.model.search.artifact.SearchMatch;
import org.eclipse.osee.orcs.rest.model.search.artifact.SearchRequest;
import org.eclipse.osee.orcs.rest.model.search.artifact.SearchResponse;
import org.eclipse.osee.orcs.search.Match;
import org.eclipse.osee.orcs.search.QueryBuilder;
import org.eclipse.osee.orcs.search.QueryFactory;

/**
 * @author John R. Misinco
 * @author Roberto E. Escobar
 */
public class ArtifactSearch_V1 extends ArtifactSearch {

   private final SearchQueryBuilder searchQueryBuilder;
   private final OrcsApi orcsApi;

   public ArtifactSearch_V1(UriInfo uriInfo, Request request, Long branchUuid, OrcsApi orcsApi) {
      super(uriInfo, request, branchUuid);
      this.orcsApi = orcsApi;
      searchQueryBuilder = DslFactory.createQueryBuilder();
   }

   @POST
   @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
   @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
   public SearchResponse getSearchWithMatrixParams(SearchRequest parameters) throws OseeCoreException {
      return search(parameters);
   }

   private SearchResponse search(SearchRequest params) throws OseeCoreException {
      long startTime = System.currentTimeMillis();

      QueryFactory qFactory = orcsApi.getQueryFactory(); // Fix this

      QueryBuilder builder = searchQueryBuilder.build(qFactory, params);

      builder.includeDeletedArtifacts(params.isIncludeDeleted());

      if (params.getFromTx() > 0) {
         builder.fromTransaction(params.getFromTx());
      }

      SearchResponse result = new SearchResponse();
      RequestType request = params.getRequestType();
      if (request != null) {
         List<Integer> localIds = new LinkedList<Integer>();
         switch (request) {
            case COUNT:
               int total = builder.getCount();
               result.setTotal(total);
               break;
            case IDS:
               for (HasLocalId<Integer> art : builder.getResultsAsLocalIds()) {
                  localIds.add(art.getLocalId());
               }
               result.setIds(localIds);
               result.setTotal(localIds.size());
               break;
            case MATCHES:
               ResultSet<Match<ArtifactReadable, AttributeReadable<?>>> matches = builder.getMatches();
               List<SearchMatch> searchMatches = new LinkedList<SearchMatch>();
               for (Match<ArtifactReadable, AttributeReadable<?>> match : matches) {
                  int artId = match.getItem().getLocalId();
                  localIds.add(artId);
                  for (AttributeReadable<?> attribute : match.getElements()) {
                     int attrId = attribute.getLocalId();
                     List<MatchLocation> locations = match.getLocation(attribute);
                     searchMatches.add(new SearchMatch(artId, attrId, locations));
                  }
               }
               result.setIds(localIds);
               result.setMatches(searchMatches);
               result.setTotal(searchMatches.size());
               break;
            default:
               throw new UnsupportedOperationException();
         }
      }
      result.setSearchRequest(params);
      result.setSearchTime(System.currentTimeMillis() - startTime);
      return result;
   }
}
