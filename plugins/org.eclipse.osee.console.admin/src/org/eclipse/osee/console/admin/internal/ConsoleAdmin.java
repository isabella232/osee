/*********************************************************************
 * Copyright (c) 2012 Boeing
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

package org.eclipse.osee.console.admin.internal;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.osee.console.admin.ConsoleCommand;
import org.eclipse.osee.framework.core.executor.ExecutorAdmin;
import org.eclipse.osee.logger.Log;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Roberto E. Escobar
 */
public class ConsoleAdmin {

   private final Map<ConsoleCommand, Map<String, String>> pending = new ConcurrentHashMap<>();

   private Log logger;
   private ExecutorAdmin executorAdmin;
   private CommandDispatcher dispatcher;
   private Thread thread;
   private ServiceRegistration<?> cmdRef;

   public void setLogger(Log logger) {
      this.logger = logger;
   }

   public void setExecutorAdmin(ExecutorAdmin executorAdmin) {
      this.executorAdmin = executorAdmin;
   }

   public ExecutorAdmin getExecutorAdmin() {
      return executorAdmin;
   }

   public Log getLogger() {
      return logger;
   }

   public void start(BundleContext context) throws Exception {
      dispatcher = new CommandDispatcher(getLogger(), getExecutorAdmin());
      thread = new Thread("Register Pending Osee Console Commands") {
         @Override
         public void run() {
            for (Entry<ConsoleCommand, Map<String, String>> entry : pending.entrySet()) {
               register(entry.getKey(), entry.getValue());
            }
            pending.clear();
         }
      };
      thread.start();
      CommandProviderImpl command = new CommandProviderImpl();
      command.setConsoleAdmin(this);
      cmdRef = context.registerService(CommandProvider.class.getName(), command, null);
   }

   public void stop(BundleContext context) {
      if (thread != null && thread.isAlive()) {
         thread.interrupt();
      }
      if (cmdRef != null) {
         cmdRef.unregister();
         cmdRef = null;
      }
   }

   private boolean isReady() {
      return getDispatcher() != null && getLogger() != null;
   }

   public void addCommand(ConsoleCommand reference, Map<String, String> props) {
      if (isReady()) {
         register(reference, props);
      } else {
         pending.put(reference, props);
      }
   }

   public void removeCommand(ConsoleCommand reference, Map<String, String> props) {
      if (isReady()) {
         unregister(reference, props);
      } else {
         pending.remove(reference);
      }
   }

   private void unregister(ConsoleCommand reference, Map<String, String> props) {
      String componentName = ConsoleAdminUtils.getComponentName(props);
      String contextName = ConsoleAdminUtils.getContextName(props);

      getDispatcher().unregister(componentName);
      getLogger().debug("De-registering command for [%s] with alias [%s]", componentName, contextName);
   }

   private void register(ConsoleCommand consoleCommand, Map<String, String> props) {
      String componentName = ConsoleAdminUtils.getComponentName(props);
      String contextName = ConsoleAdminUtils.getContextName(props);
      try {
         getDispatcher().register(componentName, consoleCommand);
         getLogger().debug("Registered command for [%s] with alias [%s]", componentName, contextName);
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
   }

   public CommandDispatcher getDispatcher() {
      return dispatcher;
   }
}
