/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.internal.attribute;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.data.AttributeId;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Roberto E. Escobar
 */
public interface AttributeManager extends AttributeContainer {

   void setAttributesNotDirty();

   void deleteAttributesByArtifact() throws OseeCoreException;

   void unDeleteAttributesByArtifact() throws OseeCoreException;

   <T> T getSoleAttributeValue(IAttributeType attributeType) throws OseeCoreException;

   <T> T getSoleAttributeValue(IAttributeType attributeType, T defaultValue) throws OseeCoreException;

   String getSoleAttributeAsString(IAttributeType attributeType) throws OseeCoreException;

   String getSoleAttributeAsString(IAttributeType attributeType, String defaultValue) throws OseeCoreException;

   <T> List<T> getAttributeValues(IAttributeType attributeType) throws OseeCoreException;

   <T> void setSoleAttributeValue(IAttributeType attributeType, T value) throws OseeCoreException;

   void setSoleAttributeFromStream(IAttributeType attributeType, InputStream inputStream) throws OseeCoreException;

   void setSoleAttributeFromString(IAttributeType attributeType, String value) throws OseeCoreException;

   <T> void setAttributesFromValues(IAttributeType attributeType, T... values) throws OseeCoreException;

   <T> void setAttributesFromValues(IAttributeType attributeType, Collection<T> values) throws OseeCoreException;

   void setAttributesFromStrings(IAttributeType attributeType, String... values) throws OseeCoreException;

   void setAttributesFromStrings(IAttributeType attributeType, Collection<String> values) throws OseeCoreException;

   void deleteSoleAttribute(IAttributeType attributeType) throws OseeCoreException;

   void deleteAttributes(IAttributeType attributeType) throws OseeCoreException;

   void deleteAttributesWithValue(IAttributeType attributeType, Object value) throws OseeCoreException;

   <T> Attribute<T> createAttribute(IAttributeType attributeType) throws OseeCoreException;

   <T> Attribute<T> createAttribute(IAttributeType attributeType, T value) throws OseeCoreException;

   <T> Attribute<T> createAttributeFromString(IAttributeType attributeType, String value) throws OseeCoreException;

   List<Attribute<Object>> getAttributes() throws OseeCoreException;

   <T> List<Attribute<T>> getAttributes(IAttributeType attributeType) throws OseeCoreException;

   List<Attribute<Object>> getAttributes(DeletionFlag deletionFlag) throws OseeCoreException;

   <T> List<Attribute<T>> getAttributes(IAttributeType attributeType, DeletionFlag deletionFlag) throws OseeCoreException;

   Attribute<Object> getAttributeById(AttributeId attributeId) throws OseeCoreException;

   Attribute<Object> getAttributeById(AttributeId attributeId, DeletionFlag includeDeleted) throws OseeCoreException;
}
