/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.orcs.core.internal.search;

import org.eclipse.osee.framework.core.OrcsTokenService;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.core.ds.ApplicabilityDsQuery;
import org.eclipse.osee.orcs.core.ds.QueryData;
import org.eclipse.osee.orcs.core.ds.QueryEngine;
import org.eclipse.osee.orcs.search.ApplicabilityQuery;
import org.eclipse.osee.orcs.search.BranchQuery;
import org.eclipse.osee.orcs.search.DeepQuery;
import org.eclipse.osee.orcs.search.QueryBuilder;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.eclipse.osee.orcs.search.TransactionQuery;
import org.eclipse.osee.orcs.search.TupleQuery;

/**
 * @author Roberto E. Escobar
 */
public class QueryFactoryImpl implements QueryFactory {
   private static final Pattern fromBranchPattern = Pattern.compile("fromBranch\\s*\\(\\s*(\\d+)\\s*\\)");
   private static final Pattern fromBranchViewPattern =
      Pattern.compile("fromBranch\\s*\\(\\s*(\\d+|\".*?\")\\s*,\\s*(\\d+|\".*?\")\\s*\\)");
   private static final String stringLiteralRegEx = "\"(?:[^\"]|\"(?<!\\\\))\"";
   private static final String parameterRegEx = "(true|false|\\d+|" + stringLiteralRegEx + ")";
   private static final String parametersRegEx = "\\s*" + parameterRegEx + "?\\s*(?:,\\s*" + parameterRegEx + "\\s*)*";
   private static final Pattern queryMethodPattern =
      Pattern.compile("\\s*.\\s*(\\w+)\\s*\\(" + parametersRegEx + "\\)");
   private static final Pattern nonWhitespacePattern = Pattern.compile("\\S");
   private final CallableQueryFactory artQueryFactory;
   private final BranchCriteriaFactory branchCriteriaFactory;
   private final TransactionCriteriaFactory txCriteriaFactory;
   private final TupleQuery tupleQuery;
   private final ApplicabilityDsQuery applicabilityDsQuery;
   private final QueryEngine queryEngine;
   private final OrcsTokenService tokenService;
   private final ConcurrentHashMap<String, Method> queryMethods = new ConcurrentHashMap<>(100);
   private final OrcsApi orcsApi;

   public QueryFactoryImpl(OrcsApi orcsApi, CallableQueryFactory artQueryFactory, BranchCriteriaFactory branchCriteriaFactory, TransactionCriteriaFactory txCriteriaFactory, TupleQuery tupleQuery, ApplicabilityDsQuery applicabilityDsQuery, QueryEngine queryEngine, OrcsTokenService tokenService) {
      this.orcsApi = orcsApi;
      this.artQueryFactory = artQueryFactory;
      this.branchCriteriaFactory = branchCriteriaFactory;
      this.txCriteriaFactory = txCriteriaFactory;
      this.tupleQuery = tupleQuery;
      this.applicabilityDsQuery = applicabilityDsQuery;
      this.queryEngine = queryEngine;
      this.tokenService = tokenService;

      Method[] methods = QueryBuilder.class.getMethods();

      for (Method method : methods) {
         boolean keep = true;
         for (Parameter parameter : method.getParameters()) {
            if (parameter.getType().isAssignableFrom(Collection.class)) {
               keep = false;
            }
         }
         if (keep) {
            Method previousValue = queryMethods.put(method.getName() + method.getParameterCount(), method);
            if (previousValue != null) {
               throw new IllegalStateException(method + " and " + previousValue + " conflict");
            }
         }
      }
   }

   @Override
   public BranchQuery branchQuery() {
      return new BranchQueryImpl(queryEngine, branchCriteriaFactory,
         new QueryData(this, queryEngine, artQueryFactory, tokenService));
   }

   @Override
   public QueryBuilder fromOrcsScript(String orcsScript) {
      Matcher fromBranchMatcher = fromBranchPattern.matcher(orcsScript);
      Matcher fromBrancViewMatcher = fromBranchViewPattern.matcher(orcsScript);
      int index;
      QueryBuilder query;

      if (fromBranchMatcher.find()) {
         query = fromBranch(toBranchId(fromBranchMatcher.group(1)));
         index = fromBranchMatcher.end();
      } else if (fromBrancViewMatcher.find()) {
         query = fromBranch(toBranchId(fromBrancViewMatcher.group(1)), toView(fromBrancViewMatcher.group(2)));
         index = fromBrancViewMatcher.end();
      } else {
         throw new OseeArgumentException("missing fromBranch");
      }

      String criteria = orcsScript.substring(index);
      Matcher methodMatcher = queryMethodPattern.matcher(criteria);
      while (methodMatcher.find()) {
         index = methodMatcher.end();
         int parametersCount = 0;

         // start at group 2 to exclude the method name capture group
         for (int i = 2; i <= methodMatcher.groupCount(); i++) {
            if (methodMatcher.group(i) != null) {
               parametersCount++;
            }
         }

         Method method = queryMethods.get(methodMatcher.group(1) + parametersCount);
         if (method == null) {
            throw new OseeArgumentException("Method %s was not found with %s parameters", methodMatcher.group(1),
               parametersCount);
         }
         try {
            Object[] parameters = getParameters(methodMatcher, method);
            method.invoke(query, parameters);
         } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
            | SecurityException ex) {
            OseeCoreException.wrapAndThrow(ex);
         }
      }

      String trailingInput = criteria.substring(index);
      if (nonWhitespacePattern.matcher(trailingInput).find()) {
         throw new OseeArgumentException("Unexpected input found %s", trailingInput);
      }

      return query;
   }

   private Object[] getParameters(Matcher methodMatcher, Method method) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
      Parameter[] formalParams = method.getParameters();
      Object[] parameters = new Object[formalParams.length];
      for (int i = 0; i < formalParams.length; i++) {
         formalParams[i].isVarArgs();
         Class<?> type = formalParams[i].getType();
         if (formalParams[i].isVarArgs()) {
            type = type.getComponentType();
         }

         String value = methodMatcher.group(i + 2);
         parameters[i] = type.getMethod("valueOf", String.class).invoke(null, value);

         if (formalParams[i].isVarArgs()) {
            Object array = Array.newInstance(type, 1);
            Array.set(array, 0, parameters[i]);
            parameters[i] = array;
         }
      }
      return parameters;
   }

   private BranchId toBranchId(String value) {
      if (Strings.isNumeric(value)) {
         return BranchId.valueOf(value);
      }
      return branchQuery().andNameEquals(value).getResultsAsId().getExactlyOne();
   }

   private ArtifactId toView(String value) {
      return ArtifactId.valueOf(value);
   }

   @Override
   public QueryBuilder fromBranch(BranchId branch) {
      return new QueryData(this, queryEngine, artQueryFactory, tokenService, branch);
   }

   @Override
   public QueryBuilder fromBranch(BranchId branch, ArtifactId view) {
      return new QueryData(this, queryEngine, artQueryFactory, tokenService, branch, view);
   }

   @Override
   public TransactionQuery transactionQuery() {
      return new TransactionQueryImpl(queryEngine, txCriteriaFactory,
         new QueryData(this, queryEngine, artQueryFactory, tokenService));
   }

   @Override
   public TupleQuery tupleQuery() {
      return tupleQuery;
   }

   @Override
   public ApplicabilityQuery applicabilityQuery() {
      return new ApplicabilityQueryImpl(applicabilityDsQuery, this, orcsApi);
   }

   @Override
   public DeepQuery deepQuery() {
      return queryEngine.createDeepQuery();
   }
}