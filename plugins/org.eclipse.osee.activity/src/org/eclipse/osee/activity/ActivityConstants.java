/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.activity;

/**
 * @author Ryan D. Brooks
 */
public final class ActivityConstants {

   private ActivityConstants() {
      // Utility class
   }

   public static final String NAMESPACE = "osee.activity.log";

   private static String qualify(String value) {
      return String.format("%s.%s", NAMESPACE, value);
   }

   public static String ACTIVITY_LOGGER__EXECUTOR_ID = qualify("executor");

   public static String ACTIVITY_LOGGER__WRITE_RATE_IN_MILLIS = qualify("write.rate.millis");
   public static String ACTIVITY_LOGGER__STACKTRACE_LINE_COUNT = qualify("stacktrace.line.count");
   public static String ACTIVITY_LOGGER__EXECUTOR_POOL_SIZE = qualify("executor.pool.size");

   public static long DEFAULT_ACTIVITY_LOGGER__WRITE_RATE_IN_MILLIS = 3000L;
   public static int DEFAULT_ACTIVITY_LOGGER__STACKTRACE_LINE_COUNT = 5;
   public static int DEFAULT_ACTIVITY_LOGGER__EXECUTOR_POOL_SIZE = 1;

   public static final String HTTP_HEADER__ACTIVITY_ENTRY_ID = "X-osee-activity-log-entryId";
   public static final String ERROR_MSG__MISSING_ACTIVITY_HEADER =
      "Error [" + ActivityConstants.HTTP_HEADER__ACTIVITY_ENTRY_ID + "] was missing from request http header";

   public static final Long DEFAULT_ACCOUNT_ID = 1896L; // Guest Account Id
   public static final Long DEFAULT_SERVER_ID = 777L;
   public static final Long DEFAULT_CLIENT_ID = 111L;
}