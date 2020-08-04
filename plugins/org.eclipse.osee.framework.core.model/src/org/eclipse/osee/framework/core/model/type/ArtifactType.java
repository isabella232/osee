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

package org.eclipse.osee.framework.core.model.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.osee.framework.core.data.ArtifactTypeToken;
import org.eclipse.osee.framework.core.data.AttributeTypeEnum;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.core.data.AttributeTypeToken;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.enums.EnumToken;
import org.eclipse.osee.framework.core.model.AbstractOseeType;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.IOseeField;
import org.eclipse.osee.framework.core.model.OseeField;
import org.eclipse.osee.framework.core.model.internal.fields.ArtifactSuperTypeField;
import org.eclipse.osee.framework.core.model.internal.fields.ArtifactTypeAttributesField;

/**
 * @author Robert A. Fisher
 */
public class ArtifactType extends AbstractOseeType implements ArtifactTypeToken {

   public static final String ARTIFACT_IS_ABSTRACT_FIELD_KEY = "osee.artifact.type.is.abstract.field";
   public static final String ARTIFACT_INHERITANCE_FIELD_KEY = "osee.artifact.type.inheritance.field";
   public static final String ARTIFACT_TYPE_ATTRIBUTES_FIELD_KEY = "osee.artifact.type.attributes.field";

   private final Set<ArtifactType> superTypes = new HashSet<>();
   private final Set<ArtifactType> childTypes = new HashSet<>();
   private final Map<BranchId, Collection<AttributeType>> attributes = new HashMap<>();

   public ArtifactType(Long guid, String name, boolean isAbstract) {
      super(guid, name);
      initializeFields();
      setAbstract(isAbstract);
   }

   protected void initializeFields() {
      addField(ARTIFACT_IS_ABSTRACT_FIELD_KEY, new OseeField<Boolean>());
      addField(ARTIFACT_INHERITANCE_FIELD_KEY, new ArtifactSuperTypeField(this, null));
      addField(ARTIFACT_TYPE_ATTRIBUTES_FIELD_KEY, new ArtifactTypeAttributesField(attributes));
   }

   private List<ArtifactType> getSuperArtifactTypes() {
      List<ArtifactType> defaultValue = new ArrayList<>();
      Collection<ArtifactType> types = getFieldValueLogException(defaultValue, ARTIFACT_INHERITANCE_FIELD_KEY);
      return new ArrayList<ArtifactType>(types);
   }

   public void setSuperTypes(Set<ArtifactType> superType) {
      Set<ArtifactType> originals = new HashSet<ArtifactType>(superTypes);
      setField(ARTIFACT_INHERITANCE_FIELD_KEY, superType);
      for (ArtifactType supers : superType) {
         supers.childTypes.add(this);
      }
      for (ArtifactType oldValue : originals) {
         if (!superType.contains(oldValue)) {
            oldValue.childTypes.remove(this);
         }
      }
   }

   @Override
   public List<ArtifactTypeToken> getDirectDescendantTypes() {
      return org.eclipse.osee.framework.jdk.core.util.Collections.cast(getDescendants(this, false));
   }

   @Override
   public List<ArtifactTypeToken> getAllDescendantTypes() {
      return getDescendants(this, true);
   }

   private List<ArtifactTypeToken> getDescendants(ArtifactType artifactType, boolean isRecursionAllowed) {
      List<ArtifactTypeToken> descendants = new ArrayList<>();
      populateDescendants(artifactType, descendants, isRecursionAllowed);
      return descendants;
   }

   private void populateDescendants(ArtifactType artifactType, List<ArtifactTypeToken> descendants, boolean isRecursionAllowed) {
      for (ArtifactType type : artifactType.childTypes) {
         if (isRecursionAllowed) {
            populateDescendants(type, descendants, isRecursionAllowed);
         }
         descendants.add(type);
      }
   }

   public void setAttributeTypes(Collection<AttributeType> attributeTypes, BranchId branch) {
      IOseeField<?> field = getField(ARTIFACT_TYPE_ATTRIBUTES_FIELD_KEY);
      ((ArtifactTypeAttributesField) field).put(branch, attributeTypes);
   }

   public void setAllAttributeTypes(Map<BranchId, Collection<AttributeType>> attributeTypes) {
      IOseeField<?> field = getField(ARTIFACT_TYPE_ATTRIBUTES_FIELD_KEY);
      ((ArtifactTypeAttributesField) field).set(attributeTypes);
   }

