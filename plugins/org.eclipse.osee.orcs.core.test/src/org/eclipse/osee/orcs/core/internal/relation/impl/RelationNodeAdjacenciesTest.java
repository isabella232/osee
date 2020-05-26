/*********************************************************************
 * Copyright (c) 2013 Boeing
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

package org.eclipse.osee.orcs.core.internal.relation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.RelationTypeToken;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.orcs.core.internal.relation.Relation;
import org.eclipse.osee.orcs.core.internal.relation.RelationVisitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Test Case for {@link RelationNodeAdjacencies}
 *
 * @author Roberto E. Escobar
 */
public class RelationNodeAdjacenciesTest {

   // @formatter:off
   @Mock Relation dirty;
   @Mock Relation clean;
   @Mock Relation deleted;
   @Mock Relation relation;
   // @formatter:on

   private final RelationNodeAdjacencies collection = new RelationNodeAdjacencies();

   @Before
   public void init() {
      MockitoAnnotations.initMocks(this);
      collection.add(CoreRelationTypes.Allocation_Requirement, dirty);
      collection.add(CoreRelationTypes.CodeRequirement_CodeUnit, clean);
      collection.add(CoreRelationTypes.DefaultHierarchical_Parent, deleted);

      when(dirty.isDirty()).thenReturn(true);
      when(dirty.getModificationType()).thenReturn(ModificationType.NEW);
      when(clean.getModificationType()).thenReturn(ModificationType.NEW);
      when(deleted.getModificationType()).thenReturn(ModificationType.ARTIFACT_DELETED);
      when(deleted.isDeleted()).thenReturn(true);
   }

   @Test
   public void testGetRelationsDirty() {
      assertEquals(1, collection.getDirties().size());
      assertEquals(dirty, collection.getDirties().iterator().next());
   }

   @Test
   public void testHasRelationsDirty() {
      boolean actual1 = collection.hasDirty();
      assertTrue(actual1);

      collection.remove(CoreRelationTypes.Allocation_Requirement, dirty);

      boolean actual2 = collection.hasDirty();
      assertFalse(actual2);
   }

   @Test
   public void testGetAllRelations() {
      Collection<Relation> relations = collection.getAll();
      assertEquals(3, relations.size());

      assertTrue(relations.contains(dirty));
      assertTrue(relations.contains(clean));
      assertTrue(relations.contains(deleted));
   }

   @Test
   public void testGetExistingTypes() {
      RelationTypeToken typeA = CoreRelationTypes.DEFAULT_HIERARCHY;
      RelationTypeToken typeB = CoreRelationTypes.Allocation_Component;
      RelationTypeToken typeC = CoreRelationTypes.Dependency_Artifact;

      when(dirty.getRelationType()).thenReturn(typeA);
      when(dirty.getModificationType()).thenReturn(ModificationType.ARTIFACT_DELETED);
      when(dirty.isDeleted()).thenReturn(true);

      when(clean.getRelationType()).thenReturn(typeB);
      when(clean.getModificationType()).thenReturn(ModificationType.ARTIFACT_DELETED);
      when(clean.isDeleted()).thenReturn(true);

      when(deleted.getRelationType()).thenReturn(typeC);
      when(deleted.getModificationType()).thenReturn(ModificationType.MODIFIED);
      when(deleted.isDeleted()).thenReturn(false);

      Collection<RelationTypeToken> types = collection.getExistingTypes(DeletionFlag.INCLUDE_DELETED);

      assertEquals(3, types.size());

      assertTrue(types.contains(typeA));
      assertTrue(types.contains(typeB));
      assertTrue(types.contains(typeC));

      Collection<RelationTypeToken> types2 = collection.getExistingTypes(DeletionFlag.EXCLUDE_DELETED);
      assertEquals(1, types2.size());

      assertFalse(types2.contains(typeA));
      assertFalse(types2.contains(typeB));
      assertTrue(types2.contains(typeC));
   }

