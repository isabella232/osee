/*********************************************************************
 * Copyright (c) 2013 Boeing
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

package org.eclipse.osee.orcs.core.ds.criteria;

import java.util.Collection;
import org.eclipse.osee.framework.core.data.ArtifactId;

/**
 * @author Roberto E. Escobar
 */
public final class CriteriaAuthorIds extends CriteriaMainTableField implements TxCriteria {
   public CriteriaAuthorIds(Collection<ArtifactId> authors) {
      super(authors);
   }

   public CriteriaAuthorIds(ArtifactId author) {
      super(author);
   }
}