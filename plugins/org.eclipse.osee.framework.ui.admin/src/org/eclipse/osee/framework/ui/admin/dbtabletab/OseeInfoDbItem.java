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
package org.eclipse.osee.framework.ui.admin.dbtabletab;

import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.admin.Activator;

public class OseeInfoDbItem extends DbItem {

   public OseeInfoDbItem() {
      super("OSEE_INFO");
   }

   @Override
   public boolean isWriteable(String columnName) {
      return true;
   }

   @Override
   public boolean isBems(String columnName) {
      return false;
   }

   public String returnTic(String str) {
      return "'" + str + "'";
   }

   @Override
   public int getColumnWidth(String columnName) {
      return 100;
   }

   public boolean exists(String key) throws OseeDataStoreException {
      boolean toReturn = false;
      IOseeStatement chStmt = ConnectionHandler.getStatement();
      try {
         String query = "SELECT * FROM " + getTableName() + " WHERE OSEE_KEY = " + returnTic(key);
         chStmt.runPreparedQuery(query);
         toReturn = chStmt.next();
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      } finally {
         chStmt.close();
      }
      return toReturn;
   }

   @Override
   public void save(DbDescribe describe, DbModel model) {
      try {
         String key = (String) model.getColumn(describe.indexOfColumn("OSEE_KEY"));
         String value = (String) model.getColumn(describe.indexOfColumn("OSEE_VALUE"));
         String query;
         if (exists(key)) {
            query = "UPDATE " + getTableName() + " SET OSEE_KEY = ?, OSEE_VALUE = ? WHERE OSEE_KEY = ?";
            ConnectionHandler.runPreparedUpdate(query, key, value, key);
         } else {
            query = "INSERT INTO " + getTableName() + " (OSEE_KEY, OSEE_VALUE) VALUES (?, ?)";
            ConnectionHandler.runPreparedUpdate(query, key, value);
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public DbModel createNewRow(DbModel example) {
      DbModel dbModel = new DbModel();
      for (int x = 0; x < example.getValues().length; x++) {
         dbModel.addColumn(x, "");
      }
      dbModel.setColumn(0, "NEW");
      dbModel.setColumnChanged("KEY");
      return dbModel;
   }

}
