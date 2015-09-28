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
public class MockLifecycePoint extends AbstractLifecycleVisitor<MockHandler> {

   public static final Type<MockHandler> TYPE = new Type<>();

   private final String a;
   private final String b;

   public MockLifecycePoint(String a, String b) {
      super();
      this.a = a;
      this.b = b;
   }

   @Override
   public Type<MockHandler> getAssociatedType() {
      return TYPE;
   }

   @Override
   protected IStatus dispatch(IProgressMonitor monitor, MockHandler handler, String sourceId) {
      handler.setData(a, b);
      handler.doSomething();
      return handler.getStatus();
   }
}