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
package org.eclipse.osee.client.integration.tests.integration.skynet.core;

import static org.eclipse.osee.client.demo.DemoChoice.OSEE_CLIENT_DEMO;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.osee.client.demo.DemoBranches;
import org.eclipse.osee.client.test.framework.OseeClientIntegrationRule;
import org.eclipse.osee.client.test.framework.OseeLogMonitorRule;
import org.eclipse.osee.client.test.framework.TestInfo;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.change.ArtifactChange;
import org.eclipse.osee.framework.skynet.core.change.ArtifactDelta;
import org.eclipse.osee.framework.skynet.core.change.AttributeChange;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.skynet.core.revision.ChangeData;
import org.eclipse.osee.framework.skynet.core.revision.ChangeData.KindType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Marc A. Potter
 */
public class ChangeDataTest {

   @Rule
   public OseeClientIntegrationRule integration = new OseeClientIntegrationRule(OSEE_CLIENT_DEMO);

   @Rule
   public OseeLogMonitorRule monitorRule = new OseeLogMonitorRule();

   @Rule
   public TestInfo method = new TestInfo();

   private IOseeBranch workingBranch;
   private ChangeData theData;

   @Before
   public void setUp() throws OseeCoreException {
      workingBranch = BranchManager.createWorkingBranch(DemoBranches.SAW_Bld_1, method.getQualifiedTestName());
      ArrayList<Change> theChanges = new ArrayList<>();

      Artifact artifactStart = ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, workingBranch);
      Artifact artifactEnd = artifactStart.duplicate(workingBranch);
      artifactStart.setName("test artifact 1");
      artifactEnd.setName("test artifact 1");
      artifactEnd.addAttribute(CoreAttributeTypes.ParagraphNumber);
      artifactEnd.setSoleAttributeFromString(CoreAttributeTypes.ParagraphNumber, "1.2");
      ArtifactDelta artDelta = new ArtifactDelta(null, artifactEnd, artifactStart);

      ModificationType modType = ModificationType.MODIFIED;
      Change change =
         new ArtifactChange(workingBranch, artifactStart.getGammaId(), artifactStart.getArtId(), null, modType, false,
            artifactStart, artDelta);
      theChanges.add(change);

      AttributeType paragraphAttributeType = AttributeTypeManager.getType(CoreAttributeTypes.ParagraphNumber);
      int typeID = ((Long) paragraphAttributeType.getId()).intValue();
      change =
         new AttributeChange(workingBranch, artifactStart.getGammaId(), artifactStart.getArtId(), null,
            ModificationType.NEW, "1.2", "", typeID, paragraphAttributeType, modType, false, artifactStart, artDelta);
      theChanges.add(change);

      artifactStart = ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, workingBranch);
      artifactEnd = artifactStart.duplicate(workingBranch);
      artifactStart.setName("test artifact 2");
      artifactEnd.setName("test artifact 2A");
      artifactEnd.addAttribute(CoreAttributeTypes.ParagraphNumber);
      artifactEnd.setSoleAttributeFromString(CoreAttributeTypes.ParagraphNumber, "2.3");
      artDelta = new ArtifactDelta(null, artifactEnd, artifactStart);
      modType = ModificationType.MODIFIED;

      change =
         new ArtifactChange(workingBranch, artifactStart.getGammaId(), artifactStart.getArtId(), null, modType, false,
            artifactStart, artDelta);
      theChanges.add(change);
      typeID = ((Long) paragraphAttributeType.getId()).intValue();
      change =
         new AttributeChange(workingBranch, artifactStart.getGammaId(), artifactStart.getArtId(), null,
            ModificationType.NEW, "1.2", "", typeID, paragraphAttributeType, modType, false, artifactStart, artDelta);
      theChanges.add(change);

      AttributeType nameAttributeType = AttributeTypeManager.getType(CoreAttributeTypes.Name);
      typeID = ((Long) nameAttributeType.getId()).intValue();
      change =
         new AttributeChange(workingBranch, artifactStart.getGammaId(), artifactStart.getArtId(), null, modType,
            "test artifact 2A", "test artifact 2", typeID, nameAttributeType, modType, false, artifactStart, artDelta);
      theChanges.add(change);

      artifactStart = ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, workingBranch);
      artifactEnd.setSoleAttributeFromString(CoreAttributeTypes.ParagraphNumber, "1.2");
      artDelta = new ArtifactDelta(null, null, artifactStart);
      change =
         new ArtifactChange(null, 1, artifactStart.getArtId(), null, ModificationType.NEW, false, artifactStart,
            artDelta);
      theChanges.add(change);
      theData = new ChangeData(theChanges);
   }

   @After
   public void tearDown() throws Exception {
      BranchManager.purgeBranch(workingBranch);
   }

   @Test
   public void testGetAll() throws Exception {
      Collection<Artifact> theChanges =
         theData.getArtifacts(KindType.Artifact, ModificationType.MODIFIED, ModificationType.NEW);
      assertEquals("Wrong number of artifacts detected -- get all", 3, theChanges.size());

      theChanges = theData.getArtifacts(KindType.Artifact, ModificationType.MODIFIED);
      assertEquals("Wrong number of artifacts detected -- get all modified", 2, theChanges.size());

      theChanges = theData.getArtifacts(KindType.Artifact, ModificationType.NEW);
      assertEquals("Wrong number of artifacts detected -- get all new", 1, theChanges.size());
   }

   @Test
   public void testGetSubset() throws Exception {
      ArrayList<IAttributeType> typesToIgnore = new ArrayList<>();
      typesToIgnore.add(CoreAttributeTypes.CrewInterfaceRequirement);

      Collection<Artifact> theChanges =
         theData.getArtifacts(KindType.Artifact, typesToIgnore, ModificationType.MODIFIED);
      assertEquals("Wrong number of artifacts detected modified case 1 ", 2, theChanges.size());

      typesToIgnore.add(CoreAttributeTypes.ParagraphNumber);
      theChanges = theData.getArtifacts(KindType.Artifact, typesToIgnore, ModificationType.MODIFIED);
      assertEquals("Wrong number of artifacts detected modified case 2 ", 1, theChanges.size());

      typesToIgnore.add(CoreAttributeTypes.Name);
      theChanges = theData.getArtifacts(KindType.Artifact, typesToIgnore, ModificationType.MODIFIED);
      assertEquals("Wrong number of artifacts detected modified case 2 ", 0, theChanges.size());
   }

}
