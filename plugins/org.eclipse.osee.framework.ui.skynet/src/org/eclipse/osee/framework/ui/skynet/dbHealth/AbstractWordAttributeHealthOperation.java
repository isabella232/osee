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
package org.eclipse.osee.framework.ui.skynet.dbHealth;

import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.WholeWordContent;
import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.WordTemplateContent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.client.server.HttpUrlBuilderClient;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.OseeServerContext;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.util.HttpProcessor;
import org.eclipse.osee.framework.core.util.HttpProcessor.AcquireResult;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.plugin.core.util.OseeData;
import org.eclipse.osee.framework.skynet.core.utility.ConnectionHandler;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.jdbc.JdbcStatement;

/**
 * @author Roberto E. Escobar
 */
public abstract class AbstractWordAttributeHealthOperation extends DatabaseHealthOperation {

   private final String baseName;

   public AbstractWordAttributeHealthOperation(String name) {
      super(name);
      this.baseName = name;
   }

   @Override
   protected void doHealthCheck(IProgressMonitor monitor) throws Exception {
      List<AttrData> attributesWithErrors = new ArrayList<AttrData>();

      IOperation operation =
         new FindAllWordAttributesNeedingFix(String.format("Find all %s enabled", baseName), attributesWithErrors);
      doSubWork(operation, monitor, 0.40);

      setItemsToFix(attributesWithErrors.size());

      appendToDetails(AHTML.beginMultiColumnTable(100, 1));
      appendToDetails(AHTML.addHeaderRowMultiColumnTable(new String[] {"GUID", "GAMMA ID", "URI"}));
      for (AttrData attrData : attributesWithErrors) {
         appendToDetails(AHTML.addRowMultiColumnTable(new String[] {
            attrData.getGuid(),
            attrData.getGammaId(),
            attrData.getUri()}));
      }
      appendToDetails(AHTML.endMultiColumnTable());
      monitor.worked(calculateWork(0.10));
      checkForCancelledStatus(monitor);

      if (isFixOperationEnabled() && getItemsToFixCount() > 0) {
         File backupFolder = OseeData.getFile(getBackUpPrefix() + Lib.getDateTimeString() + File.separator);
         backupFolder.mkdirs();

         int workAmount = calculateWork(0.40) / getItemsToFixCount();
         for (AttrData attrData : attributesWithErrors) {
            Resource resource = attrData.getResource();
            ResourceUtil.backupResourceLocally(backupFolder, resource);
            applyFix(attrData);
            ResourceUtil.uploadResource(attrData.getGammaId(), resource);
            monitor.worked(workAmount);
         }
      }

      getSummary().append(String.format("Found [%s] %s", attributesWithErrors.size(), baseName));
      monitor.worked(calculateWork(0.10));
   }

   protected abstract String getBackUpPrefix();

   protected abstract void applyFix(AttrData attrData) throws OseeCoreException;

   protected abstract boolean isFixRequired(AttrData attrData, Resource resource) throws OseeCoreException;

   private final class FindAllWordAttributesNeedingFix extends AbstractOperation {
      private static final String GET_ATTRS =
         "SELECT DISTINCT(art.guid), attr.gamma_id, attr.uri FROM osee_attribute attr, osee_artifact art WHERE attr.attr_type_id = ? AND attr.art_id = art.art_id AND attr.uri is not null AND attr.uri != '' order by attr.gamma_id asc"; // and t1.attr_id = 1155574";

      private final List<AttrData> attributesWithErrors;

      public FindAllWordAttributesNeedingFix(String operationName, List<AttrData> attributesWithErrors) {
         super(operationName, Activator.PLUGIN_ID);
         this.attributesWithErrors = attributesWithErrors;
      }

      @Override
      protected void doWork(IProgressMonitor monitor) throws Exception {
         IAttributeType[] attributeTypes = new IAttributeType[] {WordTemplateContent, WholeWordContent};

         monitor.setTaskName("Load Attribute Data");
         List<AttrData> attrDatas = new ArrayList<AttrData>();
         for (IAttributeType attributeType : attributeTypes) {
            attrDatas.addAll(loadAttributeData(monitor, attributeType));
         }
         monitor.worked(calculateWork(0.20));

         if (!attrDatas.isEmpty()) {
            int totalAttrs = attrDatas.size();
            int work = calculateWork(0.80) / totalAttrs;
            for (int index = 0; index < attrDatas.size(); index++) {
               checkForCancelledStatus(monitor);
               AttrData attrData = attrDatas.get(index);
               monitor.setTaskName(String.format("[%s of %s] - guids[%s]", index, totalAttrs, attrData.getGuid()));
               checkAttributeData(attrData);
               int size = attributesWithErrors.size();
               if (size > 0 && size % 3000 == 0) {
                  getSummary().append("Index at break: " + index + " gamma: " + attrData.getGammaId());
                  break;
               }
               monitor.worked(work);
            }
         } else {
            monitor.worked(calculateWork(0.80));
         }
      }

      private void checkAttributeData(AttrData attrData) throws OseeCoreException {
         Resource resource = ResourceUtil.getResource(attrData.getUri());
         if (isFixRequired(attrData, resource)) {
            attrData.setResource(resource);
            attributesWithErrors.add(attrData);
         }
      }

