/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.data;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Donald G. Dunne
 */
@XmlRootElement
public class OrcsTypesData {

   /**
    * This value should change and the corresponding entries made in the Tuple2 table that maps this version to the Uri
    * attribute ids of the types to load. This provides for production code to access an over version of the types
    * model/sheets while the current code base works of this specified version.</br>
    * </br>
    * Version 1 = Corresponds to 0.23.1 and earlier code base
    */
   public static final Long OSEE_TYPE_VERSION = 2L; // Corresponds to 0.24.0 code base

   private List<OrcsTypeSheet> sheets = new ArrayList<>();

   public List<OrcsTypeSheet> getSheets() {
      return sheets;
   }

   public void setSheets(List<OrcsTypeSheet> sheets) {
      this.sheets = sheets;
   }

}
