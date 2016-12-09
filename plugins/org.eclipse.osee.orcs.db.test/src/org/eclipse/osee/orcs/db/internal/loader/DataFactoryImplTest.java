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
package org.eclipse.osee.orcs.db.internal.loader;

import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.HasLocalId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.RelationalConstants;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.orcs.core.ds.ArtifactData;
import org.eclipse.osee.orcs.core.ds.AttributeData;
import org.eclipse.osee.orcs.core.ds.DataFactory;
import org.eclipse.osee.orcs.core.ds.DataProxy;
import org.eclipse.osee.orcs.core.ds.RelationData;
import org.eclipse.osee.orcs.core.ds.VersionData;
import org.eclipse.osee.orcs.data.ArtifactTypes;
import org.eclipse.osee.orcs.db.internal.IdentityLocator;
import org.eclipse.osee.orcs.db.internal.IdentityManager;
import org.eclipse.osee.orcs.db.internal.OrcsObjectFactory;
import org.eclipse.osee.orcs.db.internal.loader.data.OrcsObjectFactoryImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test Case for {@link DataFactoryImplTest} and {@link OrcsObjectFactoryImpl}
 *
 * @author Roberto E. Escobar
 */
public class DataFactoryImplTest {
   private static final BranchId BRANCH = BranchId.valueOf(11);
   private static final TransactionId tx333 = TransactionId.valueOf(333);
   private static final TransactionId tx444 = TransactionId.valueOf(444);

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   //@formatter:off
   @Mock private IdentityManager idFactory;
   @Mock private ProxyDataFactory proxyFactory;
   @Mock private IdentityLocator identityService;
   @Mock private ArtifactTypes artifactCache;

   @Mock private ArtifactData artData;
   @Mock private AttributeData attrData;
   @Mock private RelationData relData;
   @Mock private VersionData verData;
   @Mock private DataProxy dataProxy;
   @Mock private DataProxy otherDataProxy;

   @Mock private IArtifactType artifactTypeToken;
   //@formatter:on

   private DataFactory dataFactory;
   private Object[] expectedProxyData;
   private String guid;

   @Before
   public void setUp() throws OseeCoreException {
      MockitoAnnotations.initMocks(this);

      guid = GUID.create();

      OrcsObjectFactory objectFactory = new OrcsObjectFactoryImpl(proxyFactory);
      dataFactory = new DataFactoryImpl(idFactory, objectFactory, artifactCache);

      // VERSION
      when(verData.getBranch()).thenReturn(BRANCH);
      when(verData.getGammaId()).thenReturn(222L);
      when(verData.getTransactionId()).thenReturn(tx333);
      when(verData.getStripeId()).thenReturn(tx444);
      when(verData.isHistorical()).thenReturn(true);

      // ARTIFACT
      when(artData.getVersion()).thenReturn(verData);
      when(artData.getLocalId()).thenReturn(555);
      when(artData.getModType()).thenReturn(ModificationType.MODIFIED);
      when(artData.getTypeUuid()).thenReturn(666L);
      when(artData.getBaseModType()).thenReturn(ModificationType.NEW);
      when(artData.getBaseTypeUuid()).thenReturn(777L);
      when(artData.getGuid()).thenReturn("abcdefg");

      // ATTRIBUTE
      when(attrData.getVersion()).thenReturn(verData);
      when(attrData.getLocalId()).thenReturn(555);
      when(attrData.getModType()).thenReturn(ModificationType.MODIFIED);
      when(attrData.getTypeUuid()).thenReturn(666L);
      when(attrData.getBaseModType()).thenReturn(ModificationType.NEW);
      when(attrData.getBaseTypeUuid()).thenReturn(777L);
      when(attrData.getArtifactId()).thenReturn(88);
      when(attrData.getDataProxy()).thenReturn(dataProxy);

      expectedProxyData = new Object[] {45, "hello", "hello"};
      when(dataProxy.getData()).thenReturn(expectedProxyData);
      when(proxyFactory.createProxy(666L, expectedProxyData)).thenReturn(otherDataProxy);
      when(otherDataProxy.getData()).thenReturn(new Object[] {45, "hello", "hello"});

      // RELATION
      when(relData.getVersion()).thenReturn(verData);
      when(relData.getLocalId()).thenReturn(555);
      when(relData.getModType()).thenReturn(ModificationType.MODIFIED);
      when(relData.getTypeUuid()).thenReturn(666L);
      when(relData.getBaseModType()).thenReturn(ModificationType.NEW);
      when(relData.getBaseTypeUuid()).thenReturn(777L);
      when(relData.getArtIdA()).thenReturn(88);
      when(relData.getArtIdB()).thenReturn(99);
      when(relData.getRationale()).thenReturn("this is the rationale");
   }

