/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.framework.core.model.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.framework.core.data.ArtifactTypeToken;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.exception.OseeInvalidInheritanceException;
import org.eclipse.osee.framework.core.model.internal.fields.ArtifactSuperTypeField;
import org.eclipse.osee.framework.core.model.mocks.MockDataFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test Case For {@link ArtifactSuperTypeField}
 *
 * @author Roberto E. Escobar
 */
public class ArtifactSuperTypeFieldTest {

   private static ArtifactTypeToken art1;
   private static ArtifactTypeToken art2;
   private static ArtifactTypeToken art3;
   private static ArtifactTypeToken base;
   private static ArtifactTypeToken containingArt;

   @BeforeClass
   public static void prepareTest() {
      containingArt = MockDataFactory.createArtifactType(999);
      art1 = MockDataFactory.createArtifactType(1);
      art2 = MockDataFactory.createArtifactType(2);
      art3 = MockDataFactory.createArtifactType(3);
      base = CoreArtifactTypes.Artifact;
   }

   @Test
   public void testSetGet() {
      List<ArtifactTypeToken> input = new ArrayList<>();
      ArtifactSuperTypeField field = new ArtifactSuperTypeField(containingArt, input);
      Assert.assertEquals(false, field.isDirty());

      FieldTestUtil.assertSetGet(field, Arrays.asList(art1, art2, art3), Arrays.asList(art1, art2, art3), true);
      field.clearDirty();
      Assert.assertEquals(false, field.isDirty());

      // Add again in a different order
      FieldTestUtil.assertSetGet(field, Arrays.asList(art3, art1, art2), Arrays.asList(art1, art2, art3), false);

      // Remove
      FieldTestUtil.assertSetGet(field, Arrays.asList(art3), Arrays.asList(art3), true);
      field.clearDirty();
      Assert.assertEquals(false, field.isDirty());

      // Add
      FieldTestUtil.assertSetGet(field, Arrays.asList(art3, art2), Arrays.asList(art3, art2), true);
      field.clearDirty();
      Assert.assertEquals(false, field.isDirty());
   }

   @Test(expected = OseeInvalidInheritanceException.class)
   public void testBaseCircularity() {
      List<ArtifactTypeToken> input = new ArrayList<>();
      ArtifactSuperTypeField field = new ArtifactSuperTypeField(containingArt, input);
      Assert.assertEquals(false, field.isDirty());

      field.set(Collections.singletonList(containingArt));
   }

   @Test(expected = OseeInvalidInheritanceException.class)
   public void testBaseArtifact() {
      List<ArtifactTypeToken> input = new ArrayList<>();
      ArtifactSuperTypeField field = new ArtifactSuperTypeField(containingArt, input);
      Assert.assertEquals(false, field.isDirty());

      field.set(Collections.<ArtifactTypeToken> emptyList());
   }

   @Test
   public void testBaseArtifactNoSuperTypeRequired() {
      List<ArtifactTypeToken> input = new ArrayList<>();
      ArtifactSuperTypeField field = new ArtifactSuperTypeField(base, input);
      Assert.assertEquals(false, field.isDirty());

      field.set(Collections.<ArtifactTypeToken> emptyList());
   }
}