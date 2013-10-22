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
package org.eclipse.osee.framework.database.internal.core;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.database.core.SQL3DataType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Roberto E. Escobar
 */
public final class StatementUtil {

   private StatementUtil() {
      // Utility class
   }

   public static <O extends Object> String getBatchErrorMessage(List<O[]> dataList) {
      StringBuilder details = new StringBuilder(dataList.size() * dataList.get(0).length * 20);
      details.append("[ DATA OBJECT: \n");
      for (Object[] data : dataList) {
         for (int i = 0; i < data.length; i++) {
            details.append(i);
            details.append(": ");
            Object dataValue = data[i];
            if (dataValue != null) {
               details.append(dataValue.getClass().getName());
               details.append(":");

               String value = dataValue.toString();
               if (value.length() > 35) {
                  details.append(value.substring(0, 35));
               } else {
                  details.append(value);
               }
               details.append("\n");
            } else {
               details.append("NULL\n");
            }
         }
         details.append("---------\n");
      }
      details.append("]\n");
      return details.toString();
   }

   public static int calculateBatchUpdateResults(int[] updates) {
      int returnCount = 0;
      for (int update : updates) {
         if (update >= 0) {
            returnCount += update;
         } else if (Statement.EXECUTE_FAILED == update) {
            OseeLog.log(StatementUtil.class, Level.SEVERE, "sql execute failed.");
         } else if (Statement.SUCCESS_NO_INFO == update) {
            returnCount++;
         }
      }
      return returnCount;
   }

   public static void close(PreparedStatement stmt) {
      if (stmt != null) {
         try {
            stmt.close();
         } catch (SQLException ex) {
            OseeLog.log(StatementUtil.class, Level.WARNING, "Unable to close database statement: ", ex);
         }
      }
   }

   public static <O extends Object> void populateValuesForPreparedStatement(PreparedStatement preparedStatement, O... data) throws OseeCoreException {
      try {
         int preparedIndex = 0;
         for (Object dataValue : data) {
            preparedIndex++;
            if (dataValue instanceof String) {
               int length = ((String) dataValue).length();
               if (length > 4000) {
                  throw new OseeDataStoreException("SQL data value length must be  <= 4000 not %d\nValue: %s", length,
                     dataValue);
               }
            }

            if (dataValue == null) {
               throw new OseeDataStoreException(
                  "instead of passing null for an query parameter, pass the corresponding SQL3DataType");
            } else if (dataValue instanceof SQL3DataType) {
               int dataTypeNumber = ((SQL3DataType) dataValue).getSQLTypeNumber();
               if (dataTypeNumber == java.sql.Types.BLOB) {
                  // TODO Need to check this - for PostgreSql, setNull for BLOB with the new JDBC driver gives the error "column
                  //  "content" is of type bytea but expression is of type oid"
                  preparedStatement.setBytes(preparedIndex, null);
               } else {
                  preparedStatement.setNull(preparedIndex, dataTypeNumber);
               }
            } else if (dataValue instanceof ByteArrayInputStream) {
               preparedStatement.setBinaryStream(preparedIndex, (ByteArrayInputStream) dataValue,
                  ((ByteArrayInputStream) dataValue).available());
            } else if (dataValue instanceof Date) {
               java.util.Date javaDate = (java.util.Date) dataValue;
               java.sql.Timestamp date = new java.sql.Timestamp(javaDate.getTime());
               preparedStatement.setTimestamp(preparedIndex, date);
            } else if (dataValue instanceof BigInteger) {
               BigInteger bigInt = (BigInteger) dataValue;
               preparedStatement.setLong(preparedIndex, bigInt.longValue());
            } else if (dataValue instanceof BigDecimal) {
               BigDecimal bigDec = (BigDecimal) dataValue;
               preparedStatement.setLong(preparedIndex, bigDec.longValue());
            } else {
               preparedStatement.setObject(preparedIndex, dataValue);
            }
         }
      } catch (SQLException ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
   }
}
