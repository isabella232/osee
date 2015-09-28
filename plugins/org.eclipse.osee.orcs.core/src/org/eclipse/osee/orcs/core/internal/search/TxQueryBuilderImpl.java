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
package org.eclipse.osee.orcs.core.internal.search;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.core.ds.Criteria;
import org.eclipse.osee.orcs.core.ds.Options;
import org.eclipse.osee.orcs.core.ds.QueryData;
import org.eclipse.osee.orcs.search.Operator;
import org.eclipse.osee.orcs.search.TxQueryBuilder;

/**
 * @author Roberto E. Escobar
 */
public class TxQueryBuilderImpl<T> implements TxQueryBuilder<T> {

   private final TransactionCriteriaFactory criteriaFactory;
   private final QueryData queryData;

   public TxQueryBuilderImpl(TransactionCriteriaFactory criteriaFactory, QueryData queryData) {
      this.criteriaFactory = criteriaFactory;
      this.queryData = queryData;
   }

   private QueryData getQueryData() {
      return queryData;
   }

   private Options getOptions() {
      return queryData.getOptions();
   }

   @Override
   public T andTxId(int... ids) throws OseeCoreException {
      Set<Integer> values = new LinkedHashSet<>();
      for (int value : ids) {
         values.add(value);
      }
      return andTxIds(values);
   }

   @Override
   public T andTxIds(Collection<Integer> ids) throws OseeCoreException {
      Criteria criteria = criteriaFactory.newByIdsCriteria(ids);
      return addAndCheck(queryData, criteria);
   }

   @Override
   public T andTxId(Operator op, int id) throws OseeCoreException {
      Criteria criteria = criteriaFactory.newByIdWithOperator(op, id);
      return addAndCheck(queryData, criteria);
   }

   @Override
   public T andTxId(Operator op1, int id1, Operator op2, int id2) throws OseeCoreException {
      Criteria criteria = criteriaFactory.newByIdWithTwoOperators(op1, id1, op2, id2);
      return addAndCheck(queryData, criteria);
   }

   @Override
   public T andCommentEquals(String value) throws OseeCoreException {
      Criteria criteria = criteriaFactory.newCommentCriteria(value, false);
      return addAndCheck(queryData, criteria);
   }

   @Override
   public T andCommentPattern(String pattern) throws OseeCoreException {
      Criteria criteria = criteriaFactory.newCommentCriteria(pattern, true);
      return addAndCheck(queryData, criteria);
   }

   @Override
   public T andIs(TransactionDetailsType... types) throws OseeCoreException {
      return andIs(Arrays.asList(types));
   }

   @Override
   public T andIs(Collection<TransactionDetailsType> types) throws OseeCoreException {
      Criteria criteria = criteriaFactory.newTxTypeCriteria(types);
      return addAndCheck(queryData, criteria);
   }

   @Override
   public T andBranch(IOseeBranch... ids) throws OseeCoreException {
      return andBranch(Arrays.asList(ids));
   }

   @Override
   public T andBranch(Collection<? extends IOseeBranch> ids) throws OseeCoreException {
      Set<Long> values = new LinkedHashSet<>();
      for (IOseeBranch value : ids) {
         values.add(value.getUuid());
      }
      Criteria criteria = criteriaFactory.newTxBranchIdCriteria(values);
      return addAndCheck(queryData, criteria);
   }

   @Override
   public T andBranchIds(long... ids) throws OseeCoreException {
      Set<Long> values = new LinkedHashSet<>();
      for (long value : ids) {
         values.add(value);
      }
      return andBranchIds(values);
   }

   @Override
   public T andBranchIds(Collection<Long> ids) throws OseeCoreException {
      Criteria criteria = criteriaFactory.newTxBranchIdCriteria(ids);
      return addAndCheck(queryData, criteria);
   }

   @Override
   public T andDate(Operator op, Timestamp date) throws OseeCoreException {
      Criteria criteria = criteriaFactory.newByDateWithOperator(op, date);
      return addAndCheck(queryData, criteria);
   }

   @Override
   public T andDate(Timestamp from, Timestamp to) throws OseeCoreException {
      Criteria criteria = criteriaFactory.newByDateRange(from, to);
      return addAndCheck(queryData, criteria);
   }

   @Override
   public T andAuthorLocalIds(ArtifactId... id) throws OseeCoreException {
      return andAuthorLocalIds(Arrays.asList(id));
   }

   @Override
   public T andAuthorLocalIds(Collection<ArtifactId> ids) throws OseeCoreException {
      Criteria criteria = criteriaFactory.newByArtifactId(ids);
      return addAndCheck(queryData, criteria);
   }

   @Override
   public T andAuthorIds(int... id) throws OseeCoreException {
      ArrayList<Integer> theList = new ArrayList<>();
      for (int i = 0; i < id.length; i++) {
         theList.add(new Integer(id[i]));
      }
      return andAuthorIds(theList);
   }

   @Override
   public T andAuthorIds(Collection<Integer> ids) throws OseeCoreException {
      Criteria criteria = criteriaFactory.newByAuthorId(ids);
      return addAndCheck(queryData, criteria);
   }

   @Override
   public T andCommitIds(Integer... id) throws OseeCoreException {
      return andCommitIds(Arrays.asList(id));
   }

   @Override
   public T andNullCommitId() throws OseeCoreException {
      Collection<Integer> aNull = new ArrayList<>();
      aNull.add(null);
      return andCommitIds(aNull);
   }

   @Override
   public T andCommitIds(Collection<Integer> ids) throws OseeCoreException {
      Criteria criteria = criteriaFactory.newByCommitId(ids);
      return addAndCheck(queryData, criteria);
   }

   @Override
   public T andIsHead(IOseeBranch branch) throws OseeCoreException {
      return andIsHead(branch.getUuid());
   }

   @Override
   public T andIsHead(long branchUuid) throws OseeCoreException {
      Criteria criteria = criteriaFactory.newGetHead(branchUuid);
      return addAndCheck(queryData, criteria);
   }

   @Override
   public T andIsPriorTx(int txId) throws OseeCoreException {
      Criteria criteria = criteriaFactory.newGetPriorTx(txId);
      return addAndCheck(queryData, criteria);
   }

   @SuppressWarnings("unchecked")
   private T addAndCheck(QueryData queryData, Criteria criteria) throws OseeCoreException {
      criteria.checkValid(getOptions());
      queryData.addCriteria(criteria);
      return (T) this;
   }

   public QueryData buildAndCopy() {
      return build(true);
   }

   public QueryData build() {
      return build(false);
   }

   private QueryData build(boolean clone) {
      QueryData queryData = clone ? getQueryData().clone() : getQueryData();
      if (queryData.getAllCriteria().isEmpty()) {
         addAndCheck(queryData, criteriaFactory.createAllTransactionsCriteria());
      }
      return queryData;
   }

}
