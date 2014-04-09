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
package org.eclipse.osee.framework.skynet.core.artifact;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.client.server.HttpUrlBuilderClient;
import org.eclipse.osee.framework.core.data.OseeServerContext;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactURL {

   public static URL getExternalArtifactLink(final Artifact artifact) throws OseeCoreException {
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("guid", artifact.getGuid());
      parameters.put("branchUuid", String.valueOf(artifact.getBranch().getUuid()));
      String urlString =
         HttpUrlBuilderClient.getInstance().getPermanentLinkBaseUrl(OseeServerContext.ARTIFACT_CONTEXT, parameters);
      URL url = null;
      try {
         url = new URL(urlString);
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return url;
   }

   public static URL getOpenInOseeLink(final Artifact artifact, String cmd) throws OseeCoreException {
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("sessionId", ClientSessionManager.getSessionId());
      parameters.put("context", "osee/loopback");
      parameters.put("guid", artifact.getGuid());
      parameters.put("branchUuid", String.valueOf(artifact.getBranch().getGuid()));
      parameters.put("isDeleted", String.valueOf(artifact.isDeleted()));
      if (artifact.isHistorical()) {
         parameters.put("transactionId", String.valueOf(artifact.getTransactionNumber()));
      }
      parameters.put("cmd", cmd);
      String urlString =
         HttpUrlBuilderClient.getInstance().getPermanentLinkBaseUrl(OseeServerContext.CLIENT_LOOPBACK_CONTEXT,
            parameters);
      URL url = null;
      try {
         url = new URL(urlString);
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return url;
   }

   public static URL getOpenInOseeLink(final Artifact artifact) throws OseeCoreException {
      return getOpenInOseeLink(artifact, "open.artifact");
   }
}
