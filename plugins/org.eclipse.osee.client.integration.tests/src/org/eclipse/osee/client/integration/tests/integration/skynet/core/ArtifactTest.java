/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/

package org.eclipse.osee.client.integration.tests.integration.skynet.core;

import static org.eclipse.osee.client.demo.DemoChoice.OSEE_CLIENT_DEMO;
import org.eclipse.osee.client.demo.DemoBranches;
import org.eclipse.osee.client.demo.DemoTypes;
import org.eclipse.osee.client.test.framework.OseeClientIntegrationRule;
import org.eclipse.osee.client.test.framework.OseeLogMonitorRule;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.model.event.DefaultBasicGuidArtifact;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Jeff C. Phillips
 */
public final class ArtifactTest {

   @Rule
   public OseeClientIntegrationRule integration = new OseeClientIntegrationRule(OSEE_CLIENT_DEMO);

   @Rule
   public OseeLogMonitorRule monitorRule = new OseeLogMonitorRule();

   private Artifact artifactWithSpecialAttr;

   @Before
   public void setUp() throws Exception {
      artifactWithSpecialAttr = ArtifactTypeManager.addArtifact(DemoTypes.DemoTestRequirement, DemoBranches.SAW_Bld_1);
   }

   @After
   public void tearDown() throws Exception {
      if (artifactWithSpecialAttr != null) {
         artifactWithSpecialAttr.deleteAndPersist();
      }
      for (Artifact art : ArtifactQuery.getArtifactListFromTypeAndName(CoreArtifactTypes.GeneralData,
         ArtifactTest.class.getSimpleName(), BranchManager.getCommonBranch())) {
         art.deleteAndPersist();
      }
   }

   @Test
   public void attributeCopyAcrossRelatedBranches() throws Exception {
      artifactWithSpecialAttr.setSoleAttributeValue(CoreAttributeTypes.Partition, "Navigation");
      artifactWithSpecialAttr.setName("ArtifactTest-artifactWithSpecialAttr");

      Artifact copiedArtifact = artifactWithSpecialAttr.duplicate(DemoBranches.SAW_Bld_2);
      try {
         Assert.assertFalse(copiedArtifact.getAttributeCount(CoreAttributeTypes.Partition) == 0);
      } finally {
         if (copiedArtifact != null) {
            copiedArtifact.deleteAndPersist();
         }
      }
   }

   @Test
   public void attributeCopyAcrossUnrelatedBranches() throws Exception {
      artifactWithSpecialAttr.setSoleAttributeValue(CoreAttributeTypes.Partition, "Navigation");
      artifactWithSpecialAttr.setName("ArtifactTest-artifactWithSpecialAttr");

      Artifact copiedArtifact = artifactWithSpecialAttr.duplicate(CoreBranches.COMMON);
      try {
         Assert.assertTrue(copiedArtifact.getAttributeCount(CoreAttributeTypes.Partition) == 0);
      } finally {
         if (copiedArtifact != null) {
            copiedArtifact.deleteAndPersist();
         }
      }
   }

   @Test
   public void setSoleAttributeValueTest() throws Exception {
      artifactWithSpecialAttr.setName("ArtifactTest-artifactWithSpecialAttr");
      artifactWithSpecialAttr.setSoleAttributeValue(CoreAttributeTypes.Partition, "Navigation");
   }

   @Test
   public void testHashCode() throws OseeCoreException {
      Artifact art =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.GeneralData, BranchManager.getCommonBranch(),
            ArtifactTest.class.getSimpleName());
      art.persist("test");

      DefaultBasicGuidArtifact equalGuid =
         new DefaultBasicGuidArtifact(Lib.generateUuid(), CoreArtifactTypes.SoftwareDesign.getGuid(), art.getGuid());
      Assert.assertEquals(art.hashCode(), equalGuid.hashCode());

      DefaultBasicGuidArtifact equalGuidArtType =
         new DefaultBasicGuidArtifact(Lib.generateUuid(), CoreArtifactTypes.GeneralData.getGuid(), art.getGuid());
      Assert.assertEquals(art.hashCode(), equalGuidArtType.hashCode());

      DefaultBasicGuidArtifact equalGuidArtTypeBranchUuid =
         new DefaultBasicGuidArtifact(BranchManager.getCommonBranch().getUuid(),
            CoreArtifactTypes.GeneralData.getGuid(), art.getGuid());
      Assert.assertEquals(art.hashCode(), equalGuidArtTypeBranchUuid.hashCode());

      DefaultBasicGuidArtifact equalArtTypeBranchUuidNotGuid =
         new DefaultBasicGuidArtifact(BranchManager.getCommonBranch().getUuid(),
            CoreArtifactTypes.GeneralData.getGuid(), GUID.create());
      Assert.assertNotSame(art.hashCode(), equalArtTypeBranchUuidNotGuid.hashCode());
   }

   @Test
   public void testEquals() throws OseeCoreException {
      Artifact art =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.GeneralData, BranchManager.getCommonBranch(),
            ArtifactTest.class.getSimpleName());
      art.persist("test");

      DefaultBasicGuidArtifact equalGuid =
         new DefaultBasicGuidArtifact(Lib.generateUuid(), CoreArtifactTypes.SoftwareDesign.getGuid(), art.getGuid());
      Assert.assertNotSame(art, equalGuid);

      DefaultBasicGuidArtifact equalGuidArtType =
         new DefaultBasicGuidArtifact(Lib.generateUuid(), CoreArtifactTypes.GeneralData.getGuid(), art.getGuid());
      Assert.assertNotSame(art, equalGuidArtType);

      DefaultBasicGuidArtifact equalGuidArtTypeBranchUuid =
         new DefaultBasicGuidArtifact(BranchManager.getCommonBranch().getUuid(),
            CoreArtifactTypes.GeneralData.getGuid(), art.getGuid());
      Assert.assertEquals(art, equalGuidArtTypeBranchUuid);

      DefaultBasicGuidArtifact equalArtTypeBranchUuidNotGuid =
         new DefaultBasicGuidArtifact(BranchManager.getCommonBranch().getUuid(),
            CoreArtifactTypes.GeneralData.getGuid(), GUID.create());
      Assert.assertNotSame(art, equalArtTypeBranchUuidNotGuid);
   }

}
