/*********************************************************************
 * Copyright (c) 2019 Boeing
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

package org.eclipse.osee.framework.core.data;

import static org.eclipse.osee.framework.core.enums.CoreArtifactTypes.Artifact;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.framework.core.enums.EnumToken;
import org.eclipse.osee.framework.jdk.core.type.Id;
import org.eclipse.osee.framework.jdk.core.type.Named;
import org.eclipse.osee.framework.jdk.core.type.NamedId;
import org.eclipse.osee.framework.jdk.core.type.NamedIdBase;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;

/**
 * @author Ryan D. Brooks
 */
public interface ArtifactTypeToken extends NamedId, ArtifactTypeId {
   ArtifactTypeToken SENTINEL = valueOf(Id.SENTINEL, Named.SENTINEL);

   static ArtifactTypeToken valueOf(long id, String name, ArtifactTypeToken... superTypes) {
      return new AttributeMultiplicity(id, NamespaceToken.OSEE, name, false, Arrays.asList(superTypes)).get();
   }

   Multiplicity getMultiplicity(AttributeTypeToken attributeType);

   default void getSingletonAttributeTypes(Set<AttributeTypeToken> attributeTypeTokens) {
      //This implementation should never be called
   }

   static ArtifactTypeToken create(Long id, NamespaceToken namespace, String name, boolean isAbstract, List<ArtifactTypeToken> superTypes) {
      return new AttributeMultiplicity(id, namespace, name, isAbstract, superTypes).get();
   }

   <T> T getAttributeDefault(AttributeTypeGeneric<T> attributeType);

   default boolean inheritsFromAny(Collection<ArtifactTypeToken> artTypes) {
      for (ArtifactTypeToken inheritType : artTypes) {
         if (this.inheritsFrom(inheritType)) {
            return true;
         }
      }
      return false;
   }

   default boolean inheritsFrom(ArtifactTypeId otherType) {
      if (equals(otherType)) {
         return true;
      } else {
         for (ArtifactTypeToken superType : getSuperTypes()) {
            if (superType.inheritsFrom(otherType)) {
               return true;
            }
         }
      }
      return false;
   }

   List<ArtifactTypeToken> getDirectDescendantTypes();

   default List<ArtifactTypeToken> getAllDescendantTypes() {
      List<ArtifactTypeToken> allDescendantTypes = new ArrayList<>();
      getAllDescendantTypes(allDescendantTypes);
      return allDescendantTypes;
   }

   default void getAllDescendantTypes(List<ArtifactTypeToken> allDescendantTypes) {
      for (ArtifactTypeToken descendant : getDirectDescendantTypes()) {
         allDescendantTypes.add(descendant);
         descendant.getAllDescendantTypes(allDescendantTypes);
      }
   }

   boolean isAbstract();

   List<ArtifactTypeToken> getSuperTypes();

   List<AttributeTypeToken> getValidAttributeTypes();

   boolean isValidAttributeType(AttributeTypeId attributeType);

   int getMin(AttributeTypeToken attributeType);

   int getMax(AttributeTypeToken attributeType);

   <T extends EnumToken> List<T> getValidEnumValues(AttributeTypeEnum<T> attributeType);

   public static ArtifactTypeToken create(Long id, NamespaceToken namespace, String name, boolean isAbstract, AttributeMultiplicity attributeTypes, List<ArtifactTypeToken> superTypes) {
      final class ArtifactTypeTokenImpl extends NamedIdBase implements ArtifactTypeToken {
         private final boolean isAbstract;
         private final List<ArtifactTypeToken> superTypes;
         private final List<ArtifactTypeToken> directDescendants = new ArrayList<>(4);
         private final AttributeMultiplicity attributeTypes;
         private final NamespaceToken namespace;

         public ArtifactTypeTokenImpl(Long id, NamespaceToken namespace, String name, boolean isAbstract, AttributeMultiplicity attributeTypes, List<ArtifactTypeToken> superTypes) {
            super(id, name);
            this.isAbstract = isAbstract;
            this.superTypes = superTypes;
            this.attributeTypes = attributeTypes;
            this.namespace = namespace;
            if (superTypes.size() > 1 && this.superTypes.contains(Artifact)) {
               throw new OseeArgumentException("Multiple super types for artifact type [%s] and and supertype Artifact",
                  name);
            }
            // since each superType has already run the following loop, they already have all their inherited multiplicity
            for (ArtifactTypeToken superType : superTypes) {
               attributeTypes.putAll(((ArtifactTypeTokenImpl) superType).attributeTypes);
            }

            for (ArtifactTypeToken superType : superTypes) {
               ((ArtifactTypeTokenImpl) superType).addDirectDescendantType(this);
            }
         }

         @Override
         public boolean isAbstract() {
            return isAbstract;
         }

         @Override
         public List<ArtifactTypeToken> getSuperTypes() {
            return superTypes;
         }

         @Override
         public List<ArtifactTypeToken> getDirectDescendantTypes() {
            return directDescendants;
         }

         public void addDirectDescendantType(ArtifactTypeToken descendantType) {
            directDescendants.add(descendantType);
         }

         @Override
         public List<AttributeTypeToken> getValidAttributeTypes() {
            return attributeTypes.getValidAttributeTypes();
         }

         @Override
         public boolean isValidAttributeType(AttributeTypeId attributeType) {
            return attributeTypes.containsKey(attributeType);
         }

         @Override
         public int getMin(AttributeTypeToken attributeType) {
            if (isValidAttributeType(attributeType)) {
               return attributeTypes.getMinimum(attributeType);
            }
            return -1;
         }

         @Override
         public int getMax(AttributeTypeToken attributeType) {
            if (isValidAttributeType(attributeType)) {
               return attributeTypes.getMaximum(attributeType);
            }
            return -1;
         }

         @Override
         public Multiplicity getMultiplicity(AttributeTypeToken attributeType) {
            return attributeTypes.getMultiplicity(attributeType);
         }

         @Override
         public <T extends EnumToken> List<T> getValidEnumValues(AttributeTypeEnum<T> attributeType) {
            return attributeTypes.getValidEnumValues(attributeType);
         }

         @Override
         public <T> T getAttributeDefault(AttributeTypeGeneric<T> attributeType) {
            return attributeTypes.getAttributeDefault(attributeType);
         }

         @Override
         public void getSingletonAttributeTypes(Set<AttributeTypeToken> attributeTypeTokens) {
            attributeTypes.getSingletonAttributeTypes(attributeTypeTokens);
         }

         @Override
         public String getName() {
            String name = super.getName();
            if (namespace.notEqual(NamespaceToken.OSEE) && !superTypes.isEmpty()) {
               for (ArtifactTypeToken superType : superTypes) {
                  if (name.equals(superType.getName())) {
                     name = String.format("%s %s", namespace.toString().toUpperCase(), name);
                     break;
                  }
               }
            }
            return name;
         }

         @Override
         public String toString() {
            String name = getName();
            return name == null ? super.toString() : name;
         }
      }
      return new ArtifactTypeTokenImpl(id, namespace, name, isAbstract, attributeTypes, superTypes);
   }
}