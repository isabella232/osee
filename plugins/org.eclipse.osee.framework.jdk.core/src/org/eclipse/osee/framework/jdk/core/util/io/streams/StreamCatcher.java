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
package org.eclipse.osee.framework.jdk.core.util.io.streams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jeff C. Phillips
 */
@Deprecated
public class StreamCatcher extends Thread {
   private final InputStream is;
   private final String type;
   private final Logger logger;

   public StreamCatcher(InputStream is, String type) {
      this(is, type, null);
   }

   public StreamCatcher(InputStream is, String type, Logger logger) {
      this.is = is;
      this.type = type;
      this.logger = logger;
   }

   @Override
   public void run() {
      try {
         InputStreamReader isr = new InputStreamReader(is);
         BufferedReader br = new BufferedReader(isr);
         String line = null;
         StringBuilder loggerError = new StringBuilder();

         while ((line = br.readLine()) != null) {

            if (logger == null) {
               System.out.println(type + ">" + line);
            } else {
               loggerError.append(line);
               loggerError.append("\n");
            }
         }

         if (logger != null && loggerError.length() > 0) {
            logger.log(Level.SEVERE, loggerError.toString());
         }

      } catch (IOException ioe) {
         ioe.printStackTrace();
      }
   }
}
