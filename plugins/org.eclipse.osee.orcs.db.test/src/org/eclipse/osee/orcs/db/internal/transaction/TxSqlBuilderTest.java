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
package org.eclipse.osee.orcs.db.internal.transaction;

import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import static org.eclipse.osee.framework.core.enums.ModificationType.ARTIFACT_DELETED;
import static org.eclipse.osee.framework.core.enums.ModificationType.DELETED;
import static org.eclipse.osee.framework.core.enums.ModificationType.INTRODUCED;
import static org.eclipse.osee.framework.core.enums.ModificationType.MERGED;
import static org.eclipse.osee.framework.core.enums.ModificationType.MODIFIED;
import static org.eclipse.osee.framework.core.enums.ModificationType.NEW;
import static org.eclipse.osee.framework.core.enums.ModificationType.REPLACED_WITH_VERSION;
import static org.eclipse.osee.framework.core.enums.ModificationType.UNDELETED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.OseeCodeVersion;
import org.eclipse.osee.framework.core.data.RelationalConstants;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.orcs.core.ds.ArtifactData;
import org.eclipse.osee.orcs.core.ds.AttributeData;
import org.eclipse.osee.orcs.core.ds.DataProxy;
import org.eclipse.osee.orcs.core.ds.OrcsChangeSet;
import org.eclipse.osee.orcs.core.ds.OrcsData;
import org.eclipse.osee.orcs.core.ds.RelationData;
import org.eclipse.osee.orcs.core.ds.VersionData;
import org.eclipse.osee.orcs.db.internal.IdentityManager;
import org.eclipse.osee.orcs.db.internal.loader.data.ArtifactDataImpl;
import org.eclipse.osee.orcs.db.internal.loader.data.AttributeDataImpl;
import org.eclipse.osee.orcs.db.internal.loader.data.RelationDataImpl;
import org.eclipse.osee.orcs.db.internal.loader.data.TransactionDataImpl;
import org.eclipse.osee.orcs.db.internal.loader.data.VersionDataImpl;
import org.eclipse.osee.orcs.db.internal.sql.join.IdJoinQuery;
import org.eclipse.osee.orcs.db.internal.sql.join.SqlJoinFactory;
import org.eclipse.osee.orcs.db.internal.transaction.TransactionWriter.SqlOrderEnum;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test Case for {@link TxSqlBuilderImpl}
 *
 * @author Roberto E. Escobar
 */
public class TxSqlBuilderTest {

   private static final List<ModificationType> MODS_ITEMS_ROW = Arrays.asList(NEW, MODIFIED, MERGED, UNDELETED);
   private static final List<ModificationType> MODS_REUSE_ROW =
      Arrays.asList(ARTIFACT_DELETED, DELETED, INTRODUCED, REPLACED_WITH_VERSION);

   private static final TransactionId EXPECTED_TX = TransactionId.valueOf(10000);
   private static final TransactionId LOADED_TX_ID = TransactionId.valueOf(567);
   private static final ArtifactId EXPECTED_COMMIT_ART = ArtifactId.valueOf(46);
   private static final ArtifactId EXPECTED_AUTHOR = ArtifactId.valueOf(89);
   private static final String EXPECTED_COMMENT = "My comment";
   private static final TransactionDetailsType EXPECTED_TX_TYPE = TransactionDetailsType.Baselined;
   private static final Date EXPECTED_TX_TIME = new Date();

   private static final int ITEM_ID = 789;
   private static final String EXP_GUID = GUID.create();
   private static final long TYPE_UUID = 72132144189L;

   private static final long NEXT_GAMMA_ID = 751382L;

   private static final Integer A_ART_ID = 6737;
   private static final Integer B_ART_ID = 1231;
   private static final String RATIONALE = "a rationale";

   private static final int ATTR_ARTIFACT_ID = 12341242;
   private static final String ATTR_URI = "attr://123/123/123/14/some.zip";
   private static final String ATTR_VALUE = "ksahfkashfdlakshfashfaer";

   // @formatter:off
   @Mock private SqlJoinFactory joinFactory;
   @Mock private IdentityManager idManager;

   @Mock private OrcsChangeSet txData;