   @Test
   public void testGetListDeletionFlag() {
      List<Relation> list1 = collection.getList(DeletionFlag.INCLUDE_DELETED);

      assertEquals(3, list1.size());
      assertTrue(list1.contains(dirty));
      assertTrue(list1.contains(clean));
      assertTrue(list1.contains(deleted));

      List<Relation> list2 = collection.getList(DeletionFlag.EXCLUDE_DELETED);
      assertEquals(2, list2.size());

      assertTrue(list2.contains(dirty));
      assertTrue(list2.contains(clean));
      assertFalse(list2.contains(deleted));
   }

   @Test
   public void testGetSetDeletionFlag() {
      ResultSet<Relation> set1 = collection.getResultSet(DeletionFlag.INCLUDE_DELETED);

      assertEquals(3, set1.size());
      checkContains(set1, dirty, true);
      checkContains(set1, clean, true);
      checkContains(set1, deleted, true);

      ResultSet<Relation> set2 = collection.getResultSet(DeletionFlag.EXCLUDE_DELETED);
      assertEquals(2, set2.size());

      checkContains(set2, dirty, true);
      checkContains(set2, clean, true);
      checkContains(set2, deleted, false);
   }

   @Test
   public void testGetListTypeAndDelete() {
      List<Relation> list1 =
         collection.getList(CoreRelationTypes.DefaultHierarchical_Parent, DeletionFlag.INCLUDE_DELETED);
      assertEquals(1, list1.size());
      assertTrue(list1.contains(deleted));

      List<Relation> list2 =
         collection.getList(CoreRelationTypes.DefaultHierarchical_Parent, DeletionFlag.EXCLUDE_DELETED);
      assertEquals(0, list2.size());
   }

   @Test
   public void testGetSetTypeAndDelete() {
      ResultSet<Relation> set1 =
         collection.getResultSet(CoreRelationTypes.DefaultHierarchical_Parent, DeletionFlag.INCLUDE_DELETED);

      assertEquals(1, set1.size());
      checkContains(set1, deleted, true);

      ResultSet<Relation> set2 =
         collection.getResultSet(CoreRelationTypes.DefaultHierarchical_Parent, DeletionFlag.EXCLUDE_DELETED);
      assertEquals(0, set2.size());
   }

   @Test
   public void testAccept() {
      RelationVisitor visitor = Mockito.mock(RelationVisitor.class);
      collection.accept(visitor);

      verify(visitor).visit(dirty);
      verify(visitor).visit(clean);
      verify(visitor).visit(deleted);
   }

   @Test
   public void testLocalIdOnSide() {
      ArtifactId id1 = ArtifactId.valueOf(1);
      ArtifactId id2 = ArtifactId.valueOf(2);
      when(relation.isDeleted()).thenReturn(false);
      when(relation.getIdForSide(RelationSide.SIDE_A)).thenReturn(id1);
      when(relation.getIdForSide(RelationSide.SIDE_B)).thenReturn(id2);

      collection.add(CoreRelationTypes.Allocation_Requirement, relation);

      Relation actual = collection.getResultSet(CoreRelationTypes.Allocation_Requirement, DeletionFlag.EXCLUDE_DELETED,
         id2, RelationSide.SIDE_A).getOneOrNull();
      assertNull(actual);
      actual = collection.getResultSet(CoreRelationTypes.Allocation_Requirement, DeletionFlag.EXCLUDE_DELETED, id2,
         RelationSide.SIDE_B).getOneOrNull();
      assertEquals(relation, actual);

      actual = collection.getResultSet(CoreRelationTypes.Allocation_Requirement, DeletionFlag.EXCLUDE_DELETED, id1,
         RelationSide.SIDE_A).getOneOrNull();
      assertEquals(relation, actual);
      actual = collection.getResultSet(CoreRelationTypes.Allocation_Requirement, DeletionFlag.EXCLUDE_DELETED, id1,
         RelationSide.SIDE_B).getOneOrNull();
      assertNull(actual);
   }

   private void checkContains(Iterable<Relation> items, Relation toFind, boolean findExpected) {
      boolean actual = Iterables.contains(items, toFind);
      assertEquals(findExpected, actual);
   }
}
