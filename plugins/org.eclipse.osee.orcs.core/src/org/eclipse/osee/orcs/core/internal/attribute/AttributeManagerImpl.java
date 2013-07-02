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
package org.eclipse.osee.orcs.core.internal.attribute;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.framework.core.data.AbstractIdentity;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.exception.AttributeDoesNotExist;
import org.eclipse.osee.framework.core.exception.MultipleAttributesExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.orcs.core.ds.ArtifactData;
import org.eclipse.osee.orcs.core.ds.HasOrcsData;
import org.eclipse.osee.orcs.core.internal.artifact.AttributeManager;
import org.eclipse.osee.orcs.data.AttributeReadable;
import org.eclipse.osee.orcs.data.AttributeWriteable;

/**
 * @author Roberto E. Escobar
 */
public abstract class AttributeManagerImpl extends AbstractIdentity<String> implements HasOrcsData<ArtifactData>, AttributeManager, AttributeExceptionFactory {

   private final AttributeSetHelper<Object, String> ATTRIBUTE_STRING_SETTER = new FromStringAttributeSetHelper();

   private final AttributeCollection attributes;
   private boolean isLoaded;

   private final AttributeFactory attributeFactory;

   protected AttributeManagerImpl(AttributeFactory attributeFactory) {
      super();
      this.attributeFactory = attributeFactory;
      this.attributes = new AttributeCollection(this);
   }

   protected List<Attribute<?>> getAllAttributes() {
      return attributes.getAllAttributes();
   }

   @Override
   public synchronized void add(IAttributeType type, Attribute<? extends Object> attribute) {
      attributes.addAttribute(type, attribute);
      attribute.getOrcsData().setArtifactId(getLocalId());
   }

   @Override
   public void remove(IAttributeType type, Attribute<? extends Object> attribute) {
      attributes.removeAttribute(type, attribute);
      attribute.getOrcsData().setArtifactId(-1);
   }

   @Override
   public boolean isLoaded() {
      return isLoaded;
   }

   @Override
   public void setLoaded(boolean value) throws OseeCoreException {
      this.isLoaded = value;
      if (value == true) {
         onLoaded();
      }
   }

   @Override
   public List<Attribute<Object>> getAttributesDirty() throws OseeCoreException {
      return attributes.getAttributeListDirties();
   }

   @Override
   public void setAttributesNotDirty() {
      for (Attribute<?> attribute : getAllAttributes()) {
         attribute.clearDirty();
      }
   }

   @Override
   public boolean areAttributesDirty() {
      return attributes.hasAttributesDirty();
   }

   @Override
   public String getName() {
      String name;
      try {
         name = getSoleAttributeAsString(CoreAttributeTypes.Name);
      } catch (Exception ex) {
         name = Lib.exceptionToString(ex);
      }
      return name;
   }

   @Override
   public int getMaximumAttributeTypeAllowed(IAttributeType attributeType) throws OseeCoreException {
      int result = -1;
      if (isAttributeTypeValid(attributeType)) {
         result = attributeFactory.getMaxOccurrenceLimit(attributeType);
      }
      return result;
   }

   @Override
   public int getMinimumAttributeTypeAllowed(IAttributeType attributeType) throws OseeCoreException {
      int result = -1;
      if (isAttributeTypeValid(attributeType)) {
         result = attributeFactory.getMinOccurrenceLimit(attributeType);
      }
      return result;
   }

   @Override
   public Collection<? extends IAttributeType> getExistingAttributeTypes() throws OseeCoreException {
      ensureAttributesLoaded();
      return attributes.getExistingTypes(DeletionFlag.EXCLUDE_DELETED);
   }

   @Override
   public int getAttributeCount(IAttributeType attributeType) throws OseeCoreException {
      return getAttributesExcludeDeleted(attributeType).size();
   }

   @Override
   public List<AttributeReadable<Object>> getAttributes() throws OseeCoreException {

      List<Attribute<Object>> items = getAttributesExcludeDeleted();
      return Collections.castAll(items);
   }

   @Override
   public <T> List<AttributeReadable<T>> getAttributes(IAttributeType attributeType) throws OseeCoreException {
      List<Attribute<T>> items = getAttributesExcludeDeleted(attributeType);
      return Collections.castAll(items);
   }

