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

package org.eclipse.osee.framework.jdk.core.util.network;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import org.eclipse.osee.framework.jdk.core.util.OseeProperties;

/**
 * @author Andrew M. Finkbeiner
 */
public class PortUtil {

   private static PortUtil singleton;
   @SuppressWarnings("unused")
   private ServerSocket ss;

   public static PortUtil getInstance() {
      if (singleton == null) {
         singleton = new PortUtil();
      }
      return singleton;
   }

   int basePort = 18000;
   int nextPort = 18000;

   private PortUtil() {
      basePort = nextPort = OseeProperties.getOseePortScannerStartPort();
      for (int j = nextPort; j < 64000; j += 250) {
         if (checkIfPortIsTaken(j)) {
            basePort = nextPort = j;
            try {
               ss = new ServerSocket(basePort);
            } catch (IOException e) {
               e.printStackTrace();
            }
            break;
         }
      }
   }

   public void computeNewBasePort() {
      basePort = nextPort = basePort + 1000;
      for (int j = nextPort; j < 64000; j += 250) {
         if (checkIfPortIsTaken(j)) {
            basePort = nextPort = j;
            try {
               ss = new ServerSocket(basePort);
            } catch (IOException e) {
               e.printStackTrace();
            }
            break;
         }
      }
   }

   public int getValidPort() throws IOException {
      int port = getConsecutiveValidPorts(1);
      return port;
   }

   public int getConsecutiveValidPorts(int numberOfPorts) throws IOException {
      try {
         int returnVal = getConsecutiveLocalPorts(numberOfPorts);
         nextPort = returnVal + numberOfPorts;
         return returnVal;
      } catch (Exception e) {
         e.printStackTrace();
         IOException ioE = new IOException("Unable to get a valid port.");
         ioE.initCause(e);
         throw ioE;
      }
   }

   /**
    * @param numberOfPorts The number of consecutive available ports to find
    * @return The port of first number in the sequence of valid ports
    */
   private int getConsecutiveLocalPorts(int numberOfPorts) throws Exception {
      if (nextPort >= basePort + 250 - numberOfPorts) {
         nextPort = basePort;
      }
      for (int i = nextPort; i < basePort + 250; i++) {
         boolean passed = true;
         for (int j = i; j < numberOfPorts + i; j++) {
            if (!checkIfPortIsTaken(j)) {
               passed = false;
               break;
            }
         }
         if (passed) {
            return i;
         }
      }
      throw new Exception("Unable to find valid port.");
   }

   private boolean checkIfPortIsTaken(int port) {
      return checkTcpIp(port) && checkUdpPort(port);
   }

   private boolean checkTcpIp(int port) {
      try {
         ServerSocket socket;
         socket = new ServerSocket(port);
         socket.close();
      } catch (Exception e) {
         return false;
      }
      return true;
   }

   private boolean checkUdpPort(int port) {
      try {
         DatagramSocket ds = new DatagramSocket(port, InetAddress.getLocalHost());
         ds.close();
         ds.disconnect();
      } catch (Exception e) {
         return false;
      }
      return true;
   }
}
