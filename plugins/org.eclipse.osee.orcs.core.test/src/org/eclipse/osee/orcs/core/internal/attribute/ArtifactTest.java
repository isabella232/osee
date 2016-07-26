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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.MultipleAttributesExist;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.orcs.core.ds.ArtifactData;
import org.eclipse.osee.orcs.core.ds.AttributeData;
import org.eclipse.osee.orcs.core.ds.VersionData;
import org.eclipse.osee.orcs.core.internal.artifact.Artifact;
import org.eclipse.osee.orcs.core.internal.artifact.ArtifactImpl;
import org.eclipse.osee.orcs.core.internal.graph.GraphData;
import org.eclipse.osee.orcs.data.ArtifactTypes;
import org.eclipse.osee.orcs.data.BranchReadable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author John Misinco
 */
public class ArtifactTest {

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   // @formatter:off
   @Mock private Artifact artifact;
   @Mock private ArtifactData artifactData;
   @Mock private AttributeFactory attributeFactory;
   @Mock private ArtifactTypes types;

   @Mock private VersionData version;
   @Mock private AttributeData attributeData;
   @Mock private BranchReadable branch;

   @SuppressWarnings("rawtypes")
   @Mock private Attribute attribute;
   @SuppressWarnings("rawtypes")
   @Mock private Attribute notDeleted;
   @SuppressWarnings("rawtypes")
   @Mock private Attribute deleted;
   @SuppressWarnings("rawtypes")
   @Mock private Attribute differentType;

   @Mock private GraphData graph;
   // @formatter:on

   private final String guid = GUID.create();
   private final IAttributeType attributeType = CoreAttributeTypes.Annotation;
   private final IArtifactType artifactType = CoreArtifactTypes.GeneralData;

