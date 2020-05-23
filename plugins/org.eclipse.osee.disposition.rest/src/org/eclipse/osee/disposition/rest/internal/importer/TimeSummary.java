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

package org.eclipse.osee.disposition.rest.internal.importer;

import org.xml.sax.Attributes;

/**
 * @author Andrew M. Finkbeiner
 */
public class TimeSummary extends ElementHandlers {

   public TimeSummary() {
      super("TimeSummary");
   }

   @Override
   public Object createStartElementFoundObject(String uri, String localName, String name, Attributes attributes) {
      TimeSummaryData data = new TimeSummaryData(attributes.getValue("elapsed"), attributes.getValue("endDate"),
         attributes.getValue("milliseconds"), attributes.getValue("startDate"));
      return data;
   }
}
