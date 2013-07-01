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
package org.eclipse.osee.orcs.db.internal.exchange.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.services.IdentityService;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.core.util.HexUtil;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.jdk.core.util.xml.Xml;
import org.eclipse.osee.framework.resource.management.IResource;
import org.eclipse.osee.framework.resource.management.IResourceLocator;
import org.eclipse.osee.framework.resource.management.IResourceManager;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.db.internal.exchange.ExportImportXml;
import org.eclipse.osee.orcs.db.internal.exchange.handler.ExportItem;

/**
 * @author Roberto E. Escobar
 */
public class DbTableExportItem extends AbstractXmlExportItem {

   private final StringBuilder binaryContentBuffer = new StringBuilder();
   private final StringBuilder stringContentBuffer = new StringBuilder();
   private final StringBuilder oseeCommentBuffer = new StringBuilder();
   private final StringBuilder branchNameBuffer = new StringBuilder();
   private final StringBuilder rationaleBuffer = new StringBuilder();
   private final String query;
   private final Object[] bindData;

   private final IOseeDatabaseService dbService;
   private final IdentityService identityService;
   private final IResourceManager resourceManager;

   public DbTableExportItem(Log logger, IOseeDatabaseService dbService, IdentityService identityService, IResourceManager resourceManager, ExportItem id, String query, Object[] bindData) {
      super(logger, id);
      this.dbService = dbService;
      this.identityService = identityService;
      this.resourceManager = resourceManager;
      this.query = query;
      this.bindData = bindData;
   }

   private IOseeDatabaseService getDatabaseService() {
      return dbService;
   }

   protected String exportBinaryDataTo(File tempFolder, String uriTarget) throws OseeCoreException, IOException {
      tempFolder = new File(tempFolder + File.separator + ExportImportXml.RESOURCE_FOLDER_NAME);
      if (tempFolder.exists() != true) {
         tempFolder.mkdirs();
      }

      IResourceLocator locator = resourceManager.getResourceLocator(uriTarget);
      IResource resource = resourceManager.acquire(locator, new PropertyStore());

      File target = new File(tempFolder, locator.getRawPath());
      if (target.getParentFile() != null) {
         target.getParentFile().mkdirs();
      }

      InputStream sourceStream = null;
      OutputStream outputStream = null;
      try {
         sourceStream = resource.getContent();
         outputStream = new FileOutputStream(target);
         Lib.inputStreamToOutputStream(sourceStream, outputStream);
      } finally {
         Lib.close(sourceStream);
         Lib.close(outputStream);
      }
      return locator.getRawPath().replace('/', '\\');
   }

   @Override
   protected void doWork(Appendable appendable) throws Exception {
      IOseeStatement chStmt = getDatabaseService().getStatement();
      try {
         chStmt.runPreparedQuery(10000, query, bindData);
         while (chStmt.next()) {
            processData(appendable, chStmt);
         }
      } finally {
         chStmt.close();
      }
   }

