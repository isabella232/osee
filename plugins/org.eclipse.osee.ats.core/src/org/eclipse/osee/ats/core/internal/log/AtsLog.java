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

package org.eclipse.osee.ats.core.internal.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workflow.log.IAtsLog;
import org.eclipse.osee.ats.api.workflow.log.IAtsLogItem;
import org.eclipse.osee.ats.api.workflow.log.LogType;
import org.eclipse.osee.ats.core.AtsCore;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Donald G. Dunne
 */
public class AtsLog implements IAtsLog {

   private boolean dirty = false;
   private final List<IAtsLogItem> logItems = new ArrayList<IAtsLogItem>();
   private String logId = "none";

   @Override
   public String toString() {
      try {
         return org.eclipse.osee.framework.jdk.core.util.Collections.toString("\n", getLogItems());
      } catch (Exception ex) {
         OseeLog.log(AtsCore.class, Level.SEVERE, ex);
         return ex.getLocalizedMessage();
      }
   }

   @Override
   public Date getLastStatusDate() throws OseeCoreException {
      IAtsLogItem logItem = getLastEvent(LogType.Metrics);
      if (logItem == null) {
         return null;
      }
      return logItem.getDate();
   }

   @Override
   public List<IAtsLogItem> getLogItemsReversed() throws OseeCoreException {
      List<IAtsLogItem> logItems = new ArrayList<IAtsLogItem>(getLogItems());
      Collections.reverse(logItems);
      return logItems;
   }

   /**
    * Used to reset the original originated user. Only for internal use. Kept for backward compatibility.
    */
   @Override
   public void internalResetOriginator(IAtsUser user) throws OseeCoreException {
      List<IAtsLogItem> logItems = getLogItems();
      for (IAtsLogItem item : logItems) {
         if (item.getType() == LogType.Originated) {
            item.setUserId(user.getUserId());
            dirty = true;
            return;
         }
      }
   }

   /**
    * Used to reset the original originated user. Only for internal use. Kept for backward compatibility.
    */
   @Override
   public void internalResetCreatedDate(Date date) throws OseeCoreException {
      List<IAtsLogItem> logItems = getLogItems();
      for (IAtsLogItem item : logItems) {
         if (item.getType() == LogType.Originated) {
            item.setDate(date);
            dirty = true;
            return;
         }
      }
   }

   @Override
   public String internalGetCancelledReason() throws OseeCoreException {
      IAtsLogItem item = getStateEvent(LogType.StateCancelled);
      if (item == null) {
         return "";
      }
      return item.getMsg();
   }

   /**
    * This method is replaced by workItem.getCompletedFromState. Kept for backward compatibility.
    */
   @Override
   public String internalGetCompletedFromState() throws OseeCoreException {
      IAtsLogItem item = getStateEvent(LogType.StateComplete);
      if (item == null) {
         return "";
      }
      return item.getState();
   }

   @Override
   public IAtsLogItem addLogItem(IAtsLogItem item) throws OseeCoreException {
      return addLog(item.getType(), item.getState(), item.getMsg(), item.getDate(), item.getUserId());
   }

   @Override
   public IAtsLogItem addLog(LogType type, String state, String msg, String userId) throws OseeCoreException {
      return addLog(type, state, msg, new Date(), userId);
   }

   @Override
   public IAtsLogItem addLog(LogType type, String state, String msg, Date date, String userId) throws OseeCoreException {
      LogItem logItem = new LogItem(type, date, userId, state, msg);
      List<IAtsLogItem> logItems = getLogItems();
      logItems.add(logItem);
      dirty = true;
      return logItem;
   }

   @Override
   public void clearLog() {
      logItems.clear();
      dirty = true;
   }

   @Override
   public IAtsLogItem getLastEvent(LogType type) throws OseeCoreException {
      for (IAtsLogItem item : getLogItemsReversed()) {
         if (item.getType() == type) {
            return item;
         }
      }
      return null;
   }

   @Override
   public IAtsLogItem getStateEvent(LogType type, String stateName) throws OseeCoreException {
      for (IAtsLogItem item : getLogItemsReversed()) {
         if (item.getType() == type && item.getState().equals(stateName)) {
            return item;
         }
      }
      return null;
   }

   @Override
   public IAtsLogItem getStateEvent(LogType type) throws OseeCoreException {
      for (IAtsLogItem item : getLogItemsReversed()) {
         if (item.getType() == type) {
            return item;
         }
      }
      return null;
   }

   @Override
   public List<IAtsLogItem> getLogItems() throws OseeCoreException {
      return logItems;
   }

   @Override
   public boolean isDirty() {
      return dirty;
   }

   @Override
   public void setDirty(boolean dirty) {
      this.dirty = dirty;
   }

   @Override
   public void setLogId(String logId) {
      this.logId = logId;
   }

   public String getLogId() {
      return logId;
   }

}