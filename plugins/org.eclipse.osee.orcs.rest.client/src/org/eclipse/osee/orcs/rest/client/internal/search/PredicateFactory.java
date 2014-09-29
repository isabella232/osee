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
package org.eclipse.osee.orcs.rest.client.internal.search;

import java.util.Collection;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.framework.jdk.core.type.Identity;
import org.eclipse.osee.orcs.rest.model.search.artifact.Predicate;

/**
 * @author John Misinco
 */
public interface PredicateFactory {

   Predicate createUuidSearch(Collection<String> ids);

   Predicate createLocalIdsSearch(Collection<Integer> ids);

   Predicate createIdSearch(Collection<? extends Identity<String>> ids);

   Predicate createIsOfTypeSearch(Collection<? extends IArtifactType> artifactType);

   Predicate createTypeEqualsSearch(Collection<? extends IArtifactType> artifactType);

   Predicate createAttributeTypeSearch(Collection<? extends IAttributeType> attributeTypes, String value, QueryOption... options);

   Predicate createAttributeTypeSearch(Collection<? extends IAttributeType> attributeTypes, Collection<String> values, QueryOption... options);

   Predicate createAttributeExistsSearch(Collection<? extends IAttributeType> attributeTypes);

   Predicate createRelationExistsSearch(Collection<? extends IRelationType> relationTypes);

   Predicate createRelationNotExistsSearch(Collection<? extends IRelationType> relationTypes);

   Predicate createRelationTypeSideNotExistsSearch(IRelationTypeSide relationTypeSide);

   Predicate createRelationTypeSideExistsSearch(IRelationTypeSide relationTypeSide);

   Predicate createRelatedToSearch(IRelationTypeSide relationTypeSide, Collection<?> ids);

}