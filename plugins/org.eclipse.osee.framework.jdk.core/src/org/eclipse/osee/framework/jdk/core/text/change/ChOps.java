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

package org.eclipse.osee.framework.jdk.core.text.change;

/**
 * @author Ryan D. Brooks
 */
public class ChOps {

   /**
    * take a string containing one or more "\n" and replace with a new line char then replace all "\\n" with "\n"
    */
   public static char[] embedNewLines(char[] src, int srcStartIndex, int srcEndIndex) {
      int stop = srcEndIndex - 1; //don't loop for the last char so we can use src[i+1]
      int replaceCount = 0;
      for (int i = srcStartIndex; i < stop; i++) {
         if (src[i] == '\\') {
            i++;
            if (src[i] == '\\' || src[i] == 'n') {
               replaceCount++;
            }
         }
      }

      char[] result = new char[srcEndIndex - srcStartIndex - replaceCount];

      int srcPos = srcStartIndex;
      for (int i = 0; i < result.length; i++) {
         if (src[srcPos] == '\\') {
            if (src[srcPos + 1] == '\\') {
               srcPos += 2; // skip over the two escaped chars
               result[i] = '\\'; // and replace them here
            } else if (src[srcPos + 1] == 'n') {
               srcPos += 2; // skip over the two escaped chars
               result[i] = '\n'; // and replace them here
            } else {
               result[i] = src[srcPos++];
            }
         } else {
            result[i] = src[srcPos++];
         }
      }
      return result;
   }
}
