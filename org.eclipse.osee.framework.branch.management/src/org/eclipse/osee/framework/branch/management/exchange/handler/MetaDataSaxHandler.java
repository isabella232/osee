/*
 * Created on Aug 20, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.branch.management.exchange.handler;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.framework.branch.management.exchange.ExportImportXml;
import org.eclipse.osee.framework.db.connection.info.SQL3DataType;
import org.eclipse.osee.framework.db.connection.info.SupportedDatabase;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.jdk.core.util.io.xml.AbstractSaxHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Roberto E. Escobar
 */
public class MetaDataSaxHandler extends AbstractSaxHandler {

   private final Map<String, MetaData> importMetadataMap;
   private final Map<String, MetaData> targetMetadataMap;
   private MetaData currentMetadata;

   public MetaDataSaxHandler() {
      this.importMetadataMap = new HashMap<String, MetaData>();
      this.targetMetadataMap = new HashMap<String, MetaData>();
   }

   public MetaData getMetadata(String source) {
      return targetMetadataMap.get(source);
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.jdk.core.util.io.xml.AbstractSaxHandler#startElementFound(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
    */
   @Override
   public void startElementFound(String uri, String localName, String name, Attributes attributes) throws SAXException {
      try {
         if (localName.equalsIgnoreCase(ExportImportXml.METADATA)) {
            this.importMetadataMap.clear();
         } else if (localName.equalsIgnoreCase(ExportImportXml.TABLE)) {
            String tableName = attributes.getValue(ExportImportXml.TABLE_NAME);
            if (Strings.isValid(tableName)) {
               this.currentMetadata = new MetaData(tableName);
               this.importMetadataMap.put(tableName, currentMetadata);
            } else {
               this.currentMetadata = null;
            }
         } else if (localName.equalsIgnoreCase(ExportImportXml.COLUMN)) {
            String columnName = attributes.getValue(ExportImportXml.ID);
            String typeName = attributes.getValue(ExportImportXml.TYPE);
            SQL3DataType sql3DataType = SQL3DataType.valueOf(typeName);
            this.currentMetadata.addColumn(columnName, sql3DataType);
         }
      } catch (Exception ex) {
         throw new IllegalStateException(ex);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.jdk.core.util.io.xml.AbstractSaxHandler#endElementFound(java.lang.String, java.lang.String, java.lang.String)
    */
   @Override
   public void endElementFound(String uri, String localName, String name) throws SAXException {
      try {
         if (localName.equalsIgnoreCase(ExportImportXml.TABLE)) {
            this.currentMetadata = null;
         }
      } catch (Exception ex) {
         throw new IllegalStateException(ex);
      }
   }

   public void checkAndLoadTargetDbMetadata(Connection connection) throws Exception {
      Map<String, MetaData> targetTables = getTargetDbMetadata(connection);

      StringBuffer errorMessage = new StringBuffer();
      for (String tableName : targetTables.keySet()) {
         MetaData sourceMeta = this.importMetadataMap.get(tableName);
         MetaData destinationMeta = targetTables.get(tableName);
         Collection<String> sourceColumns = sourceMeta.getColumnNames();
         for (String destinationColumn : destinationMeta.getColumnNames()) {
            if (!sourceColumns.contains(destinationColumn)) {
               errorMessage.append(String.format(
                     "Target column not found in source database.\nTable:[%s] - [%s not in (%s)]\n", tableName,
                     destinationColumn, sourceColumns));
            }
         }
      }
      if (errorMessage.length() > 0) {
         throw new Exception(errorMessage.toString());
      }
      this.targetMetadataMap.putAll(targetTables);
   }

   private Map<String, MetaData> getTargetDbMetadata(Connection connection) throws SQLException {
      Map<String, MetaData> targetDbMetadata = new HashMap<String, MetaData>();
      DatabaseMetaData dbMetaData = connection.getMetaData();
      for (String sourceTables : importMetadataMap.keySet()) {
         processMetaData(targetDbMetadata, dbMetaData, sourceTables);
      }
      return targetDbMetadata;
   }

   private void processMetaData(Map<String, MetaData> targetDbMetadata, DatabaseMetaData dbMetaData, String targetTable) throws SQLException {
      ResultSet resultSet = null;
      try {
         resultSet = dbMetaData.getTables(null, null, null, new String[] {"TABLE"});
         if (resultSet != null) {
            while (resultSet.next()) {
               String tableName = resultSet.getString("TABLE_NAME");
               String schemaName = resultSet.getString("TABLE_SCHEM");
               if (targetTable.equalsIgnoreCase(tableName)) {
                  String name = tableName.toLowerCase();
                  MetaData currentMetadata = new MetaData(name);
                  targetDbMetadata.put(name, currentMetadata);
                  processColumnMetaData(currentMetadata, dbMetaData, schemaName, tableName);
               }
            }
         }
      } finally {
         if (resultSet != null) {
            resultSet.close();
         }
      }
   }

   private void processColumnMetaData(MetaData currentMetadata, DatabaseMetaData dbMetaData, String schema, String tableName) throws SQLException {
      ResultSet resultSet = null;
      try {
         try {
            resultSet = dbMetaData.getColumns(null, schema, tableName, null);
         } catch (SQLException ex) {
            resultSet = dbMetaData.getColumns(null, null, tableName, null);
         }
         if (resultSet != null) {
            SupportedDatabase dbType = SupportedDatabase.getDatabaseType(dbMetaData.getConnection());
            while (resultSet.next()) {
               String columnId = resultSet.getString("COLUMN_NAME").toLowerCase();
               int dataType = resultSet.getInt("DATA_TYPE");
               if (dbType.equals(SupportedDatabase.foxpro)) {
                  if (dataType == Types.CHAR) {
                     dataType = Types.VARCHAR;
                  }
               }
               currentMetadata.addColumn(columnId, SQL3DataType.get(dataType));
            }
         }
      } finally {
         if (resultSet != null) {
            resultSet.close();
         }
      }
   }
}
