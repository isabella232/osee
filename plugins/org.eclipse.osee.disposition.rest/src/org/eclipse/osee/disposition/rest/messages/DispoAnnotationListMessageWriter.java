/*********************************************************************
 * Copyright (c) 2016 Boeing
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

package org.eclipse.osee.disposition.rest.messages;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import org.eclipse.osee.disposition.model.DispoAnnotationData;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.jaxrs.JaxRsApi;

/**
 * @author Angel Avila
 */
public class DispoAnnotationListMessageWriter implements MessageBodyWriter<List<DispoAnnotationData>> {
   private final JaxRsApi jaxRsApi;

   public DispoAnnotationListMessageWriter(JaxRsApi jaxRsApi) {
      this.jaxRsApi = jaxRsApi;
   }

   @Override
   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
      if (genericType instanceof ParameterizedType) {
         return ((ParameterizedType) genericType).getActualTypeArguments()[0] == DispoAnnotationData.class;
      } else {
         return false;
      }
   }

   @Override
   public long getSize(List<DispoAnnotationData> dispoAnnotations, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
      return -1;
   }

   @Override
   public void writeTo(List<DispoAnnotationData> dispoAnnotations, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
      String jsonString = jaxRsApi.toJson(dispoAnnotations);
      entityStream.write(jsonString.getBytes(Strings.UTF_8));
   }
}