   @Override
   public <T> List<T> getAttributeValues(IAttributeType attributeType) throws OseeCoreException {
      List<Attribute<T>> attributes = getAttributesExcludeDeleted(attributeType);

      List<T> values = new LinkedList<T>();
      for (Attribute<T> attribute : attributes) {
         T value = attribute.getValue();
         if (value != null) {
            values.add(value);
         }
      }
      return values;
   }

   @Override
   public int getAttributeCount(IAttributeType attributeType, DeletionFlag includeDeleted) throws OseeCoreException {
      return getAttributesHelper(attributeType, includeDeleted).size();
   }

   @Override
   public List<AttributeReadable<Object>> getAttributes(DeletionFlag includeDeleted) throws OseeCoreException {
      List<Attribute<Object>> items = getAttributesHelper(includeDeleted);
      return Collections.castAll(items);
   }

   @Override
   public <T> List<AttributeReadable<T>> getAttributes(IAttributeType attributeType, DeletionFlag includeDeleted) throws OseeCoreException {
      List<Attribute<T>> items = getAttributesHelper(attributeType, includeDeleted);
      return Collections.castAll(items);
   }

   @Override
   public <T> List<AttributeWriteable<T>> getWriteableAttributes() throws OseeCoreException {
      List<Attribute<T>> items = getAttributesExcludeDeleted();
      return Collections.castAll(items);
   }

   @Override
   public <T> List<AttributeWriteable<T>> getWriteableAttributes(IAttributeType attributeType) throws OseeCoreException {
      List<Attribute<T>> items = getAttributesExcludeDeleted(attributeType);
      return Collections.castAll(items);
   }

   @Override
   public String getSoleAttributeAsString(IAttributeType attributeType, String defaultValue) throws OseeCoreException {
      String toReturn = defaultValue;
      List<Attribute<Object>> items = getAttributesExcludeDeleted(attributeType);
      if (!items.isEmpty()) {
         Attribute<Object> firstItem = items.iterator().next();
         toReturn = String.valueOf(firstItem.getValue());
      }
      return toReturn;
   }

   @Override
   public String getSoleAttributeAsString(IAttributeType attributeType) throws OseeCoreException {
      String toReturn = null;
      Object value = getSoleAttributeValue(attributeType);
      if (value instanceof InputStream) {
         InputStream inputStream = (InputStream) value;
         try {
            toReturn = Lib.inputStreamToString(inputStream);
         } catch (IOException ex) {
            OseeExceptions.wrapAndThrow(ex);
         } finally {
            try {
               inputStream.close();
            } catch (IOException ex) {
               OseeExceptions.wrapAndThrow(ex);
            }
         }
      } else {
         if (value != null) {
            toReturn = value.toString();
         }
      }
      return toReturn;
   }

   @Override
   public <T> T getSoleAttributeValue(IAttributeType attributeType) throws OseeCoreException {
      Attribute<T> attribute = getSoleAttribute(attributeType);
      return attribute.getValue();
   }

   @Override
   public <T> void setSoleAttributeValue(IAttributeType attributeType, T value) throws OseeCoreException {
      Attribute<T> attribute = getOrCreateSoleAttribute(attributeType);
      attribute.setValue(value);
   }

   @Override
   public void setSoleAttributeFromString(IAttributeType attributeType, String value) throws OseeCoreException {
      getOrCreateSoleAttribute(attributeType).setFromString(value);
   }

   @Override
   public void setSoleAttributeFromStream(IAttributeType attributeType, InputStream inputStream) throws OseeCoreException {
      getOrCreateSoleAttribute(attributeType).setValueFromInputStream(inputStream);
   }

   @Override
   public void setAttributesFromStrings(IAttributeType attributeType, String... values) throws OseeCoreException {
      setAttributesFromStrings(attributeType, Arrays.asList(values));
   }

   @Override
   public void setAttributesFromStrings(IAttributeType attributeType, Collection<String> values) throws OseeCoreException {
      setAttributesFromValuesHelper(ATTRIBUTE_STRING_SETTER, attributeType, values);
   }

