/*********************************************************************
 * Copyright (c) 2012 Boeing
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

package org.eclipse.osee.orcs.rest.internal.search.artifact.predicate;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.RelationTypeSide;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.rest.internal.search.artifact.PredicateHandler;
import org.eclipse.osee.orcs.rest.model.search.artifact.Predicate;
import org.eclipse.osee.orcs.rest.model.search.artifact.SearchMethod;
import org.eclipse.osee.orcs.search.QueryBuilder;

/**
 * @author John Misinco
 */
public class RelatedToPredicateHandler implements PredicateHandler {

   @Override
   public QueryBuilder handle(OrcsApi orcsApi, QueryBuilder builder, Predicate predicate) {
      if (predicate.getType() != SearchMethod.RELATED_TO) {
         throw new OseeArgumentException("This predicate handler only supports [%s]", SearchMethod.EXISTS_TYPE);
      }
      List<String> typeParameters = predicate.getTypeParameters();
      Collection<String> values = predicate.getValues();

      Conditions.checkNotNull(typeParameters, "typeParameters");
      Conditions.checkNotNull(values, "values");

      Collection<RelationTypeSide> types = PredicateHandlerUtil.getRelationTypeSides(typeParameters);
      Collection<ArtifactId> artIds = new LinkedList<>();

      for (String value : values) {
         if (GUID.isValid(value)) {
            throw new UnsupportedOperationException();
         } else {
            artIds.add(ArtifactId.valueOf(value));
         }
      }

      if (!artIds.isEmpty()) {
         if (artIds.size() == 1) {
            for (RelationTypeSide rts : types) {
               builder.andRelatedTo(rts, artIds.iterator().next());
            }
         } else {
            for (RelationTypeSide rts : types) {
               builder.andRelatedTo(rts, artIds);
            }
         }
      }
      return builder;
   }
}