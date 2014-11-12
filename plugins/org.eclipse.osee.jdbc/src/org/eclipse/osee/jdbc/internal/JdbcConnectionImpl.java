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
package org.eclipse.osee.jdbc.internal;

import static org.eclipse.osee.jdbc.JdbcException.newJdbcException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.eclipse.osee.jdbc.JdbcConnection;
import org.eclipse.osee.jdbc.JdbcException;

/**
 * @author Roberto E. Escobar
 */
public class JdbcConnectionImpl implements JdbcConnection {

   private static final int STALE_CONNECTION_TIMEOUT = 60000;
   private final Connection conn;

   public JdbcConnectionImpl(Connection conn) {
      super();
      this.conn = conn;
   }

   @Override
   public boolean isStale() {
      boolean result = true;
      try {
         result = !conn.isValid(STALE_CONNECTION_TIMEOUT);
      } catch (SQLException ex) {
         // do nothing
      }
      return result;
   }

   @Override
   public void close() {
      destroy();
   }

   @Override
   public boolean isClosed() {
      boolean result = false;
      try {
         result = conn.isClosed();
      } catch (SQLException ex) {
         // Do nothing;
      }
      return result;
   }

   @Override
   public DatabaseMetaData getMetaData() throws JdbcException {
      try {
         return conn.getMetaData();
      } catch (SQLException ex) {
         throw newJdbcException(ex);
      }
   }

   protected PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
   }

   protected PreparedStatement prepareStatement(String sql) throws SQLException {
      return prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
   }

   protected CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
   }

   protected void destroy() {
      try {
         conn.close();
      } catch (SQLException ex) {
         // Do Nothing - 
      }
   }

   protected void setAutoCommit(boolean autoCommit) throws JdbcException {
      try {
         conn.setAutoCommit(autoCommit);
      } catch (SQLException ex) {
         throw newJdbcException(ex);
      }
   }

   protected boolean getAutoCommit() throws JdbcException {
      try {
         return conn.getAutoCommit();
      } catch (SQLException ex) {
         throw newJdbcException(ex);
      }
   }

   protected void commit() throws JdbcException {
      try {
         conn.commit();
      } catch (SQLException ex) {
         throw newJdbcException(ex);
      }
   }

   protected void rollback() throws JdbcException {
      try {
         conn.rollback();
      } catch (SQLException ex) {
         throw newJdbcException(ex);
      }
   }

}