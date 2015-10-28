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
package org.eclipse.osee.orcs.core.internal.transaction;

import static org.eclipse.osee.framework.core.enums.CoreArtifactTypes.HardwareRequirement;
import static org.eclipse.osee.framework.core.enums.CoreArtifactTypes.SoftwareRequirement;
import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.Active;
import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.Annotation;
import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.Company;
import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.FavoriteBranch;
import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.Name;
import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.PlainTextContent;
import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.QualificationMethod;
import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.RelationOrder;
import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.WordTemplateContent;
import static org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes.LEXICOGRAPHICAL_ASC;
import static org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes.LEXICOGRAPHICAL_DESC;
import static org.eclipse.osee.framework.core.enums.RelationSide.SIDE_B;
import static org.eclipse.osee.orcs.core.internal.relation.RelationUtil.DEFAULT_HIERARCHY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.executor.admin.CancellableCallable;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.framework.jdk.core.type.ResultSets;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.internal.artifact.Artifact;
import org.eclipse.osee.orcs.core.internal.attribute.Attribute;
import org.eclipse.osee.orcs.core.internal.relation.RelationUtil;
import org.eclipse.osee.orcs.core.internal.search.QueryModule;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.AttributeReadable;
import org.eclipse.osee.orcs.data.TransactionReadable;
import org.eclipse.osee.orcs.search.QueryBuilder;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * Test Case for {@link TransactionFactoryImpl}
 *
 * @author Roberto E. Escobar
 */
public class TransactionBuilderImplTest {

   private static final IRelationType TYPE_1 = TokenFactory.createRelationType(123456789L, "TYPE_1");
   @Rule
   public ExpectedException thrown = ExpectedException.none();

   // @formatter:off
   @Mock private Log logger;
   @Mock private OrcsSession session;
   @Mock private TxDataManager txDataManager;
   @Mock private TxCallableFactory txCallableFactory;
   @Mock private QueryFactory queryFactory;
   @Mock private QueryBuilder builder;
   @Mock private QueryModule query;

   @Mock private IOseeBranch branch;

   @Mock private ArtifactReadable expectedAuthor;
   @Mock private ArtifactReadable expectedDestination;
   @Mock private ArtifactReadable node1;
   @Mock private ArtifactReadable node2;
   @Mock private Artifact artifact;
   @Mock private Artifact artifact2;
   @SuppressWarnings("rawtypes")
   @Mock private AttributeReadable attrId;
   @SuppressWarnings("rawtypes")
   @Mock private Attribute attribute;

   @Mock private TxData txData;
   // @formatter:on

   private final IOseeBranch expectedBranch = CoreBranches.COMMON;
   private final Long BRANCH_ID = expectedBranch.getUuid();
   private TransactionBuilderImpl factory;
   private String guid;

   @SuppressWarnings("unchecked")
   @Before
   public void init() throws OseeCoreException {
      initMocks(this);
      guid = GUID.create();
      factory = new TransactionBuilderImpl(txCallableFactory, txDataManager, txData, query);

      when(attrId.getLocalId()).thenReturn(12345);
      when(txDataManager.getForWrite(txData, expectedAuthor)).thenReturn(artifact);
      when(artifact.getAttributeById(attrId.getLocalId())).thenReturn(attribute);
      when(query.createQueryFactory(session)).thenReturn(queryFactory);
   }

   @Test
   public void testGetAuthor() {
      when(factory.getAuthor()).thenReturn(expectedAuthor);

      ArtifactReadable author = factory.getAuthor();

      assertEquals(expectedAuthor, author);
      verify(txData).getAuthor();
   }

   @Test
   public void testGetComment() {
      when(factory.getComment()).thenReturn("This is a comment");

      String comment = factory.getComment();

      assertEquals(comment, "This is a comment");
      verify(txData).getComment();
   }

   public void testSetAuthor() throws OseeCoreException {
      factory.setAuthor(expectedAuthor);

      verify(txDataManager).setAuthor(txData, expectedAuthor);
   }

