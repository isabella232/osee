/*********************************************************************
 * Copyright (c) 2015 Boeing
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

package org.eclipse.osee.ats.ide.integration.tests.ats.util;

import static org.eclipse.osee.framework.core.enums.DeletionFlag.EXCLUDE_DELETED;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.ide.integration.tests.AtsApiService;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class AtsChangeSetTest {

   Artifact folderArt = null;
   List<Artifact> genDocArts = new ArrayList<>();

   @Before
   public void setup() {

      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsApiService.get().getAtsBranch(), getClass().getSimpleName());

      folderArt = ArtifactTypeManager.addArtifact(CoreArtifactTypes.Folder, AtsApiService.get().getAtsBranch(),
         "AtsChangeSetTest");
      folderArt.setSoleAttributeValue(CoreAttributeTypes.StaticId, "my static id");
      folderArt.persist(transaction);

      for (int x = 0; x < 4; x++) {
         Artifact genDocArt = ArtifactTypeManager.addArtifact(CoreArtifactTypes.GeneralDocument,
            AtsApiService.get().getAtsBranch(), "Art " + x + " AtsChangeSetTest");
         genDocArt.persist(transaction);
         genDocArts.add(genDocArt);
      }

      transaction.execute();
   }

   @After
   public void cleanup() {
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsApiService.get().getAtsBranch(), getClass().getSimpleName());
      for (Artifact artifact : ArtifactQuery.getArtifactListFromName(getClass().getSimpleName(),
         AtsApiService.get().getAtsBranch(), EXCLUDE_DELETED, QueryOption.CONTAINS_MATCH_OPTIONS)) {
         artifact.deleteAndPersist(transaction);
      }
      transaction.execute();
   }

   private Artifact getGenDocArt(int x) {
      return AtsApiService.get().getQueryServiceIde().getArtifact(genDocArts.toArray()[x]);
   }

   @Test
   public void testSetRelationAndSetRelations() {
      Artifact genDocArt0 = getGenDocArt(0);
      Artifact genDocArt1 = getGenDocArt(1);
      Artifact genDocArt2 = getGenDocArt(2);
      Artifact genDocArt3 = getGenDocArt(3);

      // setRelations
      IAtsChangeSet changes = createAtsChangeSet();
      changes.setRelations(folderArt, CoreRelationTypes.DefaultHierarchical_Child,
         Arrays.asList(genDocArt0, genDocArt1, genDocArt2));
      changes.execute();

      Assert.assertEquals(3, folderArt.getChildren().size());
      Assert.assertTrue(folderArt.getChildren().contains(genDocArt0));
      Assert.assertTrue(folderArt.getChildren().contains(genDocArt1));
      Assert.assertTrue(folderArt.getChildren().contains(genDocArt2));

      // setRelations - remove one and add one
      changes = createAtsChangeSet();
      changes.setRelations(folderArt, CoreRelationTypes.DefaultHierarchical_Child,
         Arrays.asList(genDocArt0, genDocArt3, genDocArt2));
      changes.execute();

      Assert.assertEquals(3, folderArt.getChildren().size());
      Assert.assertTrue(folderArt.getChildren().contains(genDocArt0));
      Assert.assertFalse(folderArt.getChildren().contains(genDocArt1));
      Assert.assertTrue(folderArt.getChildren().contains(genDocArt2));
      Assert.assertTrue(folderArt.getChildren().contains(genDocArt3));

      // setRelation - remove one and add one
      changes = createAtsChangeSet();
      changes.setRelation(folderArt, CoreRelationTypes.DefaultHierarchical_Child, genDocArt2);
      changes.execute();

      Assert.assertTrue(folderArt.getChildren().size() == 1);
      Assert.assertTrue(folderArt.getChildren().contains(genDocArt2));
   }

   @Test
   public void testRelateUnRelateAll() {
      Artifact genDocArt0 = getGenDocArt(0);
      Artifact genDocArt1 = getGenDocArt(1);
      Artifact genDocArt2 = getGenDocArt(2);

      // Relate
      IAtsChangeSet changes = createAtsChangeSet();
      changes.relate(folderArt, CoreRelationTypes.DefaultHierarchical_Child, genDocArt0);
      changes.execute();

      Assert.assertTrue(folderArt.getChildren().size() == 1);
      Assert.assertTrue(folderArt.getChildren().contains(genDocArt0));
      Assert.assertEquals(folderArt, genDocArt0.getParent());

      // Add 2 more children
      changes = createAtsChangeSet();
      changes.relate(folderArt, CoreRelationTypes.DefaultHierarchical_Child, genDocArt1);
      changes.relate(folderArt, CoreRelationTypes.DefaultHierarchical_Child, genDocArt2);
      changes.execute();

      Assert.assertEquals(3, folderArt.getChildren().size());
      Assert.assertTrue(folderArt.getChildren().contains(genDocArt0));
      Assert.assertTrue(folderArt.getChildren().contains(genDocArt1));
      Assert.assertTrue(folderArt.getChildren().contains(genDocArt2));
      Assert.assertEquals(folderArt, genDocArt0.getParent());
      Assert.assertEquals(folderArt, genDocArt1.getParent());
      Assert.assertEquals(folderArt, genDocArt2.getParent());

      // UnRelate All
      changes = createAtsChangeSet();
      changes.unrelateAll(folderArt, CoreRelationTypes.DefaultHierarchical_Child);
      changes.execute();

      Assert.assertTrue(folderArt.getChildren().isEmpty());
      Assert.assertNull(genDocArt0.getParent());
      Assert.assertNull(genDocArt1.getParent());
      Assert.assertNull(genDocArt2.getParent());

      // Relate using opposite side
      changes = createAtsChangeSet();
      changes.relate(genDocArt0, CoreRelationTypes.DefaultHierarchical_Parent, folderArt);
      changes.execute();

      Assert.assertTrue(folderArt.getChildren().size() == 1);
      Assert.assertTrue(folderArt.getChildren().contains(genDocArt0));
      Assert.assertEquals(folderArt, genDocArt0.getParent());
   }

   @Test
   public void testSetAttributeById_ArtifactId() {
      Attribute<?> staticIdAttr = null;
      for (Attribute<?> attr : folderArt.getAttributes()) {
         if (attr.getAttributeType().equals(CoreAttributeTypes.StaticId)) {
            staticIdAttr = attr;
            break;
         }
      }
      Assert.assertNotNull(staticIdAttr);

      IAtsChangeSet changes = createAtsChangeSet();
      changes.setAttribute(folderArt, staticIdAttr, "new id");
      changes.execute();

      folderArt.reloadAttributesAndRelations();
      Assert.assertEquals("new id", folderArt.getSoleAttributeValue(CoreAttributeTypes.StaticId, null));
   }

   @Test
   public void testSetSoleAttributeById() {
      IAtsChangeSet changes = createAtsChangeSet();
      changes.setSoleAttributeValue(folderArt, CoreAttributeTypes.StaticId, "newest id");
      changes.execute();

      folderArt.reloadAttributesAndRelations();
      Assert.assertEquals("newest id", folderArt.getSoleAttributeValue(CoreAttributeTypes.StaticId, null));
   }

   private IAtsChangeSet createAtsChangeSet() {
      return AtsApiService.get().getStoreService().createAtsChangeSet(getClass().getSimpleName(),
         AtsApiService.get().getUserService().getCurrentUser());
   }
}
