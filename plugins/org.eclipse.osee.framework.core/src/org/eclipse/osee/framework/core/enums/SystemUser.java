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
package org.eclipse.osee.framework.core.enums;

import java.util.Arrays;
import java.util.List;
import org.eclipse.osee.framework.core.data.IUserToken;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.jdk.core.type.Identity;

/**
 * @author Donald G. Dunne
 */
public final class SystemUser {

   // @formatter:off
   public static final IUserToken OseeSystem = TokenFactory.createUserToken("AAABDBYPet4AGJyrc9dY1w", "OSEE System", "", "99999999", false, false, false);
   public static final IUserToken Anonymous = TokenFactory.createUserToken("AAABDi35uzwAxJLISLBZdA", "Anonymous", "", "99999998", false, false, false);
   public static final IUserToken BootStrap = TokenFactory.createUserToken("noguid", "Boot Strap", "bootstrap@osee.org", "bootstrap", true, false, false);
   public static final IUserToken UnAssigned = TokenFactory.createUserToken("AAABDi1tMx8Al92YWMjeRw", "UnAssigned", "", "99999997", true, false, false);
   public static final List<IUserToken> values = Arrays.asList(OseeSystem, Anonymous, BootStrap, UnAssigned);
   // @formatter:on

   private SystemUser() {
      // Constants
   }

   public static List<IUserToken> values() {
      return values;
   }

   public static boolean isSystemUser(Identity<String> identity) {
      return values().contains(identity);
   }

}
