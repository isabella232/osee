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
package org.eclipse.osee.ats.core.client.search;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.MultipleArtifactsExist;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * @author John Misinco
 */
public class AtsArtifactQuery {

   public static Artifact getArtifactFromId(String guidOrAtsId) throws OseeCoreException {
      List<Artifact> artifacts = new LinkedList<Artifact>();

      if (GUID.isValid(guidOrAtsId)) {
         artifacts.add(ArtifactQuery.getArtifactFromId(guidOrAtsId, AtsUtilCore.getAtsBranchToken()));
      } else {
         artifacts.addAll(ArtifactQuery.getArtifactListFromAttributeValues(AtsAttributeTypes.AtsId,
            Collections.singleton(guidOrAtsId), AtsUtilCore.getAtsBranchToken(), 1));
      }

      if (artifacts.isEmpty()) {
         throw new ArtifactDoesNotExist("AtsArtifactQuery: No artifact found with id %s on ATS branch", guidOrAtsId);
      }
      if (artifacts.size() > 1) {
         throw new MultipleArtifactsExist("%d artifacts found with id %s", artifacts.size(), guidOrAtsId);
      }
      return artifacts.iterator().next();
   }

   public static List<Artifact> getArtifactListFromIds(Collection<String> guidsOrAtsIds) throws OseeCoreException {
      List<Artifact> toReturn = new LinkedList<Artifact>();
      List<String> guids = new LinkedList<String>();
      List<String> atsIds = new LinkedList<String>();
      for (String guidOrAtsId : guidsOrAtsIds) {
         if (GUID.isValid(guidOrAtsId)) {
            guids.add(guidOrAtsId);
         } else {
            atsIds.add(guidOrAtsId.toUpperCase());
         }
      }

      if (!guids.isEmpty()) {
         List<Artifact> fromIds = ArtifactQuery.getArtifactListFromIds(guids, AtsUtilCore.getAtsBranchToken());
         toReturn.addAll(fromIds);
      }

      if (!atsIds.isEmpty()) {
         List<Artifact> fromIds =
            ArtifactQuery.getArtifactListFromAttributeValues(AtsAttributeTypes.AtsId, atsIds,
               AtsUtilCore.getAtsBranchToken(), atsIds.size());
         toReturn.addAll(fromIds);
      }

      return toReturn;
   }

}
