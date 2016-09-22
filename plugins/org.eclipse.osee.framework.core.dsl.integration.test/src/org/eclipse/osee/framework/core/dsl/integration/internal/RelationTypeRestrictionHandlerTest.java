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
package org.eclipse.osee.framework.core.dsl.integration.internal;

import static org.eclipse.osee.framework.core.enums.RelationSorter.LEXICOGRAPHICAL_ASC;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.RelationTypeSide;
import org.eclipse.osee.framework.core.data.RelationTypeToken;
import org.eclipse.osee.framework.core.dsl.integration.mocks.DslAsserts;
import org.eclipse.osee.framework.core.dsl.integration.mocks.MockArtifactProxy;
import org.eclipse.osee.framework.core.dsl.integration.mocks.MockModel;
import org.eclipse.osee.framework.core.dsl.oseeDsl.AccessPermissionEnum;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDslFactory;
import org.eclipse.osee.framework.core.dsl.oseeDsl.RelationTypeArtifactPredicate;
import org.eclipse.osee.framework.core.dsl.oseeDsl.RelationTypeArtifactTypePredicate;
import org.eclipse.osee.framework.core.dsl.oseeDsl.RelationTypeRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XArtifactMatcher;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XArtifactType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XRelationSideEnum;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XRelationType;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.enums.RelationTypeMultiplicity;
import org.eclipse.osee.framework.core.model.access.AccessDetail;
import org.eclipse.osee.framework.core.model.access.AccessDetailCollector;
import org.eclipse.osee.framework.core.model.access.Scope;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test Case for {@link RelationTypeRestrictionHandler}
 *
 * @author Roberto E. Escobar
 */
public class RelationTypeRestrictionHandlerTest extends BaseRestrictionHandlerTest<RelationTypeRestriction> {
   private static final RelationTypeToken relationType = CoreRelationTypes.Default_Hierarchical__Child;

   public RelationTypeRestrictionHandlerTest() {
      super(new RelationTypeRestrictionHandler(new ArtifactMatchInterpreter()),
         MockModel.createRelationTypeRestriction(), MockModel.createAttributeTypeRestriction());
   }

   @Test
   public void testProcessDataRelationTypeNoMatch() throws OseeCoreException {
      XRelationType relationTypeRef = MockModel.createXRelationType(relationType);

      RelationTypeRestriction restriction = MockModel.createRelationTypeRestriction();
      restriction.setPermission(AccessPermissionEnum.ALLOW);
      restriction.setRelationTypeRef(relationTypeRef);

      // Artifact Data has no relation types therefore relation type will not match
      MockArtifactProxy artData = new MockArtifactProxy(GUID.create(), null);
      Scope expectedScope = new Scope().add("fail");
      DslAsserts.assertNullAccessDetail(getRestrictionHandler(), restriction, artData, expectedScope);
   }

   @Test
   public void testProcessDataRelationTypeMatchSideANoMatch() throws OseeCoreException {
      XRelationType relationTypeRef = MockModel.createXRelationType(relationType);

      RelationTypeRestriction restriction = MockModel.createRelationTypeRestriction();
      restriction.setPermission(AccessPermissionEnum.ALLOW);
      restriction.setRelationTypeRef(relationTypeRef);
      restriction.setRestrictedToSide(XRelationSideEnum.SIDE_A);

      RelationType testRelationType =
         getTestRelationType(relationType, CoreArtifactTypes.Artifact, CoreArtifactTypes.SoftwareRequirement);

      IArtifactType artTypeToken1 = CoreArtifactTypes.SoftwareRequirement;
      ArtifactType artArtType = new ArtifactType(artTypeToken1.getGuid(), artTypeToken1.getName(), false);

      MockArtifactProxy artData =
         new MockArtifactProxy(GUID.create(), artArtType, null, null, Collections.singleton(testRelationType));
      RelationTypeSide expectedObject = new RelationTypeSide(testRelationType, RelationSide.SIDE_A);
      Scope expectedScope = new Scope();
      DslAsserts.assertAccessDetail(getRestrictionHandler(), restriction, artData, expectedObject, PermissionEnum.WRITE,
         expectedScope);
   }

