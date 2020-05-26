/*********************************************************************
 * Copyright (c) 2020 Boeing
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

package org.eclipse.osee.framework.jdk.core.util.io;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

/**
 * @author Dominic Guss
 */
public class FileParser {
   /**
    * Retrieves the value associated with its unique key in a configuration file that utilizes ': ' to separate the
    * key/value pair.
    */
   public static String getValueFromKey(String key, String filePath) {
      String value = "";
      try (Scanner scanner = new Scanner(new FileReader(filePath))) {
         String line;
         while (scanner.hasNext()) {
            line = scanner.nextLine();
            if (line.startsWith(key)) {
               value = line.split(": ")[1].trim();
               break;
            }
         }
         scanner.close();
      } catch (FileNotFoundException fnfe) {
         throw new IllegalArgumentException(filePath + " is not valid file path.");
      }
      return value;
   }
}
