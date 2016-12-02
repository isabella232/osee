/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.search;

import java.util.HashMap;
import java.util.List;
import org.eclipse.osee.framework.core.data.ApplicabilityToken;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.BranchViewData;
import org.eclipse.osee.framework.core.data.FeatureDefinitionData;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Ryan D. Brooks
 */
public interface ApplicabilityQuery {

   ApplicabilityToken getApplicabilityToken(ArtifactId artId, BranchId branch);

   List<ApplicabilityToken> getApplicabilityTokens(List<ArtifactId> artIds, BranchId branch);

   HashMap<Long, ApplicabilityToken> getApplicabilityTokens(BranchId branch1, BranchId branch2);

   HashMap<Long, ApplicabilityToken> getApplicabilityTokens(BranchId branch);

   List<FeatureDefinitionData> getFeatureDefinitionData(List<ArtifactReadable> featureDefinitionArts);

   List<ApplicabilityToken> getViewApplicabilityTokens(ArtifactId artId, BranchId branch);

   HashCollection<String, String> getBranchViewFeatureValues(BranchId branch);

   List<BranchViewData> getViews();
}