   @Mock private DataProxy dataProxy;
   @Mock private IdJoinQuery join;
   // @formatter:on

   private VersionData versionData;
   private TxSqlBuilderImpl builder;

   private ArtifactData artData;
   private AttributeData attrData;
   private RelationData relData;
   private final TransactionDataImpl tx = new TransactionDataImpl(EXPECTED_TX.getId());

   @Before
   public void setUp() throws OseeCoreException {
      MockitoAnnotations.initMocks(this);

      versionData = new VersionDataImpl();
      versionData.setBranch(COMMON);
      versionData.setTransactionId(LOADED_TX_ID);

      builder = new TxSqlBuilderImpl(joinFactory, idManager);

      artData = new ArtifactDataImpl(versionData);
      artData.setLocalId(ITEM_ID);
      artData.setTypeUuid(TYPE_UUID);
      artData.setGuid(EXP_GUID);

      attrData = new AttributeDataImpl(versionData);
      attrData.setLocalId(ITEM_ID);
      attrData.setTypeUuid(TYPE_UUID);
      attrData.setArtifactId(ATTR_ARTIFACT_ID);
      attrData.setDataProxy(dataProxy);

      Object[] proxyData = new Object[] {ATTR_VALUE, ATTR_URI};
      when(dataProxy.getData()).thenReturn(proxyData);

      relData = new RelationDataImpl(versionData);
      relData.setLocalId(ITEM_ID);
      relData.setTypeUuid(TYPE_UUID);
      relData.setArtIdA(A_ART_ID);
      relData.setArtIdB(B_ART_ID);
      relData.setRationale(RATIONALE);

      tx.setAuthor(EXPECTED_AUTHOR);
      tx.setBranch(COMMON);
      tx.setComment(EXPECTED_COMMENT);
      tx.setCommitArt(EXPECTED_COMMIT_ART);
      tx.setDate(EXPECTED_TX_TIME);
      tx.setTxType(EXPECTED_TX_TYPE);

      when(idManager.getNextGammaId()).thenReturn(NEXT_GAMMA_ID);

      when(joinFactory.createIdJoinQuery()).thenAnswer(new Answer<IdJoinQuery>() {

         @Override
         public IdJoinQuery answer(InvocationOnMock invocation) throws Throwable {
            return join;
         }

      });
   }

   @Test
   public void testEmptyData() {
      assertNotNull(builder.getBinaryStores());
      assertNotNull(builder.getInsertData(null));
      assertNotNull(builder.getTxNotCurrents());
   }

   @Test
   public void testAccept() throws OseeCoreException {
      builder.accept(tx, txData);
      verify(txData).accept(builder);

      assertTrue(builder.getBinaryStores().isEmpty());
      assertTrue(builder.getTxNotCurrents().isEmpty());

      verifyEmpty(allExcept(SqlOrderEnum.TXS_DETAIL));
      List<Object[]> datas = builder.getInsertData(SqlOrderEnum.TXS_DETAIL);

      assertEquals(1, datas.size());
      Object[] data = datas.iterator().next();
      int index = 0;
      assertEquals(EXPECTED_TX, data[index++]);
      assertEquals(EXPECTED_COMMENT, data[index++]);
      assertEquals(EXPECTED_TX_TIME, data[index++]);
      assertEquals(EXPECTED_AUTHOR, data[index++]);
      assertEquals(COMMON, data[index++]);
      assertEquals(EXPECTED_TX_TYPE.getId(), data[index++]);
      assertEquals(OseeCodeVersion.getVersionId(), data[index++]);
   }

   @Test
   public void testAcceptArtifactData() throws OseeCoreException {
      for (ModificationType modType : MODS_ITEMS_ROW) {
         builder.accept(tx, txData);
         artData.setModType(modType);

         builder.visit(artData);

         assertTrue(builder.getBinaryStores().isEmpty());
         verifyEmpty(allExcept(SqlOrderEnum.TXS_DETAIL, SqlOrderEnum.TXS, SqlOrderEnum.ARTIFACTS));

         // @formatter:off
         verifyRow(SqlOrderEnum.ARTIFACTS, ITEM_ID, TYPE_UUID, NEXT_GAMMA_ID, EXP_GUID);
         verifyRow(SqlOrderEnum.TXS, EXPECTED_TX, NEXT_GAMMA_ID, modType.getValue(), TxChange.CURRENT.getValue(), COMMON);
         verifyQuery(SqlOrderEnum.ARTIFACTS);
         // @formatter:on

         reset(artData);
      }
   }

