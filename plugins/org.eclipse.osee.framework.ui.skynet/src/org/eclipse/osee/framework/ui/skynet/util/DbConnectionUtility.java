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
package org.eclipse.osee.framework.ui.skynet.util;

import java.util.Collection;
import java.util.Collections;
import org.eclipse.osee.framework.core.client.OseeClientProperties;
import org.eclipse.osee.framework.core.data.OseeCodeVersion;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.ui.plugin.OseeUiActivator;
import org.eclipse.osee.framework.ui.skynet.internal.ServiceUtil;
import org.eclipse.osee.jaxrs.client.JaxRsExceptions;
import org.eclipse.osee.orcs.rest.client.OseeClient;
import org.eclipse.osee.orcs.rest.model.IdeClientEndpoint;
import org.eclipse.osee.orcs.rest.model.IdeVersion;

/**
 * @author Donald G Dunne
 */
public class DbConnectionUtility {

   private static Boolean supported;
   private static Boolean applicationServerAlive;

   public static Result areOSEEServicesAvailable() {
      Result toReturn = Result.FalseResult;
      if (!isVersionSupported()) {
         toReturn =
            new Result(
               "This OSEE client version [%s] is not supported by the current application server(s).\n\nDatabase capability disabled.",
               OseeCodeVersion.getVersion());
      } else {
         toReturn = OseeUiActivator.areOSEEServicesAvailable();
      }
      return toReturn;
   }

   public static Result dbConnectionIsOkResult() {
      Result result = Result.TrueResult;
      if (!isApplicationServerAlive()) {
         result = new Result("The OSEE Application Server is not available.\n\nDatabase capability disabled.");
      } else {
         result = areOSEEServicesAvailable();
      }
      return result;
   }

   public static boolean dbConnectionIsOk() {
      return dbConnectionIsOkResult().isTrue();
   }

   public static boolean isApplicationServerAlive() {
      if (applicationServerAlive == null) {
         String address = OseeClientProperties.getOseeApplicationServer();
         if (Strings.isValid(address)) {
            try {
               getIdeClientSupportedVersions();
               applicationServerAlive = true;
            } catch (Exception ex) {
               applicationServerAlive = false;
            }
         } else {
            applicationServerAlive = false;
         }
      }
      return applicationServerAlive;
   }

   public static boolean isVersionSupported() {
      if (supported == null) {
         String address = OseeClientProperties.getOseeApplicationServer();
         if (Strings.isValid(address)) {
            supported = getIdeClientSupportedVersions().contains(OseeCodeVersion.getVersion());
         } else {
            supported = false;
         }
      }
      return supported;
   }

   private static Collection<String> getIdeClientSupportedVersions() {
      IdeVersion clientResult = null;

      OseeClient client = ServiceUtil.getOseeClient();
      if (client != null) {
         IdeClientEndpoint endpoint = client.getIdeClientEndpoint();
         try {
            clientResult = endpoint.getSupportedVersions();
         } catch (Exception ex) {
            throw JaxRsExceptions.asOseeException(ex);
         }
      }
      return clientResult != null ? clientResult.getVersions() : Collections.<String> emptySet();
   }

}
