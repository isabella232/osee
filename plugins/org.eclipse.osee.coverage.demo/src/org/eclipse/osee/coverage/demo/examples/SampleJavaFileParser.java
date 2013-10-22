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
package org.eclipse.osee.coverage.demo.examples;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.coverage.model.CoverageItem;
import org.eclipse.osee.coverage.model.CoverageOptionManager;
import org.eclipse.osee.coverage.model.CoverageUnit;
import org.eclipse.osee.coverage.model.CoverageUnitFactory;
import org.eclipse.osee.coverage.model.ICoverageUnitFileContentsProvider;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;

/**
 * @author Donald G. Dunne
 */
public final class SampleJavaFileParser {

   private static final Pattern packagePattern = Pattern.compile("package\\s+(.*);");
   private static final Pattern methodPattern =
      Pattern.compile("\\s+(public|private)\\s(\\w+)\\s(\\w+)\\(.*\\)\\s+\\{\\s*");
   private static final Pattern executeLine = Pattern.compile("^(.*)\\s+//\\s+(\\w+),\\s+(\\w+),\\s+([\\w\\|]+)$");

   private SampleJavaFileParser() {
      // Static Utility Class
   }

   public static CoverageUnit createCodeUnit(URL url, ICoverageUnitFileContentsProvider fileContentsProvider) throws OseeCoreException {
      Conditions.checkNotNull(url, "url", "Valid filename must be specified");
      InputStream inputStream = null;
      CoverageUnit fileCoverageUnit = null;
      try {
         inputStream = url.openStream();
         Conditions.checkNotNull(inputStream, "input stream", "File doesn't exist [%s]", url);
         // Store file as CoverageUnit
         File file = new File(url.getFile());
         String filename = file.getCanonicalFile().getName();
         fileCoverageUnit = CoverageUnitFactory.createCoverageUnit(null, filename, url.getFile(), fileContentsProvider);
         String fileStr = Lib.inputStreamToString(inputStream);
         Matcher m = packagePattern.matcher(fileStr);
         if (m.find()) {
            fileCoverageUnit.setNamespace(m.group(1));
         } else {
            throw new OseeArgumentException("Can't find package for [%s]", url);
         }
         fileCoverageUnit.setFileContents(fileStr);
         CoverageUnit coverageUnit = null;
         int lineNum = 0;
         for (String line : fileStr.split("(\r\n|\n)")) {
            if (line.contains("IGNORE")) {
               continue;
            }
            lineNum++;
            // Determine if method; store as CoverageUnit
            m = methodPattern.matcher(line);
            if (m.find()) {
               String name = m.group(3);
               coverageUnit =
                  CoverageUnitFactory.createCoverageUnit(fileCoverageUnit, name, "Line " + lineNum,
                     fileContentsProvider);
               // Note: CoverageUnit's orderNumber is set by executeLine match below
               fileCoverageUnit.addCoverageUnit(coverageUnit);
               // Duplicate this method as error case for importing
               if (filename.contains("AuxPowerUnit2") && name.equals("clear")) {
                  CoverageUnit duplicateCoverageUnit =
                     CoverageUnitFactory.createCoverageUnit(fileCoverageUnit, name, "Line " + lineNum,
                        fileContentsProvider);
                  duplicateCoverageUnit.setOrderNumber("2");
                  fileCoverageUnit.addCoverageUnit(duplicateCoverageUnit);
                  CoverageItem item = new CoverageItem(duplicateCoverageUnit, CoverageOptionManager.Not_Covered, "1");
                  item.setName("return super.getColumn(index)");
                  duplicateCoverageUnit.addCoverageItem(item);
               }
            }
            // Determine if executable coverage line; store as CoverageItem
            m = executeLine.matcher(line);
            if (m.find()) {
               String lineText = m.group(1);
               String methodNum = m.group(2);
               String executeNum = m.group(3);
               String testUnits = m.group(4);
               boolean covered = !testUnits.equals("n");
               CoverageItem coverageItem =
                  new CoverageItem(coverageUnit,
                     covered ? CoverageOptionManager.Test_Unit : CoverageOptionManager.Not_Covered, executeNum);
               coverageUnit.setOrderNumber(methodNum);
               coverageItem.setName(lineText);
               coverageItem.setOrderNumber(executeNum);
               coverageUnit.addCoverageItem(coverageItem);
               if (covered) {
                  for (String testUnitName : testUnits.split("\\|")) {
                     coverageItem.addTestUnitName(testUnitName);
                  }
               }
            }

         }
      } catch (IOException ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return fileCoverageUnit;
   }
}
