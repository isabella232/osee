/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.framework.jdk.core.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;

/**
 * @author Ryan D. Brooks
 */
public class StringDataSource implements DataSource {

   private final String data;
   private final String name;

   public StringDataSource(String data, String name) {
      super();
      this.data = data;
      this.name = name;
   }

   @Override
   public String getContentType() {
      return "text/plain";
   }

   @Override
   public InputStream getInputStream() {
      return new ByteArrayInputStream(data.getBytes());
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public OutputStream getOutputStream() {
      throw new UnsupportedOperationException();
   }

}
