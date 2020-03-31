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
package org.eclipse.osee.ats.core.validator;

import java.util.Date;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.util.IValueProvider;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWidgetDefinition;
import org.eclipse.osee.ats.api.workdef.WidgetOption;
import org.eclipse.osee.ats.api.workdef.WidgetResult;
import org.eclipse.osee.ats.api.workdef.WidgetStatus;
import org.eclipse.osee.ats.api.workflow.transition.IAtsXWidgetValidator;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Donald G. Dunne
 */
public abstract class AtsXWidgetValidator implements IAtsXWidgetValidator {

   public boolean isTransitionToComplete(IAtsStateDefinition toStateDef) {
      return toStateDef.getStateType().isCompletedState();
   }

   public boolean isRequiredForTransition(IAtsWidgetDefinition widgetDef) {
      return widgetDef.getOptions().contains(WidgetOption.REQUIRED_FOR_TRANSITION);
   }

   public boolean isRequiredForCompletion(IAtsWidgetDefinition widgetDef) {
      return widgetDef.getOptions().contains(WidgetOption.REQUIRED_FOR_COMPLETION);
   }

   public boolean isEmptyValue(IValueProvider provider) {
      return provider.isEmpty();
   }

   public WidgetResult validateWidgetIsRequired(IValueProvider provider, IAtsWidgetDefinition widgetDef, IAtsStateDefinition fromStateDef, IAtsStateDefinition toStateDef) {
      if (isRequiredForTransition(widgetDef) && isEmptyValue(provider)) {
         return new WidgetResult(WidgetStatus.Invalid_Incompleted, "[%s] is required for transition", widgetDef.getName());
      } else if (isTransitionToComplete(toStateDef) && isRequiredForCompletion(widgetDef) && isEmptyValue(provider)) {
         return new WidgetResult(WidgetStatus.Invalid_Incompleted, "[%s] is required for transition to [%s]", widgetDef.getName(),
            toStateDef.getName());
      }
      return WidgetResult.Valid;
   }

   @Override
   public abstract WidgetResult validateTransition(IAtsWorkItem workItem, IValueProvider valueProvider, IAtsWidgetDefinition widgetDef, IAtsStateDefinition fromStateDef, IAtsStateDefinition toStateDef, AtsApi atsApi);

   public WidgetResult isValidDate(IValueProvider valueProvider, IAtsWidgetDefinition widgetDef) {
      for (Date date : valueProvider.getDateValues()) {
         if (widgetDef.is(WidgetOption.FUTURE_DATE_REQUIRED)) {
            if (date.before(new Date())) {
               return new WidgetResult(WidgetStatus.Invalid_Range, "[%s] value [%s] must be in future", valueProvider.getName(),
                  DateUtil.get(date, DateUtil.MMDDYYHHMM));
            }
         }
      }
      return WidgetResult.Valid;
   }

   public WidgetResult isValidFloat(IValueProvider valueProvider, IAtsWidgetDefinition widgetDef) {
      for (Object obj : valueProvider.getValues()) {
         if (obj instanceof Double) {
            return WidgetResult.Valid;
         }
         if (obj instanceof String) {
            String attrStr = (String) obj;
            if (attrStr.matches("[-+]?\\d*\\.?\\d*")) {
               WidgetResult result = checkValid(widgetDef, Double.parseDouble(attrStr), valueProvider.getName());
               if (!result.isValid()) {
                  return result;
               }
            } else {
               return new WidgetResult(WidgetStatus.Invalid_Type, "[%s] value [%s] is not a valid float", valueProvider.getName(),
                  attrStr);
            }
         }
      }
      return WidgetResult.Valid;
   }

   public WidgetResult isValidInteger(IValueProvider valueProvider, IAtsWidgetDefinition widgetDef) {
      for (Object obj : valueProvider.getValues()) {
         if (obj instanceof Integer) {
            return WidgetResult.Valid;
         }
         if (obj instanceof String) {
            String attrStr = (String) obj;
            if (Strings.isValid(attrStr)) {
               WidgetResult result = checkValid(widgetDef, Integer.parseInt(attrStr), valueProvider.getName());
               if (!result.isValid()) {
                  return result;
               }
            } else {
               return new WidgetResult(WidgetStatus.Invalid_Type, "[%s] value [%s] is not a valid integer", valueProvider.getName(),
                  attrStr);
            }
         }
      }
      return WidgetResult.Valid;
   }

   private WidgetResult checkValid(IAtsWidgetDefinition widgetDef, double value, String valueProviderName) {
      Double minValue = widgetDef.getMin();
      Double maxValue = widgetDef.getMax();

      if (minValue != null && Lib.lessThan(value, minValue)) {
         return new WidgetResult(WidgetStatus.Invalid_Range, "[%s] value [%s] must be >= [%s]", valueProviderName,
            value, minValue);
      } else if (maxValue != null && Lib.greaterThan(value, maxValue)) {
         return new WidgetResult(WidgetStatus.Invalid_Range, "[%s] value [%s] must be <= [%s]", valueProviderName,
            value, minValue, maxValue);
      }

      return WidgetResult.Valid;
   }

   public WidgetResult isValidList(IValueProvider valueProvider, IAtsWidgetDefinition widgetDef) {
      return checkValid(widgetDef, valueProvider.getValues().size(), valueProvider.getName());
   }

}
