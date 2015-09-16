/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.workdef;

/**
 * @author Donald G. Dunne
 */
public class JaxAtsWorkDef {

   private String name;
   private String workDefDsl;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getWorkDefDsl() {
      return workDefDsl;
   }

   public void setWorkDefDsl(String workDefDsl) {
      this.workDefDsl = workDefDsl;
   }

}