   @Test
   public void testCreateArtifact() throws OseeCoreException {
      factory.createArtifact(SoftwareRequirement, "Software Requirement");

      verify(txDataManager).createArtifact(txData, SoftwareRequirement, "Software Requirement", null);
   }

   @Test
   public void testCreateArtifactWithGuid() throws OseeCoreException {
      factory.createArtifact(HardwareRequirement, "Hardware Requirement", guid);

      verify(txDataManager).createArtifact(txData, HardwareRequirement, "Hardware Requirement", guid);
   }

   @Test
   public void testCopyArtifact() throws OseeCoreException {
      when(expectedAuthor.getBranchId()).thenReturn(BRANCH_ID);

      factory.copyArtifact(expectedAuthor);

      verify(txDataManager).copyArtifact(txData, BRANCH_ID, expectedAuthor);
   }

   @Test
   public void testCopyArtifactWithList() throws OseeCoreException {
      Collection<? extends IAttributeType> attributesToDuplicate = Arrays.asList(Name, Annotation);
      when(expectedAuthor.getBranchId()).thenReturn(BRANCH_ID);

      factory.copyArtifact(expectedAuthor, attributesToDuplicate);

      verify(txDataManager).copyArtifact(txData, BRANCH_ID, expectedAuthor, attributesToDuplicate);
   }

   @Test
   public void testIntroduceArtifactBranchException() throws OseeCoreException {
      when(expectedAuthor.getBranchId()).thenReturn(BRANCH_ID);
      when(txData.getBranchId()).thenReturn(BRANCH_ID);
      when(txData.isOnBranch(BRANCH_ID)).thenReturn(true);

      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("Source branch is same branch as transaction branch[" + BRANCH_ID + "]");
      factory.introduceArtifact(expectedBranch, expectedAuthor);
   }

   @Test
   public void testIntroduceArtifact() throws OseeCoreException {
      when(query.createQueryFactory(null)).thenReturn(queryFactory);
      when(queryFactory.fromBranch(any(Long.class))).thenReturn(builder);

      when(queryFactory.fromBranch(branch)).thenReturn(builder);
      when(builder.includeDeletedArtifacts()).thenReturn(builder);
      when(builder.andGuid(anyString())).thenReturn(builder);

      ResultSet<ArtifactReadable> source = ResultSets.singleton(expectedAuthor);
      when(builder.getResults()).thenReturn(source);

      factory.introduceArtifact(expectedBranch, expectedAuthor);

      verify(txDataManager).introduceArtifact(txData, BRANCH_ID, expectedAuthor, expectedAuthor);
   }

   @Test
   public void testCreateAttribute() throws OseeCoreException {
      factory.createAttribute(expectedAuthor, QualificationMethod);

      verify(artifact).createAttribute(QualificationMethod);
   }

   @Test
   public void testCreateAttributeWithValue() throws OseeCoreException {
      factory.createAttribute(expectedAuthor, QualificationMethod, "Demonstration");

      verify(txDataManager).getForWrite(txData, expectedAuthor);
      verify(artifact).createAttribute(QualificationMethod, "Demonstration");
   }

   @Test
   public void testCreateAttributeFromString() throws OseeCoreException {
      factory.createAttributeFromString(expectedAuthor, WordTemplateContent, "This is my word template content");

      verify(txDataManager).getForWrite(txData, expectedAuthor);
      verify(artifact).createAttributeFromString(CoreAttributeTypes.WordTemplateContent,
         "This is my word template content");
   }

   @Test
   public void testSetSoleAttributeValue() throws OseeCoreException {
      factory.setSoleAttributeValue(expectedAuthor, RelationOrder, LEXICOGRAPHICAL_DESC);

      verify(txDataManager).getForWrite(txData, expectedAuthor);
      verify(artifact).setSoleAttributeValue(RelationOrder, LEXICOGRAPHICAL_DESC);
   }

