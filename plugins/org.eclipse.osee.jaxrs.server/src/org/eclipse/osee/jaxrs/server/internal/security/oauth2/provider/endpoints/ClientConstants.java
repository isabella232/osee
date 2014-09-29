/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.jaxrs.server.internal.security.oauth2.provider.endpoints;

/**
 * @author Roberto E. Escobar
 */
public final class ClientConstants {

   private ClientConstants() {
      // Constants
   }

   public static final String CLIENT_REGISTRATION__APPLICATION_GUID = "applicationGuid";
   public static final String CLIENT_REGISTRATION__APPLICATION_NAME = "applicationName";
   public static final String CLIENT_REGISTRATION__APPLICATION_DESCRIPTION = "applicationDescription";
   public static final String CLIENT_REGISTRATION__APPLICATION_URI = "applicationUri";
   public static final String CLIENT_REGISTRATION__APPLICATION_REDIRECT_URI = "applicationRedirectUri";
   public static final String CLIENT_REGISTRATION__APPLICATION_LOGO_DATA = "applicationLogoData";
   public static final String CLIENT_REGISTRATION__APPLICATION_LOGO_URI = "applicationLogoUri";

   public static final String CLIENT_REGISTRATION__DECISION_KEY = "applicationRegistrationDecision";
   public static final String CLIENT_REGISTRATION__DECISION_CANCEL = "cancel";
   public static final String CLIENT_REGISTRATION__DECISION_REGISTER = "register";

   public static final String CLIENT_REGISTRATION__IS_CONFIDENTIAL = "applicationConfidential";
   public static final String CLIENT_REGISTRATION__AUDIENCES = "applicationAllowedAudiences";
   public static final String CLIENT_REGISTRATION__ALLOWED_GRANT_TYPES = "applicationAllowedGrantTypes";
   public static final String CLIENT_REGISTRATION__ALLOWED_SCOPES = "applicationAllowedScopes";
   public static final String CLIENT_REGISTRATION__CERTIFICATE = "applicationCertificate";

}