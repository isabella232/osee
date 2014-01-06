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
package org.eclipse.osee.define.relation.Import;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.io.xml.ExcelSaxHandler;
import org.eclipse.osee.framework.jdk.core.util.io.xml.RowProcessor;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Ryan D. Brooks
 */
public class RelationImporter implements RowProcessor {
   private static final int leadingColumnCount = 2;
   private final ExcelSaxHandler excelHandler;
   private final XMLReader xmlReader;
   private Artifact[] columnArtifacts;
   private IProgressMonitor monitor;
   private boolean done;
   private final Branch branch;

   public RelationImporter(Branch branch) throws SAXException {
      this.branch = branch;
      excelHandler = new ExcelSaxHandler(this, true, true);

      xmlReader = XMLReaderFactory.createXMLReader();
      xmlReader.setContentHandler(excelHandler);
   }

   public void extractRelationsFromSheet(InputStream importStream, IProgressMonitor monitor) throws IOException, SAXException {
      this.monitor = monitor;
      xmlReader.parse(new InputSource(importStream));
   }

   @Override
   public void processRow(String[] row) throws OseeCoreException {
      if (done) {
         return;
      }
      monitor.worked(1);
      Collection<Artifact> artifacts =
         ArtifactQuery.getArtifactListFromTypeAndAttribute(CoreArtifactTypes.SubsystemRequirementMSWord,
            CoreAttributeTypes.ParagraphNumber, row[1], branch);

      Artifact rowArtifact = getSoleArtifact(artifacts);

      if (!row[0].equals(rowArtifact.getName())) {
         System.out.printf("Warning %s != %s%n", row[0], rowArtifact.getName());
      }
      monitor.subTask(rowArtifact.getName());
      for (int i = 0; i < columnArtifacts.length; i++) {
         String rationale = row[i + leadingColumnCount];
         if (rationale != null) {
            if (rationale.equalsIgnoreCase("x")) {
               rationale = "";
            }
            columnArtifacts[i].addRelation(RelationOrderBaseTypes.USER_DEFINED,
               CoreRelationTypes.Allocation__Requirement, rowArtifact, rationale);
            columnArtifacts[i].persist(getClass().getSimpleName());
         }
      }
   }

   private Artifact getSoleArtifact(Collection<Artifact> artifacts) throws OseeArgumentException {
      Artifact artifactResult = null;
      boolean soleArtifact = true;
      for (Artifact artifact : artifacts) {
         if (soleArtifact) {
            soleArtifact = false;
         } else {
            throw new OseeArgumentException("Found more than one match for [%s]", artifact);
         }
         artifactResult = artifact;
      }
      return artifactResult;
   }

   @Override
   public void processHeaderRow(String[] row) {
      monitor.setTaskName("Aquire Column Artifacts");
      columnArtifacts = new Artifact[row.length - leadingColumnCount];
      for (int i = 0; i < columnArtifacts.length; i++) {
         monitor.worked(1);
         try {
            Collection<Artifact> artifacts =
               ArtifactQuery.getArtifactListFromTypeAndName(CoreArtifactTypes.Component, row[i + leadingColumnCount],
                  branch);

            columnArtifacts[i] = getSoleArtifact(artifacts);
            monitor.subTask(columnArtifacts[i].getName());
         } catch (Exception ex) {
            System.out.println(ex);
         }
      }
      System.out.println(Arrays.deepToString(columnArtifacts));
      monitor.setTaskName("Relate Row Artifacts");
   }

   @Override
   public void processEmptyRow() {
      // do nothing
   }

   @Override
   public void processCommentRow(String[] row) {
      // do nothing
   }

   @Override
   public void reachedEndOfWorksheet() {
      monitor.done();
      done = true;
   }

   @Override
   public void detectedRowAndColumnCounts(int rowCount, int columnCount) {
      monitor.beginTask("Importing Relations", rowCount + columnCount - leadingColumnCount);
   }

   @Override
   public void foundStartOfWorksheet(String sheetName) {
      // do nothing
   }
}
