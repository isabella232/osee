/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.disposition.rest.internal;

import java.util.Date;
import org.eclipse.osee.disposition.model.DispoItem;
import org.eclipse.osee.disposition.rest.DispoConstants;
import org.eclipse.osee.disposition.rest.util.DispoUtil;
import org.eclipse.osee.framework.jdk.core.type.BaseIdentity;
import org.eclipse.osee.framework.jdk.core.type.Named;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Angel Avila
 */
public class DispoItemArtifact extends BaseIdentity<String> implements DispoItem, Comparable<Named> {

   private final ArtifactReadable artifact;

   public DispoItemArtifact(ArtifactReadable artifact) {
      super(artifact.getGuid());
      this.artifact = artifact;
   }

   @Override
   public String getName() {
      return artifact.getName();
   }

   @Override
   public JSONObject getDiscrepanciesList() {
      String discrepanciesJson = artifact.getSoleAttributeAsString(DispoConstants.DispoDiscrepanciesJson, "{}");
      return DispoUtil.asJSONObject(discrepanciesJson);
   }

   @Override
   public JSONArray getAnnotationsList() {
      String annotationsList = artifact.getSoleAttributeAsString(DispoConstants.DispoAnnotationsJson, "[]");
      return DispoUtil.asJSONArray(annotationsList);
   }

   @Override
   public String toString() {
      return getName();
   }

   @Override
   public String getAssignee() {
      return artifact.getSoleAttributeAsString(DispoConstants.DispoItemAssignee, "");
   }

   @Override
   public Date getCreationDate() {
      return artifact.getSoleAttributeValue(DispoConstants.DispoDateCreated);

   }

   @Override
   public Date getLastUpdate() {
      return artifact.getSoleAttributeValue(DispoConstants.DispoLastUpdated);
   }

   @Override
   public String getStatus() {
      return artifact.getSoleAttributeAsString(DispoConstants.DispoItemStatus);
   }

   @Override
   public String getVersion() {
      return artifact.getSoleAttributeAsString(DispoConstants.DispoItemVersion);
   }

   @Override
   public Boolean getNeedsRerun() {
      return artifact.getSoleAttributeValue(DispoConstants.DispoItemNeedsRerun, false);
   }

   @Override
   public String getTotalPoints() {
      return artifact.getSoleAttributeAsString(DispoConstants.DispoItemTotalPoints, "0");
   }

   @Override
   public int compareTo(Named other) {
      if (other != null && other.getName() != null && getName() != null) {
         return getName().compareTo(other.getName());
      }
      return -1;
   }

   @Override
   public String getMachine() {
      return artifact.getSoleAttributeAsString(DispoConstants.DispoItemMachine, "n/a");
   }

   @Override
   public String getCategory() {
      return artifact.getSoleAttributeAsString(DispoConstants.DispoItemCategory, "");
   }

   @Override
   public String getElapsedTime() {
      return artifact.getSoleAttributeAsString(DispoConstants.DispoItemElapsedTime, "0.0");
   }

   @Override
   public Boolean getAborted() {
      return artifact.getSoleAttributeValue(DispoConstants.DispoItemAborted, false);
   }


   @Override
   public String getItemNotes() {
      return artifact.getSoleAttributeAsString(DispoConstants.DispoItemItemNotes, "");
   }
}
