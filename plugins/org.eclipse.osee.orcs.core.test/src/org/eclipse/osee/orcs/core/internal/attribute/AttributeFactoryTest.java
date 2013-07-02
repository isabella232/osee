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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.lang.ref.WeakReference;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.cache.AttributeTypeCache;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.orcs.core.ds.ArtifactData;
import org.eclipse.osee.orcs.core.ds.AttributeData;
import org.eclipse.osee.orcs.core.ds.AttributeDataFactory;
import org.eclipse.osee.orcs.core.ds.DataProxy;
import org.eclipse.osee.orcs.core.ds.ResourceNameResolver;
import org.eclipse.osee.orcs.core.ds.VersionData;
import org.eclipse.osee.orcs.core.internal.artifact.AttributeManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test Case for {@link AttributeFactory}
 * 
 * @author John Misinco
 */
public class AttributeFactoryTest {

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   // @formatter:off
   @Mock private AttributeClassResolver classResolver;
   @Mock private AttributeTypeCache cache;
   @Mock private AttributeDataFactory dataFactory;
   
   @Mock private AttributeData attributeData;
   @Mock private VersionData attrVersionData;
   
   @Mock private AttributeType attributeType;
   @Mock private Attribute<Object> attribute;

   @Mock private AttributeManager container;
   @Mock private DataProxy proxy;
   // @formatter:on

   private AttributeFactory factory;
   private long expectedGuid;
   private final IOseeBranch branch = CoreBranches.COMMON;

   @Before
   public void init() throws OseeCoreException {
      MockitoAnnotations.initMocks(this);

      factory = new AttributeFactory(classResolver, cache, dataFactory);

      expectedGuid = CoreAttributeTypes.Name.getGuid();

      when(attributeData.getTypeUuid()).thenReturn(expectedGuid);
      when(cache.getByGuid(expectedGuid)).thenReturn(attributeType);
      when(classResolver.createAttribute(attributeType)).thenReturn(attribute);
      when(attributeData.getDataProxy()).thenReturn(proxy);
   }

