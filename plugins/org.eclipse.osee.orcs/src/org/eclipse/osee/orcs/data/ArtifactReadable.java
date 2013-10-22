/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.data;

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Megumi Telles
 * @author Roberto E. Escobar
 * @author Andrew M. Finkbeiner
 */
public interface ArtifactReadable extends ArtifactId, HasLocalId, HasBranch, HasTransaction, OrcsReadable {

   IArtifactType getArtifactType() throws OseeCoreException;

   boolean isOfType(IArtifactType... otherTypes) throws OseeCoreException;

   ////////////////////

   int getAttributeCount(IAttributeType type) throws OseeCoreException;

   int getAttributeCount(IAttributeType type, DeletionFlag deletionFlag) throws OseeCoreException;

   boolean isAttributeTypeValid(IAttributeType attributeType) throws OseeCoreException;

   Collection<? extends IAttributeType> getValidAttributeTypes() throws OseeCoreException;

   Collection<? extends IAttributeType> getExistingAttributeTypes() throws OseeCoreException;

   <T> T getSoleAttributeValue(IAttributeType attributeType) throws OseeCoreException;

   String getSoleAttributeAsString(IAttributeType attributeType) throws OseeCoreException;

   String getSoleAttributeAsString(IAttributeType attributeType, String defaultValue) throws OseeCoreException;

   <T> List<T> getAttributeValues(IAttributeType attributeType) throws OseeCoreException;

   ////////////////////

   AttributeReadable<Object> getAttributeById(AttributeId attributeId) throws OseeCoreException;

   ResultSet<? extends AttributeReadable<Object>> getAttributes() throws OseeCoreException;

   <T> ResultSet<? extends AttributeReadable<T>> getAttributes(IAttributeType attributeType) throws OseeCoreException;

   ResultSet<? extends AttributeReadable<Object>> getAttributes(DeletionFlag deletionFlag) throws OseeCoreException;

   <T> ResultSet<? extends AttributeReadable<T>> getAttributes(IAttributeType attributeType, DeletionFlag deletionFlag) throws OseeCoreException;

   ////////////////////
   int getMaximumRelationAllowed(IRelationTypeSide relationTypeSide) throws OseeCoreException;

   Collection<? extends IRelationType> getValidRelationTypes() throws OseeCoreException;

   Collection<? extends IRelationType> getExistingRelationTypes() throws OseeCoreException;

   ArtifactReadable getParent() throws OseeCoreException;

   ResultSet<ArtifactReadable> getChildren() throws OseeCoreException;

   ResultSet<ArtifactReadable> getRelated(IRelationTypeSide relationTypeSide) throws OseeCoreException;

   boolean areRelated(IRelationTypeSide typeAndSide, ArtifactReadable readable) throws OseeCoreException;

   int getRelatedCount(IRelationTypeSide typeAndSide) throws OseeCoreException;

   String getRationale(IRelationTypeSide typeAndSide, ArtifactReadable readable) throws OseeCoreException;

}
