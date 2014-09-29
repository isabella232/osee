/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.disposition.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import javax.ws.rs.core.Response;
import org.eclipse.osee.disposition.model.DispoProgram;
import org.eclipse.osee.disposition.rest.DispoApi;
import org.eclipse.osee.disposition.rest.util.DispoFactory;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.framework.jdk.core.type.ResultSets;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Angel Avila
 */
public class DispoProgramResourceTest {

   @Mock
   private DispoApi dispoApi;
   @Mock
   private DispoProgram id1;
   @Mock
   private DispoProgram id2;
   @Mock
   private DispoFactory dispoFactory;

   private DispoProgramResource resource;

   @Before
   public void setUp() {
      MockitoAnnotations.initMocks(this);
      resource = new DispoProgramResource(dispoApi, dispoFactory);
      when(id1.getUuid()).thenReturn(23L);
      when(id2.getUuid()).thenReturn(25L);
   }

   @Test
   public void testGetAll() throws JSONException {
      // No Sets
      ResultSet<IOseeBranch> emptyResultSet = ResultSets.emptyResultSet();
      when(dispoApi.getDispoPrograms()).thenReturn(emptyResultSet);
      Response noProgramsResponse = resource.getAllPrograms();
      String messageActual = (String) noProgramsResponse.getEntity();
      assertEquals(Response.Status.NOT_FOUND.getStatusCode(), noProgramsResponse.getStatus());
      assertEquals("[]", messageActual);

      IOseeBranch branch = TokenFactory.createBranch(id1.getUuid(), "testBranch");
      ResultSet<IOseeBranch> branchList = ResultSets.singleton(branch);

      when(dispoApi.getDispoPrograms()).thenReturn(branchList);
      Response oneSetResponse = resource.getAllPrograms();
      JSONArray entity = new JSONArray((String) oneSetResponse.getEntity());
      JSONObject programFromEntity = entity.getJSONObject(0);
      assertEquals(Response.Status.OK.getStatusCode(), oneSetResponse.getStatus());
      assertEquals(String.valueOf(id1.getUuid()), programFromEntity.getString("value"));
   }
}
