/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.rest.internal.config;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.framework.jdk.core.util.io.xml.ExcelXmlWriter;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Angel Avila
 */
public class TypeCountWriter {

   private final OrcsApi orcsApi;

   public TypeCountWriter(OrcsApi orcsApi) {
      this.orcsApi = orcsApi;
   }

   public void write(long branchUuid, Set<Long> newArts, Set<Long> modifiedArts, Set<Long> deletedArts, List<Long> artTypes, List<Long> attrTypes, OutputStream outputStream) {
      try {
         Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
         ExcelXmlWriter sheetWriter = new ExcelXmlWriter(writer);

         List<IAttributeType> attributes = getAttrTypes(attrTypes);
         String[] headers = getHeaders(attributes);
         int columns = headers.length;
         sheetWriter.startSheet("Type Count Report", headers.length);
         sheetWriter.writeRow((Object[]) headers);

         List<IArtifactType> artifactTypes = getTypes(artTypes);

         if (!newArts.isEmpty()) {
            ResultSet<ArtifactReadable> newArtifacts =
               orcsApi.getQueryFactory().fromBranch(branchUuid).andTypeEquals(artifactTypes).andUuids(newArts).getResults();
            for (ArtifactReadable art : newArtifacts) {
               String[] row = new String[columns];
               int index = 0;

               row[index++] = art.getName();
               row[index++] = "NEW";
               row[index++] = art.getArtifactType().toString();
               for (IAttributeType type : attributes) {
                  row[index++] = art.getAttributeValues(type).toString();
               }

               sheetWriter.writeRow((Object[]) row);
            }
         }

         if (!modifiedArts.isEmpty()) {
            ResultSet<ArtifactReadable> modifiedArtifacts =
               orcsApi.getQueryFactory().fromBranch(branchUuid).andTypeEquals(artifactTypes).andUuids(modifiedArts).getResults();
            for (ArtifactReadable art : modifiedArtifacts) {
               String[] row = new String[columns];
               int index = 0;

               row[index++] = art.getName();
               row[index++] = "MODIFIED";
               row[index++] = art.getArtifactType().toString();
               for (IAttributeType type : attributes) {
                  row[index++] = art.getAttributeValues(type).toString();
               }
               sheetWriter.writeRow((Object[]) row);
            }
         }

         if (!deletedArts.isEmpty()) {
            ResultSet<ArtifactReadable> deletedArtifacts =
               orcsApi.getQueryFactory().fromBranch(branchUuid).andTypeEquals(artifactTypes).andUuids(deletedArts).getResults();
            for (ArtifactReadable art : deletedArtifacts) {
               String[] row = new String[columns];
               int index = 0;

               row[index++] = art.getName();
               row[index++] = "DELETED";
               row[index++] = art.getArtifactType().toString();
               for (IAttributeType type : attributes) {
                  row[index++] = art.getAttributeValues(type).toString();
               }
               sheetWriter.writeRow((Object[]) row);
            }
         }

         sheetWriter.endSheet();
         sheetWriter.endWorkbook();
      } catch (Exception ex) {
         throw new OseeCoreException(ex);
      }
   }

   private List<IArtifactType> getTypes(List<Long> typeIds) {
      List<IArtifactType> toReturn = new ArrayList<IArtifactType>();

      Collection<? extends IArtifactType> allTypes = orcsApi.getOrcsTypes().getArtifactTypes().getAll();
      for (IArtifactType type : allTypes) {
         if (typeIds.contains(type.getGuid())) {
            toReturn.add(type);
         }
      }
      return toReturn;
   }

   private List<IAttributeType> getAttrTypes(List<Long> typeIds) {
      List<IAttributeType> toReturn = new ArrayList<IAttributeType>();

      Collection<? extends IAttributeType> allTypes = orcsApi.getOrcsTypes().getAttributeTypes().getAll();
      for (IAttributeType type : allTypes) {
         if (typeIds.contains(type.getGuid())) {
            toReturn.add(type);
         }
      }
      return toReturn;
   }

   private String[] getHeaders(List<IAttributeType> types) {
      orcsApi.getOrcsTypes().getArtifactTypes().getAll();
      String[] toReturn = new String[types.size() + 3];
      int index = 0;
      toReturn[index++] = "Name";
      toReturn[index++] = "Mod Type";
      toReturn[index++] = "Art Type";
      for (IAttributeType type : types) {
         toReturn[index++] = type.getName();
      }
      return toReturn;
   }
}
