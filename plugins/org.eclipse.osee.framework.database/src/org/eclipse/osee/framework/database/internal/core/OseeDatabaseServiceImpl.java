/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.database.internal.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.IDatabaseInfo;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.database.DatabaseService;
import org.eclipse.osee.framework.database.core.IDatabaseInfoProvider;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.database.internal.DatabaseHelper;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Roberto E. Escobar
 */
public class OseeDatabaseServiceImpl implements DatabaseService {

   public static interface ConnectionPoolProvider {

      void disposeConnectionPool(IDatabaseInfo databaseInfo);

      OseeConnectionPoolImpl getConnectionPool(IDatabaseInfo databaseInfo) throws OseeDataStoreException;
   }

   private final ConnectionPoolProvider poolProvider;
   private final IDatabaseInfoProvider dbInfoProvider;

   public OseeDatabaseServiceImpl(ConnectionPoolProvider poolProvider, IDatabaseInfoProvider dbInfoProvider) {
      super();
      this.poolProvider = poolProvider;
      this.dbInfoProvider = dbInfoProvider;
   }

   private IDatabaseInfo getDatabaseInfoProvider() throws OseeDataStoreException {
      return dbInfoProvider.getDatabaseInfo();
   }

   private OseeConnectionPoolImpl getConnectionPool(IDatabaseInfo databaseInfo) throws OseeDataStoreException {
      return poolProvider.getConnectionPool(databaseInfo);
   }

   private OseeConnectionPoolImpl getDefaultConnectionPool() throws OseeDataStoreException {
      return getConnectionPool(getDatabaseInfoProvider());
   }

   @Override
   public OseeConnection getConnection() throws OseeCoreException {
      return getConnection(getDatabaseInfoProvider());
   }

   @Override
   public OseeConnection getConnection(IDatabaseInfo databaseInfo) throws OseeCoreException {
      OseeConnectionPoolImpl pool = getConnectionPool(databaseInfo);
      return pool.getConnection();
   }

   @Override
   public IOseeStatement getStatement() throws OseeDataStoreException {
      return new OseeStatementImpl(getDefaultConnectionPool());
   }

   @Override
   public IOseeStatement getStatement(OseeConnection connection) throws OseeDataStoreException {
      return new OseeStatementImpl(getDefaultConnectionPool(), (OseeConnectionImpl) connection);
   }

   @Override
   public IOseeStatement getStatement(OseeConnection connection, boolean autoClose) throws OseeDataStoreException {
      return new OseeStatementImpl(getDefaultConnectionPool(), (OseeConnectionImpl) connection, autoClose);
   }

   @Override
   public IOseeStatement getStatement(int resultSetType, int resultSetConcurrency) throws OseeDataStoreException {
      return new OseeStatementImpl(getDefaultConnectionPool(), resultSetType, resultSetConcurrency);
   }

   @Override
   public <O extends Object> int runPreparedUpdate(OseeConnection connection, String query, O... data) throws OseeCoreException {
      if (connection == null) {
         return runPreparedUpdate(query, data);
      }
      PreparedStatement preparedStatement = null;
      int updateCount = 0;
      try {
         preparedStatement = ((OseeConnectionImpl) connection).prepareStatement(query);
         StatementUtil.populateValuesForPreparedStatement(preparedStatement, data);
         updateCount = preparedStatement.executeUpdate();
      } catch (SQLException ex) {
         OseeExceptions.wrapAndThrow(ex);
      } finally {
         StatementUtil.close(preparedStatement);
      }
      return updateCount;
   }

   @Override
   public <O extends Object> int runBatchUpdate(OseeConnection connection, String query, List<O[]> dataList) throws OseeCoreException {
      if (connection == null) {
         return runBatchUpdate(query, dataList);
      }
      int returnCount = 0;
      PreparedStatement preparedStatement = null;
      try {
         preparedStatement = ((OseeConnectionImpl) connection).prepareStatement(query);
         boolean needExecute = false;
         int count = 0;
         for (Object[] data : dataList) {
            count++;
            StatementUtil.populateValuesForPreparedStatement(preparedStatement, data);
            preparedStatement.addBatch();
            preparedStatement.clearParameters();
            needExecute = true;
            if (count > 2000) {
               int[] updates = preparedStatement.executeBatch();
               returnCount += StatementUtil.calculateBatchUpdateResults(updates);
               count = 0;
               needExecute = false;
            }
         }
         if (needExecute) {
            int[] updates = preparedStatement.executeBatch();
            returnCount += StatementUtil.calculateBatchUpdateResults(updates);
         }

      } catch (SQLException ex) {
         SQLException exlist;
         if ((exlist = ex.getNextException()) != null) {
            OseeLog.log(DatabaseHelper.class, Level.SEVERE, "This is the nested exception", exlist);
         }
         throw new OseeCoreException("sql update failed: \n%s\n%s", query,
            StatementUtil.getBatchErrorMessage(dataList), ex);
      } finally {
         StatementUtil.close(preparedStatement);
      }
      return returnCount;
   }

   @Override
   public <O> int runBatchUpdate(String query, List<O[]> dataList) throws OseeCoreException {
      OseeConnection connection = getConnection();
      try {
         return runBatchUpdate(connection, query, dataList);
      } finally {
         connection.close();
      }
   }

   @Override
   public <O> int runPreparedUpdate(String query, O... data) throws OseeCoreException {
      OseeConnection connection = getConnection();
      try {
         return runPreparedUpdate(connection, query, data);
      } finally {
         connection.close();
      }
   }

   @Override
   public <T, O extends Object> T runPreparedQueryFetchObject(T defaultValue, String query, O... data) throws OseeCoreException {
      return runPreparedQueryFetchObject(getStatement(), defaultValue, query, data);
   }

   @Override
   public <T, O extends Object> T runPreparedQueryFetchObject(OseeConnection connection, T defaultValue, String query, O... data) throws OseeCoreException {
      return runPreparedQueryFetchObject(getStatement(connection), defaultValue, query, data);
   }

   @SuppressWarnings("unchecked")
   private <T, O extends Object> T runPreparedQueryFetchObject(IOseeStatement chStmt, T defaultValue, String query, O... data) throws OseeCoreException {
      Conditions.checkNotNull(defaultValue, "default value");
      try {
         chStmt.runPreparedQuery(1, query, data);
         if (chStmt.next()) {
            Object toReturn = null;
            Class<?> classValue = defaultValue.getClass();
            if (classValue.isAssignableFrom(Integer.class)) {
               toReturn = chStmt.getInt(1);
            } else if (classValue.isAssignableFrom(String.class)) {
               toReturn = chStmt.getString(1);
            } else if (classValue.isAssignableFrom(Long.class)) {
               toReturn = chStmt.getLong(1);
            } else {
               toReturn = chStmt.getObject(1);
            }
            return (T) toReturn;
         }
         return defaultValue;
      } finally {
         chStmt.close();
      }
   }

   @Override
   public boolean isProduction() throws OseeCoreException {
      return getDatabaseInfoProvider().isProduction();
   }

}
