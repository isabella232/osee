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

import java.util.List;
import java.util.Properties;
import org.eclipse.osee.framework.core.client.internal.InternalClientSessionManager;
import org.eclipse.osee.framework.core.data.IdeClientSession;
import org.eclipse.osee.framework.core.data.OseeSessionGrant;
import org.eclipse.osee.framework.core.data.UserToken;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Roberto E. Escobar
 */
public final class ClientSessionManager {
   private static InternalClientSessionManager internal;

   private ClientSessionManager() {
      //
   }

   public static void setInternalSessionManager(InternalClientSessionManager internalSessionManager) {
      internal = internalSessionManager;
   }

   public static final String getStatusId() {
      return InternalClientSessionManager.STATUS_ID;
   }

   public static boolean isSessionValid() {
      return internal.isSessionValid();
   }

   public static void ensureSessionCreated() {
      internal.ensureSessionCreated();
   }

   private static OseeSessionGrant getSessionGrant() {
      return internal.getOseeSessionGrant();
   }

   public static IdeClientSession getSession() {
      return internal.getOseeSession();
   }

   public static UserToken getCurrentUserToken() {
      return getSessionGrant().getUserToken();
   }

   public static String getSessionId() {
      return getSessionGrant().getSessionId();
   }

   public static String getClientName() {
      return getSession().getClientName();
   }

   public static String getClientPort() {
      return getSession().getClientPort();
   }

   public static String getDataStoreLoginName() {
      return getSessionGrant().getDbLogin();
   }

   public static String getDataStoreDriver() {
      return getSessionGrant().getDbDriver();
   }

   public static String getDataStoreName() {
      return getSessionGrant().getDbDatabaseName();
   }

   public static String getDataStorePath() {
      return getSessionGrant().getDataStorePath();
   }

   public static boolean isProductionDataStore() {
      return getSessionGrant().isDbIsProduction();
   }

   public static boolean useOracleHints() {
      return Strings.isValid(getSessionGrant().getUseOracleHints()) ? Boolean.valueOf(
         getSessionGrant().getUseOracleHints()) : false;
   }

   public static Properties getSqlProperties() {
      return getSessionGrant().getSqlProperties();
   }

   public static List<String> getAuthenticationProtocols() {
      return internal.getAuthenticationProtocols();
   }

   public static void authenticateAsAnonymous() {
      internal.authenticateAsAnonymous();
   }

   public static void authenticate(ICredentialProvider credentialProvider) {
      internal.authenticate(credentialProvider);
   }

   public static void releaseSession() {
      internal.releaseSession();
   }

   public static String getDatabaseInfo() {
      return getSessionGrant().toString();
   }

   public static IdeClientSession getSafeSession() {
      return internal.getSafeSession();
   }
}