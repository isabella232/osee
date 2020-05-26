/*********************************************************************
 * Copyright (c) 2011 Boeing
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

package org.eclipse.osee.ats.ide.util.validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.workflow.transition.IAtsXWidgetValidator;
import org.eclipse.osee.ats.api.workflow.transition.IAtsXWidgetValidatorProvider;
import org.eclipse.osee.ats.ide.workflow.review.defect.AtsXDefectValidator;
import org.eclipse.osee.ats.ide.workflow.review.role.AtsXUserRoleValidator;

/**
 * @author Donald G. Dunne
 */
public class AtsXWidgetValidateProviderClient implements IAtsXWidgetValidatorProvider {

   private static List<IAtsXWidgetValidator> atsValidators = new ArrayList<>();
   private boolean loaded = false;

   @Override
   public Collection<IAtsXWidgetValidator> getValidators() {
      if (!loaded) {
         loaded = true;
         atsValidators.add(new AtsXHyperlinkMemberSelValidator());
         atsValidators.add(new AtsXDefectValidator());
         atsValidators.add(new AtsXUserRoleValidator());
         atsValidators.add(new AtsXCommitManagerValidator());
         atsValidators.add(new AtsOperationalImpactValidator());
         atsValidators.add(new AtsOperationalImpactWithWorkaroundValidator());
      }
      return atsValidators;
   }

}
