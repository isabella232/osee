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
package org.eclipse.osee.ats.api.query;

import java.util.Collection;
import org.eclipse.osee.ats.api.IAtsConfigObject;
import org.eclipse.osee.ats.api.config.WorkType;
import org.eclipse.osee.ats.api.program.IAtsProgram;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;

/**
 * @author Donald G. Dunne
 */
public interface IAtsConfigQuery {

   IAtsConfigQuery andAttr(AttributeTypeId attributeType, String value, QueryOption... queryOption);

   <T extends IAtsConfigObject> ResultSet<T> getResults();

   Collection<ArtifactId> getItemIds() throws OseeCoreException;

   <T extends IAtsConfigObject> Collection<T> getItems();

   <T extends IAtsConfigObject> Collection<T> getItems(Class<T> clazz);

   IAtsConfigQuery isOfType(IArtifactType artifactType);

   IAtsConfigQuery andAttr(AttributeTypeId attributeType, Collection<String> values, QueryOption... queryOptions) throws OseeCoreException;

   IAtsConfigQuery andUuids(Long... uuids);

   IAtsConfigQuery andProgram(IAtsProgram program);

   IAtsConfigQuery andProgram(Long uuid);

   IAtsConfigQuery andWorkType(WorkType workType, WorkType... workTypes);

   IAtsConfigQuery andWorkType(Collection<WorkType> workTypes);

   IAtsConfigQuery andCsci(Collection<String> cscis);

   <T extends ArtifactId> ResultSet<T> getResultArtifacts();

   IAtsConfigQuery andName(String name);

   IAtsConfigQuery andTag(String... tags);

   IAtsConfigQuery andActive(boolean active);

   <T extends IAtsConfigObject> T getOneOrNull(Class<T> clazz);

}
