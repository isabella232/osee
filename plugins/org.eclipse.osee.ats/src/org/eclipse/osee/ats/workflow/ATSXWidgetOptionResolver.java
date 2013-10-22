/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.workflow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.ui.skynet.widgets.util.DefaultXWidgetOptionResolver;
import org.eclipse.osee.framework.ui.skynet.widgets.util.XWidgetRendererItem;

/**
 * @author Donald G. Dunne
 */
public final class ATSXWidgetOptionResolver extends DefaultXWidgetOptionResolver {

   private static ATSXWidgetOptionResolver instance = new ATSXWidgetOptionResolver();
   public final static String OPTIONS_FROM_ATTRIBUTE_VALIDITY = "OPTIONS_FROM_ATTRIBUTE_VALIDITY";

   private ATSXWidgetOptionResolver() {
      // private constructor
   }

   @Override
   public String[] getWidgetOptions(XWidgetRendererItem xWidgetData) {

      if (xWidgetData.getXWidgetName().contains(OPTIONS_FROM_ATTRIBUTE_VALIDITY) || xWidgetData.getXWidgetName().contains(
         "ACTIVE_USER_COMMUNITIES")) {
         Set<String> options;
         try {
            options = AttributeTypeManager.getEnumerationValues(xWidgetData.getStoreName());
         } catch (OseeCoreException ex) {
            options = new HashSet<String>();
            options.add(ex.getLocalizedMessage());
         }
         String optStrs[] = options.toArray(new String[options.size()]);
         Arrays.sort(optStrs);
         return optStrs;
      }
      return super.getWidgetOptions(xWidgetData);
   }

   /**
    * @return the instance
    */
   public static ATSXWidgetOptionResolver getInstance() {
      return instance;
   }

}
