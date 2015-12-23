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
package org.eclipse.osee.framework.core.model.type;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.model.AbstractOseeIdType;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.IOseeField;
import org.eclipse.osee.framework.core.model.OseeField;
import org.eclipse.osee.framework.core.model.internal.fields.ArtifactSuperTypeField;
import org.eclipse.osee.framework.core.model.internal.fields.ArtifactTypeAttributesField;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Robert A. Fisher
 */
public class ArtifactType extends AbstractOseeIdType<Long>implements IArtifactType {

   public static final String ARTIFACT_IS_ABSTRACT_FIELD_KEY = "osee.artifact.type.is.abstract.field";
   public static final String ARTIFACT_INHERITANCE_FIELD_KEY = "osee.artifact.type.inheritance.field";
   public static final String ARTIFACT_TYPE_ATTRIBUTES_FIELD_KEY = "osee.artifact.type.attributes.field";

   private final Set<ArtifactType> superTypes = new HashSet<>();
   private final Set<ArtifactType> childTypes = new HashSet<>();
   private final Map<IOseeBranch, Collection<AttributeType>> attributes =
      new HashMap<IOseeBranch, Collection<AttributeType>>();

   public ArtifactType(Long guid, String name, boolean isAbstract) {
      super(guid, name);
      initializeFields();
      setAbstract(isAbstract);
   }

   protected void initializeFields() {
      addField(ARTIFACT_IS_ABSTRACT_FIELD_KEY, new OseeField<Boolean>());
      addField(ARTIFACT_INHERITANCE_FIELD_KEY, new ArtifactSuperTypeField(this, superTypes));
      addField(ARTIFACT_TYPE_ATTRIBUTES_FIELD_KEY, new ArtifactTypeAttributesField(attributes));
   }

   public boolean hasSuperArtifactTypes() {
      Collection<ArtifactType> superTypes = getSuperArtifactTypes();
      return superTypes != null && !superTypes.isEmpty();
   }

   public Collection<ArtifactType> getSuperArtifactTypes() {
      Collection<ArtifactType> defaultValue = Collections.emptyList();
      return getFieldValueLogException(defaultValue, ARTIFACT_INHERITANCE_FIELD_KEY);
   }

   public void setSuperTypes(Set<ArtifactType> superType) throws OseeCoreException {
      Set<ArtifactType> originals = new HashSet<>(superTypes);
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

   public Collection<ArtifactType> getFirstLevelDescendantTypes() throws OseeCoreException {
      return getDescendants(this, false);
   }

   public Collection<ArtifactType> getAllDescendantTypes() throws OseeCoreException {
      return getDescendants(this, true);
   }

   private Collection<ArtifactType> getDescendants(ArtifactType artifactType, boolean isRecursionAllowed) throws OseeCoreException {
      Collection<ArtifactType> descendants = new HashSet<>();
      populateDescendants(artifactType, descendants, isRecursionAllowed);
      return descendants;
   }

   private void populateDescendants(ArtifactType artifactType, Collection<ArtifactType> descendants, boolean isRecursionAllowed) throws OseeCoreException {
      for (ArtifactType type : artifactType.childTypes) {
         if (isRecursionAllowed) {
            populateDescendants(type, descendants, isRecursionAllowed);
         }
         descendants.add(type);
      }
   }

   public void setAttributeTypes(Collection<AttributeType> attributeTypes, IOseeBranch branch) throws OseeCoreException {
      IOseeField<?> field = getField(ARTIFACT_TYPE_ATTRIBUTES_FIELD_KEY);
      ((ArtifactTypeAttributesField) field).put(branch, attributeTypes);
   }

   public void setAllAttributeTypes(Map<IOseeBranch, Collection<AttributeType>> attributeTypes) throws OseeCoreException {
      IOseeField<?> field = getField(ARTIFACT_TYPE_ATTRIBUTES_FIELD_KEY);
      ((ArtifactTypeAttributesField) field).set(attributeTypes);
   }

   public boolean isValidAttributeType(IAttributeType attributeType, Branch branch) throws OseeCoreException {
      Collection<IAttributeType> attributes = getAttributeTypes(branch);
      return attributes.contains(attributeType);
   }

   public Map<IOseeBranch, Collection<AttributeType>> getLocalAttributeTypes() throws OseeCoreException {
      return getFieldValue(ARTIFACT_TYPE_ATTRIBUTES_FIELD_KEY);
   }

   public Collection<IAttributeType> getAttributeTypes(Branch branch) throws OseeCoreException {
      // Do not use ARTIFACT_TYPE_ATTRIBUTES_FIELD for this call since it must use branch inheritance to get all attribute types
      Set<IAttributeType> attributeTypes = new HashSet<>();
      getAttributeTypes(attributeTypes, this, branch);
      return attributeTypes;
   }

   private static void getAttributeTypes(Set<IAttributeType> attributeTypes, ArtifactType artifactType, Branch branch) throws OseeCoreException {
      Map<IOseeBranch, Collection<AttributeType>> validityMap = artifactType.getLocalAttributeTypes();

      Branch branchCursor = branch;
      do {
         Collection<AttributeType> items = validityMap.get(branchCursor);
         if (items != null) {
            attributeTypes.addAll(items);
         }
         branchCursor = branchCursor.getParentBranch();
      } while (branchCursor != null);

      for (ArtifactType superType : artifactType.getSuperArtifactTypes()) {
         getAttributeTypes(attributeTypes, superType, branch);
      }
   }

   public boolean isAbstract() {
      return getFieldValueLogException(false, ARTIFACT_IS_ABSTRACT_FIELD_KEY);
   }

   public void setAbstract(boolean isAbstract) {
      setFieldLogException(ARTIFACT_IS_ABSTRACT_FIELD_KEY, isAbstract);
   }

   /**
    * Determines if this artifact type equals, or is a sub-type of, the artifact type specified by the
    * <code>otherType</code> parameter.
    * 
    * @param otherType artifact types to check against
    * @return whether this artifact type inherits from otherType
    */
   public boolean inheritsFrom(IArtifactType... otherTypes) {
      boolean result = false;
      for (IArtifactType otherArtifactType : otherTypes) {
         if (inheritsFromSingle(otherArtifactType)) {
            result = true;
            break;
         }
      }
      return result;
   }

   private boolean inheritsFromSingle(IArtifactType otherType) {
      boolean result = false;
      if (this.equals(otherType)) {
         result = true;
      } else {
         for (ArtifactType superType : getSuperArtifactTypes()) {
            if (superType.inheritsFrom(otherType)) {
               result = true;
               break;
            }
         }
      }
      return result;
   }

   @Override
   public String toString() {
      return getName();
   }
}