   @Test
   public void testCreateArtifactDataUsingAbstratArtifactType() throws OseeCoreException {
      when(artifactTypeToken.toString()).thenReturn("artifactTypeToken");
      when(artifactCache.get(artifactTypeToken)).thenReturn(artifactTypeToken);
      when(artifactCache.isAbstract(artifactTypeToken)).thenReturn(true);

      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("Cannot create an instance of abstract type [artifactTypeToken]");
      dataFactory.create(COMMON, artifactTypeToken, guid);
   }

   @Test
   public void testCreateArtifactDataInvalidGuid() throws OseeCoreException {
      when(artifactCache.get(artifactTypeToken)).thenReturn(artifactTypeToken);
      when(artifactCache.isAbstract(artifactTypeToken)).thenReturn(false);
      when(artifactTypeToken.toString()).thenReturn("artifactTypeToken");

      when(idFactory.getUniqueGuid(guid)).thenReturn("123");

      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("Invalid guid [123] during artifact creation [type: artifactTypeToken]");

      dataFactory.create(COMMON, artifactTypeToken, guid);
   }

   @Test
   public void testCreateArtifactData() throws OseeCoreException {
      when(artifactCache.isAbstract(artifactTypeToken)).thenReturn(false);
      when(artifactTypeToken.getGuid()).thenReturn(4536L);
      when(idFactory.getUniqueGuid(guid)).thenReturn(guid);
      when(idFactory.getNextArtifactId()).thenReturn(987);

      ArtifactData actual = dataFactory.create(COMMON, artifactTypeToken, guid);
      verify(idFactory).getUniqueGuid(guid);
      verify(idFactory).getNextArtifactId();

      VersionData actualVer = actual.getVersion();

      assertEquals(COMMON, actualVer.getBranch());
      assertEquals(RelationalConstants.GAMMA_SENTINEL, actualVer.getGammaId());
      assertEquals(TransactionId.SENTINEL, actualVer.getTransactionId());
      assertEquals(TransactionId.SENTINEL, actualVer.getStripeId());
      assertEquals(false, actualVer.isHistorical());
      assertEquals(false, actualVer.isInStorage());

      assertEquals(987, actual.getLocalId().intValue());
      assertEquals(RelationalConstants.DEFAULT_MODIFICATION_TYPE, actual.getModType());
      assertEquals(4536L, actual.getTypeUuid());
      assertEquals(RelationalConstants.DEFAULT_MODIFICATION_TYPE, actual.getBaseModType());
      assertEquals(4536L, actual.getBaseTypeUuid());
      assertEquals(guid, actual.getGuid());
   }

   @Test
   public void testCreateArtifactDataGenerateGuid() throws OseeCoreException {
      when(artifactCache.get(artifactTypeToken)).thenReturn(artifactTypeToken);
      when(artifactTypeToken.getGuid()).thenReturn(4536L);
      when(artifactCache.isAbstract(artifactTypeToken)).thenReturn(false);
      when(idFactory.getUniqueGuid(guid)).thenReturn(guid);
      when(idFactory.getNextArtifactId()).thenReturn(987);

      ArtifactData actual = dataFactory.create(COMMON, artifactTypeToken, guid);
      verify(idFactory).getUniqueGuid(guid);
      verify(idFactory).getNextArtifactId();

      VersionData actualVer = actual.getVersion();
      assertEquals(COMMON, actualVer.getBranch());
      assertEquals(RelationalConstants.GAMMA_SENTINEL, actualVer.getGammaId());
      assertEquals(TransactionId.SENTINEL, actualVer.getTransactionId());
      assertEquals(TransactionId.SENTINEL, actualVer.getStripeId());
      assertEquals(false, actualVer.isHistorical());
      assertEquals(false, actualVer.isInStorage());

      assertEquals(987, actual.getLocalId().intValue());
      assertEquals(RelationalConstants.DEFAULT_MODIFICATION_TYPE, actual.getModType());
      assertEquals(4536L, actual.getTypeUuid());
      assertEquals(RelationalConstants.DEFAULT_MODIFICATION_TYPE, actual.getBaseModType());
      assertEquals(4536L, actual.getBaseTypeUuid());
      assertEquals(guid, actual.getGuid());
   }

