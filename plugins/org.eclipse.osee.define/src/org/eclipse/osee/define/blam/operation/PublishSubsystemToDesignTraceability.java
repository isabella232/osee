/*******************************************************************************
 * Copyright (c) 2004, 2008 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.define.blam.operation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.io.CharBackedInputStream;
import org.eclipse.osee.framework.jdk.core.util.io.xml.ExcelXmlWriter;
import org.eclipse.osee.framework.jdk.core.util.io.xml.ISheetWriter;
import org.eclipse.osee.framework.plugin.core.util.AIFile;
import org.eclipse.osee.framework.plugin.core.util.OseeData;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.skynet.blam.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;
import org.eclipse.swt.program.Program;

/**
 * @author Ryan D. Brooks
 */
public class PublishSubsystemToDesignTraceability extends AbstractBlam {
   private CharBackedInputStream charBak;
   private ISheetWriter excelWriter;

   @Override
   public String getName() {
      return "Publish Subsystem To Design Traceability";
   }

   private void init() throws IOException {
      charBak = new CharBackedInputStream();
      excelWriter = new ExcelXmlWriter(charBak.getWriter());
   }

   @Override
   public void runOperation(VariableMap variableMap, IProgressMonitor monitor) throws Exception {
      monitor.beginTask(getDescriptionUsage(), 100);

      List<Artifact> subsystems = variableMap.getArtifacts("Subsystem Root Artifacts");
      IOseeBranch branch = subsystems.get(0).getBranch();

      init();

      monitor.subTask("Aquiring Design Artifacts"); // bulk load for performance reasons
      ArtifactQuery.getArtifactListFromType(CoreArtifactTypes.SubsystemDesign, branch);
      monitor.worked(10);

      monitor.subTask("Aquiring Subsystem Requirements"); // bulk load for performance reasons
      ArtifactQuery.getArtifactListFromType(CoreArtifactTypes.SubsystemRequirementMSWord, branch);
      monitor.worked(60);

      int workIncrement = 30 / subsystems.size();
      for (Artifact subsystem : subsystems) {
         if (monitor.isCanceled()) {
            return;
         }
         monitor.worked(workIncrement);
         writeSubsystemDesignTraceability(subsystem);
      }

      excelWriter.endWorkbook();
      IFile iFile = OseeData.getIFile("SubsystemToDesignTrace_" + Lib.getDateTimeString() + ".xml");
      AIFile.writeToFile(iFile, charBak);
      Program.launch(iFile.getLocation().toOSString());
   }

   private void writeSubsystemDesignTraceability(Artifact subsystem) throws IOException, OseeCoreException {
      excelWriter.startSheet(subsystem.getName(), 200);
      excelWriter.writeRow(subsystem.getName() + " Subsystem To Design Traceability");

      excelWriter.writeRow("Subsystem Requirement", null, "Subsystem Design");
      excelWriter.writeRow(CoreAttributeTypes.ParagraphNumber.getName(), "Paragraph Title",
         CoreAttributeTypes.ParagraphNumber.getName(), "Paragraph Title");

      for (Artifact subsystemRequirement : subsystem.getDescendants()) {
         excelWriter.writeCell(subsystemRequirement.getSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, ""));
         excelWriter.writeCell(subsystemRequirement.getName());

         if (subsystemRequirement.isOfType(CoreArtifactTypes.SubsystemRequirementMSWord)) {
            boolean loopNeverRan = true;
            for (Artifact subsystemDesign : subsystemRequirement.getRelatedArtifacts(
               CoreRelationTypes.Design__Design)) {
               if (subsystemDesign.isOfType(CoreArtifactTypes.SubsystemDesign)) {
                  loopNeverRan = false;
                  excelWriter.writeCell(subsystemDesign.getSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, ""),
                     2);
                  excelWriter.writeCell(subsystemDesign.getName(), 3);
                  excelWriter.endRow();
               }
            }
            if (loopNeverRan) {
               excelWriter.endRow();
            }
         } else {
            excelWriter.writeCell("N/A - " + subsystemRequirement.getArtifactTypeName());
            excelWriter.endRow();
         }
      }

      excelWriter.endSheet();
   }

   @Override
   public String getDescriptionUsage() {
      return "Publish Subsystem To Design Traceability Tables";
   }

   @Override
   public String getXWidgetsXml() {
      return "<xWidgets><XWidget xwidgetType=\"XListDropViewer\" displayName=\"Subsystem Root Artifacts\" /></xWidgets>";
   }

   @Override
   public Collection<String> getCategories() {
      return Arrays.asList("Define.Publish");
   }
}