   @Test
   public void testCreateAttributeNullType() throws OseeCoreException {
      when(cache.getByGuid(expectedGuid)).thenReturn(null);

      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("attributeType cannot be null - Cannot find attribute type with uuid[" + expectedGuid + "]");
      factory.createAttribute(container, attributeData);
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   @Test
   public void testCreateAttribute() throws OseeCoreException {
      ArgumentCaptor<ResourceNameResolver> resolverCapture = ArgumentCaptor.forClass(ResourceNameResolver.class);
      ArgumentCaptor<WeakReference> refCapture = ArgumentCaptor.forClass(WeakReference.class);

      Attribute<Object> actual = factory.createAttribute(container, attributeData);

      assertTrue(attribute == actual);

      verify(proxy).setResolver(resolverCapture.capture());
      verify(attribute).internalInitialize(eq(cache), refCapture.capture(), eq(attributeData), eq(false), eq(false));
      verify(container).add(attributeType, attribute);
      assertEquals(container, refCapture.getValue().get());

   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   @Test
   public void testCreateAttributeFromArtifactDataAndType() throws OseeCoreException {
      ArtifactData artifactData = mock(ArtifactData.class);
      VersionData artVersionData = mock(VersionData.class);

      when(dataFactory.create(artifactData, attributeType)).thenReturn(attributeData);
      when(attributeData.getVersion()).thenReturn(attrVersionData);
      when(artifactData.getVersion()).thenReturn(artVersionData);
      when(artVersionData.getBranchId()).thenReturn(45);

      ArgumentCaptor<ResourceNameResolver> resolverCapture = ArgumentCaptor.forClass(ResourceNameResolver.class);
      ArgumentCaptor<WeakReference> refCapture = ArgumentCaptor.forClass(WeakReference.class);

      Attribute<Object> actual = factory.createAttributeWithDefaults(container, artifactData, attributeType);

      verify(dataFactory).create(artifactData, attributeType);
      assertTrue(attribute == actual);

      verify(proxy).setResolver(resolverCapture.capture());
      verify(attribute).internalInitialize(eq(cache), refCapture.capture(), eq(attributeData), eq(true), eq(true));
      verify(container).add(attributeType, attribute);
      assertEquals(container, refCapture.getValue().get());

   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   @Test
   public void testCopyAttribute() throws OseeCoreException {
      AttributeData copiedAttributeData = mock(AttributeData.class);

      when(dataFactory.copy(branch, attributeData)).thenReturn(copiedAttributeData);
      when(copiedAttributeData.getTypeUuid()).thenReturn(expectedGuid);
      when(copiedAttributeData.getDataProxy()).thenReturn(proxy);

      ArgumentCaptor<ResourceNameResolver> resolverCapture = ArgumentCaptor.forClass(ResourceNameResolver.class);
      ArgumentCaptor<WeakReference> refCapture = ArgumentCaptor.forClass(WeakReference.class);

      Attribute<Object> actual = factory.copyAttribute(attributeData, branch, container);

      assertTrue(attribute == actual);

      verify(dataFactory).copy(branch, attributeData);

      verify(proxy).setResolver(resolverCapture.capture());
      verify(attribute).internalInitialize(eq(cache), refCapture.capture(), eq(copiedAttributeData), eq(true),
         eq(false));
      verify(container).add(attributeType, attribute);
      assertEquals(container, refCapture.getValue().get());
   }

   @Test
   public void testIntroduceAttributeNotInStorage() throws OseeCoreException {
      when(attributeData.getVersion()).thenReturn(attrVersionData);
      when(attrVersionData.isInStorage()).thenReturn(false);

      Attribute<Object> actual = factory.introduceAttribute(attributeData, branch, container);
      assertNull(actual);
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   @Test
   public void testIntroduceAttribute() throws OseeCoreException {
      AttributeData introducedAttributeData = mock(AttributeData.class);

      when(attributeData.getVersion()).thenReturn(attrVersionData);
      when(attrVersionData.isInStorage()).thenReturn(true);

      when(dataFactory.introduce(branch, attributeData)).thenReturn(introducedAttributeData);
      when(introducedAttributeData.getTypeUuid()).thenReturn(expectedGuid);
      when(introducedAttributeData.getDataProxy()).thenReturn(proxy);

      ArgumentCaptor<ResourceNameResolver> resolverCapture = ArgumentCaptor.forClass(ResourceNameResolver.class);
      ArgumentCaptor<WeakReference> refCapture = ArgumentCaptor.forClass(WeakReference.class);

      Attribute<Object> actual = factory.introduceAttribute(attributeData, branch, container);
      assertNotNull(actual);

      verify(dataFactory).introduce(branch, attributeData);

      verify(proxy).setResolver(resolverCapture.capture());
      verify(attribute).internalInitialize(eq(cache), refCapture.capture(), eq(introducedAttributeData), eq(true),
         eq(false));
      verify(container).add(attributeType, attribute);
      assertEquals(container, refCapture.getValue().get());
   }

   @Test
   public void testGetMaxOccurrenceLimit() throws OseeCoreException {
      IAttributeType token = mock(IAttributeType.class);

      when(cache.get(token)).thenReturn(attributeType);
      when(attributeType.getMaxOccurrences()).thenReturn(56);

      int actual = factory.getMaxOccurrenceLimit(token);

      assertEquals(56, actual);

      verify(attributeType).getMaxOccurrences();
   }

   @Test
   public void testGetMinOccurrenceLimit() throws OseeCoreException {
      IAttributeType token = mock(IAttributeType.class);

      when(cache.get(token)).thenReturn(attributeType);
      when(attributeType.getMinOccurrences()).thenReturn(99);

      int actual = factory.getMinOccurrenceLimit(token);

      assertEquals(99, actual);

      verify(attributeType).getMinOccurrences();
   }
}
