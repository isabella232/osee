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
package org.eclipse.osee.framework.core.exception;


/**
 * @author Roberto E. Escobar
 */
public class OseeAuthenticationRequiredException extends OseeDataStoreException {

   private static final long serialVersionUID = 1890728724625261131L;

   public OseeAuthenticationRequiredException(String message, Object... args) {
      super(message, args);
   }

   public OseeAuthenticationRequiredException(String message, Throwable cause) {
      super(message, cause);
   }

   public OseeAuthenticationRequiredException(Throwable cause, String message, Object... args) {
      super(cause, message, args);
   }

   public OseeAuthenticationRequiredException(Throwable cause) {
      super(cause);
   }
}