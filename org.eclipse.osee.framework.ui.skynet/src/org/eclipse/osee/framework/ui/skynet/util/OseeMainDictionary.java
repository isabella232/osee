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
package org.eclipse.osee.framework.ui.skynet.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;

/**
 * @author Donald G. Dunne
 */
public class OseeMainDictionary implements IOseeDictionary {

   Set<String> dict;

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.osee.framework.ui.skynet.util.IOseeDictionary#isWord(java.lang.String)
    */
   public boolean isWord(String word) {
      if (dict == null) loadDictionary();
      boolean contains = dict.contains(word);
      return contains;
   }

   private void loadDictionary() {
      dict = new HashSet<String>();
      String line;
      InputStream is = null;
      BufferedReader br = null;

      // open OSEE english dictionary
      try {
         is = SkynetGuiPlugin.getInstance().getBundle().getEntry("support/spellCheck/AllWords.txt").openStream();
         br = new BufferedReader(new InputStreamReader(is));
         while ((line = br.readLine()) != null) {
            line.replaceAll("[\t\r\n ]+$", "");
            dict.add(line);
         }
         is.close();
      } catch (Exception ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, false);
      }
   }

}
