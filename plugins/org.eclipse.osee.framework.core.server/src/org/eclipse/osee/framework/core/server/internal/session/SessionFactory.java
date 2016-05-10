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
package org.eclipse.osee.framework.core.server.internal.session;

import java.util.Date;
import java.util.Properties;
import org.eclipse.osee.framework.core.data.IDatabaseInfo;
import org.eclipse.osee.framework.core.data.IUserToken;
import org.eclipse.osee.framework.core.data.OseeSessionGrant;
import org.eclipse.osee.framework.core.model.cache.IOseeTypeFactory;
import org.eclipse.osee.framework.core.server.OseeServerProperties;
import org.eclipse.osee.framework.core.sql.OseeSql;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.jdbc.JdbcClientConfig;
import org.eclipse.osee.jdbc.JdbcService;
import org.eclipse.osee.logger.Log;

/**
 * @author Roberto E. Escobar
 */
public final class SessionFactory implements IOseeTypeFactory {
   private final Log logger;
   private final JdbcService jdbcService;

   public SessionFactory(Log logger, JdbcService jdbcService) {
      this.logger = logger;
      this.jdbcService = jdbcService;
   }

   public Session createLoadedSession(String guid, String userId, Date creationDate, String clientVersion, String clientMachineName, String clientAddress, int clientPort) {
      Session toReturn =
         createNewSession(guid, userId, creationDate, clientVersion, clientMachineName, clientAddress, clientPort);
      return toReturn;
   }

   public Session createNewSession(String guid, String userId, Date creationDate, String clientVersion, String clientMachineName, String clientAddress, int clientPort) {
      Session toReturn =
         new Session(guid, userId, creationDate, clientVersion, clientMachineName, clientAddress, clientPort);
      return toReturn;
   }

   public OseeSessionGrant createSessionGrant(Session session, IUserToken userToken, String authenticationType) throws OseeCoreException {
      Conditions.checkNotNull(session, "Session");
      Conditions.checkNotNull(userToken, "IUserToken");

      OseeSessionGrant sessionGrant = new OseeSessionGrant(session.getGuid());
      sessionGrant.setAuthenticationProtocol(authenticationType);
      sessionGrant.setCreationRequired(userToken.isCreationRequired());
      sessionGrant.setUserToken(userToken);
      sessionGrant.setDatabaseInfo(getDatabaseInfo());

      Properties properties = OseeSql.getSqlProperties(jdbcService.getClient().getDbType().areHintsSupported());
      sessionGrant.setSqlProperties(properties);

      sessionGrant.setDataStorePath(OseeServerProperties.getOseeApplicationServerData(logger));
      return sessionGrant;
   }

   private IDatabaseInfo getDatabaseInfo() {
      final String infoId = jdbcService.getId();
      final JdbcClientConfig config = jdbcService.getClient().getConfig();
      return new IDatabaseInfo() {

         private static final long serialVersionUID = 192942011732757789L;

         @Override
         public boolean isProduction() {
            return config.isProduction();
         }

         @Override
         public String getId() {
            return infoId;
         }

         @Override
         public String getDriver() {
            return config.getDbDriver();
         }

         @Override
         public String getDatabaseLoginName() {
            return config.getDbUsername();
         }

         @Override
         public String getConnectionUrl() {
            return config.getDbUri();
         }

         @Override
         public Properties getConnectionProperties() {
            return config.getDbProps();
         }

         @Override
         public String getDatabaseName() {
            return jdbcService.hasServer() ? jdbcService.getServerConfig().getDbName() : "";
         }

         @Override
         public String getDatabaseHome() {
            return jdbcService.hasServer() ? jdbcService.getServerConfig().getDbPath() : "";
         }

      };
   }
}