   @Test
   public void testCreateAttributeData() throws OseeCoreException {
      IAttributeType attributeType = mock(IAttributeType.class);

      when(attributeType.getId()).thenReturn(2389L);
      when(proxyFactory.createProxy(2389L, "", "")).thenReturn(otherDataProxy);
      when(otherDataProxy.getData()).thenReturn(new Object[] {2389L, "", ""});

      AttributeData actual = dataFactory.create(artData, attributeType);

      VersionData actualVer = actual.getVersion();
      assertEquals(BRANCH, actualVer.getBranch());
      assertEquals(RelationalConstants.GAMMA_SENTINEL, actualVer.getGammaId());
      assertEquals(TransactionId.SENTINEL, actualVer.getTransactionId());
      assertEquals(TransactionId.SENTINEL, actualVer.getStripeId());
      assertEquals(false, actualVer.isHistorical());
      assertEquals(false, actualVer.isInStorage());

      assertEquals(RelationalConstants.DEFAULT_ITEM_ID, actual.getLocalId());
      assertEquals(RelationalConstants.DEFAULT_MODIFICATION_TYPE, actual.getModType());
      assertEquals(2389L, actual.getTypeUuid());
      assertEquals(RelationalConstants.DEFAULT_MODIFICATION_TYPE, actual.getBaseModType());
      assertEquals(2389L, actual.getBaseTypeUuid());

      assertEquals(555, actual.getArtifactId());
      assertNotSame(dataProxy, actual.getDataProxy());

      Object[] objData = actual.getDataProxy().getData();
      assertEquals(2389L, objData[0]);
      assertEquals("", objData[1]);
      assertEquals("", objData[2]);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testCreateRelationData() throws OseeCoreException {
      IRelationType relationType = mock(IRelationType.class);
      HasLocalId<Integer> localId1 = mock(HasLocalId.class);
      HasLocalId<Integer> localId2 = mock(HasLocalId.class);

      when(relationType.getId()).thenReturn(2389L);
      when(localId1.getLocalId()).thenReturn(4562);
      when(localId2.getLocalId()).thenReturn(9513);

      RelationData actual = dataFactory.createRelationData(relationType, COMMON, localId1, localId2, "My rationale");

      VersionData actualVer = actual.getVersion();
      assertEquals(COMMON, actualVer.getBranch());
      assertEquals(RelationalConstants.GAMMA_SENTINEL, actualVer.getGammaId());
      assertEquals(TransactionId.SENTINEL, actualVer.getTransactionId());
      assertEquals(TransactionId.SENTINEL, actualVer.getStripeId());
      assertEquals(false, actualVer.isHistorical());
      assertEquals(false, actualVer.isInStorage());

      assertEquals(RelationalConstants.DEFAULT_ITEM_ID, actual.getLocalId());
      assertEquals(RelationalConstants.DEFAULT_MODIFICATION_TYPE, actual.getModType());
      assertEquals(2389L, actual.getTypeUuid());
      assertEquals(RelationalConstants.DEFAULT_MODIFICATION_TYPE, actual.getBaseModType());
      assertEquals(2389L, actual.getBaseTypeUuid());

      assertEquals(4562, actual.getArtIdA());
      assertEquals(9513, actual.getArtIdB());
      assertEquals("My rationale", actual.getRationale());
   }

   @Test
   public void testIntroduceArtifactData() throws OseeCoreException {
      ArtifactData actual = dataFactory.introduce(COMMON, artData);

      VersionData actualVer = actual.getVersion();
      assertNotSame(verData, actualVer);
      assertEquals(COMMON, actualVer.getBranch());
      assertEquals(222L, actualVer.getGammaId());
      assertEquals(TransactionId.SENTINEL, actualVer.getTransactionId());
      assertEquals(artData.getVersion().getStripeId(), actualVer.getStripeId());
      assertEquals(false, actualVer.isHistorical());
      assertEquals(false, actualVer.isInStorage());

      assertEquals(555, actual.getLocalId().intValue());
      assertEquals(artData.getModType(), actual.getModType());
      assertEquals(666L, actual.getTypeUuid());
      assertEquals(ModificationType.NEW, actual.getBaseModType());
      assertEquals(777L, actual.getBaseTypeUuid());
      assertEquals("abcdefg", actual.getGuid());
   }

   @Test
   public void testIntroduceAttributeData() throws OseeCoreException {
      AttributeData actual = dataFactory.introduce(COMMON, attrData);

      VersionData actualVer = actual.getVersion();
      assertNotSame(verData, actualVer);
      assertEquals(COMMON, actualVer.getBranch());
      assertEquals(222L, actualVer.getGammaId());
      assertEquals(TransactionId.SENTINEL, actualVer.getTransactionId());
      assertEquals(attrData.getVersion().getStripeId(), actualVer.getStripeId());
      assertEquals(false, actualVer.isHistorical());
      assertEquals(false, actualVer.isInStorage());

      assertEquals(555, actual.getLocalId().intValue());
      assertEquals(attrData.getModType(), actual.getModType());
      assertEquals(666L, actual.getTypeUuid());
      assertEquals(ModificationType.NEW, actual.getBaseModType());
      assertEquals(777L, actual.getBaseTypeUuid());

      assertEquals(88, actual.getArtifactId());
      assertNotSame(dataProxy, actual.getDataProxy());

      Object[] objData = actual.getDataProxy().getData();
      assertNotSame(expectedProxyData, objData);
      assertEquals(expectedProxyData[0], objData[0]);
      assertEquals(expectedProxyData[1], objData[1]);
      assertEquals(expectedProxyData[2], objData[2]);
   }

   @Test
   public void testCopyArtifactData() throws OseeCoreException {
      String newGuid = GUID.create();
      when(idFactory.getNextArtifactId()).thenReturn(987);
      when(idFactory.getUniqueGuid(null)).thenReturn(newGuid);

      ArtifactData actual = dataFactory.copy(COMMON, artData);
      verify(idFactory).getUniqueGuid(null);

      VersionData actualVer = actual.getVersion();
      assertNotSame(verData, actualVer);
      assertEquals(COMMON, actualVer.getBranch());
      assertEquals(RelationalConstants.GAMMA_SENTINEL, actualVer.getGammaId());
      assertEquals(TransactionId.SENTINEL, actualVer.getTransactionId());
      assertEquals(TransactionId.SENTINEL, actualVer.getStripeId());
      assertEquals(false, actualVer.isHistorical());
      assertEquals(false, actualVer.isInStorage());

      assertEquals(987, actual.getLocalId().intValue());
      assertEquals(ModificationType.NEW, actual.getModType());
      assertEquals(666L, actual.getTypeUuid());
      assertEquals(ModificationType.NEW, actual.getBaseModType());
      assertEquals(777L, actual.getBaseTypeUuid());
      assertEquals(newGuid, actual.getGuid());
   }

   @Test
   public void testCopyAttributeData() throws OseeCoreException {
      AttributeData actual = dataFactory.copy(COMMON, attrData);

      VersionData actualVer = actual.getVersion();
      assertNotSame(verData, actualVer);
      assertEquals(COMMON, actualVer.getBranch());
      assertEquals(RelationalConstants.GAMMA_SENTINEL, actualVer.getGammaId());
      assertEquals(TransactionId.SENTINEL, actualVer.getTransactionId());
      assertEquals(TransactionId.SENTINEL, actualVer.getStripeId());
      assertEquals(false, actualVer.isHistorical());
      assertEquals(false, actualVer.isInStorage());

      assertEquals(RelationalConstants.DEFAULT_ITEM_ID, actual.getLocalId());
      assertEquals(ModificationType.NEW, actual.getModType());
      assertEquals(666L, actual.getTypeUuid());
      assertEquals(ModificationType.NEW, actual.getBaseModType());
      assertEquals(777L, actual.getBaseTypeUuid());

      assertEquals(88, actual.getArtifactId());
      assertNotSame(dataProxy, actual.getDataProxy());

      Object[] objData = actual.getDataProxy().getData();
      assertNotSame(expectedProxyData, objData);
      assertEquals(expectedProxyData[0], objData[0]);
      assertEquals(expectedProxyData[1], objData[1]);
      assertEquals(expectedProxyData[2], objData[2]);
   }

   @Test
   public void testCloneArtifactData() throws OseeCoreException {
      ArtifactData actual = dataFactory.clone(artData);
      VersionData actualVer = actual.getVersion();

      assertNotSame(artData, actual);
      assertNotSame(verData, actualVer);

      assertEquals(BRANCH, actualVer.getBranch());
      assertEquals(222L, actualVer.getGammaId());
      assertEquals(tx333, actualVer.getTransactionId());
      assertEquals(tx444, actualVer.getStripeId());
      assertEquals(true, actualVer.isHistorical());
      assertEquals(true, actualVer.isInStorage());

      assertEquals(555, actual.getLocalId().intValue());
      assertEquals(ModificationType.MODIFIED, actual.getModType());
      assertEquals(666L, actual.getTypeUuid());
      assertEquals(ModificationType.NEW, actual.getBaseModType());
      assertEquals(777L, actual.getBaseTypeUuid());
      assertEquals("abcdefg", actual.getGuid());
   }

   @Test
   public void testCloneAttributeData() throws OseeCoreException {
      AttributeData actual = dataFactory.clone(attrData);
      verify(proxyFactory).createProxy(666L, expectedProxyData);

      VersionData actualVer = actual.getVersion();

      assertNotSame(attrData, actual);
      assertNotSame(verData, actualVer);

      assertEquals(BRANCH, actualVer.getBranch());
      assertEquals(222L, actualVer.getGammaId());
      assertEquals(tx333, actualVer.getTransactionId());
      assertEquals(tx444, actualVer.getStripeId());
      assertEquals(true, actualVer.isHistorical());
      assertEquals(true, actualVer.isInStorage());

      assertEquals(555, actual.getLocalId().intValue());
      assertEquals(ModificationType.MODIFIED, actual.getModType());
      assertEquals(666L, actual.getTypeUuid());
      assertEquals(ModificationType.NEW, actual.getBaseModType());
      assertEquals(777L, actual.getBaseTypeUuid());

      assertEquals(88, actual.getArtifactId());
      assertNotSame(dataProxy, actual.getDataProxy());

      Object[] objData = actual.getDataProxy().getData();
      assertNotSame(expectedProxyData, objData);
      assertEquals(expectedProxyData[0], objData[0]);
      assertEquals(expectedProxyData[1], objData[1]);
      assertEquals(expectedProxyData[2], objData[2]);
   }

   @Test
   public void testCloneRelationData() throws OseeCoreException {
      RelationData actual = dataFactory.clone(relData);
      VersionData actualVer = actual.getVersion();

      assertNotSame(relData, actual);
      assertNotSame(verData, actualVer);

      assertEquals(BRANCH, actualVer.getBranch());
      assertEquals(222L, actualVer.getGammaId());
      assertEquals(tx333, actualVer.getTransactionId());
      assertEquals(tx444, actualVer.getStripeId());
      assertEquals(true, actualVer.isHistorical());
      assertEquals(true, actualVer.isInStorage());

      assertEquals(555, actual.getLocalId().intValue());
      assertEquals(ModificationType.MODIFIED, actual.getModType());
      assertEquals(666L, actual.getTypeUuid());
      assertEquals(ModificationType.NEW, actual.getBaseModType());
      assertEquals(777L, actual.getBaseTypeUuid());

      assertEquals(88, actual.getArtIdA());
      assertEquals(99, actual.getArtIdB());
      assertEquals("this is the rationale", actual.getRationale());
   }

}