   @Test
   public void testProcessDataRelationTypeMatchSideAMatch() throws OseeCoreException {
      XRelationType relationTypeRef = MockModel.createXRelationType(relationType);

      RelationTypeRestriction restriction = MockModel.createRelationTypeRestriction();
      restriction.setPermission(AccessPermissionEnum.ALLOW);
      restriction.setRelationTypeRef(relationTypeRef);
      restriction.setRestrictedToSide(XRelationSideEnum.SIDE_A);

      RelationType testRelationType =
         getTestRelationType(relationType, CoreArtifactTypes.Artifact, CoreArtifactTypes.SoftwareRequirement);

      IArtifactType artTypeToken1 = CoreArtifactTypes.SoftwareRequirement;
      ArtifactType artArtType = new ArtifactType(artTypeToken1.getGuid(), artTypeToken1.getName(), false);
      Set<ArtifactType> superTypes = new HashSet<>();
      superTypes.add(
         new ArtifactType(CoreArtifactTypes.Artifact.getGuid(), CoreArtifactTypes.Artifact.getName(), false));
      artArtType.setSuperTypes(superTypes);

      MockArtifactProxy artData =
         new MockArtifactProxy(GUID.create(), artArtType, null, null, Collections.singleton(testRelationType));
      RelationTypeSide expectedObject = new RelationTypeSide(testRelationType, RelationSide.SIDE_A);
      Scope expectedScope = new Scope();
      DslAsserts.assertAccessDetail(getRestrictionHandler(), restriction, artData, expectedObject, PermissionEnum.WRITE,
         expectedScope);
   }

   @Test
   public void testProcessDataRelationTypeMatchSideBNoMatch() throws OseeCoreException {
      XRelationType relationTypeRef = MockModel.createXRelationType(relationType);

      RelationTypeRestriction restriction = MockModel.createRelationTypeRestriction();
      restriction.setPermission(AccessPermissionEnum.ALLOW);
      restriction.setRelationTypeRef(relationTypeRef);
      restriction.setRestrictedToSide(XRelationSideEnum.SIDE_B);

      RelationType testRelationType =
         getTestRelationType(relationType, CoreArtifactTypes.SoftwareRequirement, CoreArtifactTypes.Artifact);

      IArtifactType artTypeToken1 = CoreArtifactTypes.SoftwareRequirement;
      ArtifactType artArtType = new ArtifactType(artTypeToken1.getGuid(), artTypeToken1.getName(), false);

      MockArtifactProxy artData =
         new MockArtifactProxy(GUID.create(), artArtType, null, null, Collections.singleton(testRelationType));

      RelationTypeSide expectedObject = new RelationTypeSide(testRelationType, RelationSide.SIDE_B);
      Scope expectedScope = new Scope();
      DslAsserts.assertAccessDetail(getRestrictionHandler(), restriction, artData, expectedObject, PermissionEnum.WRITE,
         expectedScope);
   }

   @Test
   public void testProcessDataRelationTypeMatchSideBMatch() throws OseeCoreException {
      XRelationType relationTypeRef = MockModel.createXRelationType(relationType);

      RelationTypeRestriction restriction = MockModel.createRelationTypeRestriction();
      restriction.setPermission(AccessPermissionEnum.ALLOW);
      restriction.setRelationTypeRef(relationTypeRef);
      restriction.setRestrictedToSide(XRelationSideEnum.SIDE_B);

      RelationType testRelationType =
         getTestRelationType(relationType, CoreArtifactTypes.SoftwareRequirement, CoreArtifactTypes.Artifact);

      IArtifactType artTypeToken1 = CoreArtifactTypes.SoftwareRequirement;
      ArtifactType artArtType = new ArtifactType(artTypeToken1.getGuid(), artTypeToken1.getName(), false);
      Set<ArtifactType> superTypes = new HashSet<>();
      superTypes.add(
         new ArtifactType(CoreArtifactTypes.Artifact.getGuid(), CoreArtifactTypes.Artifact.getName(), false));
      artArtType.setSuperTypes(superTypes);

      MockArtifactProxy artData =
         new MockArtifactProxy(GUID.create(), artArtType, null, null, Collections.singleton(testRelationType));
      RelationTypeSide expectedObject = new RelationTypeSide(testRelationType, RelationSide.SIDE_B);
      Scope expectedScope = new Scope();
      DslAsserts.assertAccessDetail(getRestrictionHandler(), restriction, artData, expectedObject, PermissionEnum.WRITE,
         expectedScope);
   }