   @Override
   public <T> void setAttributesFromValues(IAttributeType attributeType, T... values) throws OseeCoreException {
      setAttributesFromValues(attributeType, Arrays.asList(values));
   }

   @Override
   public <T> void setAttributesFromValues(IAttributeType attributeType, Collection<T> values) throws OseeCoreException {
      AttributeSetHelper<T, T> setter = new TypedValueAttributeSetHelper<T>();
      setAttributesFromValuesHelper(setter, attributeType, values);
   }

   @Override
   public void deleteAttributesByArtifact() throws OseeCoreException {
      for (Attribute<?> attribute : getAttributesIncludeDeleted()) {
         attribute.setArtifactDeleted();
      }
   }

   @Override
   public void deleteSoleAttribute(IAttributeType attributeType) throws OseeCoreException {
      Attribute<?> attribute = getSoleAttribute(attributeType);
      if (attribute != null) {
         deleteAttribute(attribute);
      }
   }

   @Override
   public void deleteAttributes(IAttributeType attributeType) throws OseeCoreException {
      for (Attribute<?> attribute : getAttributesIncludeDeleted(attributeType)) {
         attribute.delete();
      }
   }

   @Override
   public void deleteAttributesWithValue(IAttributeType attributeType, Object value) throws OseeCoreException {
      for (Attribute<Object> attribute : getAttributesIncludeDeleted(attributeType)) {
         if (attribute.getValue().equals(value)) {
            deleteAttribute(attribute);
            break;
         }
      }
   }

   private void deleteAttribute(Attribute<?> attribute) throws OseeCoreException {
      IAttributeType attributeType = attribute.getAttributeType();
      checkMultiplicityCanDelete(attributeType);
      attribute.delete();
   }

   @Override
   public <T> AttributeWriteable<T> createAttribute(IAttributeType attributeType) throws OseeCoreException {
      return internalCreateAttributeHelper(attributeType);
   }

   @Override
   public <T> AttributeWriteable<T> createAttribute(IAttributeType attributeType, T value) throws OseeCoreException {
      Attribute<T> attribute = internalCreateAttributeHelper(attributeType);
      attribute.setValue(value);
      return attribute;
   }

   @Override
   public <T> AttributeWriteable<T> createAttributeFromString(IAttributeType attributeType, String value) throws OseeCoreException {
      Attribute<T> attribute = internalCreateAttributeHelper(attributeType);
      attribute.setFromString(value);
      return attribute;
   }

   //////////////////////////////////////////////////////////////
   private <T> Attribute<T> internalCreateAttributeHelper(IAttributeType attributeType) throws OseeCoreException {
      checkTypeValid(attributeType);
      checkMultiplicityCanAdd(attributeType);
      Attribute<T> attr = attributeFactory.createAttributeWithDefaults(this, getOrcsData(), attributeType);
      add(attributeType, attr);
      return attr;
   }

   private <T> Attribute<T> getOrCreateSoleAttribute(IAttributeType attributeType) throws OseeCoreException {
      ResultSet<Attribute<T>> result = attributes.getAttributeSet(attributeType, DeletionFlag.EXCLUDE_DELETED);
      Attribute<T> attribute = result.getAtMostOneOrNull();
      if (attribute == null) {
         attribute = internalCreateAttributeHelper(attributeType);
      }
      return attribute;
   }

   private <T> Attribute<T> getSoleAttribute(IAttributeType attributeType) throws OseeCoreException {
      ensureAttributesLoaded();
      ResultSet<Attribute<T>> result = attributes.getAttributeSet(attributeType, DeletionFlag.EXCLUDE_DELETED);
      return result.getExactlyOne();
   }

   //////////////////////////////////////////////////////////////

   private <T> List<Attribute<T>> getAttributesExcludeDeleted() throws OseeCoreException {
      return getAttributesHelper(DeletionFlag.EXCLUDE_DELETED);
   }

   private <T> List<Attribute<T>> getAttributesExcludeDeleted(IAttributeType attributeType) throws OseeCoreException {
      return getAttributesHelper(attributeType, DeletionFlag.EXCLUDE_DELETED);
   }

