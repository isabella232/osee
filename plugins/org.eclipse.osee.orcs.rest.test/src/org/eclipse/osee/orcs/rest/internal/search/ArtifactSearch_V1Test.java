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
package org.eclipse.osee.orcs.rest.internal.search;

import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Collection;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.rest.internal.search.artifact.ArtifactSearch_V1;
import org.eclipse.osee.orcs.rest.internal.search.artifact.PredicateHandler;
import org.eclipse.osee.orcs.rest.internal.search.artifact.predicate.PredicateHandlerUtil;
import org.eclipse.osee.orcs.rest.model.search.artifact.Predicate;
import org.eclipse.osee.orcs.rest.model.search.artifact.SearchMethod;
import org.eclipse.osee.orcs.rest.model.search.artifact.SearchRequest;
import org.eclipse.osee.orcs.rest.model.search.artifact.SearchResponse;
import org.eclipse.osee.orcs.search.QueryBuilder;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Megumi Telles
 */
public class ArtifactSearch_V1Test {

   // @formatter:off
   @Mock private OrcsApi orcsApi;
   @Mock private PredicateHandler handler;
   @Mock private QueryFactory queryFactory;
   @Mock private QueryBuilder builder;
   @Mock private UriInfo uriInfo;
   @Mock private Request request;
   @Captor private ArgumentCaptor<IOseeBranch> fromBranch;
   // @formatter:on

   private static final IOseeBranch BRANCH = CoreBranches.COMMON;
   private final java.util.List<String> types = Arrays.asList("1000000000000070");
   private ArtifactSearch_V1 search;

   @Before
   public void setup() {
      MockitoAnnotations.initMocks(this);
      search = new ArtifactSearch_V1(uriInfo, request, BRANCH.getUuid(), orcsApi);
   }

   @Test
   public void testSearchRequestNull() throws OseeCoreException {
      when(orcsApi.getQueryFactory(null)).thenReturn(queryFactory);
      when(queryFactory.fromBranch(BRANCH)).thenReturn(builder);

      Collection<IAttributeType> attrTypes = PredicateHandlerUtil.getIAttributeTypes(types);
      Predicate predicate = new Predicate(SearchMethod.ATTRIBUTE_TYPE, types, Arrays.asList("AtsAdmin"));
      when(builder.and(attrTypes, predicate.getValues().iterator().next(), predicate.getOptions())).thenReturn(builder);

      SearchRequest params = new SearchRequest(BRANCH.getUuid(), Arrays.asList(predicate), null, 0, false);
      SearchResponse response = search.getSearchWithMatrixParams(params);

      Assert.assertEquals(response.getSearchRequest(), params);
   }

}