   @Test
   public void testAcceptArtifactDataNoItemRow() throws OseeCoreException {
      for (ModificationType modType : MODS_REUSE_ROW) {
         builder.accept(tx, txData);
         artData.setModType(modType);

         builder.visit(artData);

         assertTrue(builder.getBinaryStores().isEmpty());
         verifyEmpty(allExcept(SqlOrderEnum.TXS_DETAIL, SqlOrderEnum.TXS));

         ModificationType expectedType = modType != REPLACED_WITH_VERSION ? modType : MODIFIED;

         // @formatter:off
         verifyRow(SqlOrderEnum.TXS, EXPECTED_TX, -1L, expectedType.getValue(), TxChange.getCurrent(expectedType).getValue(), COMMON);
         verifyQuery(SqlOrderEnum.ARTIFACTS);
         // @formatter:on

         reset(artData);
      }
   }

   @Test
   public void testAcceptArtifactDataNoRows() throws OseeCoreException {
      builder.accept(tx, txData);

      // test new artData that is deleted adds no rows
      artData.setModType(ModificationType.DELETED);
      artData.getVersion().setTransactionId(TransactionId.SENTINEL);

      builder.visit(artData);

      assertTrue(builder.getBinaryStores().isEmpty());
      verifyEmpty(allExcept(SqlOrderEnum.TXS_DETAIL));

      reset(artData);

      // test existing artifact with no changes
      artData.setBaseModType(ModificationType.DELETED);
      artData.setBaseTypeUuid(0);
      artData.setTypeUuid(0);
      builder.visit(artData);

      assertTrue(builder.getBinaryStores().isEmpty());
      verifyEmpty(allExcept(SqlOrderEnum.TXS_DETAIL));

      reset(artData);
   }

   @Test
   public void testAcceptRelationData() throws OseeCoreException {
      for (ModificationType modType : MODS_ITEMS_ROW) {
         builder.accept(tx, txData);
         relData.setModType(modType);

         builder.visit(relData);

         assertTrue(builder.getBinaryStores().isEmpty());
         verifyEmpty(allExcept(SqlOrderEnum.TXS_DETAIL, SqlOrderEnum.TXS, SqlOrderEnum.RELATIONS));

         // @formatter:off
         verifyRow(SqlOrderEnum.RELATIONS, ITEM_ID, TYPE_UUID, NEXT_GAMMA_ID, A_ART_ID, B_ART_ID, RATIONALE);
         verifyRow(SqlOrderEnum.TXS, EXPECTED_TX, NEXT_GAMMA_ID, modType.getValue(), TxChange.CURRENT.getValue(), COMMON);
         verifyQuery(SqlOrderEnum.RELATIONS);
         // @formatter:on

         reset(relData);
      }
   }

   @Test
   public void testAcceptRelationDataNoItemRow() throws OseeCoreException {
      for (ModificationType modType : MODS_REUSE_ROW) {
         builder.accept(tx, txData);
         relData.setModType(modType);

         builder.visit(relData);

         assertTrue(builder.getBinaryStores().isEmpty());
         verifyEmpty(allExcept(SqlOrderEnum.TXS_DETAIL, SqlOrderEnum.TXS));

         ModificationType expectedType = modType != REPLACED_WITH_VERSION ? modType : MODIFIED;

         // @formatter:off
         verifyRow(SqlOrderEnum.TXS, EXPECTED_TX, -1L, expectedType.getValue(), TxChange.getCurrent(expectedType).getValue(), COMMON);
         verifyQuery(SqlOrderEnum.RELATIONS);
         // @formatter:on

         reset(relData);
      }
   }

