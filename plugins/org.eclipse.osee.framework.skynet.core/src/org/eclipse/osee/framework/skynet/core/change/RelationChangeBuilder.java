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

package org.eclipse.osee.framework.skynet.core.change;

import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactTypeId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.GammaId;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.model.TransactionDelta;
import org.eclipse.osee.framework.core.model.type.RelationType;

/**
 * @author Jeff C. Phillips
 */
public class RelationChangeBuilder extends ChangeBuilder {
   private final ArtifactId bArtId;
   private final int relLinkId;
   private final String rationale;
   private final RelationType relationType;

   public RelationChangeBuilder(BranchId branch, ArtifactTypeId artifactType, GammaId sourceGamma, ArtifactId artId, TransactionDelta txDelta, ModificationType modType, ArtifactId bArtId, int relLinkId, String rationale, RelationType relationType, boolean isHistorical) {
      super(branch, artifactType, sourceGamma, artId, txDelta, modType, isHistorical);
      this.bArtId = bArtId;
      this.relLinkId = relLinkId;
      this.rationale = rationale;
      this.relationType = relationType;
   }

   public ArtifactId getbArtId() {
      return bArtId;
   }

   public int getRelLinkId() {
      return relLinkId;
   }

   public String getRationale() {
      return rationale;
   }

   public RelationType getRelationType() {
      return relationType;
   }

}