   private <T> List<Attribute<T>> getAttributesIncludeDeleted() throws OseeCoreException {
      return getAttributesHelper(DeletionFlag.INCLUDE_DELETED);
   }

   private <T> List<Attribute<T>> getAttributesIncludeDeleted(IAttributeType attributeType) throws OseeCoreException {
      return getAttributesHelper(attributeType, DeletionFlag.INCLUDE_DELETED);
   }

   private <T> List<Attribute<T>> getAttributesHelper(DeletionFlag includeDeleted) throws OseeCoreException {
      ensureAttributesLoaded();
      return attributes.getAttributeList(includeDeleted);
   }

   private <T> List<Attribute<T>> getAttributesHelper(IAttributeType attributeType, DeletionFlag includeDeleted) throws OseeCoreException {
      ensureAttributesLoaded();
      return attributes.getAttributeList(attributeType, includeDeleted);
   }
   //////////////////////////////////////////////////////////////

   private interface AttributeSetHelper<A, V> {

      boolean matches(Attribute<A> attribute, V value) throws OseeCoreException;

      void setAttributeValue(Attribute<A> attribute, V value) throws OseeCoreException;

      void createAttribute(IAttributeType attributeType, V value) throws OseeCoreException;
   }

   private <A, T> void setAttributesFromValuesHelper(AttributeSetHelper<A, T> helper, IAttributeType attributeType, Collection<T> values) throws OseeCoreException {
      ensureAttributesLoaded();

      Set<T> uniqueItems = new LinkedHashSet<T>(values);
      List<Attribute<A>> remainingAttributes = getAttributesExcludeDeleted(attributeType);
      List<T> remainingNewValues = new ArrayList<T>(uniqueItems.size());

      // all existing attributes matching a new value will be left untouched
      for (T newValue : uniqueItems) {
         boolean found = false;
         for (Attribute<A> attribute : remainingAttributes) {
            if (helper.matches(attribute, newValue)) {
               remainingAttributes.remove(attribute);
               found = true;
               break;
            }
         }

         if (!found) {
            remainingNewValues.add(newValue);
         }
      }

      for (T newValue : remainingNewValues) {
         if (remainingAttributes.isEmpty()) {
            helper.createAttribute(attributeType, newValue);
         } else {
            int index = remainingAttributes.size() - 1;
            Attribute<A> attribute = remainingAttributes.get(index);
            helper.setAttributeValue(attribute, newValue);
            remainingAttributes.remove(index);
         }
      }

      for (Attribute<A> attribute : remainingAttributes) {
         attribute.delete();
      }
   }

   private final class TypedValueAttributeSetHelper<T> implements AttributeSetHelper<T, T> {

      @Override
      public boolean matches(Attribute<T> attribute, T value) throws OseeCoreException {
         return value.equals(attribute.getValue());
      }

      @Override
      public void setAttributeValue(Attribute<T> attribute, T value) throws OseeCoreException {
         attribute.setValue(value);
      }

      @Override
      public void createAttribute(IAttributeType attributeType, T value) throws OseeCoreException {
         ResultSet<Attribute<T>> result =
            attributes.getAttributeSetFromValue(attributeType, DeletionFlag.EXCLUDE_DELETED, value);
         if (result.getOneOrNull() == null) {
            createAttribute(attributeType, value);
         }
      }
   }

   private final class FromStringAttributeSetHelper implements AttributeSetHelper<Object, String> {

      private String asString(Object object) {
         return String.valueOf(object);
      }

      @Override
      public boolean matches(Attribute<Object> attribute, String value) throws OseeCoreException {
         Object attrValue = attribute.getValue();
         return value.equals(asString(attrValue));
      }

      @Override
      public void setAttributeValue(Attribute<Object> attribute, java.lang.String value) throws OseeCoreException {
         attribute.setFromString(value);
      }

      @Override
      public void createAttribute(IAttributeType attributeType, String value) throws OseeCoreException {
         ResultSet<Attribute<Object>> result =
            attributes.getAttributeSetFromString(attributeType, DeletionFlag.EXCLUDE_DELETED, value);
         if (result.getOneOrNull() == null) {
            createAttributeFromString(attributeType, value);
         }
      }
   }

