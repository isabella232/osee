/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.database.init;

/**
 * @author Roberto E. Escobar
 */
public enum DefaultOseeTypeDefinitions {

   OSEE_BASE_TYPES("org.eclipse.osee.framework.skynet.core.OseeTypes_Framework"),
   DEFINE_TYPES("org.eclipse.osee.ote.define.OseeTypesOTE"),
   ATS_TYPES("org.eclipse.osee.ats.OseeTypes_ATS");

   private String extensionId;

   private DefaultOseeTypeDefinitions(String extensionId) {
      this.extensionId = extensionId;
   }

   public String getExtensionId() {
      return extensionId;
   }
}
