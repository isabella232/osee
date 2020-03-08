/*********************************************************************
 * Copyright (c) 2013 Boeing
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

package org.eclipse.osee.disposition.rest.internal;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.disposition.model.DispoSet;
import org.eclipse.osee.disposition.model.Note;
import org.eclipse.osee.disposition.model.OperationReport;
import org.eclipse.osee.disposition.model.OperationSummaryEntry;
import org.eclipse.osee.disposition.rest.util.DispoUtil;
import org.eclipse.osee.framework.core.enums.DispoOseeTypes;
import org.eclipse.osee.framework.core.util.JsonUtil;
import org.eclipse.osee.framework.jdk.core.type.BaseIdentity;
import org.eclipse.osee.jaxrs.JaxRsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Angel Avila
 */
public class DispoSetArtifact extends BaseIdentity<String> implements DispoSet {

   private final ArtifactReadable artifact;
   private final JaxRsApi jaxRsApi;

   public DispoSetArtifact(ArtifactReadable artifact, JaxRsApi jaxRsApi) {
      super(artifact.getIdString());
      this.artifact = artifact;
      this.jaxRsApi = jaxRsApi;
   }

   @Override
   public String getName() {
      return artifact.getName();
   }

   @Override
   public String getImportPath() {
      return artifact.getSoleAttributeAsString(DispoOseeTypes.DispoImportPath);
   }

   @Override
   public List<Note> getNotesList() {
      String notesJson = artifact.getSoleAttributeAsString(DispoOseeTypes.DispoNotesJson, "[]");
      return DispoUtil.jsonStringToList(notesJson, Note.class);
   }

   @Override
   public OperationReport getOperationSummary() {
      String operationSummaryJson = artifact.getSoleAttributeAsString(DispoOseeTypes.DispoOperationSummary, "{}");

      OperationReport summary = new OperationReport();
      List<OperationSummaryEntry> entries = new LinkedList<>();
      if (!operationSummaryJson.contains("entries")) {
         return summary;
      }
      JsonNode entriesNode = JsonUtil.readTree(operationSummaryJson).get("entries");
      entries = jaxRsApi.readValue(entriesNode.toString(), List.class);
      summary.setEntries(entries);
      return summary;
   }

   @Override
   public String toString() {
      return getName();
   }

   @Override
   public String getImportState() {
      return artifact.getSoleAttributeAsString(DispoOseeTypes.DispoImportState, "None");
   }

   @Override
   public String getDispoType() {
      return artifact.getSoleAttributeAsString(DispoOseeTypes.DispoConfig, "");
   }

   @Override
   public String getCiSet() {
      return artifact.getSoleAttributeAsString(DispoOseeTypes.DispoCiSet, "");
   }

   @Override
   public String getRerunList() {
      return artifact.getSoleAttributeAsString(DispoOseeTypes.DispoRerunList, "");
   }

   @Override
   public Date getTime() {
      return artifact.getSoleAttributeValue(DispoOseeTypes.DispoTime, null);
   }
}
