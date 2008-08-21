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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.framework.branch.management.IExportImportListener;
import org.eclipse.osee.framework.branch.management.exchange.ExportImportXml;
import org.eclipse.osee.framework.resource.management.Options;

/**
 * @author Roberto E. Escobar
 */
public abstract class AbstractExportItem implements Runnable {
   private String name;
   private String fileName;
   private int priority;
   private File writeLocation;
   private Options options;
   private Set<IExportImportListener> exportListeners;
   private String source;
   private boolean cancel;

   public AbstractExportItem(int priority, String name, String source) {
      this.name = name;
      this.fileName = name + ExportImportXml.XML_EXTENSION;
      this.priority = priority;
      this.options = null;
      this.cancel = false;
      this.source = source;
      this.exportListeners = Collections.synchronizedSet(new HashSet<IExportImportListener>());
   }

   public String getSource() {
      return this.source;
   }

   public String getFileName() {
      return fileName;
   }

   public String getName() {
      return name;
   }

   public int getPriority() {
      return priority;
   }

   public int getBufferSize() {
      return (int) Math.pow(2, 20);
   }

   public void setWriteLocation(File writeLocation) {
      this.writeLocation = writeLocation;
   }

   public File getWriteLocation() {
      return writeLocation;
   }

   public void setOptions(Options options) {
      this.options = options;
   }

   public Options getOptions() {
      return this.options;
   }

   public void addExportListener(IExportImportListener exportListener) {
      if (exportListener != null) {
         this.exportListeners.add(exportListener);
      }
   }

   public void removeExportListener(IExportImportListener exportListener) {
      if (exportListener != null) {
         this.exportListeners.remove(exportListener);
      }
   }

   public void cleanUp() {
      this.setWriteLocation(null);
      if (this.options != null) {
         this.options.clear();
      }
      this.exportListeners.clear();
   }

   private Writer createXmlWriter(File tempFolder, String name, int bufferSize) throws Exception {
      File indexFile = new File(tempFolder, name);
      Writer writer =
            new BufferedWriter(new OutputStreamWriter(new FileOutputStream(indexFile), ExportImportXml.XML_ENCODING),
                  bufferSize);
      writer.write(ExportImportXml.XML_HEADER);
      return writer;
   }

   public final void run() {
      notifyOnExportItemStarted();
      long startTime = System.currentTimeMillis();
      Writer writer = null;
      try {
         writer = createXmlWriter(getWriteLocation(), getFileName(), getBufferSize());
         ExportImportXml.openXmlNode(writer, ExportImportXml.DATA);
         if (isCancel() != true) {
            try {
               doWork(writer);
            } catch (Exception ex) {
               notifyOnExportException(ex);
            }
         }
         ExportImportXml.closeXmlNode(writer, ExportImportXml.DATA);
      } catch (Exception ex) {
         notifyOnExportException(ex);
      } finally {
         if (writer != null) {
            try {
               writer.close();
            } catch (IOException ex) {
               notifyOnExportException(ex);
            }
         }
         notifyOnExportItemCompleted(System.currentTimeMillis() - startTime);
      }
   }

   protected void notifyOnExportException(Throwable ex) {
      for (IExportImportListener listener : this.exportListeners) {
         listener.onException(getName(), ex);
      }
   }

   protected void notifyOnExportItemStarted() {
      for (IExportImportListener listener : this.exportListeners) {
         listener.onExportItemStarted(getName());
      }
   }

   protected void notifyOnExportItemCompleted(long timeToProcess) {
      for (IExportImportListener listener : this.exportListeners) {
         listener.onExportItemCompleted(getName(), timeToProcess);
      }
   }

   protected abstract void doWork(Appendable appendable) throws Exception;

   public void setCancel(boolean cancel) {
      this.cancel = cancel;
   }

   public boolean isCancel() {
      return this.cancel;
   }
}
