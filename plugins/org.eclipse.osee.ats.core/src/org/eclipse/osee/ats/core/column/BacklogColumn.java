/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.column;

import java.util.Collection;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;

/**
 * @author Donald G. Dunne
 */
public class BacklogColumn {

   public static String getColumnText(Object element, IAtsServices services, boolean forBacklog) {
      if (element instanceof IAtsWorkItem) {
         IAtsWorkItem workItem = (IAtsWorkItem) element;
         Collection<IAtsWorkItem> related =
            services.getRelationResolver().getRelated(workItem, AtsRelationTypes.Goal_Goal, IAtsWorkItem.class);
         if (!related.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (IAtsWorkItem relatedWorkItem : related) {
               if (forBacklog) {
                  if (isBacklog(relatedWorkItem, services)) {
                     sb.append(relatedWorkItem.getName());
                     sb.append("; ");
                  }
               } else {
                  if (!isBacklog(relatedWorkItem, services)) {
                     sb.append(relatedWorkItem.getName());
                     sb.append("; ");
                  }
               }
            }
            return sb.toString().replaceFirst("; $", "");
         }
      }
      return "";
   }

   public static boolean isBacklog(IAtsWorkItem workItem, IAtsServices services) {
      if (services.getStoreService().isOfType(workItem.getStoreObject(), AtsArtifactTypes.AgileBacklog)) {
         return true;
      }
      return services.getRelationResolver().getRelatedCount(workItem,
         AtsRelationTypes.AgileTeamToBacklog_AgileTeam) == 1;
   }
}
