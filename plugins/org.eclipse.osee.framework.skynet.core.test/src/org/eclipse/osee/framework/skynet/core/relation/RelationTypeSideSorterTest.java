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

package org.eclipse.osee.framework.skynet.core.relation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IRelationSorterId;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.enums.RelationTypeMultiplicity;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.RelationTypeSide;
import org.eclipse.osee.framework.core.model.cache.RelationTypeCache;
import org.eclipse.osee.framework.core.model.event.DefaultBasicGuidRelationReorder;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.skynet.core.mocks.DataFactory;
import org.eclipse.osee.framework.skynet.core.mocks.MockIArtifact;
import org.eclipse.osee.framework.skynet.core.mocks.MockOseeDataAccessor;
import org.eclipse.osee.framework.skynet.core.relation.order.IRelationOrderAccessor;
import org.eclipse.osee.framework.skynet.core.relation.order.IRelationSorter;
import org.eclipse.osee.framework.skynet.core.relation.order.RelationOrderData;
import org.eclipse.osee.framework.skynet.core.relation.order.RelationSorterProvider;
import org.eclipse.osee.framework.skynet.core.types.IArtifact;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Roberto E. Escobar
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class RelationTypeSideSorterTest {
   private final static Random randomGenerator = new Random();

   private final RelationType relationType;
   private final RelationSide relationSide;
   private final RelationOrderData orderData;
   private final RelationSorterProvider sorterProvider;
   private final RelationTypeSideSorter sorter;

   public RelationTypeSideSorterTest(RelationSorterProvider sorterProvider, RelationType relationType, RelationSide relationSide, RelationOrderData orderData, List<Object[]> expected) {
      this.relationType = relationType;
      this.relationSide = relationSide;
      this.orderData = orderData;
      this.sorterProvider = sorterProvider;
      this.sorter = new RelationTypeSideSorter(relationType, relationSide, sorterProvider, orderData);
   }

   @Test
   public void test01Construction() {
      Assert.assertNotNull(sorter);
   }

   @Test
   public void test02GetIArtifact() throws OseeCoreException {
      Assert.assertNotNull(orderData.getIArtifact());
      Assert.assertEquals(orderData.getIArtifact(), sorter.getIArtifact());

      Assert.assertTrue(sorter.getIArtifact() instanceof MockIArtifact);
      MockIArtifact mockArtifact = (MockIArtifact) sorter.getIArtifact();
      mockArtifact.clear();

      // Test Get Full Artifact is Called from IArtifact
      sorter.getArtifact();
      Assert.assertTrue(mockArtifact.wasGetFullArtifactCalled());
   }

   @Test
   public void test03GetRelationType() {
      Assert.assertEquals(relationType, sorter.getRelationType());
   }

   @Test
   public void test04GetRelationSide() {
      Assert.assertEquals(relationSide, sorter.getSide());
   }

   @Test
   public void test05GetSorterId() throws OseeCoreException {
      String sorterGuid = orderData.getCurrentSorterGuid(relationType, relationSide);
      IRelationSorterId expected = sorterProvider.getRelationOrder(sorterGuid).getSorterId();
      Assert.assertNotNull(sorterGuid);
      Assert.assertEquals(expected, sorter.getSorterId());
      Assert.assertEquals(expected.getGuid(), sorter.getSorterId().getGuid());
      Assert.assertEquals(expected.getName(), sorter.getSorterId().getName());
   }

   @Test
   public void test06SorterName() throws OseeCoreException {
      String sorterGuid = orderData.getCurrentSorterGuid(relationType, relationSide);
      IRelationSorterId expected = sorterProvider.getRelationOrder(sorterGuid).getSorterId();
      Assert.assertNotNull(sorterGuid);
      Assert.assertEquals(expected.getName(), sorter.getSorterName());
   }

   @Test
   public void test07SetOrder() throws OseeCoreException {
      IArtifact art3 = createArtifact("c", GUID.create());
      IArtifact art4 = createArtifact("d", GUID.create());

      List<IArtifact> relatives = Arrays.asList(art3, art4);
      List<String> expected = Artifacts.toGuids(relatives);

      // set same sorter id
      sorter.setOrder(relatives, sorter.getSorterId());
      List<String> actual = orderData.getOrderList(sorter.getRelationType(), sorter.getSide());
      Assert.assertFalse(actual.equals(expected));

      // Set Different sorter id
      sorter.setOrder(relatives, RelationOrderBaseTypes.USER_DEFINED);
      actual = orderData.getOrderList(sorter.getRelationType(), sorter.getSide());
      expected = Artifacts.toGuids(relatives);
      Assert.assertTrue(actual.equals(expected));
   }

   @Test
   public void test08Sort() {
      //      sorter.sort(listToOrder)
      //      RelationSorter sorter = null;
      //      List<IArtifact> sorted = sorter.getSortedRelatives(relatives);

   }

   @Test
   public void test09Equals() {
      //      RelationSorter a = new RelationSorter(RelationTypeManager.getType(6), RelationSide.SIDE_A);
      //      RelationSorter b = new RelationSorter(RelationTypeManager.getType(7), RelationSide.SIDE_B);
      //      assertFalse(a.equals(b));
      //      assertTrue(a.equals(a));
   }

   @Test
   public void test10AddItem() throws OseeCoreException {
      IArtifact itemToAdd = createArtifact("Item to Add", GUID.create());

      List<IArtifact> startingArtifacts = new ArrayList<IArtifact>();
      List<String> startingOrder = orderData.getOrderList(sorter.getRelationType(), sorter.getSide());
      for (int index = 0; index < startingOrder.size(); index++) {
         String artifactGuid = startingOrder.get(index);
         startingArtifacts.add(createArtifact("Dummy" + index, artifactGuid));
      }
      // Set Related Artifact Data
      MockArtifactWithRelations artifactMock = (MockArtifactWithRelations) sorter.getIArtifact();
      artifactMock.setRelatedArtifacts(sorter.getRelationType(), startingArtifacts);

      for (IRelationSorterId sorterId : sorterProvider.getAllRelationOrderIds()) {
         IRelationSorter relationSorter = sorterProvider.getRelationOrder(sorterId.getGuid());

         List<IArtifact> itemsToOrder = new ArrayList<IArtifact>(startingArtifacts);
         itemsToOrder.add(itemToAdd);
         if (RelationOrderBaseTypes.USER_DEFINED != sorterId) {
            relationSorter.sort(itemsToOrder, null);
         }

         // Call twice to ensure that the same items is not duplicated in the list
         sorter.addItem(sorterId, itemToAdd);
         sorter.addItem(sorterId, itemToAdd);

         List<String> currentOrder = orderData.getOrderList(sorter.getRelationType(), sorter.getSide());
         if (RelationOrderBaseTypes.USER_DEFINED != sorterId) {
            Assert.assertTrue(currentOrder.isEmpty());
         } else {
            List<String> expectedOrder = Artifacts.toGuids(itemsToOrder);
            Assert.assertTrue(expectedOrder.equals(currentOrder));
         }
      }
   }

   @Test
   public void test11ToString() throws OseeCoreException {
      String artGuid = sorter.getIArtifact().getGuid();
      String sorterGuid = orderData.getCurrentSorterGuid(relationType, relationSide);
      IRelationSorterId expectedId = sorterProvider.getRelationOrder(sorterGuid).getSorterId();
      String expectedToString =
         String.format("Relation Sorter {relationType=%s, relationSide=[%s], artifact=[%s], sorterId=%s}",
            relationType, relationSide, artGuid, expectedId);
      Assert.assertEquals(expectedToString, sorter.toString());
   }

   @Parameters
   public static Collection<Object[]> data() throws OseeCoreException {
      RelationSorterProvider provider = new RelationSorterProvider();
      IRelationOrderAccessor accessor = new DoNothingAccessor();

      RelationTypeCache cache = new RelationTypeCache(new MockOseeDataAccessor<Long, RelationType>());

      RelationType relationType1 =
         createRelationType(cache, "Rel 1", RelationOrderBaseTypes.LEXICOGRAPHICAL_ASC.getGuid());
      RelationType relationType2 =
         createRelationType(cache, "Rel 2", RelationOrderBaseTypes.LEXICOGRAPHICAL_DESC.getGuid());
      IArtifact art1 = createArtifact("a", GUID.create());
      IArtifact art2 = createArtifact("b", GUID.create());

      RelationOrderData data1 = new RelationOrderData(accessor, art1);
      RelationOrderData data2 = new RelationOrderData(accessor, art2);

      List<Object[]> expected1 = new ArrayList<Object[]>();
      List<Object[]> expected2 = new ArrayList<Object[]>();

      addData(cache, data1, expected1);
      addData(cache, data2, expected2);

      Collection<Object[]> data = new ArrayList<Object[]>();
      data.add(new Object[] {provider, relationType1, RelationSide.SIDE_A, data1, expected1});
      data.add(new Object[] {provider, relationType2, RelationSide.SIDE_B, data2, expected2});
      return data;
   }

   private static IArtifact createArtifact(String name, String guid) {
      int uniqueId = randomGenerator.nextInt();
      Branch branch = new Branch(GUID.create(), name + " - branch", BranchType.WORKING, BranchState.MODIFIED, false);
      return new MockArtifactWithRelations(uniqueId, name, guid, branch, CoreArtifactTypes.Artifact);
   }

   private static RelationType createRelationType(RelationTypeCache cache, String name, String delationRelationOrderGuid) throws OseeCoreException {
      IArtifactType type1 = new ArtifactType(0x01L, "1", false);
      IArtifactType type2 = new ArtifactType(0x02L, "2", false);
      RelationType relationType =
         new RelationType(0x03L, name, name + "_A", name + "_B", type1, type2, RelationTypeMultiplicity.MANY_TO_MANY,
            delationRelationOrderGuid);
      Assert.assertNotNull(relationType);
      cache.cache(relationType);
      return relationType;
   }

   private static void addData(RelationTypeCache cache, RelationOrderData data, List<Object[]> expected) throws OseeCoreException {
      addData(data, expected, cache.getUniqueByName("Rel 1"), RelationSide.SIDE_A, //
         RelationOrderBaseTypes.LEXICOGRAPHICAL_ASC.getGuid(), "1", "2", "3");
      addData(data, expected, cache.getUniqueByName("Rel 2"), RelationSide.SIDE_B, //
         RelationOrderBaseTypes.UNORDERED.getGuid(), "4", "5", "6");

      checkData(data, expected);
   }

   private static void checkData(RelationOrderData orderData, List<Object[]> expectedValues) {
      int index = 0;
      Assert.assertEquals(expectedValues.size(), orderData.size());
      for (Entry<Pair<String, String>, Pair<String, List<String>>> entry : orderData.getOrderedEntrySet()) {
         Object[] actual =
            new Object[] {
               entry.getKey().getFirst(),
               entry.getKey().getSecond(),
               entry.getValue().getFirst(),
               entry.getValue().getSecond()};
         Object[] expected = expectedValues.get(index++);
         Assert.assertEquals(expected.length, actual.length);
         for (int index2 = 0; index2 < expected.length; index2++) {
            Assert.assertEquals(expected[index2], actual[index2]);
         }
      }
   }

   private static void addData(RelationOrderData orderData, List<Object[]> expectedData, RelationType relationType, RelationSide side, String relationOrderIdGuid, String... guids) {
      List<String> artGuids = new ArrayList<String>();
      if (guids != null && guids.length > 0) {
         artGuids.addAll(Arrays.asList(guids));
      }
      orderData.addOrderList(relationType.getName(), side.name(), relationOrderIdGuid, artGuids);
      expectedData.add(new Object[] {relationType.getName(), side.name(), relationOrderIdGuid, artGuids});
   }

   private static final class MockArtifactWithRelations extends MockIArtifact {
      private final Map<IRelationType, List<? extends IArtifact>> relatedItemsMap;

      public MockArtifactWithRelations(int uniqueId, String name, String guid, Branch branch, IArtifactType artifactType) {
         super(uniqueId, name, guid, branch, DataFactory.fromToken(artifactType));
         this.relatedItemsMap = new HashMap<IRelationType, List<? extends IArtifact>>();
      }

      @Override
      public List<? extends IArtifact> getRelatedArtifacts(RelationTypeSide relationTypeSide) {
         List<? extends IArtifact> related = relatedItemsMap.get(relationTypeSide.getRelationType());
         if (related == null) {
            related = Collections.emptyList();
         }
         return related;
      }

      public void setRelatedArtifacts(IRelationType relationType, List<? extends IArtifact> relatedItems) {
         relatedItemsMap.put(relationType, relatedItems);
      }
   }

   private static final class DoNothingAccessor implements IRelationOrderAccessor {

      @Override
      public void load(IArtifact artifact, RelationOrderData orderData) {
         // do nothing
      }

      @Override
      public void store(IArtifact artifact, RelationOrderData orderData, DefaultBasicGuidRelationReorder reorderRecord) {
         // do nothing
      }
   }
}