   private void processData(Appendable appendable, IOseeStatement chStmt) throws Exception {
      ExportImportXml.openPartialXmlNode(appendable, ExportImportXml.ENTRY);

      try {
         int numberOfColumns = chStmt.getColumnCount();
         for (int columnIndex = 1; columnIndex <= numberOfColumns; columnIndex++) {
            String columnName = chStmt.getColumnName(columnIndex).toLowerCase();
            Object value = chStmt.getObject(columnIndex);

            if (columnName.equals("uri")) {
               handleBinaryContent(binaryContentBuffer, value);
            } else if (columnName.equals("value")) {
               handleStringContent(stringContentBuffer, value, ExportImportXml.STRING_CONTENT);
            } else if (columnName.equals(ExportImportXml.OSEE_COMMENT)) {
               handleStringContent(oseeCommentBuffer, value, columnName);
            } else if (columnName.equals(ExportImportXml.BRANCH_NAME)) {
               handleStringContent(branchNameBuffer, value, columnName);
            } else if (columnName.equals(ExportImportXml.RATIONALE)) {
               handleStringContent(rationaleBuffer, value, columnName);
            } else if (columnName.equals(ExportImportXml.ART_TYPE_ID)) {
               handleTypeId(appendable, value);
            } else if (columnName.equals(ExportImportXml.ATTR_TYPE_ID)) {
               handleTypeId(appendable, value);
            } else if (columnName.equals(ExportImportXml.REL_TYPE_ID)) {
               handleTypeId(appendable, value);
            } else {
               Timestamp timestamp = asTimestamp(value);
               if (timestamp != null) {
                  ExportImportXml.addXmlAttribute(appendable, columnName, timestamp);
               } else {
                  try {
                     ExportImportXml.addXmlAttribute(appendable, columnName, value);
                  } catch (Exception ex) {
                     throw new OseeCoreException(ex, "Unable to convert [%s] of raw type [%s] to string.", columnName,
                        chStmt.getColumnTypeName(columnIndex));
                  }
               }
            }
         }
      } finally {
         if (binaryContentBuffer.length() > 0 || stringContentBuffer.length() > 0 || oseeCommentBuffer.length() > 0 || branchNameBuffer.length() > 0 || rationaleBuffer.length() > 0) {
            ExportImportXml.endOpenedPartialXmlNode(appendable);
            if (binaryContentBuffer.length() > 0) {
               appendable.append(binaryContentBuffer);
               binaryContentBuffer.delete(0, binaryContentBuffer.length());
            }
            if (stringContentBuffer.length() > 0) {
               appendable.append(stringContentBuffer);
               stringContentBuffer.delete(0, stringContentBuffer.length());
            }
            if (oseeCommentBuffer.length() > 0) {
               appendable.append(oseeCommentBuffer);
               oseeCommentBuffer.delete(0, oseeCommentBuffer.length());
            }
            if (branchNameBuffer.length() > 0) {
               appendable.append(branchNameBuffer);
               branchNameBuffer.delete(0, branchNameBuffer.length());
            }
            if (rationaleBuffer.length() > 0) {
               appendable.append(rationaleBuffer);
               rationaleBuffer.delete(0, rationaleBuffer.length());
            }
            ExportImportXml.closeXmlNode(appendable, ExportImportXml.ENTRY);
         } else {
            ExportImportXml.closePartialXmlNode(appendable);
         }
      }
   }

   private Timestamp asTimestamp(Object value) {
      Timestamp toReturn = null;
      if (value instanceof Timestamp) {
         toReturn = (Timestamp) value;
      } else if (value != null) {
         try {
            // Account for oracle driver issues
            Class<?> clazz = value.getClass();
            Method method = clazz.getMethod("timestampValue");
            Object object = method.invoke(value);
            if (object instanceof Timestamp) {
               toReturn = (Timestamp) object;
            }
         } catch (NoSuchMethodException ex) {
            // Do Nothing
         } catch (Exception ex) {
            getLogger().warn(ex, "Error converting [%s] to timestamp", value);
         }
      }
      return toReturn;
   }

   private void handleBinaryContent(Appendable appendable, Object value) throws OseeCoreException, IOException {
      String uriData = (String) value;
      if (Strings.isValid(uriData)) {
         uriData = exportBinaryDataTo(getWriteLocation(), uriData);
         ExportImportXml.openPartialXmlNode(appendable, ExportImportXml.BINARY_CONTENT);
         ExportImportXml.addXmlAttribute(appendable, "location", uriData);
         ExportImportXml.closePartialXmlNode(appendable);
      }
   }

   private void handleTypeId(Appendable appendable, Object value) throws IOException, OseeCoreException {
      int typeId = -1;
      if (value instanceof Short) {
         Short xShort = (Short) value;
         typeId = xShort.intValue();
      } else if (value instanceof Integer) {
         typeId = (Integer) value;
      } else if (value instanceof Long) {
         typeId = ((Long) value).intValue();
      } else if (value instanceof BigInteger) {
         typeId = ((BigInteger) value).intValue();
      } else if (value instanceof BigDecimal) {
         typeId = ((BigDecimal) value).intValue();
      } else {
         throw new OseeCoreException("Undefined Type [%s]", value != null ? value.getClass().getSimpleName() : value);
      }
      Long uuid = identityService.getUniversalId(typeId);
      Conditions.checkNotNull(uuid, "abstartOseeType", "localId[%s] for [%s]", typeId, getSource());
      String uuidString = HexUtil.toString(uuid);
      ExportImportXml.addXmlAttribute(appendable, ExportImportXml.TYPE_GUID, uuidString);
   }

   private void handleStringContent(Appendable appendable, Object value, String tag) throws IOException {
      String stringValue = (String) value;
      if (Strings.isValid(stringValue)) {
         ExportImportXml.openXmlNodeNoNewline(appendable, tag);
         Xml.writeAsCdata(appendable, stringValue);
         ExportImportXml.closeXmlNode(appendable, tag);
      }
   }
}
