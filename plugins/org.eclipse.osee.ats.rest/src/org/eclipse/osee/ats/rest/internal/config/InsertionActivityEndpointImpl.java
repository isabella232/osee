/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.rest.internal.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;
import org.eclipse.osee.ats.api.IAtsConfigObject;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.insertion.IAtsInsertionActivity;
import org.eclipse.osee.ats.api.insertion.InsertionActivityEndpointApi;
import org.eclipse.osee.ats.api.insertion.JaxInsertionActivity;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.core.users.AtsCoreUsers;
import org.eclipse.osee.ats.impl.IAtsServer;
import org.eclipse.osee.ats.impl.config.BaseConfigEndpointImpl;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * Donald G. Dunne
 */
public class InsertionActivityEndpointImpl extends BaseConfigEndpointImpl<JaxInsertionActivity> implements InsertionActivityEndpointApi {

   private final long insertionUuid;

   public InsertionActivityEndpointImpl(IAtsServer atsServer) {
      this(atsServer, 0L);
   }

   public InsertionActivityEndpointImpl(IAtsServer atsServer, long insertionUuid) {
      super(AtsArtifactTypes.InsertionActivity, null, atsServer);
      this.insertionUuid = insertionUuid;
   }

   @PUT
   @Override
   public Response update(JaxInsertionActivity activity) throws Exception {
      ArtifactReadable artifact = atsServer.getArtifactByUuid(activity.getUuid());
      if (artifact == null) {
         throw new OseeStateException("Artifact with uuid %d not found", activity.getUuid());
      }
      IAtsChangeSet changes =
         atsServer.getStoreService().createAtsChangeSet("Create " + artifactType.getName(), AtsCoreUsers.SYSTEM_USER);
      ArtifactReadable configArtifact =
         (ArtifactReadable) changes.createArtifact(artifactType, activity.getName(), GUID.create(), activity.getUuid());
      IAtsConfigObject configObject = atsServer.getConfigItemFactory().getConfigObject(configArtifact);
      if (!configArtifact.getName().equals(activity.getName())) {
         changes.setSoleAttributeValue(configObject, CoreAttributeTypes.Name, activity.getName());
      }
      changes.execute();
      return Response.created(new URI("/" + activity.getUuid())).build();
   }

   @Override
   public JaxInsertionActivity getConfigObject(ArtifactId artifact) {
      JaxInsertionActivity jaxInsertion = new JaxInsertionActivity();
      IAtsInsertionActivity insertion = atsServer.getConfigItemFactory().getInsertionActivity(artifact);
      jaxInsertion.setName(insertion.getName());
      jaxInsertion.setUuid(insertion.getUuid());
      jaxInsertion.setActive(insertion.isActive());
      jaxInsertion.setDescription(insertion.getDescription());
      return jaxInsertion;
   }

   @Override
   public List<JaxInsertionActivity> getObjects() {
      List<JaxInsertionActivity> insertions = new ArrayList<JaxInsertionActivity>();
      if (insertionUuid == 0L) {
         for (ArtifactReadable art : atsServer.getQuery().andIsOfType(artifactType).getResults()) {
            insertions.add(getConfigObject(art));
         }
      } else {
         for (ArtifactReadable activityArt : atsServer.getArtifactByUuid(insertionUuid).getRelated(
            AtsRelationTypes.InsertionToInsertionActivity_InsertionActivity)) {
            JaxInsertionActivity activity = getConfigObject(activityArt);
            activity.setInsertionUuid(insertionUuid);
            insertions.add(activity);
         }
      }
      return insertions;
   }

   @Override
   protected void create(JaxInsertionActivity jaxInsertionActivity, ArtifactId insertionActivityArtId, IAtsChangeSet changes) {
      ArtifactReadable insertionActivityArt = (ArtifactReadable) insertionActivityArtId;
      if (insertionActivityArt.getRelatedCount(AtsRelationTypes.InsertionToInsertionActivity_Insertion) == 0) {
         ArtifactReadable insertionArt = atsServer.getArtifactByUuid(jaxInsertionActivity.getInsertionUuid());
         changes.relate(insertionArt, AtsRelationTypes.InsertionToInsertionActivity_InsertionActivity,
            insertionActivityArt);
      }
   }

}
