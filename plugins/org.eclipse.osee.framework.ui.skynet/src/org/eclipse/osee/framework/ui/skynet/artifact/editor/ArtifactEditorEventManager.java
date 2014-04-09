/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.artifact.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.model.event.DefaultBasicGuidArtifact;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.event.EventUtil;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.event.listener.IAccessControlEventListener;
import org.eclipse.osee.framework.skynet.core.event.listener.IArtifactEventListener;
import org.eclipse.osee.framework.skynet.core.event.listener.IBranchEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.AccessControlEvent;
import org.eclipse.osee.framework.skynet.core.event.model.AccessControlEventType;
import org.eclipse.osee.framework.skynet.core.event.model.ArtifactEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEventType;
import org.eclipse.osee.framework.skynet.core.event.model.EventBasicGuidArtifact;
import org.eclipse.osee.framework.skynet.core.event.model.EventModType;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageManager;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * Common location for event handling for ArtifactExplorers in order to keep number of registrations and processing to a
 * minimum.
 * 
 * @author Donald G. Dunne
 */
public class ArtifactEditorEventManager implements IArtifactEventListener, IBranchEventListener, IAccessControlEventListener {

   List<IArtifactEditorEventHandler> handlers = new ArrayList<IArtifactEditorEventHandler>();
   static ArtifactEditorEventManager instance;

   public static void add(IArtifactEditorEventHandler iWorldEventHandler) {
      if (instance == null) {
         instance = new ArtifactEditorEventManager();
         OseeEventManager.addListener(instance);
      }
      instance.handlers.add(iWorldEventHandler);
   }

   public static void remove(IArtifactEditorEventHandler iWorldEventHandler) {
      if (instance != null) {
         instance.handlers.remove(iWorldEventHandler);
      }
   }

   @Override
   public List<? extends IEventFilter> getEventFilters() {
      // Can't filter cause this class handles all artifact explorers which can care about different branches
      return null;
   }

   @Override
   public void handleArtifactEvent(final ArtifactEvent artifactEvent, Sender sender) {
      for (IArtifactEditorEventHandler handler : new CopyOnWriteArrayList<IArtifactEditorEventHandler>(handlers)) {
         if (handler.isDisposed()) {
            handlers.remove(handler);
         }
      }
      EventUtil.eventLog("ArtifactEditorEventManager: handleArtifactEvent called [" + artifactEvent + "] - sender " + sender + "");
      final Collection<Artifact> modifiedArts =
         artifactEvent.getCacheArtifacts(EventModType.Modified, EventModType.Reloaded);
      final Collection<Artifact> relModifiedArts = artifactEvent.getRelCacheArtifacts();
      final Collection<EventBasicGuidArtifact> deletedPurgedChangedArts =
         artifactEvent.get(EventModType.Deleted, EventModType.Purged);
      final Collection<DefaultBasicGuidArtifact> relOrderChangedArtifacts = artifactEvent.getRelOrderChangedArtifacts();

      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (!deletedPurgedChangedArts.isEmpty()) {
               for (IArtifactEditorEventHandler handler : handlers) {
                  try {
                     if (!handler.isDisposed() && handler.getArtifactFromEditorInput() != null && deletedPurgedChangedArts.contains(handler.getArtifactFromEditorInput())) {
                        handler.closeEditor();
                     }
                  } catch (Exception ex) {
                     OseeLog.log(Activator.class, Level.SEVERE,
                        "Error processing event handler for deleted - " + handler, ex);
                  }
               }
            }
            if (!modifiedArts.isEmpty() || !relModifiedArts.isEmpty() || !relOrderChangedArtifacts.isEmpty()) {
               for (IArtifactEditorEventHandler handler : handlers) {
                  try {
                     if (!handler.isDisposed() && handler.getArtifactFromEditorInput() != null) {

                        if (modifiedArts.contains(handler.getArtifactFromEditorInput())) {
                           handler.refreshDirtyArtifact();
                        }

                        for (Artifact art : modifiedArts) {
                           if (art.isOfType(CoreArtifactTypes.AccessControlModel)) {
                              handler.refreshDirtyArtifact();
                           }
                        }

                        boolean relModified = relModifiedArts.contains(handler.getArtifactFromEditorInput());
                        boolean reorderArt = relOrderChangedArtifacts.contains(handler.getArtifactFromEditorInput());
                        if (relModified || reorderArt) {
                           handler.refreshRelations();
                           handler.getEditor().onDirtied();
                        }
                     }
                  } catch (Exception ex) {
                     OseeLog.log(Activator.class, Level.SEVERE,
                        "Error processing event handler for modified - " + handler, ex);
                  }
               }
            }
         }
      });
   }

   @Override
   public void handleBranchEvent(Sender sender, final BranchEvent branchEvent) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            for (IArtifactEditorEventHandler handler : handlers) {
               if (!handler.isDisposed()) {
                  if (branchEvent.getEventType() == BranchEventType.Committing || branchEvent.getEventType() == BranchEventType.Committed) {
                     if (handler.getArtifactFromEditorInput().getBranch().getGuid() == branchEvent.getBranchUuid()) {
                        handler.closeEditor();
                     }
                  }
               }
            }
         }
      });
   }

   @Override
   public void handleAccessControlArtifactsEvent(Sender sender, AccessControlEvent accessControlEvent) {
      try {
         if (accessControlEvent.getEventType() == AccessControlEventType.ArtifactsLocked || accessControlEvent.getEventType() == AccessControlEventType.ArtifactsUnlocked) {
            for (final IArtifactEditorEventHandler handler : handlers) {
               if (!handler.isDisposed()) {
                  if (accessControlEvent.getArtifacts().contains(handler.getArtifactFromEditorInput())) {
                     Displays.ensureInDisplayThread(new Runnable() {
                        @Override
                        public void run() {
                           handler.setMainImage(ArtifactImageManager.getImage(handler.getArtifactFromEditorInput()));
                        }
                     });
                  }
               }
            }
         }
      } catch (Exception ex) {
         // do nothing
      }
   }

}