   @SuppressWarnings("unchecked")
   @Before
   public void init() throws OseeCoreException {
      MockitoAnnotations.initMocks(this);
      artifact = new ArtifactImpl(types, artifactData, attributeFactory);
      artifact.setGraph(graph);

      when(types.isValidAttributeType(any(IArtifactType.class), any(BranchId.class),
         any(IAttributeType.class))).thenReturn(true);
      when(attributeFactory.getMaxOccurrenceLimit(any(IAttributeType.class))).thenReturn(1);

      when(attributeFactory.createAttribute(any(AttributeManager.class), any(AttributeData.class))).thenReturn(
         attribute);
      when(attributeFactory.createAttributeWithDefaults(any(AttributeManager.class), any(ArtifactData.class),
         any(IAttributeType.class))).thenReturn(attribute);
      when(attribute.getOrcsData()).thenReturn(attributeData);

      when(artifactData.getGuid()).thenReturn(guid);
      when(artifactData.getVersion()).thenReturn(version);
      when(artifactData.getTypeUuid()).thenReturn(artifactType.getGuid());
      when(artifactData.getLocalId()).thenReturn(0);
      when(version.getBranchId()).thenReturn(55L);

      when(deleted.isDeleted()).thenReturn(true);
      when(notDeleted.getOrcsData()).thenReturn(attributeData);
      when(deleted.getOrcsData()).thenReturn(attributeData);
      when(differentType.getOrcsData()).thenReturn(attributeData);

      when(types.getByUuid(CoreArtifactTypes.GeneralData.getGuid())).thenReturn(CoreArtifactTypes.GeneralData);
      when(types.getByUuid(CoreArtifactTypes.CodeUnit.getGuid())).thenReturn(CoreArtifactTypes.CodeUnit);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testAddAndGet() throws OseeCoreException {
      Attribute<Object> attribute = mock(Attribute.class);
      when(attribute.getOrcsData()).thenReturn(attributeData);
      Assert.assertEquals(0, artifact.getAttributes().size());
      artifact.add(CoreAttributeTypes.City, attribute);
      Assert.assertTrue(artifact.getAttributes().contains(attribute));
      Assert.assertEquals(1, artifact.getAttributes().size());
   }

   @Test
   @SuppressWarnings({"rawtypes", "unchecked"})
   public void testAddException() throws OseeCoreException {
      Attribute one = mock(Attribute.class);
      Attribute two = mock(Attribute.class);
      when(one.getOrcsData()).thenReturn(attributeData);
      when(two.getOrcsData()).thenReturn(attributeData);

      when(attributeFactory.getMaxOccurrenceLimit(attributeType)).thenReturn(1);
      artifact.add(attributeType, one);
      artifact.add(attributeType, two);
      Assert.assertEquals(2, artifact.getAttributes(attributeType).size());
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testAreAttributesDirty() {
      Attribute<Object> attribute = mock(Attribute.class);
      when(attribute.getOrcsData()).thenReturn(attributeData);
      artifact.add(CoreAttributeTypes.City, attribute);
      Assert.assertFalse(artifact.areAttributesDirty());
      when(attribute.isDirty()).thenReturn(true);
      Assert.assertTrue(artifact.areAttributesDirty());
   }

   @Test
   public void testCreateAttribute() throws OseeCoreException {
      artifact.createAttribute(CoreAttributeTypes.City);
      verify(attributeFactory).createAttributeWithDefaults(artifact, artifactData, CoreAttributeTypes.City);
   }

   @Test
   public void testGetLocalId() {
      artifact.getLocalId();
      verify(artifactData).getLocalId();
   }

   @Test
   public void testGetGuid() {
      artifact.getGuid();
      verify(artifactData).getGuid();
   }

   @Test
   public void testGetTransactionId() {
      TransactionId expected = TransactionId.valueOf(4321);
      when(graph.getTransaction()).thenReturn(expected);
      assertEquals(expected, artifact.getTransaction());
      verify(graph).getTransaction();
   }

   @Test
   public void testLastModifiedTransaction() {
      TransactionId expected = TransactionId.valueOf(10);
      when(version.getTransactionId()).thenReturn(expected);
      assertEquals(expected, artifact.getLastModifiedTransaction());
      verify(version).getTransactionId();
   }

   @Test
   public void testArtifactType() throws OseeCoreException {
      artifact.getArtifactType();
      verify(types).getByUuid(artifactData.getTypeUuid());
   }

   @Test
   @SuppressWarnings({"rawtypes", "unchecked"})
   public void testSetName() throws OseeCoreException {
      Attribute attr = mock(Attribute.class);
      when(attr.getOrcsData()).thenReturn(attributeData);
      when(attributeFactory.createAttributeWithDefaults(any(AttributeManager.class), any(ArtifactData.class),
         eq(CoreAttributeTypes.Name))).thenReturn(attr);
      artifact.setName("test");
      verify(attr).setFromString("test");
   }

   @Test
   public void testSetArtifactType() throws OseeCoreException {
      when(version.isInStorage()).thenReturn(true);

      artifact.setArtifactType(CoreArtifactTypes.CodeUnit);

      verify(artifactData).setTypeUuid(CoreArtifactTypes.CodeUnit.getGuid());
      verify(artifactData).setModType(ModificationType.MODIFIED);

      reset(version);
      reset(artifactData);

      when(artifactData.getVersion()).thenReturn(version);
      when(artifactData.getGuid()).thenReturn(guid);
      when(artifactData.getVersion()).thenReturn(version);
      when(artifactData.getTypeUuid()).thenReturn(artifactType.getGuid());

      artifact.setArtifactType(CoreArtifactTypes.CodeUnit);
      verify(artifactData, never()).setModType(ModificationType.MODIFIED);
   }

   @Test
   public void testIsOfType() throws OseeCoreException {
      artifact.isOfType(CoreArtifactTypes.CodeUnit);

      verify(types).inheritsFrom(CoreArtifactTypes.GeneralData, CoreArtifactTypes.CodeUnit);
   }

   @Test
   @SuppressWarnings({"rawtypes", "unchecked"})
   public void testIsDirty() throws OseeCoreException {
      Assert.assertFalse(artifact.isDirty());

      // add dirty attribute
      Attribute dirty = mock(Attribute.class);
      when(dirty.getOrcsData()).thenReturn(attributeData);
      when(dirty.isDirty()).thenReturn(true);
      artifact.add(CoreAttributeTypes.Active, dirty);
      Assert.assertTrue(artifact.isDirty());

      // change artifactType
      reset(dirty);
      Assert.assertFalse(artifact.isDirty());
      artifact.setArtifactType(CoreArtifactTypes.CodeUnit);
      Assert.assertTrue(artifact.isDirty());

      // set mod type to replace with version
      artifact.setOrcsData(artifactData);
      Assert.assertFalse(artifact.isDirty());
      when(artifactData.getModType()).thenReturn(ModificationType.REPLACED_WITH_VERSION);
      Assert.assertTrue(artifact.isDirty());
   }

   @Test
   public void testIsDeleted() {
      for (ModificationType modType : ModificationType.values()) {
         reset(artifactData);
         when(artifactData.getModType()).thenReturn(modType);
         Assert.assertEquals(modType.isDeleted(), artifact.isDeleted());
      }
   }

   @Test
   public void testIsAttributeTypeValid() throws OseeCoreException {
      artifact.isAttributeTypeValid(CoreAttributeTypes.Afha);
      verify(types).isValidAttributeType(eq(artifactType), any(), eq(CoreAttributeTypes.Afha));
   }

   @Test
   public void testGetValidAttributeTypes() throws OseeCoreException {
      artifact.getValidAttributeTypes();
      verify(types).getAttributeTypes(eq(artifactType), any());
   }

   @Test
   @SuppressWarnings({"rawtypes", "unchecked"})
   public void testSetAttributesNotDirty() {
      Attribute one = mock(Attribute.class);
      Attribute two = mock(Attribute.class);
      when(one.getOrcsData()).thenReturn(attributeData);
      when(two.getOrcsData()).thenReturn(attributeData);
      artifact.add(CoreAttributeTypes.AccessContextId, one);
      artifact.add(CoreAttributeTypes.AccessContextId, two);
      artifact.setAttributesNotDirty();
      verify(one).clearDirty();
      verify(two).clearDirty();
   }

   @Test
   @SuppressWarnings({"rawtypes", "unchecked"})
   public void testGetName() throws OseeCoreException {
      String name = artifact.getName();
      Assert.assertTrue(name.contains("AttributeDoesNotExist"));

      Attribute attr = mock(Attribute.class);
      when(attr.getOrcsData()).thenReturn(attributeData);
      when(attributeFactory.createAttributeWithDefaults(any(AttributeManager.class), any(ArtifactData.class),
         eq(CoreAttributeTypes.Name))).thenReturn(attr);
      when(attr.getValue()).thenReturn("test");
      artifact.add(CoreAttributeTypes.Name, attr);
      artifact.setName("test");
      name = artifact.getName();
      Assert.assertEquals("test", name);
   }

   @Test
   public void testGetMaximumAttributeTypeAllowed() throws OseeCoreException {
      int expected = 5;

      when(attributeFactory.getMaxOccurrenceLimit(CoreAttributeTypes.AccessContextId)).thenReturn(expected);

      int result = artifact.getMaximumAttributeTypeAllowed(CoreAttributeTypes.AccessContextId);
      Assert.assertEquals(expected, result);

      reset(types);
      result = artifact.getMaximumAttributeTypeAllowed(CoreAttributeTypes.AccessContextId);
      Assert.assertEquals(-1, result);
   }

   @Test
   public void testGetMinimumAttributeTypeAllowed() throws OseeCoreException {
      int expected = 5;

      when(attributeFactory.getMinOccurrenceLimit(CoreAttributeTypes.AccessContextId)).thenReturn(expected);

      int result = artifact.getMinimumAttributeTypeAllowed(CoreAttributeTypes.AccessContextId);
      Assert.assertEquals(expected, result);

      reset(types);
      result = artifact.getMaximumAttributeTypeAllowed(CoreAttributeTypes.AccessContextId);
      Assert.assertEquals(-1, result);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testGetAttributeCount() throws OseeCoreException {
      artifact.add(CoreAttributeTypes.AccessContextId, notDeleted);
      artifact.add(CoreAttributeTypes.AccessContextId, deleted);
      artifact.add(CoreAttributeTypes.Name, differentType);
      int result = artifact.getAttributeCount(CoreAttributeTypes.AccessContextId);
      Assert.assertEquals(1, result);
      result = artifact.getAttributeCount(CoreAttributeTypes.Name);
      Assert.assertEquals(1, result);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testGetAttributes() throws OseeCoreException {
      artifact.add(CoreAttributeTypes.AccessContextId, notDeleted);
      artifact.add(CoreAttributeTypes.AccessContextId, deleted);
      artifact.add(CoreAttributeTypes.Name, differentType);
      List<Attribute<Object>> attributes = artifact.getAttributes();
      Assert.assertTrue(attributes.contains(notDeleted));
      Assert.assertTrue(attributes.contains(differentType));
      Assert.assertFalse(attributes.contains(deleted));

      attributes = artifact.getAttributes(CoreAttributeTypes.AccessContextId);
      Assert.assertEquals(1, attributes.size());
      Assert.assertTrue(attributes.contains(notDeleted));
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testGetAttributeValues() throws OseeCoreException {
      artifact.add(CoreAttributeTypes.AccessContextId, notDeleted);
      artifact.add(CoreAttributeTypes.AccessContextId, deleted);
      when(notDeleted.getValue()).thenReturn("notDeleted");
      when(deleted.getValue()).thenReturn("deleted");
      List<Object> values = artifact.getAttributeValues(CoreAttributeTypes.AccessContextId);
      Assert.assertEquals(1, values.size());
      Assert.assertTrue(values.contains("notDeleted"));
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testGetSoleAttributeAsString() throws OseeCoreException {
      when(notDeleted.getValue()).thenReturn(new Integer(5));
      artifact.add(CoreAttributeTypes.AccessContextId, notDeleted);
      String attribute = artifact.getSoleAttributeAsString(CoreAttributeTypes.AccessContextId);
      Assert.assertEquals("5", attribute);

      attribute = artifact.getSoleAttributeAsString(CoreAttributeTypes.Category, "default");
      Assert.assertEquals("default", attribute);
   }

   @Test
   @SuppressWarnings({"rawtypes", "unchecked"})
   public void testGetSoleAttributeAsStringException() throws OseeCoreException {
      Attribute one = mock(Attribute.class);
      Attribute two = mock(Attribute.class);
      when(one.getOrcsData()).thenReturn(attributeData);
      when(two.getOrcsData()).thenReturn(attributeData);
      artifact.add(CoreAttributeTypes.AccessContextId, one);
      artifact.add(CoreAttributeTypes.AccessContextId, two);
      thrown.expect(MultipleAttributesExist.class);
      artifact.getSoleAttributeAsString(CoreAttributeTypes.AccessContextId);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testDeleteAttributesByArtifact() throws OseeCoreException {
      artifact.add(CoreAttributeTypes.AccessContextId, notDeleted);
      artifact.add(CoreAttributeTypes.AccessContextId, deleted);
      artifact.add(CoreAttributeTypes.Active, differentType);
      artifact.deleteAttributesByArtifact();
      verify(notDeleted).setArtifactDeleted();
      verify(deleted).setArtifactDeleted();
      verify(differentType).setArtifactDeleted();
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testDeleteSoleAttribute() throws OseeCoreException {
      when(attributeFactory.getMinOccurrenceLimit(attributeType)).thenReturn(0);
      when(notDeleted.getAttributeType()).thenReturn(attributeType);
      when(notDeleted.getContainer()).thenReturn(artifact);
      artifact.add(attributeType, notDeleted);
      artifact.deleteSoleAttribute(attributeType);
      verify(notDeleted).delete();
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testDeleteSoleAttributeException() throws OseeCoreException {
      when(attributeFactory.getMinOccurrenceLimit(attributeType)).thenReturn(1);

      when(notDeleted.getAttributeType()).thenReturn(attributeType);
      artifact.add(attributeType, notDeleted);

      thrown.expect(OseeStateException.class);
      artifact.deleteSoleAttribute(attributeType);
   }

   @Test
   @SuppressWarnings({"rawtypes", "unchecked"})
   public void testSetAttributesFromStringsCreateAll() throws OseeCoreException {
      Attribute one = mock(Attribute.class);
      Attribute two = mock(Attribute.class);
      Attribute three = mock(Attribute.class);
      when(one.getOrcsData()).thenReturn(attributeData);
      when(two.getOrcsData()).thenReturn(attributeData);
      when(three.getOrcsData()).thenReturn(attributeData);

      when(attributeFactory.getMaxOccurrenceLimit(attributeType)).thenReturn(3);

      when(attributeFactory.createAttributeWithDefaults(eq(artifact), any(ArtifactData.class),
         eq(attributeType))).thenReturn(one, two, three);
      artifact.setAttributesFromStrings(attributeType, "one", "two", "three");
      verify(one).setFromString("one");
      verify(two).setFromString("two");
      verify(three).setFromString("three");
   }

   @Test
   @SuppressWarnings({"rawtypes", "unchecked"})
   public void testSetAttributesFromStringsCreateOne() throws OseeCoreException {
      Attribute one = mock(Attribute.class);
      Attribute two = mock(Attribute.class);
      when(one.getOrcsData()).thenReturn(attributeData);
      when(two.getOrcsData()).thenReturn(attributeData);

      when(attributeFactory.getMaxOccurrenceLimit(attributeType)).thenReturn(3);

      when(attributeFactory.createAttributeWithDefaults(eq(artifact), any(ArtifactData.class),
         eq(attributeType))).thenReturn(two);
      artifact.add(attributeType, one);
      artifact.setAttributesFromStrings(attributeType, "1", "2");
      verify(one).setFromString("1");
      verify(two).setFromString("2");

      reset(one, two);
      when(one.getValue()).thenReturn("1");
      artifact.setAttributesFromStrings(attributeType, "1", "2");
      verify(one, never()).setFromString("1");
      verify(two).setFromString("2");
   }

}
