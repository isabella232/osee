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
 * @author Donald G. Dunne
 */
public class AttributeDoesNotExist extends OseeCoreException {

   private static final long serialVersionUID = 1L;

   public AttributeDoesNotExist(String message, Object... args) {
      super(message, args);
   }

   public AttributeDoesNotExist(String message, Throwable cause) {
      super(message, cause);
   }

   public AttributeDoesNotExist(Throwable cause, String message, Object... args) {
      super(cause, message, args);
   }

   public AttributeDoesNotExist(Throwable cause) {
      super(cause);
   }
}