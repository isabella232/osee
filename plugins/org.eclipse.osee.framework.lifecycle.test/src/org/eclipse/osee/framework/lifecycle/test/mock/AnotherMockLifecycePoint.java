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
package org.eclipse.osee.framework.lifecycle.test.mock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osee.framework.lifecycle.AbstractLifecycleVisitor;

/**
 * @author Roberto E. Escobar
 * @author Jeff C. Phillips
 */
public class AnotherMockLifecycePoint extends AbstractLifecycleVisitor<NonRunHandler> {

   public static final Type<NonRunHandler> TYPE = new Type<>();

   @Override
   public Type<NonRunHandler> getAssociatedType() {
      return TYPE;
   }

   @Override
   protected IStatus dispatch(IProgressMonitor monitor, NonRunHandler handler, String sourceId) {
      return handler.onCheck(monitor);
   }
}