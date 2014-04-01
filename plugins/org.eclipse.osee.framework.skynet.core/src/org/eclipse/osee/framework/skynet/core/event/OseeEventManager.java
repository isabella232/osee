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
package org.eclipse.osee.framework.skynet.core.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.messaging.event.res.RemoteEvent;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.event.filter.BranchUuidEventFilter;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.event.listener.EventQosType;
import org.eclipse.osee.framework.skynet.core.event.listener.IEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.AccessControlEvent;
import org.eclipse.osee.framework.skynet.core.event.model.ArtifactEvent;
import org.eclipse.osee.framework.skynet.core.event.model.ArtifactEvent.ArtifactEventType;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event.model.EventBasicGuidArtifact;
import org.eclipse.osee.framework.skynet.core.event.model.EventModType;
import org.eclipse.osee.framework.skynet.core.event.model.RemoteEventServiceEventType;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;
import org.eclipse.osee.framework.skynet.core.event.model.TransactionEvent;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;
import org.eclipse.osee.framework.skynet.core.internal.event.EventListenerRegistry;

/**
 * Front end to OSEE events. Provides ability to add and remove different event listeners as well as the ability to kick
 * framework events.
 * 
 * @author Donald G. Dunne
 */
public final class OseeEventManager {

   private OseeEventManager() {
      // Utility Class
   }

   private static OseeEventService getEventService() throws OseeCoreException {
      return ServiceUtil.getEventService();
   }

   private static EventListenerRegistry getEventListeners() {
      return Activator.getEventListeners();
   }

   /**
    * Add a priority listener. This should only be done for caches where they need to be updated before all other
    * listeners are called.
    */
   public static void addPriorityListener(IEventListener listener) {
      getEventListeners().addListener(EventQosType.PRIORITY, listener);
   }

   public static void addListener(IEventListener listener) {
      getEventListeners().addListener(EventQosType.NORMAL, listener);
   }

   public static void removeAllListeners() {
      getEventListeners().clearAll();
   }

   public static void removeListener(IEventListener listener) {
      getEventListeners().removeListener(listener);
   }

   public static EventSystemPreferences getPreferences() {
      return Activator.getEventPreferences();
   }

   public static boolean isEventManagerConnected() {
      boolean result = false;
      try {
         OseeEventService eventService = getEventService();
         result = eventService.isConnected();
      } catch (Exception ex) {
         // Do Nothing;
      }
      return result;
   }

   public static String getConnectionDetails() {
      EventSystemPreferences preferences = getPreferences();
      StringBuilder sb = new StringBuilder();
      sb.append("oseeEventBrokerUri [" + preferences.getOseeEventBrokerUri() + "]");
      sb.append("eventDebug [" + preferences.getEventDebug() + "]");
      return sb.toString();
   }

   public static int getNumberOfListeners() {
      return getEventListeners().size();
   }

   // Kick LOCAL remote-event event
   public static void kickLocalRemEvent(Object source, RemoteEventServiceEventType remoteEventServiceEventType) throws OseeCoreException {
      getEventService().send(source, remoteEventServiceEventType);
   }

   //Kick LOCAL and REMOTE branch events
   public static void kickBranchEvent(Object source, BranchEvent branchEvent) throws OseeCoreException {
      getEventService().send(source, branchEvent);
   }

   // Kick LOCAL and REMOTE access control events
   public static void kickAccessControlArtifactsEvent(Object source, AccessControlEvent accessControlEvent) throws OseeCoreException {
      getEventService().send(source, accessControlEvent);
   }

   // Kick LOCAL and REMOTE transaction deleted event
   public static void kickTransactionEvent(Object source, final TransactionEvent transactionEvent) throws OseeCoreException {
      getEventService().send(source, transactionEvent);
   }

   // Kick LOCAL and REMOTE transaction event
   public static void kickPersistEvent(Object source, ArtifactEvent artifactEvent) throws OseeCoreException {
      getEventService().send(source, artifactEvent);
   }

   // Kick LOCAL transaction event
   public static void kickLocalArtifactReloadEvent(Object source, Collection<? extends Artifact> artifacts) throws OseeCoreException {
      if (isDisableEvents()) {
         return;
      }
      ArtifactEvent artifactEvent =
         new ArtifactEvent(artifacts.iterator().next().getBranch().getGuid(), ArtifactEventType.RELOAD_ARTIFACTS);
      artifactEvent.getArtifacts().addAll(EventBasicGuidArtifact.get(EventModType.Reloaded, artifacts));
      getEventService().send(source, artifactEvent);
   }

   public static boolean isDisableEvents() {
      return getPreferences().isDisableEvents();
   }

   // Turn off all event processing including LOCAL and REMOTE
   public static void setDisableEvents(boolean disableEvents) {
      getPreferences().setDisableEvents(disableEvents);
   }

   // Return report showing all listeners registered
   public static String getListenerReport() {
      String toReturn = null;
      if (OseeEventManager.isEventManagerConnected()) {
         toReturn = getEventListeners().toString();
      } else {
         toReturn = "Event system is NOT active";
      }
      return toReturn;
   }

   public static List<IEventFilter> getEventFiltersForBranch(final IOseeBranch branch) {
      try {
         List<IEventFilter> eventFilters = new ArrayList<IEventFilter>(2);
         eventFilters.add(new BranchUuidEventFilter(branch));
         return eventFilters;
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return null;
   }

   /////////////////////////////////// LEGACY TEST API ////////////////////////////////////////////
   // Only Used for Testing purposes
   public static void internalTestSendRemoteEvent(final RemoteEvent remoteEvent) throws OseeCoreException {
      getEventService().receive(remoteEvent);
   }

   // Only Used for Testing purposes
   public static void internalTestProcessBranchEvent(Sender sender, BranchEvent branchEvent) throws OseeCoreException {
      getEventService().receive(sender, branchEvent);
   }

   // Only Used for Testing purposes
   public static void internalTestProcessEventArtifactsAndRelations(Sender sender, ArtifactEvent artifactEvent) throws OseeCoreException {
      getEventService().receive(sender, artifactEvent);
   }

}
