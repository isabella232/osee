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
package org.eclipse.osee.framework.skynet.core.httpRequests;

import java.util.Set;
import javax.ws.rs.core.Response.Status;
import org.eclipse.osee.define.api.RenderEndpoint;
import org.eclipse.osee.define.api.WordTemplateContentData;
import org.eclipse.osee.define.api.WordUpdateChange;
import org.eclipse.osee.define.api.WordUpdateData;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;
import org.eclipse.osee.jaxrs.OseeWebApplicationException;

/**
 * @author David W. Miller
 */
public class HttpWordUpdateRequest {
   public static WordUpdateChange updateWordArtifacts(WordUpdateData wud) {
      RenderEndpoint endpoint = ServiceUtil.getOseeClient().getRenderEndpoint();
      try {
         return endpoint.updateWordArtifacts(wud);
      } catch (Exception ex) {
         throw new OseeWebApplicationException(ex, Status.INTERNAL_SERVER_ERROR, "Exception in WordUpdateRequest");
      }
   }

   public static Pair<String, Set<String>> renderWordTemplateContent(WordTemplateContentData data) {
      RenderEndpoint endpoint = ServiceUtil.getOseeClient().getRenderEndpoint();
      return endpoint.renderWordTemplateContent(data);
   }

}
