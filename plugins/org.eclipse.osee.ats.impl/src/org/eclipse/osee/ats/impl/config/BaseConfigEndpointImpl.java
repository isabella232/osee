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
package org.eclipse.osee.ats.impl.config;

import java.net.URI;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.config.BaseConfigEndpointApi;
import org.eclipse.osee.ats.api.config.JaxAtsObject;
import org.eclipse.osee.ats.api.data.AtsArtifactToken;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.core.users.AtsCoreUsers;
import org.eclipse.osee.ats.impl.IAtsServer;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IArtifactToken;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Donald G. Dunne
 */
public abstract class BaseConfigEndpointImpl<T extends JaxAtsObject> implements BaseConfigEndpointApi<T> {

   protected final IAtsServer atsServer;
   protected final IArtifactType artifactType;
   protected final IArtifactToken typeFolder;

   public BaseConfigEndpointImpl(IArtifactType artifactType, IArtifactToken typeFolder, IAtsServer atsServer) {
      this.artifactType = artifactType;
      this.typeFolder = typeFolder;
      this.atsServer = atsServer;
   }

   @Override
   @GET
   public List<T> get() throws Exception {
      return getObjects();
   }

   @Override
   @GET
   @Path("{uuid}")
   public T get(@PathParam("uuid") long uuid) throws Exception {
      return getObject(uuid);
   }

   @Override
   @POST
   public Response create(T jaxAtsObject) throws Exception {
      if (jaxAtsObject.getUuid() <= 0L) {
         throw new OseeStateException("Invalid uuid %d");
      } else if (!Strings.isValid(jaxAtsObject.getName())) {
         throw new OseeStateException("Invalid name [%d]");
      }
      ArtifactReadable artifact = atsServer.getArtifactByUuid(jaxAtsObject.getUuid());
      if (artifact != null) {
         throw new OseeStateException("Artifact with uuid %d already exists", jaxAtsObject.getUuid());
      }
      IAtsChangeSet changes =
         atsServer.getStoreService().createAtsChangeSet("Create " + artifactType.getName(), AtsCoreUsers.SYSTEM_USER);
      ArtifactId newArtifact =
         changes.createArtifact(artifactType, jaxAtsObject.getName(), GUID.create(), jaxAtsObject.getUuid());
      IAtsObject newAtsObject = atsServer.getConfigItemFactory().getConfigObject(newArtifact);
      if (typeFolder != null) {
         ArtifactReadable typeFolderArtifact = atsServer.getArtifact(typeFolder);
         if (typeFolderArtifact == null) {
            typeFolderArtifact = (ArtifactReadable) changes.createArtifact(AtsArtifactToken.CountryFolder);
         }
         if (typeFolderArtifact.getParent() == null) {
            ArtifactReadable headingFolder = atsServer.getArtifact(AtsArtifactToken.HeadingFolder);
            changes.relate(headingFolder, CoreRelationTypes.Default_Hierarchical__Child, typeFolderArtifact);
         }
         changes.relate(typeFolderArtifact, CoreRelationTypes.Default_Hierarchical__Child, newArtifact);
      }
      if (Strings.isValid(jaxAtsObject.getDescription())) {
         changes.setSoleAttributeValue(newAtsObject, AtsAttributeTypes.Description, jaxAtsObject.getDescription());
      } else {
         changes.deleteAttributes(newAtsObject, AtsAttributeTypes.Description);
      }
      changes.setSoleAttributeValue(newAtsObject, AtsAttributeTypes.Active, jaxAtsObject.isActive());
      create(jaxAtsObject, newArtifact, changes);
      changes.execute();
      return Response.created(new URI("/" + jaxAtsObject.getUuid())).build();
   }

   /**
    * Implement by subclass to perform other checks and sets during artifact creation
    */
   protected void create(T jaxAtsObject, ArtifactId newArtifact, IAtsChangeSet changes) {
      // provided for subclass implementation
   }

   @Override
   @DELETE
   public Response delete(@PathParam("uuid") long uuid) throws Exception {
      ArtifactReadable artifact = atsServer.getArtifactByUuid(uuid);
      if (artifact == null) {
         throw new OseeStateException("Artifact with uuid %d not found", uuid);
      }
      IAtsChangeSet changes =
         atsServer.getStoreService().createAtsChangeSet("Create " + artifactType.getName(), AtsCoreUsers.SYSTEM_USER);
      changes.deleteArtifact(artifact);
      changes.execute();
      return Response.ok().build();
   }

   public abstract T getConfigObject(ArtifactId artifact);

   protected T getObject(long uuid) {
      ArtifactReadable configArt = atsServer.getQuery().andUuid(uuid).getResults().getExactlyOne();
      return getConfigObject(configArt);
   }

   public abstract List<T> getObjects();

}
