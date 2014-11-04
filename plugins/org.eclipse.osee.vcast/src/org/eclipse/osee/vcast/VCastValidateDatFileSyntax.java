/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.vcast;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.framework.core.util.Result;

/**
 * @author Shawn F. Cook
 */
public class VCastValidateDatFileSyntax {
   private final static Pattern threeNumbersPattern = Pattern.compile("\\s*([0-9]+)\\s+([0-9]+)\\s+([0-9]+)");

   /**
    * Verify a line of text from the VCast DAT file which for statement coverage (Level C) should be of the format:<br>
    * [file#] [method#] [execLine#]<br>
    * Where file# can be looked up in the file's XML file, and method# and execLine# can be looked up in the file's LIS
    * (or "list") file.
    *
    * @param line
    * @return TRUE if line is valid. FALSE if line is invalid - an reason will be provided in the Result object.
    */
   public static Result validateDatFileSyntax(String line) {
      StringTokenizer st = new StringTokenizer(line);

      if (st.countTokens() > 3) {
         return new Result(
            false,
            "VCastVerifyDatFileSyntax.validateDatFileSyntax() - WARNING: DAT file line has to many parameters: [" + line + "]");
      }

      Matcher threeNumbersMatcher = threeNumbersPattern.matcher(line);
      if (threeNumbersMatcher.groupCount() != 3) {
         return new Result(
            false,
            "VCastVerifyDatFileSyntax.validateDatFileSyntax() - WARNING: DAT file line has 1 or more parameters that are not numeric: [" + line + "]");
      }
      return Result.TrueResult;
   }
}
