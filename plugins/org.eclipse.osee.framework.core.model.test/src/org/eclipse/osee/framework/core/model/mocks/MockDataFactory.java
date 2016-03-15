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
package org.eclipse.osee.framework.core.model.mocks;

import java.util.Date;
import java.util.Random;
import org.eclipse.osee.framework.core.data.IAccessContextId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes;
import org.eclipse.osee.framework.core.enums.RelationTypeMultiplicity;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.DefaultBasicArtifact;
import org.eclipse.osee.framework.core.model.IBasicArtifact;
import org.eclipse.osee.framework.core.model.OseeEnumEntry;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.model.access.AccessDetail;
import org.eclipse.osee.framework.core.model.access.Scope;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.cache.TransactionCache;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.framework.core.model.type.OseeEnumType;
import org.eclipse.osee.framework.core.model.type.OseeEnumTypeFactory;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.junit.Assert;

/**
 * @author Roberto E. Escobar
 */
public final class MockDataFactory {

   private static final Random random = new Random();

   private MockDataFactory() {
      // Utility Class
   }

   public static IAccessContextId createAccessContextId(String guid, String name) {
      IAccessContextId cxt = TokenFactory.createAccessContextId(guid, name);
      Assert.assertEquals(guid, cxt.getGuid());
      Assert.assertEquals(name, cxt.getName());
      return cxt;
   }

   public static <T> AccessDetail<T> createAccessDetails(T expAccessObject, PermissionEnum expPermission, String expReason, Scope scope) {
      AccessDetail<T> target;
      if (expReason != null) {
         target = new AccessDetail<>(expAccessObject, expPermission, scope, expReason);
      } else {
         target = new AccessDetail<>(expAccessObject, expPermission, scope);
      }
      return target;
   }

   public static IBasicArtifact<?> createArtifact(int index) {
      return new DefaultBasicArtifact(index * 37, GUID.create(), "user_" + index);
   }

   public static AttributeType createAttributeType() throws OseeCoreException {
      OseeEnumTypeFactory oseeEnumTypeFactory = new OseeEnumTypeFactory();
      AttributeType attributeType = new AttributeType(random.nextLong(), "name", "baseType", "providerName", ".xml", "",
         1, 1, "description", "tagger", "mediaType");
      attributeType.setOseeEnumType(oseeEnumTypeFactory.createEnumType(0x01L, "enum type name"));
      return attributeType;
   }

   public static Branch createBranch(int index) {
      return createBranch(Lib.generateUuid(), index);
   }

   public static Branch createBranch(long uuid, int index) {
      BranchState branchState = BranchState.values()[Math.abs(index % BranchState.values().length)];
      BranchType branchType = BranchType.values()[Math.abs(index % BranchType.values().length)];
      boolean isArchived = index % 2 == 0 ? true : false;
      return new Branch(uuid, "branch_" + index, branchType, branchState, isArchived, false);
   }

   public static TransactionRecord createTransaction(int index, int branchUuid) {
      TransactionDetailsType type =
         TransactionDetailsType.values()[Math.abs(index % TransactionDetailsType.values().length)];
      int value = index;
      if (value == 0) {
         value++;
      }
      MockOseeDataAccessor<Branch> accessor = new MockOseeDataAccessor<>();
      BranchCache cache = new BranchCache(accessor, new TransactionCache());
      return new TransactionRecord(value * 47, branchUuid, "comment_" + value, new Date(), value * 37, value * 42, type,
         cache);
   }

   public static OseeEnumEntry createEnumEntry(int index) {
      return new OseeEnumEntry("entry_" + index, Math.abs(index * 37), "description");
   }

   public static OseeEnumType createEnumType(Long id, int index) {
      return new OseeEnumType(id, "enum_" + index);
   }

   public static AttributeType createAttributeType(int index, OseeEnumType oseeEnumType) throws OseeCoreException {
      return createAttributeType(index, oseeEnumType, random.nextLong());
   }

   public static AttributeType createAttributeType(int index, OseeEnumType oseeEnumType, Long id) throws OseeCoreException {
      AttributeType type =
         new AttributeType(id, "attrType_" + index, "baseClass_" + index, "providerId_" + index, "ext_" + index,
            "default_" + index, index * 2, index * 7, "description_" + index, "tag_" + index, "mediaType_" + index);
      type.setOseeEnumType(oseeEnumType);
      return type;
   }

   public static ArtifactType createArtifactType(int index) {
      return createArtifactType(index, random.nextLong());
   }

   public static ArtifactType createArtifactType(int index, Long id) {
      return new ArtifactType(id, "art_" + index, index % 2 == 0);
   }

   public static ArtifactType createBaseArtifactType() {
      return new ArtifactType(CoreArtifactTypes.Artifact.getGuid(), CoreArtifactTypes.Artifact.getName(), true);
   }

   public static RelationType createRelationType(int index, IArtifactType artTypeA, IArtifactType artTypeB) {
      return createRelationType(index, artTypeA, artTypeB, random.nextLong());
   }

   public static RelationType createRelationType(int index, IArtifactType artTypeA, IArtifactType artTypeB, Long id) {
      RelationTypeMultiplicity multiplicity =
         RelationTypeMultiplicity.values()[Math.abs(index % RelationTypeMultiplicity.values().length)];
      String order = RelationOrderBaseTypes.values()[index % RelationTypeMultiplicity.values().length].getGuid();
      return new RelationType(id, "relType_" + index, "sideA_" + index, "sideB_" + index, artTypeA, artTypeB,
         multiplicity, order);
   }

}
