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
package org.eclipse.osee.orcs.transaction;

import java.io.InputStream;
import java.util.Collection;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.AttributeId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IArtifactToken;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IRelationSorterId;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.data.Tuple2Type;
import org.eclipse.osee.framework.core.data.Tuple3Type;
import org.eclipse.osee.framework.core.data.Tuple4Type;
import org.eclipse.osee.framework.core.data.TupleTypeId;
import org.eclipse.osee.framework.jdk.core.type.Identifiable;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.TransactionReadable;

/**
 * @author Roberto E. Escobar
 */
public interface TransactionBuilder {

   Long getBranchId();

   Identifiable<String> getAuthor();

   String getComment();

   void setComment(String comment) throws OseeCoreException;

   /**
    * @return TransactionRecord or null of no changes made
    */
   TransactionReadable commit() throws OseeCoreException;

   boolean isCommitInProgress();

   // ARTIFACT

   ArtifactId createArtifact(IArtifactType artifactType, String name) throws OseeCoreException;

   ArtifactId createArtifact(IArtifactType artifactType, String name, String guid) throws OseeCoreException;

   ArtifactId createArtifact(IArtifactType artifactType, String name, String guid, long uuid) throws OseeCoreException;

   ArtifactId createArtifact(IArtifactToken configsFolder);

   void deleteArtifact(ArtifactId sourceArtifact) throws OseeCoreException;

   ArtifactId copyArtifact(ArtifactReadable sourceArtifact) throws OseeCoreException;

   ArtifactId copyArtifact(ArtifactReadable sourceArtifact, Collection<? extends IAttributeType> attributesToDuplicate) throws OseeCoreException;

   ArtifactId copyArtifact(BranchId fromBranch, ArtifactId sourceArtifact) throws OseeCoreException;

   ArtifactId copyArtifact(BranchId fromBranch, ArtifactId sourceArtifact, Collection<? extends IAttributeType> attributesToDuplicate) throws OseeCoreException;

   ArtifactId introduceArtifact(BranchId fromBranch, ArtifactId sourceArtifact) throws OseeCoreException;

   ArtifactId replaceWithVersion(ArtifactReadable sourceArtifact, ArtifactReadable destination) throws OseeCoreException;

   // ATTRIBUTE

   void setName(ArtifactId art, String value) throws OseeCoreException;

   AttributeId createAttribute(ArtifactId art, IAttributeType attributeType) throws OseeCoreException;

   <T> AttributeId createAttribute(ArtifactId art, IAttributeType attributeType, T value) throws OseeCoreException;

   AttributeId createAttributeFromString(ArtifactId art, IAttributeType attributeType, String value) throws OseeCoreException;

   <T> void setSoleAttributeValue(ArtifactId art, IAttributeType attributeType, T value) throws OseeCoreException;

   void setSoleAttributeFromStream(ArtifactId art, IAttributeType attributeType, InputStream stream) throws OseeCoreException;

   void setSoleAttributeFromString(ArtifactId art, IAttributeType attributeType, String value) throws OseeCoreException;

   <T> void setAttributesFromValues(ArtifactId art, IAttributeType attributeType, T... values) throws OseeCoreException;

   <T> void setAttributesFromValues(ArtifactId art, IAttributeType attributeType, Collection<T> values) throws OseeCoreException;

   void setAttributesFromStrings(ArtifactId art, IAttributeType attributeType, String... values) throws OseeCoreException;

   void setAttributesFromStrings(ArtifactId art, IAttributeType attributeType, Collection<String> values) throws OseeCoreException;

   <T> void setAttributeById(ArtifactId art, AttributeId attrId, T value) throws OseeCoreException;

   void setAttributeById(ArtifactId art, AttributeId attrId, String value) throws OseeCoreException;

   void setAttributeById(ArtifactId art, AttributeId attrId, InputStream stream) throws OseeCoreException;

   void deleteByAttributeId(ArtifactId art, AttributeId attrId) throws OseeCoreException;

   void deleteSoleAttribute(ArtifactId art, IAttributeType attributeType) throws OseeCoreException;

   void deleteAttributes(ArtifactId art, IAttributeType attributeType) throws OseeCoreException;

   void deleteAttributesWithValue(ArtifactId art, IAttributeType attributeType, Object value) throws OseeCoreException;

   /// TX

   void addChildren(ArtifactId artA, Iterable<? extends ArtifactId> children) throws OseeCoreException;

   void addChildren(ArtifactId artA, ArtifactId... children) throws OseeCoreException;

   void relate(ArtifactId artA, IRelationType relType, ArtifactId artB) throws OseeCoreException;

   void relate(ArtifactId artA, IRelationType relType, ArtifactId artB, String rationale) throws OseeCoreException;

   void relate(ArtifactId artA, IRelationType relType, ArtifactId artB, IRelationSorterId sortType) throws OseeCoreException;

   void relate(ArtifactId artA, IRelationType relType, ArtifactId artB, String rationale, IRelationSorterId sortType) throws OseeCoreException;

   void setRelations(ArtifactId artA, IRelationType relType, Iterable<? extends ArtifactId> artBs) throws OseeCoreException;

   void setRationale(ArtifactId artA, IRelationType relType, ArtifactId artB, String rationale) throws OseeCoreException;

   void unrelate(ArtifactId artA, IRelationType relType, ArtifactId artB) throws OseeCoreException;

   void unrelateFromAll(ArtifactId art) throws OseeCoreException;

   void unrelateFromAll(IRelationTypeSide typeSide, ArtifactId art) throws OseeCoreException;

   // Tuples
   <E1, E2> Long addTuple2(Tuple2Type<E1, E2> tupleType, Long branchId, E1 e1, E2 e2);

   <E1, E2, E3> Long addTuple3(Tuple3Type<E1, E2, E3> tupleType, Long branchId, E1 e1, E2 e2, E3 e3);

   <E1, E2, E3, E4> Long addTuple4(Tuple4Type<E1, E2, E3, E4> tupleType, Long branchId, E1 e1, E2 e2, E3 e3, E4 e4);

   Long addTuple(TupleTypeId tupleTypeId, Long branchId, Object... elements);

   boolean deleteTuple(Long gammaId);

   <E1, E2> boolean deleteTuple2(Tuple2Type<E1, E2> tupleType, E1 e1, E2 e2);

   <E1, E2, E3> boolean deleteTupple3(Tuple3Type<E1, E2, E3> tupleType, E1 e1, E2 e2, E3 e3);

   <E1, E2, E3, E4> boolean deleteTupple4(Tuple4Type<E1, E2, E3, E4> tupleType, E1 e1, E2 e2, E3 e3, E4 e4);

}
