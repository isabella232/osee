/*********************************************************************
 * Copyright (c) 2016 Boeing
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

package org.eclipse.osee.framework.core.data;

/**
 * @author Donald G. Dunne
 */
public class OrcsTypesData {

   /**
    * This value should change and the corresponding entries made in the Tuple2 table that maps this version to the Uri
    * attribute ids of the types to load. This provides for production code to access an over version of the types
    * model/sheets while the current code base works of this specified version.</br>
    * </br>
    * Version 1 = Corresponds to 0.23.1 and earlier code base</br>
    * Version 2 = Corresponds to 0.24.0 code base
    */
   public static final Long OSEE_TYPE_VERSION = 3L; // Corresponds to 0.25.0 code base
}