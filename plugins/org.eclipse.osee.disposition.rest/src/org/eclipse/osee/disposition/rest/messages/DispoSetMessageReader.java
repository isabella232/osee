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
package org.eclipse.osee.disposition.rest.messages;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import org.eclipse.osee.disposition.model.DispoSetData;
import org.eclipse.osee.disposition.rest.util.DispoUtil;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.json.JSONObject;

/**
 * @author Angel Avila
 */
public class DispoSetMessageReader implements MessageBodyReader<DispoSetData> {

   @Override
   public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
      return type == DispoSetData.class;
   }

   @Override
   public DispoSetData readFrom(Class<DispoSetData> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
      String inputStreamToString = Lib.inputStreamToString(entityStream);
      try {
         JSONObject jsonObject = new JSONObject(inputStreamToString);
         DispoSetData dispoSet = DispoUtil.jsonObjToDispoSet(jsonObject);
         return dispoSet;
      } catch (Exception ex) {
         throw new IOException("Error deserializing a Disposition Set.", ex);
      }
   }
}
