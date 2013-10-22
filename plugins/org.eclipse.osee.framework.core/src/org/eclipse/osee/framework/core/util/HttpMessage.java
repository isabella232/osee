/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.translation.IDataTranslationService;
import org.eclipse.osee.framework.core.translation.ITranslatorId;
import org.eclipse.osee.framework.core.util.HttpProcessor.AcquireResult;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;

/**
 * @author Roberto E. Escobar
 */
public final class HttpMessage {

   private HttpMessage() {
      // Utility class
   }

   @SuppressWarnings("unchecked")
   public static <J, K> J send(String urlString, IDataTranslationService service, ITranslatorId requestId, K requestData, ITranslatorId responseId) throws OseeCoreException {
      InputStream inputStream = null;
      try {
         inputStream = service.convertToStream(requestData, requestId);
         ByteArrayOutputStream buffer = new ByteArrayOutputStream();
         AcquireResult result = HttpProcessor.post(new URL(urlString), inputStream, "text/xml", "UTF-8", buffer);
         if (result.wasSuccessful()) {
            if (responseId == null) {
               return (J) result;
            } else {
               return service.convert(new ByteArrayInputStream(buffer.toByteArray()), responseId);
            }
         } else {
            throw new OseeCoreException("Request [%s] failed.", urlString);
         }
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
         return null; // unreachable since wrapAndThrow() always throws an exception
      } finally {
         Lib.close(inputStream);
      }
   }
}
