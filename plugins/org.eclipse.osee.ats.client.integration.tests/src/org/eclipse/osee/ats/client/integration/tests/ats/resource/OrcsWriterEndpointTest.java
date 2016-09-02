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
package org.eclipse.osee.ats.client.integration.tests.ats.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.ats.demo.api.DemoUsers;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.orcs.rest.model.OrcsWriterEndpoint;
import org.eclipse.osee.orcs.rest.model.writer.reader.OwArtifact;
import org.eclipse.osee.orcs.rest.model.writer.reader.OwArtifactToken;
import org.eclipse.osee.orcs.rest.model.writer.reader.OwCollector;
import org.junit.Before;
import org.junit.Test;

/**
 * Test unit for {@link OrcsWriterEndpoint}
 *
 * @author Donald G. Dunne
 */
public class OrcsWriterEndpointTest extends AbstractRestTest {
   private OrcsWriterEndpoint writer;

   @Before
   public void setup() {
      writer = AtsClientService.getOrcsWriter();
   }

   @Test
   public void testGetOrcsWriterInputDefaultJson() throws Exception {
      OwCollector collector = getDefaultOwCollector();
      assertEquals(3, collector.getCreate().size());
   }

   private OwCollector getDefaultOwCollector() throws Exception {
      Response response = writer.getOrcsWriterInputDefaultJson();
      assertEquals(javax.ws.rs.core.Response.Status.OK.getStatusCode(), response.getStatus());
      OwCollector collector = response.readEntity(OwCollector.class);
      return collector;
   }

   @Test
   public void testGetOrcsWriterInputDefault() throws Exception {
      Response response = writer.getOrcsWriterInputDefault();
      assertEquals(javax.ws.rs.core.Response.Status.OK.getStatusCode(), response.getStatus());
      String excelXml = response.readEntity(String.class);
      assertTrue(excelXml.contains("Orcs Writer Import Folder"));
   }

   @Test
   public void testValidate() throws Exception {
      OwCollector collector = getDefaultOwCollector();
      collector.setAsUserId(DemoUsers.Joe_Smith.getUserId());
      collector.setPersistComment(getClass().getSimpleName() + " - testValidate");
      Response response = writer.getOrcsWriterValidate(collector);
      assertEquals(javax.ws.rs.core.Response.Status.OK.getStatusCode(), response.getStatus());
   }

   @Test
   public void testPersist() throws Exception {
      OwCollector collector = getDefaultOwCollector();
      collector.setAsUserId(DemoUsers.Joe_Smith.getUserId());
      collector.setPersistComment(getClass().getSimpleName() + " - testPersist");
      Response response = writer.getOrcsWriterPersist(collector);
      assertEquals(javax.ws.rs.core.Response.Status.OK.getStatusCode(), response.getStatus());

      for (OwArtifact art : collector.getCreate()) {
         long artTypeUuid = art.getType().getUuid();
         ArtifactType typeByGuid = ArtifactTypeManager.getTypeByGuid(artTypeUuid);
         assertNotNull(typeByGuid);
         if (typeByGuid.equals(CoreArtifactTypes.Folder)) {
            long artUuid = art.getUuid();
            Artifact folderArt = AtsClientService.get().getArtifact(artUuid);
            assertNotNull(folderArt);
            assertEquals(2, folderArt.getChildren().size());
            for (Artifact child : folderArt.getChildren()) {
               assertTrue(
                  child.getName().equals("Software Requirement 1") || child.getName().equals("Software Requirement 2"));
            }
         }
      }

      OwArtifact userGroupOwArt = collector.getUpdate().iterator().next();
      Artifact userGroupArt = AtsClientService.get().getArtifact(userGroupOwArt.getUuid());
      assertNotNull(userGroupArt);
      assertEquals("test static id", userGroupArt.getSoleAttributeValue(CoreAttributeTypes.StaticId, null));
      assertEquals("test annotation", userGroupArt.getSoleAttributeValue(CoreAttributeTypes.Annotation, null));

   }

   @Test
   public void testDelete() throws Exception {
      Artifact artifact = ArtifactTypeManager.addArtifact(CoreArtifactTypes.GeneralData, CoreBranches.COMMON,
         getClass().getSimpleName());
      artifact.persist(getClass().getSimpleName());

      Artifact artifactFromId1 = ArtifactQuery.getArtifactFromId(artifact.getUuid(), CoreBranches.COMMON);
      assertNotNull(artifactFromId1);

      OwCollector collector = getDefaultOwCollector();
      collector.getCreate().clear();
      collector.getUpdate().clear();
      OwArtifactToken owToken = new OwArtifactToken();
      owToken.setUuid(artifact.getUuid());
      owToken.setName(artifact.getName());
      String tokenStr = String.format("[%s]-[%d]", artifact.getName(), artifact.getUuid());
      owToken.setData(tokenStr);
      collector.getDelete().add(owToken);

      collector.setAsUserId(DemoUsers.Joe_Smith.getUserId());
      collector.setPersistComment(getClass().getSimpleName() + " - testValidate");

      Response response = writer.getOrcsWriterPersist(collector);
      assertEquals(javax.ws.rs.core.Response.Status.OK.getStatusCode(), response.getStatus());

      ArtifactCache.deCache(artifactFromId1);

      List<Artifact> artifacts = ArtifactQuery.getArtifactListFromIds(
         Collections.singleton(artifact.getUuid().intValue()), CoreBranches.COMMON);
      assertTrue(artifacts.iterator().next().isDeleted());
   }
}
