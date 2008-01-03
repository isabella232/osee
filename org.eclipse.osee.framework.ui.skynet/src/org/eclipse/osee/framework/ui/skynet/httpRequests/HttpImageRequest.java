/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.httpRequests;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.eclipse.osee.framework.skynet.core.linking.HttpRequest;
import org.eclipse.osee.framework.skynet.core.linking.HttpResponse;
import org.eclipse.osee.framework.skynet.core.linking.IHttpServerRequest;
import org.eclipse.osee.framework.ui.skynet.artifact.snapshot.ArtifactSnapshotManager;

/**
 * @author Roberto E. Escobar
 */
public class HttpImageRequest implements IHttpServerRequest {
   private static HttpImageRequest instance = null;

   private static final String IMAGE_KEY = "image";
   private static final String NAMESPACE_KEY = "namespace";
   private static final String KEY_KEY = "key";
   private static final String REQUEST_TYPE = "HTTP.IMAGE";

   private HttpImageRequest() {
   }

   public static HttpImageRequest getInstance() {
      if (instance == null) {
         instance = new HttpImageRequest();
      }
      return instance;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.osee.framework.skynet.core.linking.IHttpServerRequest#getRequestType()
    */
   public String getRequestType() {
      return REQUEST_TYPE;
   }

   public String getRequestUrl(String namespace, String key, String imageKey) throws UnsupportedEncodingException {
      StringBuffer toReturn = new StringBuffer(REQUEST_TYPE + "?");
      toReturn.append(NAMESPACE_KEY);
      toReturn.append("=");
      toReturn.append(URLEncoder.encode(namespace, "UTF-8"));
      toReturn.append("&");
      toReturn.append(KEY_KEY);
      toReturn.append("=");
      toReturn.append(URLEncoder.encode(key, "UTF-8"));
      toReturn.append("&");
      toReturn.append(IMAGE_KEY);
      toReturn.append("=");
      toReturn.append(URLEncoder.encode(imageKey, "UTF-8"));
      return toReturn.toString();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.osee.framework.skynet.core.linking.IHttpServerRequest#processRequest(java.io.DataOutputStream,
    *      java.util.Map, java.lang.String)
    */
   public void processRequest(HttpRequest httpRequest, HttpResponse httpResponse) {
      String imageKey = httpRequest.getParameter(IMAGE_KEY);
      String namespace = httpRequest.getParameter(NAMESPACE_KEY);
      String key = httpRequest.getParameter(KEY_KEY);
      try {
         ArtifactSnapshotManager.getInstance().getImageSnapshot(namespace, key, imageKey,
               httpResponse.getOutputStream());
      } catch (Exception ex) {
         httpResponse.outputStandardError(404, String.format("Error with image [%s::%s]. %s", namespace, key, imageKey,
               ex.toString()));
      }
   }
}
