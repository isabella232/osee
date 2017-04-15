/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.disposition.rest.internal;

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.disposition.model.DispoItem;
import org.eclipse.osee.disposition.model.DispoSet;
import org.eclipse.osee.disposition.model.OperationReport;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Angel Avila
 */
public interface DispoWriter {

   Long createDispoProgram(ArtifactReadable author, String name);

   Long createDispoSet(ArtifactReadable author, BranchId branch, DispoSet descriptor);

   void updateDispoSet(ArtifactReadable author, BranchId branch, String dispoSetId, DispoSet data);

   boolean deleteDispoSet(ArtifactReadable author, BranchId branch, String setId);

   void createDispoItems(ArtifactReadable author, BranchId branch, DispoSet parentSet, List<DispoItem> data);

   boolean deleteDispoItem(ArtifactReadable author, BranchId branch, String itemId);

   void updateDispoItem(ArtifactReadable author, BranchId branch, String dispoItemId, DispoItem data);

   void updateDispoItems(ArtifactReadable author, BranchId branch, Collection<DispoItem> data, boolean resetRerunFlag, String operation);

   void updateOperationSummary(ArtifactReadable author, BranchId branch, String setId, OperationReport summary);

   String createDispoReport(BranchId branch, ArtifactReadable author, String contens, String operationTitle);

}