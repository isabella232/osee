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
package org.eclipse.osee.framework.core.enums;

import org.eclipse.osee.framework.core.data.IOseeBranch;

/**
 * @author Donald G. Dunne
 */
public final class DemoBranches {

   public static final IOseeBranch SAW_Bld_1 = IOseeBranch.create(3, "SAW_Bld_1");
   public static final IOseeBranch SAW_Bld_2 = IOseeBranch.create(5, "SAW_Bld_2");
   public static final IOseeBranch SAW_Bld_3 = IOseeBranch.create("SAW_Bld_3");

   public static final IOseeBranch CIS_Bld_1 = IOseeBranch.create(4, "CIS_Bld_1");
   public static final IOseeBranch CIS_Bld_2 = IOseeBranch.create("CIS_Bld_2");
   public static final IOseeBranch CIS_Bld_3 = IOseeBranch.create("CIS_Bld_3");

   private DemoBranches() {
      // Constants
   }
}