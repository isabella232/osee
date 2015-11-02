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
import org.eclipse.osee.ats.api.insertion.IAtsInsertion;
import org.eclipse.osee.ats.api.insertion.InsertionActivityEndpointApi;
import org.eclipse.osee.ats.api.insertion.InsertionEndpointApi;
import org.eclipse.osee.ats.api.insertion.JaxInsertion;
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
public class InsertionEndpointImpl extends BaseConfigEndpointImpl<JaxInsertion> implements InsertionEndpointApi {

   private final long programUuid;

   public InsertionEndpointImpl(IAtsServer atsServer) {
      this(atsServer, 0L);
   }

   public InsertionEndpointImpl(IAtsServer atsServer, long programUuid) {
      super(AtsArtifactTypes.Insertion, null, atsServer);
      this.programUuid = programUuid;
   }

   @PUT
   @Override
   public Response update(JaxInsertion insertion) throws Exception {
      ArtifactReadable artifact = atsServer.getArtifact(insertion.getUuid());
      if (artifact == null) {
         throw new OseeStateException("Artifact with uuid %d not found", insertion.getUuid());
      }
      IAtsChangeSet changes =
         atsServer.getStoreService().createAtsChangeSet("Create " + artifactType.getName(), AtsCoreUsers.SYSTEM_USER);
      ArtifactReadable configArtifact =
         (ArtifactReadable) changes.createArtifact(artifactType, insertion.getName(), GUID.create(),
            insertion.getUuid());
      IAtsConfigObject configObject = atsServer.getConfigItemFactory().getConfigObject(configArtifact);
      if (!configArtifact.getName().equals(insertion.getName())) {
         changes.setSoleAttributeValue(configObject, CoreAttributeTypes.Name, insertion.getName());
      }
      changes.execute();
      return Response.created(new URI("/" + insertion.getUuid())).build();
   }

   @Override
   public JaxInsertion getConfigObject(ArtifactId artifact) {
      JaxInsertion jaxInsertion = new JaxInsertion();
      IAtsInsertion insertion = atsServer.getConfigItemFactory().getInsertion(artifact);
      jaxInsertion.setName(insertion.getName());
      jaxInsertion.setUuid(insertion.getUuid());
      jaxInsertion.setActive(insertion.isActive());
      jaxInsertion.setDescription(insertion.getDescription());
      return jaxInsertion;
   }

   @Override
   public List<JaxInsertion> getObjects() {
      List<JaxInsertion> insertions = new ArrayList<>();
      if (programUuid == 0L) {
         for (ArtifactReadable art : atsServer.getQuery().andIsOfType(artifactType).getResults()) {
            insertions.add(getConfigObject(art));
         }
      } else {
         for (ArtifactReadable insertionArt : atsServer.getArtifact(programUuid).getRelated(
            AtsRelationTypes.ProgramToInsertion_Insertion)) {
            JaxInsertion insertion = getConfigObject(insertionArt);
            insertion.setProgramUuid(programUuid);
            insertions.add(insertion);
         }
      }
      return insertions;
   }

   @Override
   protected void create(JaxInsertion jaxInsertion, ArtifactId insertionArtId, IAtsChangeSet changes) {
      ArtifactReadable insertionArt = (ArtifactReadable) insertionArtId;
      if (insertionArt.getRelatedCount(AtsRelationTypes.ProgramToInsertion_Program) == 0) {
         ArtifactReadable programArt = atsServer.getArtifact(jaxInsertion.getProgramUuid());
         changes.relate(programArt, AtsRelationTypes.ProgramToInsertion_Insertion, insertionArt);
      }
   }

   @Override
   public InsertionActivityEndpointApi getInsertionActivity(long insertionUuid) {
      return new InsertionActivityEndpointImpl(atsServer, insertionUuid);
   }

}
