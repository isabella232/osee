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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.jdbc.JdbcConnection;
import org.eclipse.osee.jdbc.JdbcStatement;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.OrcsChangeSet;
import org.eclipse.osee.orcs.data.TransactionReadable;
import org.eclipse.osee.orcs.db.internal.sql.join.IdJoinQuery;
import org.eclipse.osee.orcs.db.internal.transaction.TransactionWriter.SqlOrderEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test Case for {@link TransactionWriter}
 * 
 * @author Roberto E. Escobar
 */
public class TransactionWriterTest {

   private static final long BRANCH_ID = 65L;

   private static final int QUERY_ID_1 = 88;
   private static final int QUERY_ID_2 = 89;

   private static final int TX_1 = 51;
   private static final int TX_2 = 52;

   private static final long GAMMA_1 = 80000L;
   private static final long GAMMA_2 = 80001L;

   //@formatter:off
   @Mock private Log logger;
   @Mock private JdbcClient jdbcClient;
   
   
   @Mock private TxSqlBuilder builder;
   @Mock private JdbcConnection connection;
   @Mock private TransactionReadable tx;
   @Mock private DaoToSql dao1;
   @Mock private DaoToSql dao2;
   
   @Mock private IdJoinQuery join1;
   @Mock private IdJoinQuery join2;
   
   @Mock private JdbcStatement chStmt;
   @Captor private ArgumentCaptor<List<Object[]>> paramCaptor;
   @Mock  private OrcsChangeSet changeSet;
   //@formatter:on

   private TransactionWriter writer;
   private List<DaoToSql> stores;

   @Before
   public void setUp() throws OseeCoreException {
      MockitoAnnotations.initMocks(this);

      writer = new TransactionWriter(logger, jdbcClient, builder);

      stores = Arrays.asList(dao1, dao2);

      final Map<SqlOrderEnum, IdJoinQuery> joins = new LinkedHashMap<>();
      joins.put(SqlOrderEnum.ARTIFACTS, join1);
      joins.put(SqlOrderEnum.ATTRIBUTES, join2);

      when(join1.getQueryId()).thenReturn(QUERY_ID_1);
      when(join2.getQueryId()).thenReturn(QUERY_ID_2);

      when(tx.getBranchId()).thenReturn(BRANCH_ID);
      when(builder.getBinaryStores()).thenReturn(stores);
      when(builder.getTxNotCurrents()).thenAnswer(new Answer<Set<Entry<SqlOrderEnum, IdJoinQuery>>>() {

         @Override
         public Set<Entry<SqlOrderEnum, IdJoinQuery>> answer(InvocationOnMock invocation) throws Throwable {
            Set<Entry<SqlOrderEnum, IdJoinQuery>> values = joins.entrySet();
            return values;
         }
      });
      when(jdbcClient.getStatement(connection)).thenReturn(chStmt);

      when(chStmt.next()).thenReturn(true).thenReturn(true).thenReturn(false);
      when(chStmt.getInt("transaction_id")).thenReturn(TX_1).thenReturn(TX_2);
      when(chStmt.getLong("gamma_id")).thenReturn(GAMMA_1).thenReturn(GAMMA_2);
   }

   @Test
   public void testRollback() throws OseeCoreException {
      TransactionWriter spy = Mockito.spy(writer);

      OseeCoreException expected = new OseeCoreException("Testing");

      when(spy.getBinaryStores()).thenReturn(stores);
      doThrow(expected).when(dao1).rollBack();

      spy.rollback();

      verify(dao1).rollBack();
      verify(dao2).rollBack();

      verify(logger).error(expected, "Error during binary rollback [%s]", dao1);
   }

   @Test
   public void testWrite() throws OseeCoreException {
      InOrder inOrder = inOrder(builder, tx, join1, join2, dao1, dao2, jdbcClient, chStmt);

      writer.write(connection, tx, changeSet);

      inOrder.verify(builder).accept(tx, changeSet);
      inOrder.verify(builder).getBinaryStores();
      inOrder.verify(dao1).persist();
      inOrder.verify(dao2).persist();

      inOrder.verify(builder).getTxNotCurrents();

      inOrder.verify(join1).store();
      inOrder.verify(chStmt).runPreparedQuery(SqlOrderEnum.ARTIFACTS.getTxsNotCurrentQuery(), QUERY_ID_1, BRANCH_ID);
      inOrder.verify(join1).delete();

      inOrder.verify(join2).store();
      inOrder.verify(chStmt).runPreparedQuery(SqlOrderEnum.ATTRIBUTES.getTxsNotCurrentQuery(), QUERY_ID_2, BRANCH_ID);
      inOrder.verify(join2).delete();

      inOrder.verify(builder).getInsertData(SqlOrderEnum.ARTIFACTS);
      inOrder.verify(builder).getInsertData(SqlOrderEnum.ATTRIBUTES);
      inOrder.verify(builder).getInsertData(SqlOrderEnum.RELATIONS);
      inOrder.verify(builder).getInsertData(SqlOrderEnum.TXS_DETAIL);
      inOrder.verify(builder).getInsertData(SqlOrderEnum.TXS);

      inOrder.verify(jdbcClient).runBatchUpdate(eq(connection), eq(TransactionWriter.UPDATE_TXS_NOT_CURRENT),
         paramCaptor.capture());

      inOrder.verify(builder).clear();

      Iterator<Object[]> params = paramCaptor.getValue().iterator();
      int index = 0;
      Object[] data = params.next();
      Assert.assertEquals(BRANCH_ID, data[index++]);
      Assert.assertEquals(TX_1, data[index++]);
      Assert.assertEquals(GAMMA_1, data[index++]);

      index = 0;
      data = params.next();
      Assert.assertEquals(BRANCH_ID, data[index++]);
      Assert.assertEquals(TX_2, data[index++]);
      Assert.assertEquals(GAMMA_2, data[index++]);
   }
}
