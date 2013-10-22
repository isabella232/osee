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
package org.eclipse.osee.framework.core.data;

import java.io.InputStream;
import java.util.Properties;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.GUID;

/**
 * @author Roberto E. Escobar
 */
public class OseeSessionGrant extends BaseExchangeData {

   private static final long serialVersionUID = -7236201704435470272L;
   private static final String SESSION_ID = "sessionId";
   private static final String SQL_PROPERTIES = "slqProperty";

   private static final String DB_DRIVER = "dbDriver";
   private static final String DB_CONNECTION_URL = "dbUrl";
   private static final String DB_CONNECT_PROPERTIES = "dbConnectionProperties";
   private static final String DB_LOGIN_NAME = "dbLogin";
   private static final String DB_DATABASE_NAME = "dbDatabaseName";
   private static final String DB_IS_PRODUCTION = "dbIsProduction";
   private static final String DB_PATH = "dbDatabasePath";
   private static final String DB_ID = "dbId";

   private static final String OSEE_USER_IS_CREATION_REQUIRED = "oseeUserNeedsCreation";
   private static final String OSEE_USER_EMAIL = "oseeUserEmail";
   private static final String OSEE_USER_NAME = "oseeUserName";
   private static final String OSEE_USER_ID = "oseeUserId";
   private static final String OSEE_IS_USER_ACTIVE = "isOseeUserActive";
   private static final String OSEE_APPLICATION_SERVER_DATA_PATH = "oseeApplicationServerDataPath";
   private static final String OSEE_CLIENT_BUILD_DESIGNATION = "oseeClientBuildDesignation";
   private static final String AUTHENTICATION_PROTOCOL = "oseeAuthenticationProtocol";

   private IDatabaseInfo grantedDatabaseInfo;

   protected OseeSessionGrant() {
      super();
      this.grantedDatabaseInfo = new GrantedDatabaseInfo();
   }

   public OseeSessionGrant(String sessionId) {
      super();
      this.backingData.put(SESSION_ID, sessionId);
   }

   public String getSessionId() {
      return getString(SESSION_ID);
   }

   public IDatabaseInfo getDatabaseInfo() {
      return grantedDatabaseInfo;
   }

   public void setDatabaseInfo(IDatabaseInfo dbInfo) {
      this.backingData.put(DB_DRIVER, dbInfo.getDriver());
      this.backingData.put(DB_CONNECTION_URL, dbInfo.getConnectionUrl());
      this.backingData.put(DB_LOGIN_NAME, dbInfo.getDatabaseLoginName());
      this.backingData.put(DB_DATABASE_NAME, dbInfo.getDatabaseName());
      this.backingData.put(DB_IS_PRODUCTION, dbInfo.isProduction());
      this.backingData.put(DB_ID, dbInfo.getId());
      this.backingData.put(DB_PATH, dbInfo.getDatabaseHome());
      putProperties(DB_CONNECT_PROPERTIES, dbInfo.getConnectionProperties());
   }

   public void setSqlProperties(Properties sqlProperties) {
      putProperties(SQL_PROPERTIES, sqlProperties);
   }

   public Properties getSqlProperties() {
      return getPropertyString(SQL_PROPERTIES);
   }

   public void setUserToken(IUserToken userInfo) throws OseeCoreException {
      this.backingData.put(OSEE_USER_EMAIL, userInfo.getEmail());
      this.backingData.put(OSEE_USER_NAME, userInfo.getName());
      this.backingData.put(OSEE_USER_ID, userInfo.getUserId());
      this.backingData.put(OSEE_IS_USER_ACTIVE, userInfo.isActive());
   }

   public boolean isCreationRequired() {
      return backingData.getBoolean(OSEE_USER_IS_CREATION_REQUIRED);
   }

   public void setCreationRequired(boolean value) {
      this.backingData.put(OSEE_USER_IS_CREATION_REQUIRED, value);
   }

   public void setDataStorePath(String oseeApplicationServerData) {
      this.backingData.put(OSEE_APPLICATION_SERVER_DATA_PATH, oseeApplicationServerData);
   }

   public String getDataStorePath() {
      return getString(OSEE_APPLICATION_SERVER_DATA_PATH);
   }

   public IUserToken getUserToken() {
      return getGrantedUserToken();
   }

   public String getClientBuildDesignation() {
      return getString(OSEE_CLIENT_BUILD_DESIGNATION);
   }

   public void setClientBuildDesination(String designation) {
      this.backingData.put(OSEE_CLIENT_BUILD_DESIGNATION, designation);
   }

   public String getAuthenticationProtocol() {
      return getString(AUTHENTICATION_PROTOCOL);
   }

   public void setAuthenticationProtocol(String protocol) {
      this.backingData.put(AUTHENTICATION_PROTOCOL, protocol);
   }

   public static OseeSessionGrant fromXml(InputStream inputStream) throws OseeCoreException {
      OseeSessionGrant session = new OseeSessionGrant();
      session.loadfromXml(inputStream);
      return session;
   }

   private IUserToken getGrantedUserToken() {
      return TokenFactory.createUserToken(GUID.create(), getString(OSEE_USER_NAME), getString(OSEE_USER_EMAIL),
         getString(OSEE_USER_ID), backingData.getBoolean(OSEE_IS_USER_ACTIVE), false, false);
   }

   private final class GrantedDatabaseInfo implements IDatabaseInfo {

      private static final long serialVersionUID = -7314120611445752014L;

      @Override
      public Properties getConnectionProperties() {
         return getPropertyString(DB_CONNECT_PROPERTIES);
      }

      @Override
      public String getConnectionUrl() {
         return getString(DB_CONNECTION_URL);
      }

      @Override
      public String getDatabaseLoginName() {
         return getString(DB_LOGIN_NAME);
      }

      @Override
      public String getDatabaseName() {
         return getString(DB_DATABASE_NAME);
      }

      @Override
      public String getDriver() {
         return getString(DB_DRIVER);
      }

      @Override
      public String getId() {
         return getString(DB_ID);
      }

      @Override
      public boolean isProduction() {
         return Boolean.valueOf(getString(DB_IS_PRODUCTION));
      }

      @Override
      public String toString() {
         return getId() + ": " + getConnectionUrl();
      }

      @Override
      public String getDatabaseHome() {
         return getString(DB_PATH);
      }

   }

}
