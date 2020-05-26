/*********************************************************************
 * Copyright (c) 2017 Boeing
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

package org.eclipse.osee.ats.core.agile;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.agile.IAgileProgram;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.core.model.impl.AtsConfigObject;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.logger.Log;

/**
 * @author Donald G Dunne
 */
public class AgileProgram extends AtsConfigObject implements IAgileProgram {

   public AgileProgram(Log logger, AtsApi atsApi, ArtifactToken artifact) {
      super(logger, atsApi, artifact, AtsArtifactTypes.AgileProgram);
   }

   @Override
   public List<Long> getTeamIds() {
      List<Long> ids = new ArrayList<>();
      for (ArtifactToken child : atsApi.getRelationResolver().getChildren(artifact)) {
         if (child.isOfType(AtsArtifactTypes.AgileTeam)) {
            ids.add(child.getId());
         }
      }
      return ids;
   }

   public static IAgileProgram construct(ArtifactId progArt, AtsApi atsApi) {
      return new AgileProgram(atsApi.getLogger(), atsApi, atsApi.getQueryService().getArtifact(progArt));
   }

}
