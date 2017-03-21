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
package org.eclipse.osee.ats.api.util;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.notify.AtsNotificationCollector;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.IAttribute;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.data.AttributeId;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.RelationTypeSide;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public interface IAtsChangeSet {

   void add(Object obj) throws OseeCoreException;

   /**
    * Store changed items.
    *
    * @throws OseeCoreException if no items exist to store. Use executeIfNeeded to execute quietly.
    */
   TransactionId execute() throws OseeCoreException;

   void clear();

   void addExecuteListener(IExecuteListener listener);

   void addToDelete(Object obj) throws OseeCoreException;

   void addAll(Object... objects) throws OseeCoreException;

   boolean isEmpty();

   void deleteSoleAttribute(IAtsWorkItem workItem, AttributeTypeId attributeType) throws OseeCoreException;

   void setSoleAttributeValue(IAtsWorkItem workItem, AttributeTypeId attributeType, String value) throws OseeCoreException;

   void setSoleAttributeValue(IAtsObject atsObject, AttributeTypeId attributeType, Object value) throws OseeCoreException;

   void setSoleAttributeValue(ArtifactId artifact, AttributeTypeId attributeType, String value);

   void addAttribute(IAtsObject atsObject, AttributeTypeId attributeType, Object value) throws OseeCoreException;

   <T> void setValue(IAtsWorkItem workItem, IAttribute<String> attr, AttributeTypeId attributeType, T value) throws OseeCoreException;

   <T> void deleteAttribute(IAtsWorkItem workItem, IAttribute<T> attr) throws OseeCoreException;

   void deleteAttribute(IAtsObject atsObject, AttributeTypeId attributeType, Object value) throws OseeCoreException;

   boolean isAttributeTypeValid(IAtsWorkItem workItem, AttributeTypeId attributeType);

   ArtifactId createArtifact(IArtifactType artifactType, String name);

   void deleteAttributes(IAtsObject atsObject, AttributeTypeId attributeType);

   ArtifactToken createArtifact(IArtifactType artifactType, String name, String guid);

   ArtifactToken createArtifact(IArtifactType artifactType, String name, String guid, Long uuid);

   void relate(Object object1, RelationTypeSide relationSide, Object object2);

   AtsNotificationCollector getNotifications();

   void unrelateAll(Object object, RelationTypeSide relationType);

   void setRelation(Object object1, RelationTypeSide relationType, Object object2);

   public void setRelations(Object object, RelationTypeSide relationSide, Collection<? extends Object> objects);

   <T> void setAttribute(IAtsWorkItem workItem, int attributeId, T value);

   ArtifactId createArtifact(ArtifactToken token);

   void deleteArtifact(ArtifactId artifact);

   void deleteAttribute(ArtifactId artifact, IAttribute<?> attr);

   void addWorkflowCreated(IAtsTeamWorkflow teamWf);

   void deleteArtifact(IAtsWorkItem workItem);

   void setAttributeValues(IAtsObject atsObject, AttributeTypeId attrType, List<Object> values);

   String getComment();

   <T> void setAttribute(ArtifactId artifact, AttributeId attrId, T value);

   /**
    * Will check if anything is to be stored, else return quietly.
    */
   void executeIfNeeded();

   /**
    * User making these changes
    */
   IAtsUser getAsUser();

   void unrelate(ArtifactId artifact, RelationTypeSide relationSide, ArtifactId artifact2);

   void unrelate(IAtsObject atsObject, RelationTypeSide relationSide, IAtsObject atsObjec2);

   void unrelate(ArtifactId artifact, RelationTypeSide relationSide, IAtsObject atsObject);

   void unrelate(IAtsObject atsObject, RelationTypeSide relationSide, ArtifactId artifact);

   void addAttribute(ArtifactId artifactId, AttributeTypeId attrType, Object value);

   void setSoleAttributeFromString(ArtifactId artifact, AttributeTypeId attrType, String value);

   void setSoleAttributeFromString(IAtsObject atsObject, AttributeTypeId attributeType, String value);

   void setSoleAttributeFromStream(ArtifactId artifact, AttributeTypeId attributeType, InputStream inputStream);

   void reset(String string);

}
