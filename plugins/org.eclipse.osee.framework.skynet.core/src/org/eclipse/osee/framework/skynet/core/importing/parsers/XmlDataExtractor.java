/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.framework.skynet.core.importing.parsers;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStreamReader;
import java.net.URI;
import org.eclipse.osee.framework.core.data.ArtifactTypeToken;
import org.eclipse.osee.framework.core.operation.OperationLogger;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.skynet.core.importing.operations.RoughArtifactCollector;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Ryan D. Brooks
 */
public class XmlDataExtractor extends AbstractArtifactExtractor {

   @Override
   protected void extractFromSource(OperationLogger logger, URI source, RoughArtifactCollector collector) throws Exception {
      ArtifactTypeToken primaryArtifactType =
         ServiceUtil.getOrcsTokenService().getArtifactType(Lib.removeExtension(new File(source).getName()));
      XMLReader xmlReader = XMLReaderFactory.createXMLReader();
      xmlReader.setContentHandler(new XmlDataSaxHandler(collector, primaryArtifactType));
      xmlReader.parse(new InputSource(new InputStreamReader(source.toURL().openStream(), "UTF-8")));
   }

   @Override
   public FileFilter getFileFilter() {
      return new FileFilter() {
         @Override
         public boolean accept(File file) {
            return file.isDirectory() || file.isFile() && file.getName().endsWith(".xml");
         }
      };
   }

   @Override
   public String getName() {
      return "Excel XML Data";
   }

   @Override
   public String getDescription() {
      return "Extract Data from xml of the form <row><cell></cell>*</row>* like that created by Excel data export.";
   }

   @Override
   public boolean usesTypeList() {
      return true;
   }
}