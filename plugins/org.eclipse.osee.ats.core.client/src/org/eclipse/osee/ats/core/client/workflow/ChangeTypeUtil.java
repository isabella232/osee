/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.client.workflow;

import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.team.ChangeType;
import org.eclipse.osee.ats.core.client.internal.AtsClientService;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public class ChangeTypeUtil {

   public static String getChangeTypeStr(IAtsObject atsObject) throws OseeCoreException {
      ChangeType changeType = getChangeType(atsObject);
      if (changeType == ChangeType.None) {
         return "";
      }
      return changeType.name();
   }

   public static ChangeType getChangeType(IAtsObject atsObject) throws OseeCoreException {
      return ChangeType.getChangeType(AtsClientService.get().getArtifact(atsObject).getSoleAttributeValue(
         AtsAttributeTypes.ChangeType, ""));
   }

   public static void setChangeType(IAtsObject atsObject, ChangeType changeType) throws OseeCoreException {
      if (changeType == ChangeType.None) {
         AtsClientService.get().getArtifact(atsObject).deleteSoleAttribute(AtsAttributeTypes.ChangeType);
      } else {
         AtsClientService.get().getArtifact(atsObject).setSoleAttributeValue(AtsAttributeTypes.ChangeType,
            changeType.name());
      }
   }

}
