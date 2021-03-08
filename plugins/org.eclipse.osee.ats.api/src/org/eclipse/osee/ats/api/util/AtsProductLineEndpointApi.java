/*********************************************************************
 * Copyright (c) 2021 Boeing
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

package org.eclipse.osee.ats.api.util;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.osee.framework.core.data.BranchId;

/**
 * @author Audrey E Denk
 */
@Path("ple")
public interface AtsProductLineEndpointApi {

   @GET
   @Path("branches/{branchQueryType}")
   @Produces(MediaType.APPLICATION_JSON)
   public List<BranchId> getBranches(@PathParam("branchQueryType") String branchQueryType);

}