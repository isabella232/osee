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
package org.eclipse.osee.framework.core.model.access;

import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.model.IBasicArtifact;
import org.eclipse.osee.framework.core.model.mocks.MockArtifact;
import org.eclipse.osee.framework.core.model.mocks.MockDataFactory;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test Case for {@link AccessDataQuery}
 * 
 * @author Jeff C. Phillips
 */
public class AccessDataQueryTest {

   @Test
   public void testBranch() throws OseeCoreException {
      IOseeBranch branchToCheck = CoreBranches.SYSTEM_ROOT;

      TestObject testObject = getTestData();
      AccessData data = testObject.getAccessData();

      AccessDataQuery query = new AccessDataQuery(data);
      PermissionStatus status = new PermissionStatus();

      query.branchMatches(PermissionEnum.WRITE, branchToCheck, status);
      Assert.assertTrue(status.matched());

      query.branchMatches(PermissionEnum.FULLACCESS, branchToCheck, status);
      Assert.assertFalse(status.matched());
   }

   @Test
   public void testArtifactType() throws OseeCoreException {
      TestObject testObject = getTestData();
      AccessData data = testObject.getAccessData();
      IBasicArtifact<?> artifactToCheck = testObject.getArtifact();

      AccessDataQuery query = new AccessDataQuery(data);
      PermissionStatus status = new PermissionStatus();

      query.artifactTypeMatches(PermissionEnum.WRITE, artifactToCheck, status);
      Assert.assertTrue(status.matched());

      query.artifactTypeMatches(PermissionEnum.FULLACCESS, artifactToCheck, status);
      Assert.assertFalse(status.matched());
   }

   @Test
   public void testArtifact() throws OseeCoreException {
      TestObject testObject = getTestData();
      AccessData data = testObject.getAccessData();
      IBasicArtifact<?> artifactToCheck = testObject.getArtifact();

      AccessDataQuery query = new AccessDataQuery(data);
      PermissionStatus status = new PermissionStatus();

      query.artifactMatches(PermissionEnum.WRITE, artifactToCheck, status);
      Assert.assertTrue(status.matched());
   }

   @Test
   public void testAttributeType() throws OseeCoreException {
      TestObject testObject = getTestData();
      AccessData data = testObject.getAccessData();
      IBasicArtifact<?> artifactToCheck = testObject.getArtifact();

      IAttributeType attributeType = CoreAttributeTypes.ParagraphNumber;
      IAttributeType wordAttributeType = CoreAttributeTypes.WordTemplateContent;

      data.add(artifactToCheck, new AccessDetail<IAttributeType>(attributeType, PermissionEnum.WRITE, new Scope()));
      data.add(artifactToCheck, new AccessDetail<IAttributeType>(wordAttributeType, PermissionEnum.READ, new Scope()));

      AccessDataQuery query = new AccessDataQuery(data);
      PermissionStatus status = new PermissionStatus();

      query.artifactMatches(PermissionEnum.WRITE, artifactToCheck, status);
      Assert.assertTrue(status.matched());

      query.attributeTypeMatches(PermissionEnum.READ, artifactToCheck, wordAttributeType, status);
      Assert.assertTrue(status.matched());

      query.attributeTypeMatches(PermissionEnum.WRITE, artifactToCheck, wordAttributeType, status);
      Assert.assertFalse(status.matched());

      query.artifactTypeMatches(PermissionEnum.WRITE, artifactToCheck, status);
      Assert.assertTrue(status.matched());

      query.artifactTypeMatches(PermissionEnum.FULLACCESS, artifactToCheck, status);
      Assert.assertFalse(status.matched());
   }

   @Test
   public void testArtifactMatchesAll() throws OseeCoreException {
      AccessData data = new AccessData();
      IOseeBranch branch = CoreBranches.COMMON;
      IArtifactType artifactType = CoreArtifactTypes.AbstractSoftwareRequirement;
      IBasicArtifact<?> artifact1 = new MockArtifact("1", "one", branch, artifactType, 1);
      IBasicArtifact<?> artifact2 = new MockArtifact("2", "two", branch, artifactType, 2);

      data.add(artifact1, new AccessDetail<IBasicArtifact<?>>(artifact1, PermissionEnum.READ, new Scope()));
      data.add(artifact2, new AccessDetail<IBasicArtifact<?>>(artifact2, PermissionEnum.WRITE, new Scope()));

      AccessDataQuery query = new AccessDataQuery(data);

      Assert.assertTrue(query.matchesAll(PermissionEnum.READ));
      Assert.assertFalse(query.matchesAll(PermissionEnum.WRITE));
   }

