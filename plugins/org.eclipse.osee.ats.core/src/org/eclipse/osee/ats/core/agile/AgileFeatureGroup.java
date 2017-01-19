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
package org.eclipse.osee.ats.core.agile;

import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.agile.IAgileFeatureGroup;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.core.model.impl.AtsConfigObject;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.logger.Log;

/**
 * @author Donald G Dunne
 */
public class AgileFeatureGroup extends AtsConfigObject implements IAgileFeatureGroup {

   public AgileFeatureGroup(Log logger, IAtsServices services, ArtifactToken artifact) {
      super(logger, services, artifact);
   }

   @Override
   public String getTypeName() {
      return "Agile Feature Group";
   }

   @Override
   public long getTeamUuid() {
      long result = 0;
      try {
         ArtifactId agileTeam = services.getRelationResolver().getRelatedOrNull(artifact,
            AtsRelationTypes.AgileTeamToFeatureGroup_AgileTeam);
         if (agileTeam != null) {
            result = agileTeam.getId();
         }
      } catch (ArtifactDoesNotExist ex) {
         // do nothing
      }
      return result;
   }

}