      private List<AttrData> loadAttributeData(IProgressMonitor monitor, IAttributeType attributeType) throws OseeCoreException {
         List<AttrData> attrData = new ArrayList<AttrData>();
         JdbcStatement chStmt = ConnectionHandler.getStatement();
         try {
            chStmt.runPreparedQuery(GET_ATTRS, attributeType.getGuid());
            while (chStmt.next()) {
               checkForCancelledStatus(monitor);
               String uri = chStmt.getString("uri");
               if (Strings.isValid(uri)) {
                  attrData.add(new AttrData(chStmt.getString("gamma_Id"), chStmt.getString("guid"), uri));
               }
            }
         } finally {
            chStmt.close();
         }
         return attrData;
      }
   }

   private final static class ResourceUtil {

      public static Resource getResource(String resourcePath) throws OseeCoreException {
         Resource toReturn = null;
         ByteArrayOutputStream sourceOutputStream = new ByteArrayOutputStream();
         try {
            Map<String, String> parameterMap = new HashMap<String, String>();
            parameterMap.put("sessionId", ClientSessionManager.getSessionId());
            parameterMap.put("uri", resourcePath);
            String urlString =
               HttpUrlBuilderClient.getInstance().getOsgiServletServiceUrl(OseeServerContext.RESOURCE_CONTEXT,
                  parameterMap);

            AcquireResult result = HttpProcessor.acquire(new URL(urlString), sourceOutputStream);
            if (result.getCode() == HttpURLConnection.HTTP_OK) {
               toReturn = new Resource(resourcePath, result, sourceOutputStream.toByteArray());
            }
         } catch (Exception ex) {
            OseeExceptions.wrapAndThrow(ex);
         } finally {
            try {
               sourceOutputStream.close();
            } catch (IOException ex) {
               OseeExceptions.wrapAndThrow(ex);
            }
         }
         return toReturn;
      }

      public static void uploadResource(String gammaId, Resource resource) throws Exception {
         String fileName = resource.resourceName;
         Map<String, String> parameterMap = new HashMap<String, String>();
         parameterMap.put("sessionId", ClientSessionManager.getSessionId());
         parameterMap.put("is.overwrite.allowed", String.valueOf(true));
         parameterMap.put("protocol", "attr");
         parameterMap.put("seed", gammaId);

         String extension = Lib.getExtension(fileName);
         if (Strings.isValid(extension)) {
            parameterMap.put("extension", extension);
            int charToRemove = extension.length() + 1;
            fileName = fileName.substring(0, fileName.length() - charToRemove);
         }
         parameterMap.put("name", fileName);

         byte[] toUpload = resource.data.getBytes(resource.encoding);
         if (resource.wasZipped) {
            toUpload = Lib.compressStream(new ByteArrayInputStream(toUpload), resource.entryName);
         }

         String urlString =
            HttpUrlBuilderClient.getInstance().getOsgiServletServiceUrl(OseeServerContext.RESOURCE_CONTEXT,
               parameterMap);
         HttpProcessor.put(new URL(urlString), new ByteArrayInputStream(toUpload), resource.result.getContentType(),
            resource.result.getEncoding());
      }

      public static void backupResourceLocally(File backupFolder, Resource resource) throws IOException {
         InputStream inputStream = null;
         OutputStream outputStream = null;
         try {
            String path = resource.sourcePath;
            path = path.replaceAll("attr://", "");
            path = path.replaceAll("/", Lib.isWindows() ? "\\\\" : "/");
            File file = new File(backupFolder, path);
            File parent = file.getParentFile();
            if (parent != null) {
               parent.mkdirs();
            }
            outputStream = new FileOutputStream(file);

            inputStream = new ByteArrayInputStream(resource.rawBytes);
            Lib.inputStreamToOutputStream(inputStream, outputStream);
         } finally {
            if (inputStream != null) {
               inputStream.close();
            }
            if (outputStream != null) {
               outputStream.close();
            }
         }
      }
   }

   protected final static class Resource {
      private final String entryName;
      private final String resourceName;
      private final AcquireResult result;
      private final byte[] rawBytes;
      private final boolean wasZipped;
      private final String sourcePath;

      private String data;
      private String encoding;

      private Resource(String sourcePath, AcquireResult result, byte[] rawBytes) throws IOException {
         this.rawBytes = rawBytes;
         this.result = result;
         int index = sourcePath.lastIndexOf('/');
         this.sourcePath = sourcePath;
         this.resourceName = sourcePath.substring(index + 1, sourcePath.length());
         this.wasZipped = result.getContentType().contains("zip");
         if (wasZipped) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            this.entryName = Lib.decompressStream(new ByteArrayInputStream(rawBytes), outputStream);
            this.encoding = "UTF-8";
            this.data = new String(outputStream.toByteArray(), encoding);
         } else {
            this.data = new String(rawBytes, result.getEncoding());
            this.entryName = null;
            this.encoding = result.getEncoding();
         }
      }

      public String getData() {
         return data;
      }

      public void setData(String data) {
         this.data = data;
      }

   }

   protected final static class AttrData {
      private final String gammaId;
      private final String guid;
      private final String uri;
      private Resource resource;

      public AttrData(String gammaId, String guid, String uri) {
         super();
         this.gammaId = gammaId;
         this.guid = guid;
         this.uri = uri;
      }

      public void setResource(Resource resource) {
         this.resource = resource;
      }

      public Resource getResource() {
         return resource;
      }

      public String getGammaId() {
         return gammaId;
      }

      public String getGuid() {
         return guid;
      }

      public String getUri() {
         return uri;
      }
   }
}
