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
 * @author Ryan D. Brooks
 * @author Donald G. Dunne
 */
public class UserNotInDatabase extends UserDataStoreException {

   private static final long serialVersionUID = -676895641120036141L;

   public UserNotInDatabase(String message, Throwable cause) {
      super(message, cause);
   }

   public UserNotInDatabase(Throwable cause, String message, Object... args) {
      super(cause, message, args);
   }

   public UserNotInDatabase(Throwable cause) {
      super(cause);
   }

   public UserNotInDatabase(String message, Object... args) {
      super(message, args);
   }
}