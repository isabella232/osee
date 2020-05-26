/*********************************************************************
 * Copyright (c) 2013 Boeing
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

package org.eclipse.osee.ats.core.internal.log;

import java.util.Arrays;
import java.util.List;
import org.eclipse.osee.ats.api.user.AtsUser;
import org.eclipse.osee.ats.api.user.IAtsUserService;
import org.eclipse.osee.ats.api.workflow.log.IAtsLog;
import org.eclipse.osee.ats.api.workflow.log.IAtsLogItem;
import org.eclipse.osee.ats.api.workflow.log.ILogStorageProvider;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Donald G. Dunne
 */
public class AtsLogHtml {

   private final IAtsLog atsLog;
   private final boolean showLogTitle;
   private final ILogStorageProvider storeProvider;
   private final IAtsUserService atsUserService;

   public AtsLogHtml(IAtsLog atsLog, ILogStorageProvider storeProvider, IAtsUserService atsUserService, boolean showLogTitle) {
      this.storeProvider = storeProvider;
      this.atsUserService = atsUserService;
      this.showLogTitle = showLogTitle;
      this.atsLog = atsLog;
   }

   public String get() {
      if (atsLog.getLogItems().isEmpty()) {
         return "";
      }
      StringBuffer sb = new StringBuffer();
      if (showLogTitle) {
         sb.append(AHTML.addSpace(1) + AHTML.getLabelStr(AHTML.LABEL_FONT, storeProvider.getLogTitle()));
      }
      sb.append(getTable());
      return sb.toString();
   }

   public String getTable() {
      StringBuilder builder = new StringBuilder();
      List<IAtsLogItem> logItems = atsLog.getLogItems();
      builder.append(AHTML.beginMultiColumnTable(100, 1));
      builder.append(AHTML.addHeaderRowMultiColumnTable(Arrays.asList("Event", "State", "Message", "User", "Date")));
      for (IAtsLogItem item : logItems) {
         builder.append(AHTML.addRowMultiColumnTable(String.valueOf(item.getType()),
            item.getState().equals("") ? "." : item.getState(), item.getMsg().equals("") ? "." : item.getMsg(),
            getUserName(item.getUserId()), item.getDate(DateUtil.MMDDYYHHMM)));
      }
      builder.append(AHTML.endMultiColumnTable());
      return builder.toString();
   }

   private String getUserName(String userId) {
      String name = userId;
      if (atsUserService != null) {
         AtsUser userById = atsUserService.getUserByUserId(userId);
         if (userById != null) {
            String userName = userById.getName();
            if (Strings.isValid(userName)) {
               name = userName;
            }
         }
      }
      return name;
   }

}