   @Test
   public void testSetSoleAttributeFromStream() throws OseeCoreException {
      InputStream inputStream = Mockito.mock(InputStream.class);

      factory.setSoleAttributeFromStream(expectedAuthor, Company, inputStream);

      verify(txDataManager).getForWrite(txData, expectedAuthor);
      verify(artifact).setSoleAttributeFromStream(Company, inputStream);
   }

   @Test
   public void testSetSoleAttributeFromString() throws OseeCoreException {
      factory.setSoleAttributeFromString(expectedAuthor, Name, "Name");

      verify(txDataManager).getForWrite(txData, expectedAuthor);
      verify(artifact).setSoleAttributeFromString(Name, "Name");
   }

   @Test
   public void testSetAttributesFromValues() throws OseeCoreException {
      factory.setAttributesFromValues(expectedAuthor, PlainTextContent, Arrays.asList(true, true, false));

      verify(txDataManager).getForWrite(txData, expectedAuthor);
      verify(artifact).setAttributesFromValues(PlainTextContent, Arrays.asList(true, true, false));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testSetAttributesFromValuesList() throws OseeCoreException {
      factory.setAttributesFromValues(expectedAuthor, CoreAttributeTypes.StaticId, Collections.EMPTY_LIST);

      verify(txDataManager).getForWrite(txData, expectedAuthor);
      verify(artifact).setAttributesFromValues(CoreAttributeTypes.StaticId, Collections.EMPTY_LIST);
   }

   @Test
   public void testSetAttributesFromStrings() throws OseeCoreException {
      factory.setAttributesFromStrings(expectedAuthor, PlainTextContent, Arrays.asList("one", "two", "three"));

      verify(txDataManager).getForWrite(txData, expectedAuthor);
      verify(artifact).setAttributesFromStrings(PlainTextContent, Arrays.asList("one", "two", "three"));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testSetAttributesFromStringList() throws OseeCoreException {
      factory.setAttributesFromStrings(expectedAuthor, PlainTextContent, Collections.EMPTY_LIST);

      verify(txDataManager).getForWrite(txData, expectedAuthor);
      verify(artifact).setAttributesFromStrings(PlainTextContent, Collections.EMPTY_LIST);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testSetAttributeByIdFromValue() throws OseeCoreException {
      factory.setAttributeById(expectedAuthor, attrId, false);

      verify(txDataManager).getForWrite(txData, expectedAuthor);
      verify(attribute).setValue(false);
   }

   @Test
   public void testSetAttributeByIdFromString() throws OseeCoreException {
      factory.setAttributeById(expectedAuthor, attrId, "value");

      verify(txDataManager).getForWrite(txData, expectedAuthor);
      verify(attribute).setFromString("value");
   }

   @Test
   public void testSetAttributeById() throws OseeCoreException {
      InputStream inputStream = Mockito.mock(InputStream.class);

      factory.setAttributeById(expectedAuthor, attrId, inputStream);

      verify(txDataManager).getForWrite(txData, expectedAuthor);
      verify(attribute).setValueFromInputStream(inputStream);
   }

   @Test
   public void testDeleteByAttributeId() throws OseeCoreException {
      factory.deleteByAttributeId(expectedAuthor, attrId);

      verify(txDataManager).getForWrite(txData, expectedAuthor);
      verify(attribute).delete();
   }

   @Test
   public void testDeleteSoleAttribute() throws OseeCoreException {
      factory.deleteSoleAttribute(expectedAuthor, Name);

      verify(txDataManager).getForWrite(txData, expectedAuthor);
      verify(artifact).deleteSoleAttribute(Name);
   }

   @Test
   public void testDeleteAttributes() throws OseeCoreException {
      factory.deleteAttributes(expectedAuthor, FavoriteBranch);

      verify(txDataManager).getForWrite(txData, expectedAuthor);
      verify(artifact).deleteAttributes(FavoriteBranch);
   }

   @Test
   public void testDeleteAttributesWithValue() throws OseeCoreException {
      factory.deleteAttributesWithValue(expectedAuthor, Active, true);

      verify(txDataManager).getForWrite(txData, expectedAuthor);
      verify(artifact).deleteAttributesWithValue(Active, true);
   }

   @Test
   public void testDeleteArtifact() throws OseeCoreException {
      factory.deleteArtifact(expectedAuthor);

      verify(txDataManager).deleteArtifact(txData, expectedAuthor);
   }

   @Test
   public void testIsCommitInProgress() {
      when(factory.isCommitInProgress()).thenReturn(false);

      boolean condition = factory.isCommitInProgress();

      assertFalse(condition);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testCommit() throws Exception {
      CancellableCallable<TransactionReadable> callable = mock(CancellableCallable.class);
      TransactionReadable tx = mock(TransactionReadable.class);

      when(txCallableFactory.createTx(txData)).thenReturn(callable);
      when(callable.call()).thenReturn(tx);

      factory.commit();
      verify(txCallableFactory).createTx(txData);
   }

   @Test
   public void testCommitException() throws OseeCoreException {
      Exception exception = new IllegalStateException("onCommit Exception");

      doThrow(exception).when(txCallableFactory).createTx(txData);

      thrown.expect(Exception.class);
      factory.commit();
   }

   @Test
   public void testSetRationale() throws OseeCoreException {
      factory.setRationale(node1, DEFAULT_HIERARCHY, node2, "This is my rationale");

      verify(txDataManager).setRationale(txData, node1, DEFAULT_HIERARCHY, node2, "This is my rationale");
   }

   @Test
   public void testAddChildren() throws OseeCoreException {
      List<ArtifactReadable> list = Arrays.asList(node1, node2);

      factory.addChildren(expectedAuthor, list);

      verify(txDataManager).addChildren(txData, expectedAuthor, list);
   }

   @Test
   public void TestAddChildrenAsList() throws OseeCoreException {
      Iterable<? extends ArtifactId> children = Collections.emptyList();
      factory.addChildren(expectedAuthor, children);
      verify(txDataManager).addChildren(txData, expectedAuthor, children);
   }

   @Test
   public void testRelate() throws OseeCoreException {
      factory.relate(node1, DEFAULT_HIERARCHY, node2);
      verify(txDataManager).relate(txData, node1, DEFAULT_HIERARCHY, node2);
   }

   @Test
   public void testRelateWithOrder() throws OseeCoreException {
      factory.relate(node1, DEFAULT_HIERARCHY, node2, LEXICOGRAPHICAL_ASC);
      verify(txDataManager).relate(txData, node1, DEFAULT_HIERARCHY, node2, LEXICOGRAPHICAL_ASC);
   }

   @Test
   public void testSetRelations() throws OseeCoreException {
      Iterable<? extends ArtifactId> artBs = Collections.emptyList();
      factory.setRelations(node1, DEFAULT_HIERARCHY, artBs);

      verify(txDataManager).setRelations(txData, node1, DEFAULT_HIERARCHY, artBs);
   }

   @Test
   public void testUnrelateWithAandB() throws OseeCoreException {
      factory.unrelate(node1, TYPE_1, node2);
      verify(txDataManager).unrelate(txData, node1, TYPE_1, node2);
   }

   @Test
   public void testUnrelateFromAllWithSide() throws OseeCoreException {
      IRelationTypeSide asTypeSide = RelationUtil.asTypeSide(TYPE_1, SIDE_B);
      factory.unrelateFromAll(asTypeSide, expectedAuthor);
      verify(txDataManager).unrelateFromAll(txData, TYPE_1, expectedAuthor, SIDE_B);
   }

   @Test
   public void testUnrelateFromAll() throws OseeCoreException {
      factory.unrelateFromAll(expectedAuthor);
      verify(txDataManager).unrelateFromAll(txData, expectedAuthor);
   }

}
