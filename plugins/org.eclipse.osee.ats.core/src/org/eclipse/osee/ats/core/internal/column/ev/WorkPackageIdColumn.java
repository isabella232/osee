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

package org.eclipse.osee.ats.core.internal.column.ev;

import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.ev.IAtsEarnedValueServiceProvider;
import org.eclipse.osee.ats.api.ev.IAtsWorkPackage;
import org.eclipse.osee.framework.core.data.ArtifactToken;

/**
 * @author Donald G. Dunne
 */
public class WorkPackageIdColumn extends AbstractRelatedWorkPackageColumn {

   public WorkPackageIdColumn(IAtsEarnedValueServiceProvider earnedValueServiceProvider, AtsApi atsApi) {
      super(earnedValueServiceProvider, atsApi);
   }

   @Override
   protected String getColumnValue(IAtsWorkPackage workPkg) {
      return workPkg.getWorkPackageId();
   }

   @Override
   protected String getColumnValue(ArtifactToken wpArt) {
      if (atsApi == null) {
         return "";
      }
      return atsApi.getAttributeResolver().getSoleAttributeValue(wpArt, AtsAttributeTypes.WorkPackageId, "");
   }

}
