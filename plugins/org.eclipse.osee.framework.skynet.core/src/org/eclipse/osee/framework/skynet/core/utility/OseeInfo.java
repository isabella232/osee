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
package org.eclipse.osee.framework.skynet.core.utility;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;

/**
 * @author Donald G. Dunne
 */
public class OseeInfo {
   private static final String GET_VALUE_SQL = "Select OSEE_VALUE FROM osee_info where OSEE_KEY = ?";
   private static final String INSERT_KEY_VALUE_SQL = "INSERT INTO osee_info (OSEE_KEY, OSEE_VALUE) VALUES (?, ?)";
   private static final String DELETE_KEY_SQL = "DELETE FROM osee_info WHERE OSEE_KEY = ?";
   public static final String SAVE_OUTFILE_IN_DB = "SAVE_OUTFILE_IN_DB";
   public static final String USE_GUID_STORAGE = "osee.framework.skynet.core.guid.storage";
   // This is a unique identifier generated upon database initialization and should never be changed once it has been created.
   public static final String DB_ID_KEY = "osee.db.guid";
   public static final String DB_TYPE_KEY = "osee.db.type";

   private static Map<String, Pair<Long, String>> cache = new ConcurrentHashMap<String, Pair<Long, String>>();

   public static String getValue(String key) throws OseeCoreException {
      return getValue(key, (long) Integer.MAX_VALUE);
   }

   public static String getValue(String key, Long maxStaleness) throws OseeCoreException {
      return getValue(ServiceUtil.getOseeDatabaseService(), key, maxStaleness);
   }

   public static String getValue(IOseeDatabaseService service, String key) {
      return getValue(service, key, (long) Integer.MAX_VALUE);
   }

   public static String getValue(IOseeDatabaseService service, String key, Long maxStaleness) {
      Pair<Long, String> pair = cache.get(key);
      String value;
      if (pair == null || pair.getFirst() + maxStaleness < System.currentTimeMillis()) {
         value = service.runPreparedQueryFetchObject("", GET_VALUE_SQL, key);
         cacheValue(key, value);
      } else {
         value = pair.getSecond();
      }

      return value;
   }

   public static void setValue(String key, String value) throws OseeCoreException {
      ConnectionHandler.runPreparedUpdate(DELETE_KEY_SQL, key);
      ConnectionHandler.runPreparedUpdate(INSERT_KEY_VALUE_SQL, key, value);
      cacheValue(key, value);
   }

   public static String getDatabaseGuid() throws OseeCoreException {
      return getValue(DB_ID_KEY);
   }

   private static void cacheValue(String key, String value) {
      Long time = System.currentTimeMillis();
      cache.put(key, new Pair<Long, String>(time, value));
   }
}