   @Test
   public void testBranchMatchesAll() throws OseeCoreException {
      AccessData data = new AccessData();
      IOseeBranch common = CoreBranches.COMMON;
      IOseeBranch branch = CoreBranches.SYSTEM_ROOT;

      data.add(common, new AccessDetail<IOseeBranch>(common, PermissionEnum.READ, new Scope()));
      data.add(branch, new AccessDetail<IOseeBranch>(branch, PermissionEnum.WRITE, new Scope()));

      AccessDataQuery query = new AccessDataQuery(data);

      Assert.assertTrue(query.matchesAll(PermissionEnum.READ));
      Assert.assertFalse(query.matchesAll(PermissionEnum.WRITE));
   }

   @Test
   public void testArtifactMatches() throws OseeCoreException {
      IOseeBranch branch = CoreBranches.COMMON;
      IBasicArtifact<?> accessArtifact = new MockArtifact(GUID.create(), "test1", branch, CoreArtifactTypes.Folder, 45);
      IBasicArtifact<?> typeAccessArtifact =
         new MockArtifact(GUID.create(), "test2", branch, CoreArtifactTypes.Folder, 46);
      IBasicArtifact<?> noAccessArtifact =
         new MockArtifact(GUID.create(), "test3", branch, CoreArtifactTypes.Folder, 47);
      IArtifactType artType = TokenFactory.createArtifactType(0x01L, "Folder");

      AccessDetail<?> specificArtDetail =
         MockDataFactory.createAccessDetails(accessArtifact, PermissionEnum.DENY, "",
            new Scope().add("very").add("specific"));
      AccessDetail<?> legacyArtDetail =
         MockDataFactory.createAccessDetails(accessArtifact, PermissionEnum.FULLACCESS, "", Scope.createLegacyScope());
      AccessDetail<?> typeDetail =
         MockDataFactory.createAccessDetails(artType, PermissionEnum.WRITE, "", new Scope().add("very"));

      AccessData data = new AccessData();
      data.add(accessArtifact, specificArtDetail);
      data.add(accessArtifact, legacyArtDetail);
      data.add(accessArtifact, typeDetail);
      data.add(typeAccessArtifact, typeDetail);

      AccessDataQuery query = new AccessDataQuery(data);
      PermissionStatus ps = new PermissionStatus();
      query.artifactMatches(PermissionEnum.WRITE, accessArtifact, ps);
      Assert.assertFalse(ps.matched());

      ps = new PermissionStatus();
      query.artifactMatches(PermissionEnum.WRITE, typeAccessArtifact, ps);
      Assert.assertTrue(ps.matched());

      ps = new PermissionStatus();
      query.artifactMatches(PermissionEnum.WRITE, noAccessArtifact, ps);
      Assert.assertTrue(ps.matched());
   }

   private TestObject getTestData() throws OseeCoreException {
      IOseeBranch branchToCheck = CoreBranches.SYSTEM_ROOT;
      IArtifactType artifactType = CoreArtifactTypes.AbstractSoftwareRequirement;
      IBasicArtifact<?> artifactToCheck = new MockArtifact(GUID.create(), "Hello", branchToCheck, artifactType, 12);
      AccessData data = new AccessData();

      data.add(branchToCheck, new AccessDetail<IOseeBranch>(branchToCheck, PermissionEnum.WRITE, new Scope()));
      data.add(artifactToCheck, new AccessDetail<IBasicArtifact<?>>(artifactToCheck, PermissionEnum.WRITE, new Scope()));
      data.add(artifactToCheck, new AccessDetail<IArtifactType>(artifactType, PermissionEnum.WRITE, new Scope()));

      return new TestObject(artifactToCheck, data);
   }

   private class TestObject {
      final IBasicArtifact<?> artifact;
      final AccessData accessData;

      public TestObject(IBasicArtifact<?> artifact, AccessData accessData) {
         super();
         this.artifact = artifact;
         this.accessData = accessData;
      }

      public IBasicArtifact<?> getArtifact() {
         return artifact;
      }

      public AccessData getAccessData() {
         return accessData;
      }
   }
}
