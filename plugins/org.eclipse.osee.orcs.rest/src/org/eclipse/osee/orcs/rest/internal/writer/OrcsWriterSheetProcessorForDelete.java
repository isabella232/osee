/*********************************************************************
 * Copyright (c) 2015 Boeing
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

package org.eclipse.osee.orcs.rest.internal.writer;

import org.eclipse.osee.framework.jdk.core.result.XResultData;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.jdk.core.util.io.xml.RowProcessor;
import org.eclipse.osee.orcs.rest.model.writer.reader.OwArtifactToken;
import org.eclipse.osee.orcs.rest.model.writer.reader.OwCollector;

/**
 * @author Donald G. Dunne
 */
public class OrcsWriterSheetProcessorForDelete implements RowProcessor {

   private final OwCollector collector;
   private int rowCount = 0;
   private final OrcsWriterFactory factory;
   private final XResultData result;

   public OrcsWriterSheetProcessorForDelete(OwCollector collector, XResultData result) {
      this.collector = collector;
      this.result = result;
      this.factory = new OrcsWriterFactory(collector);
   }

   @Override
   public void detectedRowAndColumnCounts(int rowCount, int columnCount) {
      // do nothing
   }

   @Override
   public void foundStartOfWorksheet(String sheetName) {
      // do nothing
   }

   @Override
   public void processCommentRow(String[] row) {
      // do nothing
   }

   @Override
   public void processEmptyRow() {
      // do nothing
   }

   @Override
   public void processHeaderRow(String[] headerRow) {
      rowCount++;
      if (headerRow.length > 1) {
         result.warning("More than 1 column found in DELETE sheet.  Only column 1 is processed");
      }
   }

   @Override
   public void processRow(String[] row) {
      rowCount++;
      OwArtifactToken artifact = null;
      String value = row[0];
      if (Strings.isNumeric(value)) {
         artifact = new OwArtifactToken(Long.valueOf(value), "unknown");
      }
      if (Strings.isValid(value)) {
         artifact = factory.getOrCreateToken(value);
      }
      if (artifact != null) {
         artifact.setData(OrcsWriterUtil.getData(OrcsWriterUtil.DELETE_SHEET_NAME, rowCount, 0, value));
         collector.getDelete().add(artifact);
      }
   }

   @Override
   public void reachedEndOfWorksheet() {
      // do nothing
   }

}
