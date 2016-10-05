/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.rest.internal.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.core.query.AbstractAtsConfigQueryImpl;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.rest.IAtsServer;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.RelationTypeSide;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.search.QueryBuilder;

/**
 * @author Donald G. Dunne
 */
public class AtsConfigQueryImpl extends AbstractAtsConfigQueryImpl {

   private final IAtsServer atsServer;
   private QueryBuilder query;

   public AtsConfigQueryImpl(IAtsServer atsServer) {
      super(atsServer.getServices());
      this.atsServer = atsServer;
   }

   @Override
   public Collection<ArtifactId> runQuery() {
      List<ArtifactId> results = new ArrayList<ArtifactId>();
      Iterator<ArtifactReadable> iterator = query.getResults().iterator();
      while (iterator.hasNext()) {
         results.add(iterator.next());
      }
      return results;
   }

   @Override
   public void createQueryBuilder() {
      query = atsServer.getOrcsApi().getQueryFactory().fromBranch(AtsUtilCore.getAtsBranch());
   }

   private QueryBuilder getQuery() {
      Conditions.checkNotNull(query, "Query builder not created");
      return query;
   }

   @Override
   public void queryAnd(IAttributeType attrType, Collection<String> values) {
      getQuery().and(attrType, values);
   }

   @Override
   public void queryAndIsOfType(IArtifactType artifactType) {
      getQuery().andIsOfType(artifactType);
   }

   @Override
   public List<? extends ArtifactId> queryGetIds() {
      return getQuery().getResultsAsLocalIds().getList();
   }

   @Override
   public void queryAndIsOfType(List<IArtifactType> artTypes) {
      getQuery().andIsOfType(artTypes);
   }

   @Override
   public void queryAnd(IAttributeType attrType, String value) {
      getQuery().and(attrType, value);
   }

   @Override
   public void queryAnd(IAttributeType attrType, Collection<String> values, QueryOption[] queryOption) {
      getQuery().and(attrType, values, queryOption);
   }

   @Override
   public void queryAnd(IAttributeType attrType, String value, QueryOption[] queryOption) {
      getQuery().and(attrType, value, queryOption);
   }

   @Override
   public void queryAndLocalIds(List<Integer> artIds) {
      List<Long> results = new LinkedList<>();
      for (Integer artId : artIds) {
         results.add(new Long(artId));
      }
      getQuery().andUuids(results);
   }

   @Override
   public void queryAndNotExists(RelationTypeSide relationTypeSide) {
      getQuery().andNotExists(relationTypeSide);
   }

   @Override
   public void queryAndExists(RelationTypeSide relationTypeSide) {
      getQuery().andExists(relationTypeSide);
   }

}