/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.framework.core.client;

import java.net.InetAddress;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.client.internal.Activator;
import org.eclipse.osee.framework.core.client.server.HttpServer;
import org.eclipse.osee.framework.core.data.OseeCodeVersion;
import org.eclipse.osee.framework.core.data.OseeCredential;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Roberto E. Escobar
 */
public abstract class BaseCredentialProvider implements ICredentialProvider {

   @Override
   public OseeCredential getCredential() {
      OseeCredential credential = new OseeCredential();
      credential.setUserName("");
      credential.setPassword("");
      String localAddress = HttpServer.getServerAddressForExternalCommunication();

      credential.setClientAddress(Strings.isValid(localAddress) ? localAddress : "Unknown");
      credential.setClientPort(String.valueOf(HttpServer.getDefaultServicePort()));
      credential.setClientVersion(OseeCodeVersion.getVersion());
      try {
         credential.setClientAddress(InetAddress.getLocalHost().getHostName());
      } catch (Exception ex) {
         credential.setClientAddress("Unknown");
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return credential;
   }
}