   @Test
   public void testProcessDataRelationTypeMatchBothMatch() throws OseeCoreException {
      XRelationType relationTypeRef = MockModel.createXRelationType(relationType);

      RelationTypeRestriction restriction = MockModel.createRelationTypeRestriction();
      restriction.setPermission(AccessPermissionEnum.ALLOW);
      restriction.setRelationTypeRef(relationTypeRef);
      restriction.setRestrictedToSide(XRelationSideEnum.BOTH);

      RelationType testRelationType =
         getTestRelationType(relationType, CoreArtifactTypes.SoftwareRequirement, CoreArtifactTypes.Artifact);

      IArtifactType artTypeToken1 = CoreArtifactTypes.SoftwareRequirement;
      ArtifactType artArtType = new ArtifactType(artTypeToken1.getGuid(), artTypeToken1.getName(), false);
      Set<ArtifactType> superTypes = new HashSet<>();
      superTypes.add(
         new ArtifactType(CoreArtifactTypes.Artifact.getGuid(), CoreArtifactTypes.Artifact.getName(), false));
      artArtType.setSuperTypes(superTypes);

      MockArtifactProxy artData =
         new MockArtifactProxy(GUID.create(), artArtType, null, null, Collections.singleton(testRelationType));
      RelationTypeSide expectedObject1 = new RelationTypeSide(testRelationType, RelationSide.SIDE_A);
      RelationTypeSide expectedObject2 = new RelationTypeSide(testRelationType, RelationSide.SIDE_B);

      final List<AccessDetail<?>> actualAccesses = new ArrayList<>();
      AccessDetailCollector collector = new AccessDetailCollector() {

         @Override
         public void collect(AccessDetail<?> accessDetail) {
            Assert.assertNotNull(accessDetail);
            actualAccesses.add(accessDetail);
         }
      };

      Scope expectedScope = new Scope();
      getRestrictionHandler().process(restriction, artData, collector, expectedScope);

      AccessDetail<?> actualAccess = actualAccesses.get(0);
      Assert.assertEquals(actualAccess.getPermission(), PermissionEnum.WRITE);
      Assert.assertEquals(expectedObject1, actualAccess.getAccessObject());

      actualAccess = actualAccesses.get(1);
      Assert.assertEquals(actualAccess.getPermission(), PermissionEnum.WRITE);
      Assert.assertEquals(expectedObject2, actualAccess.getAccessObject());
   }

   @Test
   public void testProcessRelationWithRelationTypeAll() throws OseeCoreException {
      RelationTypeRestriction restriction = MockModel.createRelationTypeRestriction();
      restriction.setPermission(AccessPermissionEnum.ALLOW);
      restriction.setRelationTypeMatch(true);
      restriction.setRestrictedToSide(XRelationSideEnum.BOTH);

      RelationType testRelationType =
         getTestRelationType(relationType, CoreArtifactTypes.SoftwareRequirement, CoreArtifactTypes.Artifact);

      IArtifactType artTypeToken1 = CoreArtifactTypes.SoftwareRequirement;
      ArtifactType artArtType = new ArtifactType(artTypeToken1.getGuid(), artTypeToken1.getName(), false);
      Set<ArtifactType> superTypes = new HashSet<>();
      superTypes.add(
         new ArtifactType(CoreArtifactTypes.Artifact.getGuid(), CoreArtifactTypes.Artifact.getName(), false));
      artArtType.setSuperTypes(superTypes);

      MockArtifactProxy artData =
         new MockArtifactProxy(GUID.create(), artArtType, null, null, Collections.singleton(testRelationType));
      RelationTypeSide expectedObject1 = new RelationTypeSide(testRelationType, RelationSide.SIDE_A);
      RelationTypeSide expectedObject2 = new RelationTypeSide(testRelationType, RelationSide.SIDE_B);

      final List<AccessDetail<?>> actualAccesses = new ArrayList<>();
      AccessDetailCollector collector = new AccessDetailCollector() {

         @Override
         public void collect(AccessDetail<?> accessDetail) {
            Assert.assertNotNull(accessDetail);
            actualAccesses.add(accessDetail);
         }
      };

      Scope expectedScope = new Scope();
      getRestrictionHandler().process(restriction, artData, collector, expectedScope);

      AccessDetail<?> actualAccess = actualAccesses.get(0);
      Assert.assertEquals(actualAccess.getPermission(), PermissionEnum.WRITE);
      Assert.assertEquals(expectedObject1, actualAccess.getAccessObject());

      actualAccess = actualAccesses.get(1);
      Assert.assertEquals(actualAccess.getPermission(), PermissionEnum.WRITE);
      Assert.assertEquals(expectedObject2, actualAccess.getAccessObject());
   }

