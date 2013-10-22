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
package org.eclipse.osee.framework.skynet.core.attribute.providers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.util.HttpProcessor;
import org.eclipse.osee.framework.core.util.HttpProcessor.AcquireResult;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.OseeData;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.attribute.utils.AttributeURL;
import org.eclipse.osee.framework.skynet.core.internal.Activator;

/**
 * @author Roberto E. Escobar
 */
public class MappedAttributeDataProvider extends AbstractAttributeDataProvider implements ICharacterAttributeDataProvider {
   private String localUri;
   private String remoteUri;
   private IFile backingFile;

   public MappedAttributeDataProvider(Attribute<?> attribute) {
      super(attribute);
      this.remoteUri = null;
      this.localUri = null;
      this.backingFile = null;
   }

   @Override
   public Object[] getData() {
      return new Object[] {"", remoteUri};
   }

   @Override
   public String getDisplayableString() {
      return isFromLocalWorkspace() ? this.localUri : "Remote Content";
   }

   @Override
   public void loadData(Object... objects) {
      if (objects != null && objects.length > 1) {
         remoteUri = (String) objects[1];
      }
   }

   private String getOutfileName() throws OseeCoreException {
      StringBuilder builder = new StringBuilder();
      //TestRunOperator operator = new TestRunOperator(getAttribute().getArtifact());
      builder.append(getAttribute().getArtifact().getName());
      builder.append(".");
      //builder.append(operator.getChecksum());
      //builder.append(".");
      String extension = null; //operator.getOutfileExtension();
      if (!Strings.isValid(extension)) {
         extension = getAttribute().getAttributeType().getFileTypeExtension();
      }
      builder.append(extension);
      return builder.toString();
   }

   @Override
   public void persist(int storageId) throws OseeCoreException {
      try {
         if (isFromLocalWorkspace()) {
            InputStream inputStream = null;
            try {
               URI sourceUri = new URI(localUri);
               inputStream = sourceUri.toURL().openStream();
               byte[] compressed = Lib.compressStream(inputStream, getOutfileName());
               URL url = AttributeURL.getStorageURL(storageId, getAttribute().getArtifact().getGuid(), "zip");
               URI uri = HttpProcessor.save(url, new ByteArrayInputStream(compressed), "applization/zip", "ISO-8859-1");
               if (uri != null) {
                  this.remoteUri = uri.toASCIIString();
                  this.localUri = null;
               }
            } finally {
               if (inputStream != null) {
                  inputStream.close();
               }
            }
         }
      } catch (OseeCoreException ex) {
         throw ex; // keep exceptions of type OseeCoreException from being unnecessarily wrapped
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
   }

   @Override
   public void purge() throws OseeCoreException {
      try {
         if (isRemoteUriValid()) {
            URL url = AttributeURL.getAcquireURL(remoteUri);
            String response = HttpProcessor.delete(url);
            if (response != null && response.equals("Deleted: " + remoteUri)) {
               remoteUri = null;
               if (isBackingFileValid()) {
                  backingFile.delete(true, null);
               }
            }
         }
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
   }

   @Override
   public void setDisplayableString(String toDisplay) {
      // Do Nothing
   }

   @Override
   public String getValueAsString() {
      if (isRemoteUriValid() && isBackingFileValid() != true) {
         try {
            this.backingFile = requestRemoteFile();
            this.localUri = null;
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
      String toReturn = this.localUri;
      if (isBackingFileValid()) {
         toReturn = backingFile.getLocationURI().toASCIIString();
      }
      return toReturn;
   }

   private boolean isBackingFileValid() {
      return backingFile != null && backingFile.isAccessible();
   }

   @Override
   public boolean setValue(String value) {
      // DO NOTHING
      return false;
   }

   private IFile requestRemoteFile() throws Exception {
      IFile file = null;
      ByteArrayOutputStream downloadStream = new ByteArrayOutputStream();
      URL url = AttributeURL.getAcquireURL(remoteUri);
      AcquireResult results = HttpProcessor.acquire(url, downloadStream);
      if (results.wasSuccessful()) {
         ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(downloadStream.toByteArray()));
         ZipEntry entry = zipInputStream.getNextEntry();

         file = OseeData.getIFile(entry.getName(), zipInputStream, true);
      }
      return file;
   }

   public void setLocalUri(String localUri) {
      this.localUri = localUri;
   }

   private boolean isRemoteUriValid() {
      return remoteUri != null && remoteUri.length() > 0;
   }

   private boolean isFromLocalWorkspace() {
      boolean toReturn = isRemoteUriValid() != true;
      try {
         //toReturn |= new TestRunOperator(getAttribute().getArtifact()).isFromLocalWorkspace();
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return toReturn;
   }
}
