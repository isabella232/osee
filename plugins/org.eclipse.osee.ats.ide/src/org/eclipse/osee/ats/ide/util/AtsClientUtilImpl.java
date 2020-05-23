/*********************************************************************
 * Copyright (c) 2017 Boeing
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

package org.eclipse.osee.ats.ide.util;

import java.io.File;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.OseeData;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Donald G. Dunne
 */
public class AtsClientUtilImpl implements IAtsClientUtil {

   int atsDevNum = 0;

   @Override
   public int getAtsDeveloperIncrementingNum() {
      try {
         File numFile = OseeData.getFile("atsDevNum.txt");
         if (numFile.exists() && atsDevNum == 0) {
            try {
               atsDevNum = new Integer(Lib.fileToString(numFile).replaceAll("\\s", ""));
            } catch (NumberFormatException ex) {
               OseeLog.log(IAtsClientUtil.class, Level.SEVERE, ex);
            } catch (NullPointerException ex) {
               OseeLog.log(IAtsClientUtil.class, Level.SEVERE, ex);
            }
         }
         atsDevNum++;
         Lib.writeStringToFile(String.valueOf(atsDevNum), numFile);
         return atsDevNum;
      } catch (Exception ex) {
         OseeLog.log(IAtsClientUtil.class, Level.SEVERE, ex);
      }
      return 99;
   }

}
