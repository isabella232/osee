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
package org.eclipse.osee.ats.core.workflow.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.workflow.WorkItemType;
import org.eclipse.osee.ats.core.internal.AtsApiService;
import org.eclipse.osee.framework.jdk.core.util.Lib;

/**
 * @author Donald G. Dunne
 */
@Provider
public class WorkItemJsonReader implements MessageBodyReader<IAtsWorkItem> {

   @Override
   public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
      return type == IAtsWorkItem.class;
   }

   @Override
   public IAtsWorkItem readFrom(Class<IAtsWorkItem> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
      try {
         String jsonStr = Lib.inputStreamToString(entityStream);

         List<Long> workItems = WorkItemsJsonReader.getWorkItemIdsFromJson(jsonStr);

         return AtsApiService.get().getQueryService().createQuery(WorkItemType.WorkItem).andIds(
            workItems.iterator().next()).getResults().getExactlyOne();
      } catch (Exception ex) {
         throw new IOException("Error deserializing a TraxRpcr Item.", ex);
      }
   }
}