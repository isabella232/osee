/*********************************************************************
 * Copyright (c) 2015 Boeing
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

package org.eclipse.osee.framework.core.data;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Donald G. Dunne
 */
public class OseeClient {

   public static final String OSEE_APPLICATION_SERVER = "osee.application.server";
   public static final String OSEE_APPLICATION_SERVER_DATA = "osee.application.server.data";
   public static final int PORT = 8089;
   public static final String DEFAULT_URL = "http://localhost:" + PORT;
   private static String oseeServerUrl;
   public static final String OSEE_ACCOUNT_ID = "osee.account.id";

   private OseeClient() {
      // utility class
   }

   public static synchronized String getOseeApplicationServer() {
      if (oseeServerUrl == null) {
         oseeServerUrl = System.getProperty(OSEE_APPLICATION_SERVER);
         if (oseeServerUrl == null) {
            try {
               oseeServerUrl = InetAddress.getLocalHost().getCanonicalHostName();
            } catch (UnknownHostException ex) {
               oseeServerUrl = DEFAULT_URL;
            }
         }
      }
      return oseeServerUrl;
   }

   public static String getOseeApplicationServerData() {
      return System.getProperty(OSEE_APPLICATION_SERVER_DATA);
   }

   public static int getPort() {
      String[] splitForPort = getOseeApplicationServer().split(":");
      return splitForPort.length == 3 ? Integer.valueOf(splitForPort[2]) : PORT;
   }
}