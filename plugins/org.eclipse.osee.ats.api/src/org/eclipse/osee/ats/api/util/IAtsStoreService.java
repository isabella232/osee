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
package org.eclipse.osee.ats.api.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactTypeId;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.core.data.AttributeTypeToken;
import org.eclipse.osee.framework.core.data.IArtifactType;

public interface IAtsStoreService {

   public static final String ART_TYPE_FROM_ID_QUERY =
      "select art_id, art_type_id from osee_artifact where art_id in (%s)";

   IAtsChangeSet createAtsChangeSet(String comment, IAtsUser user);

   List<IAtsWorkItem> reload(Collection<IAtsWorkItem> workItems);

   boolean isDeleted(IAtsObject atsObject);

   String getGuid(IAtsObject atsObject);

   boolean isAttributeTypeValid(IAtsObject atsObject, AttributeTypeId attributeType);

   boolean isAttributeTypeValid(ArtifactId artifact, AttributeTypeId attributeType);

   /**
    * Uses artifact type inheritance to retrieve all TeamWorkflow artifact types
    */
   Set<IArtifactType> getTeamWorkflowArtifactTypes();

   AttributeTypeId getAttributeType(String attrTypeName);

   IArtifactType getArtifactType(ArtifactId artifact);

   IArtifactType getArtifactType(IAtsObject atsObject);

   boolean isDateType(AttributeTypeId attributeType);

   boolean isOfType(ArtifactId artifact, ArtifactTypeId... artifactType);

   IArtifactType getArtifactType(Long artTypeId);

   void executeChangeSet(String comment, IAtsObject atsObject);

   void executeChangeSet(String comment, Collection<? extends IAtsObject> atsObjects);

   Map<Long, IArtifactType> getArtifactTypes(Collection<Long> artIds);

   Collection<AttributeTypeToken> getAttributeTypes();

   boolean isChangedInDb(IAtsWorkItem workItem);

   boolean isOfType(IAtsObject atsObject, IArtifactType artifactType);

   void clearCaches(IAtsWorkItem workItem);

}
