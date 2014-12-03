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
package org.eclipse.osee.framework.core.server.test.mocks;

import org.eclipse.osee.framework.core.server.internal.BuildTypeIdentifier.BuildTypeDataProvider;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Roberto E. Escobar
 */
public class MockBuildTypeDataProvider implements BuildTypeDataProvider {

   private final String data;
   private final boolean isErrorOnGet;

   public MockBuildTypeDataProvider(String data, boolean isErrorOnGet) {
      this.data = data;
      this.isErrorOnGet = isErrorOnGet;
   }

   @Override
   public String getData() {
      if (isErrorOnGet) {
         throw new OseeCoreException("Error case set");
      }
      return data;
   }
}