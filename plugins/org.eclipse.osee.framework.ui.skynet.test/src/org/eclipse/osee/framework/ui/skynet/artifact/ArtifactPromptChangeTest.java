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

package org.eclipse.osee.framework.ui.skynet.artifact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.RelationTypeSide;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.model.IBasicArtifact;
import org.eclipse.osee.framework.core.model.access.PermissionStatus;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.AccessPolicy;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.artifact.prompt.IHandlePromptChange;
import org.eclipse.osee.framework.ui.skynet.artifact.prompt.IPromptFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test Case for {@link ArtifactPromptChange}
 *
 * @author Jeff C. Phillips
 */
public class ArtifactPromptChangeTest {
   private static IAttributeType TEST_ATTRIBUTE_TYPE = CoreAttributeTypes.Name;

   @Test
   public void test() throws OseeCoreException {
      boolean persist = true;
      List<Artifact> artifacts = new ArrayList<>();

      MockPromptFactory MockPromptFactory = new MockPromptFactory();
      AccessPolicy policyHandler = new MockAccessPolicyHandler();
      MockPromptFactory.createPrompt(CoreAttributeTypes.Annotation, "", artifacts, persist, false);

      ArtifactPrompt artifactPromptChange = new ArtifactPrompt(MockPromptFactory, policyHandler);

      Assert.assertFalse(
         artifactPromptChange.promptChangeAttribute(CoreAttributeTypes.Annotation, artifacts, persist, false));
      Assert.assertTrue(artifactPromptChange.promptChangeAttribute(TEST_ATTRIBUTE_TYPE, artifacts, persist, false));
   }

   private static class MockAccessPolicyHandler implements AccessPolicy {

      @Override
      public PermissionStatus hasArtifactTypePermission(BranchId branch, Collection<? extends IArtifactType> artifactTypes, PermissionEnum permission, Level level) {
         return new PermissionStatus();
      }

      @Override
      public boolean isReadOnly(Artifact artifact) {
         return false;
      }

      @Override
      public PermissionStatus hasBranchPermission(BranchId branch, PermissionEnum permission, Level level) {
         return new PermissionStatus();
      }

      @Override
      public PermissionStatus hasAttributeTypePermission(Collection<? extends IBasicArtifact<?>> artifacts, IAttributeType attributeType, PermissionEnum permission, Level level) {
         return new PermissionStatus();
      }

      @Override
      public PermissionStatus hasArtifactPermission(Collection<Artifact> artifacts, PermissionEnum permission, Level level) {
         return new PermissionStatus();
      }

      @Override
      public PermissionStatus canRelationBeModified(Artifact subject, Collection<Artifact> toBeRelated, RelationTypeSide relationTypeSide, Level level) {
         return new PermissionStatus();
      }

      @Override
      public void removePermissions(BranchId branch) {
         //
      }

   }

   private static class MockPromptFactory implements IPromptFactory {
      @Override
      public IHandlePromptChange createPrompt(IAttributeType attributeType, String displayName, Collection<? extends Artifact> artifacts, boolean persist, boolean multiLine) throws OseeCoreException {
         return new TestPromptChange(attributeType, persist);
      }
   }
   private static class TestPromptChange implements IHandlePromptChange {
      private final IAttributeType attributeType;
      private final boolean persist;

      public TestPromptChange(IAttributeType attributeType, boolean persist) {
         super();
         this.attributeType = attributeType;
         this.persist = persist;
      }

      @Override
      public boolean promptOk() throws OseeCoreException {
         return true;
      }

      @Override
      public boolean store() throws OseeCoreException {
         return persist && attributeType.equals(TEST_ATTRIBUTE_TYPE);
      }
   }
}
