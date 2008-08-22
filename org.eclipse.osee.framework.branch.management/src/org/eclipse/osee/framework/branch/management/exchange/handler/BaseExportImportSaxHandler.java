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
package org.eclipse.osee.framework.branch.management.exchange.handler;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.framework.branch.management.exchange.ExportImportXml;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.jdk.core.util.io.xml.AbstractSaxHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Roberto E. Escobar
 */
public abstract class BaseExportImportSaxHandler extends AbstractSaxHandler {
   protected final static String STRING_CONTENT = "stringContent";
   protected final static String BINARY_CONTENT_LOCATION = "binaryContentLocation";
   private Map<String, String> dataMap;

   protected BaseExportImportSaxHandler() {
      super();
      this.dataMap = new HashMap<String, String>();
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.jdk.core.util.io.xml.AbstractSaxHandler#startElementFound(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
    */
   @Override
   public void startElementFound(String uri, String localName, String name, Attributes attributes) throws SAXException {
      try {
         if (localName.equalsIgnoreCase(ExportImportXml.ENTRY)) {
            handleEntry(attributes);
         } else if (localName.equalsIgnoreCase(ExportImportXml.STRING_CONTENT)) {
            handleStringContent(attributes);
         } else if (localName.equalsIgnoreCase(ExportImportXml.BINARY_CONTENT)) {
            handleBinaryContent(attributes);
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
         if (localName.equalsIgnoreCase(ExportImportXml.ENTRY)) {
            finishEntry();
         } else if (localName.equalsIgnoreCase(ExportImportXml.STRING_CONTENT)) {
            finishStringContent();
         } else if (localName.equalsIgnoreCase(ExportImportXml.BINARY_CONTENT)) {
            finishBinaryContent();
         }
      } catch (Exception ex) {
         throw new IllegalStateException(ex);
      }
   }

   private void handleEntry(Attributes attributes) {
      this.dataMap.clear();
      int attributeCount = attributes.getLength();
      for (int index = 0; index < attributeCount; index++) {
         String columnName = attributes.getLocalName(index);
         String value = attributes.getValue(index);
         if (Strings.isValid(value) && !value.equals("null")) {
            this.dataMap.put(columnName, value);
         }
      }
   }

   private void handleBinaryContent(Attributes attributes) {
      this.dataMap.put(BINARY_CONTENT_LOCATION, attributes.getValue("location"));
   }

   private void handleStringContent(Attributes attributes) {
      // Do Nothing
   }

   private void finishEntry() {
      if (this.dataMap.isEmpty() != true) {
         try {
            processData(this.dataMap);
         } catch (Exception ex) {
            throw new IllegalStateException("Processing data - ", ex);
         }
      }
      this.dataMap.clear();
   }

   private void finishBinaryContent() {
      // Do Nothing
   }

   private void finishStringContent() {
      this.dataMap.put(STRING_CONTENT, getContents());
   }

   protected abstract void processData(Map<String, String> dataMap) throws Exception;
}
