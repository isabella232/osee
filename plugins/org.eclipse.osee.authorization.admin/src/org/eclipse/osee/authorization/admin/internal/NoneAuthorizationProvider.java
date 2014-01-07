/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.authorization.admin.internal;

import java.security.Principal;
import org.eclipse.osee.authorization.admin.Authority;
import org.eclipse.osee.authorization.admin.AuthorizationConstants;
import org.eclipse.osee.authorization.admin.AuthorizationData;
import org.eclipse.osee.authorization.admin.AuthorizationProvider;
import org.eclipse.osee.authorization.admin.AuthorizationRequest;

/**
 * @author Roberto E. Escobar
 */
public class NoneAuthorizationProvider implements AuthorizationProvider, AuthorizationData, Authority {

   @Override
   public String getScheme() {
      return AuthorizationConstants.NONE_AUTHORIZATION_PROVIDER;
   }

   @Override
   public Principal getPrincipal() {
      return null;
   }

   @Override
   public Authority getAuthority() {
      return this;
   }

   @Override
   public AuthorizationData authorize(AuthorizationRequest request) {
      return this;
   }

   @Override
   public boolean isInRole(String role) {
      return true;
   }

}