   //////////////////////////////////////////////////////////////

   private void checkTypeValid(IAttributeType attributeType) throws OseeCoreException {
      if (!CoreAttributeTypes.Name.equals(attributeType)) {
         Conditions.checkExpressionFailOnTrue(!isAttributeTypeValid(attributeType),
            "The attribute type [%s] is not valid for artifacts [%s]", attributeType, getExceptionString());
      }
   }

   private void checkMultiplicityCanAdd(IAttributeType attributeType) throws OseeCoreException {
      checkMultiplicity(attributeType, getAttributeCount(attributeType) + 1);
   }

   private void checkMultiplicityCanDelete(IAttributeType attributeType) throws OseeCoreException {
      checkMultiplicity(attributeType, getAttributeCount(attributeType) - 1);
   }

   private void checkMultiplicity(IAttributeType attributeType, int count) throws OseeCoreException {
      MultiplicityState state = getAttributeMuliplicityState(attributeType, count);
      switch (state) {
         case MAX_VIOLATION:
            throw new OseeStateException("Attribute type [%s] exceeds max occurrence rule on artifacts [%s]",
               attributeType, getExceptionString());
         case MIN_VIOLATION:
            throw new OseeStateException("Attribute type [%s] is less than min occurrence rule on artifacts [%s]",
               attributeType, getExceptionString());
         default:
            break;
      }
   }

   private static enum MultiplicityState {
      IS_VALID,
      MAX_VIOLATION,
      MIN_VIOLATION;
   }

   private MultiplicityState getAttributeMuliplicityState(IAttributeType attributeType, int count) throws OseeCoreException {
      MultiplicityState state = MultiplicityState.IS_VALID;
      if (count > attributeFactory.getMaxOccurrenceLimit(attributeType)) {
         state = MultiplicityState.MAX_VIOLATION;
      } else if (count < attributeFactory.getMinOccurrenceLimit(attributeType)) {
         state = MultiplicityState.MIN_VIOLATION;
      }
      return state;
   }

   //////////////////////////////////////////////////////////////

   private void onLoaded() throws OseeCoreException {
      //      computeLastDateModified();
      meetMinimumAttributes();
   }

   private void ensureAttributesLoaded() {
      //      if (!isLoaded() && isInDb()) {
      //         ArtifactLoader.loadArtifactData(this, LoadLevel.ATTRIBUTE);
      //      }
   }

   private void meetMinimumAttributes() throws OseeCoreException {
      for (IAttributeType attributeType : getValidAttributeTypes()) {
         int missingCount = getRemainingAttributeCount(attributeType);
         for (int i = 0; i < missingCount; i++) {
            Attribute<Object> attr = attributeFactory.createAttributeWithDefaults(this, getOrcsData(), attributeType);
            add(attributeType, attr);
            attr.clearDirty();
         }
      }
   }

   private final int getRemainingAttributeCount(IAttributeType attributeType) throws OseeCoreException {
      int minLimit = attributeFactory.getMinOccurrenceLimit(attributeType);
      return minLimit - getAttributeCount(attributeType);
   }

   //////////////////////////////////////////////////////////////

   @Override
   public MultipleAttributesExist createManyExistException(IAttributeType type, int count) {
      MultipleAttributesExist toReturn;
      if (type != null) {
         toReturn =
            new MultipleAttributesExist(
               "The attribute type [%s] has [%s] instances on [%s], but only [1] instance is allowed", type.getName(),
               count, getExceptionString());
      } else {
         toReturn =
            new MultipleAttributesExist(
               "Multiple items found - total instances [%s] on [%s], but only [1] instance is allowed", count,
               getExceptionString());
      }
      return toReturn;
   }

   @Override
   public AttributeDoesNotExist createDoesNotExistException(IAttributeType type) {
      AttributeDoesNotExist toReturn;
      if (type != null) {
         toReturn =
            new AttributeDoesNotExist("Attribute of type [%s] could not be found on [%s]", type.getName(),
               getExceptionString());
      } else {
         toReturn = new AttributeDoesNotExist("Attribute not be found on [%s]", getExceptionString());
      }
      return toReturn;
   }
}