   @Test
   public void testAcceptAttributeData() throws OseeCoreException {
      for (ModificationType modType : MODS_ITEMS_ROW) {
         when(dataProxy.getData()).thenReturn(new Object[] {ATTR_VALUE, ATTR_URI});

         builder.accept(tx, txData);
         attrData.setModType(modType);

         builder.visit(attrData);

         verifyEmpty(allExcept(SqlOrderEnum.TXS_DETAIL, SqlOrderEnum.TXS, SqlOrderEnum.ATTRIBUTES));

         // @formatter:off
         verifyRow(SqlOrderEnum.ATTRIBUTES, ITEM_ID, TYPE_UUID, NEXT_GAMMA_ID, ATTR_ARTIFACT_ID,  ATTR_VALUE, ATTR_URI);
         verifyRow(SqlOrderEnum.TXS, EXPECTED_TX, NEXT_GAMMA_ID, modType.getValue(), TxChange.CURRENT.getValue(), COMMON);
         verifyQuery(SqlOrderEnum.ATTRIBUTES);
         // @formatter:on

         assertEquals(1, builder.getBinaryStores().size());
         DaoToSql dao = builder.getBinaryStores().get(0);

         assertEquals(NEXT_GAMMA_ID, dao.getGammaId());
         assertEquals(ATTR_URI, dao.getUri());
         assertEquals(ATTR_VALUE, dao.getValue());

         when(dataProxy.getData()).thenReturn(new Object[] {"aValue", "aURI"});

         builder.updateAfterBinaryStorePersist();
         verifyRow(SqlOrderEnum.ATTRIBUTES, ITEM_ID, TYPE_UUID, NEXT_GAMMA_ID, ATTR_ARTIFACT_ID, "aValue", "aURI");

         reset(attrData);
      }
   }

   @Test
   public void testAcceptAttributeDataNoRow() throws OseeCoreException {
      for (ModificationType modType : MODS_REUSE_ROW) {
         builder.accept(tx, txData);
         attrData.setModType(modType);

         builder.visit(attrData);

         assertTrue(builder.getBinaryStores().isEmpty());
         verifyEmpty(allExcept(SqlOrderEnum.TXS_DETAIL, SqlOrderEnum.TXS));

         ModificationType expectedType = modType != REPLACED_WITH_VERSION ? modType : MODIFIED;

         // @formatter:off
         verifyRow(SqlOrderEnum.TXS, EXPECTED_TX, -1L, expectedType.getValue(), TxChange.getCurrent(expectedType).getValue(), COMMON);
         verifyQuery(SqlOrderEnum.ATTRIBUTES);
         // @formatter:on

         reset(attrData);
      }
   }

   private void reset(OrcsData data) {
      Mockito.reset(join);
      data.getVersion().setTransactionId(LOADED_TX_ID);
      data.getVersion().setGammaId(RelationalConstants.GAMMA_SENTINEL);
   }

   private void verifyQuery(SqlOrderEnum key) {
      assertEquals(1, builder.getTxNotCurrents().size());
      Entry<SqlOrderEnum, IdJoinQuery> entry = builder.getTxNotCurrents().iterator().next();
      assertEquals(key, entry.getKey());
      assertEquals(join, entry.getValue());

      verify(entry.getValue()).add(ITEM_ID);
   }

   private void verifyRow(SqlOrderEnum key, Object... expecteds) {
      List<Object[]> datas = builder.getInsertData(key);
      assertEquals(1, datas.size());
      Object[] data = datas.iterator().next();
      int index = 0;
      for (Object expected : expecteds) {
         assertEquals(String.format("Error [%s] [%s]", key.name(), index), expected, data[index++]);
      }
   }

   private SqlOrderEnum[] allExcept(SqlOrderEnum... keys) {
      SqlOrderEnum[] all = SqlOrderEnum.values();
      SqlOrderEnum[] toReturn = new SqlOrderEnum[all.length - keys.length];
      List<SqlOrderEnum> values = Arrays.asList(keys);
      int index = 0;
      for (SqlOrderEnum item : all) {
         if (!values.contains(item)) {
            toReturn[index++] = item;
         }
      }
      return toReturn;
   }

   private void verifyEmpty(SqlOrderEnum... keys) {
      for (SqlOrderEnum key : keys) {
         assertTrue(builder.getInsertData(key).isEmpty());
      }
   }

}