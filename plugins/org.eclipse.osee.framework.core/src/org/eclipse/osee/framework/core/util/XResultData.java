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

package org.eclipse.osee.framework.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.jdk.core.type.CountingMap;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.IHealthStatus;
import org.eclipse.osee.framework.logging.SevereLoggingMonitor;

/**
 * Used to log Info, Warning and Errors to multiple locations (logger, stderr/out and XResultView). Upon completion, a
 * call to report(title) will open results in the ResultsView
 *
 * @author Donald G. Dunne
 */
public class XResultData {

   public static final Pattern ErrorPattern = Pattern.compile("Error: ");
   public static final Pattern WarningPattern = Pattern.compile("Warning: ");
   public List<IResultDataListener> listeners;
   public String title;

   public static enum Type {
      Severe,
      Warning,
      Info;
   }

   private StringBuilder sb;
   private CountingMap<Type> count;

   private boolean enableOseeLog;

   public XResultData() {
      this(true);
   }

   public XResultData(boolean enableOseeLog) {
      super();
      this.enableOseeLog = enableOseeLog;
      clear();
   }

   public XResultData(boolean enableOseeLog, IResultDataListener... listeners) {
      super();
      this.enableOseeLog = enableOseeLog;
      clear();
      if (listeners != null && listeners.length > 0) {
         this.listeners = Collections.getAggregate(listeners);
      }
   }

   public void clear() {
      sb = new StringBuilder();
      count = new CountingMap<>();
   }

   public void addRaw(String str) {
      sb.append(str);
   }

   public void reportSevereLoggingMonitor(SevereLoggingMonitor monitorLog) {
      List<IHealthStatus> stats = monitorLog.getAllLogs();
      for (IHealthStatus stat : new ArrayList<IHealthStatus>(stats)) {
         if (stat.getException() != null) {
            error("Exception: " + Lib.exceptionToString(stat.getException()));
         }
      }
   }

   public void log(IProgressMonitor monitor, String str) {
      log(str);
      if (monitor != null) {
         monitor.setTaskName(str);
      }
   }

   /**
    * Adds string with newline to log
    */
   public void log(String str) {
      logStr(Type.Info, str + "\n");
   }

   public void logf(String formatStr, Object... objs) {
      logStr(Type.Info, String.format(formatStr, objs));
   }

   /**
    * Adds string with newline to log as error
    */
   public void error(String str) {
      logStr(Type.Severe, str + "\n");
   }

   public void errorf(String formatStr, Object... objs) {
      logStr(Type.Severe, String.format(formatStr + "\n", objs));
   }

   /**
    * Adds string with newline to log as warning
    */
   public void warning(String str) {
      logStr(Type.Warning, str + "\n");
   }

   public void warningf(String formatStr, Object... objs) {
      logStr(Type.Warning, String.format(formatStr + "\n", objs));
   }

   public boolean isEmpty() {
      return toString().equals("");
   }

   public void bumpCount(Type type, int byAmt) {
      count.put(type, byAmt);
   }

   public void logStr(Type type, final String str) {
      bumpCount(type, 1);
      String resultStr = "";
      if (type == Type.Warning) {
         resultStr = "Warning: " + str;
      } else if (type == Type.Severe) {
         resultStr = "Error: " + str;
      } else {
         resultStr = str;
      }
      addRaw(resultStr);
      if (listeners != null) {
         for (IResultDataListener listener : listeners) {
            listener.log(type, resultStr);
         }
      }
   }

   public void dispose() {
      // provided for subclass implementation
   }

   @Override
   public String toString() {
      return sb.toString();
   }

   private int getCount(Type type) {
      return count.get(type);
   }

   public int getNumErrors() {
      return getCount(Type.Severe);
   }

   /**
    * XResultData counts number of errors logged with logError, however users can insert their own "Error: " strings to
    * produce errors. This counts based on these occurrences.
    */
   public int getNumErrorsViaSearch() {
      return Lib.getMatcherCount(ErrorPattern, toString());
   }

   /**
    * XResultData counts number of warnings logged with logWarning, however users can insert their own "Error: " strings
    * to produce errors. This counts based on these occurrences.
    */
   public int getNumWarningsViaSearch() {
      return Lib.getMatcherCount(WarningPattern, toString());
   }

   public int getNumWarnings() {
      return getCount(Type.Warning);
   }

   public boolean isEnableOseeLog() {
      return enableOseeLog;
   }

   public void setEnableOseeLog(boolean enableOseeLog) {
      this.enableOseeLog = enableOseeLog;
   }

   public boolean isErrors() {
      return getNumErrors() > 0;
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

}
