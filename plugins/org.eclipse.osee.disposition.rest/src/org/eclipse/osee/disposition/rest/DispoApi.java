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
package org.eclipse.osee.disposition.rest;

import java.util.List;
import org.eclipse.osee.disposition.model.DispoAnnotationData;
import org.eclipse.osee.disposition.model.DispoItem;
import org.eclipse.osee.disposition.model.DispoItemData;
import org.eclipse.osee.disposition.model.DispoProgram;
import org.eclipse.osee.disposition.model.DispoSet;
import org.eclipse.osee.disposition.model.DispoSetData;
import org.eclipse.osee.disposition.model.DispoSetDescriptorData;
import org.eclipse.osee.disposition.rest.util.DispoFactory;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.jdk.core.type.Identifiable;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;

/**
 * @author Angel Avila
 */
public interface DispoApi {

   // Queries
   ResultSet<IOseeBranch> getDispoPrograms();

   List<DispoSet> getDispoSets(DispoProgram program);

   DispoSet getDispoSetById(DispoProgram program, String dispoSetId);

   List<DispoItem> getDispoItems(DispoProgram program, String dispoSetId);

   DispoItem getDispoItemById(DispoProgram program, String itemId);

   List<DispoAnnotationData> getDispoAnnotations(DispoProgram program, String itemId);

   DispoAnnotationData getDispoAnnotationById(DispoProgram program, String itemId, String annotationId);

   // Writes
   Identifiable<String> createDispoSet(DispoProgram program, DispoSetDescriptorData descriptor);

   String createDispoAnnotation(DispoProgram program, String itemId, DispoAnnotationData annotation, String userName);

   String editDispoSet(DispoProgram program, String dispoSetId, DispoSetData newDispoSet);

   boolean editDispoItem(DispoProgram program, String itemId, DispoItemData newDispoItem);

   boolean editDispoAnnotation(DispoProgram program, String itemId, String annotationId, DispoAnnotationData newAnnotation, String userName);

   String copyDispoSet(DispoProgram program, DispoSet destination, DispoSet source);

   // Deletes

   boolean deleteDispoSet(DispoProgram program, String dispoSetId);

   boolean deleteDispoItem(DispoProgram program, String itemId);

   boolean deleteDispoAnnotation(DispoProgram program, String itemId, String annotationId, String userName);

   // Utilities
   boolean isUniqueSetName(DispoProgram program, String setName);

   boolean isUniqueItemName(DispoProgram program, String setId, String itemName);

   DispoFactory getDispoFactory();

}
