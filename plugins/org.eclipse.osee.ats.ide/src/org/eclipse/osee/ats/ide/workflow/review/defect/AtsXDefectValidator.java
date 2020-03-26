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
package org.eclipse.osee.ats.ide.workflow.review.defect;

import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.util.IValueProvider;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWidgetDefinition;
import org.eclipse.osee.ats.api.workdef.WidgetResult;
import org.eclipse.osee.ats.core.review.ReviewDefectError;
import org.eclipse.osee.ats.core.review.ReviewDefectManager;
import org.eclipse.osee.ats.core.validator.AtsXWidgetValidator;

/**
 * @author Donald G. Dunne
 */
public class AtsXDefectValidator extends AtsXWidgetValidator {

   public static String WIDGET_NAME = "XDefectViewer";

   @Override
   public WidgetResult validateTransition(IAtsWorkItem workItem, IValueProvider provider, IAtsWidgetDefinition widgetDef, IAtsStateDefinition fromStateDef, IAtsStateDefinition toStateDef, AtsApi atsServices) {
      WidgetResult result = WidgetResult.Valid;
      if (WIDGET_NAME.equals(widgetDef.getXWidgetName())) {
         ReviewDefectManager mgr = new ReviewDefectManager(provider);
         ReviewDefectError error = ReviewDefectValidator.isValid(mgr.getDefectItems());
         return error.toWidgetResult(widgetDef);
      }
      return result;
   }
}
