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
package org.eclipse.osee.ats.core.client.search;

import static org.eclipse.osee.framework.core.enums.DeletionFlag.EXCLUDE_DELETED;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.core.client.internal.AtsClientService;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * Return all ATS Objects that a user is related to through logs, review roles, defects and etc.
 * 
 * @author Donald G. Dunne
 */
public class UserRelatedToAtsObjectSearch {

   private final boolean activeObjectsOnly;
   private final IAtsUser atsUser;

   public UserRelatedToAtsObjectSearch(IAtsUser user, boolean activeObjectsOnly) {
      this.atsUser = user;
      this.activeObjectsOnly = activeObjectsOnly;
   }

   public Collection<Artifact> getResults() throws OseeCoreException {
      List<Artifact> arts = new ArrayList<>();

      if (activeObjectsOnly) {
         arts.addAll(ArtifactQuery.getArtifactListFromAttributeKeywords(AtsUtilCore.getAtsBranch(), atsUser.getUserId(),
            false, EXCLUDE_DELETED, false, AtsAttributeTypes.CurrentState));
      } else {
         arts.addAll(
            ArtifactQuery.getArtifactListFromAttributeKeywords(AtsUtilCore.getAtsBranch(), atsUser.getUserId(), false,
               EXCLUDE_DELETED, false, AtsAttributeTypes.CurrentState, AtsAttributeTypes.State, AtsAttributeTypes.Log));
      }

      User user = AtsClientService.get().getUserServiceClient().getOseeUser(atsUser);
      arts.addAll(user.getRelatedArtifacts(AtsRelationTypes.TeamLead_Team));
      arts.addAll(user.getRelatedArtifacts(AtsRelationTypes.TeamMember_Team));
      arts.addAll(user.getRelatedArtifacts(AtsRelationTypes.FavoriteUser_Artifact));
      arts.addAll(user.getRelatedArtifacts(AtsRelationTypes.SubscribedUser_Artifact));
      arts.addAll(user.getRelatedArtifacts(AtsRelationTypes.PrivilegedMember_Team));

      return arts;
   }

}
