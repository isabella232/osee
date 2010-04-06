/*
 * Created on Apr 5, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.skynet.core.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.messaging.event.res.IFrameworkEvent;
import org.eclipse.osee.framework.skynet.core.event.artifact.ArtifactEventManager;
import org.eclipse.osee.framework.skynet.core.event.artifact.EventBasicGuidArtifact;
import org.eclipse.osee.framework.skynet.core.event.artifact.EventModType;
import org.eclipse.osee.framework.skynet.core.event.artifact.EventModifiedBasicGuidArtifact;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.ui.plugin.event.UnloadedArtifact;

/**
 * Internal implementation of OSEE Event Manager that should only be accessed from RemoteEventManager and
 * OseeEventManager classes.
 * 
 * @author Donald G. Dunne
 */
public class InternalEventManager2 {

   private static final List<IEventListener> priorityListeners = new CopyOnWriteArrayList<IEventListener>();
   private static final List<IEventListener> listeners = new CopyOnWriteArrayList<IEventListener>();
   public static final Collection<UnloadedArtifact> EMPTY_UNLOADED_ARTIFACTS = Collections.emptyList();
   private static boolean disableEvents = false;

   private static final ThreadFactory threadFactory = new OseeEventThreadFactory("Osee Events2");
   private static final ExecutorService executorService =
         Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);

   // This will disable all Local TransactionEvents and enable loopback routing of Remote TransactionEvents back
   // through the RemoteEventService as if they came from another client.  This is for testing purposes only and
   // should be reset to false before release.
   private static boolean enableRemoteEventLoopback = false;

   private static final boolean DEBUG = true;

   //         "TRUE".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.osee.framework.skynet.core/debug/Events"));

   private static void execute(Runnable runnable) {
      executorService.submit(runnable);
   }

   // Kick LOCAL and REMOTE purged event depending on sender
   static void kickArtifactsPurgedEvent(final Sender sender, final Set<EventBasicGuidArtifact> artifactChanges) throws OseeCoreException {
      if (isDisableEvents()) {
         return;
      }
      eventLog("OEM2:kickArtifactsPurgedEvent " + sender + " - " + artifactChanges);
      Runnable runnable = new Runnable() {
         public void run() {
            // Kick LOCAL
            ArtifactEventManager.processArtifactChanges(sender, artifactChanges);

            // Kick REMOTE (If source was Local and this was not a default branch changed event
            try {
               if (sender.isLocal()) {
                  RemoteEventManager2.kick(new org.eclipse.osee.framework.messaging.event.res.event.NetworkArtifactPurgeEvent(
                        artifactChanges, sender.getNetworkSenderRes()));
               }
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      };
      execute(runnable);
   }

   // Kick LOCAL and REMOTE artifact change type depending on sender
   static void kickArtifactsChangeTypeEvent(final Sender sender, final Set<EventBasicGuidArtifact> artifactChanges, final String toArtifactTypeGuid) throws OseeCoreException {
      if (isDisableEvents()) {
         return;
      }
      eventLog("OEM2:kickArtifactsChangeTypeEvent " + sender + " - " + artifactChanges);
      Runnable runnable = new Runnable() {
         public void run() {
            // Kick LOCAL
            ArtifactEventManager.processArtifactChanges(sender, artifactChanges);

            // Kick REMOTE (If source was Local and this was not a default branch changed event
            try {
               if (sender.isLocal()) {
                  RemoteEventManager2.kick(new org.eclipse.osee.framework.messaging.event.res.event.NetworkArtifactChangeTypeEvent(
                        toArtifactTypeGuid, artifactChanges, sender.getNetworkSenderRes()));
               }
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      };
      execute(runnable);
   }

   // Kick LOCAL and REMOTE TransactionEvent
   static void kickTransactionEvent(final Sender sender, final Set<EventBasicGuidArtifact> artifactChanges) {
      if (isDisableEvents()) {
         return;
      }
      eventLog("OEM2:kickTransactionEvent #ModEvents: " + artifactChanges.size() + " - " + sender);
      Runnable runnable = new Runnable() {
         public void run() {
            // Roll-up change information
            try {
               // Log if this is a loopback and what is happening
               if (enableRemoteEventLoopback) {
                  OseeLog.log(
                        InternalEventManager.class,
                        Level.WARNING,
                        "OEM2:TransactionEvent Loopback enabled" + (sender.isLocal() ? " - Ignoring Local Kick" : " - Kicking Local from Loopback"));
               }

               // Kick LOCAL
               if (!enableRemoteEventLoopback || enableRemoteEventLoopback && sender.isRemote()) {
                  ArtifactEventManager.processArtifactChanges(sender, artifactChanges);
               }

               // Kick REMOTE (If source was Local and this was not a default branch changed event
               if (sender.isLocal()) {
                  List<IFrameworkEvent> events = generateNetworkFrameworkEvents(sender, artifactChanges);
                  RemoteEventManager2.kick(events);
               }
            } catch (Exception ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      };
      execute(runnable);
   }

   private static List<IFrameworkEvent> generateNetworkFrameworkEvents(Sender sender, Set<EventBasicGuidArtifact> artifactChanges) {
      List<IFrameworkEvent> events = new ArrayList<IFrameworkEvent>();
      for (EventBasicGuidArtifact artifactChange : artifactChanges) {
         events.add(generateNetworkSkynetArtifactEvent(artifactChange, sender));
      }
      // TODO handle relation changes
      return events;
   }

   private static IFrameworkEvent generateNetworkSkynetArtifactEvent(EventBasicGuidArtifact artifactChange, Sender sender) {
      if (artifactChange.getModType() == EventModType.Modified) {
         return new org.eclipse.osee.framework.messaging.event.res.event.NetworkArtifactModifiedEvent(
               artifactChange.getBranchGuid(), artifactChange.getArtTypeGuid(), artifactChange.getGuid(),
               ((EventModifiedBasicGuidArtifact) artifactChange).getAttributeChanges(), sender.getNetworkSenderRes());
      } else if (artifactChange.getModType() == EventModType.Added) {
         return new org.eclipse.osee.framework.messaging.event.res.event.NetworkArtifactAddedEvent(
               artifactChange.getBranchGuid(), artifactChange.getArtTypeGuid(), artifactChange.getGuid(),
               sender.getNetworkSenderRes());
      } else if (artifactChange.getModType() == EventModType.Deleted) {
         return new org.eclipse.osee.framework.messaging.event.res.event.NetworkArtifactDeletedEvent(
               artifactChange.getBranchGuid(), artifactChange.getArtTypeGuid(), artifactChange.getGuid(),
               sender.getNetworkSenderRes());
      } else {
         OseeLog.log(InternalEventManager.class, Level.SEVERE, "Unhandled artifactChange event: " + artifactChange);
      }
      return null;
   }

   /**
    * Add a priority listener. This should only be done for caches where they need to be updated before all other
    * listeners are called.
    */
   static void addPriorityListener(IEventListener listener) {
      if (listener == null) {
         throw new IllegalArgumentException("listener can not be null");
      }
      if (!priorityListeners.contains(listener)) {
         priorityListeners.add(listener);
      }
      eventLog("OEM2:addPriorityListener (" + priorityListeners.size() + ") " + listener);
   }

   static void addListener(IEventListener listener) {
      if (listener == null) {
         throw new IllegalArgumentException("listener can not be null");
      }
      if (!listeners.contains(listener)) {
         listeners.add(listener);
      }
      eventLog("OEM2:addListener (" + listeners.size() + ") " + listener);
   }

   static void removeListeners(IEventListener listener) {
      eventLog("OEM2:removeListener: (" + listeners.size() + ") " + listener);
      listeners.remove(listener);
      priorityListeners.remove(listener);
   }

   // This method clears all listeners. Should only be used for testing purposes.
   public static void removeAllListeners() {
      listeners.clear();
      priorityListeners.clear();
   }

   public static String getObjectSafeName(Object object) {
      try {
         return object.toString();
      } catch (Exception ex) {
         return object.getClass().getSimpleName() + " - exception on toString: " + ex.getLocalizedMessage();
      }
   }

   static boolean isDisableEvents() {
      return disableEvents;
   }

   static void setDisableEvents(boolean disableEvents) {
      InternalEventManager2.disableEvents = disableEvents;
   }

   static String getListenerReport() {
      List<String> listenerStrs = new ArrayList<String>();
      for (IEventListener listener : priorityListeners) {
         listenerStrs.add("Priority: " + getObjectSafeName(listener));
      }
      for (IEventListener listener : listeners) {
         listenerStrs.add(getObjectSafeName(listener));
      }
      String[] listArr = listenerStrs.toArray(new String[listenerStrs.size()]);
      Arrays.sort(listArr);
      return org.eclipse.osee.framework.jdk.core.util.Collections.toString("\n", (Object[]) listArr);
   }

   public static void eventLog(String output) {
      try {
         if (DEBUG) {
            OseeLog.log(InternalEventManager.class, Level.INFO, output);
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.INFO, ex);
      }
   }

   public static boolean isEnableRemoteEventLoopback() {
      return enableRemoteEventLoopback;
   }

   public static void setEnableRemoteEventLoopback(boolean enableRemoteEventLoopback) {
      InternalEventManager2.enableRemoteEventLoopback = enableRemoteEventLoopback;
   }
}
