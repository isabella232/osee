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

package org.eclipse.osee.ats.api.workdef;

import java.util.Map;
import org.eclipse.osee.framework.core.data.AttributeTypeToken;

/**
 * @author Donald G. Dunne
 */
public interface IAtsWidgetDefinition extends IAtsLayoutItem {

   @Override
   public String getName();

   public String getToolTip();

   public String getDescription();

   public String getDefaultValue();

   public boolean is(WidgetOption widgetOption);

   public IAtsWidgetOptionHandler getOptions();

   public void setConstraint(double min, double max);

   public Double getMax();

   public Double getMin();

   public void set(WidgetOption widgetOption);

   public String getXWidgetName();

   public void setXWidgetName(String xWidgetName);

   public int getHeight();

   public void setHeight(int height);

   @Override
   public String toString();

   public AttributeTypeToken getAttributeType();

   public void addParameter(String key, Object obj);

   public Object getParameter(String key);

   Map<String, Object> getParameters();

}