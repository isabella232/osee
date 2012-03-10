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
package org.eclipse.osee.framework.core.server.internal.console;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.console.admin.Console;
import org.eclipse.osee.console.admin.ConsoleCommand;
import org.eclipse.osee.console.admin.ConsoleParameters;
import org.eclipse.osee.framework.core.server.IApplicationServerManager;
import org.eclipse.osee.framework.core.server.ISessionManager;
import org.eclipse.osee.framework.core.server.OseeServerProperties;
import org.eclipse.osee.framework.database.core.DatabaseInfoManager;
import org.eclipse.osee.framework.jdk.core.util.Lib;

/**
 * @author Roberto E. Escobar
 */
public class ServerStatsCommand implements ConsoleCommand {

   private IApplicationServerManager appManager;
   private ISessionManager sessionManager;

   public void setApplicationServerManager(IApplicationServerManager appManager) {
      this.appManager = appManager;
   }

   public void setSessionManager(ISessionManager sessionManager) {
      this.sessionManager = sessionManager;
   }

   private IApplicationServerManager getApplicationServerManager() {
      return appManager;
   }

   private ISessionManager getSessionManager() {
      return sessionManager;
   }

   @Override
   public String getName() {
      return "server_status";
   }

   @Override
   public String getDescription() {
      return "Displays server status information";
   }

   @Override
   public String getUsage() {
      return "";
   }

   @Override
   public Callable<?> createCallable(Console console, ConsoleParameters params) {
      return new ServerStatsCallable(getApplicationServerManager(), getSessionManager(), console);
   }

   private static final class ServerStatsCallable implements Callable<Boolean> {
      private final IApplicationServerManager manager;
      private final ISessionManager sessionManager;
      private final Console console;

      public ServerStatsCallable(IApplicationServerManager manager, ISessionManager sessionManager, Console console) {
         super();
         this.manager = manager;
         this.sessionManager = sessionManager;
         this.console = console;
      }

      @Override
      public Boolean call() throws Exception {

         console.writeln("\n----------------------------------------------");
         console.writeln("                  Server Stats");
         console.writeln("----------------------------------------------");

         console.writeln("Server:[%s:%s]", manager.getServerAddress(), manager.getPort());
         console.writeln("Id: [%s]", manager.getId());
         console.writeln("Running Since: [%s]\n",
            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(manager.getDateStarted()));

         console.writeln("Code Base Location: [%s]", System.getProperty("user.dir"));
         console.writeln("Datastore: [%s]", DatabaseInfoManager.getDefault().toString());
         console.writeln("Binary Data Path: [%s]", OseeServerProperties.getOseeApplicationServerData(null));
         console.writeln();

         console.writeln("Supported Versions: %s", Arrays.deepToString(manager.getSupportedVersions()));
         console.writeln("Accepting Requests: [%s]", manager.isAcceptingRequests());
         console.writeln(Lib.getMemoryInfo());

         logServlets(manager);

         console.writeln("\nSessionsManaged: [%s]", sessionManager.getAllSessions(false).size());
         console.writeln("\nServer State: [%s]", manager.isSystemIdle() ? "IDLE" : "BUSY");
         console.writeln("Active Threads: [%s]", manager.getNumberOfActiveThreads());

         IJobManager jobManager = Job.getJobManager();
         console.writeln("Job Manager: [%s]", jobManager.isIdle() ? "IDLE" : "BUSY");

         Job current = jobManager.currentJob();

         console.writeln("Current Job: [%s]", current != null ? current.getName() : "NONE");

         console.write("Current Tasks: ");
         List<String> entries = manager.getCurrentProcesses();
         if (entries.isEmpty()) {
            console.writeln("[NONE]");
         } else {
            console.writeln();
            for (int index = 0; index < entries.size(); index++) {
               console.writeln("\t[%s] - %s", index, entries.get(index));
            }
         }
         return Boolean.TRUE;
      }

      private void logServlets(IApplicationServerManager manager) {
         console.writeln("Servlets:");
         List<String> contexts = new ArrayList<String>(manager.getRegisteredServlets());
         Collections.sort(contexts);
         if (contexts.size() % 2 == 1) {
            contexts.add("");
         }
         int midPoint = contexts.size() / 2;
         for (int i = 0; i < midPoint; i++) {
            console.writeln("%-40.40s%s", contexts.get(i), contexts.get(i + midPoint));
         }
      }

   }

}
