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

import org.eclipse.osee.framework.jdk.core.type.Id;
import org.eclipse.osee.framework.jdk.core.type.Named;
import org.eclipse.osee.framework.jdk.core.type.NamedIdDescription;

/**
 * @author Ryan D. Brooks
 */
public abstract class AttributeTypeGeneric<T> extends NamedIdDescription implements AttributeTypeToken {
   public static final AttributeTypeString SENTINEL =
      AttributeTypeToken.createString(Id.SENTINEL, NamespaceToken.OSEE, Named.SENTINEL, Named.SENTINEL, "");

   private final String mediaType;
   private final TaggerTypeToken taggerType;
   private final NamespaceToken namespace;

   public AttributeTypeGeneric(Long id, NamespaceToken namespace, String name, String mediaType, String description, TaggerTypeToken taggerType) {
      super(id, name, description);
      this.namespace = namespace;
      this.mediaType = mediaType;
      this.taggerType = taggerType;
   }

   @Override
   public String getMediaType() {
      return mediaType;
   }

   /**
    * @param storedValue is the raw String stored in the database
    * @return the attribute value in its native Java representation
    */
   public abstract T valueFromStorageString(String storedValue);

   public String storageStringFromValue(T value) {
      return value.toString();
   }

   /**
    * @param value the attribute value in its native Java representation (converted from the storedValue as needed)
    * @return a user friendly text representation of the attribute value
    */
   public String getDisplayableString(T value) {
      return storageStringFromValue(value);
   }

   @Override
   public TaggerTypeToken getTaggerType() {
      return taggerType;
   }

   @Override
   public NamespaceToken getNamespace() {
      return namespace;
   }

   @Override
   public boolean isTaggable() {
      return taggerType.isValid();
   }
}