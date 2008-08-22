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
package org.eclipse.osee.framework.branch.management.exchange.export;

import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.framework.branch.management.Activator;
import org.eclipse.osee.framework.branch.management.ExportOptions;
import org.eclipse.osee.framework.branch.management.exchange.ExportImportXml;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.ConnectionHandlerStatement;
import org.eclipse.osee.framework.db.connection.DbUtil;
import org.eclipse.osee.framework.db.connection.info.SQL3DataType;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.jdk.core.util.xml.Xml;
import org.eclipse.osee.framework.resource.management.IResource;
import org.eclipse.osee.framework.resource.management.IResourceLocator;
import org.eclipse.osee.framework.resource.management.Options;

/**
 * @author Roberto E. Escobar
 */
public class RelationalExportItem extends AbstractDbExportItem {
   private static final String BINARY_DATA_FOLDER = "resources";

   private String query;
   private StringBuffer binaryContentBuffer;
   private StringBuffer stringContentBuffer;
   private Set<IExportColumnListener> exportColumnListeners;

   public RelationalExportItem(int priority, String name, String source, String query) {
      super(priority, name, source.toLowerCase());
      this.query = query;
      this.binaryContentBuffer = new StringBuffer();
      this.stringContentBuffer = new StringBuffer();
      this.exportColumnListeners = java.util.Collections.synchronizedSet(new HashSet<IExportColumnListener>());
   }

   public String getQuery() {
      StringBuffer toReturn = new StringBuffer(query);
      if (getOptions().getBoolean(ExportOptions.INCLUDE_BASELINE_TXS.name()) != true && query.contains("txd1")) {
         toReturn.append(" AND txd1.TX_TYPE = 0");
      }
      return query;
   }

   protected String exportBinaryDataTo(File tempFolder, String uriTarget) throws Exception {
      tempFolder = new File(tempFolder + File.separator + BINARY_DATA_FOLDER);
      if (tempFolder.exists() != true) {
         tempFolder.mkdirs();
      }
      int index = uriTarget.lastIndexOf("/");
      String fileName = uriTarget.substring(index + 1, uriTarget.length());

      Options options = new Options();

      IResourceLocator locator = Activator.getInstance().getResourceLocatorManager().getResourceLocator(uriTarget);
      IResource resource = Activator.getInstance().getResourceManager().acquire(locator, options);

      File target = new File(tempFolder, fileName);
      Lib.copyFile(new File(resource.getLocation()), target);
      return target.getName();
   }

   protected void doWork(Appendable appendable) throws Exception {
      ConnectionHandlerStatement stmt = null;
      try {
         stmt = ConnectionHandler.runPreparedQuery(getConnection(), getQuery(), SQL3DataType.INTEGER, getJoinQueryId());
         while (stmt.next()) {
            processData(appendable, stmt.getRset());
         }
      } finally {
         DbUtil.close(stmt);
      }
   }

   protected void processData(Appendable appendable, ResultSet resultSet) throws Exception {
      ExportImportXml.openPartialXmlNode(appendable, ExportImportXml.ENTRY);
      try {
         ResultSetMetaData meta = resultSet.getMetaData();
         int numberOfColumns = meta.getColumnCount() + 1;
         for (int columnIndex = 1; columnIndex < numberOfColumns; columnIndex++) {
            String name = meta.getColumnName(columnIndex).toLowerCase();
            notifyOnColumnExport(name, resultSet);
            if (name.equals("uri")) {
               handleBinaryContent(binaryContentBuffer, getWriteLocation(), name, resultSet);
            } else if (name.equals("value")) {
               handleStringContent(stringContentBuffer, getWriteLocation(), name, resultSet);
            } else {
               switch (meta.getColumnType(columnIndex)) {
                  case Types.TIMESTAMP:
                     Timestamp timestamp = resultSet.getTimestamp(columnIndex);
                     ExportImportXml.addXmlAttribute(appendable, name, timestamp);
                     break;
                  default:
                     try {
                        String value = resultSet.getString(columnIndex);
                        ExportImportXml.addXmlAttribute(appendable, name, value);
                     } catch (Exception ex) {
                        throw new Exception(String.format("Unable to convert [%s] of raw type [%s] to string.", name,
                              meta.getColumnTypeName(columnIndex)), ex);
                     }
                     break;
               }
            }
         }
      } finally {
         if (binaryContentBuffer.length() > 0 || stringContentBuffer.length() > 0) {
            ExportImportXml.endOpenedPartialXmlNode(appendable);
            if (binaryContentBuffer.length() > 0) {
               appendable.append(binaryContentBuffer.toString());
               binaryContentBuffer.delete(0, binaryContentBuffer.length());
            }
            if (stringContentBuffer.length() > 0) {
               appendable.append(stringContentBuffer.toString());
               stringContentBuffer.delete(0, stringContentBuffer.length());
            }
            ExportImportXml.closeXmlNode(appendable, ExportImportXml.ENTRY);
         } else {
            ExportImportXml.closePartialXmlNode(appendable);
         }
      }
   }

   private void handleBinaryContent(Appendable appendable, File tempFolder, String name, ResultSet resultSet) throws Exception {
      String uriData = resultSet.getString("uri");
      if (Strings.isValid(uriData)) {
         String relativePath = exportBinaryDataTo(tempFolder, uriData);
         ExportImportXml.openPartialXmlNode(appendable, ExportImportXml.BINARY_CONTENT);
         ExportImportXml.addXmlAttribute(appendable, "location", relativePath);
         ExportImportXml.closePartialXmlNode(appendable);
      }
   }

   private void handleStringContent(Appendable appendable, File tempFolder, String name, ResultSet resultSet) throws Exception {
      String stringValue = resultSet.getString("value");
      if (Strings.isValid(stringValue)) {
         ExportImportXml.openXmlNode(appendable, ExportImportXml.STRING_CONTENT);
         Xml.writeAsCdata(appendable, stringValue);
         ExportImportXml.closeXmlNode(appendable, ExportImportXml.STRING_CONTENT);
      }
   }

   public void addExportColumnListener(IExportColumnListener exportColumnListener) {
      if (exportColumnListener != null) {
         this.exportColumnListeners.add(exportColumnListener);
      }
   }

   public void removeExportColumnListener(IExportColumnListener exportColumnListener) {
      if (exportColumnListener != null) {
         this.exportColumnListeners.remove(exportColumnListener);
      }
   }

   public void cleanUp() {
      this.exportColumnListeners.clear();
      super.cleanUp();
   }

   private void notifyOnColumnExport(String columnName, ResultSet resultSet) throws Exception {
      for (IExportColumnListener listener : this.exportColumnListeners) {
         listener.onColumnExport(columnName, resultSet);
      }
   }

   public interface IExportColumnListener {
      public abstract void onColumnExport(String columnName, ResultSet resultSet) throws Exception;
   }
}