   @Test
   public void testProcessDataArtifactTypeMatch() throws OseeCoreException {

      IArtifactType artifactType = CoreArtifactTypes.AbstractSoftwareRequirement;
      XArtifactType artifactTypeRef = MockModel.createXArtifactType(artifactType.getGuid(), artifactType.getName());

      RelationTypeRestriction restriction = MockModel.createRelationTypeRestriction();
      restriction.setPermission(AccessPermissionEnum.ALLOW);
      restriction.setRelationTypeMatch(true);

      RelationTypeArtifactTypePredicate predicate = OseeDslFactory.eINSTANCE.createRelationTypeArtifactTypePredicate();
      predicate.setArtifactTypeRef(artifactTypeRef);

      restriction.setPredicate(predicate);
      restriction.setRestrictedToSide(XRelationSideEnum.SIDE_B);

      RelationType testRelationType =
         getTestRelationType(relationType, CoreArtifactTypes.SoftwareRequirement, CoreArtifactTypes.Artifact);

      IArtifactType artTypeToken1 = CoreArtifactTypes.SoftwareRequirement;
      ArtifactType artArtType = new ArtifactType(artTypeToken1.getGuid(), artTypeToken1.getName(), false);
      Set<ArtifactType> superTypes = new HashSet<>();
      superTypes.add(new ArtifactType(CoreArtifactTypes.AbstractSoftwareRequirement.getGuid(),
         CoreArtifactTypes.AbstractSoftwareRequirement.getName(), false));
      artArtType.setSuperTypes(superTypes);

      ArtifactToken expectedAccessObject = ArtifactToken.valueOf(1, "Another Artifact", BranchId.SENTINEL);
      MockArtifactProxy artData = new MockArtifactProxy(expectedAccessObject.getGuid(), artArtType,
         expectedAccessObject, null, Collections.singleton(testRelationType));

      RelationTypeSide expectedObject = new RelationTypeSide(testRelationType, RelationSide.SIDE_B);

      Scope expectedScope = new Scope();
      expectedScope.addSubPath(artData.getName());
      DslAsserts.assertAccessDetail(getRestrictionHandler(), restriction, artData, expectedObject, PermissionEnum.WRITE,
         expectedScope);
   }

   private void testProcessRelationWithArtifactHelper(String artifactName, String matcherArtifactName, Scope expectedScope) throws OseeCoreException {
      XRelationType relationTypeRef = MockModel.createXRelationType(relationType);

      RelationTypeRestriction restriction = MockModel.createRelationTypeRestriction();
      restriction.setPermission(AccessPermissionEnum.ALLOW);
      restriction.setRelationTypeRef(relationTypeRef);
      restriction.setRestrictedToSide(XRelationSideEnum.SIDE_B);

      XArtifactMatcher matcher =
         MockModel.createMatcher("artifactMatcher \"Test\" where artifactName EQ \"" + matcherArtifactName + "\";");

      RelationTypeArtifactPredicate predicate = OseeDslFactory.eINSTANCE.createRelationTypeArtifactPredicate();
      predicate.setArtifactMatcherRef(matcher);
      restriction.setPredicate(predicate);

      RelationType testRelationType =
         getTestRelationType(relationType, CoreArtifactTypes.SoftwareRequirement, CoreArtifactTypes.Artifact);

      IArtifactType artTypeToken1 = CoreArtifactTypes.SoftwareRequirement;
      ArtifactType artArtType = new ArtifactType(artTypeToken1.getGuid(), artTypeToken1.getName(), false);
      Set<ArtifactType> superTypes = new HashSet<>();
      superTypes.add(
         new ArtifactType(CoreArtifactTypes.Artifact.getGuid(), CoreArtifactTypes.Artifact.getName(), false));
      artArtType.setSuperTypes(superTypes);

      ArtifactToken dummy = ArtifactToken.valueOf(43, artifactName, BranchId.SENTINEL);
      MockArtifactProxy artData =
         new MockArtifactProxy(GUID.create(), artArtType, dummy, null, Collections.singleton(testRelationType));
      RelationTypeSide expectedObject = new RelationTypeSide(testRelationType, RelationSide.SIDE_B);
      DslAsserts.assertAccessDetail(getRestrictionHandler(), restriction, artData, expectedObject, PermissionEnum.WRITE,
         expectedScope);
   }

   @Test
   public void testProcessRelationWithArtifactMatch() throws OseeCoreException {
      testProcessRelationWithArtifactHelper("artifactToMatch", "artifactToMatch",
         new Scope().addSubPath("artifactToMatch"));
   }

   @Test
   public void testProcessRelationWithArtifactNoMatch() throws OseeCoreException {
      testProcessRelationWithArtifactHelper("artifactToMatch", "differentArtifactToMatch", new Scope());
   }

   private static RelationType getTestRelationType(RelationTypeToken relationType, IArtifactType aArtTypeToken, IArtifactType bArtTypeToken) {
      return new RelationType(relationType.getId(), relationType.getName(), "sideA_" + aArtTypeToken.getName(),
         "sideB_" + bArtTypeToken.getName(), aArtTypeToken, bArtTypeToken, RelationTypeMultiplicity.MANY_TO_MANY,
         LEXICOGRAPHICAL_ASC);
   }
}