   public boolean isValidAttributeType(AttributeTypeId attributeType, Branch branch) {
      return getAttributeTypes(branch).contains(attributeType);
   }

   public Collection<AttributeTypeToken> getAttributeTypes(Branch branch) {
      // Do not use ARTIFACT_TYPE_ATTRIBUTES_FIELD for this call since it must use branch inheritance to get all attribute types
      Set<AttributeTypeToken> attributeTypes = new HashSet<>();
      getAttributeTypes(attributeTypes, this, branch);
      return attributeTypes;
   }

   private static void getAttributeTypes(Set<AttributeTypeToken> attributeTypes, ArtifactType artifactType, Branch branch) {
      Map<BranchId, Collection<AttributeType>> validityMap =
         artifactType.getFieldValue(ARTIFACT_TYPE_ATTRIBUTES_FIELD_KEY);

      if (!validityMap.isEmpty()) {
         for (BranchId ancestor : branch.getAncestors()) {
            Collection<AttributeType> items = validityMap.get(ancestor);
            if (items != null) {
               attributeTypes.addAll(items);
            }
         }
      }

      for (ArtifactType superType : artifactType.getSuperArtifactTypes()) {
         getAttributeTypes(attributeTypes, superType, branch);
      }
   }

   @Override
   public boolean isValidAttributeType(AttributeTypeId attributeType) {
      Map<BranchId, Collection<AttributeType>> validityMap = getFieldValue(ARTIFACT_TYPE_ATTRIBUTES_FIELD_KEY);

      for (Collection<AttributeType> types : validityMap.values()) {
         if (types.contains(attributeType)) {
            return true;
         }
      }

      for (ArtifactType superType : getSuperArtifactTypes()) {
         if (superType.isValidAttributeType(attributeType)) {
            return true;
         }
      }
      return false;
   }

   private AttributeType getValidAttributeType(AttributeTypeToken attributeType) {
      Map<BranchId, Collection<AttributeType>> validityMap = getFieldValue(ARTIFACT_TYPE_ATTRIBUTES_FIELD_KEY);

      for (Collection<AttributeType> types : validityMap.values()) {
         if (types.contains(attributeType)) {
            for (AttributeType type : types) {
               if (type.getId() == attributeType.getId()) {
                  return type;
               }
            }
         }
      }
      for (ArtifactType superType : getSuperArtifactTypes()) {
         return superType.getValidAttributeType(attributeType);
      }
      return null;
   }

   @Override
   public int getMin(AttributeTypeToken attributeType) {
      if (isValidAttributeType(attributeType)) {
         return getValidAttributeType(attributeType).getMinOccurrences();
      }
      return -1;
   }

   @Override
   public int getMax(AttributeTypeToken attributeType) {
      if (isValidAttributeType(attributeType)) {
         return getValidAttributeType(attributeType).getMaxOccurrences();
      }
      return -1;
   }

   @Override
   public List<AttributeTypeToken> getValidAttributeTypes() {
      List<AttributeTypeToken> attributeTypes = new ArrayList<>(100);
      getValidAttributeTypes(attributeTypes, this);

      for (ArtifactType superType : getSuperArtifactTypes()) {
         getValidAttributeTypes(attributeTypes, superType);
      }
      return attributeTypes;
   }

   private void getValidAttributeTypes(List<AttributeTypeToken> attributeTypes, ArtifactType artifactType) {
      Map<BranchId, Collection<AttributeType>> validityMap =
         artifactType.getFieldValue(ARTIFACT_TYPE_ATTRIBUTES_FIELD_KEY);

      for (Collection<AttributeType> types : validityMap.values()) {
         attributeTypes.addAll(types);
      }
   }

   @Override
   public boolean isAbstract() {
      return getFieldValueLogException(false, ARTIFACT_IS_ABSTRACT_FIELD_KEY);
   }

   @Override
   public List<ArtifactTypeToken> getSuperTypes() {
      return org.eclipse.osee.framework.jdk.core.util.Collections.cast(getSuperArtifactTypes());
   }

   public void setAbstract(boolean isAbstract) {
      setFieldLogException(ARTIFACT_IS_ABSTRACT_FIELD_KEY, isAbstract);
   }

   @Override
   public <T extends EnumToken> List<T> getValidEnumValues(AttributeTypeEnum<T> attributeType) {
      return null;
   }

   @Override
   public String getAttributeDefault(AttributeTypeToken attributeType) {
      return "";
   }
}